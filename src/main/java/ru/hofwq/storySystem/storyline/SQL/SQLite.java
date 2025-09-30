package ru.hofwq.storySystem.storyline.SQL;

import org.bukkit.plugin.java.JavaPlugin;
import ru.hofwq.storySystem.StorySystem;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLite {
    private final JavaPlugin plugin;
    private final String databaseName;
    private Connection conn;

    public SQLite(JavaPlugin plugin, String databaseName) {
        this.plugin = plugin;
        this.databaseName = databaseName;
        this.conn = null;
    }

    public void initDatabase() {
        try {
            JavaPlugin plugin = JavaPlugin.getPlugin(StorySystem.class);
            conn = DriverManager.getConnection(
                    "jdbc:sqlite:" + plugin.getDataFolder() + "/storylinePlayers.db"
            );
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS players (" +
                                "uuid TEXT PRIMARY KEY," +
                                "flag TEXT NOT NULL)"
                );
            }
            JavaPlugin.getPlugin(StorySystem.class).log.info("SQL initialized at " + plugin.getDataFolder() + "/storylinePlayers.db");
        } catch (SQLException e) {
            JavaPlugin.getPlugin(StorySystem.class).log.severe("Failed to init SQL: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + databaseName;
            conn = DriverManager.getConnection(url);
        }
        return conn;
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Error closing SQLite: " + ex.getMessage());
            } finally {
                conn = null;
            }
        }
    }

    public boolean isPlayerExists(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String getFlag(UUID uuid) throws SQLException {
        String sql = "SELECT flag FROM players WHERE uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("flag") : null;
            }
        }
    }

    /**
     *
     * @param uuid Player UUID
     * @param newFlag New flag for player
     * @throws SQLException
     */
    public void updateFlag(UUID uuid, String newFlag) throws SQLException {
        String sql = "UPDATE players SET flag = ? WHERE uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newFlag);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public void insertPlayer(UUID uuid, String initialFlag) throws SQLException {
        String sql = "INSERT INTO players(uuid, flag) VALUES(?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, initialFlag);
            ps.executeUpdate();
        }
    }

    public void removePlayer(UUID uuid) throws SQLException {
        String sql = "DELETE FROM players WHERE uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }
}
