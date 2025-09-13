package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.utils.ConfigManager;
import de.vmoon.craftAttack.utils.SpawnTeleporterData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class SpawnTeleporterListener implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final HashMap<UUID, Long> playerStayTime = new HashMap<>();

    public SpawnTeleporterListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SpawnTeleporterData data = configManager.getSpawnTeleporterData();

        if (!data.enabled) {
            removeBlindness(player);
            playerStayTime.remove(player.getUniqueId());
            return;
        }

        if (!player.getWorld().getName().equals(data.regionWorld)) {
            removeBlindness(player);
            playerStayTime.remove(player.getUniqueId());
            return;
        }

        Location loc = player.getLocation();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        int minX = Math.min(data.x1, data.x2);
        int maxX = Math.max(data.x1, data.x2);
        int minY = Math.min(data.y1, data.y2);
        int maxY = Math.max(data.y1, data.y2);
        int minZ = Math.min(data.z1, data.z2);
        int maxZ = Math.max(data.z1, data.z2);

        boolean isInRegion = x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;

        UUID uuid = player.getUniqueId();

        if (isInRegion) {
            // Sofort Blindness mit sehr hoher Dauer setzen (z.B. 1 Stunde)
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3600, 1, false, false));

            long now = System.currentTimeMillis();
            playerStayTime.putIfAbsent(uuid, now);

            long entered = playerStayTime.get(uuid);
            if ((now - entered) >= data.delaySeconds * 1000L) {
                playerStayTime.remove(uuid);
                removeBlindness(player);

                World targetWorld = Bukkit.getWorld(data.teleportWorld);
                if (targetWorld == null) targetWorld = player.getWorld();
                Location target = new Location(targetWorld, data.teleportX, data.teleportY, data.teleportZ);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Sound abspielen (falls gesetzt)
                        if (data.sound != null && !data.sound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(data.sound.toUpperCase());
                                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Ungültiger Sound in SpawnTeleporter config: " + data.sound);
                            }
                        }

                        player.teleport(target);
                    }
                }.runTask(plugin);
            }
        } else {
            removeBlindness(player);
            playerStayTime.remove(uuid);
        }
    }

    private void removeBlindness(Player player) {
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }
}
