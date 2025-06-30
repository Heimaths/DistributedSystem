package com.example.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.example.energy.producer.CommunityProducer.displayWeatherData;

@SpringBootApplication
@EnableScheduling
public class EnergyProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyProducerApplication.class, args);
        displayWeatherData();
    }
}