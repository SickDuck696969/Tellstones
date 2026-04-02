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

import com.example.demo.model.Account;

@Service
public class SocketIOService {

    public SocketIOServer server;
    private final Object quickMatchLock = new Object();
    private com.corundumstudio.socketio.SocketIOClient waitingQuickMatchClient;

    public static class PlaceData {
        public String room;
        public String to;
        public String stone;
        public String where;
    }

    public static class scoredata {
        public String room;
        public String who;
        public String howmany;
    }

    public static class TurnUpdateData { 
        public String roomId;
        public String turn;
    }

    public static class msgData {
        public String roomId;
        public String signedId;
        public String message;
    }

    public static class swapdata {
        public String roomid;
        public String stone1;
        public String stone2;
    }

    public static class challengedata {
        public String roomid;
        public String slotid;
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
        server.addDisconnectListener(client -> {
            synchronized (quickMatchLock) {
                if (waitingQuickMatchClient != null
                        && waitingQuickMatchClient.getSessionId().equals(client.getSessionId())) {
                    waitingQuickMatchClient = null;
                }
            }
        });

        // Message listener
        server.addEventListener("message", msgData.class, (client, data, ackSender) -> {
            System.out.println("Received: " + data);
            var clients = server.getRoomOperations(data.roomId).getClients();
            var index = 0;
            for (var c : clients) {
                String pp = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
                c.sendEvent("messageget", data);
                if(index > 2){
                    break;
                }
                else{
                    System.out.println("Sent to: " + pp);
                    index++;
                }
            }
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

        server.addEventListener("quickmatch", String.class, (client, data, ack) -> {
            synchronized (quickMatchLock) {
                if (waitingQuickMatchClient != null
                        && waitingQuickMatchClient.isChannelOpen()
                        && !waitingQuickMatchClient.getSessionId().equals(client.getSessionId())) {
                    String roomId = randomRoomId();
                    waitingQuickMatchClient.joinRoom(roomId);
                    client.joinRoom(roomId);
                    waitingQuickMatchClient.sendEvent("quickmatched", roomId);
                    client.sendEvent("quickmatched", roomId);
                    waitingQuickMatchClient = null;
                } else {
                    waitingQuickMatchClient = client;
                    client.sendEvent("quickmatchwaiting");
                }
            }
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

        server.addEventListener("swap", swapdata.class, (client, data, ack) -> {
            System.out.println("swap");
            var clients = server.getRoomOperations(data.roomid).getClients();
            int index = 0;
            for (var c : clients) {
                if(index <= 2){
                    c.sendEvent("swapcommence", data);
                }
                index++;
            }
        });

        server.addEventListener("swaptell", swapdata.class, (client, data, ack) -> {
            System.out.println("swaptelling");
            var clients = server.getRoomOperations(data.roomid).getClients();
            int index = 0;
            for (var c : clients) {
                if((index <= 2) && (c != client)){
                    c.sendEvent("swapresponse", data);
                }
                index++;
            }
        });

        server.addEventListener("challenge", challengedata.class, (client, data, ack) -> {
            System.out.println("challenging");
            var clients = server.getRoomOperations(data.roomid).getClients();
            int index = 0;
            for (var c : clients) {
                if((index <= 2) && (c != client)){
                    c.sendEvent("challengecommence", data);
                }
                index++;
            }
        });

        server.addEventListener("score", scoredata.class, (client, data, ack) -> {
            System.out.println("challenging");
            var clients = server.getRoomOperations(data.room).getClients();
            int index = 0;
            for (var c : clients) {
                if((index <= 2)){
                    c.sendEvent("scorewrite", data);
                }
                index++;
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

    private String randomRoomId() {
        String roomId = Long.toString(System.nanoTime(), 36).toUpperCase();
        if (roomId.length() >= 6) {
            return roomId.substring(roomId.length() - 6);
        }
        return String.format("%6s", roomId).replace(' ', 'X');
    }
}
