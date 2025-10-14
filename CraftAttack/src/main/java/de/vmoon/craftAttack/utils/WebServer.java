package de.vmoon.craftAttack.utils;

import com.sun.net.httpserver.HttpServer;
import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.webserver.*;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class WebServer {

    private final JavaPlugin plugin;
    private final int          port;
    private       HttpServer   server;

    public WebServer(JavaPlugin plugin, int port) {
        this.plugin = plugin;
        this.port   = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            CraftAttack main = (CraftAttack) plugin;

            if (main.getConfigManager().isApiEnabled()) {
                server.createContext("/api/whitelist", new ApiHandler());
                server.createContext("/api/management", new ManagementHandler());
                server.createContext("/api/maintenance", new MaintenanceHandler((CraftAttack) plugin));
            }
            if (main.getConfigManager().isWebServerEnabled()) {
                server.createContext("/", new ResourceHandler(plugin));
            }

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            plugin.getLogger().info("HTTP-Server läuft auf Port " + port);

        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Starten des HTTP-Servers: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Deine bestehende Whitelist-API
     */
    private class ApiHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

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
                case "list":
                    handleList(exchange);
                    break;
                case "check":
                    handleCheck(exchange, params);
                    break;
                case "add":
                    handleModify(exchange, params, true);
                    break;
                case "remove":
                    handleModify(exchange, params, false);
                    break;
                default:
                    sendJson(exchange, 400, "error", "Unknown action");
            }
        }

        private void handleList(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            Collection<OfflinePlayer> players = plugin.getServer().getWhitelistedPlayers();
            StringBuilder sb = new StringBuilder("[");
            for (OfflinePlayer p : players) {
                sb.append("{\"uuid\":\"").append(p.getUniqueId()).append("\",")
                        .append("\"name\":\"").append(opt(p.getName())).append("\"},");
            }
            if (!players.isEmpty()) sb.setLength(sb.length() - 1);
            sb.append("]");
            sendRaw(exchange, 200, sb.toString());
        }

        private void handleCheck(com.sun.net.httpserver.HttpExchange exchange,
                                 Map<String, String> params) throws IOException {
            String uuidStr = params.get("uuid");
            if (uuidStr == null || uuidStr.isBlank()) {
                sendJson(exchange, 400, "error", "Missing uuid parameter");
                return;
            }

            boolean whitelisted = false;
            String  name        = null;
            try {
                UUID uuid = UUID.fromString(uuidStr);
                for (OfflinePlayer p : plugin.getServer().getWhitelistedPlayers()) {
                    if (p.getUniqueId().equals(uuid)) {
                        whitelisted = true;
                        name        = p.getName();
                        break;
                    }
                }
            } catch (IllegalArgumentException iae) {
                sendJson(exchange, 400, "error", "Invalid uuid format");
                return;
            }

            StringBuilder sb = new StringBuilder("{")
                    .append("\"status\":\"ok\",")
                    .append("\"whitelisted\":").append(whitelisted);
            if (whitelisted) {
                sb.append(",\"name\":\"").append(opt(name)).append("\"");
            }
            sb.append("}");
            sendRaw(exchange, 200, sb.toString());
        }

        private void handleModify(com.sun.net.httpserver.HttpExchange exchange,
                                  Map<String, String> params,
                                  boolean add) throws IOException {
            String name = params.getOrDefault("name", "").trim();
            if (name.isEmpty()) {
                sendJson(exchange, 400, "error", "Missing name parameter");
                return;
            }

            var server = plugin.getServer();
            var p = server.getOfflinePlayer(name);
            boolean currently = p.isWhitelisted();
            if (add && currently) {
                sendJson(exchange, 200, "error", "Spieler ist bereits gewhitelistet");
                return;
            }
            if (!add && !currently) {
                sendJson(exchange, 200, "error", "Spieler ist nicht gewhitelistet");
                return;
            }

            server.getScheduler().runTask(plugin, () -> {
                p.setWhitelisted(add);
                plugin.getLogger().info("Whitelist: \"" + name + "\" "
                        + (add ? "hinzugefügt" : "entfernt"));
            });

            sendJson(exchange, 200, "ok",
                    "Spieler " + (add ? "hinzugefügt" : "entfernt"));
        }

        private void sendJson(com.sun.net.httpserver.HttpExchange exchange,
                              int code, String status, String message) throws IOException {
            String body = String.format("{\"status\":\"%s\",\"message\":\"%s\"}",
                    status, message.replace("\"", "\\\""));
            sendRaw(exchange, code, body);
        }

        private void sendRaw(com.sun.net.httpserver.HttpExchange exchange,
                             int code, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type",
                    "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, String> parseQuery(String query)
                throws UnsupportedEncodingException {
            Map<String, String> map = new HashMap<>();
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

    /**
     * Serves static files from /web/** inside the JAR
     */
    private static class ResourceHandler
            implements com.sun.net.httpserver.HttpHandler {
        private final JavaPlugin plugin;

        ResourceHandler(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange)
                throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/index.html";
            }

            String resourcePath =
                    "web" + URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] data = readAllBytes(in);
            exchange.getResponseHeaders().set("Content-Type",
                    guessContentType(resourcePath));
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }

        private static String guessContentType(String name) {
            if (name.endsWith(".html"))    return "text/html; charset=UTF-8";
            if (name.endsWith(".css"))     return "text/css; charset=UTF-8";
            if (name.endsWith(".js"))      return "application/javascript; charset=UTF-8";
            if (name.endsWith(".png"))     return "image/png";
            if (name.endsWith(".jpg")||name.endsWith(".jpeg"))
                return "image/jpeg";
            if (name.endsWith(".json"))    return "application/json; charset=UTF-8";
            return "application/octet-stream";
        }

        private static byte[] readAllBytes(InputStream in)
                throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }
}