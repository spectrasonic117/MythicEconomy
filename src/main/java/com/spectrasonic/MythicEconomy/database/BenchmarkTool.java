package com.spectrasonic.MythicEconomy.database;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import com.spectrasonic.MythicEconomy.manager.EconomyManagerAsync;
import com.spectrasonic.MythicEconomy.utils.MessageUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Herramienta de benchmark para probar el rendimiento bajo carga concurrente.
 * Simula múltiples operaciones simultáneas para validar que el sistema
 * asíncrono
 * maneja bien la concurrencia.
 */
public class BenchmarkTool {

    private final JavaPlugin plugin;
    private final EconomyManagerAsync economyManager;
    private volatile boolean isRunning = false;
    private volatile boolean benchmarkCompleted = false;

    // Métricas
    private final AtomicLong successfulOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private long startTime;
    private long endTime;

    public BenchmarkTool(JavaPlugin plugin) {
        this.plugin = plugin;
        this.economyManager = EconomyManagerAsync.getInstance();
    }

    /**
     * Inicia una prueba de benchmark con configuración personalizada
     */
    public void startBenchmark(int concurrentUsers, int operationsPerUser, int testDurationSeconds) {
        if (isRunning) {
            MessageUtils.sendConsoleMessage("<red>Ya hay un benchmark en ejecución.</red>");
            return;
        }

        isRunning = true;
        successfulOperations.set(0);
        failedOperations.set(0);
        totalLatency.set(0);
        benchmarkCompleted = false;

        MessageUtils.sendConsoleMessage("<green>🚀 Iniciando benchmark de rendimiento</green>");
        MessageUtils.sendConsoleMessage("<yellow>Configuración: " + concurrentUsers + " usuarios concurrentes, " +
                operationsPerUser + " operaciones por usuario, " + testDurationSeconds + " segundos</yellow>");
        // Pool stats will be shown at the end

        startTime = System.currentTimeMillis();

        // Crear tareas asíncronas concurrentes
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                simulateUserOperations(userId, operationsPerUser);
            });
        }

        // Programar finalización del test
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            endBenchmark();
        }, testDurationSeconds * 20L); // 20 ticks por segundo
    }

    /**
     * Simula operaciones de un usuario
     */
    private void simulateUserOperations(int userId, int operationsCount) {
        UUID playerUUID = new UUID(1234567890L + userId, 9876543210L);
        String playerName = "BenchmarkPlayer" + userId;

        // Crear jugador en la base de datos usando el provider directamente
        if (economyManager.getAsyncDataProvider() != null) {
            economyManager.getAsyncDataProvider().createPlayer(playerUUID, "default")
                    .thenCompose(ignored -> runOperations(userId, playerUUID, operationsCount))
                    .exceptionally(throwable -> {
                        failedOperations.incrementAndGet();
                        if (plugin != null) {
                            plugin.getLogger().warning(
                                    "Error en operaciones del usuario " + userId + ": " + throwable.getMessage());
                        }
                        return null;
                    });
        } else {
            // Fallback: ejecutar operaciones sin crear jugador
            runOperations(userId, playerUUID, operationsCount);
        }
    }

    /**
     * Ejecuta operaciones simulando uso real
     */
    private CompletableFuture<Void> runOperations(int userId, UUID playerUUID, int operationsCount) {
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = new CompletableFuture[operationsCount];

        for (int i = 0; i < operationsCount; i++) {
            final int operationId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                long operationStart = System.nanoTime();

                try {
                    // Simular diferentes tipos de operaciones
                    int operationType = ThreadLocalRandom.current().nextInt(4);

                    switch (operationType) {
                        case 0:
                            // Obtener balance
                            if (economyManager.getAsyncDataProvider() != null) {
                                economyManager.getAsyncDataProvider().getBalance(playerUUID, "default")
                                        .thenAccept(balance -> {
                                            long latency = System.nanoTime() - operationStart;
                                            successfulOperations.incrementAndGet();
                                            totalLatency.addAndGet(latency / 1_000_000); // Convertir a milisegundos
                                        })
                                        .join();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                            break;

                        case 1:
                            // Añadir dinero
                            double amountToAdd = ThreadLocalRandom.current().nextDouble(10, 100);
                            if (economyManager.getAsyncDataProvider() != null) {
                                economyManager.getAsyncDataProvider().addBalance(playerUUID, amountToAdd, "default")
                                        .thenAccept(success -> {
                                            long latency = System.nanoTime() - operationStart;
                                            if (success) {
                                                successfulOperations.incrementAndGet();
                                                totalLatency.addAndGet(latency / 1_000_000);
                                            } else {
                                                failedOperations.incrementAndGet();
                                            }
                                        })
                                        .join();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                            break;

                        case 2:
                            // Quitar dinero
                            double amountToRemove = ThreadLocalRandom.current().nextDouble(5, 50);
                            if (economyManager.getAsyncDataProvider() != null) {
                                economyManager.getAsyncDataProvider()
                                        .removeBalance(playerUUID, amountToRemove, "default")
                                        .thenAccept(success -> {
                                            long latency = System.nanoTime() - operationStart;
                                            if (success) {
                                                successfulOperations.incrementAndGet();
                                                totalLatency.addAndGet(latency / 1_000_000);
                                            } else {
                                                failedOperations.incrementAndGet();
                                            }
                                        })
                                        .join();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                            break;

                        case 3:
                            // Verificar fondos suficientes
                            double checkAmount = ThreadLocalRandom.current().nextDouble(1, 200);
                            if (economyManager.getAsyncDataProvider() != null) {
                                economyManager.getAsyncDataProvider()
                                        .hasEnoughBalance(playerUUID, checkAmount, "default")
                                        .thenAccept(hasEnough -> {
                                            long latency = System.nanoTime() - operationStart;
                                            successfulOperations.incrementAndGet();
                                            totalLatency.addAndGet(latency / 1_000_000);
                                        })
                                        .join();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                            break;
                    }
                } catch (Exception e) {
                    failedOperations.incrementAndGet();
                    if (plugin != null) {
                        plugin.getLogger().warning(
                                "Error en operación " + operationId + " del usuario " + userId + ": " + e.getMessage());
                    }
                }
            });
        }

        return CompletableFuture.allOf(futures);
    }

    /**
     * Finaliza el benchmark y muestra resultados
     */
    private void endBenchmark() {
        endTime = System.currentTimeMillis();
        isRunning = false;
        benchmarkCompleted = true;

        long totalOperations = successfulOperations.get() + failedOperations.get();
        double successRate = totalOperations > 0 ? (double) successfulOperations.get() / totalOperations * 100 : 0;
        double operationsPerSecond = totalOperations > 0 ? totalOperations / ((endTime - startTime) / 1000.0) : 0;
        long averageLatency = totalOperations > 0 ? totalLatency.get() / totalOperations : 0;

        MessageUtils.sendConsoleMessage("<green>🏁 Benchmark finalizado</green>");
        MessageUtils.sendConsoleMessage("<aqua>Duración: " + ((endTime - startTime) / 1000) + " segundos</aqua>");
        MessageUtils.sendConsoleMessage("<aqua>Operaciones totales: " + totalOperations + "</aqua>");
        MessageUtils.sendConsoleMessage("<aqua>Operaciones exitosas: " + successfulOperations.get() + "</aqua>");
        MessageUtils.sendConsoleMessage("<aqua>Operaciones fallidas: " + failedOperations.get() + "</aqua>");
        MessageUtils.sendConsoleMessage("<aqua>Tasa de éxito: " + String.format("%.2f", successRate) + "%</aqua>");
        MessageUtils.sendConsoleMessage(
                "<aqua>Operaciones por segundo: " + String.format("%.2f", operationsPerSecond) + "</aqua>");
        MessageUtils.sendConsoleMessage("<aqua>Latencia promedio: " + averageLatency + "ms</aqua>");

        // Mostrar estadísticas del pool
        // Pool stats will be shown via MySQLAsyncConnection if available
        MessageUtils.sendConsoleMessage("<yellow>Benchmark completado exitosamente</yellow>");

        // Recomendaciones basadas en resultados
        if (successRate < 95) {
            MessageUtils.sendConsoleMessage(
                    "<red>⚠️  Advertencia: Baja tasa de éxito. Considera reducir la carga o aumentar el pool.</red>");
        }

        if (averageLatency > 100) {
            MessageUtils.sendConsoleMessage(
                    "<yellow>⚠️  Latencia alta detectada. Verifica la configuración de la base de datos.</yellow>");
        }

        if (operationsPerSecond > 1000) {
            MessageUtils
                    .sendConsoleMessage("<green>✅ Excelente rendimiento: Más de 1000 operaciones por segundo!</green>");
        }
    }

    /**
     * Obtiene el estado actual del benchmark
     */
    public String getBenchmarkStatus() {
        if (!isRunning && !benchmarkCompleted) {
            return "No hay benchmark en ejecución";
        }

        if (isRunning) {
            return "Benchmark en ejecución - Operaciones exitosas: " + successfulOperations.get() +
                    ", Fallidas: " + failedOperations.get();
        }

        return "Benchmark finalizado";
    }

    /**
     * Verifica si el benchmark está actualmente en ejecución
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Cancela el benchmark actual
     */
    public void cancelBenchmark() {
        if (isRunning) {
            isRunning = false;
            MessageUtils.sendConsoleMessage("<yellow> Benchmark cancelado manualmente</yellow>");
        }
    }

    /**
     * Métodos de conveniencia para pruebas rápidas
     */
    public void quickBenchmark() {
        MessageUtils.sendConsoleMessage(
                "<green>🚀 Iniciando benchmark rápido (10 usuarios, 100 operaciones cada uno)</green>");
        startBenchmark(10, 100, 30);
    }

    public void stressBenchmark() {
        MessageUtils.sendConsoleMessage(
                "<red>🔥 Iniciando benchmark de estrés (100 usuarios, 1000 operaciones cada uno)</red>");
        startBenchmark(100, 1000, 60);
    }
}