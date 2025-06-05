package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.CraftAttack;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VanishCommand implements CommandExecutor {

    // Speichert alle Spieler, die im Vanish-Modus sind.
    private final Set<Player> vanishedPlayers = new HashSet<>();
    private final CraftAttack plugin;

    public VanishCommand(CraftAttack plugin) {
        this.plugin = plugin;
    }

    /**
     * Entfernt den Spieler aus der Tabliste, indem ein REMOVE_PLAYER-Paket via ProtocolLib an alle gesendet wird.
     */
    private void removeFromTabList(Player player) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager == null) {
            plugin.getLogger().severe("ProtocolLib ist nicht geladen! Kann Spieler nicht aus der Tabliste entfernen.");
            return;
        }
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        List<PlayerInfoData> infoDataList = new ArrayList<>();
        PlayerInfoData infoData = new PlayerInfoData(
                WrappedGameProfile.fromPlayer(player),
                0, // Ping, hier als 0 gesetzt
                EnumWrappers.NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(player.getDisplayName())
        );
        infoDataList.add(infoData);
        packet.getPlayerInfoDataLists().write(0, infoDataList);

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                protocolManager.sendServerPacket(online, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fügt den Spieler wieder in die Tabliste ein, indem ein ADD_PLAYER-Paket via ProtocolLib an alle gesendet wird.
     */
    private void addToTabList(Player player) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager == null) {
            plugin.getLogger().severe("ProtocolLib ist nicht geladen! Kann Spieler nicht zur Tabliste hinzufügen.");
            return;
        }
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        List<PlayerInfoData> infoDataList = new ArrayList<>();
        PlayerInfoData infoData = new PlayerInfoData(
                WrappedGameProfile.fromPlayer(player),
                0,
                EnumWrappers.NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(player.getDisplayName())
        );
        infoDataList.add(infoData);
        packet.getPlayerInfoDataLists().write(0, infoDataList);

        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                protocolManager.sendServerPacket(online, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Versetzt den Spieler in den Vanish-Modus, indem er von allen anderen Spielern versteckt wird
     * und aus der Tabliste entfernt wird.
     */
    public void vanish(Player player) {
        // Verstecke den Spieler vor allen anderen
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }
        removeFromTabList(player);
        vanishedPlayers.add(player);
    }

    /**
     * Macht den Spieler wieder sichtbar und fügt ihn der Tabliste wieder hinzu.
     */
    public void unvanish(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        addToTabList(player);
        vanishedPlayers.remove(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("ca.vanish")) {
            player.sendMessage("§cDu hast keine Berechtigung diesen Befehl zu verwenden.");
            return true;
        }

        if (vanishedPlayers.contains(player)) {
            unvanish(player);
            player.sendMessage("§aDu bist jetzt wieder sichtbar.");
        } else {
            vanish(player);
            player.sendMessage("§aDu bist nun vollständig unsichtbar. Rüstung, Items und dein Name in der Tabliste werden verborgen.");
        }
        return true;
    }
}