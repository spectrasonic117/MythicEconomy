package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Evento que se dispara cuando se agrega dinero a un jugador
 * 
 * @author Spectrasonic
 * @version 1.1.0
 */

public class MoneyAddEvent extends EconomyEvent implements Cancellable {

    private boolean cancelled = false;

    /**
     * Constructor del evento de agregar dinero
     * 
     * @param player     El jugador al que se le agrega dinero
     * @param amount     La cantidad que se agrega
     * @param oldBalance El balance anterior del jugador
     * @param newBalance El nuevo balance del jugador
     */
    public MoneyAddEvent(Player player, double amount, double oldBalance, double newBalance) {
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