package de.vmoon.craftAttack.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Kopiere die Default-Config (aus dem Jar) ins Pluginverzeichnis, falls sie noch nicht vorhanden ist
        plugin.saveDefaultConfig();
        // Ergänze vorhandene Config um fehlende Schlüssel
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public double getInitialBorder() {
        return getConfig().getDouble("worldborder.initial", 100.0);
    }

    public double getTargetBorder() {
        return getConfig().getDouble("worldborder.target", 20000.0);
    }

    public double getWorldBorderCenterX() {
        return getConfig().getDouble("worldborder.center.x", 0.0);
    }

    public double getWorldBorderCenterZ() {
        return getConfig().getDouble("worldborder.center.z", 0.0);
    }

    public double getTeleportX() {
        return getConfig().getDouble("teleport.x", 100.0);
    }

    public double getTeleportY() {
        return getConfig().getDouble("teleport.y", 64.0);
    }

    public double getTeleportZ() {
        return getConfig().getDouble("teleport.z", 100.0);
    }

    public String getWelcomeMessage() {
        return getConfig().getString("welcome", "&aWillkommen auf CraftAttack! Viel Spaß beim Spiel!");
    }

    // Neuer Wert: Ist die Tabtext-Funktionalität aktiviert?
    public boolean isTabTextEnabled() {
        return getConfig().getBoolean("tabtext", true);
    }
}