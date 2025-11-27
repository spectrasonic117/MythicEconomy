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
import com.spectrasonic.MythicEconomy.database.MySQLConnection;
import com.spectrasonic.MythicEconomy.database.MySQLEconomyProvider;
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
    private MySQLConnection mysqlConnection;
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
            // Cargar datos de múltiples monedas
            dataProvider.load();
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
        } else if (useExternalDB && "MYSQL".equalsIgnoreCase(databaseType)) {
            // Usar MySQL
            this.mysqlConnection = new MySQLConnection(plugin);

            if (mysqlConnection.connect()) {
                this.dataProvider = new MySQLEconomyProvider(plugin, mysqlConnection);
                plugin.getLogger().info("Usando MySQL como proveedor de datos de economía");
            } else {
                // Fallback al sistema interno si MySQL falla
                plugin.getLogger().warning("No se pudo conectar a MySQL, usando sistema interno");
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
        // Para compatibilidad hacia atrás, cargar el formato antiguo
        if (dataConfig.getConfigurationSection("players") != null) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                double balance = dataConfig.getDouble("players." + uuidString + ".balance", 0.0);
                playerBalances.put(uuid, balance);
            }
        }
        // La carga de múltiples monedas se hace ahora en InternalEconomyProvider.load()
    }

    public void savePlayerData() {
        // Usar el proveedor de datos para guardar
        dataProvider.save();

        // Si es el sistema interno, también guardar el respaldo (solo para compatibilidad)
        if (dataProvider instanceof InternalEconomyProvider) {
            // El InternalEconomyProvider ya maneja el guardado de múltiples monedas
            // Solo mantener compatibilidad con el formato antiguo si es necesario
            // (El código de guardado está ahora en InternalEconomyProvider.save())
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

    // Obtiene el total de dinero en circulación (moneda por defecto para compatibilidad)
    public double getTotalMoney() {
        return dataProvider.getTotalMoney("default");
    }

    // Obtiene el número total de cuentas (moneda por defecto para compatibilidad)
    public int getTotalAccounts() {
        return (int) dataProvider.getTotalPlayers("default");
    }

    // Recarga la configuración desde el archivo
    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfiguration();

        // Recargar configuración del proveedor de datos si es necesario
        if (dataProvider instanceof MongoDBEconomyProvider) {
            mongoConnection.reloadConfiguration();
        } else if (dataProvider instanceof MySQLEconomyProvider) {
            mysqlConnection.reloadConfiguration();
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

        // Usar el proveedor de datos configurado con soporte para múltiples monedas
        double balance;
        if (dataProvider instanceof InternalEconomyProvider) {
            balance = ((InternalEconomyProvider) dataProvider).getBalance(player.getUniqueId(), currencyId);
        } else if (dataProvider instanceof MongoDBEconomyProvider) {
            balance = ((MongoDBEconomyProvider) dataProvider).getBalance(player.getUniqueId(), currencyId);
        } else {
            // Fallback para otros proveedores
            balance = dataProvider.getBalance(player.getUniqueId());
        }

        // Si es MongoDB y el jugador no existe, crearlo
        if (dataProvider instanceof MongoDBEconomyProvider && balance == currency.getStartingBalance()) {
            if (dataProvider instanceof MongoDBEconomyProvider) {
                ((MongoDBEconomyProvider) dataProvider).createPlayer(player.getUniqueId(), currencyId);
            } else {
                dataProvider.createPlayer(player.getUniqueId());
            }
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

        // Usar el proveedor de datos configurado con soporte para múltiples monedas
        if (dataProvider instanceof InternalEconomyProvider) {
            ((InternalEconomyProvider) dataProvider).setBalance(player.getUniqueId(), amount, currencyId);
        } else if (dataProvider instanceof MongoDBEconomyProvider) {
            ((MongoDBEconomyProvider) dataProvider).setBalance(player.getUniqueId(), amount, currencyId);
        } else {
            // Fallback para otros proveedores - solo funciona con default
            if (currencyId.equals("default")) {
                dataProvider.setBalance(player.getUniqueId(), amount);
            }
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

        // Usar el proveedor de datos configurado con soporte para múltiples monedas
        if (dataProvider instanceof InternalEconomyProvider) {
            return ((InternalEconomyProvider) dataProvider).addBalance(player.getUniqueId(), amount, currencyId);
        } else if (dataProvider instanceof MongoDBEconomyProvider) {
            return ((MongoDBEconomyProvider) dataProvider).addBalance(player.getUniqueId(), amount, currencyId);
        } else {
            // Fallback para otros proveedores - solo funciona con default
            if (currencyId.equals("default")) {
                return dataProvider.addBalance(player.getUniqueId(), amount);
            }
            return false;
        }
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

        // Usar el proveedor de datos configurado con soporte para múltiples monedas
        if (dataProvider instanceof InternalEconomyProvider) {
            return ((InternalEconomyProvider) dataProvider).removeBalance(player.getUniqueId(), amount, currencyId);
        } else if (dataProvider instanceof MongoDBEconomyProvider) {
            return ((MongoDBEconomyProvider) dataProvider).removeBalance(player.getUniqueId(), amount, currencyId);
        } else {
            // Fallback para otros proveedores - solo funciona con default
            if (currencyId.equals("default")) {
                return dataProvider.removeBalance(player.getUniqueId(), amount);
            }
            return false;
        }
    }

    /**
     * Verifica si un jugador tiene suficiente dinero en una moneda específica
     */
    public boolean hasEnoughMoney(Player player, double amount, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return false;
        }

        // Usar el proveedor de datos configurado con soporte para múltiples monedas
        if (dataProvider instanceof InternalEconomyProvider) {
            return ((InternalEconomyProvider) dataProvider).hasEnoughBalance(player.getUniqueId(), amount, currencyId);
        } else if (dataProvider instanceof MongoDBEconomyProvider) {
            return ((MongoDBEconomyProvider) dataProvider).hasEnoughBalance(player.getUniqueId(), amount, currencyId);
        } else {
            // Fallback para otros proveedores - solo funciona con default
            if (currencyId.equals("default")) {
                return dataProvider.hasEnoughBalance(player.getUniqueId(), amount);
            }
            return false;
        }
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

    // Obtiene la conexión MySQL (si está disponible)
    public MySQLConnection getMySQLConnection() {
        return mysqlConnection;
    }

    // Verifica si está usando MySQL
    public boolean isUsingMySQL() {
        return dataProvider instanceof MySQLEconomyProvider;
    }
    // ========== MÉTODOS NUEVOS PARA SOPORTE DE NOMBRES DE JUGADORES ==========

    /**
     * Actualiza el nombre de un jugador en el proveedor de datos
     */
    public void updatePlayerName(UUID playerUUID, String playerName) {
        if (dataProvider != null) {
            dataProvider.updatePlayerName(playerUUID, playerName);
        }
    }

    /**
     * Obtiene el nombre de un jugador por su UUID desde el proveedor de datos
     */
    public String getPlayerName(UUID playerUUID) {
        if (dataProvider != null) {
            return dataProvider.getPlayerName(playerUUID);
        }
        return null;
    }

    /**
     * Obtiene nombres de jugadores por sus UUIDs desde el proveedor de datos
     */
    public Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs) {
        if (dataProvider != null) {
            return dataProvider.getPlayerNames(playerUUIDs);
        }
        return new java.util.HashMap<>();
    }

    /**
     * Sincroniza nombres de jugadores activos con el proveedor de datos
     */
    public void syncPlayerNames(Map<UUID, String> activePlayers) {
        if (dataProvider != null) {
            dataProvider.syncPlayerNames(activePlayers);
        }
    }

    /**
     * Obtiene el top de balances con nombres para una moneda específica
     */
    public Object[][] getTopBalancesWithNames(String currencyId, int limit) {
        if (dataProvider != null) {
            return dataProvider.getTopBalancesWithNames(currencyId, limit);
        }
        return new Object[0][0];
    }

    /**
     * Obtiene el top de balances con nombres usando la moneda por defecto (para compatibilidad)
     */
    public Object[][] getTopBalancesWithNames(int limit) {
        return getTopBalancesWithNames("default", limit);
    }

    /**
     * Resuelve el nombre de un jugador usando el API de Bukkit si no está en la base de datos
     */
    public String resolvePlayerName(UUID playerUUID) {
        // Primero intentar obtener de la base de datos
        String name = getPlayerName(playerUUID);
        if (name != null && !name.isEmpty()) {
            return name;
        }

        // Si no está en la base de datos, intentar obtener del servidor
        org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer != null && offlinePlayer.getName() != null) {
            String playerName = offlinePlayer.getName();
            // Guardar el nombre en la base de datos para futuras consultas
            updatePlayerName(playerUUID, playerName);
            return playerName;
        }

        return "Unknown";
    }

    /**
     * Resuelve nombres de múltiples jugadores usando el API de Bukkit
     */
    public Map<UUID, String> resolvePlayerNames(Iterable<UUID> playerUUIDs) {
        Map<UUID, String> resolvedNames = new java.util.HashMap<>();
        
        // Obtener nombres existentes de la base de datos
        Map<UUID, String> existingNames = getPlayerNames(playerUUIDs);
        
        // Para los que no están en la base de datos, intentar resolver del servidor
        for (UUID uuid : playerUUIDs) {
            String name = existingNames.get(uuid);
            if (name == null || name.isEmpty()) {
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer != null && offlinePlayer.getName() != null) {
                    name = offlinePlayer.getName();
                    // Guardar en la base de datos para futuras consultas
                    updatePlayerName(uuid, name);
                } else {
                    name = "Unknown";
                }
            }
            resolvedNames.put(uuid, name);
        }
        
        return resolvedNames;
    }
}