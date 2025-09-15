package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.utils.MaintenanceManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class MaintenanceListener implements Listener {

    private final MaintenanceManager maintenanceManager;

    public MaintenanceListener(MaintenanceManager maintenanceManager) {
        this.maintenanceManager = maintenanceManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (maintenanceManager.isActive()) {
            if (!maintenanceManager.isPlayerAllowed(event.getPlayer())) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        ChatColor.RED + "Der Server befindet sich im Wartungsmodus. Zugang verweigert.");
            }
        }
    }
}
