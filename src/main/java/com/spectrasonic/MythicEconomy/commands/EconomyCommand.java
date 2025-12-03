package com.spectrasonic.MythicEconomy.commands;

import com.spectrasonic.MythicEconomy.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.CommandPermission;
import org.bukkit.entity.Player;
import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import java.util.Map;

public class EconomyCommand {

    public void register() {
        // Comando principal /economy
        new CommandAPICommand("mythiceconomy")
                .withAliases("eco", "econ", "meco", "mythiceco", "me")
                .withPermission(CommandPermission.OP)
                .withSubcommands(
                        // /economy give <player> <amount> [currency]
                        new CommandAPICommand("give")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0.01),
                                        new StringArgument("currency").setOptional(true).replaceSuggestions(
                                                ArgumentSuggestions.strings(getCurrencySuggestions())))
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
                                    String currencyId = (String) args.get("currency");
                                    if (currencyId == null) {
                                        currencyId = "default";
                                    }

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    CurrencyManager currencyManager = CurrencyManager.getInstance();
                                    Currency currency = currencyManager.getCurrency(currencyId);
                                    if (currency == null || !currency.isEnabled()) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Moneda no encontrada o deshabilitada: " + currencyId);
                                        return;
                                    }

                                    if (economyManager.addMoney(target, amount, currencyId)) {
                                        MessageUtils.sendMessage(sender,
                                                "<green>Se han dado <yellow>" + currency.formatMoney(amount) +
                                                        "</yellow> a <aqua>" + target.getName() + "</aqua>.");
                                        MessageUtils.sendMessage(target,
                                                "<green>Has recibido <yellow>" + currency.formatMoney(amount) +
                                                        "</yellow>. Nuevo saldo: <yellow>" +
                                                        currency.formatMoney(
                                                                economyManager.getBalance(target, currencyId))
                                                        + "</yellow>");
                                    } else {
                                        MessageUtils.sendMessage(sender, "<red>Error: La cantidad debe ser mayor a 0.");
                                    }
                                }),

                        // /economy take <player> <amount> [currency]
                        new CommandAPICommand("take")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0.01),
                                        new StringArgument("currency").setOptional(true).replaceSuggestions(
                                                ArgumentSuggestions.strings(getCurrencySuggestions())))
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
                                    String currencyId = (String) args.get("currency");
                                    if (currencyId == null) {
                                        currencyId = "default";
                                    }

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    CurrencyManager currencyManager = CurrencyManager.getInstance();
                                    Currency currency = currencyManager.getCurrency(currencyId);
                                    if (currency == null || !currency.isEnabled()) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Moneda no encontrada o deshabilitada: " + currencyId);
                                        return;
                                    }

                                    if (economyManager.removeMoney(target, amount, currencyId)) {
                                        MessageUtils.sendMessage(sender,
                                                "<green>Se han quitado <yellow>" + currency.formatMoney(amount) +
                                                        "</yellow> a <aqua>" + target.getName() + "</aqua>.");
                                        MessageUtils.sendMessage(target,
                                                "<red>Se te han quitado <yellow>" + currency.formatMoney(amount) +
                                                        "</yellow>. Nuevo saldo: <yellow>" +
                                                        currency.formatMoney(
                                                                economyManager.getBalance(target, currencyId))
                                                        + "</yellow>");
                                    } else {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Error: <aqua>" + target.getName()
                                                        + "</aqua> no tiene suficiente dinero. " +
                                                        "Saldo actual: <yellow>"
                                                        + currency.formatMoney(
                                                                economyManager.getBalance(target, currencyId))
                                                        + "</yellow>");
                                    }
                                }),

                        // /economy set <player> <amount> [currency]
                        new CommandAPICommand("set")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new DoubleArgument("amount", 0),
                                        new StringArgument("currency").setOptional(true).replaceSuggestions(
                                                ArgumentSuggestions.strings(getCurrencySuggestions())))
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
                                    String currencyId = (String) args.get("currency");
                                    if (currencyId == null) {
                                        currencyId = "default";
                                    }

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    CurrencyManager currencyManager = CurrencyManager.getInstance();
                                    Currency currency = currencyManager.getCurrency(currencyId);
                                    if (currency == null || !currency.isEnabled()) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Moneda no encontrada o deshabilitada: " + currencyId);
                                        return;
                                    }

                                    double previousBalance = economyManager.getBalance(target, currencyId);
                                    economyManager.setBalance(target, amount, currencyId);

                                    MessageUtils.sendMessage(sender,
                                            "<green>El saldo de <aqua>" + target.getName()
                                                    + "</aqua> ha sido establecido a <yellow>" +
                                                    currency.formatMoney(amount) + "</yellow>.");
                                    MessageUtils.sendMessage(target,
                                            "<green>Tu saldo ha sido establecido a <yellow>"
                                                    + currency.formatMoney(amount) +
                                                    "</yellow>. (Anterior: <gray>"
                                                    + currency.formatMoney(previousBalance) + "</gray>)");
                                }),

                        // /economy balance <player> [currency]
                        new CommandAPICommand("balance")
                                .withArguments(
                                        new PlayerArgument("player"),
                                        new StringArgument("currency").setOptional(true).replaceSuggestions(
                                                ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executes((sender, args) -> {
                                    Player target = (Player) args.get("player");
                                    if (target == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Jugador no encontrado.");
                                        return;
                                    }
                                    String currencyId = (String) args.get("currency");
                                    if (currencyId == null) {
                                        currencyId = "default";
                                    }

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    CurrencyManager currencyManager = CurrencyManager.getInstance();
                                    Currency currency = currencyManager.getCurrency(currencyId);
                                    if (currency == null || !currency.isEnabled()) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Moneda no encontrada o deshabilitada: " + currencyId);
                                        return;
                                    }

                                    double balance = economyManager.getBalance(target, currencyId);

                                    MessageUtils.sendMessage(sender,
                                            "<green>Saldo de <aqua>" + target.getName() + "</aqua>: <yellow>" +
                                                    currency.formatMoney(balance) + "</yellow>.");
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
                                                .withArguments(new IntegerArgument(
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
                                    Main plugin = (Main) Bukkit
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

                        // /economy setstarting <cantidad> [currency]
                        new CommandAPICommand("setstarting")
                                .withArguments(
                                        new DoubleArgument("amount", 0),
                                        new StringArgument("currency").setOptional(true).replaceSuggestions(
                                                ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executes((sender, args) -> {
                                    Object amountObj = args.get("amount");
                                    if (amountObj == null) {
                                        MessageUtils.sendMessage(sender, "<red>Error: Cantidad no válida.");
                                        return;
                                    }
                                    double amount = (double) amountObj;
                                    String currencyId = (String) args.get("currency");
                                    if (currencyId == null) {
                                        currencyId = "default";
                                    }

                                    EconomyManager economyManager = EconomyManager.getInstance();
                                    CurrencyManager currencyManager = CurrencyManager.getInstance();
                                    Currency currency = currencyManager.getCurrency(currencyId);
                                    if (currency == null || !currency.isEnabled()) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Moneda no encontrada o deshabilitada: " + currencyId);
                                        return;
                                    }

                                    economyManager.setStartingBalance(amount);
                                    MessageUtils.sendMessage(sender, "<green>Saldo inicial establecido a <yellow>" +
                                            currency.formatMoney(amount) + "</yellow> para nuevos jugadores.");
                                }),

                        // /economy list - Lista todas las monedas disponibles
                        new CommandAPICommand("list")
                                .executes((sender, args) -> {
                                    listCurrencies(sender);
                                }),

                        // /economy info <currency> - Muestra información de una moneda
                        new CommandAPICommand("info")
                                .withArguments(new StringArgument("currency")
                                        .replaceSuggestions(ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executes((sender, args) -> {
                                    String currencyId = (String) args.get("currency");
                                    showCurrencyInfo(sender, currencyId);
                                }))
                .register();
    }

    private String[] getCurrencySuggestions() {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        return currencyManager.getCurrencyIds().stream()
                .toArray(String[]::new);
    }

    private void listCurrencies(CommandSender sender) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();

        MessageUtils.sendMessage(sender, "<green><bold>=== MONEDAS DISPONIBLES ===</bold></green>");

        for (Currency currency : currencyManager.getEnabledCurrencies()) {
            String status = currency.isEnabled() ? "<green>Habilitada</green>" : "<red>Deshabilitada</red>";
            String decimal = currency.isDecimal() ? "<green>Con decimales</green>" : "<red>Sin decimales</red>";

            MessageUtils.sendMessage(sender,
                    "<yellow>" + currency.getId() + "</yellow> <gray>-</gray> " +
                            "<aqua>" + currency.getName() + "</aqua> " +
                            "<gray>(</gray>" + currency.getSymbol() + "<gray>)</gray> " +
                            "<gray>|</gray> " + status + " <gray>|</gray> " + decimal);
        }

        MessageUtils.sendMessage(sender,
                "<gray>Total: " + currencyManager.getEnabledCurrenciesCount() + " monedas habilitadas</gray>");
    }

    private void showCurrencyInfo(CommandSender sender, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        Currency currency = currencyManager.getCurrency(currencyId);

        if (currency == null) {
            MessageUtils.sendMessage(sender, "<red>No existe una moneda con el ID: " + currencyId);
            return;
        }

        MessageUtils.sendMessage(sender, "<green><bold>=== INFORMACIÓN DE MONEDA ===</bold></green>");
        MessageUtils.sendMessage(sender, "<yellow>ID:</yellow> " + currency.getId());
        MessageUtils.sendMessage(sender, "<yellow>Nombre:</yellow> " + currency.getName());
        MessageUtils.sendMessage(sender, "<yellow>Nombre Singular:</yellow> " + currency.getNameSingular());
        MessageUtils.sendMessage(sender, "<yellow>Símbolo:</yellow> " + currency.getSymbol());
        MessageUtils.sendMessage(sender,
                "<yellow>Decimal:</yellow> " + (currency.isDecimal() ? "<green>Sí</green>" : "<red>No</red>"));
        MessageUtils.sendMessage(sender,
                "<yellow>Saldo Inicial:</yellow> " + currency.formatMoney(currency.getStartingBalance()));
        MessageUtils.sendMessage(sender,
                "<yellow>Balance Máximo:</yellow> " + currency.formatMoney(currency.getMaxBalance()));
        MessageUtils.sendMessage(sender,
                "<yellow>Transferencia Mínima:</yellow> " + currency.formatMoney(currency.getMinTransfer()));
        MessageUtils.sendMessage(sender,
                "<yellow>Transferencia Máxima:</yellow> " + currency.formatMoney(currency.getMaxTransfer()));
        MessageUtils.sendMessage(sender,
                "<yellow>Habilitada:</yellow> " + (currency.isEnabled() ? "<green>Sí</green>" : "<red>No</red>"));
    }

    /**
     * Muestra el top de jugadores más ricos
     */
    private void showTopBalances(CommandSender sender, int limit) {
        EconomyManager economyManager = EconomyManager.getInstance();
        Map<String, Double> topBalances = economyManager.getTopBalances(limit);

        if (topBalances.isEmpty()) {
            MessageUtils.sendMessage(sender, "<red>No hay datos de jugadores disponibles.");
            return;
        }

        MessageUtils.sendMessage(sender, "<gold>═══ Top " + limit + " Jugadores Más Ricos ═══</gold>");

        int position = 1;
        for (Map.Entry<String, Double> entry : topBalances.entrySet()) {
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