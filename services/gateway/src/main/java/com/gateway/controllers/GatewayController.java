package com.gateway.controllers;

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

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private CrudService crudService;

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

    // TODO: шлем в rabbitmq
    @PostMapping("/data")
    public ResponseEntity<Void> postData(@RequestBody DataPayload payload) {
        crudService.saveData(payload);
        elasticsearchService.logRequest(payload.toString());
        return ResponseEntity.ok().build();
    }

    // TODO: шлем в rabbitmq
    @PutMapping("data")
    public ResponseEntity<Void> putData(@RequestBody DataPayload payload) {
        elasticsearchService.logRequest(payload.toString());
        return ResponseEntity.ok().build();
    }

    // TODO: шлем в rabbitmq
    @DeleteMapping("data")
    public ResponseEntity<Void> deleteData(@RequestBody DataPayload payload) {
        elasticsearchService.logRequest(payload.toString());
        return ResponseEntity.ok().build();
    }

}
