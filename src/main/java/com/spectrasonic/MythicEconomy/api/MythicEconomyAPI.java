package com.spectrasonic.MythicEconomy.api;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import org.bukkit.entity.Player;

/**
 * API principal de MythicEconomy para uso en otros plugins
 * 
 * Esta clase proporciona métodos simples y fáciles de usar para interactuar
 * con el sistema de economía de MythicEconomy desde otros plugins.
 * 
 * @author Spectrasonic
 * @version 1.1.0
 */
public class MythicEconomyAPI {

    private static MythicEconomyAPI instance;
    private final EconomyManager economyManager;

    /**
     * Constructor interno - usar getInstance() para obtener la instancia
     */
    private MythicEconomyAPI() {
        this.economyManager = EconomyManager.getInstance();
        if (this.economyManager == null) {
            throw new IllegalStateException(
                    "MythicEconomy no está inicializado. Asegúrate de que el plugin esté cargado.");
        }
    }

    /**
     * Obtiene la instancia singleton de la API
     * 
     * @return Instancia de MythicEconomyAPI
     * @throws IllegalStateException si MythicEconomy no está inicializado
     */
    public static MythicEconomyAPI getInstance() {
        if (instance == null) {
            instance = new MythicEconomyAPI();
        }
        return instance;
    }

    /**
     * Verifica si MythicEconomy está disponible
     * 
     * @return true si MythicEconomy está disponible, false en caso contrario
     */
    public static boolean isAvailable() {
        try {
            return EconomyManager.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== MÉTODOS DE BALANCE ==========

    /**
     * Obtiene el balance de un jugador
     * 
     * @param player El jugador
     * @return El balance del jugador
     */
    public double getBalance(Player player) {
        return economyManager.getBalance(player);
    }

    /**
     * Establece el balance de un jugador
     * 
     * @param player El jugador
     * @param amount La cantidad a establecer (no puede ser negativa)
     * @return true si la operación fue exitosa
     */
    public boolean setBalance(Player player, double amount) {
        if (amount < 0) {
            return false;
        }
        economyManager.setBalance(player, amount);
        return true;
    }

    // ========== MÉTODOS DE AGREGAR DINERO ==========

    /**
     * Agrega dinero al balance de un jugador
     * 
     * @param player El jugador
     * @param amount La cantidad a agregar (debe ser positiva)
     * @return true si la operación fue exitosa, false si la cantidad es inválida
     */
    public boolean addMoney(Player player, double amount) {
        return economyManager.addMoney(player, amount);
    }

    /**
     * Agrega dinero al balance de un jugador con validación adicional
     * 
     * @param player        El jugador
     * @param amount        La cantidad a agregar
     * @param allowNegative Si se permite agregar cantidades negativas (equivale a
     *                      quitar)
     * @return true si la operación fue exitosa
     */
    public boolean addMoney(Player player, double amount, boolean allowNegative) {
        if (!allowNegative && amount < 0) {
            return false;
        }

        if (amount < 0) {
            return removeMoney(player, Math.abs(amount));
        }

        return economyManager.addMoney(player, amount);
    }

    // ========== MÉTODOS DE QUITAR DINERO ==========

    /**
     * Quita dinero del balance de un jugador
     * 
     * @param player El jugador
     * @param amount La cantidad a quitar (debe ser positiva)
     * @return true si la operación fue exitosa, false si no tiene suficiente dinero
     *         o la cantidad es inválida
     */
    public boolean removeMoney(Player player, double amount) {
        return economyManager.removeMoney(player, amount);
    }

    /**
     * Quita dinero del balance de un jugador con opción de forzar
     * 
     * @param player El jugador
     * @param amount La cantidad a quitar
     * @param force  Si es true, permite que el balance quede negativo
     * @return true si la operación fue exitosa
     */
    public boolean removeMoney(Player player, double amount, boolean force) {
        if (amount <= 0) {
            return false;
        }

        if (force) {
            double currentBalance = getBalance(player);
            setBalance(player, currentBalance - amount);
            return true;
        }

        return economyManager.removeMoney(player, amount);
    }

    // ========== MÉTODOS DE VERIFICACIÓN ==========

    /**
     * Verifica si un jugador tiene suficiente dinero
     * 
     * @param player El jugador
     * @param amount La cantidad a verificar
     * @return true si el jugador tiene suficiente dinero
     */
    public boolean hasEnoughMoney(Player player, double amount) {
        return economyManager.hasEnoughMoney(player, amount);
    }

    /**
     * Verifica si un jugador puede pagar una cantidad específica
     * Alias de hasEnoughMoney para mayor claridad
     * 
     * @param player El jugador
     * @param amount La cantidad a verificar
     * @return true si el jugador puede pagar la cantidad
     */
    public boolean canPay(Player player, double amount) {
        return hasEnoughMoney(player, amount);
    }

    // ========== MÉTODOS DE TRANSFERENCIA ==========

    /**
     * Transfiere dinero de un jugador a otro
     * 
     * @param from   El jugador que envía el dinero
     * @param to     El jugador que recibe el dinero
     * @param amount La cantidad a transferir
     * @return true si la transferencia fue exitosa
     */
    public boolean transferMoney(Player from, Player to, double amount) {
        if (amount <= 0) {
            return false;
        }

        if (!hasEnoughMoney(from, amount)) {
            return false;
        }

        if (removeMoney(from, amount)) {
            return addMoney(to, amount);
        }

        return false;
    }

    // ========== MÉTODOS DE FORMATO ==========

    /**
     * Formatea una cantidad de dinero con el símbolo de moneda
     * 
     * @param amount La cantidad a formatear
     * @return La cantidad formateada como string
     */
    public String formatMoney(double amount) {
        return economyManager.formatMoney(amount);
    }

    /**
     * Obtiene el símbolo de la moneda configurado
     * 
     * @return El símbolo de la moneda
     */
    public String getCurrencySymbol() {
        return economyManager.getCurrencySymbol();
    }

    /**
     * Obtiene el nombre de la moneda (plural)
     * 
     * @return El nombre de la moneda en plural
     */
    public String getCurrencyName() {
        return economyManager.getCurrencyName();
    }

    /**
     * Obtiene el nombre de la moneda (singular)
     * 
     * @return El nombre de la moneda en singular
     */
    public String getCurrencyNameSingular() {
        return economyManager.getCurrencyNameSingular();
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========

    /**
     * Obtiene el balance inicial para nuevos jugadores
     * 
     * @return El balance inicial
     */
    public double getStartingBalance() {
        return economyManager.getStartingBalance();
    }

    // ========== MÉTODOS DE ESTADÍSTICAS ==========

    /**
     * Obtiene el total de dinero en circulación en el servidor
     * 
     * @return El total de dinero en circulación
     */
    public double getTotalMoney() {
        return economyManager.getTotalMoney();
    }

    /**
     * Obtiene el número total de cuentas de jugadores
     * 
     * @return El número total de cuentas
     */
    public int getTotalAccounts() {
        return economyManager.getTotalAccounts();
    }
}