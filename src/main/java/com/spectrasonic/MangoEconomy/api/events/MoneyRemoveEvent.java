package com.spectrasonic.MangoEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Evento que se dispara cuando se quita dinero a un jugador
 * 
 * @author Spectrasonic
 * @version 1.1.0
 */
public class MoneyRemoveEvent extends EconomyEvent implements Cancellable {
    
    private boolean cancelled = false;
    
    /**
     * Constructor del evento de quitar dinero
     * 
     * @param player El jugador al que se le quita dinero
     * @param amount La cantidad que se quita
     * @param oldBalance El balance anterior del jugador
     * @param newBalance El nuevo balance del jugador
     */
    public MoneyRemoveEvent(Player player, double amount, double oldBalance, double newBalance) {
        super(player, amount, oldBalance, newBalance);
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}