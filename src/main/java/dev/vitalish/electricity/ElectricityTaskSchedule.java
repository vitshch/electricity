package dev.vitalish.electricity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ElectricityTaskSchedule {

    private Function<String, String> electricityTaskRunner;

    public ElectricityTaskSchedule(Function<String, String> electricityTaskRunner) {
        this.electricityTaskRunner = electricityTaskRunner;
    }

    @Scheduled(cron = "0 0 0/1 * * *")
    public void executeElectricityTask() {
        electricityTaskRunner.apply("");
    }

}
