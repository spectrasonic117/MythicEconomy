package com.spectrasonic.MythicEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import com.spectrasonic.MythicEconomy.manager.EconomyManagerAsync;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;

// import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

// Versión asíncrona del comando Money para operaciones no bloqueantes.
// Basado en PaperMC recomendaciones para async command handling.
public class MoneyCommandAsync {

    public void register() {
        // /money - Ver tu propio saldo o el de otro jugador (con permisos)
        new CommandAPICommand("money")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalanceAsync(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalanceAsync(sender, target);
                                }))
                .register();

        // /balance - Alias para /money
        new CommandAPICommand("balance")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalanceAsync(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalanceAsync(sender, target);
                                }))
                .register();

        // /bal - Otro alias para /money
        new CommandAPICommand("bal")
                .withPermission("MythicEconomy.money")
                .executesPlayer((sender, args) -> {
                    showPlayerBalanceAsync(sender, sender);
                })
                .withSubcommand(
                        new CommandAPICommand("player")
                                .withPermission("MythicEconomy.economy.balance.others")
                                .withArguments(new PlayerArgument("target"))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    showOtherPlayerBalanceAsync(sender, target);
                                }))
                .register();
    }

    // Muestra el saldo del propio jugador de forma asíncrona
    private void showPlayerBalanceAsync(Player sender, Player target) {
        EconomyManagerAsync economyManager = EconomyManagerAsync.getInstance();

        // Mostrar mensaje de carga
        MessageUtils.sendMessage(sender, "<yellow>💰 Cargando tu saldo...</yellow>");

        // Operación asíncrona
        CompletableFuture<Double> balanceFuture = economyManager.getBalanceAsync(target);

        balanceFuture
                .thenAccept(balance -> {
                    // Volver al hilo principal para enviar mensaje
                    Runnable task = () -> {
                        MessageUtils.sendMessage(sender,
                                "<green>💰 Tu saldo actual es: <yellow>" +
                                        economyManager.getCurrencyManager().getCurrency("default").formatMoney(balance)
                                        +
                                        "</yellow> monedas.");
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                })
                .exceptionally(throwable -> {
                    // Manejar errores en el hilo principal
                    Runnable task = () -> {
                        MessageUtils.sendMessage(sender,
                                "<red>❌ Error al cargar tu saldo. Inténtalo de nuevo.");
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                    return null;
                });
    }

    // Muestra el saldo de otro jugador de forma asíncrona
    private void showOtherPlayerBalanceAsync(Player sender, Player target) {
        EconomyManagerAsync economyManager = EconomyManagerAsync.getInstance();

        // Mostrar mensaje de carga
        MessageUtils.sendMessage(sender,
                "<yellow>💰 Cargando saldo de " + target.getName() + "...</yellow>");

        // Operación asíncrona
        CompletableFuture<Double> balanceFuture = economyManager.getBalanceAsync(target);

        balanceFuture
                .thenAccept(balance -> {
                    // Volver al hilo principal para enviar mensaje
                    Runnable task = () -> {
                        MessageUtils.sendMessage(sender,
                                "<green>💰 Saldo de <aqua>" + target.getName() + "</aqua>: <yellow>" +
                                        economyManager.getCurrencyManager().getCurrency("default").formatMoney(balance)
                                        +
                                        "</yellow> monedas.");
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                })
                .exceptionally(throwable -> {
                    // Manejar errores en el hilo principal
                    Runnable task = () -> {
                        MessageUtils.sendMessage(sender,
                                "<red>❌ Error al cargar el saldo de " + target.getName() + ". Inténtalo de nuevo.");
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                    return null;
                });
    }

    // Obtiene el plugin desde EconomyManagerAsync (necesario para el scheduler)
    // private JavaPlugin getPlugin() {
    // return EconomyManagerAsync.getInstance().getPlugin();
    // }
}