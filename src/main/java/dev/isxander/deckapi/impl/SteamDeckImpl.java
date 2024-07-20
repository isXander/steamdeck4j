package dev.isxander.deckapi.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.isxander.deckapi.api.ControllerInfo;
import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.ControllerType;
import dev.isxander.deckapi.api.SteamDeck;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SteamDeckImpl implements SteamDeck {
    private final HttpClient httpClient;
    private final JSTab sharedJsContext;
    private final Gson gson;

    private ControllerState currentState = ControllerState.ZERO;
    private @Nullable ControllerInfo deckInfo;

    public SteamDeckImpl(String url) {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ControllerType.class, new ControllerTypeTypeAdapter())
                .create();

        String tabsUrl = "http://%s/json".formatted(url);

        // Get the list of tabs (debuggable pages) from the CEF debugger
        HttpRequest tabsRequest = HttpRequest.newBuilder()
                .uri(URI.create(tabsUrl))
                .GET()
                .build();
        HttpResponse<String> tabsResponse = httpClient
                .sendAsync(tabsRequest, HttpResponse.BodyHandlers.ofString())
                .join();
        TabInfo[] tabs = gson.fromJson(tabsResponse.body(), TabInfo[].class);

        // Find the SharedJSContext tab, which contains the `SteamClient` API object
        TabInfo sharedJsContextTabInfo = Arrays.stream(tabs)
                .filter(tab -> "SharedJSContext".equals(tab.title()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find SharedJSContext tab"));
        String wsUrl = sharedJsContextTabInfo.webSocketDebuggerUrl();
        this.sharedJsContext = JSTab.open(wsUrl, httpClient).join();

        // Set up state listeners
        this.sharedJsContext.eval(
                """
                window.controlifyListUnregister = window.SteamClient.Input.RegisterForControllerListChanges((controllers) => {
                    window.controlifySteamDeckInfo = controllers.find((controller) => controller.eControllerType == 4);
                });
                
                window.controlifyStateUnregister = window.SteamClient.Input.RegisterForControllerStateChanges((controllerStates) => {
                    for (state of controllerStates) {
                        if (state.unControllerIndex == window.controlifySteamDeckInfo.nControllerIndex) {
                            window.controlifySteamDeckState = state;
                        }
                    }
                });
                """.stripIndent(),
                JsonObject.class
        ).join();
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
                "JSON.stringify(window.controlifySteamDeckState)",
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
    public void close() throws IOException {
        sharedJsContext.eval("window.controlifyListUnregister.unregister()", JsonObject.class).join();
        sharedJsContext.eval("window.controlifyStateUnregister.unregister()", JsonObject.class).join();

        sharedJsContext.close();
    }
}
