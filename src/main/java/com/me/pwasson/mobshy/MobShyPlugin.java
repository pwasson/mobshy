/*
 * Copyright 2018 Philip Wasson.
 * All rights reserved.
 */
package com.me.pwasson.mobshy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author pwasson
 */
public class MobShyPlugin extends JavaPlugin {
    private final Map<String, String> chickens = new HashMap<>();


    @Override
    public void onEnable() {
        try {
            MobShyCommandExecutor myExec = new MobShyCommandExecutor(this);
            PluginCommand mobShyCommand = this.getCommand("mobshy");
            mobShyCommand.setExecutor(myExec);
            mobShyCommand.setTabCompleter(myExec);
            getServer().getPluginManager().registerEvents(new EventHandler(this), this);

            readChickens();

            getLogger().info("MobShy loaded.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to enable the MobShy plugin: ", e);
        }
    }


    @Override
    public void onDisable() {
        // TODO are we supposed to deregister commands and event handlers?
        getLogger().info("MobShy unloaded");
    }


    private void readChickens() {
        File chickensFile = new File(getDataFolder(), "chickens.xml");
        if (chickensFile.isFile()) {
            try (InputStream is = new FileInputStream(chickensFile)) {
                Properties chickenProps = new Properties();
                chickenProps.loadFromXML(is);
                for (String key : chickenProps.stringPropertyNames()) {
                    this.chickens.put(key, chickenProps.getProperty(key));
                }
                broadcast(null, false, "Read " + chickens.size() + " chickens from settings.");
            } catch (IOException iox) {
                this.getLogger().log(Level.SEVERE,
                        "Unable to load settings file; continuing without", iox);
            }
        }
    }


    private void saveChickens() {
        File ourDir = getDataFolder();
        ourDir.mkdirs();
        if (ourDir.isDirectory()) {
            File chickensFile = new File(ourDir, "chickens.xml");
            this.getLogger().log(Level.SEVERE, "saving MobShy chickens to {0}", 
                    chickensFile.getAbsolutePath());
            try (OutputStream os = new FileOutputStream(chickensFile)) {
                Properties chickenProps = new Properties();
                for (Map.Entry<String, String> ent : chickens.entrySet()) {
                    chickenProps.put(ent.getKey(), ent.getValue());
                }
                chickenProps.storeToXML(os,
                        "Do not edit this file while the server is running; it will be overwritten");
            } catch (IOException iox) {
                this.getLogger().log(Level.SEVERE, "Unable to save settings file!", iox);
            }
        } else {
            getLogger().log(Level.SEVERE, "Data directory does not exist and cannot be created.");
        }
    }


    /**
     * adds a player to the chicken list if they are not already on it.
     * @param uuid
     * @param name
     * @param playerWhoJoined
     * @return whether the user was added: true if they were added, false if they were already
     *         on the list.
     */
    public boolean addChicken(String uuid,
            String name,
            OfflinePlayer playerWhoJoined) {
        String curValue = chickens.get(uuid);
        if (null == curValue || !curValue.equals(name)) {
            chickens.put(uuid, name);
            saveChickens();
            establishDifficulty(name, null, null);
            return true;
        }
        return false;
    }


    /**
     * removes a player from the chicken list if they are on it.
     * @param uuid
     * @param name
     * @param playerWhoQuit
     * @return whether the user was removed: true if they were removed, false if they were not 
     *         already on the list.
     */
    public boolean removeChicken(String uuid,
            String name,
            OfflinePlayer playerWhoQuit) {
        if (chickens.containsKey(uuid)) {
            chickens.remove(uuid);
            saveChickens();
            establishDifficulty(name, null, null);
            return true;
        }
        return false;
    }


    public List<String> getChickenNames() {
        return new ArrayList<>(chickens.values());
    }


    /**
     * compares the chicken list with the world difficulty and adjusts the difficulty as needed.
     * @param changedPlayerName the player that joined or left, thus causing the change, or null.
     * @param playerJoined a player we know is joining but might not be in the official "online players" list yet.
     * @param playerQuit a player we know is quitting and should be considered to be offline.
     */
    public void establishDifficulty(String changedPlayerName,
            OfflinePlayer playerJoined,
            OfflinePlayer playerQuit) {
        Set<? extends Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        boolean anyChickens = false;
        for (Player onePlayer : onlinePlayers) {
            // ignore players we know are quitting
            if (null != playerQuit && onePlayer.getUniqueId().equals(playerQuit.getUniqueId()))
                continue;
            if (chickens.containsKey(onePlayer.getUniqueId().toString())) {
                anyChickens = true;
                break;
            }
        }
        if (!anyChickens && null != playerJoined && chickens.containsKey(playerJoined.getUniqueId().toString()))
            anyChickens = true;

        for (World oneWorld : Bukkit.getWorlds()) {
            if (oneWorld.getEnvironment().equals(Environment.NORMAL)) {
                if (anyChickens && !oneWorld.getDifficulty().equals(Difficulty.PEACEFUL)) {
                    oneWorld.setDifficulty(Difficulty.PEACEFUL);
                    if (null != changedPlayerName)
                        broadcast(null, false, String.format("Difficulty set to Peaceful because %s is a chicken.", changedPlayerName));
                    else
                        broadcast(null, false, "Difficulty set to Peaceful");
                } else if (!anyChickens && oneWorld.getDifficulty().equals(Difficulty.PEACEFUL)) {
                    // TODO make the non-chicken difficulty configurable
                    Difficulty nonChickenDifficulty = Difficulty.NORMAL;
                    oneWorld.setDifficulty(nonChickenDifficulty);
                    if (null != changedPlayerName)
                        broadcast(null, true,
                                String.format("Difficulty set back to %s because the chicken %s left or is no longer a chicken.",
                                        nonChickenDifficulty.toString(), changedPlayerName));
                    else
                        broadcast(null, true, "Difficulty set to " + nonChickenDifficulty.toString());
                }
            }
        }
    }


    /**
     * send a message to a particular player or broadcast to the world.
     * @param sender the command sender to send the message to, or null to broadcast to the world.
     * @param danger basically: true to color the message red or false to color it white.
     * @param msg    the message to send.
     */
    public void broadcast(CommandSender sender,
            boolean danger,
            String msg) {
        String PREFIX = "[MobShy] ";
        ChatColor MsgColor = danger ? ChatColor.RED : ChatColor.WHITE;

        String finalMsg = ChatColor.BLUE + PREFIX + MsgColor + msg;

        getServer().broadcastMessage(finalMsg);
    }
}
