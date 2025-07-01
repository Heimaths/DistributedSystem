package com.example.energy.percentage.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Markiert die Klasse als Konfigurationsklasse für Spring
public class RabbitConfig {

    @Bean  // Erstellt eine Bean, die beim Start von Spring registriert wird
    public Queue producerEnergyQueue() {
        // Erstellt eine dauerhafte RabbitMQ-Queue namens "Producer-energy-queue" - Diese Queues werden beim Start automatisch beim RabbitMQ-Server registriert, sofern sie noch nicht existieren.
        return new Queue("Producer-energy-queue", true);  // durable = true bedeutet, dass Queue und nicht konsumierte Nachrichten erhalten bleibt, auch wenn RabbitMQ neu gestartet wird
    }

    @Bean
    public Queue consumerEnergyQueue() {
        // siehe oben
        return new Queue("Consumer-energy-queue", true);
    }
    @Bean
    public Queue usageUpdateQueue() {
        // Durable = true, damit die Nachricht auch Broker-Neustarts übersteht
        return new Queue("Usage-update-queue", true);
    }

}
