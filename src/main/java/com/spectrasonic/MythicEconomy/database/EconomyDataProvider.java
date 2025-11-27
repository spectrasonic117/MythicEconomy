package com.spectrasonic.MythicEconomy.database;

import java.util.UUID;
import java.util.Map;

// Interfaz para proveedores de datos de economía
// Permite cambiar entre diferentes sistemas de almacenamiento
// Soporta múltiples monedas con esquema de tabla única
// Además soporta almacenamiento y recuperación de nombres de jugadores para visualización web
public interface EconomyDataProvider {

    // ========== MÉTODOS BÁSICOS (para compatibilidad hacia atrás) ==========
    
    // Obtiene el saldo de un jugador (moneda por defecto)
    double getBalance(UUID playerUUID);

    // Establece el saldo de un jugador (moneda por defecto)
    void setBalance(UUID playerUUID, double amount);

    // Agrega dinero al saldo de un jugador (moneda por defecto)
    boolean addBalance(UUID playerUUID, double amount);

    // Reduce dinero del saldo de un jugador (moneda por defecto)
    boolean removeBalance(UUID playerUUID, double amount);

    // Verifica si un jugador tiene suficiente dinero (moneda por defecto)
    boolean hasEnoughBalance(UUID playerUUID, double amount);

    // Crea un jugador nuevo con saldo inicial (moneda por defecto)
    void createPlayer(UUID playerUUID);

    // ========== MÉTODOS PARA MÚLTIPLES MONEDAS ==========
    
    /**
     * Obtiene el saldo de un jugador en una moneda específica
     */
    double getBalance(UUID playerUUID, String currencyId);

    /**
     * Establece el saldo de un jugador en una moneda específica
     */
    void setBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Agrega dinero al saldo de un jugador en una moneda específica
     */
    boolean addBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Reduce dinero del saldo de un jugador en una moneda específica
     */
    boolean removeBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Verifica si un jugador tiene suficiente dinero en una moneda específica
     */
    boolean hasEnoughBalance(UUID playerUUID, double amount, String currencyId);

    /**
     * Crea un jugador nuevo con saldo inicial para una moneda específica
     */
    void createPlayer(UUID playerUUID, String currencyId);

    /**
     * Obtiene el número total de jugadores para una moneda específica
     */
    long getTotalPlayers(String currencyId);

    /**
     * Obtiene el dinero total en circulación para una moneda específica
     */
    double getTotalMoney(String currencyId);

    // ========== MÉTODOS DE ESTADÍSTICAS GENERALES ==========
    
    /**
     * Obtiene el número total de jugadores únicos (todas las monedas)
     */
    long getTotalUniquePlayers();

    /**
     * Obtiene el dinero total en circulación (todas las monedas)
     */
    double getTotalMoneyAllCurrencies();

    /**
     * Obtiene el top de jugadores más ricos para una moneda específica
     * @param currencyId ID de la moneda
     * @param limit Número máximo de jugadores a retornar
     * @return Array de arreglos [UUID, balance]
     */
    Object[][] getTopBalances(String currencyId, int limit);

    /**
     * Obtiene el top de jugadores más ricos para una moneda específica incluyendo nombres
     * @param currencyId ID de la moneda
     * @param limit Número máximo de jugadores a retornar
     * @return Array de arreglos [UUID, playerName, balance]
     */
    Object[][] getTopBalancesWithNames(String currencyId, int limit);

    // ========== MÉTODOS DE GESTIÓN DE NOMBRES DE JUGADORES ==========
    
    /**
     * Almacena o actualiza el nombre de un jugador asociado a su UUID
     * @param playerUUID UUID del jugador
     * @param playerName Nombre del jugador
     */
    void updatePlayerName(UUID playerUUID, String playerName);

    /**
     * Obtiene el nombre de un jugador por su UUID
     * @param playerUUID UUID del jugador
     * @return Nombre del jugador o null si no existe
     */
    String getPlayerName(UUID playerUUID);

    /**
     * Obtiene una lista de nombres de jugadores por sus UUIDs
     * @param playerUUIDs Lista de UUIDs de jugadores
     * @return Mapa de UUID -> nombre de jugador
     */
    Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs);

    /**
     * Sincroniza nombres de jugadores activos (útil para mantener nombres actualizados)
     * @param activePlayers Mapa de UUID -> nombre de jugadores actualmente activos
     */
    void syncPlayerNames(Map<UUID, String> activePlayers);

    // ========== MÉTODOS DE GESTIÓN ==========
    
    // Guarda los datos (si es necesario)
    void save();

    // Carga los datos (si es necesario)
    void load();

    // Verifica si el proveedor está disponible y funcionando
    boolean isAvailable();
}