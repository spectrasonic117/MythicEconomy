package com.spectrasonic.MythicEconomy.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.utils.MessageUtils;

import lombok.Getter;

/**
 * Clase para manejar la conexión a una base de datos MySQL externa
 * Implementa el esquema de tabla única para múltiples monedas
 */
public class MySQLConnection {

    @Getter
    private Connection connection;
    private final JavaPlugin plugin;

    // Configuración
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private boolean useSSL;

    public MySQLConnection(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    /**
     * Carga la configuración de MySQL desde el archivo de configuración del plugin
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();

        host = config.getString("database.mysql.host", "localhost");
        port = config.getInt("database.mysql.port", 3306);
        database = config.getString("database.mysql.database", "minecraft");
        username = config.getString("database.mysql.username", "root");
        password = config.getString("database.mysql.password", "");
        useSSL = config.getBoolean("database.mysql.use-ssl", false);
    }

    /**
     * Establece la conexión a la base de datos MySQL
     */
    public boolean connect() {
        try {
            // Si ya hay una conexión activa, verificar que funcione
            if (connection != null && !connection.isClosed() && connection.isValid(2)) {
                return true;
            }

            // Cerrar conexión anterior si existe
            if (connection != null && !connection.isClosed()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    plugin.getLogger().warning("Error al cerrar conexión anterior: " + e.getMessage());
                }
            }

            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&maxReconnects=10&initialTimeout=10",
                    host, port, database, useSSL);

            plugin.getLogger().info("Conectando a MySQL en " + host + ":" + port + "/" + database);
            connection = DriverManager.getConnection(url, username, password);

            // Establecer timeout para la conexión
            connection.setNetworkTimeout(null, 30000); // 30 segundos

            // Crear la tabla si no existe
            createTableIfNotExists();

            MessageUtils.sendConsoleMessage("<green>Conexión exitosa a MySQL</green>");
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("No se pudo conectar a MySQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                MessageUtils.sendConsoleMessage("<yellow>Conexión a MySQL cerrada</yellow>");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al cerrar conexión MySQL: " + e.getMessage());
        }
    }

    /**
     * Crea la tabla player_balances si no exists
     * Esta tabla almacena todos los balances de todos los jugadores para todas las
     * monedas
     */
    private void createTableIfNotExists() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS player_balances (
                    player_uuid VARCHAR(36) NOT NULL,
                    currency_id VARCHAR(50) NOT NULL,
                    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_uuid, currency_id),
                    INDEX idx_currency_id (currency_id),
                    INDEX idx_player_uuid (player_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            plugin.getLogger().info("Tabla player_balances verificada/creada exitosamente");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al crear tabla player_balances: " + e.getMessage());
        }
    }

    /**
     * Verifica si la conexión está activa
     */
    public boolean isConnected() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            // Usar un timeout más corto para verificar la conexión
            return connection.isValid(2);
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al verificar conexión MySQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recarga la configuración desde el archivo
     */
    public void reloadConfiguration() {
        loadConfiguration();

        // Si hay una conexión activa, intentar reconectar
        if (isConnected()) {
            disconnect();
            connect();
        }
    }

    /**
     * Obtiene el balance total de dinero en circulación para todas las monedas
     */
    public double getTotalMoneyAllCurrencies() {
        // Intentar reconectar si no hay conexión
        if (!isConnected() && !connect()) {
            return 0.0;
        }

        try (java.sql.Statement stmt = connection.createStatement()) {
            String query = "SELECT SUM(balance) as total FROM player_balances";
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener dinero total: " + e.getMessage());
            // Intentar reconectar para la próxima vez
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                // Ignorar error al cerrar
            }
            connection = null;
        }

        return 0.0;
    }

    /**
     * Obtiene el número total de jugadores únicos registrados
     */
    public long getTotalUniquePlayers() {
        // Intentar reconectar si no hay conexión
        if (!isConnected() && !connect()) {
            return 0;
        }

        try (java.sql.Statement stmt = connection.createStatement()) {
            String query = "SELECT COUNT(DISTINCT player_uuid) as total FROM player_balances";
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener total de jugadores: " + e.getMessage());
            // Intentar reconectar para la próxima vez
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                // Ignorar error al cerrar
            }
            connection = null;
        }

        return 0;
    }
}