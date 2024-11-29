package dev.vitalish.electricity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElectricityApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(ElectricityApplication.class, args);

        ElectricityTask electricityTask = context.getBean(
                "electricityTask", ElectricityTask.class
        );
        electricityTask.electricityTaskRunner().apply("");
    }

}
