package com.spectrasonic.MythicEconomy.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;

import com.spectrasonic.MythicEconomy.database.BenchmarkTool;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import com.spectrasonic.MythicEconomy.manager.EconomyManagerAsync;

// Comando para ejecutar benchmarks de rendimiento del sistema económico asíncrono.

public class BenchmarkCommand {

    private final BenchmarkTool benchmarkTool;

    public BenchmarkCommand() {
        this.benchmarkTool = new BenchmarkTool(EconomyManagerAsync.getInstance().getPlugin());
    }

    public void register() {
        new CommandAPICommand("benchmark")
                .withPermission("MythicEconomy.admin.benchmark")
                .withSubcommand(
                        new CommandAPICommand("start")
                                .withArguments(
                                        new IntegerArgument("users", 1),
                                        new IntegerArgument("operations", 1),
                                        new IntegerArgument("duration", 10))
                                .executes((sender, args) -> {
                                    Integer usersObj = (Integer) args.get("users");
                                    Integer operationsObj = (Integer) args.get("operations");
                                    Integer durationObj = (Integer) args.get("duration");

                                    if (usersObj == null || operationsObj == null || durationObj == null) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Error: Argumentos inválidos en el comando.</red>");
                                        return;
                                    }

                                    int users = usersObj;
                                    int operations = operationsObj;
                                    int duration = durationObj;

                                    if (users > 1000) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Demasiados usuarios concurrentes. Máximo 1000.</red>");
                                        return;
                                    }

                                    if (operations > 10000) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Demasiadas operaciones por usuario. Máximo 10000.</red>");
                                        return;
                                    }

                                    if (duration > 300) {
                                        MessageUtils.sendMessage(sender,
                                                "<red>Duración demasiado larga. Máximo 300 segundos.</red>");
                                        return;
                                    }

                                    benchmarkTool.startBenchmark(users, operations, duration);
                                    MessageUtils.sendMessage(sender,
                                            "<green>🚀 Benchmark iniciado: " + users + " usuarios, " +
                                                    operations + " operaciones, " + duration + "s de duración</green>");
                                }))
                .withSubcommand(
                        new CommandAPICommand("quick")
                                .executes((sender, args) -> {
                                    benchmarkTool.quickBenchmark();
                                    MessageUtils.sendMessage(sender, "<green>🚀 Benchmark rápido iniciado</green>");
                                }))
                .withSubcommand(
                        new CommandAPICommand("stress")
                                .executes((sender, args) -> {
                                    benchmarkTool.stressBenchmark();
                                    MessageUtils.sendMessage(sender, "<red>🔥 Benchmark de estrés iniciado</red>");
                                }))
                .withSubcommand(
                        new CommandAPICommand("status")
                                .executes((sender, args) -> {
                                    String status = benchmarkTool.getBenchmarkStatus();
                                    MessageUtils.sendMessage(sender,
                                            "<yellow>Estado del benchmark: " + status + "</yellow>");
                                }))
                .withSubcommand(
                        new CommandAPICommand("cancel")
                                .executes((sender, args) -> {
                                    benchmarkTool.cancelBenchmark();
                                    MessageUtils.sendMessage(sender, "<yellow>Benchmark cancelado</yellow>");
                                }))
                .register();
    }
}