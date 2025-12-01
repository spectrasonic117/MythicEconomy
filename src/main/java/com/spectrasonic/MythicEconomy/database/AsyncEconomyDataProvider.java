package com.spectrasonic.MythicEconomy.database;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz asíncrona para proveedores de datos de economía.
 * Permite operaciones no bloqueantes para alta concurrencia.
 */
public interface AsyncEconomyDataProvider {

    // ========== MÉTODOS BÁSICOS ASÍNCRONOS ==========

    /**
     * Obtiene el saldo de un jugador (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Double> getBalance(UUID playerUUID);

    /**
     * Obtiene el saldo de un jugador en una moneda específica de forma asíncrona
     */
    CompletableFuture<Double> getBalance(UUID playerUUID, String currencyId);

    /**
     * Establece el saldo de un jugador (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Boolean> setBalance(UUID playerUUID, double amount);

    /**
     * Establece el saldo de un jugador en una moneda específica de forma asíncrona
     */
    CompletableFuture<Boolean> setBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Agrega dinero al saldo de un jugador (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Boolean> addBalance(UUID playerUUID, double amount);

    /**
     * Agrega dinero al saldo de un jugador en una moneda específica de forma asíncrona
     */
    CompletableFuture<Boolean> addBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Reduce dinero del saldo de un jugador (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Boolean> removeBalance(UUID playerUUID, double amount);

    /**
     * Reduce dinero del saldo de un jugador en una moneda específica de forma asíncrona
     */
    CompletableFuture<Boolean> removeBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Verifica si un jugador tiene suficiente dinero (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Boolean> hasEnoughBalance(UUID playerUUID, double amount);

    /**
     * Verifica si un jugador tiene suficiente dinero en una moneda específica de forma asíncrona
     */
    CompletableFuture<Boolean> hasEnoughBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Crea un jugador nuevo con saldo inicial (moneda por defecto) de forma asíncrona
     */
    CompletableFuture<Void> createPlayer(UUID playerUUID);

    /**
     * Crea un jugador nuevo con saldo inicial para una moneda específica de forma asíncrona
     */
    CompletableFuture<Void> createPlayer(UUID playerUUID, String currencyId);

    // ========== MÉTODOS DE ESTADÍSTICAS ASÍNCRONOS ==========

    /**
     * Obtiene el número total de jugadores para una moneda específica de forma asíncrona
     */
    CompletableFuture<Long> getTotalPlayers(String currencyId);

    /**
     * Obtiene el dinero total en circulación para una moneda específica de forma asíncrona
     */
    CompletableFuture<Double> getTotalMoney(String currencyId);

    /**
     * Obtiene el número total de jugadores únicos (todas las monedas) de forma asíncrona
     */
    CompletableFuture<Long> getTotalUniquePlayers();

    /**
     * Obtiene el dinero total en circulación (todas las monedas) de forma asíncrona
     */
    CompletableFuture<Double> getTotalMoneyAllCurrencies();

    /**
     * Obtiene el top de jugadores más ricos para una moneda específica de forma asíncrona
     * @param currencyId ID de la moneda
     * @param limit Número máximo de jugadores a retornar
     * @return CompletableFuture con array de arreglos [UUID, balance]
     */
    CompletableFuture<Object[][]> getTopBalances(String currencyId, int limit);

    /**
     * Obtiene el top de jugadores más ricos para una moneda específica incluyendo nombres de forma asíncrona
     * @param currencyId ID de la moneda
     * @param limit Número máximo de jugadores a retornar
     * @return CompletableFuture con array de arreglos [UUID, playerName, balance]
     */
    CompletableFuture<Object[][]> getTopBalancesWithNames(String currencyId, int limit);

    // ========== MÉTODOS DE GESTIÓN DE NOMBRES DE JUGADORES ASÍNCRONOS ==========

    /**
     * Almacena o actualiza el nombre de un jugador asociado a su UUID de forma asíncrona
     */
    CompletableFuture<Void> updatePlayerName(UUID playerUUID, String playerName);

    /**
     * Obtiene el nombre de un jugador por su UUID de forma asíncrona
     */
    CompletableFuture<String> getPlayerName(UUID playerUUID);

    /**
     * Obtiene una lista de nombres de jugadores por sus UUIDs de forma asíncrona
     */
    CompletableFuture<Map<UUID, String>> getPlayerNames(Iterable<UUID> playerUUIDs);

    /**
     * Sincroniza nombres de jugadores activos de forma asíncrona
     */
    CompletableFuture<Void> syncPlayerNames(Map<UUID, String> activePlayers);

    // ========== MÉTODOS DE GESTIÓN ==========

    /**
     * Inicializa el proveedor de forma asíncrona
     */
    CompletableFuture<Boolean> initialize();

    /**
     * Cierra el proveedor de forma asíncrona
     */
    CompletableFuture<Void> shutdown();

    /**
     * Verifica si el proveedor está disponible y funcionando
     */
    boolean isAvailable();
}