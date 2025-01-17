package com.example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CRUD_QUEUE = "crudQueue";

    @Bean
    public Queue crudQueue() {
        return new Queue(CRUD_QUEUE, true);
    }
}
