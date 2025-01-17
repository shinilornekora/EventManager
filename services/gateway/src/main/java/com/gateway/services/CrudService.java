package com.gateway.services;

import org.example.event.grpc.Event;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.gateway.payloads.DataPayload;

@Service
public class CrudService {

    private RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = "crudQueue";


    public String getData(String key) {
        System.out.println("Fetching data from CRUD service for key: " + key);
        return "Sample data for key: " + key;
    }

    public void throwMessageToQueue(Event payload) {
        try {
            byte[] grpcBinaryData = payload.toByteArray();

            rabbitTemplate.convertAndSend(QUEUE_NAME, grpcBinaryData);
            System.out.println("Posted data to RabbitMQ: " + payload);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save data");
        }
    }
}
