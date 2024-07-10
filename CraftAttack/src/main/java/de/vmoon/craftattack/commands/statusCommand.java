package de.vmoon.craftattack.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class statusCommand implements CommandExecutor, TabCompleter, Listener {

    private final JavaPlugin plugin;
    private final List<String> colors;

    public statusCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.colors = Arrays.stream(ChatColor.values())
                .filter(ChatColor::isColor)
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

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
            String displayName = color + "[" + status + "]" + ChatColor.RESET + " " + originalName;
            player.setDisplayName(displayName);
            player.setPlayerListName(displayName);

            // Speichere den Status
            FileConfiguration config = plugin.getConfig();
            config.set("status." + player.getUniqueId() + ".status", status);
            config.set("status." + player.getUniqueId() + ".color", color.name());
            plugin.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Dein Status wurde auf '" + displayName + ChatColor.GREEN + "' gesetzt.");
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerStatus(event.getPlayer());
    }

    public void loadPlayerStatus(Player player) {
        FileConfiguration config = plugin.getConfig();
        String status = config.getString("status." + player.getUniqueId() + ".status", "");
        String colorName = config.getString("status." + player.getUniqueId() + ".color", "WHITE");
        ChatColor color = ChatColor.valueOf(colorName);

        if (!status.isEmpty()) {
            String displayName = color + "[" + status + "]" + ChatColor.RESET + " " + player.getName();
            player.setDisplayName(displayName);
            player.setPlayerListName(displayName);
        }
    }
}
