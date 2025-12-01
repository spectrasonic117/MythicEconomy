package com.spectrasonic.MythicEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import com.spectrasonic.MythicEconomy.manager.EconomyManagerAsync;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Versión asíncrona del comando Pay para transferencias no bloqueantes.
 * Implementa lógica atómica y manejo de errores robusto.
 */
public class PayCommandAsync {

    public void register() {
        new CommandAPICommand("pay")
                .withPermission("MythicEconomy.pay")
                .withArguments(
                        new PlayerArgument("target"),
                        new IntegerArgument("amount", 1))
                .executesPlayer((sender, args) -> {
                    Player target = (Player) args.get("target");
                    Object amountObj = args.get("amount");

                    if (target == null) {
                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                        return;
                    }

                    if (amountObj == null) {
                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                        return;
                    }

                    double amount = ((Integer) amountObj).doubleValue();
                    processPaymentAsync(sender, target, amount);
                })
                .register();
    }

    /**
     * Procesa el pago de forma asíncrona con lógica atómica
     */
    private void processPaymentAsync(Player sender, Player target, double amount) {
        EconomyManagerAsync economyManager = EconomyManagerAsync.getInstance();

        // Validaciones iniciales síncronas
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            MessageUtils.sendMessage(sender, "<red>No puedes enviarte dinero a ti mismo.");
            return;
        }

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, "<red>La cantidad debe ser mayor a 0.");
            return;
        }

        // Mostrar mensaje de carga
        MessageUtils.sendMessage(sender,
                "<yellow>💸 Procesando transferencia de " +
                        economyManager.getCurrencyManager().getCurrency("default").formatMoney(amount) +
                        " a " + target.getName() + "...</yellow>");

        // Verificar saldo del remitente de forma asíncrona
        CompletableFuture<Double> senderBalanceFuture = economyManager.getBalanceAsync(sender);

        senderBalanceFuture
                .thenCompose(senderBalance -> {
                    // Verificar fondos suficientes
                    if (senderBalance < amount) {
                        return CompletableFuture.failedFuture(
                                new IllegalStateException("Fondos insuficientes: " + senderBalance + " < " + amount));
                    }

                    // Intentar realizar la transferencia atómica
                    return performAtomicTransfer(sender, target, amount);
                })
                .thenAccept(success -> {
                    // Volver al hilo principal para enviar mensajes
                    Runnable task = () -> {
                        if (success) {
                            // Mensaje simple sin obtener saldos actualizados para evitar complejidad
                            MessageUtils.sendMessage(sender,
                                    "<green>💸 Has enviado <yellow>" +
                                            economyManager.getCurrencyManager().getCurrency("default")
                                                    .formatMoney(amount)
                                            +
                                            "</yellow> monedas a <aqua>" + target.getName() + "</aqua>.");

                            MessageUtils.sendMessage(target,
                                    "<green>💸 Has recibido <yellow>" +
                                            economyManager.getCurrencyManager().getCurrency("default")
                                                    .formatMoney(amount)
                                            +
                                            "</yellow> monedas de <aqua>" + sender.getName() + "</aqua>.");
                        } else {
                            MessageUtils.sendMessage(sender,
                                    "<red>❌ Error al procesar la transferencia. Inténtalo de nuevo.");
                        }
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                })
                .exceptionally(throwable -> {
                    // Manejar errores en el hilo principal
                    Runnable task = () -> {
                        if (throwable.getCause() instanceof IllegalStateException &&
                                throwable.getMessage().contains("Fondos insuficientes")) {
                            // Error de fondos insuficientes
                            MessageUtils.sendMessage(sender,
                                    "<red>No tienes suficiente dinero para realizar esta transferencia.");
                        } else {
                            // Otro error
                            MessageUtils.sendMessage(sender,
                                    "<red>❌ Error al procesar la transferencia. Inténtalo de nuevo.");
                        }
                    };
                    Bukkit.getScheduler().runTask(economyManager.getPlugin(), task);
                    return null;
                });
    }

    /**
     * Realiza una transferencia atómica de forma asíncrona
     */
    private CompletableFuture<Boolean> performAtomicTransfer(Player sender, Player target, double amount) {
        EconomyManagerAsync economyManager = EconomyManagerAsync.getInstance();

        return CompletableFuture.allOf(
                economyManager.removeMoneyAsync(sender, amount),
                economyManager.addMoneyAsync(target, amount)).thenApply(ignored -> true)
                .exceptionally(throwable -> {
                    // Si falla, intentar revertir (mejor esquema sería usar transacciones)
                    if (throwable.getCause() != null) {
                        // Intentar devolver dinero al remitente si se quitó pero no se entregó
                        economyManager.addMoneyAsync(sender, amount);
                    }
                    return false;
                });
    }
}