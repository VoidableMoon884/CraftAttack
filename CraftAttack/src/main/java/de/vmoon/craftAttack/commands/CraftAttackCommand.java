package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CraftAttackCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /craftattack <start|stop|teleportall|settitle|settab|reload|invsee>");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("start") || sub.equals("stop") || sub.equals("teleportall")) {
            return StartCommand.handle(sender, args);
        } else if (sub.equals("settitle") || sub.equals("settab")) {
            return SetTextsCommand.handle(sender, args);
        } else if (sub.equals("reload")) {
            return ReloadCommand.handle(sender, args);
        } else if (sub.equals("invsee")) {
            // Prüfe, ob der invsee-Befehl in der Config aktiviert ist
            if (!CraftAttack.getInstance().getConfigManager().getConfig().getBoolean("invsee", false)) {
                sender.sendMessage("§cDer invsee-Befehl ist derzeit deaktiviert.");
                return true;
            }
            // Berechtigungsprüfung: Sender benötigt die Berechtigung "ca.admin.invsee"
            if (!sender.hasPermission("ca.admin.invsee")) {
                sender.sendMessage("§cDu hast keine Berechtigung diesen Befehl zu verwenden.");
                return true;
            }
            return InvseeCommand.handle(sender, args);
        } else {
            sender.sendMessage("§cUngültiger Sub-Befehl. Usage: /craftattack <start|stop|teleportall|settitle|settab|reload|invsee>");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Für den ersten Parameter
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("ca.admin.start")) {
                subs.add("start");
            }
            if (sender.hasPermission("ca.admin.stop")) {
                subs.add("stop");
            }
            if (sender.hasPermission("ca.admin.teleportall")) {
                subs.add("teleportall");
            }
            if (sender.hasPermission("ca.admin.settitle")) {
                subs.add("settitle");
            }
            if (CraftAttack.getInstance().getConfigManager().isTabTextEnabled()) {
                if (sender.hasPermission("ca.admin.settab")) {
                    subs.add("settab");
                }
            }
            if (sender.hasPermission("ca.admin.reload")) {
                subs.add("reload");
            }
            if (CraftAttack.getInstance().getConfigManager().getConfig().getBoolean("invsee", false)) {
                if (sender.hasPermission("ca.admin.invsee")) {
                    subs.add("invsee");
                }
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        // Tab-Complete für den zweiten Parameter beim invsee-Befehl
        else if (args.length == 2 && args[0].equalsIgnoreCase("invsee")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}