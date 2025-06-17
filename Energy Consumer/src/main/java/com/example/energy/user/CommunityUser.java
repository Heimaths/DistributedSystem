package com.example.energy.user;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @Service
 * Markiert diese Klasse als Spring-Bean im Service-Layer.
 * Spring managed hier die Instanz und ihre Abhängigkeiten.
 */
@Service
public class CommunityUser {

    // RabbitTemplate: Spring-Wrapper um Nachrichten an RabbitMQ zu senden
    private final RabbitTemplate rabbitTemplate;
    // Random für einen kleinen Verbrauchs-„Rausch“-Effekt
    private final Random random = new Random();

    /**
     * Konstruktor-Injektion:
     * Spring übergibt hier automatisch das RabbitTemplate.
     * Vorteil: Klasse ist leichter testbar (Dependencies können gemockt werden)
     */
    public CommunityUser(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * @Scheduled(fixedRate = 5000)
     * Diese Annotation sorgt dafür, dass die Methode alle 5 Sekunden
     * automatisch aufgerufen wird (fixedRate in Millisekunden).
     * Use Case: Simuliere periodische Nutzer-„Verbrauchs“-Nachrichten.
     */
    @Scheduled(fixedRate = 5000)  // Alle 5 Sekunden eine Nachricht senden
    public void sendPeriodicEnergyUsage() {
        // Berechne kWh-Verbrauch basierend auf Tageszeit
        double kwh = calculateKwhBasedOnTimeOfDay();

        // Baue das JSON-String-Nachricht:
        // - type = "USER" (Kennzeichnung als Verbrauchernachricht)
        // - association = "COMMUNITY" (Gehört zur Community-Gruppe)
        // - kwh = Verbrauchswert mit 3 Nachkommastellen
        // - datetime = aktueller Zeitstempel im ISO-Format
        String message = String.format(
                java.util.Locale.US,
                "{\"type\": \"USER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        // Sende die Nachricht in die RabbitMQ-Queue "Consumer-energy-queue"
        rabbitTemplate.convertAndSend("Consumer-energy-queue", message);

        // Einfaches Log auf der Konsole, um zu sehen, dass gesendet wurde
        System.out.println("Community User sent: " + message);
    }

    /**
     * Berechnet den kWh-Verbrauch abhängig von der aktuellen Stunde:
     * - Basisverbrauch = 0.001 kWh
     * - Peak-Zeiten (Morgen 6–9 Uhr, Abend 17–21 Uhr) -> doppelter Verbrauch
     * - Zufälliges kleines Rauschen, damit Werte nicht immer exakt gleich sind
     */
    private double calculateKwhBasedOnTimeOfDay() {
        int hour = LocalDateTime.now().getHour();  // Aktuelle Stunde (0–23)

        double base = 0.001;     // Basisverbrauch
        double peakFactor = 1.0; // Standard-Faktor

        // Morgens und abends mehr Verbrauch (Simulierung realer Spitzenlast)
        if ((hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 21)) {
            peakFactor = 2.0;     // Verbrauch verdoppeln in Peak-Zeiten
        }

        // Endergebnis = Basis * Faktor + kleines zufälliges Rauschen
        return base * peakFactor + (0.001 * random.nextDouble());
    }
}
