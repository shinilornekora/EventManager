package com.gateway.controllers;

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
import org.example.event.grpc.Event;

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

    @GetMapping("/data")
    public ResponseEntity<String> getData(@RequestParam String key) {
        String cachedData = redisService.getFromCache(key);

        if (cachedData != null) {
            return ResponseEntity.ok(cachedData);
        }

        String data = crudService.getData(key);
        redisService.saveToCache(key, data);
        elasticsearchService.logRequest(key);

        return ResponseEntity.ok(data);
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
                .setQueryType("CHANGE")
                .setEventId(payload.id)
                .build();

        crudService.throwMessageToQueue(event);
        elasticsearchService.logRequest(payload.toString());
        rabbitTemplate.convertAndSend(QUEUE_NAME, event.toByteArray());

        return ResponseEntity.ok().build();
    }
}
