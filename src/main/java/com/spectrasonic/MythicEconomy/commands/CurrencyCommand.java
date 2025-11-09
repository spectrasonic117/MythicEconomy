package com.spectrasonic.MythicEconomy.commands;

import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;

public class CurrencyCommand {

    public void register() {
        // Comando principal /currency
        new CommandAPICommand("currency")
                .withPermission("MythicEconomy.currency.admin")
                .withSubcommands(
                        // /currency list - Lista todas las monedas disponibles
                        new CommandAPICommand("list")
                                .executesPlayer((sender, args) -> {
                                    listCurrencies(sender);
                                }),

                        // /currency add <id> <name> <nameSingular> <symbol> <decimal> - Agrega una
                        // nueva moneda
                        new CommandAPICommand("add")
                                .withArguments(
                                        new StringArgument("id"),
                                        new StringArgument("name"),
                                        new StringArgument("nameSingular"),
                                        new StringArgument("symbol"),
                                        new StringArgument("decimal"))
                                .executesPlayer((sender, args) -> {
                                    String id = (String) args.get("id");
                                    String name = (String) args.get("name");
                                    String nameSingular = (String) args.get("nameSingular");
                                    String symbol = (String) args.get("symbol");
                                    boolean decimal = "true".equalsIgnoreCase((String) args.get("decimal"));

                                    addCurrency(sender, id, name, nameSingular, symbol, decimal);
                                }),

                        // /currency remove <id> - Remueve una moneda
                        new CommandAPICommand("remove")
                                .withArguments(new StringArgument("id"))
                                .executesPlayer((sender, args) -> {
                                    String id = (String) args.get("id");
                                    removeCurrency(sender, id);
                                }),

                        // /currency info <id> - Muestra información de una moneda
                        new CommandAPICommand("info")
                                .withArguments(new StringArgument("id"))
                                .executesPlayer((sender, args) -> {
                                    String id = (String) args.get("id");
                                    showCurrencyInfo(sender, id);
                                }),

                        // /currency reload - Recarga todas las monedas
                        new CommandAPICommand("reload")
                                .executesPlayer((sender, args) -> {
                                    reloadCurrencies(sender);
                                }))
                .register();
    }

    private void listCurrencies(Player sender) {
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

    private void addCurrency(Player sender, String id, String name, String nameSingular, String symbol,
            boolean decimal) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();

        if (currencyManager.currencyExists(id)) {
            MessageUtils.sendMessage(sender, "<red>Ya existe una moneda con el ID: " + id);
            return;
        }

        Currency currency = new Currency(id, name, nameSingular, symbol, decimal);
        boolean success = currencyManager.addCurrency(currency);

        if (success) {
            MessageUtils.sendMessage(sender, "<green>Moneda <yellow>" + id + "</yellow> agregada correctamente.");
        } else {
            MessageUtils.sendMessage(sender, "<red>Error al agregar la moneda.");
        }
    }

    private void removeCurrency(Player sender, String id) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();

        if (!currencyManager.currencyExists(id)) {
            MessageUtils.sendMessage(sender, "<red>No existe una moneda con el ID: " + id);
            return;
        }

        if (id.equals("default")) {
            MessageUtils.sendMessage(sender, "<red>No puedes remover la moneda por defecto.");
            return;
        }

        boolean success = currencyManager.removeCurrency(id);

        if (success) {
            MessageUtils.sendMessage(sender, "<green>Moneda <yellow>" + id + "</yellow> removida correctamente.");
        } else {
            MessageUtils.sendMessage(sender, "<red>Error al remover la moneda.");
        }
    }

    private void showCurrencyInfo(Player sender, String id) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        Currency currency = currencyManager.getCurrency(id);

        if (currency == null) {
            MessageUtils.sendMessage(sender, "<red>No existe una moneda con el ID: " + id);
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

    private void reloadCurrencies(Player sender) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        currencyManager.reloadCurrencies();

        MessageUtils.sendMessage(sender, "<green>Monedas recargadas correctamente.");
    }
}