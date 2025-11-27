package com.spectrasonic.MythicEconomy.database;

import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;

// Adaptador para el sistema de economía interno existente
// Implementa la interfaz EconomyDataProvider para mantener compatibilidad
// Además incluye soporte para nombres de jugadores
@Getter
@RequiredArgsConstructor
public class InternalEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    // Mapa para almacenar balances por moneda: currencyId -> (playerUUID -> balance)
    private final Map<String, Map<UUID, Double>> currencyBalances = new HashMap<>();
    
    // Mapa para almacenar nombres de jugadores: playerUUID -> playerName
    private final Map<UUID, String> playerNames = new HashMap<>();

    @Override
    public double getBalance(UUID playerUUID) {
        // Para compatibilidad, usa la moneda por defecto
        return getBalance(playerUUID, "default");
    }

    @Override
    public void setBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        setBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return addBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return removeBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return hasEnoughBalance(playerUUID, amount, "default");
    }

    // Métodos para múltiples monedas
    public double getBalance(UUID playerUUID, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        return currencyMap.getOrDefault(playerUUID, economyManager.getCurrencyManager().getCurrency(currencyId).getStartingBalance());
    }

    public void setBalance(UUID playerUUID, double amount, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        currencyMap.put(playerUUID, amount);
    }

    public boolean addBalance(UUID playerUUID, double amount, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        double currentBalance = currencyMap.getOrDefault(playerUUID, economyManager.getCurrencyManager().getCurrency(currencyId).getStartingBalance());
        currencyMap.put(playerUUID, currentBalance + amount);
        return true;
    }

    public boolean removeBalance(UUID playerUUID, double amount, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        double currentBalance = currencyMap.getOrDefault(playerUUID, economyManager.getCurrencyManager().getCurrency(currencyId).getStartingBalance());
        if (currentBalance >= amount) {
            currencyMap.put(playerUUID, currentBalance - amount);
            return true;
        }
        return false;
    }

    public boolean hasEnoughBalance(UUID playerUUID, double amount, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        double currentBalance = currencyMap.getOrDefault(playerUUID, economyManager.getCurrencyManager().getCurrency(currencyId).getStartingBalance());
        return currentBalance >= amount;
    }

    // Métodos heredados de la interfaz (para compatibilidad hacia atrás)
    // Estos métodos están marcados como @Deprecated para indicar que se deben usar las versiones con currencyId
    
    @Override
    @Deprecated
    public void createPlayer(UUID playerUUID) {
        if (!economyManager.playerBalances.containsKey(playerUUID)) {
            economyManager.playerBalances.put(playerUUID, economyManager.startingBalance);
        }
    }

    // Métodos heredados de la interfaz para compatibilidad hacia atrás
    // Nota: Estos métodos fueron eliminados de la interfaz para evitar conflictos
    // pero se mantienen aquí para compatibilidad con implementaciones anteriores
    
    @Deprecated
    public long getTotalPlayers() {
        // Para compatibilidad, cuenta solo la moneda por defecto
        return getTotalPlayers("default");
    }

    @Deprecated
    public double getTotalMoney() {
        // Para compatibilidad, suma solo la moneda por defecto
        return getTotalMoney("default");
    }

    // Implementación de métodos nuevos de la interfaz
    @Override
    public void createPlayer(UUID playerUUID, String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
        double startingBalance = economyManager.getCurrencyManager().getCurrency(currencyId).getStartingBalance();
        currencyMap.putIfAbsent(playerUUID, startingBalance);
    }

    @Override
    public long getTotalUniquePlayers() {
        return currencyBalances.values().stream()
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .count();
    }

    @Override
    public double getTotalMoneyAllCurrencies() {
        return currencyBalances.values().stream()
                .flatMapToDouble(map -> map.values().stream().mapToDouble(Double::doubleValue))
                .sum();
    }

    @Override
    public Object[][] getTopBalances(String currencyId, int limit) {
        Map<UUID, Double> currencyMap = currencyBalances.get(currencyId);
        if (currencyMap == null || currencyMap.isEmpty()) {
            return new Object[0][0];
        }

        return currencyMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> new Object[]{entry.getKey().toString(), entry.getValue()})
                .toArray(Object[][]::new);
    }

    // Métodos para múltiples monedas
    public long getTotalPlayers(String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.get(currencyId);
        return currencyMap != null ? currencyMap.size() : 0;
    }

    public double getTotalMoney(String currencyId) {
        Map<UUID, Double> currencyMap = currencyBalances.get(currencyId);
        return currencyMap != null ? currencyMap.values().stream().mapToDouble(Double::doubleValue).sum() : 0.0;
    }

    @Override
    public void save() {
        // Guardar todas las monedas en el archivo
        for (Map.Entry<String, Map<UUID, Double>> currencyEntry : currencyBalances.entrySet()) {
            String currencyId = currencyEntry.getKey();
            Map<UUID, Double> playerBalances = currencyEntry.getValue();

            for (Map.Entry<UUID, Double> entry : playerBalances.entrySet()) {
                String uuidString = entry.getKey().toString();
                economyManager.dataConfig.set("currencies." + currencyId + ".players." + uuidString + ".balance", entry.getValue());
            }
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
        // Cargar datos desde el archivo de configuración
        if (economyManager.dataConfig.getConfigurationSection("currencies") != null) {
            for (String currencyId : economyManager.dataConfig.getConfigurationSection("currencies").getKeys(false)) {
                if (economyManager.dataConfig.getConfigurationSection("currencies." + currencyId + ".players") != null) {
                    Map<UUID, Double> currencyMap = currencyBalances.computeIfAbsent(currencyId, k -> new HashMap<>());
                    for (String uuidString : economyManager.dataConfig.getConfigurationSection("currencies." + currencyId + ".players").getKeys(false)) {
                        UUID uuid = UUID.fromString(uuidString);
                        double balance = economyManager.dataConfig.getDouble("currencies." + currencyId + ".players." + uuidString + ".balance", 0.0);
                        currencyMap.put(uuid, balance);
                    }
                }
            }
        }

        // Para compatibilidad hacia atrás, cargar también el formato antiguo
        if (economyManager.dataConfig.getConfigurationSection("players") != null) {
            Map<UUID, Double> defaultMap = currencyBalances.computeIfAbsent("default", k -> new HashMap<>());
            for (String uuidString : economyManager.dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                double balance = economyManager.dataConfig.getDouble("players." + uuidString + ".balance", 0.0);
                defaultMap.put(uuid, balance);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return economyManager != null;
    }

    @Override
    public Object[][] getTopBalancesWithNames(String currencyId, int limit) {
        Map<UUID, Double> currencyMap = currencyBalances.get(currencyId);
        if (currencyMap == null || currencyMap.isEmpty()) {
            return new Object[0][0];
        }

        return currencyMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(entry -> new Object[]{
                    entry.getKey().toString(), // UUID como String
                    playerNames.getOrDefault(entry.getKey(), "Unknown"), // Nombre del jugador
                    entry.getValue() // Balance
                })
                .toArray(Object[][]::new);
    }

    @Override
    public void updatePlayerName(UUID playerUUID, String playerName) {
        if (playerName != null && !playerName.trim().isEmpty()) {
            playerNames.put(playerUUID, playerName);
        }
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return playerNames.get(playerUUID);
    }

    @Override
    public Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs) {
        Map<UUID, String> names = new HashMap<>();
        
        for (UUID uuid : playerUUIDs) {
            String name = playerNames.get(uuid);
            if (name != null) {
                names.put(uuid, name);
            }
        }
        
        return names;
    }

    @Override
    public void syncPlayerNames(Map<UUID, String> activePlayers) {
        for (Map.Entry<UUID, String> entry : activePlayers.entrySet()) {
            UUID playerUUID = entry.getKey();
            String playerName = entry.getValue();
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                playerNames.put(playerUUID, playerName);
            }
        }
    }
}