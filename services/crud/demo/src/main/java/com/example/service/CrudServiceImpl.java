package com.example.service;

import com.example.entities.EventEntity;
import com.example.rabbit.MessageProcessor;
import io.grpc.stub.StreamObserver;
import org.example.event.grpc.*;

import java.util.List;

public class CrudServiceImpl extends CrudServiceGrpc.CrudServiceImplBase {
    private final MessageProcessor messageProcessor;

    public CrudServiceImpl(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void getAllData(Empty request, StreamObserver<GetDataResponseAll> responseObserver) {
        System.out.println("Запрос на получение данных:");
        GetDataResponseAll.Builder builder = GetDataResponseAll.newBuilder();

        List<EventEntity> eventEntityList = messageProcessor.handleGetAll();
        System.out.println("события найдены: " + eventEntityList);

        for (EventEntity entity : eventEntityList) {
            Event product = Event.newBuilder()
                    .setEventId(entity.getEventId())
                    .setEventDate(entity.getEventDate())
                    .setEventLocation(entity.getEventLocation())
                    .setEventName(entity.getEventName())
                    .build();
            builder.addEvent(product);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getData(GetDataRequest request, StreamObserver<Event> responseObserver) {
        System.out.println("Запрос на получение данных:\n" + request);
        Event.Builder builder = Event.newBuilder();

        EventEntity eventEntity = messageProcessor.handleGet(request.getEventId());
        System.out.println("Продукт найден: " + eventEntity);

        builder.setEventId(eventEntity.getEventId())
                .setEventName(eventEntity.getEventName())
                .setEventDate(eventEntity.getEventDate())
                .setEventLocation(eventEntity.getEventLocation());

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
