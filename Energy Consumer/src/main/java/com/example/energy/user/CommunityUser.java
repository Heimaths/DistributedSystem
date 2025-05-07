package com.example.energy.user;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class CommunityUser {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public CommunityUser(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 5000)  // Alle 5 Sekunden eine Nachricht
    public void sendPeriodicEnergyUsage() {
        double kwh = calculateKwhBasedOnTimeOfDay();
        String message = String.format(
                java.util.Locale.US,
                "{\"type\": \"USER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        rabbitTemplate.convertAndSend("Consumer-energy-queue", message);
        System.out.println("Community User sent: " + message);
    }

    private double calculateKwhBasedOnTimeOfDay() {
        int hour = LocalDateTime.now().getHour();

        // Basisverbrauch (z. B. nachts gering)
        double base = 0.001;
        double peakFactor = 1.0;

        // Morgens (6-9 Uhr) und abends (17-21 Uhr) höherer Verbrauch
        if ((hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 21)) {
            peakFactor = 2.0;  // In Peak-Zeiten doppelt so viel Verbrauch
        }

        return base * peakFactor + (0.001 * random.nextDouble());  // Minimalrauschen
    }
} 