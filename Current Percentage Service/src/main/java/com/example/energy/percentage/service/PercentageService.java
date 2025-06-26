package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyPercentage;
import com.example.energy.percentage.model.EnergyUsage;
import com.example.energy.percentage.repository.EnergyPercentageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/*
Empfängt Energie-Nachrichten über RabbitMQ
Holt für die übermittelte Stunde den Verbrauch
Berechnet:
Wie viel selbst erzeugte Energie genutzt wurde
Wie viel Energie vom Stromnetz kam
Speichert das Ergebnis in EnergyPercentage (pro Stunde)
*/

@Service
@Slf4j // Logging über Lombok
public class PercentageService {

    // UsageDataService: Holt Verbrauchsdaten
    // percentageRepo: Speichert berechnete Prozentwerte
    private final UsageDataService usageDataService;
    private final EnergyPercentageRepository percentageRepo;

    public PercentageService(UsageDataService usageDataService,
                             EnergyPercentageRepository percentageRepo) {
        this.usageDataService = usageDataService;
        this.percentageRepo   = percentageRepo;
    }

    // Diese Methode wird automatisch aufgerufen, wenn eine neue Nachricht auf Producer-energy-queue empfangen wird.
    // @Transactional: alle DB-Operationen laufen in einer Transaktion
    @RabbitListener(queues = "Producer-energy-queue")
    @Transactional
    public void handleProducer(String message) throws JsonProcessingException {
        processPercentage(message);
    }
    // … handleConsumer identisch …

    private void processPercentage(String message) throws JsonProcessingException {
        // JSON wird eingelesen und der Zeitstempel auf volle Stunde gekürzt
        JsonNode json = new ObjectMapper().readTree(message);
        LocalDateTime dt = LocalDateTime.parse(json.get("datetime").asText())
                .truncatedTo(ChronoUnit.HOURS);

        // 1) Holt EnergyUsage per JPA
        Optional<EnergyUsage> optUsage = usageDataService.getUsageData(dt);
        if (optUsage.isEmpty()) {
            log.warn("Keine Usage-Daten für {}", dt);
            return;
        }
        EnergyUsage usage = optUsage.get();

        // 2) Lese direkt die Entity-Felder
        double communityProduced = usage.getCommunityProduced();
        double communityUsed     = usage.getCommunityUsed();
        double gridUsed          = usage.getGridUsed();

        // 3) Berechne Prozente
        // communityDepleted → Wie viel der selbst erzeugten Energie wurde verbraucht?
        // gridPortion → Welcher Anteil des Gesamtverbrauchs kam aus dem Netz?
        double communityDepleted = communityProduced > 0
                ? Math.min((communityUsed / communityProduced) * 100, 100.0)
                : 100.0; // Berechnet, wie viel von der selbst erzeugten Energie verbraucht wurde. Wenn nichts erzeugt wurde: depleted = 100%
        double totalUsage = communityUsed + gridUsed;
        double gridPortion = totalUsage > 0
                ? (gridUsed / totalUsage) * 100
                : 0.0;

        // 4) Speichere oder update die EnergyPercentage-Entity
        EnergyPercentage p = percentageRepo.findByHour(dt)
                .orElseGet(() -> {
                    EnergyPercentage e = new EnergyPercentage();
                    e.setHour(dt);
                    return e;
                });
        p.setCommunityDepleted(communityDepleted);
        p.setGridPortion(gridPortion);
        percentageRepo.save(p);

        log.info("Gespeichert: {} – depleted={}%, gridPortion={}%",
                dt, communityDepleted, gridPortion);
    }
}
