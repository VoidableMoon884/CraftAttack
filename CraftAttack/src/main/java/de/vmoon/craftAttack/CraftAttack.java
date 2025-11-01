package de.vmoon.craftAttack;

import de.vmoon.craftAttack.commands.*;
import de.vmoon.craftAttack.listeners.*;
import de.vmoon.craftAttack.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftAttack extends JavaPlugin {

    private static CraftAttack instance;
    private ConfigManager configManager;
    private pvpCommand pvpCmd;
    private SpawnBoostListener spawnBoostListener;
    private WebServer webServer;
    private SpawnTeleporterListener spawnTeleporterListener;
    private MotdListener motdListener;
    private MaintenanceManager maintenanceManager;
    private MaintenanceListener maintenanceListener;

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config laden
        configManager = new ConfigManager(this);
        int maxPlayers = getServer().getMaxPlayers();

        // 2. Metrics initialisieren (falls genutzt)
        int pluginId = 25435;
        new Metrics(this, pluginId);

        // 3. HTTP-Server nur starten, wenn API oder Webserver aktiviert sind
        boolean apiEnabled = configManager.isApiEnabled();
        boolean webUiEnabled = configManager.isWebServerEnabled();
        if (apiEnabled || webUiEnabled) {
            int port = configManager.getServerPort();
            webServer = new WebServer(this, port);
            webServer.start();
            getLogger().info("HTTP-Server läuft auf Port " + port);

            if (apiEnabled) getLogger().info("JSON-API aktiviert (Endpoint: /api)");
            if (webUiEnabled) getLogger().info("Web-Interface aktiviert (Endpoint: /)");

        } else {
            getLogger().info("Weder API noch Webserver aktiviert – HTTP-Server wird nicht gestartet.");
        }

        // 4. craftattack-Befehl registrieren
        if (getCommand("craftattack") == null) {
            getLogger().severe("Der Befehl 'craftattack' wurde nicht in der plugin.yml gefunden!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        CraftAttackCommand mainCmd = new CraftAttackCommand();
        getCommand("craftattack").setExecutor(mainCmd);
        getCommand("craftattack").setTabCompleter(mainCmd);

        // 5. status-Command registrieren
        StatusManager.init(this, configManager);
        StatusCommand statusCommand = new StatusCommand(this);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(statusCommand);


        spawnTeleporterListener = new SpawnTeleporterListener(this, configManager);
        getServer().getPluginManager().registerEvents(spawnTeleporterListener, this);

        // MOTD-Listener erstellen und registrieren
        motdListener = new MotdListener(getConfigManager());
        getServer().getPluginManager().registerEvents(motdListener, this);

        // Init MaintenanceManager
        maintenanceManager = new MaintenanceManager(configManager);

        // Register Listener
        maintenanceListener = new MaintenanceListener(maintenanceManager);
        getServer().getPluginManager().registerEvents(maintenanceListener, this);


        // 6. SpawnBoostListener optional registrieren
        SpawnBoostListener listener = SpawnBoostListener.create(this);
        if (listener != null) {
            getServer().getPluginManager().registerEvents(listener, this);
            setSpawnBoostListener(listener);
        } else {
            getLogger().info("SpawnElytra-Feature deaktiviert. Kein Listener registriert.");
        }

        // 7. Weitere Listener
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLimitBypassListener(maxPlayers), this);


        // 8. pvp-Command registrieren
        pvpCmd = new pvpCommand();
        if (getCommand("pvp") != null) {
            getCommand("pvp").setExecutor(pvpCmd);
            getCommand("pvp").setTabCompleter(pvpCmd);
        }

        // 9. Tab-Text direkt setzen, falls aktiviert
        if (configManager.isTabTextEnabled()) {
            updateTabText();
        }

        getLogger().info("CraftAttack Plugin wurde erfolgreich geladen.");
    }

    @Override
    public void onDisable() {
        // HTTP-Server sauber stoppen
        if (webServer != null) {
            webServer.stop();
            getLogger().info("HTTP-Server gestoppt.");
        }
    }

    /**
     * Liefert die Plugin-Instanz.
     */
    public static CraftAttack getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public pvpCommand getPvpCmd() {
        return pvpCmd;
    }

    public SpawnBoostListener getSpawnBoostListener() {
        return spawnBoostListener;
    }

    public void setSpawnBoostListener(SpawnBoostListener listener) {
        this.spawnBoostListener = listener;
    }

    /**
     * Setzt den Tab-Text für alle Online-Spieler.
     */
    public void updateTabText() {
        String tabJson = configManager.getConfig()
                .getString("tab", "{\"text\":\"Default Tab\",\"color\":\"gold\"}");
        String tabText = JsonColorConverter.convertTab(tabJson);
        Bukkit.getOnlinePlayers().forEach(p ->
                p.setPlayerListHeaderFooter("", tabText)
        );
        getLogger().info("Tab-Text wurde aktualisiert.");
    }

    /**
     * Löst einen Server-Neustart aus.
     * Ruft Bukkit.shutdown() auf,
     * sodass externe Startskripte den Server wieder hochfahren können.
     */
    public void restartServer() {
        getLogger().info("Starte Server-Neustart...");
        // Shutdown im Haupt-Thread durchführen
        Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
    }

    public SpawnTeleporterListener getSpawnTeleporterListener() {
        return spawnTeleporterListener;
    }

    public void setSpawnTeleporterListener(SpawnTeleporterListener listener) {
        this.spawnTeleporterListener = listener;
    }

    public MotdListener getMotdListener() {
        return motdListener;
    }

    public void setMotdListener(MotdListener listener) {
        this.motdListener = listener;
    }

}