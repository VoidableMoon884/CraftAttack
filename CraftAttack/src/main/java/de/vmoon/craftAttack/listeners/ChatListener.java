package de.vmoon.craftAttack.listeners;

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