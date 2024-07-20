package dev.isxander.deckapi.impl;

public record JSStringResult(
        JSStringType result
) {
    public JSStringResult(String result) {
        this(new JSStringType("string", result));
    }

    public String getValue() {
        return result.value();
    }

    public record JSStringType(
            String type,
            String value
    ) {}
}
