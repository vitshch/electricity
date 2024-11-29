package dev.vitalish.electricity.model;

import java.time.LocalDate;
import java.util.Map;

public record Electricity(
        String address,
        String note,
        Integer queue,
        Map<LocalDate, ElectricitySchedule> electricitySchedules
) {
}
