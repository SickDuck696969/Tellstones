package com.example.demo.socketio;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.springframework.stereotype.Component;

@Component
public class Client {

    public Socket socket;

    // 1. Initialize socket
    public void init() throws Exception {
        socket = IO.socket("http://localhost:9092");
    }

    // 2. Register events
    public void registerEvents() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("Connected to server");
        });

        socket.on("message", args -> {
            System.out.println("Received: " + args[0]);
        });
    }

    // 3. Connect
    public void connect() {
        if (socket != null) {
            socket.connect();
        }
    }

    // 4. Send message
    public void sendMessage(String message) {
        if (socket != null && socket.connected()) {
            socket.emit("message", message);
        } else {
            System.out.println("Socket not connected!");
        }
    }

    // 5. Disconnect
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            System.out.println("Disconnected");
        }
    }
}