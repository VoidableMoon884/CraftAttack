package de.vmoon.craftAttack.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.vmoon.craftAttack.CraftAttack;
import de.vmoon.craftAttack.utils.JsonColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;

public class SetTextsCommand {

    public static boolean handle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /craftattack <settitle|settab> <JSON>");
            return true;
        }
        String sub = args[0].toLowerCase();
        // Fasse alle Argumente ab args[1] zu einem einzigen JSON-String zusammen.
        String newJson = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();

        if (sub.equals("settitle")) {
            // 1. Berechtigung prüfen
            if (!sender.hasPermission("ca.admin.settitle")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, den Title zu setzen!");
                return true;
            }

            // 2. JSON aus den Argumenten zusammensetzen
            //    z.B. /ca settitle {"text":"Hallo Welt","color":"aqua"}
            String newJson_1 = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            // 3. JSON-Validierung
            JsonObject json;
            try {
                json = JsonParser.parseString(newJson_1).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                sender.sendMessage(ChatColor.RED + "Ungültiges JSON! Bitte gib ein korrektes JSON-Format an.");
                return true;
            }

            // 4. Pflichtfeld "text"
            if (!json.has("text") || json.get("text").getAsString().trim().isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Das Feld \"text\" muss vorhanden und nicht leer sein.");
                return true;
            }

            // 5. Optionales Feld "color" prüfen
            Set<String> validColors = Set.of(
                    "dark_red","red","gold","yellow",
                    "dark_green","green","aqua","dark_aqua",
                    "dark_blue","blue","light_purple","dark_purple",
                    "white","gray","dark_gray","black",
                    "reset","bold","italic","underlined",
                    "strikethrough","obfuscated"
            );
            if (json.has("color")) {
                String color = json.get("color").getAsString().toLowerCase();
                if (!validColors.contains(color)) {
                    sender.sendMessage(ChatColor.RED +
                            "Ungültige Farbe \"" + color + "\". Erlaubt sind z.B. \"red\", \"gold\", \"dark_blue\" usw.");
                    return true;
                }
            }

            // 6. JSON in der Config speichern
            CraftAttack plugin = CraftAttack.getInstance();
            plugin.getConfig().set("title", newJson_1);
            plugin.saveConfig();

            // 7. Sofort anwenden
            String title = JsonColorConverter.convertTitle(newJson_1);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(title, "", 10, 40, 10);
            }

            sender.sendMessage(ChatColor.GREEN + "Title erfolgreich aktualisiert!");
            return true;
        }
        else if (sub.equals("settab")) {
            if (!CraftAttack.getInstance().getConfigManager().isTabTextEnabled()) {
                sender.sendMessage(ChatColor.RED + "Die Tabtext-Funktion ist in der Config deaktiviert.");
                return true;
            }

            if (!sender.hasPermission("ca.admin.settab")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, den Tab-Text zu setzen!");
                return true;
            }

            // Liste gültiger Farben
            Set<String> validColors = Set.of(
                    "dark_red", "red", "gold", "yellow", "dark_green", "green", "aqua", "dark_aqua",
                    "dark_blue", "blue", "light_purple", "dark_purple", "white", "gray", "dark_gray",
                    "black", "reset", "bold", "italic", "underlined", "strikethrough", "obfuscated"
            );

            // JSON-Validierung
            JsonObject json;
            try {
                json = JsonParser.parseString(newJson).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                sender.sendMessage(ChatColor.RED + "Ungültiges JSON! Bitte gib ein korrektes JSON-Format an.");
                return true;
            }

            // Pflichtfeld "text"
            if (!json.has("text") || json.get("text").getAsString().trim().isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Das Feld \"text\" muss vorhanden und nicht leer sein.");
                return true;
            }

            // Optionales Feld "color"
            if (json.has("color")) {
                String color = json.get("color").getAsString().toLowerCase();
                if (!validColors.contains(color)) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe \"" + color + "\". Erlaubt sind z.B. \"red\", \"gold\", \"dark_blue\" usw.");
                    return true;
                }
            }

            // Speichern und Anwenden
            CraftAttack.getInstance().getConfig().set("tab", newJson);
            CraftAttack.getInstance().saveConfig();

            String tabText = JsonColorConverter.convertTab(newJson);
            Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", tabText));

            sender.sendMessage(ChatColor.GREEN + "Tab-Text erfolgreich aktualisiert!");
            return true;
        }


        sender.sendMessage(ChatColor.RED + "Ungültiger Sub-Befehl. Usage: settitle|settab");
        return true;
    }
}