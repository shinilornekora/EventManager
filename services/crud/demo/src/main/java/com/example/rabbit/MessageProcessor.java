package com.example.rabbit;

import com.example.entities.EventEntity;
import org.example.event.grpc.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final EventRepository eventRepository;

    public MessageProcessor(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void processMessage(byte[] message) {
        logger.info("Processing gRPC binary message");

        try {
            Event payload = Event.parseFrom(message);

            switch (payload.getQueryType().toUpperCase()) {
                case "ADD" ->
                    handleCreate(payload);
                case "CHANGE" ->
                    handleUpdate(payload);
                case "DELETE" ->
                    handleDelete(payload);
                default -> logger.warn("Unsupported queryType: {}", payload.getQueryType());
            }
        } catch (Exception e) {
            logger.error("Error processing gRPC message", e);
        }
    }

    private void handleCreate(Event payload) {
        EventEntity event = convertToEntity(payload);
        eventRepository.save(event);
        logger.info("EventEntity created: {}", event);
    }

    private void handleUpdate(Event payload) {
        EventEntity event = convertToEntity(payload);
        String eventId = event.getEventId();

        if (eventRepository.existsById(Long.valueOf(eventId))) {
            eventRepository.save(event);
            logger.info("EventEntity updated: {}", event);
        } else {
            logger.warn("EventEntity not found for update: {}", event.getEventId());
        }
    }

    private void handleDelete(Event payload) {
        Long eventId = Long.parseLong(payload.getEventId());

        if (eventRepository.existsById(eventId)) {
            eventRepository.deleteById(eventId);
            logger.info("EventEntity deleted with ID: {}", eventId);
        } else {
            logger.warn("EventEntity not found for deletion: {}", eventId);
        }
    }

    private EventEntity convertToEntity(Event grpcEvent) {
        EventEntity event = new EventEntity();

        if (!grpcEvent.getEventId().isEmpty()) {
            event.setEventId(grpcEvent.getEventId());
        }

        event.setEventName(grpcEvent.getEventName());
        event.setEventDate(LocalDateTime.parse(grpcEvent.getEventDate(), DateTimeFormatter.ISO_DATE_TIME));
        event.setEventLocation(grpcEvent.getEventLocation());

        return event;
    }
}

