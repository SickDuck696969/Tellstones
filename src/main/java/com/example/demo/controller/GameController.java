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
    private static final Map<String, Long> SKIN_ID_BY_CODE = Map.of(
            "default", 0L,
            "black", 1L,
            "golden", 2L,
            "neon", 3L,
            "primitive", 4L
    );
    private static final Map<Long, String> SKIN_CODE_BY_ID = Map.of(
            0L, "default",
            1L, "black",
            2L, "golden",
            3L, "neon",
            4L, "primitive"
    );
    private static final Map<String, String> SKIN_NAME_BY_CODE = Map.of(
            "default", "Default",
            "black", "Black",
            "golden", "Golden",
            "neon", "Neon",
            "primitive", "Primitive"
    );
    private List<String> phases = Arrays.asList(
        "standby",
        "action",
        "choose",
        "end"
    );

    private int phase = 0;

    public static class StartGameRequest {
        private String roomId;
        private Account me;
        private Account opponent;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
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
    }

    @PostMapping("/StartGame")
    public ResponseEntity<?> startGame(@RequestBody StartGameRequest request) {
        Game game = ensureGame(request.getRoomId(), request.getMe(), request.getOpponent());
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
        model.addAttribute("ownedSkinIds", getOwnedSkinIds(s));
        model.addAttribute("selectedStoneSkin", getSelectedStoneSkinCode(s));
        model.addAttribute("accountId", s.getId());
        model.addAttribute("accountUsername", s.getUsername());
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
        model.addAttribute("ownedSkinIds", getOwnedSkinIds(s));
        model.addAttribute("selectedStoneSkin", getSelectedStoneSkinCode(s));
        model.addAttribute("accountId", s.getId());
        model.addAttribute("accountUsername", s.getUsername());
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
        List<Account> players = new ArrayList<Account>();

        for (var c : clients) {
            String pp = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
            System.out.println("getme" + pp);
            if (c.getAllRooms().contains(id)) {
                Account vc = accountService.getAccountByUsername(pp).orElse(null);
                if (vc == null) {
                    continue;
                }
                boolean exists = players.stream().anyMatch(player -> player.getUsername().equals(vc.getUsername()));
                if (!exists) {
                    System.out.println("player " + pp + " in room " + id);
                    System.out.println("Player " + vc.getUsername() + " in room " + c.getAllRooms());
                    players.add(vc);
                }
            }
        }

        System.out.println(players);

        return ResponseEntity.ok(players);
    }

    @GetMapping("/getskin/{id}")
    public ResponseEntity<?> getskin(@PathVariable String id, @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.ok(buildSkinLinks("default"));
        }
        Account account = accountService.getAccountByUsername(user.getUsername()).orElse(null);
        return ResponseEntity.ok(buildSkinLinks(getSelectedStoneSkinCode(account)));
    }

    @PostMapping("/settinggame")
    public ResponseEntity<?> deleteProduct(@RequestBody List<Map<String, String>> body, @RequestParam(value = "roomId", required = false) String roomId, @AuthenticationPrincipal UserDetails user) {
        String targetRoomId = roomId;
        if ((targetRoomId == null || targetRoomId.isBlank()) && user != null) {
            targetRoomId = resolveRoomForUser(user.getUsername());
        }
        Game game = ensureGame(targetRoomId, null, null);
        Stone[] line = new Stone[7];
        body.forEach(stone -> {
            int stoneId = Integer.parseInt(stone.get("stoneid"));
            int position = Integer.parseInt(stone.get("position"));
            boolean faceUp = stone.get("turnup").equals("true");
            line[position] = createStone(stoneId, faceUp);
        });
        game.setLine(line);
        return ResponseEntity.ok(game.getLine());
    }

    @PostMapping("/setturn")
    public ResponseEntity<?> setturn(@RequestBody Map<String, String> body) {
        Game game = ensureGame(body.get("roomId"), null, null);
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
        Game game = ensureGame(body.get("room"), null, null);
        Account opponent = game.getOpponent();
        Account selfPlayer = game.getMe();
        if(opponent.getId().toString().equals(body.get("who"))){
            game.setTheyscore(game.getTheyscore() + Integer.parseInt((String) body.get("howmany")));
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
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        String requestedCode = resolveSkinCode(request);
        if (requestedCode == null) {
            return ResponseEntity.badRequest().body("Unknown skin");
        }

        if (!canUseSkin(s, requestedCode)) {
            return ResponseEntity.badRequest().body("Skin not owned");
        }

        playerService.setSelectedSkin(s.getUsername(), requestedCode);
        Map<String, Object> response = new HashMap<>();
        response.put("selectedSkin", requestedCode);
        response.put("links", buildSkinLinks(requestedCode));
        return ResponseEntity.ok(response);
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
        public String selfUsername;
        public String opponentUsername;
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

        String skinCode = request.skin.toLowerCase();
        if (!SKIN_ID_BY_CODE.containsKey(skinCode) || "default".equals(skinCode)) {
            return ResponseEntity.badRequest().body("Unknown skin");
        }

        Account account = accountService.getAccountByUsername(user.getUsername()).get();
        Set<Long> ownedSkinIds = stoneskinservice.findByBelong(account).stream()
                .map(stoneskin::getId)
                .collect(java.util.stream.Collectors.toSet());
        long skinId = SKIN_ID_BY_CODE.get(skinCode);

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
        unlockedSkin.setName(SKIN_NAME_BY_CODE.get(skinCode));
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

        Game game = ensureGame(request.roomId, null, null);
        if (game == null) {
            return ResponseEntity.badRequest().body("Game not found");
        }

        Account surrenderingPlayer = accountService.getAccountByUsername(user.getUsername()).get();
        syncPlayersFromRoom(game, request.roomId, surrenderingPlayer);
        hydratePlayersForSurrender(game, request, surrenderingPlayer);

        if (game.getMe() == null || game.getOpponent() == null) {
            return ResponseEntity.badRequest().body("Match players not ready");
        }

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

    private void hydratePlayersForSurrender(Game game, SurrenderRequest request, Account surrenderingPlayer) {
        if (game == null) {
            return;
        }

        Account me = game.getMe();
        Account opponent = game.getOpponent();

        if (me == null) {
            me = surrenderingPlayer;
        }

        if (request != null) {
            if (me == null && request.selfUsername != null && !request.selfUsername.isBlank()) {
                me = accountService.getAccountByUsername(request.selfUsername).orElse(null);
            }

            if (opponent == null && request.opponentUsername != null && !request.opponentUsername.isBlank()) {
                opponent = accountService.getAccountByUsername(request.opponentUsername).orElse(null);
            }
        }

        if (opponent == null) {
            List<Account> players = getAccountsInRoom(request.roomId);
            for (Account player : players) {
                if (me == null || !player.getUsername().equals(me.getUsername())) {
                    opponent = player;
                    break;
                }
            }
        }

        if (opponent == null) {
            for (Game existingGame : gameService.getGames()) {
                if (existingGame == null || existingGame == game) {
                    continue;
                }
                if (!request.roomId.equals(existingGame.getRoomCode())) {
                    continue;
                }
                if (existingGame.getMe() != null
                        && (me == null || !existingGame.getMe().getUsername().equals(me.getUsername()))) {
                    opponent = existingGame.getMe();
                    break;
                }
                if (existingGame.getOpponent() != null
                        && (me == null || !existingGame.getOpponent().getUsername().equals(me.getUsername()))) {
                    opponent = existingGame.getOpponent();
                    break;
                }
            }
        }

        if (me != null) {
            game.setMe(me);
        }
        if (opponent != null) {
            game.setOpponent(opponent);
        }
    }

    private Game ensureGame(String roomId, Account me, Account opponent) {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }

        Game game = gameService.getGame(roomId);
        if (game == null) {
            game = new Game();
            game.setRoomCode(roomId);
            game.setLine(new Stone[7]);
            gameService.addGame(game);
        }

        if (me != null) {
            game.setMe(me);
        }
        if (opponent != null) {
            game.setOpponent(opponent);
        }

        if (game.getMe() == null || game.getOpponent() == null) {
            List<Account> players = getAccountsInRoom(roomId);
            if (game.getMe() == null && !players.isEmpty()) {
                game.setMe(players.get(0));
            }
            if (game.getOpponent() == null && players.size() > 1) {
                Account first = game.getMe();
                for (Account player : players) {
                    if (first == null || !player.getUsername().equals(first.getUsername())) {
                        game.setOpponent(player);
                        break;
                    }
                }
            }
        }

        if (game.getLine() == null || game.getLine().length != 7) {
            game.setLine(new Stone[7]);
        }

        return game;
    }

    private List<Account> getAccountsInRoom(String roomId) {
        Collection<com.corundumstudio.socketio.SocketIOClient> clients = socketservice.getServer().getRoomOperations(roomId).getClients();
        List<Account> players = new ArrayList<>();

        for (var c : clients) {
            String username = java.net.URLDecoder.decode(c.getHandshakeData().getSingleUrlParam("userId"), StandardCharsets.UTF_8);
            if (!c.getAllRooms().contains(roomId)) {
                continue;
            }
            Account account = accountService.getAccountByUsername(username).orElse(null);
            if (account == null) {
                continue;
            }
            boolean exists = players.stream().anyMatch(p -> p.getUsername().equals(account.getUsername()));
            if (!exists) {
                players.add(account);
            }
        }
        return players;
    }

    private void syncPlayersFromRoom(Game game, String roomId, Account currentPlayer) {
        List<Account> players = getAccountsInRoom(roomId);

        if (players.isEmpty()) {
            if (game.getMe() == null) {
                game.setMe(currentPlayer);
            }
            return;
        }

        Account me = null;
        Account opponent = null;

        for (Account player : players) {
            if (currentPlayer != null && player.getUsername().equals(currentPlayer.getUsername())) {
                me = player;
            } else if (opponent == null) {
                opponent = player;
            }
        }

        if (me == null && currentPlayer != null) {
            me = currentPlayer;
        }

        if (opponent == null) {
            for (Account player : players) {
                if (me == null || !player.getUsername().equals(me.getUsername())) {
                    opponent = player;
                    break;
                }
            }
        }

        if (game.getMe() == null || currentPlayer != null) {
            game.setMe(me);
        }
        if (game.getOpponent() == null || (opponent != null && !opponent.getUsername().equals(game.getMe() != null ? game.getMe().getUsername() : ""))) {
            game.setOpponent(opponent);
        }
    }

    private String resolveRoomForUser(String username) {
        for (Game game : gameService.getGames()) {
            if ((game.getMe() != null && username.equals(game.getMe().getUsername()))
                    || (game.getOpponent() != null && username.equals(game.getOpponent().getUsername()))) {
                return game.getRoomCode();
            }
        }
        return null;
    }

    private Stone createStone(int stoneId, boolean faceUp) {
        Map<String, String> skinLinks = buildSkinLinks("default");
        String icon = switch (stoneId) {
            case 2 -> skinLinks.get("Sword");
            case 3 -> skinLinks.get("Shield");
            case 4 -> skinLinks.get("Scale");
            case 5 -> skinLinks.get("Knight");
            case 6 -> skinLinks.get("Hammer");
            case 7 -> skinLinks.get("Flag");
            case 8 -> skinLinks.get("Crown");
            default -> null;
        };

        Stone stone = new Stone();
        stone.setId((long) stoneId);
        stone.setIcon(icon);
        stone.setFaceup(faceUp);
        return stone;
    }

    private List<Long> getOwnedSkinIds(Account account) {
        if (account == null) {
            return List.of();
        }
        return stoneskinservice.findByBelong(account).stream()
                .map(stoneskin::getId)
                .filter(SKIN_CODE_BY_ID::containsKey)
                .toList();
    }

    private boolean canUseSkin(Account account, String skinCode) {
        if (skinCode == null) {
            return false;
        }
        if ("default".equals(skinCode)) {
            return true;
        }
        Long skinId = SKIN_ID_BY_CODE.get(skinCode);
        return skinId != null && getOwnedSkinIds(account).contains(skinId);
    }

    private String getSelectedStoneSkinCode(Account account) {
        String selectedCode = account == null ? "default" : playerService.getSelectedSkin(account.getUsername());
        return canUseSkin(account, selectedCode) ? selectedCode : "default";
    }

    private String resolveSkinCode(Map<String, Long> request) {
        if (request == null) {
            return null;
        }
        Long skinId = request.get("stoneid");
        if (skinId == null) {
            return null;
        }
        return SKIN_CODE_BY_ID.get(skinId);
    }

    private Map<String, String> buildSkinLinks(String skinCode) {
        String safeCode = SKIN_ID_BY_CODE.containsKey(skinCode) ? skinCode : "default";
        return new HashMap<>(Map.of(
                "Sword", "/textu/stones/%s/sword.png".formatted(safeCode),
                "Shield", "/textu/stones/%s/shield.png".formatted(safeCode),
                "Scale", "/textu/stones/%s/scale.png".formatted(safeCode),
                "Knight", "/textu/stones/%s/knight.png".formatted(safeCode),
                "Hammer", "/textu/stones/%s/hammer.png".formatted(safeCode),
                "Flag", "/textu/stones/%s/flag.png".formatted(safeCode),
                "Crown", "/textu/stones/%s/crown.png".formatted(safeCode)
        ));
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
