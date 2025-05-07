package com.example.energy.usage.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue producerEnergyQueue() {
        return new Queue("Producer-energy-queue", true);
    }

    @Bean
    public Queue consumerEnergyQueue() {
        return new Queue("Consumer-energy-queue", true);
    }
}
