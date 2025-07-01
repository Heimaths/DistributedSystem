package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyPercentage;
import com.example.energy.percentage.model.EnergyUsage;
import com.example.energy.percentage.repository.EnergyPercentageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Dieser Service lauscht jetzt auf die neue Queue "Usage-update-queue",
 * verarbeitet jede eingehende Stunden-Update-Meldung und speichert die
 * Prozentwerte in energy_percentage.
 */
@Service
@Slf4j
public class PercentageService {

    private final UsageDataService usageDataService;
    private final EnergyPercentageRepository percentageRepo;

    public PercentageService(UsageDataService usageDataService,
                             EnergyPercentageRepository percentageRepo) {
        this.usageDataService = usageDataService;
        this.percentageRepo   = percentageRepo;
    }

    /**
     * Neuer Listener: reagiert auf jede Nachricht in der Usage-update-queue
     * ({"datetime":"2025-07-01T14:00:00"}).
     */
    @RabbitListener(queues = "Usage-update-queue")
    @Transactional
    public void handleUsageUpdate(String message) {
        try {
            processPercentage(message);
        } catch (Exception e) {
            log.error("Failed to process usage update [{}]: {}", message, e.getMessage(), e);
        }
    }

    private void processPercentage(String message) throws Exception {
        // Nur der Zeitstempel im JSON, z.B. {"datetime":"2025-07-01T14:00:00"}
        String datetimeText = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(message)
                .get("datetime").asText();

        LocalDateTime dt = LocalDateTime.parse(datetimeText)
                .truncatedTo(ChronoUnit.HOURS);

        Optional<EnergyUsage> optUsage = usageDataService.getUsageData(dt);
        if (optUsage.isEmpty()) {
            log.warn("Keine Usage-Daten für {}", dt);
            return;
        }
        EnergyUsage usage = optUsage.get();

        double produced = usage.getCommunityProduced();
        double used     = usage.getCommunityUsed();
        double grid     = usage.getGridUsed();

        double communityDepleted = produced > 0
                ? Math.min((used / produced) * 100, 100.0)
                : 100.0;
        double totalUsage = used + grid;
        double gridPortion = totalUsage > 0
                ? (grid / totalUsage) * 100
                : 0.0;

        EnergyPercentage p = percentageRepo.findByHour(dt)
                .orElseGet(() -> {
                    EnergyPercentage e = new EnergyPercentage();
                    e.setHour(dt);
                    return e;
                });
        p.setCommunityDepleted(communityDepleted);
        p.setGridPortion(gridPortion);
        percentageRepo.save(p);

        log.info("Saved percentage for {} → depleted={}%, gridPortion={}%",
                dt, communityDepleted, gridPortion);
    }
}
