// src/main/java/com/example/energy/percentage/service/PercentageService.java
package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyPercentage;
import com.example.energy.percentage.repository.EnergyPercentageRepository;
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
public class PercentageService {

    private final EnergyPercentageRepository repository;
    private final ObjectMapper objectMapper;
    private final UsageDataService usageDataService;

    public PercentageService(EnergyPercentageRepository repository,
                             ObjectMapper objectMapper,
                             UsageDataService usageDataService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.usageDataService = usageDataService;
    }

    // 1. Producer-Messages empfängt, extrahiert nur das datetime-Feld
    @RabbitListener(queues = "Producer-energy-queue")
    @Transactional
    public void handleProducer(String message) {
        processPercentage(message);
    }

    // 2. Consumer-Messages empfängt, extrahiert nur das datetime-Feld
    @RabbitListener(queues = "Consumer-energy-queue")
    @Transactional
    public void handleConsumer(String message) {
        processPercentage(message);
    }

    private void processPercentage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            // Daten aus UsageService abrufen
            JsonNode usageData = usageDataService.getUsageData(hourDateTime);
            log.info("Requested usage data for hour: {}", hourDateTime);

            if (usageData == null || usageData.isEmpty()) {
                log.warn("No usage data found for hour: {}", hourDateTime);
                return;
            }

            double communityProduced = usageData.path("community_produced").asDouble(0.0);
            double communityUsed     = usageData.path("community_used").asDouble(0.0);
            double gridUsed          = usageData.path("grid_used").asDouble(0.0);

            // Prozentwerte berechnen
            double communityDepleted = communityProduced > 0
                    ? Math.min((communityUsed / communityProduced) * 100, 100.0)
                    : 100.0;
            double totalUsage = communityUsed + gridUsed;
            double gridPortion = totalUsage > 0
                    ? (gridUsed / totalUsage) * 100
                    : 0.0;

            // Entity laden oder neu anlegen
            EnergyPercentage percentage = repository.findByHour(hourDateTime)
                    .orElseGet(() -> {
                        EnergyPercentage p = new EnergyPercentage();
                        p.setHour(hourDateTime);
                        return p;
                    });

            percentage.setCommunityDepleted(communityDepleted);
            percentage.setGridPortion(gridPortion);

            repository.save(percentage);
            log.info("Saved percentages for hour {}: depleted={}%, gridPortion={}%",
                    hourDateTime, communityDepleted, gridPortion);

        } catch (Exception e) {
            log.error("Error processing percentage for message: {}", message, e);
        }
    }
}
