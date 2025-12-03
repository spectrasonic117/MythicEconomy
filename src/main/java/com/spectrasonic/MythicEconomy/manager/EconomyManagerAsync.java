package com.spectrasonic.MythicEconomy.manager;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import com.spectrasonic.MythicEconomy.api.events.MoneyAddEvent;
import com.spectrasonic.MythicEconomy.api.events.MoneyRemoveEvent;
import com.spectrasonic.MythicEconomy.database.MySQLAsyncConnection;
import com.spectrasonic.MythicEconomy.database.MySQLEconomyProviderAsync;
import com.spectrasonic.MythicEconomy.models.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

/**
 * Versión asíncrona del EconomyManager para operaciones no bloqueantes.
 * Basado en PaperMC recomendaciones para async database operations.
 */
@Slf4j
public class EconomyManagerAsync {

    private static EconomyManagerAsync instance;
    private final JavaPlugin plugin;
    private MySQLEconomyProviderAsync asyncDataProvider;
    private MySQLAsyncConnection asyncConnection;
    private CurrencyManager currencyManager;
    private final Map<UUID, Double> fallbackBalances;
    private double startingBalance;
    private boolean useAsyncMode;

    public EconomyManagerAsync(JavaPlugin plugin) {
        this.plugin = plugin;
        this.fallbackBalances = new HashMap<>();
        this.loadConfiguration();
        this.currencyManager = new CurrencyManager(plugin);
        this.initializeAsyncDataProvider();
        instance = this;

        MessageUtils.sendConsoleMessage("<green>Sistema de economía asíncrono MythicEconomy inicializado.</green>");
    }

    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        this.startingBalance = config.getDouble("economy.starting-balance", 100.0);
        this.useAsyncMode = config.getBoolean("database.async-mode", true);

