package com.example.energy.usage.service;

import com.example.energy.usage.model.EnergyUsage;
import com.example.energy.usage.repository.EnergyUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
@Slf4j
public class UsageService {

    private final EnergyUsageRepository repository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;  // neu: zum Senden der Update-Nachricht

    public UsageService(EnergyUsageRepository repository,
                        ObjectMapper objectMapper,
                        RabbitTemplate rabbitTemplate) {
        this.repository     = repository;
        this.objectMapper   = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "Producer-energy-queue")
    @Transactional
    public void handleProducerMessage(String message) {
        processMessage(message, true);
    }

    @RabbitListener(queues = "Consumer-energy-queue")
    @Transactional
    public void handleConsumerMessage(String message) {
        processMessage(message, false);
    }

    private void processMessage(String message, boolean isProducer) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            double kwh = jsonNode.get("kwh").asDouble();
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            EnergyUsage usage = repository.findByHour(hourDateTime)
                    .orElseGet(() -> {
                        EnergyUsage u = new EnergyUsage();
                        u.setHour(hourDateTime);
                        u.setCommunityProduced(0.0);
                        u.setCommunityUsed(0.0);
                        u.setGridUsed(0.0);
                        return u;
                    });

            if (isProducer) {
                usage.setCommunityProduced(usage.getCommunityProduced() + kwh);
            } else {
                usage.setCommunityUsed(usage.getCommunityUsed() + kwh);
                if (usage.getCommunityUsed() > usage.getCommunityProduced()) {
                    double excess = usage.getCommunityUsed() - usage.getCommunityProduced();
                    usage.setGridUsed(usage.getGridUsed() + excess);
                }
            }

            repository.save(usage);
            log.info("Updated {} usage for hour {}: +{} kWh",
                    isProducer ? "producer" : "consumer",
                    hourDateTime, kwh);

            // **Neu**: nach jedem Aggregat-Update eine Kurzmeldung in die Usage-update-queue
            String updateMsg = String.format(
                    Locale.US,
                    "{\"datetime\":\"%s\"}",
                    hourDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            rabbitTemplate.convertAndSend("Usage-update-queue", updateMsg);
            log.info("Sent usage-update notification for hour {}", hourDateTime);

        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
