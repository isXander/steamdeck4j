package dev.isxander.deckapi.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.isxander.deckapi.api.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;
import net.platinumdigitalgroup.jvdf.VDFWriter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SteamDeckImpl implements SteamDeck {
    private static final Logger LOGGER = LoggerFactory.getLogger("SteamDeck4j");

    private final HttpClient httpClient;
    private final JSTab sharedJsContext;
    private final Gson gson;

    private ControllerState currentState = ControllerState.ZERO;
    private @Nullable ControllerInfo deckInfo;
    private Long appId;

    public SteamDeckImpl(String url) throws SteamDeckException {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ControllerType.class, new ControllerTypeTypeAdapter())
                .create();

        String tabsUrl = "%s/json".formatted(url);

        // Get the list of tabs (debuggable pages) from the CEF debugger
        HttpRequest tabsRequest = HttpRequest.newBuilder()
                .uri(URI.create(tabsUrl))
                .timeout(Duration.of(5, ChronoUnit.SECONDS))
                .GET()
                .build();

        LOGGER.info("Requesting tabs from CEF at {}", tabsUrl);
        HttpResponse<String> tabsResponse;
        try {
            tabsResponse = httpClient
                    .send(tabsRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new SteamDeckException("Failed to talk to CEF", e);
        }

        if (tabsResponse.statusCode() != 200) {
            throw new SteamDeckException(
                    "Failed to talk to CEF with code %d. PLEASE ENSURE DECKY IS RUNNING!"
                            .formatted(tabsResponse.statusCode())
            );
        }

        TabInfo[] tabs = gson.fromJson(tabsResponse.body(), TabInfo[].class);

        // Find the SharedJSContext tab, which contains the `SteamClient` API object
        TabInfo sharedJsContextTabInfo = Arrays.stream(tabs)
                .filter(tab -> "SharedJSContext".equals(tab.title()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find SharedJSContext tab"));
        String wsUrl = sharedJsContextTabInfo.webSocketDebuggerUrl();
        this.sharedJsContext = JSTab.open(wsUrl, httpClient).join();
        LOGGER.info("Successfully connected to SharedJSContext tab");

        // Set up state listeners
        System.out.println(this.sharedJsContext.eval(
                """
                        // we don't want to keep adding more and more listeners between launches when not closed gracefully
                        if (window.controlifyListUnregister) {
                            window.controlifyListUnregister.unregister();
                        }
                        if (window.controlifyStateUnregister) {
                            window.controlifyStateUnregister.unregister();
                        }

                        window.controlifyListUnregister = SteamClient.Input.RegisterForControllerListChanges((controllers) => {
                            window.controlifySteamDeckInfo = controllers.find((controller) => controller.eControllerType == 4);
                        });
                        
                        window.controlifyStateUnregister = SteamClient.Input.RegisterForControllerStateChanges((controllerStates) => {
                            for (state of controllerStates) {
                                if (state.unControllerIndex === window.controlifySteamDeckInfo.nControllerIndex) {
                                    window.controlifySteamDeckState = state;
                                }
                            }
                        });
                        
                        SteamUIStore.MainRunningAppID
                        """,
                JsonObject.class
        ).join());
    }

    @Override
    public ControllerState getControllerState() {
        return currentState;
    }

    @Override
    public ControllerInfo getControllerInfo() {
        return deckInfo;
    }

    @Override
    public CompletableFuture<Void> poll() {
        CompletableFuture<Void> stateResult = sharedJsContext.eval(
                """
                SteamClient.Input.RequestGyroActive(window.controlifySteamDeckInfo.nControllerIndex, true);
                JSON.stringify(window.controlifySteamDeckState)
                """,
                JSStringResult.class
        ).thenAccept(json -> {
            currentState = gson.fromJson(json.getValue(), ControllerState.class);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        CompletableFuture<Void> listResult = sharedJsContext.eval(
                "JSON.stringify(window.controlifySteamDeckInfo)",
                JSStringResult.class
        ).thenAccept(json -> {
            deckInfo = gson.fromJson(json.getValue(), ControllerInfo.class);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        return CompletableFuture.allOf(stateResult, listResult);
    }

    @Override
    public CompletableFuture<Void> openModalKeyboard(boolean enterDismissesKeyboard) {
        if (this.deckInfo == null) {
            throw new IllegalStateException("Cannot open modal keyboard without controller info. Make sure to poll first");
        }

        return sharedJsContext.eval(
                """
                SteamUIStore.OnModalKeyboardMessage({
                      nAppID: 0,
                      bChordInvoked: false,
                      bEnterDismissesKeyboard: $$,
                      nControllerIndex: $$,
                      nXPosition: 0,
                      nYPosition: 0
                })
                """,
                JsonObject.class,
                enterDismissesKeyboard, deckInfo.nControllerIndex()
        ).thenAccept(json -> {});
    }

    @Override
    public CompletableFuture<Path> doSteamScreenshot(Path screenshotPath, String caption) {
        return getScreenshotPath().thenCompose(gameScreenshotFolder -> {
            Path targetScreenshotPath = gameScreenshotFolder.resolve(screenshotPath.getFileName());

            BufferedImage image;
            try {
                Files.copy(screenshotPath, targetScreenshotPath);
                image = ImageIO.read(targetScreenshotPath.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // steam creates a thumbnail that (at least for 1280x800) is 6.4x smaller
            Image thumbnailImage = image.getScaledInstance((int) (image.getWidth() / 6.4), (int) (image.getHeight() / 6.4), BufferedImage.SCALE_SMOOTH);
            BufferedImage thumbnail = new BufferedImage(thumbnailImage.getWidth(null), thumbnailImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.drawImage(thumbnailImage, 0, 0, null);
            g2d.dispose();
            Path thumbnailPath = targetScreenshotPath.getParent()
                    .resolve("thumbnails")
                    .resolve(targetScreenshotPath.getFileName());
            try {
                Files.createDirectories(thumbnailPath.getParent());
                ImageIO.write(thumbnail, "png", thumbnailPath.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int screenshotAppId = (int)(appId & ~(-1 << 22));

            // database of screenshots in steam
            // removing /<gameid>/screenshots/ from path
            Path rootScreenshotFolder = gameScreenshotFolder.getParent().getParent();
            Path screenshotsVDF = rootScreenshotFolder.resolve("screenshots.vdf");
            String screenshotsVDFString = null;
            try {
                screenshotsVDFString = Files.readString(screenshotsVDF);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            VDFNode screenshotsNode = new VDFParser().parse(screenshotsVDFString).getSubNode("screenshots");
            VDFNode currentGameScreenshots = screenshotsNode.getSubNode(String.valueOf(screenshotAppId));

            int freeIndex = 0;
            while (currentGameScreenshots.getSubNode(String.valueOf(freeIndex)) != null) {
                freeIndex++;

                if (freeIndex > 50000) {
                    throw new IllegalStateException("Potential stack overflow. 50k screenshots?");
                }
            }

            long creationTime = System.currentTimeMillis() / 1000;

            VDFNode newScreenshot = new VDFNode();
            newScreenshot.put("type", "1");
            newScreenshot.put("filename", targetScreenshotPath.relativize(rootScreenshotFolder).toString());
            newScreenshot.put("thumbnail", thumbnailPath.relativize(rootScreenshotFolder).toString());
            newScreenshot.put("vrfilename", ""); // steam leaves this empty
            newScreenshot.put("imported", "1"); // let's tell the truth!
            newScreenshot.put("width", String.valueOf(image.getWidth()));
            newScreenshot.put("height", String.valueOf(image.getHeight()));
            newScreenshot.put("gameid", String.valueOf(screenshotAppId));
            newScreenshot.put("creation", String.valueOf(creationTime)); // unix timestamp
            newScreenshot.put("caption", caption);
            newScreenshot.put("Permissions", "2"); // i believe this represents the privacy level of the media, 2 is only me
            newScreenshot.put("hscreenshot", "18446744073709551615"); // steamclient.so seems to hardcode this value
            newScreenshot.put("publishedfileid", "0"); // unpublished to begin with

            currentGameScreenshots.put(String.valueOf(freeIndex), newScreenshot);

            try {
                Files.writeString(screenshotsVDF, new VDFWriter().write(screenshotsNode, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return sharedJsContext.eval(
                    """
                    const listeners = [
                        screenshotStore.OnScreenshotNotification,
                        appSpotlightStore.OnScreenshotNotification,
                        appActivityStore.OnScreenshotNotification
                    ];
                    const onScreenshotNotification = (notification) => {
                        listeners.forEach(listener => listener(notification));
                    };

                    onScreenshotNotification({
                        strOperation: 'started'
                    });
                    onScreenshotNotification({
                        strOperation: 'written',
                        details: {
                            bSpoilers: false,
                            bUploaded: false,
                            ePrivacy: 2,
                            hHandle: $$,
                            nAppID: $$,
                            nCreated: $$,
                            nHeight: $$,
                            nWidth: $$,
                            publishedFileID: "0",
                            strCaption: "$$",
                            strGameID: "$$",
                            strUrl: "https://steamloopback.host/screenshots/$$/screenshots/$$",
                            ugcHandle: 18446744073709551615
                        }
                    })
                    """,
                    JsonObject.class,
                    freeIndex, // hHandle
                    screenshotAppId, // nAppID
                    creationTime, // nCreated
                    image.getHeight(), image.getWidth(),  // nHeight, nWidth
                    caption, // strCaption
                    screenshotAppId, // strGameID
                    screenshotAppId, targetScreenshotPath.getFileName() // strUrl
            ).thenApply(json -> targetScreenshotPath);
        });
    }

    @Override
    public CompletableFuture<Path> getScreenshotPath() {
        return sharedJsContext.eval(
                """
                // non-steam games use 22-bit app ids for screenshots (thanks Valve)
                await SteamClient.Screenshots.GetLocalScreenshotPath($$, 0)
                """,
                JSStringResult.class,
                appId & ~(-1 << 22)
        ).thenApply(string -> Path.of(string.getValue()));
    }

    @Override
    public void close() {
        sharedJsContext.eval(
                """
                window.controlifyListUnregister.unregister()
                window.controlifyStateUnregister.unregister()
                SteamClient.Input.RequestGyroActive(window.controlifySteamDeckInfo.nControllerIndex, false);
                """,
                JsonObject.class
        ).join();

        sharedJsContext.close();
    }
}
