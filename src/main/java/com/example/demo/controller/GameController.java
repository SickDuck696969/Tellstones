package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;
import com.example.demo.model.Game;
import com.example.demo.model.Stone;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional; 
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import tools.jackson.databind.JsonNode;


@Controller
@RequestMapping("/tellstones")
@RequiredArgsConstructor
public class GameController {

    private Game game = new Game();
    private final AccountService accountService;
    private List<Stone> bag = Arrays.asList(
        new Stone(2, "/Sword.png", true),
        new Stone(3, "/Horse.png", true),
        new Stone(4, "/Shield.png", true),
        new Stone(5, "/Hammer.png", true),
        new Stone(6, "/Flag.png", true),
        new Stone(7, "/Crown.png", true),
        new Stone(8, "/Scale.png", true)
    );
    private Stone[] theline = new Stone[7];

    public void startgame(String playername) {
        List<Account> players = game.getPlayers();
        Account player = accountService.getAccountByName(playername).get();
        if (player == null){
            player = new Account();
            player.setName(playername);
        }
        players.add(player);
        game.setPlayers(players);
    }

    @GetMapping("/")
    public String backtologin() {
        return "forward:/tellstone/game.html";
    }

    @PostMapping("/settinggame")
    public ResponseEntity<?> deleteProduct(@RequestBody List<Map<String, String>> body) {
        body.forEach(stone -> {
            bag.forEach(bagstone -> {
                System.out.println("Value from key: " + stone.get("stoneid"));
                int stoneId = Integer.parseInt(stone.get("stoneid"));
                if(stoneId == bagstone.getId()){
                    theline[Integer.parseInt(stone.get("position"))] = bagstone;
                }
            });
        });
        return ResponseEntity.ok(theline);
    }
}