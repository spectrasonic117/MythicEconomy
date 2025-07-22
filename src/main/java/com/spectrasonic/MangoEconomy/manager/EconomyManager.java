package com.spectrasonic.MangoEconomy.manager;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.Utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    
    private static EconomyManager instance;
    private final JavaPlugin plugin;
    private final Map<UUID, Double> playerBalances;
    private File dataFile;
    private FileConfiguration dataConfig;
    private double startingBalance;
    private String currencySymbol;
    private String currencyName;
    private String currencyNameSingular;
    
    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerBalances = new HashMap<>();
        this.setupDataFile();
        this.loadConfiguration();
        this.loadPlayerData();
        instance = this;
        
        MessageUtils.sendConsoleMessage("<green>Sistema de economía MangoEconomy inicializado correctamente.");
    }
    
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        
        // Cargar configuración de economía
        this.startingBalance = config.getDouble("economy.starting-balance", 100.0);
        this.currencySymbol = config.getString("economy.currency.symbol", "$");
        this.currencyName = config.getString("economy.currency.name", "monedas");
        this.currencyNameSingular = config.getString("economy.currency.name-singular", "moneda");
        
        plugin.getLogger().info("Configuración cargada - Saldo inicial: " + startingBalance + " " + currencyName);
    }
    
    public static EconomyManager getInstance() {
        return instance;
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml file!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    private void loadPlayerData() {
        if (dataConfig.getConfigurationSection("players") != null) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                double balance = dataConfig.getDouble("players." + uuidString + ".balance", 0.0);
                playerBalances.put(uuid, balance);
            }
        }
    }
    
    public void savePlayerData() {
        for (Map.Entry<UUID, Double> entry : playerBalances.entrySet()) {
            String uuidString = entry.getKey().toString();
            dataConfig.set("players." + uuidString + ".balance", entry.getValue());
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml file!");
            e.printStackTrace();
        }
    }
    
    public double getBalance(Player player) {
        // Si es la primera vez que el jugador se conecta, darle el saldo inicial
        if (!playerBalances.containsKey(player.getUniqueId())) {
            playerBalances.put(player.getUniqueId(), startingBalance);
            savePlayerData();
            return startingBalance;
        }
        
        return playerBalances.getOrDefault(player.getUniqueId(), startingBalance);
    }
    
    public void setBalance(Player player, double amount) {
        if (amount < 0) {
            amount = 0;
        }
        
        playerBalances.put(player.getUniqueId(), amount);
        savePlayerData();
    }
    
    public boolean addMoney(Player player, double amount) {
        if (amount <= 0) {
            return false;
        }
        double currentBalance = getBalance(player);
        setBalance(player, currentBalance + amount);
        return true;
    }
    
    public boolean removeMoney(Player player, double amount) {
        if (amount <= 0) {
            return false;
        }
        double currentBalance = getBalance(player);
        if (currentBalance < amount) {
            return false;
        }
        setBalance(player, currentBalance - amount);
        return true;
    }
    
    public boolean hasEnoughMoney(Player player, double amount) {
        return getBalance(player) >= amount;
    }
    
    public String formatMoney(double amount) {
        return currencySymbol + String.format("%.2f", amount);
    }
    
    /**
     * Obtiene el símbolo de la moneda
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    /**
     * Obtiene el nombre de la moneda (plural)
     */
    public String getCurrencyName() {
        return currencyName;
    }
    
    /**
     * Obtiene el nombre de la moneda (singular)
     */
    public String getCurrencyNameSingular() {
        return currencyNameSingular;
    }
    
    /**
     * Obtiene el saldo inicial para nuevos jugadores
     */
    public double getStartingBalance() {
        return startingBalance;
    }
    
    /**
     * Establece el saldo inicial para nuevos jugadores
     */
    public void setStartingBalance(double amount) {
        this.startingBalance = Math.max(0, amount);
        plugin.getConfig().set("economy.starting-balance", this.startingBalance);
        plugin.saveConfig();
    }
    
    /**
     * Obtiene el top de jugadores más ricos
     */
    public Map<String, Double> getTopBalances(int limit) {
        Map<String, Double> topBalances = new java.util.LinkedHashMap<>();
        
        playerBalances.entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> {
                String playerName = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                if (playerName != null) {
                    topBalances.put(playerName, entry.getValue());
                }
            });
            
        return topBalances;
    }
    
    /**
     * Obtiene el total de dinero en circulación
     */
    public double getTotalMoney() {
        return playerBalances.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Obtiene el número total de cuentas
     */
    public int getTotalAccounts() {
        return playerBalances.size();
    }
    
    /**
     * Recarga la configuración desde el archivo
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfiguration();
        plugin.getLogger().info("Configuración de economía recargada.");
    }
}