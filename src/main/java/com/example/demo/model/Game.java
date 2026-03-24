package com.example.demo.model;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Setter 
@Getter 
@NoArgsConstructor
@AllArgsConstructor 
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
}