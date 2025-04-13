package de.vmoon.craftAttack.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden.");
            return true;
        }

        // Hier wird die Berechtigung geprüft
        if (!sender.hasPermission("ca.admin.invsee")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cBenutzung: /craftattack invsee <Spielername>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cSpieler nicht gefunden oder offline.");
            return true;
        }

        Player viewer = (Player) sender;
        viewer.openInventory(target.getInventory());
        viewer.sendMessage("§aDu siehst jetzt das Inventar von §e" + target.getName() + "§a.");
        return true;
    }
}