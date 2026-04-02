package com.example.demo.service;

import com.example.demo.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
 
import java.util.List; 
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.demo.model.Game;

@Service 
@RequiredArgsConstructor
@Transactional 
public class GameService {
    private final List<Game> Games = new ArrayList<>();
    private final Map<String, MatchResult> matchResults = new HashMap<>();

    @lombok.Getter
    @lombok.Setter
    public static class MatchResult {
        private String roomCode;
        private Long winnerId;
        private String winnerUsername;
        private String loserUsername;
        private String resultReason;
        private String rewardType;
        private int rewardAmount;
        private int updatedGemTotal;
        private int updatedFragmentTotal;
    }

    public void addGame(Game game) {
        Games.add(game);
    }

    public Game getGame(String roomcode) {
        for (Game s : Games) {
            if (s.getRoomCode().equals(roomcode)) {
                return s;
            }
        }
        return null;
    }

    public List<Game> getGames() {
        return Games;
    }

    public synchronized MatchResult getMatchResult(String roomCode) {
        return matchResults.get(roomCode);
    }

    public synchronized void saveMatchResult(String roomCode, MatchResult result) {
        matchResults.put(roomCode, result);
    }
}
