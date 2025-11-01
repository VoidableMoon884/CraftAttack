package de.vmoon.craftAttack.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class PlayerLimitBypassListener implements Listener {

    private final int maxPlayers;

    public PlayerLimitBypassListener(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // If server not full, always allow
        if (Bukkit.getOnlinePlayers().size() < maxPlayers) {
            return;
        }

        // If server full, check if player has the bypass permission
        if (event.getPlayer().hasPermission("ca.admin.join.always")) {
            // Allow the player to join anyway, bypassing the limit
            event.setResult(Result.ALLOWED);
        }
    }
}
