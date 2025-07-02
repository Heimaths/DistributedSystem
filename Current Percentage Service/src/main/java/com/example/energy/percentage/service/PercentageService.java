package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyPercentage;
import com.example.energy.percentage.model.EnergyUsage;
import com.example.energy.percentage.repository.EnergyPercentageRepository;
import com.example.energy.percentage.repository.EnergyUsageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
public class PercentageService {

    private final EnergyUsageRepository usageRepository;
    private final EnergyPercentageRepository percentageRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PercentageService(EnergyUsageRepository usageRepository,
                             EnergyPercentageRepository percentageRepo) {
        this.usageRepository = usageRepository;
        this.percentageRepo  = percentageRepo;
    }

    /**
     * Hört auf jede Nachricht in der Usage-update-queue
     * und berechnet die Prozentwerte basierend auf dem aktuellen
     * Datensatz in energy_usage.
     */
    @RabbitListener(queues = "Usage-update-queue")
    @Transactional
    public void handleUsageUpdate(String message) {
        try {
            // Extrahiere den Stunden-Timestamp aus {"datetime":"..."}
            String datetimeText = objectMapper
                    .readTree(message)
                    .get("datetime").asText();

            LocalDateTime dt = LocalDateTime.parse(datetimeText)
                    .truncatedTo(ChronoUnit.HOURS);

            // Direkter Zugriff auf usageRepository
            Optional<EnergyUsage> optUsage = usageRepository.findByHour(dt);
            if (optUsage.isEmpty()) {
                log.warn("Keine Usage-Daten für {}", dt);
                return;
            }
            EnergyUsage usage = optUsage.get();

            // Werte auslesen
            double produced = usage.getCommunityProduced();
            double used     = usage.getCommunityUsed();
            double grid     = usage.getGridUsed();

            // Prozentberechnung
            double communityDepleted = produced > 0
                    ? Math.min((used / produced) * 100, 100.0)
                    : 100.0;
            double totalUsage = used + grid;
            double gridPortion = totalUsage > 0
                    ? (grid / totalUsage) * 100
                    : 0.0;

            // Entity anlegen oder laden
            EnergyPercentage p = percentageRepo.findByHour(dt)
                    .orElseGet(() -> {
                        EnergyPercentage e = new EnergyPercentage();
                        e.setHour(dt);
                        return e;
                    });
            p.setCommunityDepleted(communityDepleted);
            p.setGridPortion(gridPortion);

            // Speichern
            percentageRepo.save(p);
            log.info("Saved percentage for {} → depleted={}%, gridPortion={}%",
                    dt, communityDepleted, gridPortion);

        } catch (Exception e) {
            log.error("Failed to process usage update [{}]: {}", message, e.getMessage(), e);
        }
    }
}
