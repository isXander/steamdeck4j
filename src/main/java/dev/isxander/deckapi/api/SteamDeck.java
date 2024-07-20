package dev.isxander.deckapi.api;

import dev.isxander.deckapi.impl.SteamDeckImpl;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface SteamDeck extends Closeable {
    static SteamDeck create(String url) {
        return new SteamDeckImpl(url);
    }

    ControllerState getControllerState();

    ControllerInfo getControllerInfo();

    CompletableFuture<Void> poll();
}
