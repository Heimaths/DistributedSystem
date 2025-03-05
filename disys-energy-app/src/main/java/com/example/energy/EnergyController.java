package com.example.energy;

import com.example.energy.producer.CommunityProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnergyController {
    private final CommunityProducer producer;

    public EnergyController(CommunityProducer producer) {
        this.producer = producer;
    }

    @GetMapping("/send")
    public String sendMessage(@RequestParam String message) {
        producer.sendEnergyMessage(message);
        return "Message sent: " + message;
    }
}