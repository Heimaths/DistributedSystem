package com.example.energy;

import com.example.energy.producer.CommunityProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class EnergyController {

    private final CommunityProducer producer;

    public EnergyController(CommunityProducer producer) {
        this.producer = producer;
    }

    @GetMapping("/send")
    public String sendMessage(@RequestParam double kwh) {
        String message = String.format(
                "{\"type\": \"PRODUCER\", \"association\": \"COMMUNITY\", \"kwh\": %.3f, \"datetime\": \"%s\"}",
                kwh,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        producer.sendEnergyMessage(message);
        return "Message sent: " + message;
    }
}