package com.openjiuwen.studio.agent.space.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ToStringDeserializer extends JsonDeserializer<String> {

    public static final ToStringDeserializer INSTANCE = new ToStringDeserializer();

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonToken jsonToken = jp.getCurrentToken();
        if (JsonToken.VALUE_NULL.equals(jsonToken)) {
            return null;
        } else if (JsonToken.VALUE_STRING.equals(jsonToken)) {
            return ctxt.readTree(jp).asText();
        }
        return ctxt.readTree(jp).toString();
    }
}
