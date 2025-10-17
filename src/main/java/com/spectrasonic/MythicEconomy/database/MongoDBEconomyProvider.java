package com.spectrasonic.MythicEconomy.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

// Proveedor de economía que utiliza MongoDB como almacenamiento
@RequiredArgsConstructor
public class MongoDBEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final MongoDBConnection mongoConnection;

    // Obtiene el saldo de un jugador desde MongoDB
    public double getBalance(UUID playerUUID) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return 0.0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();
            Document playerDoc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();

            if (playerDoc != null) {
                return playerDoc.getDouble("balance");
            } else {
                // Jugador nuevo, devolver saldo inicial
                double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
                return startingBalance;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener saldo desde MongoDB: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // Establece el saldo de un jugador en MongoDB
    public void setBalance(UUID playerUUID, double amount) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return;
        }

        if (amount < 0) {
            amount = 0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            Document playerDoc = new Document()
                    .append("uuid", playerUUID.toString())
                    .append("balance", amount)
                    .append("lastUpdated", System.currentTimeMillis());

            // Usar upsert para crear o actualizar
            collection.replaceOne(
                    Filters.eq("uuid", playerUUID.toString()),
                    playerDoc,
                    new ReplaceOptions().upsert(true));

        } catch (Exception e) {
            plugin.getLogger().severe("Error al establecer saldo en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Agrega dinero al saldo de un jugador en MongoDB
    public boolean addBalance(UUID playerUUID, double amount) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return false;
        }

        if (amount <= 0) {
            return false;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Incrementar el saldo usando operación atómica
            long modifiedCount = collection.updateOne(
                    Filters.eq("uuid", playerUUID.toString()),
                    Updates.combine(
                            Updates.inc("balance", amount),
                            Updates.set("lastUpdated", System.currentTimeMillis())),
                    new UpdateOptions().upsert(true)).getModifiedCount();

            return modifiedCount > 0;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al agregar saldo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Reduce dinero del saldo de un jugador en MongoDB
    public boolean removeBalance(UUID playerUUID, double amount) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return false;
        }

        if (amount <= 0) {
            return false;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Obtener saldo actual para verificar
            Document playerDoc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
            if (playerDoc == null) {
                return false; // Jugador no existe
            }

            double currentBalance = playerDoc.getDouble("balance");
            if (currentBalance < amount) {
                return false; // Saldo insuficiente
            }

            // Decrementar el saldo usando operación atómica
            long modifiedCount = collection.updateOne(
                    Filters.eq("uuid", playerUUID.toString()),
                    Updates.combine(
                            Updates.inc("balance", -amount),
                            Updates.set("lastUpdated", System.currentTimeMillis())))
                    .getModifiedCount();

            return modifiedCount > 0;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al reducir saldo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Verifica si un jugador tiene suficiente dinero en MongoDB
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return false;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();
            Document playerDoc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();

            if (playerDoc != null) {
                double currentBalance = playerDoc.getDouble("balance");
                return currentBalance >= amount;
            }

            return false;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al verificar saldo en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Crea un jugador nuevo en MongoDB con saldo inicial
    public void createPlayer(UUID playerUUID) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return;
        }

        try {
            double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
            setBalance(playerUUID, startingBalance);

            plugin.getLogger().info("Jugador creado en MongoDB: " + playerUUID);

        } catch (Exception e) {
            plugin.getLogger().severe("Error al crear jugador en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Obtiene estadísticas de la colección
    public long getTotalPlayers() {
        if (!mongoConnection.isConnected()) {
            return 0;
        }

        try {
            return mongoConnection.getCollection().countDocuments();
        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener total de jugadores: " + e.getMessage());
            return 0;
        }
    }

    // Obtiene el dinero total en circulación
    public double getTotalMoney() {
        if (!mongoConnection.isConnected()) {
            return 0.0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Usar agregación para sumar todos los balances
            Document result = collection.aggregate(java.util.Arrays.asList(
                    new Document("$group", new Document("_id", null)
                            .append("totalBalance", new Document("$sum", "$balance")))))
                    .first();

            if (result != null) {
                return result.getDouble("totalBalance");
            }

            return 0.0;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener dinero total: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // Limpia todos los datos de la colección (útil para mantenimiento)
    public void clearAllData() {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return;
        }

        try {
            mongoConnection.getCollection().drop();
            plugin.getLogger().info("Todos los datos de economía eliminados de MongoDB");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al limpiar datos de MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        // MongoDB guarda automáticamente, no necesitamos hacer nada
    }

    @Override
    public void load() {
        // MongoDB carga automáticamente cuando se necesita, no necesitamos hacer nada
    }

    @Override
    public boolean isAvailable() {
        return mongoConnection != null && mongoConnection.isConnected();
    }
}