package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class MoneyRemoveEvent extends EconomyEvent implements Cancellable {

    private boolean cancelled = false;

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