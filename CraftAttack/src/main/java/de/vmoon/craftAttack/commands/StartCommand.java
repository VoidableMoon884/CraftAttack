package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class StartCommand {

    private static boolean processActive = false;
    private static int phase2TaskId = -1;

    public static boolean handle(CommandSender sender, String[] args) {
        String sub = args[0].toLowerCase();
        ConfigManager config = CraftAttack.getInstance().getConfigManager();
        World world = Bukkit.getWorld("world");
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World 'world' wurde nicht gefunden!");
            return true;
        }
        WorldBorder border = world.getWorldBorder();

        if (sub.equals("start")) {
            if (!sender.hasPermission("ca.admin.start")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen.");
                return true;
            }
            if (processActive) {
                sender.sendMessage(ChatColor.RED + "Ein Prozess läuft bereits!");
                return true;
            }
            processActive = true;
            double initialBorder = config.getInitialBorder();
            double targetBorder = config.getTargetBorder();
            double centerX = config.getWorldBorderCenterX();
            double centerZ = config.getWorldBorderCenterZ();

            border.setCenter(centerX, centerZ);
            border.setSize(initialBorder, 0);

            // Phase 1: Erweitere um 400 Blöcke in 4 Sekunden.
            double intermediateSize = initialBorder + 400;
            border.setSize(intermediateSize, 4);
            // Phase 2: Nach 4 Sekunden in 1 Sekunde bis zum Zielwert.
            phase2TaskId = Bukkit.getScheduler().runTaskLater(CraftAttack.getInstance(), () -> {
                border.setSize(targetBorder, 1);
                processActive = false;
                phase2TaskId = -1;
            }, 4 * 20L).getTaskId();

            // Teleportiere alle Spieler zu den in der Config festgelegten Teleportkoordinaten.
            double teleportX = config.getTeleportX();
            double teleportY = config.getTeleportY();
            double teleportZ = config.getTeleportZ();
            Location loc = new Location(world, teleportX, teleportY, teleportZ);
            Bukkit.getOnlinePlayers().forEach(p -> p.teleport(loc));

            // Verwende die Hilfsklasse, um den Title aus dem JSON umzuwandeln.
            String titleJson = CraftAttack.getInstance().getConfig().getString("title", "{\"text\":\"Willkommen auf CraftAttack!\",\"color\":\"red\"}");
            String title = JsonColorConverter.convertTitle(titleJson);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(title, "", 10, 40, 10);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getWelcomeMessage()));
            PvPUtils.togglePvP(true);
            return true;

        } else if (sub.equals("stop")) {
            if (!sender.hasPermission("ca.admin.stop")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung!");
                return true;
            }
            if (phase2TaskId != -1) {
                Bukkit.getScheduler().cancelTask(phase2TaskId);
                phase2TaskId = -1;
            }
            border.setSize(9999999, 0);
            processActive = false;
            sender.sendMessage(ChatColor.GREEN + "Prozess gestoppt. WorldBorder deaktiviert!");
            PvPUtils.togglePvP(false);
            return true;

        } else if (sub.equals("teleportall")) {
            if (!sender.hasPermission("ca.admin.teleportall")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung!");
                return true;
            }
            FileConfiguration Fileconfig = CraftAttack.getInstance().getConfig();
            double teleportX = Fileconfig.getDouble("teleport.x", 0);
            double teleportY = Fileconfig.getDouble("teleport.y", 0);
            double teleportZ = Fileconfig.getDouble("teleport.z", 0);
            Location loc = new Location(world, teleportX, teleportY, teleportZ);
            Bukkit.getOnlinePlayers().forEach(p -> p.teleport(loc));
            sender.sendMessage(ChatColor.GREEN + "Alle Spieler wurden zu den festgelegten Koordinaten teleportiert!");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Ungültiger Sub-Befehl. Use: start|stop|teleportall");
        return true;
    }
}