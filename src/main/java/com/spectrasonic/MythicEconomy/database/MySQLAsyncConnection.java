package com.spectrasonic.MythicEconomy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.utils.MessageUtils;
import com.spectrasonic.MythicEconomy.utils.AsyncUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Conexión MySQL asíncrona con HikariCP para alto rendimiento y concurrencia.
 * Basado en PaperMC recomendaciones para operaciones no bloqueantes.
 */
@Slf4j
public class MySQLAsyncConnection {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    private boolean initialized = false;
    private ScheduledExecutorService hikariExecutor;

    // Configuración de pool
    @Getter
    private String host;
    @Getter
    private int port;
    @Getter
    private String database;
    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private boolean useSSL;
    @Getter
    private int maximumPoolSize;
    @Getter
    private int minimumIdle;
    @Getter
    private int connectionTimeout;
    @Getter
    private int idleTimeout;
    @Getter
    private int maxLifetime;

    public MySQLAsyncConnection(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    /**
     * Carga la configuración desde el archivo de configuración del plugin
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();

        host = config.getString("database.mysql.host", "localhost");
        port = config.getInt("database.mysql.port", 3306);
        database = config.getString("database.mysql.database", "minecraft");
        username = config.getString("database.mysql.username", "root");
        password = config.getString("database.mysql.password", "");
        useSSL = config.getBoolean("database.mysql.use-ssl", false);

        // Configuración de HikariCP
        maximumPoolSize = config.getInt("database.mysql.pool.maximum-pool-size", 50);
        minimumIdle = config.getInt("database.mysql.pool.minimum-idle", 10);
        connectionTimeout = config.getInt("database.mysql.pool.connection-timeout", 30000);
        idleTimeout = config.getInt("database.mysql.pool.idle-timeout", 600000);
        maxLifetime = config.getInt("database.mysql.pool.max-lifetime", 1800000);
    }

    /**
     * Inicializa el pool de conexiones de forma síncrona (para uso durante startup)
     */
    public boolean initializeSync() throws Exception {
        if (initialized) {
            log.warn("MySQLAsyncConnection ya fue inicializado");
            return true;
        }

        // Create executor for HikariCP background tasks using server's async scheduler
        hikariExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "HikariCP-" + plugin.getName());
            t.setDaemon(true);
            return t;
        });

        HikariConfig config = new HikariConfig();
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false",
                host, port, database, useSSL);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(60000); // 1 min
        config.setScheduledExecutor(hikariExecutor); // Set custom executor for background tasks

        // Configuración para alto rendimiento
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");

        dataSource = new HikariDataSource(config);

        // Crear tablas si no existen
        createTablesIfNotExistsSync();

        initialized = true;
        MessageUtils.sendConsoleMessage("<green>Conexión MySQL asíncrona inicializada exitosamente</green>");
        log.info("MySQL pool initialized: max={} idle={} timeout={}",
                maximumPoolSize, minimumIdle, connectionTimeout);

        return true;
    }

    /**
     * Inicializa el pool de conexiones de forma asíncrona
     */
    public CompletableFuture<Boolean> initialize() {
        return AsyncUtils.supplyAsync(plugin, () -> {
            try {
                if (initialized) {
                    log.warn("MySQLAsyncConnection ya fue inicializado");
                    return true;
                }

                // Create executor for HikariCP background tasks using server's async scheduler
                hikariExecutor = Executors.newScheduledThreadPool(2, r -> {
                    Thread t = new Thread(r, "HikariCP-" + plugin.getName());
                    t.setDaemon(true);
                    return t;
                });

                HikariConfig config = new HikariConfig();
                String jdbcUrl = String.format(
                        "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false",
                        host, port, database, useSSL);

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(maximumPoolSize);
                config.setMinimumIdle(minimumIdle);
                config.setConnectionTimeout(connectionTimeout);
                config.setIdleTimeout(idleTimeout);
                config.setMaxLifetime(maxLifetime);
                config.setLeakDetectionThreshold(60000); // 1 min
                config.setScheduledExecutor(hikariExecutor); // Set custom executor for background tasks

                // Configuración para alto rendimiento
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");

                dataSource = new HikariDataSource(config);

                // Crear tablas si no existen
                createTablesIfNotExists();

                initialized = true;
                MessageUtils.sendConsoleMessage("<green>Conexión MySQL asíncrona inicializada exitosamente</green>");
                log.info("MySQL pool initialized: max={} idle={} timeout={}",
                        maximumPoolSize, minimumIdle, connectionTimeout);

                return true;

            } catch (Exception e) {
                log.error("Error al inicializar MySQLAsyncConnection", e);
                MessageUtils.sendConsoleMessage("<red>Error al conectar con MySQL: " + e.getMessage() + "</red>");
                return false;
            }
        });
    }

    /**
     * Obtiene una conexión del pool de forma síncrona (para uso durante
     * inicialización)
     */
    public Optional<Connection> getConnectionSync() {
        try {
            if (!initialized || dataSource == null || dataSource.isClosed()) {
                return Optional.empty();
            }
            return Optional.of(dataSource.getConnection());
        } catch (SQLException e) {
            log.error("Error al obtener conexión del pool", e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene una conexión del pool de forma asíncrona
     */
    public CompletableFuture<Connection> getConnection() {
        return AsyncUtils.supplyAsync(plugin, () -> {
            try {
                if (!initialized || dataSource == null || dataSource.isClosed()) {
                    throw new SQLException("DataSource no inicializado o cerrado");
                }
                return dataSource.getConnection();
            } catch (SQLException e) {
                log.error("Error al obtener conexión del pool", e);
                throw new RuntimeException("No se pudo obtener conexión de MySQL", e);
            }
        });
    }

    /**
     * Verifica si la conexión está activa de forma asíncrona
     */
    public CompletableFuture<Boolean> isConnected() {
        return AsyncUtils.supplyAsync(plugin, () -> {
            try {
                if (!initialized || dataSource == null) {
                    return false;
                }

                try (Connection conn = dataSource.getConnection()) {
                    return conn.isValid(5); // 5 segundos timeout
                }
            } catch (SQLException e) {
                log.warn("Conexión MySQL no válida", e);
                return false;
            }
        });
    }

    /**
     * Verifica si el pool ha sido inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtiene el DataSource de HikariCP
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Cierra el pool de forma asíncrona
     */
    public CompletableFuture<Void> shutdown() {
        return AsyncUtils.runAsync(plugin, () -> {
            try {
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                    initialized = false;
                    MessageUtils.sendConsoleMessage("<yellow>Conexión MySQL asíncrona cerrada</yellow>");
                    log.info("MySQL pool shutdown completed");
                }

                // Shutdown the custom executor
                if (hikariExecutor != null && !hikariExecutor.isShutdown()) {
                    hikariExecutor.shutdown();
                    log.info("HikariCP executor shutdown completed");
                }
            } catch (Exception e) {
                log.error("Error al cerrar MySQL pool", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Crea las tablas necesarias si no existen (versión síncrona)
     */
    private void createTablesIfNotExistsSync() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            // Tabla de balances
            String createBalancesTable = """
                    CREATE TABLE IF NOT EXISTS player_balances (
                        player_uuid VARCHAR(36) NOT NULL,
                        currency_id VARCHAR(50) NOT NULL,
                        balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (player_uuid, currency_id),
                        INDEX idx_currency_id (currency_id),
                        INDEX idx_player_uuid (player_uuid)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """;

            // Tabla de nombres de jugadores
            String createNamesTable = """
                    CREATE TABLE IF NOT EXISTS player_names (
                        player_uuid VARCHAR(36) NOT NULL,
                        player_name VARCHAR(64) NOT NULL,
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (player_uuid),
                        INDEX idx_player_name (player_name)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """;

            try (var stmt = conn.createStatement()) {
                stmt.execute(createBalancesTable);
                stmt.execute(createNamesTable);
                log.info("Tablas MySQL verificadas/creadas exitosamente");
            }
        }
    }

    /**
     * Crea las tablas necesarias si no existen
     */
    private void createTablesIfNotExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            // Tabla de balances
            String createBalancesTable = """
                    CREATE TABLE IF NOT EXISTS player_balances (
                        player_uuid VARCHAR(36) NOT NULL,
                        currency_id VARCHAR(50) NOT NULL,
                        balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (player_uuid, currency_id),
                        INDEX idx_currency_id (currency_id),
                        INDEX idx_player_uuid (player_uuid)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """;

            // Tabla de nombres de jugadores
            String createNamesTable = """
                    CREATE TABLE IF NOT EXISTS player_names (
                        player_uuid VARCHAR(36) NOT NULL,
                        player_name VARCHAR(64) NOT NULL,
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (player_uuid),
                        INDEX idx_player_name (player_name)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """;

            try (var stmt = conn.createStatement()) {
                stmt.execute(createBalancesTable);
                stmt.execute(createNamesTable);
                log.info("Tablas MySQL verificadas/creadas exitosamente");
            }
        }
    }

    /**
     * Recarga la configuración y reconecta si es necesario
     */
    public CompletableFuture<Boolean> reloadConfiguration() {
        return AsyncUtils.supplyAsync(plugin, () -> {
            try {
                loadConfiguration();

                if (initialized) {
                    // Esperar a que se cierre y se inicialice de forma síncrona dentro del task
                    shutdown().get();
                    return initialize().get();
                } else {
                    return true;
                }
            } catch (Exception e) {
                log.error("Error al recargar configuración MySQL", e);
                return false;
            }
        });
    }

    /**
     * Obtiene estadísticas del pool (para monitoreo)
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "Pool no inicializado";
        }

        return String.format("Pool stats - Active: %d, Idle: %d, Total: %d, Pending: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}