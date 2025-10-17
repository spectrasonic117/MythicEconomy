package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class EconomyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    protected final Player player;
    protected final double amount;
    protected final double oldBalance;
    protected final double newBalance;

    public EconomyEvent(Player player, double amount, double oldBalance, double newBalance) {
        this.player = player;
        this.amount = amount;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public double getOldBalance() {
        return oldBalance;
    }

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