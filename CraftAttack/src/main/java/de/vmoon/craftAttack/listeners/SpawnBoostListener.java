package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.utils.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.KeybindComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SpawnBoostListener extends BukkitRunnable implements Listener {

    private final Plugin plugin;
    private final int multiplyValue;
    private final boolean boostEnabled;
    private final World world;
    private final String message;

    // Felder für den separaten Cuboid-Bereich (spawnArea)
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    /**
     * Erstellt einen neuen SpawnBoostListener basierend auf der Konfiguration.
     * Die Einstellungen zur Elytra liegen im Abschnitt "SpawnElytra", während
     * der Spawnbereich separat im Knoten "spawnArea" definiert wird.
     *
     * Ist das Elytra-Feature (enabled) deaktiviert, wird null zurückgegeben.
     */
    public static SpawnBoostListener create(JavaPlugin plugin) {
        ConfigManager configManager = new ConfigManager(plugin);
        // Lade die Elytra-Konfiguration
        ConfigurationSection elytraSection = configManager.getConfig().getConfigurationSection("SpawnElytra");
        if (elytraSection == null) {
            plugin.getLogger().severe("Der Konfigurationsabschnitt 'SpawnElytra' wurde nicht gefunden!");
            return null;
        }

        // Prüfe den Ein-/Aus-Schalter für das Elytra-Feature
        if (!elytraSection.getBoolean("enabled", true)) {
            plugin.getLogger().info("Das Feature SpawnElytra ist deaktiviert.");
            return null;
        }

        int multiplyValue = elytraSection.getInt("multiplyValue", 2);
        boolean boostEnabled = elytraSection.getBoolean("boostEnabled", true);
        String worldName = elytraSection.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("Ungültige Welt: " + worldName);
        }
        String message = elytraSection.getString("message", "Boost ready: Press %key% to boost");

        // Lese den separaten Spawnbereich aus der obersten Konfiguration
        double x1 = configManager.getConfig().getDouble("spawnArea.x1", 0.0);
        double y1 = configManager.getConfig().getDouble("spawnArea.y1", 0.0);
        double z1 = configManager.getConfig().getDouble("spawnArea.z1", 0.0);
        double x2 = configManager.getConfig().getDouble("spawnArea.x2", 0.0);
        double y2 = configManager.getConfig().getDouble("spawnArea.y2", 0.0);
        double z2 = configManager.getConfig().getDouble("spawnArea.z2", 0.0);

        // Berechne die minimalen und maximalen Werte der jeweiligen Koordinaten
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return new SpawnBoostListener(plugin, multiplyValue, boostEnabled, world, message, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private SpawnBoostListener(Plugin plugin, int multiplyValue, boolean boostEnabled, World world, String message,
                               double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.plugin = plugin;
        this.multiplyValue = multiplyValue;
        this.boostEnabled = boostEnabled;
        this.world = world;
        this.message = message;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        // Starte den Task – dieser läuft alle 3 Ticks
        this.runTaskTimer(plugin, 0, 3);
    }

    @Override
    public void run() {
        world.getPlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
                return;
            player.setAllowFlight(isInSpawnArea(player));
            if (flying.contains(player) &&
                    !player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN).getType().isAir()) {
                player.setAllowFlight(false);
                player.setGliding(false);
                boosted.remove(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> flying.remove(player), 5);
            }
        });
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;
        if (!isInSpawnArea(player))
            return;
        event.setCancelled(true);
        player.setGliding(true);
        flying.add(player);
        if (!boostEnabled)
            return;
        String[] messageParts = message.split("%key%");
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new ComponentBuilder(messageParts[0])
                        .append(new KeybindComponent("key.swapOffhand"))
                        .append(messageParts.length > 1 ? messageParts[1] : "")
                        .create());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER &&
                (event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                        event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) &&
                flying.contains(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (!boostEnabled || !flying.contains(event.getPlayer()) || boosted.contains(event.getPlayer()))
            return;
        event.setCancelled(true);
        boosted.add(event.getPlayer());
        event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(multiplyValue));
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && flying.contains(event.getEntity()))
            event.setCancelled(true);
    }

    /**
     * Prüft, ob sich der Spieler innerhalb des definierten Spawnbereichs (Cuboid) befindet.
     */
    private boolean isInSpawnArea(Player player) {
        if (!player.getWorld().equals(world))
            return false;
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    // Listenervariablen für das interne Handling
    private final List<Player> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();
}