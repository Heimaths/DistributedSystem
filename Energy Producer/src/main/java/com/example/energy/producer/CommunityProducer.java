package com.example.energy.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CommunityProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private static double temp = 0;

    // Einmalig konfigurierte HTTP-Client- und JSON-Mapper-Instanzen
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public CommunityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 5000)  // alle 5 Sekunden eine Nachricht senden
    public void sendPeriodicEnergyMessage() {
        // Vor dem Senden die Temperatur vom Wetter-API aktualisieren
        displayWeatherData();

        double kwh = getRandomKwh();
        String message = String.format(
                java.util.Locale.US,
                "{\"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        rabbitTemplate.convertAndSend("Producer-energy-queue", message);
        System.out.println("Automatically sent: " + message);
    }

    private double getRandomKwh() {
        if (temp > 10) {
            // bei hoher Temperatur leicht mehr Erzeugung
            return 0.001 + 0.004 * random.nextDouble() * 2;  // bis zu 0.009 kWh
        } else {
            return 0.001 + 0.004 * random.nextDouble();       // bis zu 0.005 kWh
        }
    }

    public static void displayWeatherData() {
        try {
            // 1. HTTP-Anfrage bauen
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.openweathermap.org/data/2.5/weather"
                                    + "?lat=48.21&lon=16.36&units=metric"
                                    + "&appid=3c8aab4d3346f32df1d6dbe09d215f9f"
                    ))
                    .GET()
                    .build();

            // 2. Anfrage senden und Antwort als String erhalten
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            // 3. Status prüfen
            if (response.statusCode() != 200) {
                System.out.println("Error: Could not fetch weather data, status = "
                        + response.statusCode());
                return;
            }

            // 4. JSON in Java-Objekt mappen
            WeatherResponse weather = objectMapper.readValue(
                    response.body(), WeatherResponse.class
            );

            // 5. Temperatur auslesen und speichern
            temp = weather.main.temp;
            System.out.println("Current Temperature (C): " + temp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hilfsklassen für Jackson
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WeatherResponse {
        public Main main;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Main {
        public double temp;
    }
}
