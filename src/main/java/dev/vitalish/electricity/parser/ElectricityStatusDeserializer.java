package dev.vitalish.electricity.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ElectricityStatusDeserializer extends StdDeserializer<Boolean> {

    public ElectricityStatusDeserializer() {
        this(null);
    }

    protected ElectricityStatusDeserializer(JavaType valueType) {
        super(valueType);
    }

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        boolean booleanValue = jsonParser.getValueAsBoolean();
        return !booleanValue;
    }
}
