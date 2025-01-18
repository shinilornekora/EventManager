package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private final GrpcServer grpcServer;

	public DemoApplication(GrpcServer grpcServer) {
		this.grpcServer = grpcServer;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		grpcServer.start();
		grpcServer.blockUntilShutdown();
	}
}
