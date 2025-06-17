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

@Service      // Kennzeichnet diese Klasse als “Service” in Spring – Business-Logik-Layer
@Slf4j        // Erzeugt automatisch ein SLF4J-Log-Objekt “log” für Debug/Info/Error
public class PercentageService {

    // Repository-Paar für DB-Zugriff, ObjectMapper für JSON, und Service für Usage-Daten
    private final EnergyPercentageRepository repository;
    private final ObjectMapper objectMapper;
    private final UsageDataService usageDataService;

    /**
     * Konstruktor-Injektion: Spring gibt automatisch die Dependencies hier rein.
     * Vorteil: Felder können final sein, Klasse ist leichter testbar.
     */
    public PercentageService(EnergyPercentageRepository repository,
                             ObjectMapper objectMapper,
                             UsageDataService usageDataService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.usageDataService = usageDataService;
    }

    /**
     * 1. RabbitMQ-Messages vom “Producer-energy-queue” empfangen.
     *    Jede Nachricht landet in dieser Methode.
     *
     * @RabbitListener – sorgt für asynchrones Abonnieren der Queue.
     * @Transactional – öffnet eine DB-Transaktion, damit Saves/Rollsbacks automatisch laufen.
     */
    @RabbitListener(queues = "Producer-energy-queue")
    @Transactional
    public void handleProducer(String message) {
        processPercentage(message);
    }

    /**
     * 2. Gleiches für Consumer-Messages vom “Consumer-energy-queue”.
     *    Wir verarbeiten die Logik zentral in processPercentage().
     */
    @RabbitListener(queues = "Consumer-energy-queue")
    @Transactional
    public void handleConsumer(String message) {
        processPercentage(message);
    }

    /**
     * Kern-Logik: JSON einlesen, relevanten Timestamp extrahieren,
     * aus UsageDataService die Verbrauchs-/Erzeugungs-Daten holen,
     * Prozentwerte berechnen und in der DB speichern.
     *
     * @param message die rohe JSON-Nachricht von RabbitMQ
     */
    private void processPercentage(String message) {
        try {
            // 1. JSON-String in einen Tree parsen, um auf Felder zuzugreifen
            JsonNode jsonNode = objectMapper.readTree(message);

            // 2. “datetime” aus dem JSON auslesen und in LocalDateTime parsen
            LocalDateTime dateTime = LocalDateTime.parse(
                    jsonNode.get("datetime").asText()
            );

            // 3. Auf volle Stunde truncaten (z.B. 17:23 → 17:00),
            //    damit wir später DB-Einträge pro Stunde zusammenfassen.
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);

            // 4. Holt zu dieser Stunde die Usage-Daten (Produktion/Verbrauch)
            JsonNode usageData = usageDataService.getUsageData(hourDateTime);
            log.info("Requested usage data for hour: {}", hourDateTime);

            // 5. Wenn keine Daten vorhanden sind, abbrechen (Log-Warnung)
            if (usageData == null || usageData.isEmpty()) {
                log.warn("No usage data found for hour: {}", hourDateTime);
                return;
            }

            // 6. Werte aus dem JSON extrahieren (Default 0.0, falls Feld fehlt)
            double communityProduced = usageData
                    .path("community_produced").asDouble(0.0);
            double communityUsed     = usageData
                    .path("community_used").asDouble(0.0);
            double gridUsed          = usageData
                    .path("grid_used").asDouble(0.0);

            // 7. Prozent-Werte berechnen:
            //    - communityDepleted: Anteil genutzter Community-Energie, max 100%
            double communityDepleted = communityProduced > 0
                    ? Math.min((communityUsed / communityProduced) * 100, 100.0)
                    : 100.0;

            //    - gridPortion: Anteil Netzbezug an der Gesamt-Nutzung
            double totalUsage = communityUsed + gridUsed;
            double gridPortion = totalUsage > 0
                    ? (gridUsed / totalUsage) * 100
                    : 0.0;

            // 8. Lade existierenden Eintrag für diese Stunde ODER erstelle neuen
            EnergyPercentage percentage = repository
                    .findByHour(hourDateTime)
                    .orElseGet(() -> {
                        EnergyPercentage p = new EnergyPercentage();
                        p.setHour(hourDateTime);
                        return p;
                    });

            // 9. Werte setzen und speichern (persistiert durch Spring Data JPA)
            percentage.setCommunityDepleted(communityDepleted);
            percentage.setGridPortion(gridPortion);
            repository.save(percentage);

            log.info("Saved percentages for hour {}: depleted={}%, gridPortion={}%",
                    hourDateTime, communityDepleted, gridPortion);

        } catch (Exception e) {
            // 10. Fehler im JSON-Parsing, DB-Zugriff oder Usage-Service loggen
            log.error("Error processing percentage for message: {}", message, e);
        }
    }
}
