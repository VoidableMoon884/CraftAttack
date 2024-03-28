package de.vmoon.craftattack.commands;

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
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("pvp")) {
            if (!commandSender.hasPermission("pvp.use")) {
                commandSender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                return true;
            }
            else {
                if (strings.length == 0) {
                    commandSender.sendMessage("An oder aus?");
                    return true;
                }
                else {
                    if (strings[0].equalsIgnoreCase("on")) {
                        if (!commandSender.hasPermission("pvp.on")) {
                            commandSender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                            return true;
                        }
                        else {
                            enablepvp();
                        }
                    }
                    else if (strings[0].equalsIgnoreCase("off")) {
                        if (!commandSender.hasPermission("pvp.off")) {
                            commandSender.sendMessage("§cDu hast keine Berechtigung um diesen Befehl auszuführen!");
                            return true;
                        }
                        else {
                            disablepvp();
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] strings) {
        if (strings.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            return completions.stream()
                    .filter(s -> s.startsWith(strings[0]))
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
