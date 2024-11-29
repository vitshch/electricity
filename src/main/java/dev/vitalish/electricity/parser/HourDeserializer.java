package dev.vitalish.electricity.parser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalTime;

public class HourDeserializer extends StdDeserializer<LocalTime> {

    public HourDeserializer() {
        this(null);
    }

    protected HourDeserializer(JavaType type) {
        super(type);
    }

    @Override
    public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        var hourString = jsonParser.getValueAsString();
        var hour = hourString.split("-")[0];
        return LocalTime.of(Integer.parseInt(hour), 0);
    }
}
