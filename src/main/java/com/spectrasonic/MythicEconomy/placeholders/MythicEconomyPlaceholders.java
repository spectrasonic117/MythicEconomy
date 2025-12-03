package com.spectrasonic.MythicEconomy.placeholders;

import com.spectrasonic.MythicEconomy.Main;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.leaderboard.LeaderboardCache;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class MythicEconomyPlaceholders extends PlaceholderExpansion {

    private final Main plugin;
    private final EconomyManager economyManager;
    private final CurrencyManager currencyManager;
    private final LeaderboardCache leaderboardCache;

    public MythicEconomyPlaceholders(Main plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.currencyManager = economyManager.getCurrencyManager();
        this.leaderboardCache = new LeaderboardCache(plugin, 100, 20L); // 100 jugadores, 20 ticks (1 segundo)
        
        // Iniciar el sistema de cache
        this.leaderboardCache.start();
    }
    
    /**
     * Obtiene la instancia del LeaderboardCache para acceso externo
     * @return LeaderboardCache instance
     */
    public LeaderboardCache getLeaderboardCache() {
        return leaderboardCache;
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
    
    /**
     * Método para limpiar recursos antes de que el plugin se deshabilite
     */
    public void cleanup() {
        // Detener el sistema de cache al desregistrar
        if (leaderboardCache != null) {
            leaderboardCache.stop();
        }
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

                // ========== PLACEHOLDERS NUEVOS PARA MONEDAS ESPECÍFICAS ==========
                
                // Placeholder para obtener nombre del top 1 jugador de una moneda específica: %eco_<currency>_top_1_player%
                if (params.endsWith("_top_1_player") && params.length() > 14) {
                    try {
                        String currencyId = params.substring(0, params.length() - 14);
                        return getTopPlayerWithUUID(1, currencyId);
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener UUID del top 1 jugador de una moneda específica: %eco_<currency>_top_1_uuid%
                if (params.endsWith("_top_1_uuid") && params.length() > 13) {
                    try {
                        String currencyId = params.substring(0, params.length() - 13);
                        return getTopPlayerUUID(1, currencyId);
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener dinero del top 1 jugador de una moneda específica: %eco_<currency>_top_1_money%
                if (params.endsWith("_top_1_money") && params.length() > 13) {
                    try {
                        String currencyId = params.substring(0, params.length() - 13);
                        return getTopMoneyWithUUID(1, currencyId);
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener nombre del top N jugador de una moneda específica: %eco_<currency>_top_<N>_player%
                if (params.startsWith("top_") && params.contains("_") && params.endsWith("_player")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 4) {
                            String currencyId = params.substring(0, params.lastIndexOf("_top_"));
                            int position = Integer.parseInt(parts[1]);
                            return getTopPlayerWithUUID(position, currencyId);
                        }
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener UUID del top N jugador de una moneda específica: %eco_<currency>_top_<N>_uuid%
                if (params.startsWith("top_") && params.contains("_") && params.endsWith("_uuid")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 4) {
                            String currencyId = params.substring(0, params.lastIndexOf("_top_"));
                            int position = Integer.parseInt(parts[1]);
                            return getTopPlayerUUID(position, currencyId);
                        }
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener dinero del top N jugador de una moneda específica: %eco_<currency>_top_<N>_money%
                if (params.startsWith("top_") && params.contains("_") && params.endsWith("_money")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 4) {
                            String currencyId = params.substring(0, params.lastIndexOf("_top_"));
                            int position = Integer.parseInt(parts[1]);
                            return getTopMoneyWithUUID(position, currencyId);
                        }
                    } catch (Exception e) {
                        return "N/A";
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

                // ========== NUEVOS PLACEHOLDERS DE LEADERBOARD CON CACHE ==========
                
                // Placeholder para obtener nombre del jugador en posición N: %eco_<currency>_<N>_player%
                if (params.matches("^[a-zA-Z0-9_]+_\\d+_player$")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 3) {
                            // Reconstruir el ID de la moneda (puede contener guiones bajos)
                            StringBuilder currencyIdBuilder = new StringBuilder();
                            for (int i = 0; i < parts.length - 2; i++) {
                                if (i > 0) currencyIdBuilder.append("_");
                                currencyIdBuilder.append(parts[i]);
                            }
                            String currencyId = currencyIdBuilder.toString();
                            int position = Integer.parseInt(parts[parts.length - 2]);
                            
                            // Verificar que la moneda exista y esté habilitada
                            Currency currency = currencyManager.getCurrency(currencyId);
                            if (currency != null && currency.isEnabled()) {
                                return leaderboardCache.getPlayerName(currencyId, position);
                            }
                        }
                        return "N/A";
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener valor del jugador en posición N: %eco_<currency>_<N>_value%
                if (params.matches("^[a-zA-Z0-9_]+_\\d+_value$")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 3) {
                            // Reconstruir el ID de la moneda (puede contener guiones bajos)
                            StringBuilder currencyIdBuilder = new StringBuilder();
                            for (int i = 0; i < parts.length - 2; i++) {
                                if (i > 0) currencyIdBuilder.append("_");
                                currencyIdBuilder.append(parts[i]);
                            }
                            String currencyId = currencyIdBuilder.toString();
                            int position = Integer.parseInt(parts[parts.length - 2]);
                            
                            // Verificar que la moneda exista y esté habilitada
                            Currency currency = currencyManager.getCurrency(currencyId);
                            if (currency != null && currency.isEnabled()) {
                                return leaderboardCache.getPlayerBalance(currencyId, position);
                            }
                        }
                        return "N/A";
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener UUID del jugador en posición N: %eco_<currency>_<N>_uuid%
                if (params.matches("^[a-zA-Z0-9_]+_\\d+_uuid$")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 3) {
                            // Reconstruir el ID de la moneda (puede contener guiones bajos)
                            StringBuilder currencyIdBuilder = new StringBuilder();
                            for (int i = 0; i < parts.length - 2; i++) {
                                if (i > 0) currencyIdBuilder.append("_");
                                currencyIdBuilder.append(parts[i]);
                            }
                            String currencyId = currencyIdBuilder.toString();
                            int position = Integer.parseInt(parts[parts.length - 2]);
                            
                            // Verificar que la moneda exista y esté habilitada
                            Currency currency = currencyManager.getCurrency(currencyId);
                            if (currency != null && currency.isEnabled()) {
                                return leaderboardCache.getPlayerUuid(currencyId, position);
                            }
                        }
                        return "N/A";
                    } catch (Exception e) {
                        return "N/A";
                    }
                }

                // Placeholder para obtener valor sin formato del jugador en posición N: %eco_<currency>_<N>_value_raw%
                if (params.matches("^[a-zA-Z0-9_]+_\\d+_value_raw$")) {
                    try {
                        String[] parts = params.split("_");
                        if (parts.length >= 4) {
                            // Reconstruir el ID de la moneda (puede contener guiones bajos)
                            StringBuilder currencyIdBuilder = new StringBuilder();
                            for (int i = 0; i < parts.length - 3; i++) {
                                if (i > 0) currencyIdBuilder.append("_");
                                currencyIdBuilder.append(parts[i]);
                            }
                            String currencyId = currencyIdBuilder.toString();
                            int position = Integer.parseInt(parts[parts.length - 3]);
                            
                            // Verificar que la moneda exista y esté habilitada
                            Currency currency = currencyManager.getCurrency(currencyId);
                            if (currency != null && currency.isEnabled()) {
                                double balance = leaderboardCache.getPlayerBalanceRaw(currencyId, position);
                                return String.format(currency.isDecimal() ? "%.2f" : "%.0f", balance);
                            }
                        }
                        return "0.00";
                    } catch (Exception e) {
                        return "0.00";
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

    // ========== PLACEHOLDERS ACTUALIZADOS PARA SOPORTE DE NOMBRES ==========
    
    // Obtiene el nombre del jugador en la posición especificada del top usando el nuevo sistema de nombres
    private String getTopPlayerWithUUID(int position, String currencyId) {
        Object[][] topBalances;
        
        if ("default".equals(currencyId)) {
            topBalances = economyManager.getTopBalancesWithNames(position);
        } else {
            topBalances = economyManager.getTopBalancesWithNames(currencyId, position);
        }

        if (topBalances.length >= position) {
            // El nuevo formato es [UUID, playerName, balance]
            String playerName = (String) topBalances[position - 1][1];
            return playerName != null ? playerName : "Unknown";
        }

        return "N/A";
    }

    // Obtiene el UUID del jugador en la posición especificada del top
    private String getTopPlayerUUID(int position, String currencyId) {
        Object[][] topBalances;
        
        if ("default".equals(currencyId)) {
            topBalances = economyManager.getTopBalancesWithNames(position);
        } else {
            topBalances = economyManager.getTopBalancesWithNames(currencyId, position);
        }

        if (topBalances.length >= position) {
            // El nuevo formato es [UUID, playerName, balance]
            return (String) topBalances[position - 1][0];
        }

        return "N/A";
    }

    // Obtiene el dinero del jugador en la posición especificada del top usando el nuevo sistema
    private String getTopMoneyWithUUID(int position, String currencyId) {
        Object[][] topBalances;
        
        if ("default".equals(currencyId)) {
            topBalances = economyManager.getTopBalancesWithNames(position);
        } else {
            topBalances = economyManager.getTopBalancesWithNames(currencyId, position);
        }

        if (topBalances.length >= position) {
            // El nuevo formato es [UUID, playerName, balance]
            Double amount = (Double) topBalances[position - 1][2];
            return economyManager.formatMoney(amount, currencyId);
        }

        return "N/A";
    }
}