package de.vmoon.craftAttack;

import de.vmoon.craftAttack.commands.*;
import de.vmoon.craftAttack.listeners.PlayerJoinListener;
import de.vmoon.craftAttack.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftAttack extends JavaPlugin {

    private static CraftAttack instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);

        // Metrics initialisieren (falls genutzt)
        int pluginId = 25435;
        Metrics metrics = new Metrics(this, pluginId);

        // Überprüfe, ob der Befehl "craftattack" registriert wurde
        if (getCommand("craftattack") == null) {
            getLogger().severe("Der Befehl 'craftattack' wurde nicht in der plugin.yml gefunden!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Registriere die Hauptbefehle
        getCommand("craftattack").setExecutor(new CraftAttackCommand());
        getCommand("craftattack").setTabCompleter(new CraftAttackCommand());
        if (getCommand("pvp") != null) {
            getCommand("pvp").setExecutor(new pvpCommand());
        }
        // Registriere den Reload-Befehl (als eigener Command)
        if (getCommand("craftattackreload") != null) {
            getCommand("craftattackreload");
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
        // Plugin shutdown logic
    }

    public static CraftAttack getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Aktualisiert den Tablist-Footer für alle Online-Spieler.
     */
    public void updateTabText() {
        String tabJson = getConfig().getString("tab", "{\"text\":\"Default Tab\",\"color\":\"gold\"}");
        String tabText = JsonColorConverter.convertTab(tabJson);
        Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", tabText));
        getLogger().info("Tab-Text wurde aktualisiert.");
    }
}