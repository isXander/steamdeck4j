package dev.isxander.deckapi.api;

import dev.isxander.deckapi.impl.SteamDeckImpl;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface SteamDeck extends Closeable {
    String DEFAULT_URL = "http://localhost:8080";

    static SteamDeck create(String url) {
        try {
            return new SteamDeckImpl(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static SteamDeck create() {
        return create(DEFAULT_URL);
    }

    ControllerState getControllerState();

    ControllerInfo getControllerInfo();

    CompletableFuture<Void> openModalKeyboard(boolean enterDismissesKeyboard);

    CompletableFuture<Path> doSteamScreenshot(Path screenshotPath, String caption);

    CompletableFuture<Path> getScreenshotPath();

    CompletableFuture<Void> poll();
}
