package com.example.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnergyConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyConsumerApplication.class, args);
    }
}