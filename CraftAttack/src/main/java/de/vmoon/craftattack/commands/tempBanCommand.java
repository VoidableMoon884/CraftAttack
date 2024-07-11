package de.vmoon.craftattack.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class tempBanCommand extends JavaPlugin implements CommandExecutor, TabCompleter {

    private final List<String> timeOptions = Arrays.asList("1h", "12h", "24h", "2T", "7T", "1M", "2M");
    private final HashMap<UUID, Long> banList = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /tempban [spielername] [zeit] [grund]");
            return false;
        }

        String playerName = args[0];
        String time = args[1];
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (!timeOptions.contains(time)) {
            sender.sendMessage("Invalid time format. Use one of: " + String.join(", ", timeOptions));
            return false;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return false;
        }

        long banDuration = parseTimeToMillis(time);
        long banEndTime = System.currentTimeMillis() + banDuration;
        banList.put(target.getUniqueId(), banEndTime);

        target.kickPlayer("You have been banned for " + time + ". Reason: " + reason);
        sender.sendMessage("Player " + playerName + " has been banned for " + time + ". Reason: " + reason);

        // Schedule unban
        Bukkit.getScheduler().runTaskLater(this, () -> banList.remove(target.getUniqueId()), banDuration / 50); // Convert ms to ticks

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        } else if (args.length == 2) {
            return timeOptions;
        }
        return new ArrayList<>();
    }

    private long parseTimeToMillis(String time) {
        char unit = time.charAt(time.length() - 1);
        int amount = Integer.parseInt(time.substring(0, time.length() - 1));

        switch (unit) {
            case 'h':
                return amount * 60 * 60 * 1000L; // hours to milliseconds
            case 'T':
                return amount * 24 * 60 * 60 * 1000L; // days to milliseconds
            case 'M':
                return amount * 30L * 24 * 60 * 60 * 1000L; // months (approximated as 30 days) to milliseconds
            default:
                throw new IllegalArgumentException("Invalid time format.");
        }
    }

    public boolean isBanned(UUID playerUUID) {
        if (!banList.containsKey(playerUUID)) {
            return false;
        }

        long banEndTime = banList.get(playerUUID);
        if (System.currentTimeMillis() > banEndTime) {
            banList.remove(playerUUID);
            return false;
        }

        return true;
    }
}
