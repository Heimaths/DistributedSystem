package com.example.energy.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class CommunityConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public CommunityConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 5000)  // alle 5 Sekunden eine Nachricht senden
    public void sendPeriodicEnergyMessage() {
        double kwh = getRandomKwh();
        String message = String.format(
                "{\"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("Automatically sent: " + message);
    }

    private double getRandomKwh() {
        return 0.001 + (0.004 * random.nextDouble());  // Werte zwischen 0.001 und 0.005 kWh
    }

    public void sendEnergyMessage(String message) {
    }
}