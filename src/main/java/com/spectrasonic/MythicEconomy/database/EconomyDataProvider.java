package com.spectrasonic.MythicEconomy.database;

import java.util.UUID;

/**
 * Interfaz para proveedores de datos de economía
 * Permite cambiar entre diferentes sistemas de almacenamiento
 */
public interface EconomyDataProvider {

    /**
     * Obtiene el saldo de un jugador
     */
    double getBalance(UUID playerUUID);

    /**
     * Establece el saldo de un jugador
     */
    void setBalance(UUID playerUUID, double amount);

    /**
     * Agrega dinero al saldo de un jugador
     */
    boolean addBalance(UUID playerUUID, double amount);

    /**
     * Reduce dinero del saldo de un jugador
     */
    boolean removeBalance(UUID playerUUID, double amount);

    /**
     * Verifica si un jugador tiene suficiente dinero
     */
    boolean hasEnoughBalance(UUID playerUUID, double amount);

    /**
     * Crea un jugador nuevo con saldo inicial
     */
    void createPlayer(UUID playerUUID);

    /**
     * Obtiene el número total de jugadores registrados
     */
    long getTotalPlayers();

    /**
     * Obtiene el dinero total en circulación
     */
    double getTotalMoney();

    /**
     * Guarda los datos (si es necesario)
     */
    void save();

    /**
     * Carga los datos (si es necesario)
     */
    void load();

    /**
     * Verifica si el proveedor está disponible y funcionando
     */
    boolean isAvailable();
}