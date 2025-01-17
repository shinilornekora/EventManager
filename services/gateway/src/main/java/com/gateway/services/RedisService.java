package com.gateway.services;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Получение данных из Redis по ключу.
     *
     * @param key Ключ.
     * @return Значение или null, если не найдено.
     */
    public String getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Сохранение данных в Redis.
     *
     * @param key   Ключ.
     * @param value Значение.
     */
    public void saveToCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES); // Данные будут храниться 10 минут
    }
}
