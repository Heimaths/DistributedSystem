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

@Service              // Markiert diese Klasse als Spring Service
@Slf4j                // Ermöglicht Logging über log.info(), log.error() usw.
public class UsageService {

    private final EnergyUsageRepository repository;     // Zugriff auf die Datenbank (energy_usage)
    private final ObjectMapper objectMapper;            // Zum Parsen von JSON-Nachrichten

    public UsageService(EnergyUsageRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // Empfängt Nachrichten aus der Producer-Queue
    @RabbitListener(queues = "Producer-energy-queue")
    @Transactional // schreibt entweder alle oder keinen Eintrag in die DB, Spring rollt alle Änderungen zurück, sobald ein Fehler auftaucht
    public void handleProducerMessage(String message) {
        processMessage(message, true);  // true = Nachricht kommt vom Produzenten
    }

    // Empfängt Nachrichten aus der Consumer-Queue
    @RabbitListener(queues = "Consumer-energy-queue")
    @Transactional
    public void handleConsumerMessage(String message) {
        processMessage(message, false);  // false = Nachricht kommt vom Verbraucher
    }

    // Verarbeitet eingehende JSON-Nachrichten für Producer und Consumer
    private void processMessage(String message, boolean isProducer) {
        try {
            // JSON-Nachricht in Objekt umwandeln
            JsonNode jsonNode = objectMapper.readTree(message);
            double kwh = jsonNode.get("kwh").asDouble();  // Menge in Kilowattstunden
            LocalDateTime dateTime = LocalDateTime.parse(jsonNode.get("datetime").asText());
            LocalDateTime hourDateTime = dateTime.truncatedTo(ChronoUnit.HOURS);  // Runden auf ganze Stunde

            // Entweder vorhandene Datenzeile holen oder neue Zeile für diese Stunde anlegen
            EnergyUsage usage = repository.findByHour(hourDateTime)
                    .orElseGet(() -> {
                        EnergyUsage newUsage = new EnergyUsage();
                        newUsage.setHour(hourDateTime);
                        newUsage.setCommunityProduced(0.0);
                        newUsage.setCommunityUsed(0.0);
                        newUsage.setGridUsed(0.0);
                        return newUsage;
                    });

            // PRODUCER: Erhöht die erzeugte Energiemenge
            if (isProducer) {
                usage.setCommunityProduced(usage.getCommunityProduced() + kwh);
            }
            // CONSUMER: Erhöht die verbrauchte Energie und ggf. Netzbezug
            else {
                usage.setCommunityUsed(usage.getCommunityUsed() + kwh);
                if (usage.getCommunityUsed() > usage.getCommunityProduced()) {
                    usage.setGridUsed(usage.getGridUsed() + (usage.getCommunityProduced() - usage.getCommunityUsed()));
                }
            }

            // Speichern in der Datenbank
            repository.save(usage);

            // Logging des Vorgangs
            log.info("Updated {} usage for hour {}: +{} kWh",
                    isProducer ? "producer" : "consumer",
                    hourDateTime, kwh);

        } catch (Exception e) {
            // Fehler beim Parsen oder Speichern
            log.error("Error processing message: {}", message, e);
        }
    }
}
