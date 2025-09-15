package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.MaintenanceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MaintenanceCommand {
    public static boolean handle(CommandSender sender, String[] args) {
        CraftAttack plugin = CraftAttack.getInstance();
        MaintenanceManager manager = plugin.getMaintenanceManager();

        if (!sender.hasPermission("ca.admin.maintenance")) {
            sender.sendMessage(ChatColor.RED + "Keine Rechte, diesen Befehl zu verwenden.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Wartungsmodus ist " + (manager.isActive() ? "aktiv" : "inaktiv"));
            sender.sendMessage(ChatColor.YELLOW + "/craftattack maintenance start");
            sender.sendMessage(ChatColor.YELLOW + "/craftattack maintenance stop");
            sender.sendMessage(ChatColor.YELLOW + "/craftattack maintenance addplayer <Spieler>");
            sender.sendMessage(ChatColor.YELLOW + "/craftattack maintenance removeplayer <Spieler>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on":
                manager.setActive(true);
                Bukkit.broadcastMessage(ChatColor.RED + "Wartungsmodus aktiviert!");

                boolean forceKick = args.length > 1 && args[1].equalsIgnoreCase("force");
                if (forceKick) {
                    String kickMessage = ChatColor.RED + "Der Server ist im Wartungsmodus. Du wurdest gekickt.";
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!manager.isPlayerAllowed(online)) {
                            online.kickPlayer(kickMessage);
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "Alle nicht autorisierten Spieler wurden gekickt.");
                }
                break;

            case "off":
                manager.setActive(false);
                Bukkit.broadcastMessage(ChatColor.GREEN + "Wartungsmodus deaktiviert!");
                break;
            case "addplayer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Bitte Spielername angeben.");
                    break;
                }
                OfflinePlayer toAdd = Bukkit.getOfflinePlayer(args[1]);
                if (toAdd == null || !toAdd.hasPlayedBefore()) {
                    sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden oder nie gespielt.");
                    break;
                }
                manager.addPlayer(toAdd);
                sender.sendMessage(ChatColor.GREEN + "Spieler " + toAdd.getName() + " zur Wartungs-Whitelist hinzugefügt.");
                break;
            case "removeplayer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Bitte Spielername angeben.");
                    break;
                }
                OfflinePlayer toRemove = Bukkit.getOfflinePlayer(args[1]);
                if (toRemove == null) {
                    sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                    break;
                }
                manager.removePlayer(toRemove);
                sender.sendMessage(ChatColor.GREEN + "Spieler " + toRemove.getName() + " von der Wartungs-Whitelist entfernt.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unbekannter Parameter.");
        }
        return true;
    }
}
