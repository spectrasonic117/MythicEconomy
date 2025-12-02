package com.spectrasonic.MythicEconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;

import lombok.RequiredArgsConstructor;

/**
 * Listener para manejar eventos de entrada de jugadores
 * Asegura que los jugadores se creen correctamente en la base de datos
 */
@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final EconomyManager economyManager;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Ejecutar de forma asíncrona para no bloquear el hilo principal
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(economyManager.getPlugin(), () -> {
            try {
                // Actualizar el nombre del jugador en la base de datos primero
                economyManager.updatePlayerName(player.getUniqueId(), player.getName());
                
                // Solo si usa base de datos externa, asegurarse de que el jugador exista
                if (economyManager.isUsingMySQL()) {
                    // Usar el método optimizado para MySQL
                    com.spectrasonic.MythicEconomy.database.MySQLEconomyProvider mysqlProvider =
                        (com.spectrasonic.MythicEconomy.database.MySQLEconomyProvider) economyManager.getDataProvider();
                    mysqlProvider.ensurePlayerExists(player.getUniqueId());
                } else if (economyManager.isUsingMongoDB()) {
                    // Para MongoDB, crear el jugador para todas las monedas habilitadas
                    CurrencyManager currencyManager = economyManager.getCurrencyManager();
                    for (Currency currency : currencyManager.getEnabledCurrencies()) {
                        economyManager.getDataProvider().createPlayer(player.getUniqueId(), currency.getId());
                    }
                }
                
            } catch (Exception e) {
                economyManager.getPlugin().getLogger().warning("Error al procesar entrada del jugador " + player.getName() + ": " + e.getMessage());
            }
        });
    }
}