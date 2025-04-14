package de.vmoon.craftAttack.listeners;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpawnSelectionListener implements Listener {

    // Speichert pro Spieler die Liste der bereits markierten Punkte
    private final Map<UUID, List<Location>> selections = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Es muss ein Block angeklickt worden sein
        if (event.getClickedBlock() == null) {
            return;
        }

        // Wir interessieren uns nur für Links- oder Rechtsklick auf einen Block
        Action action = event.getAction();
        if (!(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();

        // Prüfe, ob der Spieler das spezielle Spawn-Auswahl Werkzeug in der Hand hält
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STONE_AXE || !item.hasItemMeta()) {
            return;
        }
        String displayName = item.getItemMeta().getDisplayName();
        if (!ChatColor.stripColor(displayName).equals("Spawn-Auswahl Werkzeug")) {
            return;
        }

        // Verhindere die normale Blockinteraktion
        event.setCancelled(true);

        // Hole (oder erstelle) die Liste der bisher markierten Punkte für diesen Spieler
        UUID uuid = player.getUniqueId();
        List<Location> points = selections.getOrDefault(uuid, new ArrayList<>());

        // Füge den angeklickten Block hinzu (exakte Blockposition)
        Location loc = event.getClickedBlock().getLocation();
        points.add(loc);

        // Informiere den Spieler über die gesetzte Koordinate
        player.sendMessage(ChatColor.YELLOW + "Punkt " + points.size() + " gesetzt: ("
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");

        // Aktualisiere die gespeicherten Punkte
        selections.put(uuid, points);

        // Wenn drei Punkte gesetzt wurden, werden die Grenzen ermittelt und der Bereich gespeichert
        if (points.size() == 3) {
            double minX = points.stream().mapToDouble(Location::getX).min().orElse(0);
            double minY = points.stream().mapToDouble(Location::getY).min().orElse(0);
            double minZ = points.stream().mapToDouble(Location::getZ).min().orElse(0);
            double maxX = points.stream().mapToDouble(Location::getX).max().orElse(0);
            double maxY = points.stream().mapToDouble(Location::getY).max().orElse(0);
            double maxZ = points.stream().mapToDouble(Location::getZ).max().orElse(0);

            // Speichere den Spawnbereich in der Config als axis-aligned Bounding Box
            CraftAttack pluginInstance = CraftAttack.getInstance();
            pluginInstance.getConfig().set("spawnArea.x1", minX);
            pluginInstance.getConfig().set("spawnArea.y1", minY);
            pluginInstance.getConfig().set("spawnArea.z1", minZ);
            pluginInstance.getConfig().set("spawnArea.x2", maxX);
            pluginInstance.getConfig().set("spawnArea.y2", maxY);
            pluginInstance.getConfig().set("spawnArea.z2", maxZ);
            pluginInstance.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Spawn-Bereich gesetzt!");
            player.sendMessage(ChatColor.GREEN + "Min: (" + (int) minX + ", " + (int) minY + ", " + (int) minZ + ")  " +
                    "Max: (" + (int) maxX + ", " + (int) maxY + ", " + (int) maxZ + ")");

            // Auswahl zurücksetzen, sodass der Spieler neu markieren kann
            selections.remove(uuid);

            // Entferne das Spawn-Auswahl Werkzeug aus dem Inventar:
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack != null
                        && stack.getType() == Material.STONE_AXE
                        && stack.hasItemMeta()
                        && ChatColor.stripColor(stack.getItemMeta().getDisplayName()).equals("Spawn-Auswahl Werkzeug")) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }
}