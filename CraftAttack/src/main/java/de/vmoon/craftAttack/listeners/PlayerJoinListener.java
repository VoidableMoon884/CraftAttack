package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.JsonColorConverter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (CraftAttack.getInstance().getConfigManager().isTabTextEnabled()) {
            // Lese den Tab-Text aus der Config und konvertiere ihn
            String tabJson = CraftAttack.getInstance().getConfig().getString("tab", "{\"text\":\"Default Tab\",\"color\":\"gold\"}");
            String tabText = JsonColorConverter.convertTab(tabJson);
            event.getPlayer().setPlayerListHeaderFooter("", tabText);
        }
    }
}