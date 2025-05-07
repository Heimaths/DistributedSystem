package com.example.energy.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue energyQueue() {
        return new Queue("Producer-energy-queue", true);
    }
}