package dev.isxander.deckapi.api;

import dev.isxander.deckapi.impl.SteamDeckImpl;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface SteamDeck extends Closeable {
    String DEFAULT_URL = "http://steamdeck:8080";

    static SteamDeck create(String url) {
        return new SteamDeckImpl(url);
    }

    static SteamDeck create() {
        return create(DEFAULT_URL);
    }

    ControllerState getControllerState();

    ControllerInfo getControllerInfo();

    CompletableFuture<Void> poll();
}
