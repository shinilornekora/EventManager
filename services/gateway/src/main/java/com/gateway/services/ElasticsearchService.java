package com.gateway.services;

import org.springframework.stereotype.Service;

@Service
public class ElasticsearchService {

    public void logRequest(String data) {
        System.out.println("Logging request to Elasticsearch: " + data);
    }
}
