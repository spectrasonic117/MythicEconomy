package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Evento base para todos los eventos de economía de MythicEconomy
 * 
 * @author Spectrasonic
 * @version 1.1.0
 */
public abstract class EconomyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    protected final Player player;
    protected final double amount;
    protected final double oldBalance;
    protected final double newBalance;

    /**
     * Constructor del evento de economía
     * 
     * @param player     El jugador involucrado en la transacción
     * @param amount     La cantidad de la transacción
     * @param oldBalance El balance anterior del jugador
     * @param newBalance El nuevo balance del jugador
     */
    public EconomyEvent(Player player, double amount, double oldBalance, double newBalance) {
        this.player = player;
        this.amount = amount;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    /**
     * Obtiene el jugador involucrado en la transacción
     * 
     * @return El jugador
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Obtiene la cantidad de la transacción
     * 
     * @return La cantidad
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Obtiene el balance anterior del jugador
     * 
     * @return El balance anterior
     */
    public double getOldBalance() {
        return oldBalance;
    }

    /**
     * Obtiene el nuevo balance del jugador
     * 
     * @return El nuevo balance
     */
    public double getNewBalance() {
        return newBalance;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}