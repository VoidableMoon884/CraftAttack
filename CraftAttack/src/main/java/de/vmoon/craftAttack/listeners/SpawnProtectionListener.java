package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class SpawnProtectionListener implements Listener {

    /**
     * Prüft, ob die übergebene Location innerhalb des in der Config gesetzten Spawn-Bereichs liegt.
     */
    private boolean isInSpawnArea(Location loc) {
        FileConfiguration config = CraftAttack.getInstance().getConfig();
        double x1 = config.getDouble("spawnArea.x1", 0);
        double y1 = config.getDouble("spawnArea.y1", 0);
        double z1 = config.getDouble("spawnArea.z1", 0);
        double x2 = config.getDouble("spawnArea.x2", 0);
        double y2 = config.getDouble("spawnArea.y2", 0);
        double z2 = config.getDouble("spawnArea.z2", 0);

        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);
        double maxZ = Math.max(z1, z2);

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (isInSpawnArea(loc)) {
            // Nur Spieler mit den Berechtigungen "ca.builder.spawn" oder "ca.admin.spawn" dürfen bauen.
            if (!player.hasPermission("ca.builder.spawn") && !player.hasPermission("ca.admin.spawn")) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst hier nicht bauen.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (isInSpawnArea(loc)) {
            // Nur Spieler mit den Berechtigungen "ca.builder.spawn" oder "ca.admin.spawn" dürfen abbauen.
            if (!player.hasPermission("ca.builder.spawn") && !player.hasPermission("ca.admin.spawn")) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst hier nicht abbauen.");
            }
        }
    }
}