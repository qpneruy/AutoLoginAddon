package org.qpneruy.autoLoginAddon.data.Database;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.qpneruy.autoLoginAddon.cache.Service.PlayerCache;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class PlayerAuthRepository {

    private static final String CREATE_TABLES_SQL = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS player_ips (
                player_uuid VARCHAR(36),
                ip_address VARCHAR(15),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_uuid) REFERENCES players(uuid),
                UNIQUE (player_uuid, ip_address)
            );""";

    private final DataSource dataSource;
    private final PlayerCache playerCache;

    @Getter
    private static final PlayerAuthRepository INSTANCE = new PlayerAuthRepository();

    public PlayerAuthRepository() {
        this.dataSource = DatabaseManager.getDataSource();
        this.playerCache = PlayerCache.getINSTANCE();
        initializeTables();
    }

    private void initializeTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLES_SQL);
        } catch (SQLException e) {
            log.error("Failed to initialize database tables", e);
        }
    }

    public void registerPlayer(UUID playerUUID) {
        String uuid = playerUUID.toString();
        if (playerCache.isPlayerKnown(uuid)) return;

        String sql = "MERGE INTO players (uuid, last_login) KEY(uuid) VALUES (?, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            playerCache.cachePlayer(uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to register player: {}", uuid, e);
        }
    }

    public void registerPlayerIp(UUID playerUUID, String ipAddress) {
        String uuid = playerUUID.toString();
        if (playerCache.isIpRegistered(uuid, ipAddress)) {
            log.debug("IP {} already registered for player {}", ipAddress, uuid);
            return;
        }

        String sql = "MERGE INTO player_ips (player_uuid, ip_address, created_at) KEY(player_uuid, ip_address) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();
            playerCache.cachePlayerIp(uuid, ipAddress);
        } catch (SQLException e) {
            log.error("Failed to register IP for player: {} - {}", uuid, ipAddress, e);
            e.printStackTrace(); // Add detailed error logging
        }
    }


    public void updateLastLogin(UUID playerUUID) {
        String uuid = playerUUID.toString();
        String sql = "UPDATE players SET last_login = CURRENT_TIMESTAMP WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();

            Set<String> existingIps = playerCache.getPlayerIps(uuid);
            PlayerCache.PlayerData updatedData = new PlayerCache.PlayerData(uuid, existingIps, LocalDateTime.now());
            playerCache.updatePlayerData(uuid, updatedData);
        } catch (SQLException e) {
            log.error("Failed to update last login for player: {}", uuid, e);
        }
    }

    public boolean isIpRegisteredForPlayer(UUID playerUUID, String ipAddress) {
        String uuid = playerUUID.toString();
        if (playerCache.isIpRegistered(uuid, ipAddress)) return true;


        String sql = "SELECT 1 FROM player_ips WHERE player_uuid = ? AND ip_address = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, ipAddress);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                if (exists) playerCache.cachePlayerIp(uuid, ipAddress);
                return exists;
            }
        } catch (SQLException e) {
            log.error("Failed to check IP registration for player: {} - {}", uuid, ipAddress, e);
            return false;
        }
    }

    public void removeIp(UUID playerUUID, String ipAddress) {
        String uuid = playerUUID.toString();
        String sql = "DELETE FROM player_ips WHERE player_uuid = ? AND ip_address = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();

            playerCache.removeIp(uuid, ipAddress);
        } catch (SQLException e) {
            log.error("Failed to remove IP for player: {} - {}", uuid, ipAddress, e);
        }
    }

    public void removeAllIps(UUID playerUUID) {
        String uuid = playerUUID.toString();
        String sql = "DELETE FROM player_ips WHERE player_uuid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();

            playerCache.clearPlayerIps(uuid);
        } catch (SQLException e) {
            log.error("Failed to remove all IPs for player: {}", uuid, e);
        }
    }
}