        log.info("Configuración asíncrona cargada - Async Mode: {}, Starting Balance: {}",
                useAsyncMode, startingBalance);
    }

    private void initializeAsyncDataProvider() {
        FileConfiguration config = plugin.getConfig();
        boolean useExternalDB = config.getBoolean("database.use-external-database", false);
        String databaseType = config.getString("database.type", "FILE");

        if (useAsyncMode && useExternalDB && "MYSQL".equalsIgnoreCase(databaseType)) {
            // Inicializar conexión MySQL asíncrona con HikariCP
            this.asyncConnection = new MySQLAsyncConnection(plugin);

            asyncConnection.initialize()
                    .thenAccept(success -> {
                        if (success) {
                            this.asyncDataProvider = new MySQLEconomyProviderAsync(plugin, asyncConnection);
                            log.info("MySQL Async Provider inicializado exitosamente");
                            MessageUtils.sendConsoleMessage("<green>MySQL Async Provider listo</green>");
                        } else {
                            log.error("Falla al inicializar MySQL Async Provider");
                            fallbackToSyncMode();
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("Excepción al inicializar MySQL Async Provider", throwable);
                        fallbackToSyncMode();
                        return null;
                    });
        } else {
            log.warn("Modo asíncrono desactivado o base de datos no compatible");
            fallbackToSyncMode();
        }
    }

    private void fallbackToSyncMode() {
        log.warn("Cayendo a modo síncrono");
        this.useAsyncMode = false;
        // Aquí podrías inicializar un proveedor síncrono como fallback
    }

    public static EconomyManagerAsync getInstance() {
        return instance;
    }

    /**
     * Obtiene el balance de un jugador de forma asíncrona
     */
    public CompletableFuture<Double> getBalanceAsync(Player player) {
        return getBalanceAsync(player, "default");
    }

    /**
     * Obtiene el balance de un jugador en una moneda específica de forma asíncrona
     */
    public CompletableFuture<Double> getBalanceAsync(Player player, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            // Fallback síncrono
            return CompletableFuture.completedFuture(getBalanceSync(player, currencyId));
        }

        return asyncDataProvider.getBalanceAsync(player.getUniqueId(), currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al obtener balance para {} en moneda {}", player.getName(), currencyId, throwable);
                    return 0.0;
                });
    }

    /**
     * Establece el balance de un jugador de forma asíncrona
     */
    public CompletableFuture<Boolean> setBalanceAsync(Player player, double amount) {
        return setBalanceAsync(player, amount, "default");
    }

    /**
     * Establece el balance de un jugador en una moneda específica de forma
     * asíncrona
     */
    public CompletableFuture<Boolean> setBalanceAsync(Player player, double amount, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            setBalanceSync(player, amount, currencyId);
            return CompletableFuture.completedFuture(true);
        }

        if (amount < 0)
            amount = 0;

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency != null && !currency.isValidAmount(amount)) {
            amount = currency.getMaxBalance();
        }

        return asyncDataProvider.setBalanceAsync(player.getUniqueId(), amount, currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al establecer balance para {} en moneda {}", player.getName(), currencyId,
                            throwable);
                    return false;
                });
    }

    /**
     * Agrega dinero al balance de un jugador de forma asíncrona
     */
    public CompletableFuture<Boolean> addMoneyAsync(Player player, double amount) {
        return addMoneyAsync(player, amount, "default");
    }

    /**
     * Agrega dinero al balance de un jugador en una moneda específica de forma
     * asíncrona
     */
    public CompletableFuture<Boolean> addMoneyAsync(Player player, double amount, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            boolean result = addMoneySync(player, amount, currencyId);
            return CompletableFuture.completedFuture(result);
        }

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled() || amount <= 0) {
            return CompletableFuture.completedFuture(false);
        }

        if (!currency.isValidTransferAmount(amount)) {
            return CompletableFuture.completedFuture(false);
        }

        return getBalanceAsync(player, currencyId)
                .thenCompose(currentBalance -> {
                    double newBalance = currentBalance + amount;

                    if (!currency.isValidAmount(newBalance)) {
                        return CompletableFuture.completedFuture(false);
                    }

                    // Disparar evento de forma síncrona (necesario)
                    MoneyAddEvent event = new MoneyAddEvent(player, amount, currentBalance, newBalance);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return CompletableFuture.completedFuture(false);
                    }

                    // Actualizar balance de forma asíncrona
                    return asyncDataProvider.addBalanceAsync(player.getUniqueId(), amount, currencyId)
                            .exceptionally(throwable -> {
                                log.error("Error al agregar dinero para {} en moneda {}", player.getName(), currencyId,
                                        throwable);
                                return false;
                            });
                });
    }

    /**
     * Quita dinero del balance de un jugador de forma asíncrona
     */
    public CompletableFuture<Boolean> removeMoneyAsync(Player player, double amount) {
        return removeMoneyAsync(player, amount, "default");
    }

    /**
     * Quita dinero del balance de un jugador en una moneda específica de forma
     * asíncrona
     */
    public CompletableFuture<Boolean> removeMoneyAsync(Player player, double amount, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            boolean result = removeMoneySync(player, amount, currencyId);
            return CompletableFuture.completedFuture(result);
        }

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled() || amount <= 0) {
            return CompletableFuture.completedFuture(false);
        }

        return getBalanceAsync(player, currencyId)
                .thenCompose(currentBalance -> {
                    if (currentBalance < amount) {
                        return CompletableFuture.completedFuture(false);
                    }

                    double newBalance = currentBalance - amount;

                    // Disparar evento de forma síncrona
                    MoneyRemoveEvent event = new MoneyRemoveEvent(player, amount, currentBalance, newBalance);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return CompletableFuture.completedFuture(false);
                    }

                    // Actualizar balance de forma asíncrona
                    return asyncDataProvider.removeBalanceAsync(player.getUniqueId(), amount, currencyId)
                            .exceptionally(throwable -> {
                                log.error("Error al remover dinero para {} en moneda {}", player.getName(), currencyId,
                                        throwable);
                                return false;
                            });
                });
    }

    /**
     * Verifica si un jugador tiene suficiente dinero de forma asíncrona
     */
    public CompletableFuture<Boolean> hasEnoughMoneyAsync(Player player, double amount) {
        return hasEnoughMoneyAsync(player, amount, "default");
    }

    /**
     * Verifica si un jugador tiene suficiente dinero en una moneda específica de
     * forma asíncrona
     */
    public CompletableFuture<Boolean> hasEnoughMoneyAsync(Player player, double amount, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            boolean result = hasEnoughMoneySync(player, amount, currencyId);
            return CompletableFuture.completedFuture(result);
        }

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return CompletableFuture.completedFuture(false);
        }

        return asyncDataProvider.hasEnoughBalanceAsync(player.getUniqueId(), amount, currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al verificar saldo para {} en moneda {}", player.getName(), currencyId, throwable);
                    return false;
                });
    }

    /**
     * Crea un jugador nuevo de forma asíncrona
     */
    public CompletableFuture<Void> createPlayerAsync(Player player) {
        return createPlayerAsync(player, "default");
    }

    /**
     * Crea un jugador nuevo en una moneda específica de forma asíncrona
     */
    public CompletableFuture<Void> createPlayerAsync(Player player, String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            createPlayerSync(player, currencyId);
            return CompletableFuture.completedFuture(null);
        }

        return asyncDataProvider.createPlayerAsync(player.getUniqueId(), currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al crear jugador {} para moneda {}", player.getName(), currencyId, throwable);
                    return null;
                });
    }

    /**
     * Obtiene estadísticas de forma asíncrona
     */
    public CompletableFuture<Long> getTotalPlayersAsync(String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            return CompletableFuture.completedFuture((long) getTotalPlayersSync(currencyId));
        }

        return asyncDataProvider.getTotalPlayersAsync(currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al obtener total de jugadores para moneda {}", currencyId, throwable);
                    return 0L;
                });
    }

    public CompletableFuture<Double> getTotalMoneyAsync(String currencyId) {
        if (!useAsyncMode || asyncDataProvider == null) {
            return CompletableFuture.completedFuture(getTotalMoneySync(currencyId));
        }

        return asyncDataProvider.getTotalMoneyAsync(currencyId)
                .exceptionally(throwable -> {
                    log.error("Error al obtener dinero total para moneda {}", currencyId, throwable);
                    return 0.0;
                });
    }

    public CompletableFuture<Object[][]> getTopBalancesAsync(String currencyId, int limit) {
        if (!useAsyncMode || asyncDataProvider == null) {
            return CompletableFuture.completedFuture(getTopBalancesSync(currencyId, limit));
        }

        return asyncDataProvider.getTopBalancesAsync(currencyId, limit)
                .exceptionally(throwable -> {
                    log.error("Error al obtener top balances para moneda {}", currencyId, throwable);
                    return new Object[0][0];
                });
    }

    /**
     * Actualiza el nombre de un jugador de forma asíncrona
     */
    public CompletableFuture<Void> updatePlayerNameAsync(UUID playerUUID, String playerName) {
        if (!useAsyncMode || asyncDataProvider == null) {
            // Fallback síncrono
            return CompletableFuture.runAsync(() -> {
                try {
                    // Implementación de fallback síncrona si es necesario
                } catch (Exception e) {
                    log.error("Error en fallback updatePlayerName", e);
                }
            }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
        }

        return asyncDataProvider.updatePlayerNameAsync(playerUUID, playerName)
                .exceptionally(throwable -> {
                    log.error("Error al actualizar nombre de jugador {}", playerUUID, throwable);
                    return null;
                });
    }

    /**
     * Fallback métodos síncronos para compatibilidad
     */
    private double getBalanceSync(Player player, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            return 0.0;
        }
        return fallbackBalances.getOrDefault(player.getUniqueId(), currency.getStartingBalance());
    }

    private void setBalanceSync(Player player, double amount, String currencyId) {
        if (amount < 0)
            amount = 0;
        fallbackBalances.put(player.getUniqueId(), amount);
    }

    private boolean addMoneySync(Player player, double amount, String currencyId) {
        if (amount <= 0)
            return false;
        UUID uuid = player.getUniqueId();
        double current = fallbackBalances.getOrDefault(uuid, startingBalance);
        fallbackBalances.put(uuid, current + amount);
        return true;
    }

    private boolean removeMoneySync(Player player, double amount, String currencyId) {
        if (amount <= 0)
            return false;
        UUID uuid = player.getUniqueId();
        double current = fallbackBalances.getOrDefault(uuid, startingBalance);
        if (current < amount)
            return false;
        fallbackBalances.put(uuid, current - amount);
        return true;
    }

    private boolean hasEnoughMoneySync(Player player, double amount, String currencyId) {
        return getBalanceSync(player, currencyId) >= amount;
    }

    private void createPlayerSync(Player player, String currencyId) {
        Currency currency = currencyManager.getCurrency(currencyId);
        double starting = currency != null ? currency.getStartingBalance() : startingBalance;
        fallbackBalances.put(player.getUniqueId(), starting);
    }

    private int getTotalPlayersSync(String currencyId) {
        return fallbackBalances.size();
    }

    private double getTotalMoneySync(String currencyId) {
        return fallbackBalances.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private Object[][] getTopBalancesSync(String currencyId, int limit) {
        // Implementación básica para fallback
        return new Object[0][0];
    }

    /**
     * Verifica si el modo asíncrono está activo
     */
    public boolean isAsyncMode() {
        return useAsyncMode && asyncDataProvider != null;
    }

    /**
     * Obtiene el proveedor de datos asíncrono
     */
    public MySQLEconomyProviderAsync getAsyncDataProvider() {
        return asyncDataProvider;
    }

    /**
     * Obtiene la instancia del plugin
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Obtiene el CurrencyManager
     */
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    /**
     * Cierra el sistema asíncrono
     */
    public CompletableFuture<Void> shutdown() {
        if (asyncDataProvider != null) {
            return asyncDataProvider.shutdown()
                    .exceptionally(throwable -> {
                        log.error("Error al cerrar AsyncDataProvider", throwable);
                        return null;
                    });
        }
        return CompletableFuture.completedFuture(null);
    }
}