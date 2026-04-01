package com.example.demo.socketio;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.demo.model.Stone;
import com.example.demo.socketio.SocketIOService.PlaceData;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.example.demo.model.Account;

import java.util.Random;

@Service
public class SocketIOService {

    public SocketIOServer server;

    public static class PlaceData {
        public String room;
        public String to;
        public String stone;
        public String where;
    }

    public static class TurnUpdateData { 
        public String roomId;
        public String turn;
    }

    public static class LineUpdateData {
        public String roomId;
        private Stone[] line;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }
        
        public Stone[] getLine() {
            return line;
        }

        public void setLine(Stone[] line) {
            this.line = line;
        }
    }

    public void startServer() {
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9092);
        config.setOrigin("*");

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

        server.addEventListener("leaveRoom", String.class, (client, roomId, ack) -> {
            System.out.println("Leave room: " + roomId);
            client.leaveRoom(roomId);
            client.sendEvent("outthedoor");
        });

        server.addEventListener("startgame", String.class, (client, roomId, ack) -> {
            System.out.println("starting game");
            var clients = server.getRoomOperations(roomId).getClients();
            for (var c : clients) {
                c.sendEvent("gamestart");
            }
        });

        server.addEventListener("massspawn", String.class, (client, roomId, ack) -> {
            System.out.println("massspawning");
            var clients = server.getRoomOperations(roomId).getClients();
            for (var c : clients) {
                c.sendEvent("massspawnapprove");
            }
        });

        server.addEventListener("whogofirst", String.class, (client, roomId, ack) -> {
            Random random = new Random();
            int result = random.nextInt(2);
            var clients = server.getRoomOperations(roomId).getClients();
            int index = 0;
            for (var c : clients) {
                if(index == result){
                    c.sendEvent("yougofirst", "yougofirst");
                }
                else if (index == 1 - result){
                    c.sendEvent("younotgofirst", "younotgofirst");
                }
                index++;
            }
        });

        server.addEventListener("moveRoom", String.class, (client, roomId, ack) -> {
            client.sendEvent("roomJoined", roomId);
            client.sendEvent("goto", client.getSessionId().toString());
        });

        server.addEventListener("massleave", Void.class, (client, data, ack) -> {
            System.out.println("leaving rooms");
            var rooms = client.getAllRooms();
            for (var c : rooms) {
                client.leaveRoom(c);
            }
        });

        server.addEventListener("lineupdate", LineUpdateData.class, (client, data, ack) -> {
            System.out.println("Updating line");
            for (var s : data.line) {
                if(s == null) continue;
                System.out.println(s.getId() + " " + s.getIcon());
            }
            var clients = server.getRoomOperations(data.roomId).getClients();
            for (var c : clients) {
                c.sendEvent("updateline", data);
            }
        });

        server.addEventListener("turnupdate", TurnUpdateData.class, (client, data, ack) -> {
            System.out.println("Changing turns");
            var clients = server.getRoomOperations(data.roomId).getClients();
            int index = 0;
            for (var c : clients) {
                if(index <= 2){
                    c.sendEvent("turnupdate", data);
                }
                index++;
            }
        });
        
        server.addEventListener("place", PlaceData.class, (client, data, ackSender) -> {
            System.out.println(data);
            var clients = server.getRoomOperations(data.room).getClients();
            for (var c : clients){
                String pp = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
                String aa = java.net.URLDecoder.decode(data.to, StandardCharsets.UTF_8);
                if(pp.equals(aa)){
                    c.sendEvent("place", data);
                }
            }
        });

        server.addEventListener("hide", PlaceData.class, (client, data, ackSender) -> {
            System.out.println(data);
            var clients = server.getRoomOperations(data.room).getClients();
            for (var c : clients){
                String pp = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
                String aa = java.net.URLDecoder.decode(data.to, StandardCharsets.UTF_8);
                if(pp.equals(aa)){
                    c.sendEvent("hide", data);
                }
            }
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
