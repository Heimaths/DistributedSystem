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

    @Scheduled(fixedRate = 5000)  // alle 5 Sekunden eine Nachricht senden
    public void sendPeriodicEnergyMessage() {
        double kwh = getRandomKwh();
        String message = String.format(
                java.util.Locale.US,
                "{\"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("Automatically sent: " + message);
    }

    private double getRandomKwh() {
        if (temp > 10){
            return 0.001 + (0.004 * random.nextDouble()*2);  // Werte zwischen 0.001 und 0.005 kWh
    }
        else return 0.001 + (0.004 * random.nextDouble());  // Werte zwischen 0.001 und 0.005 kWh
    }

    public static void displayWeatherData() {

        try {
            // 1. Fetch the API response based on API Link
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=48.21&lon=16.36&units=metric&appid=3c8aab4d3346f32df1d6dbe09d215f9f";
            HttpURLConnection apiConnection = fetchApiResponse(url);

            // check for response status
            // 200 - means that the connection was a success
            if (apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API test1");
                return;
            }

            // 2. Read the response and convert store String type
            String jsonResponse = readApiResponse(apiConnection);

            // 3. Parse the string into a JSON Object
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            JSONObject currentWeatherJson = (JSONObject) jsonObject.get("main");
//            System.out.println(currentWeatherJson.toJSONString());

            // 4. Store the data into their corresponding data type
            String timezone = (String) currentWeatherJson.get("timezone");
            System.out.println("Current Time: " + timezone);

            double temperature = (double) currentWeatherJson.get("temp");
            System.out.println("Current Temperature (C): " + temperature);
             temp =  temperature;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
            try{
                // attempt to create connection
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // set request method to get
                conn.setRequestMethod("GET");

                return conn;
            }catch(IOException e){
                e.printStackTrace();
            }

            // could not make connection
            return null;
        }


    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            // Create a StringBuilder to store the resulting JSON data
            StringBuilder resultJson = new StringBuilder();

            // Create a Scanner to read from the InputStream of the HttpURLConnection
            Scanner scanner = new Scanner(apiConnection.getInputStream());

            // Loop through each line in the response and append it to the StringBuilder
            while (scanner.hasNext()) {
                // Read and append the current line to the StringBuilder
                resultJson.append(scanner.nextLine());
            }

            // Close the Scanner to release resources associated with it
            scanner.close();

            // Return the JSON data as a String
            return resultJson.toString();

        } catch (IOException e) {
            // Print the exception details in case of an IOException
            e.printStackTrace();
        }

        // Return null if there was an issue reading the response
        return null;
    }
    }

