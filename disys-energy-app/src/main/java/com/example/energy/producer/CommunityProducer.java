package com.example.energy.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunityProducer {
    private final RabbitTemplate rabbitTemplate;

    public CommunityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEnergyMessage(String message) {
        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("Message sent: " + message);
    }
}