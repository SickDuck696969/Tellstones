package com.example.demo;

import java.net.Socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.demo.socketio.SocketIOService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
