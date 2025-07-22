package com.spectrasonic.MangoEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;
import com.spectrasonic.MangoEconomy.manager.EconomyManager;
import com.spectrasonic.Utils.MessageUtils;

public class PayCommand {
    public void register() {
        new CommandAPICommand("pay")
            .withPermission("mangoeconomy.pay")
            .withArguments(
                new PlayerArgument("target"),
                new IntegerArgument("amount", 1)
            )
            .executesPlayer((sender, args) -> {
                Player target = (Player) args.get("target");
                if (target == null) {
                    MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                    return;
                }
                Object amountObj = args.get("amount");
                if (amountObj == null) {
                    MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                    return;
                }
                int amount = (int) amountObj;
                
                EconomyManager economyManager = EconomyManager.getInstance();
                
                // Verificar que no se envíe dinero a sí mismo
                if (sender.getUniqueId().equals(target.getUniqueId())) {
                    MessageUtils.sendMessage(sender, "<red>No puedes enviarte dinero a ti mismo.");
                    return;
                }
                
                // Verificar que la cantidad sea válida
                if (amount <= 0) {
                    MessageUtils.sendMessage(sender, "<red>La cantidad debe ser mayor a 0.");
                    return;
                }
                
                // Verificar que el jugador tenga suficiente dinero
                if (!economyManager.hasEnoughMoney(sender, amount)) {
                    MessageUtils.sendMessage(sender, 
                        "<red>No tienes suficiente dinero. Tu saldo actual es: <yellow>" + 
                        economyManager.formatMoney(economyManager.getBalance(sender)) + "</yellow> monedas.");
                    return;
                }
                
                // Realizar la transferencia
                if (economyManager.removeMoney(sender, amount) && economyManager.addMoney(target, amount)) {
                    // Mensaje al remitente
                    MessageUtils.sendMessage(sender, 
                        "<green>Has enviado <yellow>" + economyManager.formatMoney(amount) + 
                        "</yellow> monedas a <aqua>" + target.getName() + "</aqua>. " +
                        "Tu nuevo saldo: <yellow>" + economyManager.formatMoney(economyManager.getBalance(sender)) + "</yellow>");
                    
                    // Mensaje al receptor
                    MessageUtils.sendMessage(target, 
                        "<green>Has recibido <yellow>" + economyManager.formatMoney(amount) + 
                        "</yellow> monedas de <aqua>" + sender.getName() + "</aqua>. " +
                        "Tu nuevo saldo: <yellow>" + economyManager.formatMoney(economyManager.getBalance(target)) + "</yellow>");
                } else {
                    MessageUtils.sendMessage(sender, "<red>Error al procesar la transferencia. Inténtalo de nuevo.");
                }
            })
            .register();
    }
}
