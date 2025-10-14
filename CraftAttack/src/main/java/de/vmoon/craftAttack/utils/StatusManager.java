package de.vmoon.craftAttack.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatusManager {
    private static StatusManager instance;
    private static JavaPlugin pluginInstance;
    private ConfigManager configManager;  // Referenz auf den ConfigManager

    // Speichert für jeden Spieler den Statusnamen (z. B. "Admin")
    private final Map<UUID, String> playerStatusKeys = new HashMap<>();
    private final Scoreboard scoreboard;

    private StatusManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public static StatusManager getInstance() {
        if (instance == null) {
            instance = new StatusManager();
        }
        return instance;
    }

    /**
     * Initialisierung mit Plugin-Instanz und ConfigManager.
     */
    public static void init(JavaPlugin plugin, ConfigManager configManager) {
        pluginInstance = plugin;
        getInstance().configManager = configManager;
    }

    /**
     * Gibt alle Statusnamen mit den dazugehörigen Berechtigungen zurück.
     */
    public Map<String, String> getStatusPermissions() {
        Map<String, String> permissions = new HashMap<>();
        if (pluginInstance == null) {
            return permissions;
        }
        ConfigurationSection statusesSection = pluginInstance.getConfig().getConfigurationSection("statuses");
        if (statusesSection == null) {
            return permissions;
        }
        for (String statusKey : statusesSection.getKeys(false)) {
            permissions.put(statusKey, "ca.status." + statusKey.toLowerCase());
        }
        return permissions;
    }

    /**
     * Setzt den Status eines Spielers anhand des Statusnamens.
     * Dieser wird intern gespeichert, in der Extra‑Datei persistiert und als Scoreboard-Präfix aktualisiert.
     */
    public void setPlayerStatus(Player player, String statusKey) {
        playerStatusKeys.put(player.getUniqueId(), statusKey);
        // Persistiere den Status in der Extra-Datei
        configManager.getExtraConfig().set(player.getUniqueId().toString(), statusKey);
        configManager.saveExtraConfig();

        String formattedPrefix = getFormattedStatus(statusKey);
        updatePlayerTeam(player, formattedPrefix);
    }

    /**
     * Gibt den formatierten Status (Präfix) zurück – basierend auf dem Statusnamen.
     */
    public String getPlayerStatus(Player player) {
        String statusKey = playerStatusKeys.getOrDefault(player.getUniqueId(), "");
        return getFormattedStatus(statusKey);
    }

    /**
     * Wandelt den Statusnamen in einen farbigen Präfix um, indem der JSON-String aus der Haupt-Config gelesen und übersetzt wird.
     */
    private String getFormattedStatus(String statusKey) {
        if (statusKey == null || statusKey.isEmpty() || pluginInstance == null) {
            return "";
        }
        String statusJson = pluginInstance.getConfig().getConfigurationSection("statuses").getString(statusKey, "");
        if (statusJson.isEmpty()) {
            return "";
        }
        return JsonColorConverter.convertJson(statusJson);
    }

    /**
     * Aktualisiert das Scoreboard-Team des Spielers.
     */
    private void updatePlayerTeam(Player player, String prefix) {
        String teamName = "status_" + player.getUniqueId().toString().substring(0, 8);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        team.setPrefix(prefix + " ");
        team.addEntry(player.getName());
    }

    /**
     * Wird beim Join (oder Reload) aufgerufen.
     * Der gespeicherte Status wird aus der Extra‑Datei gelesen,
     * in die interne Map übernommen und das Scoreboard aktualisiert.
     */
    public void updatePlayerOnJoin(Player player) {
        String storedStatus = configManager.getExtraConfig().getString(player.getUniqueId().toString(), "");
        playerStatusKeys.put(player.getUniqueId(), storedStatus);
        updatePlayerTeam(player, getFormattedStatus(storedStatus));
    }

    /**
     * Entfernt den Status eines Spielers (intern, im Scoreboard und in der Extra‑Datei).
     */
    public void removePlayer(Player player) {
        playerStatusKeys.remove(player.getUniqueId());
        configManager.getExtraConfig().set(player.getUniqueId().toString(), null);
        configManager.saveExtraConfig();

        String teamName = "status_" + player.getUniqueId().toString().substring(0, 8);
        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }
}

