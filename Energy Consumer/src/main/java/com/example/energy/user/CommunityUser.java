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


    @Scheduled(fixedRate = 5000)
    public void sendPeriodicEnergyUsage() {
        // Berechne kWh-Verbrauch basierend auf Tageszeit
        double kwh = calculateKwhBasedOnTimeOfDay();


        String message = String.format(
                java.util.Locale.US,
                "{\"type\": \"USER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        // Sende die Nachricht in die RabbitMQ-Queue "Consumer-energy-queue"
        rabbitTemplate.convertAndSend("Consumer-energy-queue", message);

        // Einfaches Log auf der Konsole, um zu sehen, dass gesendet wurde
        System.out.println("Community User sent: " + message);
    }


    private double calculateKwhBasedOnTimeOfDay() {
        int hour = LocalDateTime.now().getHour();  // Aktuelle Stunde (0–23)

        double base = 0.001;     // Basisverbrauch
        double peakFactor = 1.0; // Standard-Faktor

        // Morgens und abends mehr Verbrauch (Simulierung realer Spitzenlast)
        if ((hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 21)) {
            peakFactor = 2.0;     // Verbrauch verdoppeln in Peak-Zeiten
        }

        // Endergebnis = Basis * Faktor + kleines zufälliges Rauschen
        return base * peakFactor + (0.001 * random.nextDouble());
    }
}
