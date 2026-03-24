package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;
import com.example.demo.service.PlayerService;
import com.example.demo.model.Game;
import com.example.demo.model.Stone;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.net.Socket;
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
import java.util.HashMap;

import tools.jackson.databind.JsonNode;

import com.example.demo.socketio.*;


@Controller
@RequestMapping("/tellstones")
@RequiredArgsConstructor
public class GameController {

    private final SocketIOService socketservice;
    private final Client coreclient;
    List<Map<String, String>> playerlistlist = new ArrayList<Map<String, String>>();
    private final PlayerService playerService;

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
        try {
            if(coreclient.socket == null){
                coreclient.init();
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return "tellstone/menu";
    }

    @GetMapping("/room/{id}")
    public String loadroom(@PathVariable String id, Model model) {
        model.addAttribute("roomId", id);
        return "tellstone/game";
    }

    @GetMapping("/getplayers/{id}")
    public ResponseEntity<?> getPlayers(@PathVariable String id) {

        System.out.println(id);

        var clients = socketservice.getServer().getRoomOperations(id).getClients();

        for (var c : clients) {
            System.out.println("Player session: " + c.getSessionId());
            System.out.println(playerService.getPlayers());
        }

        List<String> players = clients.stream()
                .map(c -> c.getSessionId().toString())
                .toList();

        System.out.println(players);
        return ResponseEntity.ok(players);
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

    @PostMapping("/welcomeplayer")
    public ResponseEntity<?> welcomePlayer(@RequestBody Map<String, Object> body) {
        Map<String, String> player = new HashMap<>();
        player.put("username", (String) body.get("username"));
        player.put("session", (String) body.get("session"));

        playerService.addPlayer(player);

        return ResponseEntity.ok(playerService.getPlayers());
    }
}