package dev.isxander.deckapi.impl;

public record JSBooleanResult(
        JSBooleanType result
) {
    public JSBooleanResult(boolean result) {
        this(new JSBooleanType("boolean", result));
    }

    public boolean getValue() {
        return result.value();
    }

    public record JSBooleanType(
            String type,
            boolean value
    ) {}
}
