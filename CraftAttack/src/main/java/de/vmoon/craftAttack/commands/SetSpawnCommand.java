package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1) Nur Spieler mit Permission
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur ein Spieler kann das ausführen.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("ca.admin.spawn")) {
            player.sendMessage(ChatColor.RED + "Dazu hast du keine Rechte.");
            return true;
        }

        // 2) Wir erwarten genau 4 Argumente: x y z radius
        if (args.length != 4) {
            player.sendMessage(ChatColor.RED +
                    "Usage: /ca setspawn <x> <y> <z> <radius>");
            return true;
        }

        Location loc = player.getLocation();
        double x, y, z, r;
        try {
            x = parseArg(args[0], loc.getX());
            y = parseArg(args[1], loc.getY());
            z = parseArg(args[2], loc.getZ());
            r = Double.parseDouble(args[3]);
            if (r < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED +
                    "Koordinaten müssen Zahl oder '~' sein. Radius muss positive Zahl sein.");
            return true;
        }

        // 3) Teleport-Punkt speichern
        CraftAttack plugin = CraftAttack.getInstance();
        plugin.getConfig().set("teleport.x", x);
        plugin.getConfig().set("teleport.y", y);
        plugin.getConfig().set("teleport.z", z);

        // 4) spawnArea Ecken berechnen und speichern
        double x1 = x - r, y1 = y - r, z1 = z - r;
        double x2 = x + r, y2 = y + r, z2 = z + r;
        plugin.getConfig().set("spawnArea.x1", x1);
        plugin.getConfig().set("spawnArea.y1", y1);
        plugin.getConfig().set("spawnArea.z1", z1);
        plugin.getConfig().set("spawnArea.x2", x2);
        plugin.getConfig().set("spawnArea.y2", y2);
        plugin.getConfig().set("spawnArea.z2", z2);

        plugin.saveConfig();

        // 5) Feedback an Spieler
        player.sendMessage(ChatColor.GREEN + "Teleport-Punkt gesetzt: " +
                formatVec(x, y, z));
        player.sendMessage(ChatColor.GREEN + "Spawn-Bereich (Radius " + r + "):");
        player.sendMessage(ChatColor.GREEN +
                "Ecke1 " + formatVec(x1, y1, z1) +
                "  Ecke2 " + formatVec(x2, y2, z2));

        return true;
    }

    /**
     * Parst eine Koordinate: "~" -> current, sonst Double.parse
     */
    private double parseArg(String arg, double current) {
        if ("~".equals(arg)) {
            return current;
        }
        return Double.parseDouble(arg);
    }

    private String formatVec(double x, double y, double z) {
        return "(" +
                String.format("%.1f", x) + ", " +
                String.format("%.1f", y) + ", " +
                String.format("%.1f", z) + ")";
    }
}