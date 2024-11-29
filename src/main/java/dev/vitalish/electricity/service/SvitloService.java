package dev.vitalish.electricity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.vitalish.electricity.model.Electricity;
import dev.vitalish.electricity.model.ElectricitySchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class SvitloService {

    public static final Logger log = LoggerFactory.getLogger(SvitloService.class);

    @Value("${svitlo.base.url}")
    private String svitloBaseUrl;

    private final HttpClient httpClient;

    public SvitloService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Electricity fetchSchedule(String address) {
        try {
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers
                    .ofString("accountNumber=&userSearchChoice=pob&address=" + address);
            HttpRequest request = HttpRequest.newBuilder(URI.create(svitloBaseUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("X-Forwarded-For", InetAddress.getLocalHost().getHostName())
                    .header("Accept", "application/json")
                    .POST(body)
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Svitlo response: {}", response);
            log.debug("Svitlo response body: {}", response.body());
            return parseSchedule(address, response.body());
        } catch (IOException | InterruptedException e) {
            log.error("Unable to fetch schedules", e);
            throw new RuntimeException("Unable to fetch schedules", e);
        }
    }

    private static Electricity parseSchedule(String address, String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode jsonNode = objectMapper.readTree(body);

        String note = jsonNode.get("current").get("note").toString();
        String queue = jsonNode.get("current").get("queue").toString();

        Map<LocalDate, ElectricitySchedule> schedules = new HashMap<>();

        var graphs = jsonNode.get("graphs");
        if (graphs != null) {
            var todayNode = graphs.get("today");
            if (todayNode != null) {
                var today = objectMapper.readValue(todayNode.toString(), ElectricitySchedule.class);
                schedules.put(LocalDate.now(), today);
            }
            var tomorrowNode = graphs.get("tomorrow");
            if (tomorrowNode != null) {
                var tomorrow = objectMapper.readValue(tomorrowNode.toString(), ElectricitySchedule.class);
                schedules.put(LocalDate.now().plus(1, ChronoUnit.DAYS), tomorrow);
            }
        }

        return new Electricity(address, note, Integer.parseInt(queue), schedules);
    }

}
