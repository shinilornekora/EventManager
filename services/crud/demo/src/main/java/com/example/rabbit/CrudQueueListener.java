package com.example.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.config.RabbitMQConfig;

@Component
public class CrudQueueListener {

    private final MessageProcessor messageProcessor;

    public CrudQueueListener(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @RabbitListener(queues = RabbitMQConfig.CRUD_QUEUE)
    public void handleMessage(String message) {
         messageProcessor.processMessage(message.getBytes());
    }
}
