package de.vmoon.craftAttack.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaintenanceManager {
    private final ConfigManager configManager;
    private boolean active;
    private final Set<UUID> allowedPlayers = new HashSet<>();

    public MaintenanceManager(ConfigManager configManager) {
        this.configManager = configManager;
        load();
    }

    public void load() {
        FileConfiguration config = configManager.getMaintenanceConfig();
        active = config.getBoolean("maintenance.active", false);
        allowedPlayers.clear();
        for (String uuidStr : config.getStringList("maintenance.whitelist")) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                allowedPlayers.add(uuid);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        FileConfiguration config = configManager.getMaintenanceConfig();
        config.set("maintenance.active", active);
        config.set("maintenance.whitelist", allowedPlayers.stream()
                .map(UUID::toString).collect(Collectors.toList()));
        configManager.saveMaintenanceConfig();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        save();
    }

    public boolean isPlayerAllowed(OfflinePlayer player) {
        return allowedPlayers.contains(player.getUniqueId());
    }

    public void addPlayer(OfflinePlayer player) {
        allowedPlayers.add(player.getUniqueId());
        save();
    }

    public void removePlayer(OfflinePlayer player) {
        allowedPlayers.remove(player.getUniqueId());
        save();
    }

    public Set<UUID> getAllowedPlayers() {
        return new HashSet<>(allowedPlayers);
    }
}
