package com.example.demo.service;

import com.example.demo.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
 
import java.util.List; 
import java.util.Map;
import java.util.ArrayList;

import com.example.demo.model.Game;

@Service 
@RequiredArgsConstructor
@Transactional 
public class GameService {
    private final List<Game> Games = new ArrayList<>();

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
}
