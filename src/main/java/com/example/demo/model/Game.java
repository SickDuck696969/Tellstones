package com.example.demo.model;
 
import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String gameId;
    String roomCode;

    Account me;
    Account opponent;

    Map <String, Long> Scoreboard;
    int wincondition = 3;

    Account currentPlayerIndex;
}
