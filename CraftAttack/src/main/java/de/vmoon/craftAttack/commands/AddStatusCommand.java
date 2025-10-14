package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class AddStatusCommand implements CommandExecutor {

    private final CraftAttack plugin;

    public AddStatusCommand(CraftAttack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ca.admin.addstatus")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ca addstatus <statusname> <json>");
            return true;
        }

        String statusName = args[0].toLowerCase();

        // JSON-String aus den restlichen Argumenten zusammensetzen (für JSON mit Leerzeichen)
        StringBuilder jsonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            jsonBuilder.append(args[i]).append(" ");
        }
        String jsonString = jsonBuilder.toString().trim();

        // Einfache JSON-Formatprüfung
        if (!jsonString.startsWith("{") || !jsonString.endsWith("}")) {
            sender.sendMessage(ChatColor.RED + "Ungültiges JSON-Format.");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection statusesSection = config.getConfigurationSection("statuses");
        if (statusesSection == null) {
            statusesSection = config.createSection("statuses");
        }

        // Prüfen, ob Statusname schon existiert
        if (statusesSection.contains(statusName)) {
            sender.sendMessage(ChatColor.RED + "Ein Status mit diesem Namen existiert bereits.");
            return true;
        }

        // Neuen Status hinzufügen
        statusesSection.set(statusName, jsonString);

        // Config speichern
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Neuer Status '" + statusName + "' wurde hinzugefügt.");

        return true;
    }
}
