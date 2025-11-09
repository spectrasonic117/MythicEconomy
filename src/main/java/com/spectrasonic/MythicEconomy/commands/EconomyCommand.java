package com.spectrasonic.MythicEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;

public class EconomyCommand {

    public void register() {
        // Comando principal /economy
        new CommandAPICommand("mythiceconomy")
                .withAliases("eco", "econ", "meco", "mythiceco", "me")
                .withPermission("MythicEconomy.economy.admin")
                .withSubcommands(
                        // /economy give <player> <amount>
                        new CommandAPICommand("give")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0.01))
                                .executes((sender, args) -> {
                                    Player target = (Player) args.get("player");
                                    if (target == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                                        return;
                                    }
                                    Object amountObj = args.get("amount");
                                    if (amountObj == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                                        return;
                                    }
                                    double amount = (double) amountObj;

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    if (economyManager.addMoney(target, amount)) {
                                        MessageUtils.sendMessage(sender,
                                                "<green>Se han dado <yellow>" + economyManager.formatMoney(amount) +
                                                        "</yellow> monedas a <aqua>" + target.getName() + "</aqua>.");
                                        MessageUtils.sendMessage(target,
                                                "<green>Has recibido <yellow>" + economyManager.formatMoney(amount) +
                                                        "</yellow> monedas. Nuevo saldo: <yellow>" +
                                                        economyManager.formatMoney(economyManager.getBalance(target))
                                                        + "</yellow>");
                                    } else {
                                        MessageUtils.sendMessage(sender, "<red>Error: La cantidad debe ser mayor a 0.");
                                    }
                                }),

