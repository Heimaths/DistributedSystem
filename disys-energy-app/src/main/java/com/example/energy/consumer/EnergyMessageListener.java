package com.example.energy.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EnergyMessageListener {
    @RabbitListener(queues = "energy-queue")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}