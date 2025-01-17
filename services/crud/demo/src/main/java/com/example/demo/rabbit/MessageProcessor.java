package com.example.demo.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    public void processMessage(String message) {
        logger.info("Processing message: {}", message);
        // Добавьте здесь логику обработки сообщения
    }
}