                        // /economy take <player> <amount>
                        new CommandAPICommand("take")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0.01))
                                .executes((sender, args) -> {
                                    Player target = (Player) args.get("player");
                                    if (target == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                                        return;
                                    }
                                    Object amountObj = args.get("amount");
                                    if (amountObj == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                                        return;
                                    }
                                    double amount = (double) amountObj;

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    if (economyManager.removeMoney(target, amount)) {
                                        MessageUtils.sendMessage(sender,
                                                "<green>Se han quitado <yellow>" + economyManager.formatMoney(amount) +
                                                        "</yellow> monedas a <aqua>" + target.getName() + "</aqua>.");
                                        MessageUtils.sendMessage(target,
                                                "<red>Se te han quitado <yellow>" + economyManager.formatMoney(amount) +
                                                        "</yellow> monedas. Nuevo saldo: <yellow>" +
                                                        economyManager.formatMoney(economyManager.getBalance(target))
                                                        + "</yellow>");
                                    } else {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Error: <aqua>" + target.getName()
                                                        + "</aqua> no tiene suficiente dinero. " +
                                                        "Saldo actual: <yellow>"
                                                        + economyManager.formatMoney(economyManager.getBalance(target))
                                                        + "</yellow>");
                                    }
                                }),

                        // /economy set <player> <amount>
                        new CommandAPICommand("set")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0))
                                .executes((sender, args) -> {
                                    Player target = (Player) args.get("player");
                                    if (target == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                                        return;
                                    }
                                    Object amountObj = args.get("amount");
                                    if (amountObj == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                                        return;
                                    }
                                    double amount = (double) amountObj;

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    double previousBalance = economyManager.getBalance(target);
                                    economyManager.setBalance(target, amount);

                                    MessageUtils.sendMessage(sender,
                                            "<green>El saldo de <aqua>" + target.getName()
                                                    + "</aqua> ha sido establecido a <yellow>" +
                                                    economyManager.formatMoney(amount) + "</yellow> monedas.");
                                    MessageUtils.sendMessage(target,
                                            "<green>Tu saldo ha sido establecido a <yellow>"
                                                    + economyManager.formatMoney(amount) +
                                                    "</yellow> monedas. (Anterior: <gray>"
                                                    + economyManager.formatMoney(previousBalance) + "</gray>)");
                                }),

                        // /economy balance <player>
                        new CommandAPICommand("balance")
                                .withArguments(new PlayerArgument("player"))
                                .executes((sender, args) -> {
                                    Player target = (Player) args.get("player");
                                    if (target == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                                        return;
                                    }
                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    double balance = economyManager.getBalance(target);

                                    MessageUtils.sendMessage(sender,
                                            "<green>Saldo de <aqua>" + target.getName() + "</aqua>: <yellow>" +
                                                    economyManager.formatMoney(balance) + "</yellow> monedas.");
                                }),

                        // /economy reload
                        new CommandAPICommand("reload")
                                .executes((sender, args) -> {
                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    economyManager.reloadConfig();
                                    // No llamar savePlayerData aquí para evitar recursión infinita
                                    MessageUtils.sendMessage(sender,
                                            "<green>Configuración y datos de economía recargados correctamente.");
                                }),

                        // /economy top [cantidad]
                        new CommandAPICommand("top")
                                .executes((sender, args) -> {
                                    showTopBalances(sender, 10);
                                })
                                .withSubcommand(
                                        new CommandAPICommand("amount")
                                                .withArguments(new dev.jorel.commandapi.arguments.IntegerArgument(
                                                        "limit", 1, 50))
                                                .executes((sender, args) -> {
                                                    Object limitObj = args.get("limit");
                                                    if (limitObj == null) {
                                                        MessageUtils.sendMessage(sender,
                                                                "<red>Error: Límite no válido.");
                                                        return;
                                                    }
                                                    int limit = (int) limitObj;
                                                    showTopBalances(sender, limit);
                                                })),

                        // /economy stats
                        new CommandAPICommand("stats")
                                .executes((sender, args) -> {
                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    com.spectrasonic.MythicEconomy.Main plugin = (com.spectrasonic.MythicEconomy.Main) org.bukkit.Bukkit
                                            .getPluginManager().getPlugin("MythicEconomy");

                                    MessageUtils.sendMessage(sender, "<gold>═══ Estadísticas de Economía ═══</gold>");
                                    MessageUtils.sendMessage(sender, "<green>💰 Total en circulación: <yellow>" +
                                            economyManager.formatMoney(economyManager.getTotalMoney()) + "</yellow>");
                                    MessageUtils.sendMessage(sender, "<green>👥 Total de cuentas: <yellow>" +
                                            economyManager.getTotalAccounts() + "</yellow>");
                                    MessageUtils.sendMessage(sender, "<green>🏦 Saldo inicial: <yellow>" +
                                            economyManager.formatMoney(economyManager.getStartingBalance())
                                            + "</yellow>");
                                    MessageUtils.sendMessage(sender, "<green>💎 Moneda: <yellow>" +
                                            economyManager.getCurrencyName() + " (" + economyManager.getCurrencySymbol()
                                            + ")</yellow>");

                                    // Estado de Vault
                                    if (plugin != null && plugin.isVaultEnabled()) {
                                        MessageUtils.sendMessage(sender, "<green>✅ Vault: <yellow>HABILITADO</yellow>");
                                        MessageUtils.sendMessage(sender,
                                                "<green>   Otros plugins pueden usar MythicEconomy</yellow>");
                                    } else {
                                        MessageUtils.sendMessage(sender,
                                                "<red>❌ Vault: <yellow>DESHABILITADO</yellow>");
                                    }
                                }),

                        // /economy setstarting <cantidad>
                        new CommandAPICommand("setstarting")
                                .withArguments(new DoubleArgument("amount", 0))
                                .executes((sender, args) -> {
                                    Object amountObj = args.get("amount");
                                    if (amountObj == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                                        return;
                                    }
                                    double amount = (double) amountObj;
                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    economyManager.setStartingBalance(amount);
                                    MessageUtils.sendMessage(sender, "<green>Saldo inicial establecido a <yellow>" +
                                            economyManager.formatMoney(amount) + "</yellow> para nuevos jugadores.");
                                }))
                .register();
    }

    /**
     * Muestra el top de jugadores más ricos
     */
    private void showTopBalances(org.bukkit.command.CommandSender sender, int limit) {
        EconomyManager economyManager = EconomyManager.getInstance();
        java.util.Map<String, Double> topBalances = economyManager.getTopBalances(limit);

        if (topBalances.isEmpty()) {
            MessageUtils.sendMessage(sender, "<red>No hay datos de jugadores disponibles.");
            return;
        }

        MessageUtils.sendMessage(sender, "<gold>═══ Top " + limit + " Jugadores Más Ricos ═══</gold>");

        int position = 1;
        for (java.util.Map.Entry<String, Double> entry : topBalances.entrySet()) {
            String playerName = entry.getKey();
            double balance = entry.getValue();

            String medal = "";
            switch (position) {
                case 1:
                    medal = "🥇";
                    break;
                case 2:
                    medal = "🥈";
                    break;
                case 3:
                    medal = "🥉";
                    break;
                default:
                    medal = "<gray>" + position + ".</gray>";
                    break;
            }

            MessageUtils.sendMessage(sender, medal + " <aqua>" + playerName + "</aqua>: <yellow>" +
                    economyManager.formatMoney(balance) + "</yellow>");

            position++;
        }

        MessageUtils.sendMessage(sender, "<gray>Total mostrado: " + topBalances.size() + " jugadores</gray>");
    }
}