package de.vmoon.craftAttack.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import de.vmoon.craftAttack.CraftAttack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimeoutCommand implements CommandExecutor, TabCompleter {

    private static Set<UUID> mutedPlayers = new HashSet<>();
    private static Map<UUID, Long> muteEndTime = new HashMap<>();

    private final Pattern timePattern = Pattern.compile("(\\d+)([smhd])");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ca.admin.timeout")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage:");
            sender.sendMessage(ChatColor.RED + "/ca timeout add <Spieler> <Zeit>");
            sender.sendMessage(ChatColor.RED + "/ca timeout remove <Spieler>");
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("remove")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /ca timeout remove <Spieler>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                return true;
            }
            mutedPlayers.remove(target.getUniqueId());
            muteEndTime.remove(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Timeout für " + target.getName() + " wurde aufgehoben.");
            Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
            if (onlineTarget != null && onlineTarget.isOnline()) {
                onlineTarget.sendMessage(ChatColor.GREEN + "Deine Stummschaltung wurde aufgehoben.");
            }
            return true;
        } else if (action.equals("add")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /ca timeout add <Spieler> <Zeit>");
                sender.sendMessage(ChatColor.RED + "Beispiele: 30m, 15m, 2h, 3d, 24h=1d");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                return true;
            }
            long durationMillis = parseTime(args[2]);
            if (durationMillis <= 0) {
                sender.sendMessage(ChatColor.RED + "Ungültige Zeitangabe! Beispiele: 30m, 15m, 2h, 3d, 24h=1d");
                return true;
            }

            mutedPlayers.add(target.getUniqueId());
            long endTime = System.currentTimeMillis() + durationMillis;
            muteEndTime.put(target.getUniqueId(), endTime);

            String formattedDuration = formatDuration(durationMillis);
            sender.sendMessage(ChatColor.GREEN + "Spieler " + target.getName() + " wurde für " + formattedDuration + " stumm geschaltet.");
            Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
            if (onlineTarget != null && onlineTarget.isOnline()) {
                onlineTarget.sendMessage(ChatColor.RED + "Du wurdest für " + formattedDuration + " stumm geschaltet.");
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    mutedPlayers.remove(target.getUniqueId());
                    muteEndTime.remove(target.getUniqueId());
                    Player p = Bukkit.getPlayer(target.getUniqueId());
                    if (p != null && p.isOnline()) {
                        p.sendMessage(ChatColor.GREEN + "Deine Stummschaltung ist abgelaufen.");
                    }
                }
            }.runTaskLater(CraftAttack.getInstance(), durationMillis / 50L);

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Unbekannter Subbefehl. Nutze add oder remove.");
            return true;
        }
    }

    public static boolean isMuted(Player player) {
        if (!mutedPlayers.contains(player.getUniqueId())) {
            return false;
        }
        Long end = muteEndTime.get(player.getUniqueId());
        if (end == null || System.currentTimeMillis() > end) {
            mutedPlayers.remove(player.getUniqueId());
            muteEndTime.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public static long getRemainingMuteTime(Player player) {
        if (!isMuted(player)) return 0;
        Long end = muteEndTime.get(player.getUniqueId());
        if (end == null) return 0;
        long rest = end - System.currentTimeMillis();
        return Math.max(rest, 0);
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

    public static String formatDuration(long millis) {
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
        if (args.length == 1) {
            List<String> subs = Arrays.asList("add", "remove");
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("add")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (action.equals("remove")) {
                // Spieler die gemutet sind
                return mutedPlayers.stream()
                        .map(uuid -> {
                            Player p = Bukkit.getPlayer(uuid);
                            return p != null ? p.getName() : null;
                        })
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            List<String> suggestions = Arrays.asList("30m", "15m", "2h", "3d", "24h");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
