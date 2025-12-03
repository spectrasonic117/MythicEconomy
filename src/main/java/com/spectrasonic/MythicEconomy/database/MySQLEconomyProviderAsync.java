package com.spectrasonic.MythicEconomy.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;
import com.spectrasonic.MythicEconomy.utils.AsyncUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MySQLEconomyProviderAsync implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final MySQLAsyncConnection asyncConnection;
    private CurrencyManager currencyManager;

    public CompletableFuture<Double> getBalanceAsync(UUID playerUUID) {
        return getBalanceAsync(playerUUID, "default");
    }

    public CompletableFuture<Double> getBalanceAsync(UUID playerUUID, String currencyId) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT balance FROM player_balances WHERE player_uuid = ? AND currency_id = ?";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, currencyId);

                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return rs.getDouble("balance");
                                } else {
                                    // Jugador nuevo, obtener saldo inicial y crearlo en la base de datos
                                    double startingBalance = getCurrencyStartingBalance(currencyId);

                                    // Crear el jugador de forma asíncrona
                                    createPlayer(playerUUID, currencyId);

                                    return startingBalance;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener saldo para {} en moneda {}", playerUUID, currencyId, e);
                        return 0.0;
                    }
                }));
    }

    public CompletableFuture<Boolean> setBalanceAsync(UUID playerUUID, double amount) {
        return setBalanceAsync(playerUUID, amount, "default");
    }

    public CompletableFuture<Boolean> setBalanceAsync(UUID playerUUID, double amount, String currencyId) {
        final double finalAmount = amount < 0 ? 0 : amount;

        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = """
                                INSERT INTO player_balances (player_uuid, currency_id, balance, last_updated)
                                VALUES (?, ?, ?, NOW())
                                ON DUPLICATE KEY UPDATE balance = ?, last_updated = NOW()
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setDouble(3, finalAmount); // Valor para INSERT
                            stmt.setDouble(4, finalAmount); // Valor para UPDATE
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, currencyId);

                            int rowsAffected = stmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (SQLException e) {
                        log.error("Error al establecer saldo para {} en moneda {}", playerUUID, currencyId, e);
                        return false;
                    }
                }));
    }

    public CompletableFuture<Boolean> addBalanceAsync(UUID playerUUID, double amount) {
        return addBalanceAsync(playerUUID, amount, "default");
    }

    public CompletableFuture<Boolean> addBalanceAsync(UUID playerUUID, double amount, String currencyId) {
        if (amount <= 0)
            return CompletableFuture.completedFuture(false);

        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = """
                                INSERT INTO player_balances (player_uuid, currency_id, balance, last_updated)
                                VALUES (?, ?, ?, NOW())
                                ON DUPLICATE KEY UPDATE balance = balance + ?, last_updated = NOW()
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setDouble(3, amount); // Valor para INSERT
                            stmt.setDouble(4, amount); // Incremento para UPDATE
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, currencyId);

                            int rowsAffected = stmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (SQLException e) {
                        log.error("Error al agregar saldo para {} en moneda {}", playerUUID, currencyId, e);
                        return false;
                    }
                }));
    }

    public CompletableFuture<Boolean> removeBalanceAsync(UUID playerUUID, double amount) {
        return removeBalanceAsync(playerUUID, amount, "default");
    }

    public CompletableFuture<Boolean> removeBalanceAsync(UUID playerUUID, double amount, String currencyId) {
        if (amount <= 0)
            return CompletableFuture.completedFuture(false);

        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        // Operación atómica: verificar saldo y actualizar en una sola query
                        String sql = """
                                UPDATE player_balances
                                SET balance = balance - ?, last_updated = NOW()
                                WHERE player_uuid = ? AND currency_id = ? AND balance >= ?
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setDouble(1, amount);
                            stmt.setString(2, playerUUID.toString());
                            stmt.setString(3, currencyId);
                            stmt.setDouble(4, amount); // Condición: saldo >= amount

                            int rowsAffected = stmt.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (SQLException e) {
                        log.error("Error al remover saldo para {} en moneda {}", playerUUID, currencyId, e);
                        return false;
                    }
                }));
    }

    public CompletableFuture<Boolean> hasEnoughBalanceAsync(UUID playerUUID, double amount) {
        return hasEnoughBalanceAsync(playerUUID, amount, "default");
    }

    public CompletableFuture<Boolean> hasEnoughBalanceAsync(UUID playerUUID, double amount, String currencyId) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT balance FROM player_balances WHERE player_uuid = ? AND currency_id = ?";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, currencyId);

                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return rs.getDouble("balance") >= amount;
                                } else {
                                    // Jugador no existe, crearlo y verificar saldo inicial
                                    double startingBalance = getCurrencyStartingBalance(currencyId);

                                    // Crear el jugador de forma asíncrona
                                    createPlayer(playerUUID, currencyId);

                                    return startingBalance >= amount;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al verificar saldo para {} en moneda {}", playerUUID, currencyId, e);
                        return false;
                    }
                }));
    }

    public CompletableFuture<Void> createPlayerAsync(UUID playerUUID) {
        return createPlayerAsync(playerUUID, "default");
    }

    public CompletableFuture<Void> createPlayerAsync(UUID playerUUID, String currencyId) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.runAsync(plugin, () -> {
                    try (conn) {
                        double startingBalance = getCurrencyStartingBalance(currencyId);

                        // Usar INSERT IGNORE para evitar duplicados y asegurar que el jugador exista
                        String sql = """
                                INSERT IGNORE INTO player_balances (player_uuid, currency_id, balance, last_updated)
                                VALUES (?, ?, ?, NOW())
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, currencyId);
                            stmt.setDouble(3, startingBalance);

                            int rowsAffected = stmt.executeUpdate();

                            if (rowsAffected > 0) {
                                log.debug("Jugador creado en MySQL para moneda {}: {}", currencyId, playerUUID);
                            } else {
                                // El jugador ya existía, actualizar su balance por si acaso
                                setBalance(playerUUID, startingBalance, currencyId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error al crear jugador {} para moneda {}", playerUUID, currencyId, e);
                        throw new RuntimeException(e);
                    }
                }));
    }

    public CompletableFuture<Long> getTotalPlayersAsync(String currencyId) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT COUNT(*) FROM player_balances WHERE currency_id = ?";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, currencyId);

                            try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next() ? rs.getLong(1) : 0L;
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener total de jugadores para moneda {}", currencyId, e);
                        return 0L;
                    }
                }));
    }

    public CompletableFuture<Double> getTotalMoneyAsync(String currencyId) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT COALESCE(SUM(balance), 0) FROM player_balances WHERE currency_id = ?";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, currencyId);

                            try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next() ? rs.getDouble(1) : 0.0;
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener dinero total para moneda {}", currencyId, e);
                        return 0.0;
                    }
                }));
    }

    public CompletableFuture<Long> getTotalUniquePlayersAsync() {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT COUNT(DISTINCT player_uuid) FROM player_balances";

                        try (var stmt = conn.createStatement()) {
                            try (ResultSet rs = stmt.executeQuery(sql)) {
                                return rs.next() ? rs.getLong(1) : 0L;
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener total de jugadores únicos", e);
                        return 0L;
                    }
                }));
    }

    public CompletableFuture<Double> getTotalMoneyAllCurrenciesAsync() {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT COALESCE(SUM(balance), 0) FROM player_balances";

                        try (var stmt = conn.createStatement()) {
                            try (ResultSet rs = stmt.executeQuery(sql)) {
                                return rs.next() ? rs.getDouble(1) : 0.0;
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener dinero total de todas las monedas", e);
                        return 0.0;
                    }
                }));
    }

    public CompletableFuture<Object[][]> getTopBalancesAsync(String currencyId, int limit) {
        final int validatedLimit = Math.max(0, limit);
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = """
                                SELECT player_uuid, balance
                                FROM player_balances
                                WHERE currency_id = ?
                                ORDER BY balance DESC
                                LIMIT ?
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, currencyId);
                            stmt.setInt(2, validatedLimit);

                            try (ResultSet rs = stmt.executeQuery()) {
                                List<Object[]> results = new ArrayList<>();

                                while (rs.next()) {
                                    Object[] entry = new Object[2];
                                    entry[0] = rs.getString("player_uuid");
                                    entry[1] = rs.getDouble("balance");
                                    results.add(entry);
                                }

                                return results.toArray(new Object[0][0]);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error al obtener top balances para moneda {}", currencyId, e);
                        return new Object[0][0];
                    }
                }));
    }

    public CompletableFuture<Object[][]> getTopBalancesWithNamesAsync(String currencyId, int limit) {
        final int validatedLimit = Math.max(0, limit);
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = """
                                SELECT pb.player_uuid, pb.balance, pn.player_name
                                FROM player_balances pb
                                LEFT JOIN player_names pn ON pb.player_uuid COLLATE utf8mb4_unicode_ci = pn.player_uuid COLLATE utf8mb4_unicode_ci
                                WHERE pb.currency_id = ?
                                ORDER BY pb.balance DESC
                                LIMIT ?
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, currencyId);
                            stmt.setInt(2, validatedLimit);

                            try (ResultSet rs = stmt.executeQuery()) {
                                List<Object[]> results = new ArrayList<>();

                                while (rs.next()) {
                                    Object[] entry = new Object[3];
                                    entry[0] = rs.getString("player_uuid");
                                    entry[1] = rs.getString("player_name");
                                    entry[2] = rs.getDouble("balance");
                                    results.add(entry);
                                }

                                return results.toArray(new Object[0][0]);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error al obtener top balances con nombres para moneda {}", currencyId, e);
                        return new Object[0][0];
                    }
                }));
    }

    public CompletableFuture<Void> updatePlayerNameAsync(UUID playerUUID, String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.runAsync(plugin, () -> {
                    try (conn) {
                        String sql = """
                                INSERT INTO player_names (player_uuid, player_name, last_updated)
                                VALUES (?, ?, NOW())
                                ON DUPLICATE KEY UPDATE player_name = ?, last_updated = NOW()
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, playerUUID.toString());
                            stmt.setString(2, playerName);
                            stmt.setString(3, playerName);

                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        log.error("Error al actualizar nombre de jugador {}", playerUUID, e);
                        throw new RuntimeException(e);
                    }
                }));
    }

    public CompletableFuture<String> getPlayerNameAsync(UUID playerUUID) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    try (conn) {
                        String sql = "SELECT player_name FROM player_names WHERE player_uuid = ?";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, playerUUID.toString());

                            try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next() ? rs.getString("player_name") : null;
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener nombre de jugador {}", playerUUID, e);
                        return null;
                    }
                }));
    }

    public CompletableFuture<Map<UUID, String>> getPlayerNamesAsync(Iterable<UUID> playerUUIDs) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.supplyAsync(plugin, () -> {
                    Map<UUID, String> names = new HashMap<>();

                    try (conn) {
                        List<String> uuidStrings = new ArrayList<>();
                        for (UUID uuid : playerUUIDs) {
                            uuidStrings.add("'" + uuid.toString() + "'");
                        }

                        if (uuidStrings.isEmpty()) {
                            return names;
                        }

                        String inClause = String.join(",", uuidStrings);
                        String sql = """
                                SELECT player_uuid, player_name
                                FROM player_names
                                WHERE player_uuid IN (%s)
                                """.formatted(inClause);

                        try (var stmt = conn.createStatement()) {
                            try (ResultSet rs = stmt.executeQuery(sql)) {
                                while (rs.next()) {
                                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                                    String name = rs.getString("player_name");
                                    names.put(uuid, name);
                                }
                            }
                        }
                    } catch (SQLException e) {
                        log.error("Error al obtener nombres de jugadores", e);
                    }

                    return names;
                }));
    }

    public CompletableFuture<Void> syncPlayerNamesAsync(Map<UUID, String> activePlayers) {
        return asyncConnection.getConnection()
                .thenCompose(conn -> AsyncUtils.runAsync(plugin, () -> {
                    try (conn) {
                        conn.setAutoCommit(false);

                        String sql = """
                                INSERT INTO player_names (player_uuid, player_name, last_updated)
                                VALUES (?, ?, NOW())
                                ON DUPLICATE KEY UPDATE player_name = ?, last_updated = NOW()
                                """;

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            int batchSize = 0;

                            for (Map.Entry<UUID, String> entry : activePlayers.entrySet()) {
                                UUID playerUUID = entry.getKey();
                                String playerName = entry.getValue();

                                if (playerName != null && !playerName.trim().isEmpty()) {
                                    stmt.setString(1, playerUUID.toString());
                                    stmt.setString(2, playerName);
                                    stmt.setString(3, playerName);

                                    stmt.addBatch();
                                    batchSize++;

                                    // Ejecutar batch cada 1000 registros
                                    if (batchSize >= 1000) {
                                        stmt.executeBatch();
                                        batchSize = 0;
                                    }
                                }
                            }

                            if (batchSize > 0) {
                                stmt.executeBatch();
                            }

                            conn.commit();
                            conn.setAutoCommit(true);

                            log.debug("Sincronizados {} nombres de jugadores", activePlayers.size());
                        } catch (SQLException e) {
                            conn.rollback();
                            log.error("Error al sincronizar nombres de jugadores", e);
                            throw new RuntimeException(e);
                        }
                    } catch (Exception e) {
                        log.error("Error al sincronizar nombres de jugadores", e);
                        throw new RuntimeException(e);
                    }
                }));
    }

    public CompletableFuture<Boolean> initialize() {
        return asyncConnection.initialize();
    }

    public CompletableFuture<Void> shutdown() {
        return asyncConnection.shutdown();
    }

    @Override
    public boolean isAvailable() {
        return asyncConnection.isInitialized() && asyncConnection.getDataSource() != null;
    }

    // Implementación de EconomyDataProvider (SÍNCRONA)
    // Estos métodos usan la conexión asíncrona internamente pero bloquean para
    // compatibilidad

    @Override
    public double getBalance(UUID playerUUID) {
        return getBalance(playerUUID, "default");
    }

    @Override
    public void setBalance(UUID playerUUID, double amount) {
        setBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount) {
        return addBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount) {
        return removeBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        return hasEnoughBalance(playerUUID, amount, "default");
    }

    @Override
    public void createPlayer(UUID playerUUID) {
        createPlayer(playerUUID, "default");
    }

    @Override
    public double getBalance(UUID playerUUID, String currencyId) {
        return getBalanceAsync(playerUUID, currencyId).join();
    }

    @Override
    public void setBalance(UUID playerUUID, double amount, String currencyId) {
        setBalanceAsync(playerUUID, amount, currencyId).join();
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount, String currencyId) {
        return addBalanceAsync(playerUUID, amount, currencyId).join();
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount, String currencyId) {
        return removeBalanceAsync(playerUUID, amount, currencyId).join();
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount, String currencyId) {
        return hasEnoughBalanceAsync(playerUUID, amount, currencyId).join();
    }

    @Override
    public void createPlayer(UUID playerUUID, String currencyId) {
        createPlayerAsync(playerUUID, currencyId).join();
    }

    @Override
    public long getTotalPlayers(String currencyId) {
        return getTotalPlayersAsync(currencyId).join();
    }

    @Override
    public double getTotalMoney(String currencyId) {
        return getTotalMoneyAsync(currencyId).join();
    }

    @Override
    public long getTotalUniquePlayers() {
        return getTotalUniquePlayersAsync().join();
    }

    @Override
    public double getTotalMoneyAllCurrencies() {
        return getTotalMoneyAllCurrenciesAsync().join();
    }

    @Override
    public Object[][] getTopBalances(String currencyId, int limit) {
        return getTopBalancesAsync(currencyId, limit).join();
    }

    @Override
    public Object[][] getTopBalancesWithNames(String currencyId, int limit) {
        return getTopBalancesWithNamesSync(currencyId, limit);
    }

    /**
     * Versión síncrona para uso durante inicialización
     */
    public Object[][] getTopBalancesWithNamesSync(String currencyId, int limit) {
        final int validatedLimit = Math.max(0, limit);
        try {
            return asyncConnection.getConnectionSync()
                    .map(conn -> {
                        try (conn) {
                            String sql = """
                                    SELECT pb.player_uuid, pb.balance, pn.player_name
                                    FROM player_balances pb
                                    LEFT JOIN player_names pn ON pb.player_uuid COLLATE utf8mb4_unicode_ci = pn.player_uuid COLLATE utf8mb4_unicode_ci
                                    WHERE pb.currency_id = ?
                                    ORDER BY pb.balance DESC
                                    LIMIT ?
                                    """;

                            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                stmt.setString(1, currencyId);
                                stmt.setInt(2, validatedLimit);

                                try (ResultSet rs = stmt.executeQuery()) {
                                    List<Object[]> results = new ArrayList<>();

                                    while (rs.next()) {
                                        Object[] entry = new Object[3];
                                        entry[0] = rs.getString("player_uuid");
                                        entry[1] = rs.getString("player_name");
                                        entry[2] = rs.getDouble("balance");
                                        results.add(entry);
                                    }

                                    return results.toArray(new Object[0][0]);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error al obtener top balances con nombres para moneda {}", currencyId, e);
                            return new Object[0][0];
                        }
                    })
                    .orElse(new Object[0][0]);
        } catch (Exception e) {
            log.error("Error al obtener top balances sync", e);
            return new Object[0][0];
        }
    }

    @Override
    public void updatePlayerName(UUID playerUUID, String playerName) {
        updatePlayerNameAsync(playerUUID, playerName).join();
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return getPlayerNameAsync(playerUUID).join();
    }

    @Override
    public Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs) {
        return getPlayerNamesAsync(playerUUIDs).join();
    }

    @Override
    public void syncPlayerNames(Map<UUID, String> activePlayers) {
        syncPlayerNamesAsync(activePlayers).join();
    }

    @Override
    public void load() {
        if (currencyManager == null) {
            currencyManager = CurrencyManager.getInstance();
        }
        initialize().join();
    }

    @Override
    public void save() {
        // No need to save, MySQL saves data immediately
    }

    private double getCurrencyStartingBalance(String currencyId) {
        if (currencyManager == null) {
            currencyManager = CurrencyManager.getInstance();
        }
        Currency currency = currencyManager.getCurrency(currencyId);
        return currency != null ? currency.getStartingBalance() : 100.0; // Default fallback
    }
}
