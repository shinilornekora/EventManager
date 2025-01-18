package com.gateway.controllers;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.event.grpc.*;
import com.google.protobuf.util.JsonFormat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.gateway.services.ElasticsearchService;
import com.gateway.services.RedisService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final String QUEUE_NAME = "crudQueue";

    @Autowired
    private RedisService redisService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final CrudServiceGrpc.CrudServiceBlockingStub crudServiceBlockingStub;

    public GatewayController() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        this.crudServiceBlockingStub = CrudServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping("/data")
    public ResponseEntity<?> getData(@RequestParam String key) {
        System.out.println("Howdy! I will handle your get request. Key is: " + key);

        GetDataRequest request = GetDataRequest.newBuilder()
                .setEventId(key)
                .build();

        System.out.println("created getdararequest");
        try {
            Event response = crudServiceBlockingStub.getData(request);

            System.out.println("got response from grpc");

            String cachedInRedis = redisService.getFromCache(response.getEventId());

            System.out.println("made dome things with redis");

            if (cachedInRedis == null) {
                redisService.saveToCache(response.getEventId(), String.valueOf(response));
            }

            String jsonResponse = JsonFormat.printer().print(response);

            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while fetching data: " + e.getMessage());
        }
    }

    @GetMapping("/data/all")
    public ResponseEntity<?> getAllData() {
        try {
            GetDataResponseAll response = crudServiceBlockingStub.getAllData(Empty.newBuilder().build());
            String cachedInRedis = redisService.getFromCache("all");

            if (cachedInRedis == null) {
                redisService.saveToCache("all", String.valueOf(response));
            }

            String jsonResponse = JsonFormat.printer().print(response);

            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error while fetching data: " + e.getMessage());
        }
    }

    @PostMapping("/data")
    public ResponseEntity<Void> postData(@RequestBody DataPayload payload) {
        final String uuid = UUID.randomUUID().toString();
        
        Event event = Event.newBuilder()
                .setQueryType("ADD")
                .setEventId(uuid)
                .setEventDate(payload.date)
                .setEventLocation(payload.location)
                .setEventName(payload.name)
                .build();
        
        crudService.throwMessageToQueue(event);
        elasticsearchService.logRequest(payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/data")
    public ResponseEntity<Void> putData(@RequestBody DataPayload payload) {
        Event event = Event.newBuilder()
                .setQueryType("CHANGE")
                .setEventId(payload.id)
                .setEventDate(payload.date)
                .setEventLocation(payload.location)
                .setEventName(payload.name)
                .build();

        crudService.throwMessageToQueue(event);
        elasticsearchService.logRequest(payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/data")
    public ResponseEntity<Void> deleteData(@RequestBody DataPayload payload) {
        Event event = Event.newBuilder()
                .setQueryType("DELETE")
                .setEventId(payload.id)
                .build();

        crudService.throwMessageToQueue(event);
        elasticsearchService.logRequest(payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok().build();
    }
}
