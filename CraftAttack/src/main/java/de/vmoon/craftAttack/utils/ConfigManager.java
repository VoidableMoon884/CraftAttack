package de.vmoon.craftAttack.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Konfigurationsmanager, der die Hauptkonfiguration (config.yml)
 * sowie eine separate Extra-Konfigurationsdatei (z. B. playerstatuses.yml)
 * verwaltet. Beim ersten Start wird die Datei aus dem JAR übernommen.
 * Beim Neustart werden nur neue Schlüssel ergänzt, während benutzerdefinierte Werte
 * erhalten bleiben.
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    // Extra-Konfiguration (zum Beispiel für persistente Daten wie Spielerstatus)
    private FileConfiguration extraConfig;
    private File extraFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Hauptconfig: Wenn noch keine config.yml existiert, wird sie aus dem JAR kopiert.
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        // Lade die bestehende config.yml
        config = YamlConfiguration.loadConfiguration(configFile);

        // Lade die internen Defaults aus der im Plugin-JAR enthaltenen config.yml,
        // um neue Schlüssel zu ergänzen, ohne bestehende Werte zu überschreiben.
        InputStreamReader defaults = new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8);
        if (defaults != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defaults);
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Erstelle und lade die Extra-Konfiguration (zum Beispiel playerstatuses.yml)
        createExtraConfig("playerstatuses.yml");
    }

    // Hauptkonfiguration abrufen
    public FileConfiguration getConfig() {
        return config;
    }

    // Beispielhafte Getter-Methoden für Werte der Hauptkonfiguration
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

    // Neuer Wert: Gibt an, ob der Tabtext aktiviert ist.
    public boolean isTabTextEnabled() {
        return getConfig().getBoolean("tabtext", true);
    }

    public int getSpawnAreaX1() {
        return getConfig().getInt("spawnArea.x1", 0);
    }

    public int getSpawnAreaY1() {
        return getConfig().getInt("spawnArea.y1", 0);
    }

    public int getSpawnAreaZ1() {
        return getConfig().getInt("spawnArea.z1", 0);
    }

    public int getSpawnAreaX2() {
        return getConfig().getInt("spawnArea.x2", 0);
    }

    public int getSpawnAreaY2() {
        return getConfig().getInt("spawnArea.y2", 0);
    }

    public int getSpawnAreaZ2() {
        return getConfig().getInt("spawnArea.z2", 0);
    }

    // Methoden für die Extra-Konfiguration (z. B. playerstatuses.yml)

    /**
     * Gibt die Extra-Konfiguration zurück. Falls diese noch nicht geladen wurde,
     * wird sie neu eingelesen.
     */
    public FileConfiguration getExtraConfig() {
        if (extraConfig == null) {
            reloadExtraConfig();
        }
        return extraConfig;
    }

    /**
     * Lädt die Extra-Konfigurationsdatei neu.
     */
    public void reloadExtraConfig() {
        if (extraFile != null) {
            extraConfig = YamlConfiguration.loadConfiguration(extraFile);
        }
    }

    /**
     * Erstellt und lädt die Extra-Konfiguration (zum Beispiel playerstatuses.yml).
     * Falls die Datei nicht existiert, wird versucht, sie aus den Ressourcen zu kopieren.
     *
     * @param fileName der Name der Extra-Datei, z. B. "playerstatuses.yml"
     */
    private void createExtraConfig(String fileName) {
        extraFile = new File(plugin.getDataFolder(), fileName);
        if (!extraFile.exists()) {
            try {
                extraFile.getParentFile().mkdirs();
                // Kopiere die Datei aus dem JAR in das Plugin-Verzeichnis (falls vorhanden)
                plugin.saveResource(fileName, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        extraConfig = YamlConfiguration.loadConfiguration(extraFile);
        plugin.getLogger().info("Extra config geladen: " + (extraConfig != null));
    }

    /**
     * Speichert die Extra-Konfiguration in die Datei.
     */
    public void saveExtraConfig() {
        if (extraConfig != null && extraFile != null) {
            try {
                extraConfig.save(extraFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Gibt zurück, ob die JSON-API aktiviert ist.
     */
    public boolean isApiEnabled() {
        return getConfig().getBoolean("enable_api", false);
    }

    /**
     * Gibt zurück, ob das statische Web-Interface aktiviert ist.
     */
    public boolean isWebServerEnabled() {
        return getConfig().getBoolean("enable_webserver", false);
    }

    /**
     * Lies den konfigurierten Server-Port aus.
     */
    public int getServerPort() {
        return getConfig().getInt("server.port", 8020);
    }
    public void saveConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Speichern der config.yml");
            e.printStackTrace();
        }
    }
}