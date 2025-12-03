package com.spectrasonic.MythicEconomy.leaderboard;

import com.spectrasonic.MythicEconomy.Main;
import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema de cache para leaderboards de economía que se actualiza automáticamente cada segundo
 * Proporciona acceso rápido a los datos de ranking sin consultar directamente la base de datos
 */
public class LeaderboardCache {
    
    private final Main plugin;
    private final EconomyManager economyManager;
    private final CurrencyManager currencyManager;
    
    // Cache principal: currencyId -> posición -> [playerName, balance]
    private final Map<String, Map<Integer, LeaderboardEntry>> leaderboardCache;
    
    // Cache para nombres de jugadores: UUID -> playerName
    private final Map<String, String> playerNameCache;
    
    // Tarea programada para actualización automática
    private BukkitTask updateTask;
    
    // Configuración
    private final int cacheSize; // Cuántos jugadores mantener en cache por moneda
    private final long updateIntervalTicks; // Intervalo de actualización en ticks
    
    // Para detectar nuevas monedas
    private Set<String> lastKnownCurrencies;
    
    @Getter
    private boolean isRunning = false;
    
    public LeaderboardCache(Main plugin, int cacheSize, long updateIntervalTicks) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.currencyManager = economyManager.getCurrencyManager();
        this.cacheSize = cacheSize;
        this.updateIntervalTicks = updateIntervalTicks;
        
