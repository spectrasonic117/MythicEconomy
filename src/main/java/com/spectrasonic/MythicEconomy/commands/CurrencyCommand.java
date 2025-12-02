package com.spectrasonic.MythicEconomy.commands;

import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.manager.EconomyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
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
                                }),

                        // /currency check <target> <currency> - Verifica el estado del almacenamiento
                        new CommandAPICommand("check")
                                .withArguments(
                                        new PlayerArgument("target"),
                                        new StringArgument("currency").replaceSuggestions(ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    String currencyId = (String) args.get("currency");
                                    checkStorageStatus(sender, target, currencyId);
                                }),

                        // /currency give <target> <amount> <currency> - Da monedas a un jugador
                        new CommandAPICommand("give")
                                .withArguments(
                                        new PlayerArgument("target"),
                                        new DoubleArgument("amount", 0.01),
                                        new StringArgument("currency").replaceSuggestions(ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    Double amt = (Double) args.get("amount");
                                    double amount = amt != null ? amt : 0.0;
                                    String currencyId = (String) args.get("currency");

                                    giveCurrency(sender, target, amount, currencyId);
                                }),

                        // /currency take <target> <amount> <currency> - Quita monedas a un jugador
                        new CommandAPICommand("take")
                                .withArguments(
                                        new PlayerArgument("target"),
                                        new DoubleArgument("amount", 0.01),
                                        new StringArgument("currency").replaceSuggestions(ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    Double amt = (Double) args.get("amount");
                                    double amount = amt != null ? amt : 0.0;
                                    String currencyId = (String) args.get("currency");

                                    takeCurrency(sender, target, amount, currencyId);
                                }),

                        // /currency set <target> <amount> <currency> - Establece el saldo de un jugador
                        new CommandAPICommand("set")
                                .withArguments(
                                        new PlayerArgument("target"),
                                        new DoubleArgument("amount", 0),
                                        new StringArgument("currency").replaceSuggestions(ArgumentSuggestions.strings(getCurrencySuggestions())))
                                .executesPlayer((sender, args) -> {
                                    Player target = (Player) args.get("target");
                                    Double amt = (Double) args.get("amount");
                                    double amount = amt != null ? amt : 0.0;
                                    String currencyId = (String) args.get("currency");

                                    setCurrency(sender, target, amount, currencyId);
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

        // Actualizar sugerencias de comandos después del reload
        // Nota: Las sugerencias se actualizan dinámicamente en getCurrencySuggestions()

        MessageUtils.sendMessage(sender, "<green>Monedas recargadas correctamente. Las nuevas monedas ahora están disponibles en los comandos.");
    }

    private String[] getCurrencySuggestions() {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        return currencyManager.getCurrencyIds().stream()
                .toArray(String[]::new);
    }

    private void giveCurrency(Player sender, Player target, double amount, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>Moneda no encontrada o deshabilitada: " + currencyId);
            return;
        }

        if (economyManager.addMoney(target, amount, currencyId)) {
            MessageUtils.sendMessage(sender,
                    "<green>Se han dado <yellow>" + currency.formatMoney(amount) + "</yellow> a <aqua>"
                            + target.getName() + "</aqua>.");
            MessageUtils.sendMessage(target,
                    "<green>Has recibido <yellow>" + currency.formatMoney(amount) + "</yellow>. Nuevo saldo: <yellow>" +
                            currency.formatMoney(economyManager.getBalance(target, currencyId)) + "</yellow>");
        } else {
            MessageUtils.sendMessage(sender, "<red>Error al dar monedas.");
        }
    }

    private void takeCurrency(Player sender, Player target, double amount, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>Moneda no encontrada o deshabilitada: " + currencyId);
            return;
        }

        if (economyManager.removeMoney(target, amount, currencyId)) {
            MessageUtils.sendMessage(sender,
                    "<green>Se han quitado <yellow>" + currency.formatMoney(amount) + "</yellow> a <aqua>"
                            + target.getName() + "</aqua>.");
            MessageUtils.sendMessage(target,
                    "<red>Se te han quitado <yellow>" + currency.formatMoney(amount)
                            + "</yellow>. Nuevo saldo: <yellow>" +
                            currency.formatMoney(economyManager.getBalance(target, currencyId)) + "</yellow>");
        } else {
            MessageUtils.sendMessage(sender,
                    "<red><aqua>" + target.getName() + "</aqua> no tiene suficiente dinero. Saldo actual: <yellow>" +
                            currency.formatMoney(economyManager.getBalance(target, currencyId)) + "</yellow>");
        }
    }

    private void setCurrency(Player sender, Player target, double amount, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>Moneda no encontrada o deshabilitada: " + currencyId);
            return;
        }

        double previousBalance = economyManager.getBalance(target, currencyId);
        economyManager.setBalance(target, amount, currencyId);

        MessageUtils.sendMessage(sender,
                "<green>El saldo de <aqua>" + target.getName() + "</aqua> ha sido establecido a <yellow>" +
                        currency.formatMoney(amount) + "</yellow>.");
        MessageUtils.sendMessage(target,
                "<green>Tu saldo ha sido establecido a <yellow>" + currency.formatMoney(amount) +
                        "</yellow>. (Anterior: <gray>" + currency.formatMoney(previousBalance) + "</gray>)");
    }

    private void checkStorageStatus(Player sender, Player target, String currencyId) {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        EconomyManager economyManager = EconomyManager.getInstance();

        Currency currency = currencyManager.getCurrency(currencyId);
        if (currency == null || !currency.isEnabled()) {
            MessageUtils.sendMessage(sender, "<red>Moneda no encontrada o deshabilitada: " + currencyId);
            return;
        }

        MessageUtils.sendMessage(sender, "<gray>=== VERIFICANDO SISTEMA DE ALMACENAMIENTO ===</gray>");
        MessageUtils.sendMessage(sender, "<gray>Jugador: <aqua>" + target.getName() + "</aqua> (" + target.getUniqueId() + ")</gray>");
        MessageUtils.sendMessage(sender, "<gray>Moneda: <gold>" + currencyId + "</gold></gray>");
        
        // Verificar tipo de almacenamiento
        if (economyManager.isUsingMySQL()) {
            MessageUtils.sendMessage(sender, "<green>Tipo de almacenamiento: <yellow>MySQL</yellow></green>");
            
            try {
                com.spectrasonic.MythicEconomy.database.MySQLEconomyProvider mysqlProvider =
                    (com.spectrasonic.MythicEconomy.database.MySQLEconomyProvider) economyManager.getDataProvider();
                
                boolean exists = mysqlProvider.playerExists(target.getUniqueId(), currencyId);
                MessageUtils.sendMessage(sender, "<gray>Existe en BD: " + (exists ? "<green>Sí</green>" : "<red>No</red>") + "</gray>");
                
                if (!exists) {
                    MessageUtils.sendMessage(sender, "<yellow>Creando jugador en la base de datos...</yellow>");
                    mysqlProvider.createPlayer(target.getUniqueId(), currencyId);
                    MessageUtils.sendMessage(sender, "<green>Jugador creado exitosamente</green>");
                }
                
            } catch (Exception e) {
                MessageUtils.sendMessage(sender, "<red>Error al verificar MySQL: " + e.getMessage() + "</red>");
            }
            
        } else if (economyManager.isUsingMongoDB()) {
            MessageUtils.sendMessage(sender, "<green>Tipo de almacenamiento: <yellow>MongoDB</yellow></green>");
        } else {
            MessageUtils.sendMessage(sender, "<green>Tipo de almacenamiento: <yellow>Archivos locales</yellow></green>");
        }
        
        // Mostrar balance actual
        double balance = economyManager.getBalance(target, currencyId);
        MessageUtils.sendMessage(sender, "<gray>Balance actual: <gold>" + currency.formatMoney(balance) + "</gold></gray>");
        MessageUtils.sendMessage(sender, "<gray>Saldo inicial: <yellow>" + currency.formatMoney(currency.getStartingBalance()) + "</yellow></gray>");
        
        // Verificar si el balance parece correcto
        if (balance == currency.getStartingBalance()) {
            MessageUtils.sendMessage(sender, "<yellow>⚠ El balance es igual al saldo inicial (posible jugador nuevo)</yellow>");
        } else if (balance == 0.0) {
            MessageUtils.sendMessage(sender, "<red>⚠ El balance es 0 (posible problema de persistencia)</red>");
        } else {
            MessageUtils.sendMessage(sender, "<green>✓ El balance parece ser correcto</green>");
        }
        
        MessageUtils.sendMessage(sender, "<gray>=== FIN DE VERIFICACIÓN ===</gray>");
    }
}