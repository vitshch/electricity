package dev.vitalish.electricity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ElectricitySchedule(
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
        LocalDateTime scheduleApprovedSince,
        LocalDateTime currentDate,
        @JsonProperty("hoursList")
        List<HourItem> availabilityHors,
        String eventDate
) {
}
