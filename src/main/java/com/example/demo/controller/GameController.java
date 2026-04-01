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
import lombok.Data;

import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Object;

import java.util.Collection;

import java.util.Random;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import tools.jackson.databind.ObjectMapper;

import com.example.demo.service.GameService;

@Controller
@RequestMapping("/tellstones")
public class GameController {

    private final SocketIOService socketservice;
    private final Client coreclient;
    List<Map<String, String>> playerlistlist = new ArrayList<Map<String, String>>();
    private final PlayerService playerService;
    private final GameService gameService;
    private final Emailservice themailman;

    private final AccountService accountService;

    public GameController(SocketIOService socketservice, Client coreclient, PlayerService playerService,
            Emailservice themailman, AccountService accountService, GameService gameService) {
            this.socketservice = socketservice;
            this.coreclient = coreclient;
            this.playerService = playerService;
            this.themailman = themailman;
            this.accountService = accountService;
            this.gameService = gameService;
    }

    public int howmany = 0;
    public int forhow = 0;
    private List<String> skins = List.of(
        "default",
        "black",
        "golden",
        "neon",
        "primitive"
    );
    int skinpointer = 2;
    private Map<String, String> skinlinks = new HashMap<>(
            Map.of(
                "Sword", "/textu/stones/%s/sword.png".formatted(skins.get(skinpointer)),
                "Shield", "/textu/stones/%s/shield.png".formatted(skins.get(skinpointer)),
                "Scale", "/textu/stones/%s/Scale.png".formatted(skins.get(skinpointer)),
                "Knight", "/textu/stones/%s/knight.png".formatted(skins.get(skinpointer)),
                "Hammer", "/textu/stones/%s/hammer.png".formatted(skins.get(skinpointer)),
                "Flag", "/textu/stones/%s/flag.png".formatted(skins.get(skinpointer)),
                "Crown", "/textu/stones/%s/crown.png".formatted(skins.get(skinpointer))
            )
    );
    private List<Stone> bag = Arrays.asList(
        new Stone(2, skinlinks.get("Sword"), true),
        new Stone(3, skinlinks.get("Shield"), true),
        new Stone(4, skinlinks.get("Scale"), true),
        new Stone(5, skinlinks.get("Knight"), true),
        new Stone(6, skinlinks.get("Hammer"), true),
        new Stone(7, skinlinks.get("Flag"), true),
        new Stone(8, skinlinks.get("Crown"), true)
    );
    private Stone[] theline = new Stone[7];
    private List<String> phases = Arrays.asList(
        "standby",
        "action",
        "choose",
        "end"
    );

    private int phase = 0;

    @Data
    public static class StartGameRequest {
        private String roomId;
        private Account me;
        private Account opponent;
    }

    @PostMapping("/StartGame")
    public ResponseEntity<?> startGame(@RequestBody StartGameRequest request) {
        Game game = null;
        List<Game> games = gameService.getGames();
        for (Game g : games) {
            if (g.getRoomCode().equals(request.getRoomId())) {
                game = g;
            }
        }
        if(game == null){
            game = new Game();
            game.setRoomCode(request.getRoomId());
            game.setMe(request.getMe());
            game.setOpponent(request.getOpponent());
            gameService.addGame(game);
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{actualid}/{id}/{userid}")
    public String backtologin(@PathVariable String id, Model model) {
        
        if(socketservice.getServer().getRoomOperations(id).getClients().isEmpty()){
            return "redirect:/";
        }
        model.addAttribute("roomId", id);
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

    @GetMapping("/room/{actualid}/{id}/{userid}")
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
        System.out.println("in room" + id);
        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(id).getClients();
        List<Map<String, Account>> playersl = playerService.getPlayers();
        List<Account> players = new ArrayList<Account>();

        for (var c : clients) {
            String pp = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
            System.out.println("getme" + pp);
            for (var cl : playersl){
                if(cl.containsKey(pp) && c.getAllRooms().contains(id)){
                    System.out.println("player " + pp + " in room " + id );
                    Account vc = cl.get(pp);
                    System.out.println("Player " + vc.getUsername() + " in room " + c.getAllRooms());
                    players.add(vc);
                }
            }
        }

        System.out.println(players);

        return ResponseEntity.ok(players);
    }

    @GetMapping("/getskin/{id}")
    public ResponseEntity<?> getskin(@PathVariable String id) {
        return ResponseEntity.ok(skinlinks);
    }

    @PostMapping("/settinggame")
    public ResponseEntity<?> deleteProduct(@RequestBody List<Map<String, String>> body) {
        body.forEach(stone -> {
            bag.forEach(bagstone -> {
                System.out.println("Value from key: " + stone.get("stoneid"));
                int stoneId = Integer.parseInt(stone.get("stoneid"));
                if(stoneId == bagstone.getId()){
                    theline[Integer.parseInt(stone.get("position"))] = bagstone;
                    System.out.println("Value from: " + stone.get("turnup"));
                    bagstone.setFaceup(stone.get("turnup").equals("true"));
                }
            });
        });
        return ResponseEntity.ok(theline);
    }

    @PostMapping("/setturn")
    public ResponseEntity<?> setturn(@RequestBody Map<String, String> body) {
        Game game = gameService.getGame(body.get("roomId"));
        Account opponent = game.getOpponent();
        Account selfPlayer = game.getMe();
        if(selfPlayer.getId().toString().equals(body.get("turn"))){
            game.setCurrentPlayerIndex(selfPlayer);
        }
        else{
            game.setCurrentPlayerIndex(opponent);
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/welcomeplayer")
    public ResponseEntity<?> welcomePlayer(@AuthenticationPrincipal UserDetails user) {
        Map<String, Account> player = new HashMap<>();
        Account tempAccount = accountService.getAccountByUsername(user.getUsername()).get();
        player.put(tempAccount.getUsername(), tempAccount);
        if(!playerService.getPlayers().contains(player)){
            playerService.addPlayer(player);
        }
        return ResponseEntity.ok(tempAccount);
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

    @GetMapping("/payos-callbacksuccess")
    public String payOSCallbackHandler(HttpServletRequest request, @AuthenticationPrincipal UserDetails user) {
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
    }

    @GetMapping("/payos-callbackfail")
    public String payOSfailCallbackHandler(HttpServletRequest request, @AuthenticationPrincipal UserDetails user) {
        return "redirect:/tellstone/shop";
    }
}
