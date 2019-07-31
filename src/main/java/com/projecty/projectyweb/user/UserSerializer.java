package com.projecty.projectyweb.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class UserSerializer extends JsonSerializer<User> {
    @Override
    public void serialize(User value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeStringField("username", value.getUsername());
        gen.writeEndObject();
    }
}
