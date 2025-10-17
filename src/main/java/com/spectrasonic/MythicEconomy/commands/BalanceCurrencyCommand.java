package com.spectrasonic.MythicEconomy.commands;

import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.Utils.MessageUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;

public class BalanceCurrencyCommand {

    public void register() {
        // Comando principal /balcur
        new CommandAPICommand("balcur")
                .withPermission("MythicEconomy.balance.currency")
                .withArguments(new StringArgument("currency"))
                .executesPlayer((sender, args) -> {
                    String currencyId = (String) args.get("currency");
                    showPlayerBalance(sender, sender, currencyId);
                })
                .withSubcommand(
                        // /balcur <currency> <player> - Ver balance de otro jugador
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.balance.currency.others")
                                .withArguments(
                                        new StringArgument("currency"),
                                        new PlayerArgument("target")
                                )
                                .executesPlayer((sender, args) -> {
                                    String currencyId = (String) args.get("currency");
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalance(sender, target, currencyId);
                                })
                )
                .register();

        // Alias /balancecur
        new CommandAPICommand("balancecur")
                .withPermission("MythicEconomy.balance.currency")
                .withArguments(new StringArgument("currency"))
                .executesPlayer((sender, args) -> {
                    String currencyId = (String) args.get("currency");
                    showPlayerBalance(sender, sender, currencyId);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.balance.currency.others")
                                .withArguments(
                                        new StringArgument("currency"),
                                        new PlayerArgument("target")
                                )
                                .executesPlayer((sender, args) -> {
                                    String currencyId = (String) args.get("currency");
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalance(sender, target, currencyId);
                                })
                )
                .register();
    }

    private void showPlayerBalance(Player sender, Player target, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);

        if (currency == null) {
            MessageUtils.sendMessage(sender, "<red>La moneda <yellow>" + currencyId + "</yellow> no existe.");
            return;
        }

        if (!currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>La moneda <yellow>" + currencyId + "</yellow> está deshabilitada.");
            return;
        }

        double balance = economyManager.getBalance(target, currencyId);

        MessageUtils.sendMessage(sender,
                "<green>💰 Tu saldo en <yellow>" + currency.getName() + "</yellow> es: " +
                "<aqua>" + currency.formatMoney(balance) + "</aqua>");
    }

    private void showOtherPlayerBalance(Player sender, Player target, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);

        if (currency == null) {
            MessageUtils.sendMessage(sender, "<red>La moneda <yellow>" + currencyId + "</yellow> no existe.");
            return;
        }

        if (!currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>La moneda <yellow>" + currencyId + "</yellow> está deshabilitada.");
            return;
        }

        double balance = economyManager.getBalance(target, currencyId);

        MessageUtils.sendMessage(sender,
                "<green>💰 Saldo de <aqua>" + target.getName() + "</aqua> en <yellow>" +
                currency.getName() + "</yellow>: <aqua>" + currency.formatMoney(balance) + "</aqua>");
    }
}