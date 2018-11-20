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

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


/**
 *
 * @author pwasson
 */
public class EventHandler implements org.bukkit.event.Listener {
    private final MobShyPlugin plugin;


    public EventHandler(MobShyPlugin plugin) {
        this.plugin = plugin;
    }


    /**
     * Receives an event when a player joins the server; if this player is the only online chicken,
     * the server&rsquo;s difficulty will be reset to Peaceful.
     * @param inEvent
     */
    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent inEvent) {
        plugin.establishDifficulty(inEvent.getPlayer().getName(), inEvent.getPlayer(), null);
    }


    /**
     * Receives an event when a player leaves the server; if this player was the last online chicken,
     * the server&rsquo;s difficulty will be reset to the default.
     * @param inEvent
     */
    @org.bukkit.event.EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent inEvent) {
        plugin.establishDifficulty(inEvent.getPlayer().getName(), null, inEvent.getPlayer());
    }
}
