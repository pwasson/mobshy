/*
 * Copyright 2018 Philip Wasson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.me.pwasson.mobshy;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


/**
 *
 * @author pwasson
 */
public class MobShyCommandExecutor implements CommandExecutor, TabCompleter {
    private final MobShyPlugin plugin;


    public MobShyCommandExecutor(MobShyPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (!p.isOp()) {
                sender.sendMessage("mobshy can only be used by server operators.");
                return false;
            }
        }

        // verify we at least have a subcommand
        if (null == args || args.length < 1) {
            return false;
        }
        
        String subcommand = args[0].toLowerCase();
        if (null == subcommand) {
            return false;
        } else switch (subcommand) {
            case "add":
            {
                if (args.length != 2) {
                    return false;
                }
                if (!sender.hasPermission("mobshy.modify")) {
                    plugin.broadcast(sender, true, "You don't have permission.");
                    return true;
                }
                String playerName = args[1].toLowerCase();
                OfflinePlayer theChicken = findPlayer(playerName);
                if (null != theChicken) {
                    if (plugin.addChicken(theChicken.getUniqueId().toString(), theChicken.getName(), theChicken))
                        plugin.broadcast(sender, false, String.format("\"%s\" added to the chicken list.", playerName));
                    else
                        plugin.broadcast(sender, false, String.format("\"%s\" was already a chicken.", playerName));
                    return true;
                } else {
                    plugin.broadcast(sender, true, String.format("\"%s\" is not a known player.", playerName));
                    return false;
                }
            }
            case "remove":
            {
                if (args.length != 2) {
                    return false;
                }
                if (!sender.hasPermission("mobshy.modify")) {
                    plugin.broadcast(sender, true, "You don't have permission.");
                    return true;
                }
                String playerName = args[1].toLowerCase();
                OfflinePlayer theChicken = findPlayer(playerName);
                if (null != theChicken) {
                    if (plugin.removeChicken(theChicken.getUniqueId().toString(), theChicken.getName(), theChicken))
                        plugin.broadcast(sender, false, String.format("\"%s\" removed from the chicken list.", playerName));
                    else
                        plugin.broadcast(sender, false, String.format("\"%s\" was not a chicken.", playerName));
                    return true;
                } else {
                    plugin.broadcast(sender, true, String.format("\"%s\" is not a known player.", playerName));
                    return false;
                }
            }
            case "list":
            {
                if (!sender.hasPermission("mobshy.list")) {
                    plugin.broadcast(sender, true, "You don't have permission.");
                    return true;
                }
                List<String> names = plugin.getChickenNames();
                if (null == names || names.isEmpty()) {
                    plugin.broadcast(sender, false, "There are currently no chickens.");
                } else {
                    names.sort(null);
                    String namesFragment = org.apache.commons.lang.StringUtils.join(names, ", ");
                    plugin.broadcast(sender, false, String.format("Current chickens: %s", namesFragment));
                }
                return true;
            }
            default:
                return false;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,
            Command command,
            String alias,
            String[] args) {// the args _after_ the command name
        if (args.length == 0)
            return java.util.Arrays.asList("add", "remove", "list");
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("add".startsWith(partial))
                return java.util.Arrays.asList("add");
            else if ("remove".startsWith(partial))
                return java.util.Arrays.asList("remove");
            else if ("list".startsWith(partial))
                return java.util.Arrays.asList("list");
        }
        return null;
    }


    private OfflinePlayer findPlayer(String name) {
        for (OfflinePlayer onePlayer : Bukkit.getOfflinePlayers()) {
            if (onePlayer.getName().equalsIgnoreCase(name))
                return onePlayer;
        }

        return null;
    }
}
