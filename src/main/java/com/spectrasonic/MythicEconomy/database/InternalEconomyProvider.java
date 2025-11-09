package com.spectrasonic.MythicEconomy.database;

import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.Map;
import java.io.IOException;

// Adaptador para el sistema de economía interno existente
// Implementa la interfaz EconomyDataProvider para mantener compatibilidad
@Getter
@RequiredArgsConstructor
public class InternalEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    @Override
    public double getBalance(UUID playerUUID) {
        return economyManager.playerBalances.getOrDefault(playerUUID, economyManager.startingBalance);
    }

    @Override
    public void setBalance(UUID playerUUID, double amount) {
        economyManager.playerBalances.put(playerUUID, amount);
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount) {
        double currentBalance = economyManager.playerBalances.getOrDefault(playerUUID, economyManager.startingBalance);
        economyManager.playerBalances.put(playerUUID, currentBalance + amount);
        return true;
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount) {
        double currentBalance = economyManager.playerBalances.getOrDefault(playerUUID, economyManager.startingBalance);
        if (currentBalance >= amount) {
            economyManager.playerBalances.put(playerUUID, currentBalance - amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        double currentBalance = economyManager.playerBalances.getOrDefault(playerUUID, economyManager.startingBalance);
        return currentBalance >= amount;
    }

    @Override
    public void createPlayer(UUID playerUUID) {
        if (!economyManager.playerBalances.containsKey(playerUUID)) {
            economyManager.playerBalances.put(playerUUID, economyManager.startingBalance);
        }
    }

    @Override
    public long getTotalPlayers() {
        return economyManager.playerBalances.size();
    }

    @Override
    public double getTotalMoney() {
        return economyManager.playerBalances.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public void save() {
        // Guardar directamente en el archivo sin llamar a savePlayerData para evitar recursión
        for (Map.Entry<UUID, Double> entry : economyManager.playerBalances.entrySet()) {
            String uuidString = entry.getKey().toString();
            economyManager.dataConfig.set("players." + uuidString + ".balance", entry.getValue());
        }

        try {
            economyManager.dataConfig.save(economyManager.dataFile);
        } catch (IOException e) {
            economyManager.plugin.getLogger().severe("Could not save playerdata.yml file!");
            e.printStackTrace();
        }
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