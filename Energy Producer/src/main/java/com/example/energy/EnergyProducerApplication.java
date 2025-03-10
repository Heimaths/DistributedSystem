package com.example.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Diese Annotation sorgt dafür, dass @Scheduled Methoden laufen
public class EnergyProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyProducerApplication.class, args);
    }
}