package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Berechtigungsprüfung
        if (!sender.hasPermission("ca.admin.spawn")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu verwenden.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Diesen Befehl kannst du nur als Spieler ausführen.");
            return true;
        }
        Player player = (Player) sender;

        // Bei richtiger Ausführung erwarten wir keine weitere Argumente (weil der Sub-Befehl bereits entfernt wurde)
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "Usage: /craftattack setspawn (ohne zusätzliche Argumente)");
            return true;
        }

        // Erstelle einen Stone Axe als Auswahlwerkzeug
        ItemStack tool = new ItemStack(Material.STONE_AXE, 1);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Spawn-Auswahl Werkzeug");
        tool.setItemMeta(meta);

        // Verteile das Werkzeug dem Spieler und informiere ihn
        player.getInventory().addItem(tool);
        player.sendMessage(ChatColor.GREEN + "Du hast das Spawn-Auswahl Werkzeug erhalten.");
        player.sendMessage(ChatColor.GREEN + "Markiere mit Links- oder Rechtsklick drei Ecken des gewünschten Bereichs. Die Koordinaten der angeklickten Blöcke werden im Chat ausgegeben.");

        return true;
    }
}