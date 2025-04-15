package de.vmoon.craftAttack;

import de.vmoon.craftAttack.commands.*;
import de.vmoon.craftAttack.listeners.*;
import de.vmoon.craftAttack.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftAttack extends JavaPlugin {

    private static CraftAttack instance;
    private ConfigManager configManager;
    private pvpCommand pvpCmd; // Instanz von pvpCommand hinzufügen
    private SpawnBoostListener spawnBoostListener;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);

        // Metrics initialisieren (falls genutzt)
        int pluginId = 25435;
        new Metrics(this, pluginId);

        // Überprüfe, ob der Befehl "craftattack" registriert wurde
        if (getCommand("craftattack") == null) {
            getLogger().severe("Der Befehl 'craftattack' wurde nicht in der plugin.yml gefunden!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Erstelle und registriere den Elytra-Listener nur, wenn er nicht null ist
        SpawnBoostListener listener = SpawnBoostListener.create(this);
        if (listener != null) {
            getServer().getPluginManager().registerEvents(listener, this);
            setSpawnBoostListener(listener);
        } else {
            getLogger().info("SpawnElytra Feature ist deaktiviert. Kein Listener wird registriert.");
        }

        // Registriere andere Listener
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);

        // Entferne diese Zeile, da sie den Listener unbeding registriert!
        // getServer().getPluginManager().registerEvents(SpawnBoostListener.create(this), this);

        // Registriere den Hauptbefehl (/craftattack)
        getCommand("craftattack").setExecutor(new CraftAttackCommand());
        getCommand("craftattack").setTabCompleter(new CraftAttackCommand());

        // Initialisiere die pvpCommand Instanz und registriere den "pvp"-Befehl
        pvpCmd = new pvpCommand();
        if (getCommand("pvp") != null) {
            getCommand("pvp").setExecutor(pvpCmd);
        }

        // Registriere den Listener, der neuen Spielern den Tab-Text setzt
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Setze beim Start für alle Online-Spieler den Tablist-Footer, falls aktiviert
        if (configManager.isTabTextEnabled()) {
            updateTabText();
        }

        getLogger().info("CraftAttack Plugin wurde erfolgreich geladen.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic (falls benötigt)
    }

    public static CraftAttack getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public pvpCommand getPvpCmd() {
        return pvpCmd; // Getter für pvpCommand
    }
    public SpawnBoostListener getSpawnBoostListener() {
        return spawnBoostListener;
    }

    public void setSpawnBoostListener(SpawnBoostListener listener) {
        this.spawnBoostListener = listener;
    }

    public void updateTabText() {
        String tabJson = getConfig().getString("tab", "{\"text\":\"Default Tab\",\"color\":\"gold\"}");
        String tabText = JsonColorConverter.convertTab(tabJson);
        Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", tabText));
        getLogger().info("Tab-Text wurde aktualisiert.");
    }
}