package com.spectrasonic.MythicEconomy.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

// Proveedor de economía que utiliza MongoDB como almacenamiento
@RequiredArgsConstructor
public class MongoDBEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final MongoDBConnection mongoConnection;

    // Obtiene el saldo de un jugador desde MongoDB
    public double getBalance(UUID playerUUID) {
        // Para compatibilidad, usa la moneda por defecto
        return getBalance(playerUUID, "default");
    }

    // Obtiene el saldo de un jugador en una moneda específica desde MongoDB
    public double getBalance(UUID playerUUID, String currencyId) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return 0.0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();
            Document playerDoc = collection.find(Filters.and(
                    Filters.eq("uuid", playerUUID.toString()),
                    Filters.eq("currencyId", currencyId))).first();

            if (playerDoc != null) {
                return playerDoc.getDouble("balance");
            } else {
                // Jugador nuevo, devolver saldo inicial de la moneda
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
        // Para compatibilidad, usa la moneda por defecto
        setBalance(playerUUID, amount, "default");
    }

    // Establece el saldo de un jugador en una moneda específica en MongoDB
    public void setBalance(UUID playerUUID, double amount, String currencyId) {
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
                    .append("currencyId", currencyId)
                    .append("balance", amount)
                    .append("lastUpdated", System.currentTimeMillis());

            // Usar upsert para crear o actualizar
            collection.replaceOne(
                    Filters.and(
                            Filters.eq("uuid", playerUUID.toString()),
                            Filters.eq("currencyId", currencyId)),
                    playerDoc,
                    new ReplaceOptions().upsert(true));

        } catch (Exception e) {
            plugin.getLogger().severe("Error al establecer saldo en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Agrega dinero al saldo de un jugador en MongoDB
    public boolean addBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return addBalance(playerUUID, amount, "default");
    }

    // Agrega dinero al saldo de un jugador en una moneda específica en MongoDB
    public boolean addBalance(UUID playerUUID, double amount, String currencyId) {
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
                    Filters.and(
                            Filters.eq("uuid", playerUUID.toString()),
                            Filters.eq("currencyId", currencyId)),
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
        // Para compatibilidad, usa la moneda por defecto
        return removeBalance(playerUUID, amount, "default");
    }

    // Reduce dinero del saldo de un jugador en una moneda específica en MongoDB
    public boolean removeBalance(UUID playerUUID, double amount, String currencyId) {
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
            Document playerDoc = collection.find(Filters.and(
                    Filters.eq("uuid", playerUUID.toString()),
                    Filters.eq("currencyId", currencyId))).first();
            if (playerDoc == null) {
                return false; // Jugador no existe
            }

            double currentBalance = playerDoc.getDouble("balance");
            if (currentBalance < amount) {
                return false; // Saldo insuficiente
            }

            // Decrementar el saldo usando operación atómica
            long modifiedCount = collection.updateOne(
                    Filters.and(
                            Filters.eq("uuid", playerUUID.toString()),
                            Filters.eq("currencyId", currencyId)),
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
        // Para compatibilidad, usa la moneda por defecto
        return hasEnoughBalance(playerUUID, amount, "default");
    }

    // Verifica si un jugador tiene suficiente dinero en una moneda específica en
    // MongoDB
    public boolean hasEnoughBalance(UUID playerUUID, double amount, String currencyId) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return false;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();
            Document playerDoc = collection.find(Filters.and(
                    Filters.eq("uuid", playerUUID.toString()),
                    Filters.eq("currencyId", currencyId))).first();

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
        // Para compatibilidad, crea con la moneda por defecto
        createPlayer(playerUUID, "default");
    }

    // Crea un jugador nuevo en MongoDB con saldo inicial para una moneda específica
    public void createPlayer(UUID playerUUID, String currencyId) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return;
        }

        try {
            double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
            setBalance(playerUUID, startingBalance, currencyId);

            plugin.getLogger().info("Jugador creado en MongoDB para moneda " + currencyId + ": " + playerUUID);

        } catch (Exception e) {
            plugin.getLogger().severe("Error al crear jugador en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos heredados de la interfaz para compatibilidad hacia atrás
    // Nota: Estos métodos fueron eliminados de la interfaz para evitar conflictos
    // pero se mantienen aquí para compatibilidad con implementaciones anteriores

    @Deprecated
    public long getTotalPlayers() {
        // Para compatibilidad, cuenta solo la moneda por defecto
        return getTotalPlayers("default");
    }

    @Deprecated
    public double getTotalMoney() {
        // Para compatibilidad, suma solo la moneda por defecto
        return getTotalMoney("default");
    }

    // Obtiene el total de jugadores para una moneda específica
    public long getTotalPlayers(String currencyId) {
        if (!mongoConnection.isConnected()) {
            return 0;
        }

        try {
            return mongoConnection.getCollection().countDocuments(Filters.eq("currencyId", currencyId));
        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener total de jugadores: " + e.getMessage());
            return 0;
        }
    }

    // Obtiene el dinero total en circulación para una moneda específica
    public double getTotalMoney(String currencyId) {
        if (!mongoConnection.isConnected()) {
            return 0.0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Usar agregación para sumar todos los balances de la moneda específica
            Document result = collection.aggregate(java.util.Arrays.asList(
                    new Document("$match", new Document("currencyId", currencyId)),
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

    @Override
    public long getTotalUniquePlayers() {
        if (!mongoConnection.isConnected()) {
            return 0;
        }

        try {
            // Contar jugadores únicos (distintos UUIDs)
            Document result = mongoConnection.getCollection().aggregate(java.util.Arrays.asList(
                    new Document("$group", new Document("_id", null)
                            .append("uniquePlayers", new Document("$addToSet", "$uuid"))),
                    new Document("$project", new Document("count", new Document("$size", "$uniquePlayers")))))
                    .first();

            if (result != null) {
                return result.getLong("count");
            }

            return 0;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener total de jugadores únicos: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public double getTotalMoneyAllCurrencies() {
        if (!mongoConnection.isConnected()) {
            return 0.0;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Usar agregación para sumar todos los balances de todas las monedas
            Document result = collection.aggregate(java.util.Arrays.asList(
                    new Document("$group", new Document("_id", null)
                            .append("totalBalance", new Document("$sum", "$balance")))))
                    .first();

            if (result != null) {
                return result.getDouble("totalBalance");
            }

            return 0.0;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener dinero total de todas las monedas: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public Object[][] getTopBalances(String currencyId, int limit) {
        if (!mongoConnection.isConnected()) {
            return new Object[0][0];
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();

            // Obtener top balances ordenados por saldo descendente
            java.util.List<Document> results = collection.find(Filters.eq("currencyId", currencyId))
                    .sort(new Document("balance", -1))
                    .limit(limit)
                    .into(new java.util.ArrayList<>());

            Object[][] topBalances = new Object[results.size()][2];
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                topBalances[i][0] = doc.getString("uuid"); // UUID como String
                topBalances[i][1] = doc.getDouble("balance");
            }

            return topBalances;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener top balances desde MongoDB: " + e.getMessage());
            e.printStackTrace();
            return new Object[0][0];
        }
    }

    @Override
    public Object[][] getTopBalancesWithNames(String currencyId, int limit) {
        if (!mongoConnection.isConnected()) {
            return new Object[0][0];
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection();
            MongoCollection<Document> namesCollection = mongoConnection.getDatabase().getCollection("player_names");

            // Obtener top balances
            List<Document> results = collection.find(Filters.eq("currencyId", currencyId))
                    .sort(new Document("balance", -1))
                    .limit(limit)
                    .into(new ArrayList<>());

            Object[][] topBalances = new Object[results.size()][3];
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                String uuid = doc.getString("uuid");
                
                // Obtener nombre del jugador
                Document nameDoc = namesCollection.find(Filters.eq("uuid", uuid)).first();
                String playerName = nameDoc != null ? nameDoc.getString("name") : "Unknown";
                
                topBalances[i][0] = uuid; // UUID como String
                topBalances[i][1] = playerName; // Nombre del jugador
                topBalances[i][2] = doc.getDouble("balance");
            }

            return topBalances;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener top balances con nombres desde MongoDB: " + e.getMessage());
            e.printStackTrace();
            return new Object[0][0];
        }
    }

    @Override
    public void updatePlayerName(UUID playerUUID, String playerName) {
        if (!mongoConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MongoDB");
            return;
        }

        if (playerName == null || playerName.trim().isEmpty()) {
            return;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection("player_names");

            Document nameDoc = new Document()
                    .append("uuid", playerUUID.toString())
                    .append("name", playerName)
                    .append("lastUpdated", System.currentTimeMillis());

            // Usar upsert para crear o actualizar
            collection.replaceOne(
                    Filters.eq("uuid", playerUUID.toString()),
                    nameDoc,
                    new ReplaceOptions().upsert(true));

        } catch (Exception e) {
            plugin.getLogger().severe("Error al actualizar nombre de jugador en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        if (!mongoConnection.isConnected()) {
            return null;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection("player_names");
            Document nameDoc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
            
            return nameDoc != null ? nameDoc.getString("name") : null;

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener nombre de jugador desde MongoDB: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs) {
        Map<UUID, String> names = new HashMap<>();
        
        if (!mongoConnection.isConnected()) {
            return names;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection("player_names");
            
            List<String> uuidStrings = new ArrayList<>();
            for (UUID uuid : playerUUIDs) {
                uuidStrings.add(uuid.toString());
            }

            if (uuidStrings.isEmpty()) {
                return names;
            }

            List<Document> results = collection.find(Filters.in("uuid", uuidStrings))
                    .into(new ArrayList<>());

            for (Document doc : results) {
                UUID uuid = UUID.fromString(doc.getString("uuid"));
                String name = doc.getString("name");
                names.put(uuid, name);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error al obtener nombres de jugadores desde MongoDB: " + e.getMessage());
            e.printStackTrace();
        }

        return names;
    }

    @Override
    public void syncPlayerNames(Map<UUID, String> activePlayers) {
        if (!mongoConnection.isConnected()) {
            return;
        }

        try {
            MongoCollection<Document> collection = mongoConnection.getCollection("player_names");

            for (Map.Entry<UUID, String> entry : activePlayers.entrySet()) {
                UUID playerUUID = entry.getKey();
                String playerName = entry.getValue();
                
                if (playerName != null && !playerName.trim().isEmpty()) {
                    Document nameDoc = new Document()
                            .append("uuid", playerUUID.toString())
                            .append("name", playerName)
                            .append("lastUpdated", System.currentTimeMillis());

                    collection.replaceOne(
                            Filters.eq("uuid", playerUUID.toString()),
                            nameDoc,
                            new ReplaceOptions().upsert(true));
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error al sincronizar nombres de jugadores en MongoDB: " + e.getMessage());
            e.printStackTrace();
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