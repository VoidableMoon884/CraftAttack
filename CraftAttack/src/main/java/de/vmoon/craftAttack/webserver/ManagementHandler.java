// Datei: src/main/java/de/vmoon/craftAttack/webserver/ManagementHandler.java
package de.vmoon.craftAttack.webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import de.vmoon.craftAttack.CraftAttack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.BanList;
import org.bukkit.BanEntry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManagementHandler implements HttpHandler {
    private final JavaPlugin plugin;
    public ManagementHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final SimpleDateFormat ISO8601 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
    static {
        ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "error", "Method Not Allowed");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
        String action = params.get("action");
        if (action == null) {
            sendJson(exchange, 400, "error", "Missing action parameter");
            return;
        }

        switch (action.toLowerCase()) {
            case "kick":
                handleKick(exchange, params);
                break;
            case "ban":
                handleBan(exchange, params);
                break;
            case "unban":
                handleUnban(exchange, params);
                break;
            case "banlist":
                handleBanList(exchange);
                break;
            case "restart":
                CraftAttack.getInstance().restartServer();
                sendJson(exchange, 200, "ok", "Server wird neugestartet.");
                break;
            default:
                sendJson(exchange, 400, "error", "Unknown action");
        }
    }

    private void handleKick(HttpExchange exchange, Map<String, String> params) throws IOException {
        String name = params.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            sendJson(exchange, 400, "error", "Missing name parameter");
            return;
        }

        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            sendJson(exchange, 404, "error", "Spieler " + name + " ist nicht online.");
            return;
        }

        String reason = params.getOrDefault("reason", "Vom Server gekickt");

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            target.kickPlayer(reason);
        });

        sendJson(exchange, 200, "ok", "Spieler " + name + " wurde gekickt: " + reason);
    }

    private void handleBan(HttpExchange exchange, Map<String, String> params) throws IOException {
        String name = params.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            sendJson(exchange, 400, "error", "Missing name parameter");
            return;
        }

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (banList.isBanned(name)) {
            sendJson(exchange, 200, "error", "Spieler " + name + " ist bereits gebannt.");
            return;
        }

        String reason = params.getOrDefault("reason", "Gebannt von Admin");
        // Cast null to Date, damit der richtige addBan overload gewählt wird:
        banList.addBan(name, reason, (Date) null, CraftAttack.getInstance().getName());

        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                online.kickPlayer(reason);
            });
        }

        sendJson(exchange, 200, "ok", "Spieler " + name + " wurde gebannt: " + reason);
    }

    private void handleUnban(HttpExchange exchange, Map<String, String> params) throws IOException {
        String name = params.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            sendJson(exchange, 400, "error", "Missing name parameter");
            return;
        }

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (!banList.isBanned(name)) {
            sendJson(exchange, 200, "error", "Spieler " + name + " ist nicht gebannt.");
            return;
        }

        banList.pardon(name);
        sendJson(exchange, 200, "ok", "Spieler " + name + " wurde entbannt.");
    }

    private void handleBanList(HttpExchange exchange) throws IOException {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        Set<BanEntry> entries = banList.getBanEntries();

        StringBuilder sb = new StringBuilder("[");
        for (BanEntry e : entries) {
            String expires = e.getExpiration() == null
                    ? "null"
                    : "\"" + ISO8601.format(e.getExpiration()) + "\"";
            sb.append("{")
                    .append("\"target\":\"").append(opt(e.getTarget())).append("\",")
                    .append("\"reason\":\"").append(opt(e.getReason())).append("\",")
                    .append("\"created\":\"").append(ISO8601.format(e.getCreated())).append("\",")
                    .append("\"expires\":").append(expires).append(",")
                    .append("\"source\":\"").append(opt(e.getSource())).append("\"")
                    .append("},");
        }
        if (!entries.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("]");

        sendRaw(exchange, 200, sb.toString());
    }

    private void sendJson(HttpExchange exchange, int code, String status, String message) throws IOException {
        String body = String.format("{\"status\":\"%s\",\"message\":\"%s\"}",
                status, message.replace("\"","\\\""));
        sendRaw(exchange, code, body);
    }

    private void sendRaw(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String,String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String,String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
            String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
            map.put(key, val);
        }
        return map;
    }

    private String opt(String s) {
        return s == null ? "" : s.replace("\"","\\\"");
    }
}