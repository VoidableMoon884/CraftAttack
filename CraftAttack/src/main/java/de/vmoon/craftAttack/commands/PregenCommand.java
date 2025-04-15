package de.vmoon.craftAttack.commands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class PregenCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    // Konstruktor: Plugin-Instanz wird benötigt für den Scheduler
    public PregenCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Berechtigungsprüfung
        if (!sender.hasPermission("ca.admin.pregen")) {
            sender.sendMessage("§cDu hast keine Berechtigung diesen Befehl zu verwenden.");
            return true;
        }
        // Minimaler Parameter: /craftattack pregen [blockweite] ([confirm])
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /craftattack pregen [blockweite] [confirm]");
            return true;
        }

        // Blockweite parsen und validieren
        int blockRange;
        try {
            blockRange = Integer.parseInt(args[1]);
            if (blockRange <= 0) {
                sender.sendMessage("§cDie Blockweite muss eine positive Zahl sein.");
                return true;
            }
        } catch (NumberFormatException ex) {
            sender.sendMessage("§cDie Blockweite muss eine gültige Zahl sein.");
            return true;
        }

        // Berechne den Chunk-Radius und Gesamtchunkanzahl:
        int chunkRadius = (int) Math.ceil(blockRange / 16.0);
        int totalChunks = (2 * chunkRadius + 1) * (2 * chunkRadius + 1);

        // Bei Spielern – wenn noch nicht bestätigt – eine Bestätigung anfordern.
        if (sender instanceof Player && args.length < 3) {
            sender.sendMessage("§eWarnung: Du wirst insgesamt " + totalChunks + " Chunks (für " + blockRange + " Blöcke) generieren, was den Server stark belasten kann.");
            sender.sendMessage("§eBitte bestätige mit: /craftattack pregen " + blockRange + " confirm");
            return true;
        }

        // Ermittle Welt und Mittelpunkt (Spielerposition oder Ursprung bei der Konsole)
        World world;
        int centerX, centerZ;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            world = player.getWorld();
            centerX = player.getLocation().getBlockX();
            centerZ = player.getLocation().getBlockZ();
        } else {
            world = Bukkit.getWorlds().get(0);
            centerX = 0;
            centerZ = 0;
        }

        // Starte den Vorgang – in der Konsole erhalten die Betreiber nur zu Beginn eine Nachricht
        sender.sendMessage("§aChunk-Pregen gestartet: Generiere " + totalChunks + " Chunks (Blockweite: " + blockRange + ").");

        // Ermittle den Chunk des Zentrums
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;

        // Erstelle eine Queue aller zu ladender Chunks
        Queue<int[]> chunkQueue = new ArrayDeque<>();
        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                chunkQueue.add(new int[]{x, z});
            }
        }

        // Starte einen BukkitRunnable, der den Chunk-Ladevorgang asynchron (über mehrere Ticks) abarbeitet
        new BukkitRunnable() {
            final int chunksPerTick = 10; // Anzahl Chunks pro Tick (anpassbar)
            int loadedChunks = 0;

            @Override
            public void run() {
                int counter = 0;
                while (counter < chunksPerTick && !chunkQueue.isEmpty()) {
                    int[] coord = chunkQueue.poll();
                    int chunkX = coord[0];
                    int chunkZ = coord[1];
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) {
                        // Chunk-Loading; true = forcieren, synchron, da diese Methode auf dem Main-Thread laufen muss
                        chunk.load(true);
                    }
                    loadedChunks++;
                    counter++;
                }

                // Wenn der Sender ein Spieler ist, sende nur alle 1000 geladene Chunks eine Fortschrittsmeldung
                if (sender instanceof Player) {
                    if (loadedChunks % 1000 == 0 && loadedChunks < totalChunks) {
                        sender.sendMessage("§aFortschritt: " + loadedChunks + " von " + totalChunks + " Chunks geladen.");
                    }
                }
                // Beende die Aufgabe, wenn alle Chunks bearbeitet wurden.
                if (chunkQueue.isEmpty()) {
                    if (sender instanceof Player) {
                        sender.sendMessage("§aChunk-Pregen abgeschlossen. Gesamt: " + loadedChunks + " von " + totalChunks + " Chunks geladen.");
                    } else {
                        sender.sendMessage("§aChunk-Pregen abgeschlossen.");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ca.admin.pregen")) {
            return new ArrayList<>();
        }
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("256");
            suggestions.add("512");
            suggestions.add("1024");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("confirm");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}