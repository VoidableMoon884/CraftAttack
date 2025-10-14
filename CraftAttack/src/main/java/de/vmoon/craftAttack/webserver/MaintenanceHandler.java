package de.vmoon.craftAttack.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.MaintenanceManager;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MaintenanceHandler implements HttpHandler {

    private final CraftAttack plugin;

    public MaintenanceHandler(CraftAttack plugin) {
        this.plugin = plugin;
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
            case "status":
                handleMaintenanceStatus(exchange);
                break;
            case "set":
                handleMaintenanceSet(exchange, params);
                break;
            case "add":
                handleMaintenanceModify(exchange, params, true);
                break;
            case "remove":
                handleMaintenanceModify(exchange, params, false);
                break;
            default:
                sendJson(exchange, 400, "error", "Unknown action");
        }
    }

    private void handleMaintenanceStatus(HttpExchange exchange) throws IOException {
        MaintenanceManager mm = plugin.getMaintenanceManager();
        boolean active = mm.isActive();
        Set<UUID> allowed = mm.getAllowedPlayers();

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"status\":\"ok\",");
        sb.append("\"active\":").append(active).append(",");
        sb.append("\"allowedPlayers\":[");

        boolean first = true;
        for (UUID uuid : allowed) {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
            if (!first) sb.append(",");
            sb.append("{\"uuid\":\"").append(uuid.toString()).append("\",")
                    .append("\"name\":\"").append(opt(player.getName())).append("\"}");
            first = false;
        }
        sb.append("]}");

        sendRaw(exchange, 200, sb.toString());
    }

    private void handleMaintenanceSet(HttpExchange exchange, Map<String, String> params) throws IOException {
        String activeStr = params.get("active");
        if (activeStr == null) {
            sendJson(exchange, 400, "error", "Missing active parameter");
            return;
        }

        boolean active = Boolean.parseBoolean(activeStr);
        MaintenanceManager mm = plugin.getMaintenanceManager();
        mm.setActive(active);

        sendJson(exchange, 200, "ok", "Maintenance mode set to " + active);
    }

    private void handleMaintenanceModify(HttpExchange exchange,
                                         Map<String, String> params, boolean add) throws IOException {
        String name = params.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            sendJson(exchange, 400, "error", "Missing name parameter");
            return;
        }

        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
        MaintenanceManager mm = plugin.getMaintenanceManager();

        if (add) {
            if (mm.isPlayerAllowed(player)) {
                sendJson(exchange, 200, "error", "Player is already allowed during maintenance");
                return;
            }
            mm.addPlayer(player);
            sendJson(exchange, 200, "ok", "Player added to maintenance whitelist");
        } else {
            if (!mm.isPlayerAllowed(player)) {
                sendJson(exchange, 200, "error", "Player is not in maintenance whitelist");
                return;
            }
            mm.removePlayer(player);
            sendJson(exchange, 200, "ok", "Player removed from maintenance whitelist");
        }
    }

    private void sendJson(HttpExchange exchange, int code, String status, String message) throws IOException {
        String body = String.format("{\"status\":\"%s\",\"message\":\"%s\"}",
                status, message.replace("\"", "\\\""));
        sendRaw(exchange, code, body);
    }

    private void sendRaw(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String query)
            throws UnsupportedEncodingException {
        Map<String, String> map = new java.util.HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, idx),
                    StandardCharsets.UTF_8.name());
            String val = URLDecoder.decode(pair.substring(idx + 1),
                    StandardCharsets.UTF_8.name());
            map.put(key, val);
        }
        return map;
    }

    private String opt(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
