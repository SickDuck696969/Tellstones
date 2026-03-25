package com.example.demo.socketio;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class SocketIOService {

    public SocketIOServer server;

    public void startServer() {
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9092);
        config.setOrigin("http://localhost:8080");

        server = new SocketIOServer(config);

        // Connection listener
        server.addConnectListener(client ->
                System.out.println("Client connected: " + client.getSessionId())
        );

        // Message listener
        server.addEventListener("message", String.class, (client, data, ackSender) -> {
            System.out.println("Received: " + data);

            // Broadcast
            server.getBroadcastOperations().sendEvent("message", data);
        });
        System.out.printf("butt %d\n", server.getConfiguration().getPort());

        server.addEventListener("startRoom", String.class, (client, roomId, ack) -> {
            client.sendEvent("roomJoined", roomId);
            client.sendEvent("sessionID", client.getSessionId().toString());
        });

        server.addEventListener("inRoom", String.class, (client, roomId, ack) -> {
            System.out.println("Start room: " + roomId);
            client.joinRoom(roomId);
            client.sendEvent("Youin");
        });

        try {
            server.start();
        } catch (Exception e) {
            System.out.println("Failed to start SocketIO server: " + e.getMessage());
        }
        System.out.printf("connected on port %d", server.getConfiguration().getPort());
    }

    @PostConstruct
    public void start() {
        startServer();
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            server.stop();
            System.out.println("SocketIO server stopped");
        }
    }

    public SocketIOServer getServer() {
        return server;
    }
}