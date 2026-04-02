package com.example.demo.service;

import com.example.demo.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
 
import java.util.List; 
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.model.stoneskin;

@Service 
@RequiredArgsConstructor
@Transactional 
public class PlayerService {
    private final List<Map<String, Account>> players = new ArrayList<>();
    private final List<Map<Account, stoneskin>> stoneskins = new ArrayList<>();
    private final Map<String, String> selectedStoneSkins = new ConcurrentHashMap<>();

    public void addPlayer(Map<String, Account> player) {
        players.add(player);
    }

    public void addSkin(Map<Account, stoneskin> da) {
        stoneskins.add(da);
    }

    public void removePlayer(Account account) {
        players.removeIf(player -> player.containsKey(account.getUsername()));
    }

    public Map<String, Account> getPlayerByUsername(String username) {
        return players.stream()
                .filter(player -> player.containsKey(username))
                .findFirst()
                .orElse(null);
    }

    public List<Map<String, Account>> getPlayers() {
        return players;
    }

    public stoneskin getSkin(Account account) {
        return stoneskins.stream()
                .filter(map -> map.containsKey(account))
                .map(map -> map.get(account))
                .findFirst()
                .orElse(null);
    }

    public void setSelectedSkin(String username, String skinCode) {
        if (username == null || username.isBlank() || skinCode == null || skinCode.isBlank()) {
            return;
        }
        selectedStoneSkins.put(username, skinCode);
    }

    public String getSelectedSkin(String username) {
        if (username == null || username.isBlank()) {
            return "default";
        }
        return selectedStoneSkins.getOrDefault(username, "default");
    }
}
