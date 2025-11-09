package com.spectrasonic.MythicEconomy.managers;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventManager {

    private final JavaPlugin plugin;

    public void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}