package com.spectrasonic.MythicEconomy.manager;

import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class CurrencyManager {

    private static CurrencyManager instance;
    private final JavaPlugin plugin;
    private final Map<String, Currency> currencies;
    private final File currenciesDirectory;
    private Currency defaultCurrency;

    public CurrencyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currencies = new HashMap<>();
        this.currenciesDirectory = new File(plugin.getDataFolder(), "currencies");

        // Crear directorio si no existe
        if (!currenciesDirectory.exists()) {
            currenciesDirectory.mkdirs();
        }

        this.loadCurrencies();
        instance = this;

        MessageUtils.sendConsoleMessage("<green>Sistema de múltiples monedas inicializado correctamente.");
    }

    public static CurrencyManager getInstance() {
        return instance;
    }

    // Carga todas las monedas desde sus archivos de configuración
    private void loadCurrencies() {
        // Crear moneda por defecto si no existe
        createDefaultCurrencyIfNotExists();

        // Cargar moneda por defecto desde configuración principal
        loadDefaultCurrency();

        // Cargar otras monedas desde el directorio currencies/
        loadCustomCurrencies();
    }

    // Crea la moneda por defecto si no existe configuración
    private void createDefaultCurrencyIfNotExists() {
        File defaultConfig = new File(currenciesDirectory, "default.yml");
        if (!defaultConfig.exists()) {
            Currency defaultCurrency = new Currency(
                    "default",
                    "monedas",
                    "moneda",
                    "$",
                    true);

            saveCurrency(defaultCurrency);
        }
    }

    // Carga la moneda por defecto desde configuración principal
    private void loadDefaultCurrency() {
        FileConfiguration config = plugin.getConfig();

        String symbol = config.getString("economy.currency.symbol", "$");
        String name = config.getString("economy.currency.name", "monedas");
        String nameSingular = config.getString("economy.currency.name-singular", "moneda");
        boolean decimal = config.getBoolean("economy.currency.decimals", true); // Cambiado de "decimal" a "decimals"
        double startingBalance = config.getDouble("economy.starting-balance", 100.0);

        Currency currency = new Currency(
                "default",
                name,
                nameSingular,
                symbol,
                decimal);
        currency.setStartingBalance(startingBalance);

        currencies.put("default", currency);
        this.defaultCurrency = currency;
    }

    // Carga monedas personalizadas desde el directorio currencies/
    private void loadCustomCurrencies() {
        File[] currencyFiles = currenciesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));

        if (currencyFiles != null) {
            for (File file : currencyFiles) {
                if (!file.getName().equals("default.yml")) {
                    loadCurrencyFromFile(file);
                }
            }
        }
    }

    // Carga una moneda desde su archivo de configuración
    private void loadCurrencyFromFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String id = config.getString("id", file.getName().replace(".yml", ""));
        String name = config.getString("name", "monedas");
        String nameSingular = config.getString("name-singular", "moneda");
        String symbol = config.getString("symbol", "$");
        boolean decimal = config.getBoolean("decimal", true);
        double startingBalance = config.getDouble("starting-balance", 100.0);
        double maxBalance = config.getDouble("max-balance", 999999999.99);
        double minTransfer = config.getDouble("min-transfer", 0.01);
        double maxTransfer = config.getDouble("max-transfer", 100000.0);
        boolean enabled = config.getBoolean("enabled", true);

        Currency currency = new Currency(id, name, nameSingular, symbol, decimal);
        currency.setStartingBalance(startingBalance);
        currency.setMaxBalance(maxBalance);
        currency.setMinTransfer(minTransfer);
        currency.setMaxTransfer(maxTransfer);
        currency.setEnabled(enabled);

        currencies.put(id, currency);

        plugin.getLogger().info("Moneda cargada: " + id + " (" + name + ")");
    }

    // Guarda una moneda en su archivo de configuración
    public void saveCurrency(Currency currency) {
        File file = new File(currenciesDirectory, currency.getId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("id", currency.getId());
        config.set("name", currency.getName());
        config.set("name-singular", currency.getNameSingular());
        config.set("symbol", currency.getSymbol());
        config.set("decimal", currency.isDecimal());
        config.set("starting-balance", currency.getStartingBalance());
        config.set("max-balance", currency.getMaxBalance());
        config.set("min-transfer", currency.getMinTransfer());
        config.set("max-transfer", currency.getMaxTransfer());
        config.set("enabled", currency.isEnabled());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar moneda " + currency.getId() + ": " + e.getMessage());
        }
    }

    // Agrega una nueva moneda
    public boolean addCurrency(Currency currency) {
        if (currencies.containsKey(currency.getId())) {
            return false;
        }

        currencies.put(currency.getId(), currency);
        saveCurrency(currency);

        MessageUtils.sendConsoleMessage("<green>Moneda agregada: " + currency.getId() + "</green>");
        return true;
    }

    // Remueve una moneda
    public boolean removeCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId) || currencyId.equals("default")) {
            return false;
        }

        Currency currency = currencies.remove(currencyId);
        File file = new File(currenciesDirectory, currencyId + ".yml");

        if (file.exists()) {
            file.delete();
        }

        MessageUtils.sendConsoleMessage("<green>Moneda removida: " + currencyId + "</green>");
        return true;
    }

    // Obtiene una moneda por ID
    public Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }

    // Obtiene la moneda por defecto
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    // Obtiene todas las monedas habilitadas
    public Collection<Currency> getEnabledCurrencies() {
        return currencies.values().stream()
                .filter(Currency::isEnabled)
                .toList();
    }

    // Obtiene todos los IDs de monedas
    public Set<String> getCurrencyIds() {
        return currencies.keySet();
    }

    // Verifica si existe una moneda
    public boolean currencyExists(String currencyId) {
        return currencies.containsKey(currencyId);
    }

    // Obtiene la moneda por defecto del servidor (para compatibilidad hacia atrás)
    public Currency getServerDefaultCurrency() {
        return defaultCurrency;
    }

    // Recarga todas las monedas
    public void reloadCurrencies() {
        currencies.clear();
        loadCurrencies();
        MessageUtils.sendConsoleMessage("<green>Monedas recargadas correctamente.</green>");
    }

    // Obtiene el número total de monedas
    public int getTotalCurrencies() {
        return currencies.size();
    }

    // Obtiene el número de monedas habilitadas
    public int getEnabledCurrenciesCount() {
        return (int) currencies.values().stream().filter(Currency::isEnabled).count();
    }
}