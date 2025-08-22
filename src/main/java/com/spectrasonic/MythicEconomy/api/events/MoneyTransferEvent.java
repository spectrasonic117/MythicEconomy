package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento que se dispara cuando se transfiere dinero entre jugadores
 * 
 * @author Spectrasonic
 * @version 1.1.0
 */
public class MoneyTransferEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player from;
    private final Player to;
    private final double amount;

    /**
     * Constructor del evento de transferencia de dinero
     * 
     * @param from   El jugador que envía el dinero
     * @param to     El jugador que recibe el dinero
     * @param amount La cantidad transferida
     */
    public MoneyTransferEvent(Player from, Player to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    /**
     * Obtiene el jugador que envía el dinero
     * 
     * @return El jugador remitente
     */
    public Player getFrom() {
        return from;
    }

    /**
     * Obtiene el jugador que recibe el dinero
     * 
     * @return El jugador receptor
     */
    public Player getTo() {
        return to;
    }

    /**
     * Obtiene la cantidad transferida
     * 
     * @return La cantidad
     */
    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}