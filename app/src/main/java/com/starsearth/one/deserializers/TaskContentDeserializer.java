package com.starsearth.one.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.starsearth.one.domain.TaskContent;

import java.lang.reflect.Type;

public class TaskContentDeserializer implements JsonDeserializer<TaskContent> {

    @Override
    public TaskContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jobject = json.getAsJsonObject();

        return new TaskContent(
                jobject.get("question").getAsString(),
                jobject.get("isTapSwipe").getAsBoolean(),
                jobject.get("isTrue").getAsBoolean(),
                jobject.get("explanation").getAsString()
        );
    }
}
