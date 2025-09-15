package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.listeners.*;
import de.vmoon.craftAttack.utils.*;
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

        if (plugin.getSpawnTeleporterListener() != null) {
            HandlerList.unregisterAll(plugin.getSpawnTeleporterListener());
        }

        SpawnTeleporterListener newSpawnTeleportListener = new SpawnTeleporterListener(plugin, plugin.getConfigManager());
        plugin.getServer().getPluginManager().registerEvents(newSpawnTeleportListener, plugin);
        plugin.setSpawnTeleporterListener(newSpawnTeleportListener);

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

        // Alten MOTD-Listener abmelden, falls vorhanden
        if (plugin.getMotdListener() != null) {
            HandlerList.unregisterAll(plugin.getMotdListener());
        }

// Neuen MOTD-Listener anhand der neuen Konfiguration erzeugen
        MotdListener newMotdListener = new MotdListener(plugin.getConfigManager());
        plugin.getServer().getPluginManager().registerEvents(newMotdListener, plugin);
        plugin.setMotdListener(newMotdListener); // Setter-Methode im Plugin hinzufügen

// Optional: Reload-Methode innerhalb des Listeners aufrufen
        newMotdListener.reloadMotds();


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