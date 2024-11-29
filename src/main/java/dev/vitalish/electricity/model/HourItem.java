package dev.vitalish.electricity.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.vitalish.electricity.parser.ElectricityStatusDeserializer;
import dev.vitalish.electricity.parser.HourDeserializer;

import java.time.LocalTime;

public record HourItem(
        @JsonDeserialize(using = HourDeserializer.class)
        LocalTime hour,
        @JsonDeserialize(using = ElectricityStatusDeserializer.class)
        boolean electricity,
        String description,
        int periodLimitValue
) {
}
