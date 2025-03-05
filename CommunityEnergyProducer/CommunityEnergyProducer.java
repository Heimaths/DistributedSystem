package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Eine eigenständige Java-Anwendung, die alle paar Sekunden
 * eine PRODUCER-Nachricht simuliert. Optional wird eine Wetter-API
 * abgefragt, um bei Sonnenschein mehr Energie zu produzieren.
 */
public class CommunityEnergyProducer {

    // OpenWeatherMap API-Key hier eintragen (oder per Umgebungsvariable)
    private static final String API_KEY = "DEIN_OPENWEATHER_API_KEY";
    // Beispiel-Stadt
    private static final String CITY = "Berlin";

    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("CommunityEnergyProducer gestartet ...");

        while (true) {
            try {
                // 1) Wetterdaten abrufen (optional)
                double weatherFactor = fetchWeatherFactor();

                // 2) Zufällige Produktionsmenge (kWh pro Minute)
                //    Basiswert z.B. zwischen 0.001 und 0.005
                double baseProduction = 0.001 + random.nextDouble() * 0.004;
                // Wetterfaktor multiplizieren (z.B. mehr Energie bei Sonne)
                double kwh = baseProduction * weatherFactor;

                // 3) Nachricht erstellen
                String message = String.format(
                        "{ \"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", " +
                                "\"kwh\": %.6f, \"datetime\": \"%s\" }",
                        kwh,
                        LocalDateTime.now().toString()
                );

                // 4) Nachricht „versenden“ – hier nur auf Konsole
                System.out.println("Sende PRODUCER-Message: " + message);

                // 5) Alle paar Sekunden wiederholen
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                // Kurze Pause vor nächstem Versuch
                try { Thread.sleep(5000); } catch (InterruptedException ex) {}
            }
        }
    }

    /**
     * Ruft das aktuelle Wetter über die OpenWeatherMap-API ab und
     * gibt einen Faktor zurück, der die Produktion erhöht oder senkt.
     *
     * @return z.B. 2.0 bei Sonnenschein, 0.5 bei Regen, 1.0 neutral
     */
    private static double fetchWeatherFactor() {
        // Wenn kein API-Key hinterlegt ist, einfach 1.0 zurückgeben
        if (API_KEY.equals("DEIN_OPENWEATHER_API_KEY")) {
            return 1.0;
        }

        try {
            String urlStr = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s",
                    CITY, API_KEY
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Einfachste Variante: nach "clear" suchen => sonnig
                String json = response.toString().toLowerCase();
                if (json.contains("clear")) {
                    // Sonnig => 2x Produktion
                    return 2.0;
                } else if (json.contains("rain") || json.contains("snow")) {
                    // Regen/Schnee => weniger Produktion
                    return 0.5;
                }
                // Bewölkt => normal
                return 1.0;
            }
        } catch (Exception e) {
            // Bei Fehler => neutral
            System.err.println("Fehler beim Abruf der Wetterdaten: " + e.getMessage());
        }
        return 1.0;
    }
}
