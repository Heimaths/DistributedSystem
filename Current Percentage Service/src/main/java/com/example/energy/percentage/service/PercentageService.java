/* package com.example.energy.percentage.service;

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

    public PercentageService(EnergyPercentageRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "energy-queue")
    @Transactional
    public void processEnergyMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            // Hole oder erstelle einen neuen Eintrag für die aktuelle Stunde
            EnergyPercentage percentage = repository.findByHour(hourDateTime)
                    .orElse(new EnergyPercentage());

            if (percentage.getId() == null) {
                percentage.setHour(hourDateTime);
                percentage.setCommunityDepleted(0.0);
                percentage.setGridPortion(0.0);
            }

            // Berechne die Prozentsätze basierend auf den empfangenen Daten
            double communityProduced = jsonNode.get("community_produced").asDouble();
            double communityUsed = jsonNode.get("community_used").asDouble();
            double gridUsed = jsonNode.get("grid_used").asDouble();

            // Berechne Community Depletion (wie viel Prozent der Community-Produktion genutzt wurde)
            double communityDepleted = communityProduced > 0 ? 
                    Math.min((communityUsed / communityProduced) * 100, 100.0) : 100.0;

            // Berechne Grid Portion (Anteil des Netzbezugs am Gesamtverbrauch)
            double totalUsage = communityUsed + gridUsed;
            double gridPortion = totalUsage > 0 ? (gridUsed / totalUsage) * 100 : 0.0;

            percentage.setCommunityDepleted(communityDepleted);
            percentage.setGridPortion(gridPortion);

            repository.save(percentage);
            log.info("Updated energy percentages for hour: {}", hourDateTime);
        } catch (Exception e) {
            log.error("Error processing message for percentage calculation: {}", message, e);
        }
    }
}


 */

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

    public PercentageService(EnergyPercentageRepository repository, ObjectMapper objectMapper, UsageDataService usageDataService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.usageDataService = usageDataService;
    }

    @RabbitListener(queues = "energy-queue")
    @Transactional
    public void processEnergyMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            JsonNode usageData = usageDataService.getUsageData(hourDateTime);
            if (usageData == null) {
                log.warn("No usage data found for hour: {}", hourDateTime);
                return;
            }

            double communityProduced = usageData.get("community_produced").asDouble();
            double communityUsed = usageData.get("community_used").asDouble();
            double gridUsed = usageData.get("grid_used").asDouble();

            double communityDepleted = communityProduced > 0
                    ? Math.min((communityUsed / communityProduced) * 100, 100.0)
                    : 100.0;

            double totalUsage = communityUsed + gridUsed;
            double gridPortion = totalUsage > 0 ? (gridUsed / totalUsage) * 100 : 0.0;

            EnergyPercentage percentage = repository.findByHour(hourDateTime)
                    .orElse(new EnergyPercentage());

            if (percentage.getId() == null) {
                percentage.setHour(hourDateTime);
            }

            percentage.setCommunityDepleted(communityDepleted);
            percentage.setGridPortion(gridPortion);
            log.info("Saving: hour={}, depleted={}%, gridPortion={}%", hourDateTime, communityDepleted, gridPortion);

            repository.save(percentage);
            log.info("Updated energy percentages for hour: {}", hourDateTime);
        } catch (Exception e) {
            log.error("Error processing message for percentage calculation", e);
        }
    }
}





