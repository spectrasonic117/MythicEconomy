package com.spectrasonic.MythicEconomy.manager;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import com.spectrasonic.MythicEconomy.api.events.MoneyRemoveEvent;
import com.spectrasonic.MythicEconomy.database.EconomyDataProvider;
import com.spectrasonic.MythicEconomy.database.InternalEconomyProvider;
import com.spectrasonic.MythicEconomy.database.MongoDBConnection;
import com.spectrasonic.MythicEconomy.database.MongoDBEconomyProvider;
import com.spectrasonic.MythicEconomy.models.Currency;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class EconomyManager {

    private static EconomyManager instance;
    public final JavaPlugin plugin;
    private EconomyDataProvider dataProvider;
    private MongoDBConnection mongoConnection;
    private CurrencyManager currencyManager;

    // Configuración de respaldo para sistema interno
    public final Map<UUID, Double> playerBalances;
    public File dataFile;
    public FileConfiguration dataConfig;
    public double startingBalance;
    private String currencySymbol;
    private String currencyName;
    private String currencyNameSingular;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerBalances = new HashMap<>();

        // Inicializar configuración
        this.loadConfiguration();

        // Inicializar CurrencyManager
        this.currencyManager = new CurrencyManager(plugin);

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

    // ========== MÉTODOS DE COMPATIBILIDAD HACIA ATRÁS ==========
    // Estos métodos usan la moneda por defecto para mantener compatibilidad

    public double getBalance(Player player) {
        return getBalance(player, "default");
    }

    public void setBalance(Player player, double amount) {
        setBalance(player, amount, "default");
    }

    public boolean addMoney(Player player, double amount) {
        return addMoney(player, amount, "default");
    }

    public boolean removeMoney(Player player, double amount) {
        return removeMoney(player, amount, "default");
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        return hasEnoughMoney(player, amount, "default");
    }

    public String formatMoney(double amount) {
        return formatMoney(amount, "default");
    }

    // ========== MÉTODOS LEGACY PARA COMPATIBILIDAD ==========

    // Obtiene el símbolo de la moneda (para compatibilidad hacia atrás)
    public String getCurrencySymbol() {
        Currency defaultCurrency = currencyManager.getCurrency("default");
        return defaultCurrency != null ? defaultCurrency.getSymbol() : "$";
    }

    // Obtiene el nombre de la moneda (plural) (para compatibilidad hacia atrás)
    public String getCurrencyName() {
        Currency defaultCurrency = currencyManager.getCurrency("default");
        return defaultCurrency != null ? defaultCurrency.getName() : "monedas";
    }

    // Obtiene el nombre de la moneda (singular) (para compatibilidad hacia atrás)
    public String getCurrencyNameSingular() {
        Currency defaultCurrency = currencyManager.getCurrency("default");
        return defaultCurrency != null ? defaultCurrency.getNameSingular() : "moneda";
    }

    // Obtiene el saldo inicial para nuevos jugadores (para compatibilidad hacia
    // atrás)
    public double getStartingBalance() {
        Currency defaultCurrency = currencyManager.getCurrency("default");
        return defaultCurrency != null ? defaultCurrency.getStartingBalance() : 100.0;
    }

    // Establece el saldo inicial para nuevos jugadores (para compatibilidad hacia
    // atrás)
    public void setStartingBalance(double amount) {
        this.startingBalance = Math.max(0, amount);
        plugin.getConfig().set("economy.starting-balance", this.startingBalance);
        plugin.saveConfig();

        // También actualizar la moneda por defecto
        Currency defaultCurrency = currencyManager.getCurrency("default");
        if (defaultCurrency != null) {
            defaultCurrency.setStartingBalance(this.startingBalance);
            currencyManager.saveCurrency(defaultCurrency);
        }
    }

    // Obtiene el top de jugadores más ricos (para compatibilidad hacia atrás)
    public Map<String, Double> getTopBalances(int limit) {
        // Implementación básica - en el futuro debería usar agregaciones de MongoDB
        // Para múltiples monedas, esto obtiene el top de la moneda por defecto
        Map<String, Double> topBalances = new java.util.LinkedHashMap<>();

        // Por simplicidad, devolver un mapa vacío por ahora
        // Esta implementación necesitaría acceso a todos los jugadores y sus balances
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

    // ========== MÉTODOS PARA MÚLTIPLES MONEDAS ==========

    /**
     * Obtiene el balance de un jugador en una moneda específica
     */
    public double getBalance(Player player, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return 0.0;
        }

        // Por ahora, usar solo la moneda por defecto hasta que los proveedores soporten
        // múltiples monedas
        if (!currencyId.equals("default")) {
            return currency.getStartingBalance();
        }

        // Usar el proveedor de datos configurado para moneda por defecto
        double balance = dataProvider.getBalance(player.getUniqueId());

        // Si es MongoDB y el jugador no existe, crearlo
        if (dataProvider instanceof MongoDBEconomyProvider && balance == currency.getStartingBalance()) {
            dataProvider.createPlayer(player.getUniqueId());
        }

        return balance;
    }

    /**
     * Establece el balance de un jugador en una moneda específica
     */
    public void setBalance(Player player, double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return;
        }

        if (amount < 0) {
            amount = 0;
        }

        if (!currency.isValidAmount(amount)) {
            amount = currency.getMaxBalance();
        }

        // Por ahora, solo funciona con moneda por defecto hasta que los proveedores
        // soporten múltiples monedas
        if (currencyId.equals("default")) {
            dataProvider.setBalance(player.getUniqueId(), amount);
        }
    }

    /**
     * Agrega dinero al balance de un jugador en una moneda específica
     */
    public boolean addMoney(Player player, double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return false;
        }

        if (amount <= 0 || !currency.isValidTransferAmount(amount)) {
            return false;
        }

        double currentBalance = getBalance(player, currencyId);
        double newBalance = currentBalance + amount;

        if (!currency.isValidAmount(newBalance)) {
            return false;
        }

        // Disparar evento
        MoneyAddEvent event = new MoneyAddEvent(player, amount, currentBalance, newBalance);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Por ahora, solo funciona con moneda por defecto hasta que los proveedores
        // soporten múltiples monedas
        if (currencyId.equals("default")) {
            dataProvider.setBalance(player.getUniqueId(), newBalance);
        }
        return true;
    }

    /**
     * Quita dinero del balance de un jugador en una moneda específica
     */
    public boolean removeMoney(Player player, double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return false;
        }

        if (amount <= 0 || !currency.isValidTransferAmount(amount)) {
            return false;
        }

        double currentBalance = getBalance(player, currencyId);
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

        // Por ahora, solo funciona con moneda por defecto hasta que los proveedores
        // soporten múltiples monedas
        if (currencyId.equals("default")) {
            dataProvider.setBalance(player.getUniqueId(), newBalance);
        }
        return true;
    }

    /**
     * Verifica si un jugador tiene suficiente dinero en una moneda específica
     */
    public boolean hasEnoughMoney(Player player, double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return false;
        }

        // Por ahora, solo funciona con moneda por defecto hasta que los proveedores
        // soporten múltiples monedas
        if (!currencyId.equals("default")) {
            return getBalance(player, currencyId) >= amount;
        }

        return dataProvider.hasEnoughBalance(player.getUniqueId(), amount);
    }

    /**
     * Formatea una cantidad de dinero según la moneda especificada
     */
    public String formatMoney(double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null) {
            // Fallback para moneda desconocida
            return "$" + String.format("%.2f", amount);
        }

        return currency.formatMoney(amount);
    }

    /**
     * Obtiene una moneda por su ID
     */
    public Currency getCurrency(String currencyId) {
        return currencyManager.getCurrency(currencyId);
    }

    /**
     * Obtiene el CurrencyManager
     */
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    // ========== MÉTODOS EXISTENTES (ACTUALIZADOS) ==========

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