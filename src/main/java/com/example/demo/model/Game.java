package com.example.demo.model;
 
import jakarta.persistence.*;
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String gameId;
    private String roomCode;

    private Account me;
    private Account opponent;

    private int mescore = 0;
    private int theyscore = 0;
    
    private int wincondition = 3;

    private Account currentPlayerIndex;
    private Stone[] line = new Stone[7];

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Account getMe() {
        return me;
    }

    public void setMe(Account me) {
        this.me = me;
    }

    public Account getOpponent() {
        return opponent;
    }

    public void setOpponent(Account opponent) {
        this.opponent = opponent;
    }

    public int getMescore() {
        return mescore;
    }

    public void setMescore(int mescore) {
        this.mescore = mescore;
    }

    public int getTheyscore() {
        return theyscore;
    }

    public void setTheyscore(int theyscore) {
        this.theyscore = theyscore;
    }

    public int getWincondition() {
        return wincondition;
    }

    public void setWincondition(int wincondition) {
        this.wincondition = wincondition;
    }

    public Account getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(Account currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Stone[] getLine() {
        return line;
    }

    public void setLine(Stone[] line) {
        this.line = line;
    }
}
