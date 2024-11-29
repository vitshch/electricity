package dev.vitalish.electricity.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import dev.vitalish.electricity.model.Electricity;
import dev.vitalish.electricity.model.HourItem;
import dev.vitalish.electricity.service.google.GoogleCalendarApIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final ZoneId TZ = ZoneId.of("Europe/Kiev");

    private final String calendarId;
    private final String eventSummary;
    private final GoogleCalendarApIService googleCalendarApIService;

    public GoogleCalendarService(
            @Value("${google.calendar.id}")
            String calendarId,
            @Value("${event.summary}")
            String eventSummary,
            GoogleCalendarApIService googleCalendarApIService
    ) {
        this.calendarId = calendarId;
        this.eventSummary = eventSummary;
        this.googleCalendarApIService = googleCalendarApIService;
    }

    public void processEvents(LocalDate date, Set<HourItem> hourItems, Electricity electricity) {
        var calendarService = googleCalendarApIService.getCalendarService();
        cleanEventsForDate(date, electricity, calendarService);

        var electricityOutages = hourItems.stream()
                .filter(hi -> !hi.electricity())
                .map(hourItem -> createEvent(date, electricity, hourItem))
                .collect(Collectors.toSet());
        electricityOutages.forEach(event -> insertEvent(calendarService, event));
    }

    private void cleanEventsForDate(LocalDate date, Electricity electricity, Calendar calendarService) {
        var toBeRemoved = getEvents(calendarService, date).stream()
                .filter(event -> Objects.equals(electricity.address(), event.getLocation()))
                .collect(Collectors.toSet());
        toBeRemoved.forEach(event -> deleteEvent(calendarService, event));
    }

    private List<Event> getEvents(Calendar calendarService, LocalDate date) {
        try {
            return calendarService.events()
                    .list(calendarId)
                    .setMaxResults(200)
                    .setTimeMin(toDateTime(date))
                    .setTimeMax(toDateTime(date.plus(1, ChronoUnit.DAYS)))
                    .execute().getItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteEvent(Calendar calendarService, Event event) {
        try {
            calendarService.events().delete(calendarId, event.getId()).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertEvent(Calendar calendarService, Event event) {
        try {
            var execute = calendarService.events().insert(calendarId, event).execute();
            log.debug("Inserted new Event: {}", execute);
        } catch (IOException e) {
            throw new RuntimeException("Unable to insert new Event", e);
        }
    }

    private Event createEvent(LocalDate currentDate, Electricity electricity, HourItem hourItem) {
        return new Event()
                .setSummary(eventSummary.formatted(electricity.queue()))
                .setLocation(electricity.address())
                .setDescription(electricity.note())
                .setReminders(getReminders())
                .setColorId(electricity.queue().toString())
                .setStart(calculateEventStart(currentDate, hourItem))
                .setEnd(calculateEventEnd(currentDate, hourItem));
    }

    private static Event.Reminders getReminders() {
        Event.Reminders reminders = new Event.Reminders();
        EventReminder eventReminder = new EventReminder().setMethod("popup").setMinutes(10);
        reminders.setUseDefault(false);
        reminders.setOverrides(List.of(eventReminder));
        return reminders;
    }

    private static EventDateTime calculateEventStart(LocalDate currentDate, HourItem hourItem) {
        var startDateTime = LocalDateTime.of(currentDate, hourItem.hour());
        DateTime start = new DateTime(Date.from(startDateTime.atZone(TZ).toInstant()));
        return new EventDateTime().setDateTime(start);
    }

    private static EventDateTime calculateEventEnd(LocalDate currentDate, HourItem hourItem) {
        var endDateTime = LocalDateTime.of(currentDate, hourItem.hour().plus(1, ChronoUnit.HOURS));
        DateTime start = new DateTime(Date.from(endDateTime.atZone(TZ).toInstant()));
        return new EventDateTime().setDateTime(start);
    }

    private static DateTime toDateTime(LocalDate localDate) {
        var startDateTime = LocalDateTime.of(localDate, LocalTime.of(0, 0));
        return new DateTime(Date.from(startDateTime.atZone(TZ).toInstant()));
    }

}
