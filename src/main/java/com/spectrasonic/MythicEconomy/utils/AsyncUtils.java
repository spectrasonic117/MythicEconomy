package com.spectrasonic.MythicEconomy.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utilidad para manejar operaciones asíncronas de forma segura en plugins de Bukkit/Paper
 * Evita el problema "Executor can not be null" usando directamente el scheduler del servidor
 */
public class AsyncUtils {

    /**
     * Ejecuta una tarea asíncrona y devuelve un CompletableFuture
     * @param plugin El plugin que ejecuta la tarea
     * @param supplier La tarea a ejecutar
     * @param <T> El tipo de retorno
     * @return CompletableFuture con el resultado
     */
    public static <T> CompletableFuture<T> supplyAsync(JavaPlugin plugin, Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    /**
     * Ejecuta una tarea asíncrona que no retorna valor
     * @param plugin El plugin que ejecuta la tarea
     * @param runnable La tarea a ejecutar
     * @return CompletableFuture que se completa cuando la tarea termina
     */
    public static CompletableFuture<Void> runAsync(JavaPlugin plugin, Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
}