package dev.vitalish.electricity.service;

import dev.vitalish.electricity.model.Electricity;
import dev.vitalish.electricity.model.HourItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ElectricityScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ElectricityScheduleService.class);

    private final SvitloService svitloService;
    private final GoogleCalendarService googleCalendarService;

    public ElectricityScheduleService(SvitloService svitloService, GoogleCalendarService googleCalendarService) {
        this.svitloService = svitloService;
        this.googleCalendarService = googleCalendarService;
    }

    public void processElectricityAddress(String address) {
        log.debug("Processing address: {}", address);
        var electricity = svitloService.fetchSchedule(address);
        processSchedules(electricity);
        log.debug("Processed: {}", address);
    }

    private void processSchedules(Electricity electricity) {
        var electricitySchedules = electricity.electricitySchedules();
        for (LocalDate date : electricitySchedules.keySet()) {
            Set<HourItem> outages = electricitySchedules.get(date).availabilityHors().stream()
                    .filter(hourItem -> !hourItem.electricity())
                    .collect(Collectors.toSet());
            googleCalendarService.processEvents(date, outages, electricity);
        }
    }

}
