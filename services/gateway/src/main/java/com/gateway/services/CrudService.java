package com.gateway.services;

import org.springframework.stereotype.Service;

import com.gateway.payloads.DataPayload;

@Service
public class CrudService {

    public String getData(String key) {
        System.out.println("Fetching data from CRUD service for key: " + key);
        return "Sample data for key: " + key;
    }

    public void saveData(DataPayload payload) {
        try {
            // Convert payload to gRPC binary format
            byte[] grpcBinaryData = payload.toByteArray();

            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend("post_operations_queue", grpcBinaryData);

            System.out.println("Posted data to RabbitMQ: " + payload);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save data");
        }
    }

    public void putData(DataPayload payload) {
        System.out.println("Putting data to the CRUD service: " + payload);
    }

    public void deleteData(DataPayload payload) {
        System.out.println("Deleting data from the CRUD service: " + payload);
    }
}
