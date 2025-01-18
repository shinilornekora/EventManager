package com.example;

import com.example.rabbit.MessageProcessor;
import com.example.repositories.EventRepository;
import com.example.service.CrudServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServer {
    private Server server;

    @Autowired
    EventRepository eventRepository;

    public void start() throws IOException {
        server = ServerBuilder.forPort(50051)
                .addService(new CrudServiceImpl(new MessageProcessor(eventRepository)))
                .build()
                .start();

        System.out.println("gRPC server started on port 50051");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            GrpcServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
