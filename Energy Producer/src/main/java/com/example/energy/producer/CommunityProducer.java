package com.example.energy.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class CommunityProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private static double temp = 0;

    public CommunityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private void displayWeatherData() {
        try {
            URL url = new URL("http://api.weatherapi.com/v1/current.json?key=YOUR_API_KEY&q=Vienna&aqi=no");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }
                scanner.close();

                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline.toString());
                JSONObject current = (JSONObject) data_obj.get("current");
                temp = (double) current.get("temp_c");
                
                System.out.println("Current temperature in Vienna: " + temp + "°C");
            }
        } catch (Exception e) {
            System.out.println("Error fetching weather data: " + e.getMessage());
            // Fallback temperature if API call fails
            temp = 20.0;
        }
    }

    @Scheduled(fixedRate = 5000)  // alle 5 Sekunden eine Nachricht senden
    public void sendPeriodicEnergyMessage() {
        displayWeatherData(); // Update temperature data
        double kwh = calculateKwhBasedOnWeather();
        String message = String.format(
                "{\"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("Automatically sent: " + message);
    }

    private double calculateKwhBasedOnWeather() {
        // Basisproduktion
        double baseProduction = 0.003;
        
        // Temperatureinfluss (mehr Produktion bei höheren Temperaturen, da mehr Sonnenschein)
        double tempFactor = Math.max(0.5, Math.min(2.0, temp / 20.0));
        
        // Tageszeit-Einfluss
        int hour = LocalDateTime.now().getHour();
        double timeFactor = 1.0;
        
        // Höhere Produktion während Tagesstunden (8-18 Uhr)
        if (hour >= 8 && hour <= 18) {
            timeFactor = 2.0;
        }
        // Reduzierte Produktion in der Dämmerung (6-8 und 18-20 Uhr)
        else if ((hour >= 6 && hour < 8) || (hour > 18 && hour <= 20)) {
            timeFactor = 1.5;
        }
        // Minimale Produktion nachts
        else {
            timeFactor = 0.5;
        }

        return baseProduction * tempFactor * timeFactor + (0.001 * random.nextDouble());
    }
}