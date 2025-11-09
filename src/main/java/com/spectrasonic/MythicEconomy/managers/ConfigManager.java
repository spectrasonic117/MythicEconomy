package com.spectrasonic.MythicEconomy.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigManager {

    private final JavaPlugin plugin;

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public Object get(String path) {
        return plugin.getConfig().get(path);
    }

    public Object get(String path, Object defaultValue) {
        return plugin.getConfig().get(path, defaultValue);
    }

    public void set(String path, Object value) {
        plugin.getConfig().set(path, value);
    }
}