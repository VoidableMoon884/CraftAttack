package de.vmoon.craftAttack.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TempBanCommand implements CommandExecutor, TabCompleter {

    private final Pattern timePattern = Pattern.compile("(\\d+)([smhd])");

    // Vorschläge für Ban-Gründe (leicht erweiterbar)
    private final List<String> reasonSuggestions = Arrays.asList(
            "Spam", "Beleidigung", "Griefing", "Cheating", "Werbung", "Sonstiges"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ca.admin.tempban")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl (ca.admin.tempban).");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /craftattack tempban <Spieler> <Zeit> [Grund]");
            sender.sendMessage(ChatColor.YELLOW + "Beispiel: /craftattack tempban Steve 2d Beleidigung");
            return true;
        }

        String targetName = args[0];
        String timeString = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Temporärer Ban";

        long duration = parseTime(timeString);
        if (duration <= 0) {
            sender.sendMessage(ChatColor.RED + "Ungültige Zeitangabe! Beispiele: 30m, 15m, 2h, 3d, 24h=1d");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getName() == null) {
            sender.sendMessage(ChatColor.RED + "Spieler '" + targetName + "' wurde nicht gefunden.");
            return true;
        }

        String bannedName = target.getName();

        Date expireDate = new Date(System.currentTimeMillis() + duration);
        Bukkit.getBanList(BanList.Type.NAME).addBan(bannedName, reason, expireDate, sender.getName());

        // Wenn Spieler online ist, kicken
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.kickPlayer(ChatColor.RED + "Du wurdest temporär gebannt!\n" +
                        ChatColor.YELLOW + "Grund: " + reason + "\n" +
                        ChatColor.YELLOW + "Dauer: " + formatDuration(duration) + "\n" +
                        ChatColor.YELLOW + "Ablauf: " + expireDate);
            }
        }

        Bukkit.getOnlinePlayers().stream()
                .filter(Player::isOp)
                .forEach(op -> op.sendMessage(ChatColor.GOLD + "[CraftAttack] " + ChatColor.RED + bannedName +
                        " wurde für " + formatDuration(duration) + " gebannt. Grund: " + reason));

        return true;
    }

    private long parseTime(String input) {
        input = input.toLowerCase().replace("24h", "1d");
        Matcher matcher = timePattern.matcher(input);
        long total = 0;
        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            switch (matcher.group(2)) {
                case "s": total += TimeUnit.SECONDS.toMillis(amount); break;
                case "m": total += TimeUnit.MINUTES.toMillis(amount); break;
                case "h": total += TimeUnit.HOURS.toMillis(amount); break;
                case "d": total += TimeUnit.DAYS.toMillis(amount); break;
                default: return -1;
            }
        }
        return total;
    }

    private String formatDuration(long millis) {
        long d = TimeUnit.MILLISECONDS.toDays(millis);
        long h = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        return sb.toString().trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ca.admin.tempban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // Spielername vervollständigen
            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Zeitvorschläge vervollständigen
            List<String> suggestions = Arrays.asList("30m", "1h", "5h", "24h", "1d", "7d");
            String partial = args[1].toLowerCase();
            return suggestions.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length >= 3) {
            // Grundvorschläge vervollständigen
            String partialReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).toLowerCase();
            return reasonSuggestions.stream()
                    .filter(r -> r.toLowerCase().startsWith(partialReason))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
