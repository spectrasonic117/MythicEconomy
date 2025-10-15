package com.spectrasonic.MythicEconomy.database;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Adaptador para el sistema de economía interno existente
 * Implementa la interfaz EconomyDataProvider para mantener compatibilidad
 */
@RequiredArgsConstructor
public class InternalEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    @Override
    public double getBalance(UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            return economyManager.getBalance(player);
        }
        return 0.0;
    }

    @Override
    public void setBalance(UUID playerUUID, double amount) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            economyManager.setBalance(player, amount);
        }
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            return economyManager.addMoney(player, amount);
        }
        return false;
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            return economyManager.removeMoney(player, amount);
        }
        return false;
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            return economyManager.hasEnoughMoney(player, amount);
        }
        return false;
    }

    @Override
    public void createPlayer(UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            // El sistema interno ya maneja la creación automática de jugadores
            // Solo necesitamos asegurar que tenga el saldo inicial
            double currentBalance = getBalance(playerUUID);
            if (currentBalance == 0) {
                double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
                setBalance(playerUUID, startingBalance);
            }
        }
    }

    @Override
    public long getTotalPlayers() {
        return economyManager.getTotalAccounts();
    }

    @Override
    public double getTotalMoney() {
        return economyManager.getTotalMoney();
    }

    @Override
    public void save() {
        economyManager.savePlayerData();
    }

    @Override
    public void load() {
        // El sistema interno carga automáticamente en el constructor
        // No necesitamos hacer nada aquí
    }

    @Override
    public boolean isAvailable() {
        return economyManager != null;
    }
}