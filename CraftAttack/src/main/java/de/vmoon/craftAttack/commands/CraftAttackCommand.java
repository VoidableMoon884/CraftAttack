package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CraftAttackCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /craftattack <start|stop|teleportall|settitle|settab|reload>");
            return true;
        }
        String sub = args[0].toLowerCase();
        // Delegiere die Unterbefehle an entsprechende Handler-Klassen.
        if (sub.equals("start") || sub.equals("stop") || sub.equals("teleportall")) {
            return StartCommand.handle(sender, args);
        } else if (sub.equals("settitle") || sub.equals("settab")) {
            return SetTextsCommand.handle(sender, args);
        } else if (sub.equals("reload")) {
            return ReloadCommand.handle(sender, args);
        } else {
            sender.sendMessage("§cUngültiger Sub-Befehl. Usage: /craftattack <start|stop|teleportall|settitle|settab|reload>");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("start");
            subs.add("stop");
            subs.add("teleportall");
            subs.add("settitle");
            // Falls Tabtext aktiviert ist, soll auch settab als Subbefehl erscheinen.
            if (CraftAttack.getInstance().getConfigManager().isTabTextEnabled()) {
                subs.add("settab");
            }
            subs.add("reload");
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}