package de.vmoon.craftattack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import de.vmoon.craftattack.commands.*;

public final class CraftAttack extends JavaPlugin {

    private statusCommand statusCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig(); // Save default config if not exists

        this.statusCommand = new statusCommand(this);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(statusCommand);
        getServer().getPluginManager().registerEvents(statusCommand, this);
        getCommand("pvp").setExecutor(new pvpCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        statusCommand.loadPlayerStatus(event.getPlayer());
    }
}
