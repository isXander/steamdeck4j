package dev.isxander.deckapi.impl;

import java.util.Map;

public record CEFOutgoingMessage(
        int id,
        String method,
        Map<String, Object> params
) {}
