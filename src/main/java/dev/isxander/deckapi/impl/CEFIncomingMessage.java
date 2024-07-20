package dev.isxander.deckapi.impl;

import com.google.gson.JsonElement;

public record CEFIncomingMessage(
        int id,
        JsonElement result // javascript object
) {}
