package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.commands.TimeoutCommand;
import de.vmoon.craftAttack.utils.StatusManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Neue Zeilen für Timeout/Mute prüfen und Nachricht blockieren
        if (TimeoutCommand.isMuted(player)) {
            long restMillis = TimeoutCommand.getRemainingMuteTime(player);
            String restFormatted = TimeoutCommand.formatDuration(restMillis);
            player.sendMessage("§cDu bist stumm geschaltet. Verbleibende Zeit: " + restFormatted);
            event.setCancelled(true);
            return;
        }

        String statusPrefix = StatusManager.getInstance().getPlayerStatus(player);
        if (statusPrefix == null || statusPrefix.isEmpty()) {
            Team team = player.getScoreboard().getEntryTeam(player.getName());
            if (team != null && team.getPrefix() != null) {
                statusPrefix = team.getPrefix().trim();
            } else {
                statusPrefix = "";
            }
        }
        String originalFormat = event.getFormat();
        if (originalFormat == null || originalFormat.isEmpty()) {
            originalFormat = "%s: %s";
        }
        String newFormat = statusPrefix + ChatColor.RESET + " " + originalFormat;
        event.setFormat(newFormat);
    }
}
