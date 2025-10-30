package de.vmoon.craftAttack.commands;

import de.vmoon.craftAttack.*;
import de.vmoon.craftAttack.utils.MaintenanceManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftAttackCommand implements CommandExecutor, TabCompleter {
    private TempBanCommand tempBanCommand = new TempBanCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /craftattack <start|stop|teleportall|settitle|settab|reload|invsee|pvp>");
            return true;
        }
        String sub = args[0].toLowerCase();

        if (sub.equals("pvp")) {
            // /craftattack pvp <on|off>
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /craftattack pvp <on|off>");
                return true;
            }
            if (!sender.hasPermission("ca.admin.pvp")) {
                sender.sendMessage("§cDu hast keine Berechtigung diesen Befehl zu verwenden.");
                return true;
            }
            String pvpAction = args[1].toLowerCase();
            if (pvpAction.equals("on")) {
                CraftAttack.getInstance().getPvpCmd().enablepvp();
                sender.sendMessage("§aPvP wurde aktiviert.");
                return true;
            } else if (pvpAction.equals("off")) {
                CraftAttack.getInstance().getPvpCmd().disablepvp();
                sender.sendMessage("§aPvP wurde deaktiviert.");
                return true;
            } else {
                sender.sendMessage("§cUngültige Option. Usage: /craftattack pvp <on|off>");
                return true;
            }
        } else if (sub.equals("start") || sub.equals("stop") || sub.equals("teleportall")) {
            return StartCommand.handle(sender, args);
        } else if (sub.equals("settitle") || sub.equals("settab")) {
            return SetTextsCommand.handle(sender, args);
        } else if (sub.equals("reload")) {
            return ReloadCommand.handle(sender, args);
        } else if (sub.equals("addstatus")) {
            String[] addstatusArgs = new String[args.length - 1];
            System.arraycopy(args, 1, addstatusArgs, 0, addstatusArgs.length);
            return new AddStatusCommand(CraftAttack.getInstance()).onCommand(sender, command, label, addstatusArgs);
        } else if (sub.equals("setspawn")) {
            return new SetSpawnCommand().onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else if (sub.equals("tempban")) {
            return new TempBanCommand().onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else if (sub.equals("pregen")) {
            return new PregenCommand(CraftAttack.getInstance()).onCommand(sender, command, label, args);
        } else if (sub.equals("maintenance")) {
            return MaintenanceCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
        } else if (sub.equals("timeout")) {
            TimeoutCommand timeoutCommand = new TimeoutCommand();
            return timeoutCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else if (sub.equals("invsee")) {
            if (!CraftAttack.getInstance().getConfigManager().getConfig().getBoolean("invsee", false)) {
                sender.sendMessage("§cDer invsee-Befehl ist derzeit deaktiviert.");
                return true;
            }
            if (!sender.hasPermission("ca.admin.invsee")) {
                sender.sendMessage("§cDu hast keine Berechtigung diesen Befehl zu verwenden.");
                return true;
            }
            return InvseeCommand.handle(sender, args);
        } else {
            sender.sendMessage("§cUngültiger Sub-Befehl. Usage: /craftattack <start|stop|teleportall|settitle|settab|reload|invsee|pvp>");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Tab-Completion für den ersten Parameter (Sub-Befehle)
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("ca.admin.start")) {
                subs.add("start");
            }
            if (sender.hasPermission("ca.admin.stop")) {
                subs.add("stop");
            }
            if (sender.hasPermission("ca.admin.teleportall")) {
                subs.add("teleportall");
            }
            if (sender.hasPermission("ca.admin.settitle")) {
                subs.add("settitle");
            }
            if (sender.hasPermission("ca.admin.tempban")) {
                subs.add("tempban");
            }
            if (sender.hasPermission("ca.admin.setspawn")) {
                subs.add("setspawn");
            }
            if (sender.hasPermission("ca.admin.addstatus")) {
                subs.add("addstatus");
            }
            if (CraftAttack.getInstance().getConfigManager().isTabTextEnabled() && sender.hasPermission("ca.admin.settab")) {
                subs.add("settab");
            }
            if (sender.hasPermission("ca.admin.reload")) {
                subs.add("reload");
            }
            if (CraftAttack.getInstance().getConfigManager().getConfig().getBoolean("invsee", false)
                    && sender.hasPermission("ca.admin.invsee")) {
                subs.add("invsee");
            }
            if (sender.hasPermission("ca.admin.pvp")) {
                subs.add("pvp");
            }
            if (sender.hasPermission("ca.admin.pregen")) {
                subs.add("pregen");
            }
            if (sender.hasPermission("ca.admin.maintenance")) {
                subs.add("maintenance");
            }
            if (sender.hasPermission("ca.admin.timeout")) {
                subs.add("timeout");
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        // Tab-Completion für den zweiten Parameter beim pvp- oder invsee-Befehl
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("pvp")) {
                List<String> options = Arrays.asList("on", "off");
                return options.stream()
                        .filter(option -> option.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("invsee")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("maintenance") && sender.hasPermission("ca.admin.maintenance")) {
                List<String> options = Arrays.asList("on", "off", "addplayer", "removeplayer");
                return options.stream()
                        .filter(option -> option.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());

            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("maintenance") && sender.hasPermission("ca.admin.maintenance")) {
            MaintenanceManager manager = CraftAttack.getInstance().getMaintenanceManager();

            if (args[1].equalsIgnoreCase("addplayer")) {
                // Spieler online, die bisher nicht in maintenance whitelist sind
                Set<String> allowedNames = manager.getAllowedPlayers().stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .collect(Collectors.toSet());

                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> !allowedNames.contains(name))
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());

            } else if (args[1].equalsIgnoreCase("removeplayer")) {
                // Spieler in maintenance whitelist
                return manager.getAllowedPlayers().stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .filter(name -> name != null && name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[1].equalsIgnoreCase("on")) {
                List<String> options = Arrays.asList("force");
                return options.stream()
                        .filter(option -> option.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } if (args.length > 0 && args[0].equalsIgnoreCase("tempban")) {
            return tempBanCommand.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        } if (args.length > 0 && args[0].equalsIgnoreCase("timeout")) {
            return new TimeoutCommand().onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }
        return new ArrayList<>();
    }
}