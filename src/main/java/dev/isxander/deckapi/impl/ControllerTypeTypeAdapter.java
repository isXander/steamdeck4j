package dev.isxander.deckapi.impl;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.isxander.deckapi.api.ControllerType;

import java.io.IOException;

public class ControllerTypeTypeAdapter extends TypeAdapter<ControllerType> {
    @Override
    public ControllerType read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            return ControllerType.UNKNOWN;
        }

        return ControllerType.byId(jsonReader.nextInt());
    }

    @Override
    public void write(JsonWriter jsonWriter, ControllerType controllerType) throws IOException {
        if (controllerType == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.value(controllerType.id());
    }
}
