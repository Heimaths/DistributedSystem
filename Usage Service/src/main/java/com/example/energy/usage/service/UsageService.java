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

    @RabbitListener(queues = "energy-queue")
    @Transactional
    public void processEnergyMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.get("type").asText();
            String association = jsonNode.get("association").asText();
            double kwh = jsonNode.get("kwh").asDouble();
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());

            // Runde auf volle Stunde ab
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            EnergyUsage usage = repository.findByHour(hourDateTime)
                    .orElse(new EnergyUsage());

            if (usage.getId() == null) {
                usage.setHour(hourDateTime);
                usage.setCommunityProduced(0.0);
                usage.setCommunityUsed(0.0);
                usage.setGridUsed(0.0);
            }

            if ("PRODUCER".equals(type) && "COMMUNITY".equals(association)) {
                usage.setCommunityProduced(usage.getCommunityProduced() + kwh);
            } else if ("USER".equals(type) && "COMMUNITY".equals(association)) {
                usage.setCommunityUsed(usage.getCommunityUsed() + kwh);
                
                // Wenn mehr verbraucht als produziert wurde, erhöhe den Grid-Verbrauch
                double additionalGridUsage = 0.0;
                if (usage.getCommunityUsed() > usage.getCommunityProduced()) {
                    additionalGridUsage = kwh;
                }
                usage.setGridUsed(usage.getGridUsed() + additionalGridUsage);
            }

            repository.save(usage);
            log.info("Updated energy usage for hour: {}", hourDateTime);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
} 