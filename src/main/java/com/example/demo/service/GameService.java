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

        public String getRoomCode() {
            return roomCode;
        }

        public void setRoomCode(String roomCode) {
            this.roomCode = roomCode;
        }

        public Long getWinnerId() {
            return winnerId;
        }

        public void setWinnerId(Long winnerId) {
            this.winnerId = winnerId;
        }

        public String getWinnerUsername() {
            return winnerUsername;
        }

        public void setWinnerUsername(String winnerUsername) {
            this.winnerUsername = winnerUsername;
        }

        public String getLoserUsername() {
            return loserUsername;
        }

        public void setLoserUsername(String loserUsername) {
            this.loserUsername = loserUsername;
        }

        public String getResultReason() {
            return resultReason;
        }

        public void setResultReason(String resultReason) {
            this.resultReason = resultReason;
        }

        public String getRewardType() {
            return rewardType;
        }

        public void setRewardType(String rewardType) {
            this.rewardType = rewardType;
        }

        public int getRewardAmount() {
            return rewardAmount;
        }

        public void setRewardAmount(int rewardAmount) {
            this.rewardAmount = rewardAmount;
        }

        public int getUpdatedGemTotal() {
            return updatedGemTotal;
        }

        public void setUpdatedGemTotal(int updatedGemTotal) {
            this.updatedGemTotal = updatedGemTotal;
        }

        public int getUpdatedFragmentTotal() {
            return updatedFragmentTotal;
        }

        public void setUpdatedFragmentTotal(int updatedFragmentTotal) {
            this.updatedFragmentTotal = updatedFragmentTotal;
        }
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
