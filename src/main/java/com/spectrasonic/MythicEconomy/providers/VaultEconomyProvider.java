package com.spectrasonic.MythicEconomy.providers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;

import java.util.List;
import java.util.ArrayList;

/**
 * Proveedor de Vault Economy que permite que otros plugins usen MythicEconomy
 * a través de la API estándar de Vault
 */
public class VaultEconomyProvider implements Economy {

    private final EconomyManager economyManager;
    private final String currencyName = "Monedas";
    private final String currencyNameSingular = "Moneda";

    public VaultEconomyProvider(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean isEnabled() {
        return economyManager != null;
    }

    @Override
    public String getName() {
        return "MythicEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false; // No implementamos sistema de bancos por ahora
    }

    @Override
    public int fractionalDigits() {
        return 2; // Dos decimales para las monedas
    }

    @Override
    public String format(double amount) {
        return economyManager.formatMoney(amount);
    }

    @Override
    public String currencyNamePlural() {
        return currencyName;
    }

    @Override
    public String currencyNameSingular() {
        return currencyNameSingular;
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return player != null && player.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null && player.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName); // No manejamos economías por mundo
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player); // No manejamos economías por mundo
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player != null && player.isOnline()) {
            return economyManager.getBalance(player.getPlayer());
        }
        return 0.0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player != null && player.isOnline()) {
            return economyManager.getBalance(player.getPlayer());
        }
        return 0.0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName); // No manejamos economías por mundo
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player); // No manejamos economías por mundo
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount); // No manejamos economías por mundo
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount); // No manejamos economías por mundo
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null || !player.isOnline()) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Jugador no encontrado o no está online.");
        }

        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "No se puede retirar una cantidad negativa.");
        }

        Player onlinePlayer = player.getPlayer();
        double currentBalance = economyManager.getBalance(onlinePlayer);

        if (currentBalance < amount) {
            return new EconomyResponse(amount, currentBalance, ResponseType.FAILURE, "Fondos insuficientes.");
        }

        if (economyManager.removeMoney(onlinePlayer, amount)) {
            double newBalance = economyManager.getBalance(onlinePlayer);
            return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(amount, currentBalance, ResponseType.FAILURE,
                    "Error al procesar la transacción.");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount); // No manejamos economías por mundo
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount); // No manejamos economías por mundo
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null || !player.isOnline()) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Jugador no encontrado o no está online.");
        }

        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "No se puede depositar una cantidad negativa.");
        }

        Player onlinePlayer = player.getPlayer();
        double currentBalance = economyManager.getBalance(onlinePlayer);

        if (economyManager.addMoney(onlinePlayer, amount)) {
            double newBalance = economyManager.getBalance(onlinePlayer);
            return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(amount, currentBalance, ResponseType.FAILURE,
                    "Error al procesar la transacción.");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount); // No manejamos economías por mundo
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount); // No manejamos economías por mundo
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player != null && player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            // Si el jugador no tiene saldo registrado, establecer saldo inicial
            if (economyManager.getBalance(onlinePlayer) == 0.0) {
                economyManager.setBalance(onlinePlayer, 100.0); // Saldo inicial por defecto
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName); // No manejamos economías por mundo
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player); // No manejamos economías por mundo
    }

    // Métodos de banco - No implementados
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "MythicEconomy no soporta bancos.");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>(); // No hay bancos
    }
}