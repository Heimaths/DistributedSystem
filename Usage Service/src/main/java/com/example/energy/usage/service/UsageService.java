package com.example.energy.usage.service;

import com.example.energy.usage.model.EnergyUsage;
import com.example.energy.usage.repository.EnergyUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class UsageService {

    private final EnergyUsageRepository repository;
    private final ObjectMapper objectMapper;

    public UsageService(EnergyUsageRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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
                        EnergyUsage newUsage = new EnergyUsage();
                        newUsage.setHour(hourDateTime);
                        newUsage.setCommunityProduced(0.0);
                        newUsage.setCommunityUsed(0.0);
                        newUsage.setGridUsed(0.0);
                        return newUsage;
                    });

            if (isProducer) {
                // PRODUCER → immer Community-Produktion
                usage.setCommunityProduced(usage.getCommunityProduced() + kwh);
            } else {
                // CONSUMER → immer Community-Verbrauch + ggf. Grid
                usage.setCommunityUsed(usage.getCommunityUsed() + kwh);
                if (usage.getCommunityUsed() > usage.getCommunityProduced()) {
                    usage.setGridUsed(usage.getGridUsed() + kwh);
                }
            }

            repository.save(usage);
            log.info("Updated {} usage for hour {}: +{} kWh",
                    isProducer ? "producer" : "consumer",
                    hourDateTime, kwh);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
