package com.example.energy.percentage.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Markiert die Klasse als Konfigurationsklasse für Spring
public class RabbitConfig {

    @Bean
    public Queue usageUpdateQueue() {
        // Durable = true, damit die Nachricht auch Broker-Neustarts übersteht
        return new Queue("Usage-update-queue", true);
    }

}
