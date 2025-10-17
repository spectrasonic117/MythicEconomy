package com.spectrasonic.MythicEconomy.api.events;

import org.bukkit.entity.Player;
import lombok.Getter;

@Getter
public class CurrencyMoneyRemoveEvent extends CurrencyEconomyEvent {

    private final double balanceBefore;
    private final double balanceAfter;

    public CurrencyMoneyRemoveEvent(Player player, double amount, double balanceBefore, double balanceAfter, String currencyId) {
        super(player, amount, currencyId);
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
    }
}