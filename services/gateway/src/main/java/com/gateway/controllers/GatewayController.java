package com.gateway.controllers;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micrometer.core.annotation.Timed;
import org.example.event.grpc.*;
import com.google.protobuf.util.JsonFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.payloads.DataPayload;
import com.gateway.services.CrudService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@EnableCaching
public class GatewayController {

    private static final String QUEUE_NAME = "crudQueue";

    private static final Logger logRequest = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private CrudService crudService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final CrudServiceGrpc.CrudServiceBlockingStub crudServiceBlockingStub;

    public GatewayController() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("domain-service", 50051)
                .usePlaintext()
                .build();

        this.crudServiceBlockingStub = CrudServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping("/data")
    @Cacheable("events")
    @Timed(value = "gateway.get_certain_data", description = "Time taken to fetch certain event")
    public String getData(@RequestParam String key) {
        GetDataRequest request = GetDataRequest.newBuilder()
                .setEventId(key)
                .build();
        try {
            Event response = crudServiceBlockingStub.getData(request);
            String jsonResponse = JsonFormat.printer().print(response);

            logRequest.info("get: " + jsonResponse);

            return jsonResponse;
        } catch (Exception e) {
            return "Error while fetching data: " + e.getMessage();
        }
    }

    @GetMapping("/data/all")
    @Cacheable("events")
    @Timed(value = "gateway.get_all_data", description = "All events are came throughout this time")
    public String getAllData() {
        try {
            GetDataResponseAll response = crudServiceBlockingStub.getAllData(Empty.newBuilder().build());
            String jsonResponse = JsonFormat.printer().print(response);

            logRequest.info("get_all: " + jsonResponse);

            return jsonResponse;
        } catch (Exception e) {
            return "Error while fetching data: " + e.getMessage();
        }
    }

    @PostMapping("/data")
    @CacheEvict(cacheNames = "events", allEntries = true)
    @Timed(value = "gateway.post_data", description = "Certain event has came throughout this time")
    public ResponseEntity<String> postData(@RequestBody DataPayload payload) {
        final String uuid = UUID.randomUUID().toString();
        
        Event event = Event.newBuilder()
                .setQueryType("ADD")
                .setEventId(uuid)
                .setEventDate(payload.date)
                .setEventLocation(payload.location)
                .setEventName(payload.name)
                .build();
        
        crudService.throwMessageToQueue(event);
        logRequest.info("post: " + payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok("Your query was placed in queue. Try to make another operation to see if it was ok");
    }

    @PutMapping("/data")
    @CacheEvict(cacheNames = "events", allEntries = true)
    @Timed(value = "gateway.post_data", description = "Certain event has put throughout this time")
    public ResponseEntity<String> putData(@RequestBody DataPayload payload) {
        Event event = Event.newBuilder()
                .setQueryType("CHANGE")
                .setEventId(payload.id)
                .setEventDate(payload.date)
                .setEventLocation(payload.location)
                .setEventName(payload.name)
                .build();

        crudService.throwMessageToQueue(event);
        logRequest.info("put: " + payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok("Your query was placed in queue. Try to make another operation to see if it was ok");
    }

    @DeleteMapping("/data")
    @CacheEvict(cacheNames = "events", allEntries = true)
    @Timed(value = "gateway.post_data", description = "Certain event has been deleted throughout this time")
    public ResponseEntity<String> deleteData(@RequestBody DataPayload payload) {
        Event event = Event.newBuilder()
                .setQueryType("DELETE")
                .setEventId(payload.id)
                .build();

        crudService.throwMessageToQueue(event);
        logRequest.info("delete: " + payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok("Your query was placed in queue. Try to make another operation to see if it was ok");
    }
}
