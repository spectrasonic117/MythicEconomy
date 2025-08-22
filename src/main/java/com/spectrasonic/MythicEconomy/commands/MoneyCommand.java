package com.spectrasonic.MythicEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;

import org.bukkit.entity.Player;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.Utils.MessageUtils;

public class MoneyCommand {

    public void register() {
        // /money - Ver tu propio saldo o el de otro jugador (con permisos)
        new CommandAPICommand("money")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalance(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalance(sender, target);
                                }))
                .register();

        // /balance - Alias para /money
        new CommandAPICommand("balance")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalance(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalance(sender, target);
                                }))
                .register();

        // /bal - Otro alias para /money
        new CommandAPICommand("bal")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalance(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalance(sender, target);
                                }))
                .register();
    }

    /**
     * Muestra el saldo del propio jugador
     */
    private void showPlayerBalance(Player sender, Player target) {
        EconomyManager economyManager = EconomyManager.getInstance();
        double balance = economyManager.getBalance(target);

        MessageUtils.sendMessage(sender,
                "<green>💰 Tu saldo actual es: <yellow>" + economyManager.formatMoney(balance) + "</yellow> monedas.");
    }

    /**
     * Muestra el saldo de otro jugador (requiere permisos)
     */
    private void showOtherPlayerBalance(Player sender, Player target) {
        EconomyManager economyManager = EconomyManager.getInstance();
        double balance = economyManager.getBalance(target);

        MessageUtils.sendMessage(sender,
                "<green>💰 Saldo de <aqua>" + target.getName() + "</aqua>: <yellow>" +
                        economyManager.formatMoney(balance) + "</yellow> monedas.");
    }
}
