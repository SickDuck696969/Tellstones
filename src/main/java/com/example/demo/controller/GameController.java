package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;
import com.example.demo.service.PlayerService;
import com.example.demo.service.stoneskinService;
import com.example.demo.model.Game;
import com.example.demo.model.Stone;
import com.example.demo.model.stoneskin;

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
import java.util.Set;

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

    private final stoneskinService stoneskinservice;

    public GameController(SocketIOService socketservice, Client coreclient, PlayerService playerService,
            Emailservice themailman, AccountService accountService, GameService gameService, stoneskinService stoneskinservice) {
            this.socketservice = socketservice;
            this.coreclient = coreclient;
            this.playerService = playerService;
            this.themailman = themailman;
            this.accountService = accountService;
            this.gameService = gameService;
            this.stoneskinservice = stoneskinservice;
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
            game.setMescore(0);
            game.setTheyscore(0);
            gameService.addGame(game);
        }
        for (int i = 0; i < theline.length; i++) {
            theline[i] = null;
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{actualid}/{id}/{userid}")
    public String backtologin(@PathVariable String id, Model model, @AuthenticationPrincipal UserDetails user) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        if(!"QUEUE".equals(id) && socketservice.getServer().getRoomOperations(id).getClients().isEmpty()){
            return "redirect:/";
        }
        model.addAttribute("roomId", id);
        model.addAttribute("stoneskins", stoneskinservice.findByBelong(s));
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

    @GetMapping("/queue")
    public String queueLobby(@AuthenticationPrincipal UserDetails user, Model model) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("roomId", "QUEUE");
        model.addAttribute("stoneskins", stoneskinservice.findByBelong(s));
        try {
            if (coreclient.socket == null) {
                coreclient.init();
            }
        }
        catch (Exception e) {
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
    public ResponseEntity<?> deleteProduct(@RequestBody List<Map<String, String>> body,  @AuthenticationPrincipal UserDetails user) {
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

    @PostMapping("/score")
    public ResponseEntity<?> score(@RequestBody Map<String, String> body) {
        Game game = gameService.getGame(body.get("room"));
        Account opponent = game.getOpponent();
        Account selfPlayer = game.getMe();
        if(opponent.getId().toString().equals(body.get("who"))){
            game.setTheyscore(game.getMescore() + Integer.parseInt((String) body.get("howmany")));
        } else {
            game.setMescore(game.getMescore() + Integer.parseInt((String) body.get("howmany")));
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/welcomeplayer")
    public ResponseEntity<?> welcomePlayer(@AuthenticationPrincipal UserDetails user) {
        Map<String, Account> player = new HashMap<>();
        Account tempAccount = accountService.getAccountByUsername(user.getUsername()).get();
        player.put(tempAccount.getUsername(), tempAccount);
        if(playerService.getPlayerByUsername(tempAccount.getUsername()) == null){
            playerService.addPlayer(player);
        }
        return ResponseEntity.ok(tempAccount);
    }

    @PostMapping("/setskins")
    public ResponseEntity<?> handlesPayment(@RequestBody Map<String, Long> request, @AuthenticationPrincipal UserDetails user) {
        skinpointer = request.get("stoneid").intValue();
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        Map<Account, stoneskin> fs = new HashMap<>();
        fs.put(s, stoneskinservice.findById(request.get("stoneid")).get());
        playerService.addSkin(fs);
        return ResponseEntity.ok().body(skinpointer);
    }

    @PostMapping("/keeptabs")
    public ResponseEntity<?> handlePayment(@RequestBody Map<String, Integer> request, @AuthenticationPrincipal UserDetails user) {
        System.out.println("Username: " + user.getUsername());
        int gems = request.get("gems");
        howmany = gems;
        forhow = request.get("price");
        return ResponseEntity.ok().body("Received!");
    }

    public static class BuyRequest {
        public int price;
        public String name;
        public  long id;
    }

    public static class ExchangeSkinRequest {
        public String skin;
    }

    public static class SurrenderRequest {
        public String roomId;
    }

    @PostMapping("/buy")
        public ResponseEntity<?> buy(@RequestBody BuyRequest request, @AuthenticationPrincipal UserDetails user) {
        int price = request.price;
        long id = request.id;
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        if(s.getCredit() >= price){
            stoneskin temp = new stoneskin();
            temp.setId(id);
            temp.setName(request.name);
            temp.setBelong(s);
            stoneskinservice.save(temp);
            s.setCredit(s.getCredit() - price);
            accountService.save(s);
            return ResponseEntity.ok("Bought successfully");
        }
        else{
            return ResponseEntity.ok("Get more gems");
        }
    }

    @PostMapping("/exchange-skin")
    public ResponseEntity<?> exchangeSkin(@RequestBody ExchangeSkinRequest request, @AuthenticationPrincipal UserDetails user) {
        if (request == null || request.skin == null) {
            return ResponseEntity.badRequest().body("Invalid skin");
        }

        Map<String, Long> skinIdByCode = Map.of(
                "black", 1L,
                "golden", 2L,
                "neon", 3L,
                "primitive", 4L
        );
        Map<String, String> skinNameByCode = Map.of(
                "black", "Black Skin",
                "golden", "Golden Skin",
                "neon", "Neon Skin",
                "primitive", "Primitive Skin"
        );

        String skinCode = request.skin.toLowerCase();
        if (!skinIdByCode.containsKey(skinCode)) {
            return ResponseEntity.badRequest().body("Unknown skin");
        }

        Account account = accountService.getAccountByUsername(user.getUsername()).get();
        Set<Long> ownedSkinIds = stoneskinservice.findByBelong(account).stream()
                .map(stoneskin::getId)
                .collect(java.util.stream.Collectors.toSet());
        long skinId = skinIdByCode.get(skinCode);

        Map<String, java.lang.Object> response = new HashMap<>();
        response.put("fragmentCount", account.getFragment() == null ? 0 : account.getFragment());
        response.put("ownedSkinIds", ownedSkinIds);

        if (ownedSkinIds.contains(skinId)) {
            response.put("message", "Already unlocked");
            return ResponseEntity.ok(response);
        }

        if (account.getFragment() == null || account.getFragment() < 10) {
            response.put("message", "Not enough fragments");
            return ResponseEntity.ok(response);
        }

        account.setFragment(account.getFragment() - 10);
        accountService.save(account);

        stoneskin unlockedSkin = new stoneskin();
        unlockedSkin.setId(skinId);
        unlockedSkin.setName(skinNameByCode.get(skinCode));
        unlockedSkin.setBelong(account);
        stoneskinservice.save(unlockedSkin);

        response.put("message", "Exchange successful");
        response.put("fragmentCount", account.getFragment());
        response.put("ownedSkinIds", stoneskinservice.findByBelong(account).stream()
                .map(stoneskin::getId)
                .toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(@RequestBody SurrenderRequest request, @AuthenticationPrincipal UserDetails user) {
        if (request == null || request.roomId == null || request.roomId.isBlank()) {
            return ResponseEntity.badRequest().body("Room not found");
        }

        Game game = gameService.getGame(request.roomId);
        if (game == null) {
            return ResponseEntity.badRequest().body("Game not found");
        }

        Account surrenderingPlayer = accountService.getAccountByUsername(user.getUsername()).get();
        Account winner = game.getMe().getUsername().equals(surrenderingPlayer.getUsername()) ? game.getOpponent() : game.getMe();
        Account loser = surrenderingPlayer;

        GameService.MatchResult existingResult = gameService.getMatchResult(request.roomId);
        if (existingResult == null) {
            existingResult = createMatchResult(request.roomId, winner, loser, "Victory By Surrender");
            gameService.saveMatchResult(request.roomId, existingResult);
        }

        Map<String, String> payload = Map.of(
                "winnerUsername", existingResult.getWinnerUsername(),
                "loserUsername", existingResult.getLoserUsername(),
                "winnerRedirect", "/tellstones/result/" + request.roomId
        );

        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(request.roomId).getClients();
        for (com.corundumstudio.socketio.SocketIOClient client : clients) {
            String connectedUser = java.net.URLDecoder.decode(client.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
            if (connectedUser.equals(existingResult.getWinnerUsername())) {
                client.sendEvent("matchResult", payload);
            }
        }

        return ResponseEntity.ok(Map.of("redirectUrl", "/", "winnerRedirect", "/tellstones/result/" + request.roomId));
    }

    @GetMapping("/result/{roomId}")
    public String resultPage(@PathVariable String roomId, @AuthenticationPrincipal UserDetails user, Model model) {
        GameService.MatchResult result = gameService.getMatchResult(roomId);
        if (result == null || !result.getWinnerUsername().equals(user.getUsername())) {
            return "redirect:/";
        }

        Account account = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("winnerName", result.getWinnerUsername());
        model.addAttribute("resultReason", result.getResultReason());
        model.addAttribute("rewardType", result.getRewardType());
        model.addAttribute("rewardAmount", result.getRewardAmount());
        model.addAttribute("currentGemBalance", account.getCredit());
        model.addAttribute("currentFragmentBalance", account.getFragment() == null ? 0 : account.getFragment());
        return "tellstone/result";
    }

    private GameService.MatchResult createMatchResult(String roomId, Account winner, Account loser, String resultReason) {
        Random random = new Random();
        boolean grantFragment = random.nextBoolean();
        int rewardAmount = grantFragment ? 1 : (random.nextInt(141) + 60);

        winner.setMatchHistory(appendMatchHistory(winner.getMatchHistory(),
                "Win vs %s by surrender".formatted(loser.getUsername())));
        loser.setMatchHistory(appendMatchHistory(loser.getMatchHistory(),
                "Lose vs %s by surrender".formatted(winner.getUsername())));

        if (grantFragment) {
            winner.setFragment((winner.getFragment() == null ? 0 : winner.getFragment()) + rewardAmount);
        } else {
            winner.setCredit((winner.getCredit() == null ? 0 : winner.getCredit()) + rewardAmount);
        }

        accountService.save(winner);
        accountService.save(loser);

        GameService.MatchResult result = new GameService.MatchResult();
        result.setRoomCode(roomId);
        result.setWinnerId(winner.getId());
        result.setWinnerUsername(winner.getUsername());
        result.setLoserUsername(loser.getUsername());
        result.setResultReason(resultReason);
        result.setRewardType(grantFragment ? "fragment" : "gems");
        result.setRewardAmount(rewardAmount);
        result.setUpdatedGemTotal(winner.getCredit() == null ? 0 : winner.getCredit());
        result.setUpdatedFragmentTotal(winner.getFragment() == null ? 0 : winner.getFragment());
        return result;
    }

    private List<String> appendMatchHistory(List<String> history, String entry) {
        List<String> updatedHistory = history == null ? new ArrayList<>() : new ArrayList<>(history);
        updatedHistory.add(0, entry);
        return updatedHistory;
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
