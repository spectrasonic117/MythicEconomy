package com.spectrasonic.MythicEconomy.api.events;

import com.spectrasonic.MythicEconomy.models.Currency;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import lombok.Getter;

@Getter
public abstract class CurrencyEconomyEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    protected final Player player;
    protected final double amount;
    protected final String currencyId;
    protected final Currency currency;
    protected boolean cancelled = false;

    public CurrencyEconomyEvent(Player player, double amount, String currencyId) {
        this.player = player;
        this.amount = amount;
        this.currencyId = currencyId;
        this.currency = getCurrencyById(currencyId);
    }

    private Currency getCurrencyById(String id) {
        // Nota: Esta implementación necesitaría acceso al CurrencyManager
        // Por simplicidad, devolveremos null por ahora
        // En una implementación real, inyectaríamos el CurrencyManager
        return null;
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