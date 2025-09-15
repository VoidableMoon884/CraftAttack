package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.utils.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;

public class MotdListener implements Listener {
    private final ConfigManager configManager;
    private List<String> motds;
    private int interval;
    private int currentIndex = 0;
    private long lastUpdateTime = 0;

    public MotdListener(ConfigManager configManager) {
        this.configManager = configManager;
        reloadMotds();
    }

    public void reloadMotds() {
        motds = configManager.getConfig().getStringList("motd.messages");
        interval = configManager.getConfig().getInt("motd.interval", 2);
        currentIndex = 0;
        lastUpdateTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (motds == null || motds.isEmpty()) {
            event.setMotd(configManager.getWelcomeMessage());
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime >= interval * 1000) {
            currentIndex = (currentIndex + 1) % motds.size();
            lastUpdateTime = now;
        }
        event.setMotd(motds.get(currentIndex));
    }
}
