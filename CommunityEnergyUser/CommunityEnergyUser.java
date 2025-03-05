package com.example;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

/**
 * Eine eigenständige Java-Anwendung, die alle paar Sekunden
 * eine USER-Nachricht simuliert. Berücksichtigt Peak-Hours (z.B. morgens
 * und abends) für höheren Verbrauch.
 */
public class CommunityEnergyUser {

    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("CommunityEnergyUser gestartet ...");

        while (true) {
            try {
                // 1) Bestimme aktuelle Uhrzeit, um Peak-Hours zu erkennen
                LocalTime now = LocalTime.now();

                // Beispiel: 6-9 Uhr & 17-20 Uhr => Peak
                boolean isPeakHour = (now.getHour() >= 6 && now.getHour() < 9)
                        || (now.getHour() >= 17 && now.getHour() < 20);

                // 2) Zufällige Verbrauchsmenge (kWh pro Minute)
                double baseUsage = isPeakHour
                        ? (0.001 + random.nextDouble() * 0.001) // z.B. 0.001 - 0.002
                        : (0.0005 + random.nextDouble() * 0.0005); // 0.0005 - 0.001

                // 3) Nachricht erstellen
                String message = String.format(
                        "{ \"type\": \"USER\", \"association\": \"COMMUNITY\", " +
                                "\"kwh\": %.6f, \"datetime\": \"%s\" }",
                        baseUsage,
                        LocalDateTime.now().toString()
                );

                // 4) Nachricht „versenden“ – hier nur auf Konsole
                System.out.println("Sende USER-Message: " + message);

                // 5) Alle paar Sekunden wiederholen
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
