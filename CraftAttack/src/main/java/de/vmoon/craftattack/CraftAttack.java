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
        saveDefaultConfig();

        this.statusCommand = new statusCommand(this);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(statusCommand);
        getServer().getPluginManager().registerEvents(statusCommand, this);
        getCommand("pvp").setExecutor(new pvpCommand());
        getCommand("tempban").setExecutor(new tempBanCommand());
        getCommand("tempban").setTabCompleter(new tempBanCommand());
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
