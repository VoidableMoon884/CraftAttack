package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.utils.StatusManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class StatusCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public StatusCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ca.user.status")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu benutzen!");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl ist nur für Spieler!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Verwendung: /status <statusname>");
            return true;
        }
        String perm = "ca.status." + args[0].toLowerCase();
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Status zu setzen!");
            return true;
        }
        String inputStatus = args[0];
        if (inputStatus.equalsIgnoreCase("None")) {
            StatusManager.getInstance().setPlayerStatus(player, "");
            player.sendMessage(ChatColor.GREEN + "Dein Status wurde entfernt!");
            return true;
        }

        ConfigurationSection statusesSection = plugin.getConfig().getConfigurationSection("statuses");
        if (statusesSection == null) {
            player.sendMessage(ChatColor.RED + "Es sind keine Statuswerte konfiguriert!");
            return true;
        }

        String matchedKey = null;
        for (String key : statusesSection.getKeys(false)) {
            if (key.equalsIgnoreCase(inputStatus)) {
                matchedKey = key;
                break;
            }
        }
        if (matchedKey == null) {
            player.sendMessage(ChatColor.RED + "Status '" + inputStatus + "' existiert nicht!");
            return true;
        }

        String statusJson = statusesSection.getString(matchedKey);
        if (statusJson == null || statusJson.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Der Status '" + matchedKey + "' ist nicht korrekt konfiguriert!");
            return true;
        }

        // Speichere nur den Statusnamen (z. B. "Admin")
        StatusManager.getInstance().setPlayerStatus(player, matchedKey);
        String formattedStatus = StatusManager.getInstance().getPlayerStatus(player);
        player.sendMessage(ChatColor.GREEN + "Dein Status wurde auf " + formattedStatus + ChatColor.GREEN + " gesetzt!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String typed = args[0].toLowerCase();
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("statuses");
            if (section != null) {
                for (String status : section.getKeys(false)) {
                    if (status.toLowerCase().startsWith(typed)) {
                        completions.add(status);
                    }
                }
            }
            if ("none".startsWith(typed)) {
                completions.add("None");
            }
        }
        return completions;
    }
}