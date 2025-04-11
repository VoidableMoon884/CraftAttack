package de.vmoon.craftAttack.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class pvpCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvp")) {
            if (!sender.hasPermission("pvp.use")) {
                sender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                return true;
            }
            else {
                if (args.length == 0) {
                    sender.sendMessage("An oder aus?");
                    return true;
                }
                else {
                    if (args[0].equalsIgnoreCase("on")) {
                        if (!sender.hasPermission("pvp.on")) {
                            sender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                            return true;
                        }
                        else {
                            sender.sendMessage("§2PVP wurde aktiviert!");
                            enablepvp();
                        }
                    }
                    else if (args[0].equalsIgnoreCase("off")) {
                        if (!sender.hasPermission("pvp.off")) {
                            sender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                            return true;
                        }
                        else {
                            sender.sendMessage("§2PVP wurde deaktiviert!");
                            disablepvp();
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            return completions.stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public void disablepvp() {
        Bukkit.getWorld("world").setPVP(false);
        Bukkit.getConsoleSender().sendMessage("§6[DEBUG] PVP wurde deaktiviert!");
    }
    public void enablepvp() {
        Bukkit.getWorld("world").setPVP(true);
        Bukkit.getConsoleSender().sendMessage("§6[DEBUG] PVP wurde aktiviert!");
    }
}
