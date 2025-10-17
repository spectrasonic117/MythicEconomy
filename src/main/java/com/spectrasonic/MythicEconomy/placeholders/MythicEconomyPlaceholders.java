package com.spectrasonic.MythicEconomy.placeholders;

import com.spectrasonic.MythicEconomy.Main;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class MythicEconomyPlaceholders extends PlaceholderExpansion {

    private final Main plugin;
    private final EconomyManager economyManager;
    private final CurrencyManager currencyManager;

    public MythicEconomyPlaceholders(Main plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.currencyManager = economyManager.getCurrencyManager();
    }

    @Override
    public String getIdentifier() {
        return "eco";
    }

    @Override
    public String getAuthor() {
        return "Spectrasonic";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Mantener la expansión cargada
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public int getUpdateInterval() {
        return 10; // Actualizar cada 10 ticks (0.5 segundos)
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        // Convertir a Player si está online
        Player onlinePlayer = player.getPlayer();

        switch (params.toLowerCase()) {
            // Placeholder principal del dinero
            case "money":
                if (onlinePlayer != null) {
                    return economyManager.formatMoney(economyManager.getBalance(onlinePlayer));
                }
                return "N/A";

            // Dinero sin formato (solo número)
            case "money_raw":
                if (onlinePlayer != null) {
                    return String.format("%.2f", economyManager.getBalance(onlinePlayer));
                }
                return "0.00";

            // Dinero formateado con separadores de miles
            case "money_formatted":
                if (onlinePlayer != null) {
                    double balance = economyManager.getBalance(onlinePlayer);
                    return economyManager.getCurrencySymbol() + String.format("%,.2f", balance);
                }
                return "N/A";

            // Dinero en formato corto (K, M, B)
            case "money_short":
                if (onlinePlayer != null) {
                    double balanceShort = economyManager.getBalance(onlinePlayer);
                    return economyManager.getCurrencySymbol() + formatShort(balanceShort);
                }
                return "N/A";

            // Símbolo de la moneda
            case "currency_symbol":
                return economyManager.getCurrencySymbol();

            // Nombre de la moneda (plural)
            case "currency_name":
                return economyManager.getCurrencyName();

            // Nombre de la moneda (singular)
            case "currency_name_singular":
                return economyManager.getCurrencyNameSingular();

            // Saldo inicial para nuevos jugadores
            case "starting_balance":
                return economyManager.formatMoney(economyManager.getStartingBalance());

            // Total de dinero en circulación
            case "total_money":
                return economyManager.formatMoney(economyManager.getTotalMoney());

            // Total de dinero en circulación (formato corto)
            case "total_money_short":
                return economyManager.getCurrencySymbol() + formatShort(economyManager.getTotalMoney());

            // Número total de cuentas
            case "total_accounts":
                return String.valueOf(economyManager.getTotalAccounts());

            // Posición en el ranking de dinero
            case "rank":
                if (onlinePlayer != null) {
                    return String.valueOf(getPlayerRank(onlinePlayer));
                }
                return "N/A";

            // Top 1 jugador más rico
            case "top_1_player":
                return getTopPlayer(1);

            // Top 1 dinero
            case "top_1_money":
                return getTopMoney(1);

            // Top 2 jugador más rico
            case "top_2_player":
                return getTopPlayer(2);

            // Top 2 dinero
            case "top_2_money":
                return getTopMoney(2);

            // Top 3 jugador más rico
            case "top_3_player":
                return getTopPlayer(3);

            // Top 3 dinero
            case "top_3_money":
                return getTopMoney(3);

            // Estado de Vault
            case "vault_enabled":
                return plugin.isVaultEnabled() ? "Habilitado" : "Deshabilitado";

            // ========== PLACEHOLDERS DINÁMICOS (DENTRO DEL DEFAULT) ==========

            default:
                // ========== PLACEHOLDERS PARA MÚLTIPLES MONEDAS ==========

                // Placeholders dinámicos para monedas específicas: %eco_<currency>_money%
                if (params.endsWith("_money") && params.length() > 6) {
                    try {
                        String currencyId = params.substring(0, params.length() - 6);
                        Currency currency = currencyManager.getCurrency(currencyId);

                        if (currency != null && currency.isEnabled() && onlinePlayer != null) {
                            double balance = economyManager.getBalance(onlinePlayer, currencyId);
                            return currency.formatMoney(balance);
                        }
                        return "N/A";
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholders dinámicos para monedas específicas (raw): %eco_<currency>_money_raw%
                if (params.endsWith("_money_raw") && params.length() > 10) {
                    try {
                        String currencyId = params.substring(0, params.length() - 10);
                        Currency currency = currencyManager.getCurrency(currencyId);

                        if (currency != null && currency.isEnabled() && onlinePlayer != null) {
                            double balance = economyManager.getBalance(onlinePlayer, currencyId);
                            return String.format(currency.isDecimal() ? "%.2f" : "%.0f", balance);
                        }
                        return "0.00";
                    } catch (Exception e) {
                        return "0.00";
                    }
                }

                // Placeholders dinámicos para símbolos de monedas: %eco_<currency>_symbol%
                if (params.endsWith("_symbol") && params.length() > 7) {
                    try {
                        String currencyId = params.substring(0, params.length() - 7);
                        Currency currency = currencyManager.getCurrency(currencyId);

                        if (currency != null) {
                            return currency.getSymbol();
                        }
                        return "$";
                    } catch (Exception e) {
                        return "$";
                    }
                }

                // Placeholders dinámicos para nombres de monedas: %eco_<currency>_name%
                if (params.endsWith("_name") && params.length() > 5) {
                    try {
                        String currencyId = params.substring(0, params.length() - 5);
                        Currency currency = currencyManager.getCurrency(currencyId);

                        if (currency != null) {
                            return currency.getName();
                        }
                        return "monedas";
                    } catch (Exception e) {
                        return "monedas";
                    }
                }

                // ========== PLACEHOLDERS LEGACY (COMPATIBILIDAD) ==========

                // Placeholder dinámico para verificar si puede pagar:
                // %eco_can_pay_<amount>%
                if (params.startsWith("can_pay_")) {
                    if (onlinePlayer != null) {
                        try {
                            double amount = Double.parseDouble(params.substring(8));
                            return economyManager.hasEnoughMoney(onlinePlayer, amount) ? "Sí" : "No";
                        } catch (NumberFormatException e) {
                            return "Error";
                        }
                    }
                    return "N/A";
                }

                // Placeholder dinámico para top players: %eco_top_<number>_player%
                if (params.startsWith("top_") && params.endsWith("_player")) {
                    try {
                        String numberStr = params.substring(4, params.length() - 7);
                        int position = Integer.parseInt(numberStr);
                        return getTopPlayer(position);
                    } catch (NumberFormatException e) {
                        return "N/A";
                    }
                }

                // Placeholder dinámico para top money: %eco_top_<number>_money%
                if (params.startsWith("top_") && params.endsWith("_money")) {
                    try {
                        String numberStr = params.substring(4, params.length() - 6);
                        int position = Integer.parseInt(numberStr);
                        return getTopMoney(position);
                    } catch (NumberFormatException e) {
                        return "N/A";
                    }
                }

                return null; // Placeholder no reconocido
        }
    }

    // Formatea un número en formato corto (K, M, B, T)
    private String formatShort(double amount) {
        if (amount >= 1_000_000_000_000L) {
            return String.format("%.1fT", amount / 1_000_000_000_000.0);
        } else if (amount >= 1_000_000_000) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        } else {
            return String.format("%.2f", amount);
        }
    }

    // Obtiene la posición del jugador en el ranking de dinero
    private int getPlayerRank(Player player) {
        Map<String, Double> topBalances = economyManager.getTopBalances(Integer.MAX_VALUE);
        int rank = 1;

        for (Map.Entry<String, Double> entry : topBalances.entrySet()) {
            if (entry.getKey().equals(player.getName())) {
                return rank;
            }
            rank++;
        }

        return rank;
    }

    // Obtiene el nombre del jugador en la posición especificada del top
    private String getTopPlayer(int position) {
        Map<String, Double> topBalances = economyManager.getTopBalances(position);

        if (topBalances.size() >= position) {
            return topBalances.keySet().toArray(new String[0])[position - 1];
        }

        return "N/A";
    }

    // Obtiene el dinero del jugador en la posición especificada del top
    private String getTopMoney(int position) {
        Map<String, Double> topBalances = economyManager.getTopBalances(position);

        if (topBalances.size() >= position) {
            Double amount = topBalances.values().toArray(new Double[0])[position - 1];
            return economyManager.formatMoney(amount);
        }

        return "N/A";
    }
}