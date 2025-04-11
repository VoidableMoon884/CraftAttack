package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.JsonColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class SetTextsCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /craftattack <settitle|settab> <JSON>");
            return true;
        }
        String sub = args[0].toLowerCase();
        // Fasse alle Argumente ab args[1] zu einem einzigen JSON-String zusammen.
        String newJson = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();

        if (sub.equals("settitle")) {
            if (!sender.hasPermission("ca.admin.settitle")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, den Title zu setzen!");
                return true;
            }
            CraftAttack.getInstance().getConfig().set("title", newJson);
            CraftAttack.getInstance().saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Title erfolgreich aktualisiert!");
            return true;
        } else if (sub.equals("settab")) {
            // Überprüfe zuerst, ob die Tabtext-Funktion aktiviert ist
            if (!CraftAttack.getInstance().getConfigManager().isTabTextEnabled()) {
                sender.sendMessage(ChatColor.RED + "Die Tabtext-Funktion ist in der Config deaktiviert.");
                return true;
            }
            if (!sender.hasPermission("ca.admin.settab")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, den Tab-Text zu setzen!");
                return true;
            }
            CraftAttack.getInstance().getConfig().set("tab", newJson);
            CraftAttack.getInstance().saveConfig();
            // Aktualisiere den Tablist-Footer für alle Spieler (Header bleibt leer)
            String tabText = JsonColorConverter.convertTab(newJson);
            Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", tabText));
            sender.sendMessage(ChatColor.GREEN + "Tab-Text erfolgreich aktualisiert!");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Ungültiger Sub-Befehl. Usage: settitle|settab");
        return true;
    }
}