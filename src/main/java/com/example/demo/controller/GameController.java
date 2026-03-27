package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;
import com.example.demo.service.PlayerService;
import com.example.demo.model.Game;
import com.example.demo.model.Stone;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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

import java.util.Collection;

import java.util.Random;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/tellstones")
public class GameController {

    private final SocketIOService socketservice;
    private final Client coreclient;
    List<Map<String, String>> playerlistlist = new ArrayList<Map<String, String>>();
    private final PlayerService playerService;
    private final Emailservice themailman;

    private Game game = new Game();
    private final AccountService accountService;

    public GameController(SocketIOService socketservice, Client coreclient, PlayerService playerService,
            Emailservice themailman, AccountService accountService) {
        this.socketservice = socketservice;
        this.coreclient = coreclient;
        this.playerService = playerService;
        this.themailman = themailman;
        this.accountService = accountService;
    }

    public int howmany = 0;
    public int forhow = 0;
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
    private List<String> phases = Arrays.asList(
        "standby",
        "action",
        "choose",
        "end"
    );

    private int phase = 0;

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
        return "tellstone/game";
    }

    @GetMapping("/leave")
    public String leave() {
        try {
            if(coreclient.socket == null){
                coreclient.init();
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return "tellstone/game";
    }

    @GetMapping("/room/{id}/{userid}")
    public String loadroom(@PathVariable String id, Model model) {
        model.addAttribute("roomId", id);
        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(id).getClients();

        for (com.corundumstudio.socketio.SocketIOClient c : clients) {
            System.out.println("Player session: " + c.getSessionId());
            System.out.println(playerService.getPlayers());
        }
        return "tellstone/room";
    }

    @GetMapping("/getplayers/{id}")
    public ResponseEntity<?> getPlayers(@PathVariable String id) {
        System.out.println(id);
        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(id).getClients();
        List<Map<String, Account>> playersl = playerService.getPlayers();
        List<Account> players = new ArrayList<Account>();

        for (com.corundumstudio.socketio.SocketIOClient c : clients) {
            System.out.println(" in room " + c.getSessionId().toString());
            for (Map<String, Account> cl : playersl){
                if(cl.containsKey(c.getSessionId().toString()) && c.getAllRooms().contains(id)){
                    Account vc = cl.get(c.getSessionId().toString());
                    System.out.println("Player " + vc.getUsername() + " in room " + c.getAllRooms());
                    players.add(vc);
                }
            }
        }

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
        Map<String, Account> player = new HashMap<>();
        Random random = new Random();
        long id = random.nextLong();
        Account tempAccount = new Account();
        System.out.println((String) body.get("username"));
        tempAccount.setId(id);
        tempAccount.setUsername((String) body.get("username"));
        player.put((String) body.get("session"), tempAccount);
        playerService.addPlayer(player);

        System.out.println("welcome" + playerService.getPlayers());
        return ResponseEntity.ok(tempAccount);
    }

    @PostMapping("/startgame/{id}")
    public String start(@PathVariable String id) {
        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(id).getClients();
        List<Map<String, Account>> playersl = playerService.getPlayers();
        List<Account> players = new ArrayList<Account>();

        for (com.corundumstudio.socketio.SocketIOClient c : clients) {
            System.out.println(" in room " + c.getSessionId().toString());
            for (Map<String, Account> cl : playersl){
                if(cl.containsKey(c.getSessionId().toString()) && c.getAllRooms().contains(id)){
                    Account vc = cl.get(c.getSessionId().toString());
                    System.out.println("Player " + vc.getUsername() + " in room " + c.getAllRooms());
                    players.add(vc);
                }
            }
        }
        game.setPlayers(players);
        phase = 0;
        return phases.get(phase);
    }

    @PostMapping("/keeptabs")
    public ResponseEntity<?> handlePayment(@RequestBody Map<String, Integer> request, @AuthenticationPrincipal UserDetails user) {
        System.out.println("Username: " + user.getUsername());
        int gems = request.get("gems");
        howmany = gems;
        forhow = request.get("price");
        return ResponseEntity.ok().body("Received!");
    }

    @GetMapping("/vn-pay-callback")
    public String payCallbackHandler(HttpServletRequest request, @AuthenticationPrincipal UserDetails user) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            Account s = accountService.getAccountByUsername(user.getUsername()).get();
            s.setCredit(s.getCredit() + howmany);
            accountService.save(s);
            String mail = "You just bought %d gems for %d ₫ in Tellstones the digital"
            .formatted(howmany, forhow);
            themailman.sendHtmlEmail(
                s.getEmail(),
                "Diamond Cheque",
                mail
            );
            return "redirect:/shop";
        } else {
            return "redirect:/shop/gem";
        }
    }
}