        // Usar ConcurrentHashMap para seguridad en hilos
        this.leaderboardCache = new ConcurrentHashMap<>();
        this.playerNameCache = new ConcurrentHashMap<>();
        this.lastKnownCurrencies = ConcurrentHashMap.newKeySet();
    }
    
    /**
     * Inicia el sistema de cache y la actualización automática
     */
    public void start() {
        if (isRunning) {
            return;
        }

        // Carga inicial de datos de forma síncrona para evitar problemas durante startup
        refreshAllLeaderboardsSynchronously();

        // Programar actualización automática
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            plugin,
            this::refreshAllLeaderboards,
            updateIntervalTicks,
            updateIntervalTicks
        );

        isRunning = true;
        plugin.getLogger().info("LeaderboardCache iniciado - Actualizando cada " + (updateIntervalTicks / 20) + " segundos");
    }
    
    /**
     * Detiene el sistema de cache
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        isRunning = false;
        plugin.getLogger().info("LeaderboardCache detenido");
    }
    
    /**
     * Refresca todos los leaderboards de forma síncrona (para inicialización)
     */
    private void refreshAllLeaderboardsSynchronously() {
        try {
            // Obtener monedas actuales habilitadas
            Set<String> currentCurrencies = ConcurrentHashMap.newKeySet();
            for (Currency currency : currencyManager.getEnabledCurrencies()) {
                currentCurrencies.add(currency.getId());
            }

            // Detectar nuevas monedas
            for (String currencyId : currentCurrencies) {
                if (!lastKnownCurrencies.contains(currencyId)) {
                    plugin.getLogger().info("Nueva moneda detectada en leaderboard: " + currencyId);
                }
                refreshLeaderboardSynchronously(currencyId);
            }

            // Limpiar cache de monedas que ya no existen
            for (String oldCurrency : lastKnownCurrencies) {
                if (!currentCurrencies.contains(oldCurrency)) {
                    leaderboardCache.remove(oldCurrency);
                    plugin.getLogger().info("Moneda removida del leaderboard: " + oldCurrency);
                }
            }

            // Actualizar lista de monedas conocidas
            lastKnownCurrencies.clear();
            lastKnownCurrencies.addAll(currentCurrencies);

        } catch (Exception e) {
            plugin.getLogger().warning("Error al actualizar leaderboards: " + e.getMessage());
        }
    }

    /**
     * Refresca el leaderboard para una moneda específica de forma síncrona
     */
    private void refreshLeaderboardSynchronously(String currencyId) {
        try {
            // Obtener top jugadores desde la base de datos de forma síncrona
            Object[][] topBalances = economyManager.getTopBalancesWithNames(currencyId, cacheSize);

            // Crear mapa para esta moneda
            Map<Integer, LeaderboardEntry> currencyLeaderboard = new HashMap<>();

            // Procesar resultados
            for (int i = 0; i < topBalances.length; i++) {
                Object[] entry = topBalances[i];
                if (entry.length >= 3) {
                    String playerUuid = (String) entry[0];
                    String playerName = (String) entry[1];
                    Double balance = (Double) entry[2];

                    // Guardar en cache
                    LeaderboardEntry leaderboardEntry = new LeaderboardEntry(playerUuid, playerName, balance);
                    currencyLeaderboard.put(i + 1, leaderboardEntry); // Posición empieza en 1

                    // Actualizar cache de nombres
                    if (playerUuid != null && playerName != null) {
                        playerNameCache.put(playerUuid, playerName);
                    }
                }
            }

            // Actualizar cache principal
            leaderboardCache.put(currencyId, currencyLeaderboard);

        } catch (Exception e) {
            plugin.getLogger().warning("Error al refrescar leaderboard para " + currencyId + ": " + e.getMessage());
        }
    }

    /**
     * Refresca todos los leaderboards para todas las monedas habilitadas
     * Detecta automáticamente nuevas monedas y las agrega al sistema
     */
    private void refreshAllLeaderboards() {
        try {
            // Obtener monedas actuales habilitadas
            Set<String> currentCurrencies = ConcurrentHashMap.newKeySet();
            for (Currency currency : currencyManager.getEnabledCurrencies()) {
                currentCurrencies.add(currency.getId());
            }
            
            // Detectar nuevas monedas
            for (String currencyId : currentCurrencies) {
                if (!lastKnownCurrencies.contains(currencyId)) {
                    plugin.getLogger().info("Nueva moneda detectada en leaderboard: " + currencyId);
                }
                refreshLeaderboard(currencyId);
            }
            
            // Limpiar cache de monedas que ya no existen
            for (String oldCurrency : lastKnownCurrencies) {
                if (!currentCurrencies.contains(oldCurrency)) {
                    leaderboardCache.remove(oldCurrency);
                    plugin.getLogger().info("Moneda removida del leaderboard: " + oldCurrency);
                }
            }
            
            // Actualizar lista de monedas conocidas
            lastKnownCurrencies.clear();
            lastKnownCurrencies.addAll(currentCurrencies);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error al actualizar leaderboards: " + e.getMessage());
        }
    }
    
    /**
     * Refresca el leaderboard para una moneda específica
     * @param currencyId ID de la moneda
     */
    public void refreshLeaderboard(String currencyId) {
        try {
            // Obtener top jugadores desde la base de datos
            Object[][] topBalances = economyManager.getTopBalancesWithNames(currencyId, cacheSize);
            
            // Crear mapa para esta moneda
            Map<Integer, LeaderboardEntry> currencyLeaderboard = new HashMap<>();
            
            // Procesar resultados
            for (int i = 0; i < topBalances.length; i++) {
                Object[] entry = topBalances[i];
                if (entry.length >= 3) {
                    String playerUuid = (String) entry[0];
                    String playerName = (String) entry[1];
                    Double balance = (Double) entry[2];
                    
                    // Guardar en cache
                    LeaderboardEntry leaderboardEntry = new LeaderboardEntry(playerUuid, playerName, balance);
                    currencyLeaderboard.put(i + 1, leaderboardEntry); // Posición empieza en 1
                    
                    // Actualizar cache de nombres
                    if (playerUuid != null && playerName != null) {
                        playerNameCache.put(playerUuid, playerName);
                    }
                }
            }
            
            // Actualizar cache principal
            leaderboardCache.put(currencyId, currencyLeaderboard);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error al refrescar leaderboard para " + currencyId + ": " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el nombre del jugador en una posición específica del leaderboard
     * @param currencyId ID de la moneda
     * @param position Posición en el ranking (1-based)
     * @return Nombre del jugador o "N/A" si no existe
     */
    public String getPlayerName(String currencyId, int position) {
        Map<Integer, LeaderboardEntry> currencyLeaderboard = leaderboardCache.get(currencyId);
        if (currencyLeaderboard == null) {
            return "N/A";
        }
        
        LeaderboardEntry entry = currencyLeaderboard.get(position);
        return entry != null ? entry.getPlayerName() : "N/A";
    }
    
    /**
     * Obtiene el balance del jugador en una posición específica del leaderboard
     * @param currencyId ID de la moneda
     * @param position Posición en el ranking (1-based)
     * @return Balance formateado o "N/A" si no existe
     */
    public String getPlayerBalance(String currencyId, int position) {
        Map<Integer, LeaderboardEntry> currencyLeaderboard = leaderboardCache.get(currencyId);
        if (currencyLeaderboard == null) {
            return "N/A";
        }
        
        LeaderboardEntry entry = currencyLeaderboard.get(position);
        if (entry == null) {
            return "N/A";
        }
        
        // Formatear según la moneda
        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency != null) {
            return currency.formatMoney(entry.getBalance());
        }
        
        // Formato por defecto
        return String.format("%.2f", entry.getBalance());
    }
    
    /**
     * Obtiene el balance sin formato del jugador en una posición específica
     * @param currencyId ID de la moneda
     * @param position Posición en el ranking (1-based)
     * @return Balance como número o 0.0 si no existe
     */
    public double getPlayerBalanceRaw(String currencyId, int position) {
        Map<Integer, LeaderboardEntry> currencyLeaderboard = leaderboardCache.get(currencyId);
        if (currencyLeaderboard == null) {
            return 0.0;
        }
        
        LeaderboardEntry entry = currencyLeaderboard.get(position);
        return entry != null ? entry.getBalance() : 0.0;
    }
    
    /**
     * Obtiene el UUID del jugador en una posición específica
     * @param currencyId ID de la moneda
     * @param position Posición en el ranking (1-based)
     * @return UUID del jugador o null si no existe
     */
    public String getPlayerUuid(String currencyId, int position) {
        Map<Integer, LeaderboardEntry> currencyLeaderboard = leaderboardCache.get(currencyId);
        if (currencyLeaderboard == null) {
            return null;
        }
        
        LeaderboardEntry entry = currencyLeaderboard.get(position);
        return entry != null ? entry.getPlayerUuid() : null;
    }
    
    /**
     * Verifica si una moneda tiene datos en cache
     * @param currencyId ID de la moneda
     * @return true si hay datos disponibles
     */
    public boolean hasCurrencyData(String currencyId) {
        Map<Integer, LeaderboardEntry> currencyLeaderboard = leaderboardCache.get(currencyId);
        return currencyLeaderboard != null && !currencyLeaderboard.isEmpty();
    }
    
    /**
     * Obtiene estadísticas del cache
     * @return String con información del estado actual
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("LeaderboardCache Stats:\n");
        stats.append("- Monedas en cache: ").append(leaderboardCache.size()).append("\n");
        stats.append("- Nombres en cache: ").append(playerNameCache.size()).append("\n");
        stats.append("- Tamaño por moneda: ").append(cacheSize).append("\n");
        stats.append("- Intervalo actualización: ").append(updateIntervalTicks / 20).append(" segundos\n");
        stats.append("- Estado: ").append(isRunning ? "Activo" : "Inactivo");
        
        return stats.toString();
    }
    
    /**
     * Limpia toda la cache
     */
    public void clearCache() {
        leaderboardCache.clear();
        playerNameCache.clear();
        plugin.getLogger().info("Cache de leaderboards limpiado");
    }
    
    /**
     * Clase interna para representar una entrada del leaderboard
     */
    @Getter
    public static class LeaderboardEntry {
        private final String playerUuid;
        private final String playerName;
        private final double balance;
        
        public LeaderboardEntry(String playerUuid, String playerName, double balance) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.balance = balance;
        }
    }
}