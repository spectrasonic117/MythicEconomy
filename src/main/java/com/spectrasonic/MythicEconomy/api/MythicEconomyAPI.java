package com.spectrasonic.MythicEconomy.api;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import org.bukkit.entity.Player;

public class MythicEconomyAPI {

    private static MythicEconomyAPI instance;
    private final EconomyManager economyManager;

    // Constructor interno - usar getInstance() para obtener la instancia
    private MythicEconomyAPI() {
        this.economyManager = EconomyManager.getInstance();
        if (this.economyManager == null) {
            throw new IllegalStateException(
                    "MythicEconomy no está inicializado. Asegúrate de que el plugin esté cargado.");
        }
    }

    public static MythicEconomyAPI getInstance() {
        if (instance == null) {
            instance = new MythicEconomyAPI();
        }
        return instance;
    }

    public static boolean isAvailable() {
        try {
            return EconomyManager.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== MÉTODOS DE BALANCE ==========

    public double getBalance(Player player) {
        return economyManager.getBalance(player);
    }

    public boolean setBalance(Player player, double amount) {
        if (amount < 0) {
            return false;
        }
        economyManager.setBalance(player, amount);
        return true;
    }

    // ========== MÉTODOS DE AGREGAR DINERO ==========

    public boolean addMoney(Player player, double amount) {
        return economyManager.addMoney(player, amount);
    }

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

    public boolean removeMoney(Player player, double amount) {
        return economyManager.removeMoney(player, amount);
    }

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

    public boolean hasEnoughMoney(Player player, double amount) {
        return economyManager.hasEnoughMoney(player, amount);
    }

    public boolean canPay(Player player, double amount) {
        return hasEnoughMoney(player, amount);
    }

    // ========== MÉTODOS DE TRANSFERENCIA ==========

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

    public String formatMoney(double amount) {
        return economyManager.formatMoney(amount);
    }

    public String getCurrencySymbol() {
        return economyManager.getCurrencySymbol();
    }

    public String getCurrencyName() {
        return economyManager.getCurrencyName();
    }

    public String getCurrencyNameSingular() {
        return economyManager.getCurrencyNameSingular();
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========

    public double getStartingBalance() {
        return economyManager.getStartingBalance();
    }

    // ========== MÉTODOS DE ESTADÍSTICAS ==========

    public double getTotalMoney() {
        return economyManager.getTotalMoney();
    }

    public int getTotalAccounts() {
        return economyManager.getTotalAccounts();
    }

    // ========== MÉTODOS PARA MÚLTIPLES MONEDAS ==========

    public double getBalance(Player player, String currencyId) {
        return economyManager.getBalance(player, currencyId);
    }

    public boolean setBalance(Player player, double amount, String currencyId) {
        if (amount < 0) {
            return false;
        }
        economyManager.setBalance(player, amount, currencyId);
        return true;
    }

    public boolean addMoney(Player player, double amount, String currencyId) {
        return economyManager.addMoney(player, amount, currencyId);
    }

    public boolean removeMoney(Player player, double amount, String currencyId) {
        return economyManager.removeMoney(player, amount, currencyId);
    }

    public boolean hasEnoughMoney(Player player, double amount, String currencyId) {
        return economyManager.hasEnoughMoney(player, amount, currencyId);
    }

    public boolean transferMoney(Player from, Player to, double amount, String currencyId) {
        if (amount <= 0) {
            return false;
        }

        if (!hasEnoughMoney(from, amount, currencyId)) {
            return false;
        }

        if (removeMoney(from, amount, currencyId)) {
            return addMoney(to, amount, currencyId);
        }

        return false;
    }

    public String formatMoney(double amount, String currencyId) {
        return economyManager.formatMoney(amount, currencyId);
    }

    public Currency getCurrency(String currencyId) {
        return economyManager.getCurrency(currencyId);
    }

    public java.util.Collection<Currency> getEnabledCurrencies() {
        return economyManager.getCurrencyManager().getEnabledCurrencies();
    }

    public java.util.Set<String> getCurrencyIds() {
        return economyManager.getCurrencyManager().getCurrencyIds();
    }

    public boolean currencyExists(String currencyId) {
        return economyManager.getCurrencyManager().currencyExists(currencyId);
    }
}