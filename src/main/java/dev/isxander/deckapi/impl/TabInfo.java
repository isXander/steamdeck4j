package dev.isxander.deckapi.impl;

public record TabInfo(
        String description,
        String devtoolsFrontendUrl,
        String id,
        String title,
        String type,
        String url,
        String webSocketDebuggerUrl
) {}
