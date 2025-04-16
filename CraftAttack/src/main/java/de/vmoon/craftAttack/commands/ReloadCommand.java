package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.listeners.SpawnBoostListener;
import de.vmoon.craftAttack.utils.StatusManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.GameMode;

public class ReloadCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ca.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, die Config neu zu laden!");
            return true;
        }

        CraftAttack plugin = CraftAttack.getInstance();

        // Standard-Config neu laden
        plugin.reloadConfig();
        // Zusätzlich: Extra-Konfigurationsdatei neu laden
        plugin.getConfigManager().reloadExtraConfig();

        // Aktualisiere für alle Online-Spieler ihren Status anhand der neuen (Extra-)Konfiguration
        Bukkit.getOnlinePlayers().forEach(player -> {
            StatusManager.getInstance().updatePlayerOnJoin(player);
        });

        // Aktualisiere den Tabtext wie gehabt
        if (plugin.getConfigManager().isTabTextEnabled()) {
            plugin.updateTabText();
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", ""));
            plugin.getLogger().info("Tab-Text ist deaktiviert – daher wurde der Tabtext gelöscht.");
        }

        // Alten Elytra-Listener abmelden, falls vorhanden
        if (plugin.getSpawnBoostListener() != null) {
            HandlerList.unregisterAll(plugin.getSpawnBoostListener());
            plugin.getSpawnBoostListener().cancel();
            plugin.setSpawnBoostListener(null);
        }

        // Neuen Elytra-Listener anhand der neuen Konfiguration erzeugen
        SpawnBoostListener newListener = SpawnBoostListener.create(plugin);
        if (newListener != null) {
            plugin.getServer().getPluginManager().registerEvents(newListener, plugin);
            plugin.setSpawnBoostListener(newListener);
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            });
            plugin.getLogger().info("SpawnElytra Feature bleibt deaktiviert. Flight wurde bei allen Spielern deaktiviert.");
        }

        sender.sendMessage(ChatColor.GREEN + "Config wurde erfolgreich neu geladen!");
        return true;
    }
}