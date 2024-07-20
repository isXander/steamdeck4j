package dev.isxander.deckapi.api;

public class StateUtils {
    public static float short2float(short s) {
        // we need to do this since signed short range / 2 != 0
        return clampedMap(s, Short.MIN_VALUE, 0, -1f, 0f)
                + clampedMap(s, 0, Short.MAX_VALUE, 0f, 1f);
    }

    public static float clampedMap(float input, float inputMin, float inputMax, float outputMin, float outputMax) {
        return clampedLerp(outputMin, outputMax, inverseLerp(input, inputMin, inputMax));
    }

    public static float clampedLerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float inverseLerp(float a, float b, float value) {
        return (value - a) / (b - a);
    }
}
