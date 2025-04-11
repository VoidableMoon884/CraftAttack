package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand {

    /**
     * Bearbeitet den Sub-Befehl "reload" für /craftattack reload.
     * Lädt die Config neu und – wenn die Option "tabtext" deaktiviert ist –
     * löscht den Tab-Text (Header und Footer) bei allen Online-Spielern.
     */
    public static boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ca.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, die Config neu zu laden!");
            return true;
        }

        CraftAttack plugin = CraftAttack.getInstance();
        plugin.reloadConfig();

        // Nach dem Reload: Wenn Tabtext aktiviert ist, setze den Tab-Text;
        // andernfalls lösche ihn (leere Header/Footer).
        if (plugin.getConfigManager().isTabTextEnabled()) {
            plugin.updateTabText();
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", ""));
            plugin.getLogger().info("Tab-Text ist deaktiviert – daher wurde der Tabtext gelöscht.");
        }

        sender.sendMessage(ChatColor.GREEN + "Config wurde erfolgreich neu geladen!");
        return true;
    }
}