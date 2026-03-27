package com.example.demo.model;
 
import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

public class Game { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String gameId;
    String roomCode;

    List<Account> players;
    int maxPlayers = 4;

    Map <String, Long> Scoreboard;
    int wincondition = 3;

    int currentPlayerIndex;
    boolean started;

    public Game() {
    }

    public Game(String gameId, String roomCode, List<Account> players, int maxPlayers, Map<String, Long> scoreboard,
            int wincondition, int currentPlayerIndex, boolean started) {
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.Scoreboard = scoreboard;
        this.wincondition = wincondition;
        this.currentPlayerIndex = currentPlayerIndex;
        this.started = started;
    }

    public List<Account> getPlayers() {
        return players;
    }

    public void setPlayers(List<Account> players) {
        this.players = players;
    }
}
