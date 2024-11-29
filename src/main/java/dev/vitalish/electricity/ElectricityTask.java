package dev.vitalish.electricity;

import dev.vitalish.electricity.service.ElectricityScheduleService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ElectricityTask {

    private static final String[] addresses = new String[]{
            "Івано-Франківськ,Бельведерська,25а"
//            "Сівка-Калуська,Височана,37"
    };

    private final ElectricityScheduleService electricityScheduleService;

    public ElectricityTask(ElectricityScheduleService electricityScheduleService) {
        this.electricityScheduleService = electricityScheduleService;
    }


    @Bean
    public Function<String, String> electricityTaskRunner() {
        return input -> {
            System.out.println("Executing Electricity Task");
            for (String address : addresses) {
                electricityScheduleService.processElectricityAddress(address);
            }
            return "Electricity Task Completed";
        };
    }

}
