package com.spectrasonic.MythicEconomy.manager;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import com.spectrasonic.Utils.MessageUtils;
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import com.spectrasonic.MythicEconomy.api.events.MoneyRemoveEvent;
import com.spectrasonic.MythicEconomy.database.EconomyDataProvider;
import com.spectrasonic.MythicEconomy.database.InternalEconomyProvider;
import com.spectrasonic.MythicEconomy.database.MongoDBConnection;
import com.spectrasonic.MythicEconomy.database.MongoDBEconomyProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private static EconomyManager instance;
    private final JavaPlugin plugin;
    private EconomyDataProvider dataProvider;
    private MongoDBConnection mongoConnection;

    // Configuración de respaldo para sistema interno
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

        // Inicializar configuración
        this.loadConfiguration();

        // Inicializar proveedor de datos basado en configuración
        this.initializeDataProvider();

        // Configurar respaldo para sistema interno si es necesario
        if (dataProvider instanceof InternalEconomyProvider) {
            this.setupDataFile();
            this.loadPlayerData();
        }

        instance = this;
        MessageUtils.sendConsoleMessage("<green>Sistema de economía MythicEconomy inicializado correctamente.");
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

    // Inicializa el proveedor de datos basado en la configuración
    private void initializeDataProvider() {
        FileConfiguration config = plugin.getConfig();
        boolean useExternalDB = config.getBoolean("database.use-external-database", false);
        String databaseType = config.getString("database.type", "FILE");

        if (useExternalDB && "MONGODB".equalsIgnoreCase(databaseType)) {
            // Usar MongoDB
            this.mongoConnection = new MongoDBConnection(plugin);

            if (mongoConnection.connect()) {
                this.dataProvider = new MongoDBEconomyProvider(plugin, mongoConnection);
                plugin.getLogger().info("Usando MongoDB como proveedor de datos de economía");
            } else {
                // Fallback al sistema interno si MongoDB falla
                plugin.getLogger().warning("No se pudo conectar a MongoDB, usando sistema interno");
                this.dataProvider = new InternalEconomyProvider(plugin, this);
            }
        } else {
            // Usar sistema interno
            this.dataProvider = new InternalEconomyProvider(plugin, this);
            plugin.getLogger().info("Usando sistema interno de archivos como proveedor de datos");
        }
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
        // Usar el proveedor de datos para guardar
        dataProvider.save();

        // Si es el sistema interno, también guardar el respaldo
        if (dataProvider instanceof InternalEconomyProvider) {
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
    }

    public double getBalance(Player player) {
        // Usar el proveedor de datos configurado
        double balance = dataProvider.getBalance(player.getUniqueId());

        // Si es MongoDB y el jugador no existe, crearlo
        if (dataProvider instanceof MongoDBEconomyProvider && balance == startingBalance) {
            dataProvider.createPlayer(player.getUniqueId());
        }

        return balance;
    }

    public void setBalance(Player player, double amount) {
        if (amount < 0) {
            amount = 0;
        }

        dataProvider.setBalance(player.getUniqueId(), amount);
    }

    public boolean addMoney(Player player, double amount) {
        if (amount <= 0) {
            return false;
        }
        double currentBalance = getBalance(player);
        double newBalance = currentBalance + amount;

        // Disparar evento
        MoneyAddEvent event = new MoneyAddEvent(player, amount, currentBalance, newBalance);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        dataProvider.setBalance(player.getUniqueId(), newBalance);
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
        double newBalance = currentBalance - amount;

        // Disparar evento
        MoneyRemoveEvent event = new MoneyRemoveEvent(player, amount, currentBalance, newBalance);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        dataProvider.setBalance(player.getUniqueId(), newBalance);
        return true;
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        return dataProvider.hasEnoughBalance(player.getUniqueId(), amount);
    }

    public String formatMoney(double amount) {
        return currencySymbol + String.format("%.2f", amount);
    }

    // Obtiene el símbolo de la moneda
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    // Obtiene el nombre de la moneda (plural)
    public String getCurrencyName() {
        return currencyName;
    }

    // Obtiene el nombre de la moneda (singular)
    public String getCurrencyNameSingular() {
        return currencyNameSingular;
    }

    // Obtiene el saldo inicial para nuevos jugadores
    public double getStartingBalance() {
        return startingBalance;
    }

    // Establece el saldo inicial para nuevos jugadores
    public void setStartingBalance(double amount) {
        this.startingBalance = Math.max(0, amount);
        plugin.getConfig().set("economy.starting-balance", this.startingBalance);
        plugin.saveConfig();
    }

    // Obtiene el top de jugadores más ricos
    public Map<String, Double> getTopBalances(int limit) {
        // Nota: Esta implementación necesitaría ser mejorada para MongoDB
        // Por simplicidad, devolveremos una implementación básica usando el
        // dataProvider
        Map<String, Double> topBalances = new java.util.LinkedHashMap<>();

        // Esta es una implementación simplificada
        // En un escenario real, usarías agregaciones de MongoDB para obtener el top
        return topBalances;
    }

    // Obtiene el total de dinero en circulación
    public double getTotalMoney() {
        return dataProvider.getTotalMoney();
    }

    // Obtiene el número total de cuentas
    public int getTotalAccounts() {
        return (int) dataProvider.getTotalPlayers();
    }

    // Recarga la configuración desde el archivo
    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfiguration();

        // Recargar configuración del proveedor de datos si es necesario
        if (dataProvider instanceof MongoDBEconomyProvider) {
            mongoConnection.reloadConfiguration();
        }

        plugin.getLogger().info("Configuración de economía recargada.");
    }

    // Obtiene el proveedor de datos actual
    public EconomyDataProvider getDataProvider() {
        return dataProvider;
    }

    // Obtiene la conexión MongoDB (si está disponible)
    public MongoDBConnection getMongoConnection() {
        return mongoConnection;
    }

    // Verifica si está usando MongoDB
    public boolean isUsingMongoDB() {
        return dataProvider instanceof MongoDBEconomyProvider;
    }

    // Verifica si el proveedor de datos está disponible
    public boolean isDataProviderAvailable() {
        return dataProvider != null && dataProvider.isAvailable();
    }
}