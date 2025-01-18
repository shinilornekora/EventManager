package com.gateway.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void logRequest(String indexName, String data) {
        try {
            // Пример структуры данных для индексации
            Map<String, Object> document = new HashMap<>();
            document.put("timestamp", System.currentTimeMillis());
            document.put("log", data);

            // Создаем запрос на индексирование
            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index(indexName)
                    .document(document)
            );

            // Выполняем запрос
            IndexResponse response = elasticsearchClient.index(request);
            System.out.println("Document indexed with ID: " + response.id());
        } catch (IOException e) {
            System.err.println("Error while indexing document: " + e.getMessage());
        }
    }
}
