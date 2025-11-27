package com.spectrasonic.MythicEconomy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import com.spectrasonic.MythicEconomy.manager.CurrencyManager;
import com.spectrasonic.MythicEconomy.models.Currency;

import lombok.RequiredArgsConstructor;

/**
 * Proveedor de economía que utiliza MySQL como almacenamiento
 * Implementa el esquema de tabla única para múltiples monedas
 */
@RequiredArgsConstructor
public class MySQLEconomyProvider implements EconomyDataProvider {

    private final JavaPlugin plugin;
    private final MySQLConnection mysqlConnection;
    private CurrencyManager currencyManager;

    @Override
    public double getBalance(UUID playerUUID) {
        // Para compatibilidad, usa la moneda por defecto
        return getBalance(playerUUID, "default");
    }

    @Override
    public void setBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        setBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return addBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return removeBalance(playerUUID, amount, "default");
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount) {
        // Para compatibilidad, usa la moneda por defecto
        return hasEnoughBalance(playerUUID, amount, "default");
    }

    @Override
    public void createPlayer(UUID playerUUID) {
        // Para compatibilidad, crea con la moneda por defecto
        createPlayer(playerUUID, "default");
    }

    @Override
    public double getBalance(UUID playerUUID, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return 0.0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT balance FROM player_balances WHERE player_uuid = ? AND currency_id = ?")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, currencyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                // Jugador nuevo, devolver saldo inicial de la moneda
                if (currencyManager == null) {
                    currencyManager = new CurrencyManager(plugin);
                }

                Currency currency = currencyManager.getCurrency(currencyId);
                return currency != null ? currency.getStartingBalance() : 100.0;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener saldo desde MySQL: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    @Override
    public void setBalance(UUID playerUUID, double amount, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return;
        }

        if (amount < 0) {
            amount = 0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_balances (player_uuid, currency_id, balance, last_updated) " +
                                "VALUES (?, ?, ?, NOW()) " +
                                "ON DUPLICATE KEY UPDATE balance = ?, last_updated = NOW()")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, currencyId);
            stmt.setDouble(3, amount);
            stmt.setDouble(4, amount);

            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al establecer saldo en MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean addBalance(UUID playerUUID, double amount, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return false;
        }

        if (amount <= 0) {
            return false;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_balances (player_uuid, currency_id, balance, last_updated) " +
                                "VALUES (?, ?, ?, NOW()) " +
                                "ON DUPLICATE KEY UPDATE balance = balance + ?, last_updated = NOW()")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, currencyId);
            stmt.setDouble(3, amount);
            stmt.setDouble(4, amount);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al agregar saldo en MySQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeBalance(UUID playerUUID, double amount, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return false;
        }

        if (amount <= 0) {
            return false;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE player_balances SET balance = balance - ?, last_updated = NOW() " +
                                "WHERE player_uuid = ? AND currency_id = ? AND balance >= ?")) {

            stmt.setDouble(1, amount);
            stmt.setString(2, playerUUID.toString());
            stmt.setString(3, currencyId);
            stmt.setDouble(4, amount);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al reducir saldo en MySQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasEnoughBalance(UUID playerUUID, double amount, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return false;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT balance FROM player_balances WHERE player_uuid = ? AND currency_id = ?")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, currencyId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                return currentBalance >= amount;
            }

            // Si el jugador no existe, verificar saldo inicial
            if (currencyManager == null) {
                currencyManager = new CurrencyManager(plugin);
            }

            Currency currency = currencyManager.getCurrency(currencyId);
            double startingBalance = currency != null ? currency.getStartingBalance() : 100.0;
            return startingBalance >= amount;

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al verificar saldo en MySQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void createPlayer(UUID playerUUID, String currencyId) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return;
        }

        try {
            double startingBalance = 100.0;

            // Obtener saldo inicial de la moneda específica
            if (currencyManager == null) {
                currencyManager = new CurrencyManager(plugin);
            }

            Currency currency = currencyManager.getCurrency(currencyId);
            if (currency != null) {
                startingBalance = currency.getStartingBalance();
            }

            setBalance(playerUUID, startingBalance, currencyId);
            plugin.getLogger().info("Jugador creado en MySQL para moneda " + currencyId + ": " + playerUUID);

        } catch (Exception e) {
            plugin.getLogger().severe("Error al crear jugador en MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public long getTotalPlayers(String currencyId) {
        if (!mysqlConnection.isConnected()) {
            return 0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM player_balances WHERE currency_id = ?")) {

            stmt.setString(1, currencyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener total de jugadores: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public double getTotalMoney(String currencyId) {
        if (!mysqlConnection.isConnected()) {
            return 0.0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COALESCE(SUM(balance), 0) FROM player_balances WHERE currency_id = ?")) {

            stmt.setString(1, currencyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener dinero total: " + e.getMessage());
        }

        return 0.0;
    }

    @Override
    public long getTotalUniquePlayers() {
        if (!mysqlConnection.isConnected()) {
            return 0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT player_uuid) FROM player_balances");

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener total de jugadores únicos: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public double getTotalMoneyAllCurrencies() {
        if (!mysqlConnection.isConnected()) {
            return 0.0;
        }

        try (Connection conn = mysqlConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(balance), 0) FROM player_balances");

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener dinero total de todas las monedas: " + e.getMessage());
        }

        return 0.0;
    }

    @Override
    public Object[][] getTopBalances(String currencyId, int limit) {
        if (!mysqlConnection.isConnected()) {
            return new Object[0][0];
        }

        List<Object[]> results = new ArrayList<>();

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT player_uuid, balance FROM player_balances " +
                                "WHERE currency_id = ? ORDER BY balance DESC LIMIT ?")) {

            stmt.setString(1, currencyId);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] entry = new Object[2];
                entry[0] = rs.getString("player_uuid"); // UUID como String
                entry[1] = rs.getDouble("balance");
                results.add(entry);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener top balances desde MySQL: " + e.getMessage());
            e.printStackTrace();
        }

        return results.toArray(new Object[0][0]);
    }

    @Override
    public Object[][] getTopBalancesWithNames(String currencyId, int limit) {
        if (!mysqlConnection.isConnected()) {
            return new Object[0][0];
        }

        List<Object[]> results = new ArrayList<>();

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT pb.player_uuid, pb.balance, pn.player_name FROM player_balances pb " +
                                "LEFT JOIN player_names pn ON pb.player_uuid = pn.player_uuid " +
                                "WHERE pb.currency_id = ? ORDER BY pb.balance DESC LIMIT ?")) {

            stmt.setString(1, currencyId);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] entry = new Object[3];
                entry[0] = rs.getString("player_uuid"); // UUID como String
                entry[1] = rs.getString("player_name"); // Nombre del jugador
                entry[2] = rs.getDouble("balance");
                results.add(entry);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener top balances con nombres desde MySQL: " + e.getMessage());
            e.printStackTrace();
        }

        return results.toArray(new Object[0][0]);
    }

    @Override
    public void updatePlayerName(UUID playerUUID, String playerName) {
        if (!mysqlConnection.isConnected()) {
            plugin.getLogger().warning("No hay conexión activa con MySQL");
            return;
        }

        if (playerName == null || playerName.trim().isEmpty()) {
            return;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_names (player_uuid, player_name, last_updated) " +
                                "VALUES (?, ?, NOW()) " +
                                "ON DUPLICATE KEY UPDATE player_name = ?, last_updated = NOW()")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, playerName);
            stmt.setString(3, playerName);

            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al actualizar nombre de jugador en MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        if (!mysqlConnection.isConnected()) {
            return null;
        }

        try (Connection conn = mysqlConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT player_name FROM player_names WHERE player_uuid = ?")) {

            stmt.setString(1, playerUUID.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("player_name");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener nombre de jugador desde MySQL: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<UUID, String> getPlayerNames(Iterable<UUID> playerUUIDs) {
        Map<UUID, String> names = new HashMap<>();
        
        if (!mysqlConnection.isConnected()) {
            return names;
        }

        List<String> uuidStrings = new ArrayList<>();
        for (UUID uuid : playerUUIDs) {
            uuidStrings.add("'" + uuid.toString() + "'");
        }

        if (uuidStrings.isEmpty()) {
            return names;
        }

        try (Connection conn = mysqlConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            String inClause = String.join(",", uuidStrings);
            ResultSet rs = stmt.executeQuery(
                    "SELECT player_uuid, player_name FROM player_names WHERE player_uuid IN (" + inClause + ")");

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                String name = rs.getString("player_name");
                names.put(uuid, name);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener nombres de jugadores desde MySQL: " + e.getMessage());
            e.printStackTrace();
        }

        return names;
    }

    @Override
    public void syncPlayerNames(Map<UUID, String> activePlayers) {
        if (!mysqlConnection.isConnected()) {
            return;
        }

        try (Connection conn = mysqlConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            for (Map.Entry<UUID, String> entry : activePlayers.entrySet()) {
                UUID playerUUID = entry.getKey();
                String playerName = entry.getValue();
                
                if (playerName != null && !playerName.trim().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_names (player_uuid, player_name, last_updated) " +
                                    "VALUES (?, ?, NOW()) " +
                                    "ON DUPLICATE KEY UPDATE player_name = ?, last_updated = NOW()")) {
                        
                        stmt.setString(1, playerUUID.toString());
                        stmt.setString(2, playerName);
                        stmt.setString(3, playerName);
                        
                        stmt.executeUpdate();
                    }
                }
            }
            
            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al sincronizar nombres de jugadores en MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        // MySQL guarda automáticamente, no necesitamos hacer nada
    }

    @Override
    public void load() {
        // MySQL carga automáticamente cuando se necesita, no necesitamos hacer nada
    }

    @Override
    public boolean isAvailable() {
        return mysqlConnection != null && mysqlConnection.isConnected();
    }
}