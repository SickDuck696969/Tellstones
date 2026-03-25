package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
 
import jakarta.validation.constraints.NotNull; 
import java.util.List; 
import java.util.Optional; 

import java.util.Map;
 
import java.util.ArrayList;

@Service 
@RequiredArgsConstructor
@Transactional 
public class PlayerService {
    private final List<Map<String, Account>> players = new ArrayList<>();

    public void addPlayer(Map<String, Account> player) {
        players.add(player);
    }

    public List<Map<String, Account>> getPlayers() {
        return players;
    }
}