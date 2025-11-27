package com.spectrasonic.MythicEconomy.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

// Clase para manejar la conexión con MongoDB

@Getter
public class MongoDBConnection {

    private final JavaPlugin plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private boolean connected = false;

    // Configuración de conexión
    private String connectionString;
    private String databaseName;
    private String collectionName;
    private int connectionTimeout;
    private int maxPoolSize;
    private int minPoolSize;

    public MongoDBConnection(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loadConfiguration();
    }

    // Carga la configuración de MongoDB desde el config.yml
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();

        this.connectionString = config.getString("database.mongodb.connection-string", "mongodb://localhost:27017");
        this.databaseName = config.getString("database.mongodb.database", "MythicEconomy");
        this.collectionName = config.getString("database.mongodb.collection", "player_economy");
        this.connectionTimeout = config.getInt("database.mongodb.connection.timeout", 30);
        this.maxPoolSize = config.getInt("database.mongodb.connection.max-pool-size", 10);
        this.minPoolSize = config.getInt("database.mongodb.connection.min-pool-size", 5);
    }

    // Establece la conexión con MongoDB
    public boolean connect() {
        try {
            // Configurar opciones de conexión
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .applyToConnectionPoolSettings(builder -> builder.maxSize(maxPoolSize)
                            .minSize(minPoolSize))
                    .applyToSocketSettings(builder -> builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS))
                    .build();

            // Crear cliente MongoDB
            this.mongoClient = MongoClients.create(settings);

            // Obtener base de datos y colección
            this.database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection(collectionName);

            // Probar la conexión
            database.runCommand(new Document("ping", 1));

            this.connected = true;
            MessageUtils.sendConsoleMessage("<green>Conexión a MongoDB establecida correctamente.</green>");
            plugin.getLogger()
                    .info("Conectado a MongoDB - Base de datos: " + databaseName + ", Colección: " + collectionName);

            return true;

        } catch (Exception e) {
            this.connected = false;
            MessageUtils.sendConsoleMessage("<red>Error al conectar con MongoDB: " + e.getMessage() + "</red>");
            plugin.getLogger().severe("Error de conexión MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cierra la conexión con MongoDB
    public void disconnect() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                this.connected = false;
                MessageUtils.sendConsoleMessage("<yellow>Conexión a MongoDB cerrada.</yellow>");
            } catch (Exception e) {
                plugin.getLogger().warning("Error al cerrar conexión MongoDB: " + e.getMessage());
            }
        }
    }

    // Verifica si la conexión está activa
    public boolean isConnected() {
        if (!connected || mongoClient == null) {
            return false;
        }

        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            this.connected = false;
            return false;
        }
    }

    // Método para obtener una colección específica por nombre
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    // Recarga la configuración desde el archivo
    public void reloadConfiguration() {
        this.loadConfiguration();

        // Si está conectado, intentar reconectar con nueva configuración
        if (connected) {
            disconnect();
            connect();
        }
    }
}