package de.vmoon.craftAttack.utils;

import org.bukkit.ChatColor;

public class JsonColorConverter {

    // Wandelt einen JSON-ähnlichen String für Title in einen formatierten String um.
    public static String convertTitle(String json) {
        return convertText(json);
    }

    // Wandelt einen JSON-ähnlichen String für einen Tabtext in einen formatierten String um.
    public static String convertTab(String json) {
        return convertText(json);
    }

    /**
     * Verarbeitet den Input. Beginnt der String mit "[" wird er als Array
     * verarbeitet, ansonsten als einzelnes Objekt oder Literal.
     */
    private static String convertText(String json) {
        json = json.trim();
        // Falls der JSON-String ein Array ist, parsen wir alle Elemente.
        if (json.startsWith("[")) {
            return parseJsonArray(json);
        } else {
            // Einzelnes Objekt oder Literal
            return convertSingle(json);
        }
    }

    /**
     * Parst einen JSON-ähnlichen Array-String.
     * Beispiel: ["",{"text":"Hi","color":"red"},{"text":"\n"},{"text":"Dies ist ein","color":"black"}]
     */
    private static String parseJsonArray(String json) {
        // Entferne die äußeren eckigen Klammern.
        String inner = json.substring(1, json.length() - 1).trim();
        StringBuilder result = new StringBuilder();
        int braceDepth = 0;
        boolean inQuotes = false;
        StringBuilder currentElement = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                // Annahme: keine escaped Quotes
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') {
                    braceDepth++;
                }
                if (c == '}') {
                    braceDepth--;
                }
                // Ein Komma, das außerhalb von geschweiften Klammern auftaucht, trennt Elemente.
                if (c == ',' && braceDepth == 0) {
                    String element = currentElement.toString().trim();
                    if (!element.isEmpty()) {
                        result.append(parseElement(element));
                    }
                    currentElement.setLength(0);
                    continue;
                }
            }
            currentElement.append(c);
        }
        // Letztes Element verarbeiten.
        String element = currentElement.toString().trim();
        if (!element.isEmpty()) {
            result.append(parseElement(element));
        }
        return result.toString();
    }

    /**
     * Verarbeitet ein einzelnes Element des Arrays – es kann ein Objekt (mit {}) oder ein Stringliteral sein.
     */
    private static String parseElement(String element) {
        element = element.trim();
        if (element.startsWith("{")) {
            return convertSingle(element);
        } else if (element.startsWith("\"") && element.endsWith("\"")) {
            // Entferne die umschließenden Anführungszeichen.
            String literal = element.substring(1, element.length() - 1);
            return literal.replace("\\n", "\n");
        } else {
            return element;
        }
    }

    /**
     * Wandelt einen JSON-ähnlichen String, der ein Objekt repräsentiert, in einen formatierten Chat-String um.
     * Erwartetes Format: {"text":"Hi","color":"red","bold":true,"italic":true, ... }
     */
    private static String convertSingle(String json) {
        // Extrahiere den Text.
        String text = extractValue(json, "text");
        if (text == null) {
            text = "";
        }
        // Ersetze "\n" (als Literal) durch tatsächliche Zeilenumbrüche.
        text = text.replace("\\n", "\n");

        // Baue den Formatierungsstring auf.
        StringBuilder format = new StringBuilder();

        // Falls ein Farbwert gesetzt ist.
        String color = extractValue(json, "color");
        if (color != null && !color.isEmpty()) {
            try {
                ChatColor chatColor = ChatColor.valueOf(color.toUpperCase());
                format.append(chatColor.toString());
            } catch (Exception e) {
                // Falls der Farbwert ungültig ist, wird er ignoriert.
            }
        }
        // Optionale Formatierungen:
        if ("true".equalsIgnoreCase(extractValue(json, "bold"))) {
            format.append(ChatColor.BOLD);
        }
        if ("true".equalsIgnoreCase(extractValue(json, "italic"))) {
            format.append(ChatColor.ITALIC);
        }
        if ("true".equalsIgnoreCase(extractValue(json, "underlined"))) {
            format.append(ChatColor.UNDERLINE);
        }
        if ("true".equalsIgnoreCase(extractValue(json, "strikethrough"))) {
            format.append(ChatColor.STRIKETHROUGH);
        }
        if ("true".equalsIgnoreCase(extractValue(json, "obfuscated"))) {
            format.append(ChatColor.MAGIC);
        }

        return format.toString() + text;
    }

    /**
     * Extrahiert den Wert eines Schlüssels aus einem JSON-ähnlichen String.
     * Achtung: Diese Methode ist sehr simpel und erwartet genaue Muster.
     */
    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        // Jetzt prüfen, ob es sich um einen Stringwert handelt
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            return json.substring(start, end);
        } else {
            // Für boolesche oder numerische Werte
            int end = json.indexOf(",", start);
            if (end == -1) {
                end = json.indexOf("}", start);
            }
            if (end == -1) return "";
            return json.substring(start, end).trim();
        }
    }
}