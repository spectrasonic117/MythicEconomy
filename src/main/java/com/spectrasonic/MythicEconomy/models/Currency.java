package com.spectrasonic.MythicEconomy.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    private String id;
    private String name;
    private String nameSingular;
    private String symbol;
    private boolean decimal;
    private double startingBalance;
    private double maxBalance;
    private double minTransfer;
    private double maxTransfer;
    private boolean enabled;

    // Constructor simplificado para monedas básicas
    public Currency(String id, String name, String nameSingular, String symbol, boolean decimal) {
        this.id = id;
        this.name = name;
        this.nameSingular = nameSingular;
        this.symbol = symbol;
        this.decimal = decimal;
        this.startingBalance = 100.0;
        this.maxBalance = 999999999.99;
        this.minTransfer = 0.01;
        this.maxTransfer = 100000.0;
        this.enabled = true;
    }

    // Formatea una cantidad de dinero según la configuración de la moneda
    public String formatMoney(double amount) {
        if (decimal) {
            return symbol + String.format("%.2f", amount);
        } else {
            return symbol + String.format("%.0f", amount);
        }
    }

    // Formatea una cantidad corta (K, M, B, T)
    public String formatMoneyShort(double amount) {
        if (amount >= 1_000_000_000_000L) {
            return symbol + String.format("%.1fT", amount / 1_000_000_000_000.0);
        } else if (amount >= 1_000_000_000) {
            return symbol + String.format("%.1fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return symbol + String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return symbol + String.format("%.1fK", amount / 1_000.0);
        } else {
            return formatMoney(amount);
        }
    }

    // Valida si una cantidad es válida para esta moneda
    public boolean isValidAmount(double amount) {
        return amount >= 0 && amount <= maxBalance;
    }

    // Valida si una cantidad de transferencia es válida
    public boolean isValidTransferAmount(double amount) {
        return amount >= minTransfer && amount <= maxTransfer;
    }
}