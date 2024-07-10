package de.vmoon.craftattack.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class statusCommand implements CommandExecutor, TabCompleter {

    private final List<String> colors = Arrays.stream(ChatColor.values())
            .filter(ChatColor::isColor)
            .map(Enum::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Bitte gib einen Status an.");
            return false;
        }

        String status = args[0];
        ChatColor color = ChatColor.WHITE;

        if (args.length > 1) {
            try {
                color = ChatColor.valueOf(args[1].toUpperCase());
                if (!color.isColor()) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe. Verfügbare Farben sind: " + String.join(", ", colors));
                    return false;
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Ungültige Farbe. Verfügbare Farben sind: " + String.join(", ", colors));
                return false;
            }
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            String originalName = player.getName();
            player.setDisplayName(color + "[" + status + "]" + ChatColor.RESET + " " + originalName);
            sender.sendMessage(ChatColor.GREEN + "Dein Status wurde auf '" + color + "[" + status + "]" + ChatColor.GREEN + "' gesetzt.");
        } else {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl verwenden.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return colors.stream()
                    .filter(color -> color.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
