package de.vmoon.craftattack;

import de.vmoon.craftattack.commands.pvpCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftAttack extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("pvp").setExecutor(new pvpCommand());
        // Plugin startup logic
        Bukkit.getConsoleSender().sendMessage("§6CraftAttack Plugin geladen!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage("§6CraftAttack Plugin deaktiviert!");
    }
}
