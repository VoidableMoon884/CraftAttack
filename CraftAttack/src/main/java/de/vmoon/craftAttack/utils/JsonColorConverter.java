package de.vmoon.craftAttack.utils;

import org.bukkit.ChatColor;

public class JsonColorConverter {

    // Öffentliche Methode, um einen JSON-ähnlichen String zu konvertieren
    public static String convertJson(String json) {
        return convertText(json);
    }

    // Bereits vorhandene Methoden für Titles und Tabtexte
    public static String convertTitle(String json) {
        return convertText(json);
    }

    public static String convertTab(String json) {
        return convertText(json);
    }

    /**
     * Interne Umwandlung – prüft, ob es sich um ein Array oder ein einzelnes Objekt handelt.
     */
    private static String convertText(String json) {
        json = json.trim();
        if (json.startsWith("[")) {
            return parseJsonArray(json);
        } else {
            return convertSingle(json);
        }
    }

    private static String parseJsonArray(String json) {
        String inner = json.substring(1, json.length() - 1).trim();
        StringBuilder result = new StringBuilder();
        int braceDepth = 0;
        boolean inQuotes = false;
        StringBuilder currentElement = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') {
                    braceDepth++;
                }
                if (c == '}') {
                    braceDepth--;
                }
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
        String element = currentElement.toString().trim();
        if (!element.isEmpty()) {
            result.append(parseElement(element));
        }
        return result.toString();
    }

    private static String parseElement(String element) {
        element = element.trim();
        if (element.startsWith("{")) {
            return convertSingle(element);
        } else if (element.startsWith("\"") && element.endsWith("\"")) {
            String literal = element.substring(1, element.length() - 1);
            return literal.replace("\\n", "\n");
        } else {
            return element;
        }
    }

    // Hier wurde die Methode von private nach public entfernt – alternativ kannst du auch Option B nutzen, indem du eine neue Methode wie convertJson schreibst.
    public static String convertSingle(String json) {
        String text = extractValue(json, "text");
        if (text == null) {
            text = "";
        }
        text = text.replace("\\n", "\n");
        StringBuilder format = new StringBuilder();
        String color = extractValue(json, "color");
        if (color != null && !color.isEmpty()) {
            try {
                ChatColor chatColor = ChatColor.valueOf(color.toUpperCase());
                format.append(chatColor.toString());
            } catch (Exception e) {
                // Ungültige Farbe -> ignorieren
            }
        }
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

    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start);
            if (end == -1) {
                end = json.indexOf("}", start);
            }
            if (end == -1) return "";
            return json.substring(start, end).trim();
        }
    }
}