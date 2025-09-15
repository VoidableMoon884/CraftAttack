package de.vmoon.craftAttack.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    // Extra-Konfiguration (zum Beispiel für persistente Daten wie Spielerstatus)
    private FileConfiguration extraConfig;
    private File extraFile;
    private FileConfiguration maintenanceConfig;
    private File maintenanceFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createMaintenanceConfig("maintenance.yml");

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

    private void createMaintenanceConfig(String fileName) {
        maintenanceFile = new File(plugin.getDataFolder(), fileName);
        if (!maintenanceFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        maintenanceConfig = YamlConfiguration.loadConfiguration(maintenanceFile);
        plugin.getLogger().info("Maintenance Config geladen: " + (maintenanceConfig != null));
    }

    // Getter
    public FileConfiguration getMaintenanceConfig() {
        if (maintenanceConfig == null) {
            reloadMaintenanceConfig();
        }
        return maintenanceConfig;
    }

    // Reload
    public void reloadMaintenanceConfig() {
        if (maintenanceFile != null) {
            maintenanceConfig = YamlConfiguration.loadConfiguration(maintenanceFile);
        }
    }

    // Speichern
    public void saveMaintenanceConfig() {
        if (maintenanceConfig != null && maintenanceFile != null) {
            try {
                maintenanceConfig.save(maintenanceFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public SpawnTeleporterData getSpawnTeleporterData() {
        FileConfiguration config = getConfig();
        boolean enabled = config.getBoolean("SpawnTeleporter.enabled", true);
        String regionWorld = config.getString("SpawnTeleporter.region.world", "world");
        String sound = config.getString("SpawnTeleporter.sound", "ENTITY_ENDERMAN_TELEPORT");
        int x1 = config.getInt("SpawnTeleporter.region.x1", 0);
        int y1 = config.getInt("SpawnTeleporter.region.y1", 0);
        int z1 = config.getInt("SpawnTeleporter.region.z1", 0);
        int x2 = config.getInt("SpawnTeleporter.region.x2", 0);
        int y2 = config.getInt("SpawnTeleporter.region.y2", 0);
        int z2 = config.getInt("SpawnTeleporter.region.z2", 0);
        int delay = config.getInt("SpawnTeleporter.delay", 5);
        String teleportWorld = config.getString("SpawnTeleporter.teleport.world", "world");
        double teleX = config.getDouble("SpawnTeleporter.teleport.x", 0.0);
        double teleY = config.getDouble("SpawnTeleporter.teleport.y", 64.0);
        double teleZ = config.getDouble("SpawnTeleporter.teleport.z", 0.0);

        return new SpawnTeleporterData(
                enabled, regionWorld, x1, y1, z1, x2, y2, z2,
                delay, teleportWorld, teleX, teleY, teleZ,
                sound
        );
    }
}