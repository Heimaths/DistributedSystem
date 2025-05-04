package com.example.energy.percentage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class PercentageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PercentageServiceApplication.class, args);
    }
} 