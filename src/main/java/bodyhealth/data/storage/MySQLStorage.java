package bodyhealth.data.storage;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.Storage;
import bodyhealth.data.StorageType;
import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLStorage implements Storage {

    @Override
    public StorageType getType() {
        return StorageType.MySQL;
    }

    HikariDataSource dataSource;

    public MySQLStorage() {
        setupDataSource();
        migrateTable();
        createTable();
    }

    private synchronized void setupDataSource() {
        if (dataSource != null && !dataSource.isClosed()) return;
        HikariConfig config = new HikariConfig();

        String jdbcUrl = "jdbc:mysql://" + Config.storage_mysql_host + ":" + Config.storage_mysql_port + "/" + Config.storage_mysql_database;
        config.setJdbcUrl(jdbcUrl);

        config.setUsername(Config.storage_mysql_user);
        config.setPassword(Config.storage_mysql_password);

        config.setPoolName("MySQLPool");
        config.setConnectionTestQuery("SELECT 1");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        config.setIdleTimeout(600000); // 10m
        config.setMaxLifetime(1800000); // 30m
        config.setConnectionTimeout(30000); // 30s

        dataSource = new HikariDataSource(config);
    }

    private void migrateTable() {
        String tableName = Config.storage_mysql_prefix + "body_health";
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, null, tableName, null)) {
                if (!tables.next()) return; // Table doesn't exist (yet)
            }
            boolean hasBody = false, hasTorso = false;
            try (ResultSet cols = meta.getColumns(null, null, tableName, null)) {
                while (cols.next()) {
                    String colName = cols.getString("COLUMN_NAME");
                    if ("torso".equalsIgnoreCase(colName)) hasTorso = true;
                    if ("body".equalsIgnoreCase(colName)) hasBody = true;
                }
            }
            if (hasBody && !hasTorso) {
                String sql = "ALTER TABLE " + tableName + " CHANGE COLUMN body torso DOUBLE";
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + Config.storage_mysql_prefix + "body_health ("
            + "uuid VARCHAR(36) PRIMARY KEY, "
            + "head DOUBLE, "
            + "torso DOUBLE, "
            + "arm_left DOUBLE, "
            + "arm_right DOUBLE, "
            + "leg_left DOUBLE, "
            + "leg_right DOUBLE, "
            + "foot_left DOUBLE, "
            + "foot_right DOUBLE"
            + ")";
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    @Override
    public boolean erase() {
        final String sql = "TRUNCATE TABLE " + Config.storage_mysql_prefix + "body_health";
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            Debug.logErr(e);
            return false;
        }
    }

    @Override
    public void saveBodyHealth(UUID uuid, BodyHealth bodyHealth) {
        String sql = "INSERT INTO " + Config.storage_mysql_prefix + "body_health (uuid, head, torso, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "head = VALUES(head), torso = VALUES(torso), arm_left = VALUES(arm_left), arm_right = VALUES(arm_right), "
            + "leg_left = VALUES(leg_left), leg_right = VALUES(leg_right), foot_left = VALUES(foot_left), foot_right = VALUES(foot_right)";
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, uuid.toString());

            int index = 2; // Should work, right?
            for (BodyPart part : BodyPart.values()) {
                pstmt.setDouble(index++, bodyHealth.getHealth(part));
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    @Override
    public @NotNull BodyHealth loadBodyHealth(UUID uuid) {
        String sql = "SELECT * FROM " + Config.storage_mysql_prefix + "body_health WHERE uuid = ?";

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) return new BodyHealth(uuid);
            return getBodyHealth(uuid, rs);
        } catch (SQLException e) {
            Debug.logErr(e);
        }
        return new BodyHealth(uuid);
    }

    @Override
    public @NotNull Map<UUID, BodyHealth> loadAllBodyHealth() {
        final String sql = "SELECT uuid, head, torso, arm_left, arm_right, " +
                "leg_left, leg_right, foot_left, foot_right FROM " +
                Config.storage_mysql_prefix + "body_health";

        Map<UUID, BodyHealth> map = new HashMap<>();
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    BodyHealth bh = getBodyHealth(uuid, rs);
                    map.put(uuid, bh);
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException e) {
            Debug.logErr(e);
        }
        return map;
    }

    private BodyHealth getBodyHealth(UUID uuid, ResultSet rs) throws SQLException {
        return new BodyHealth(uuid,
            rs.getDouble("head"),
            rs.getDouble("torso"),
            rs.getDouble("arm_left"),
            rs.getDouble("arm_right"),
            rs.getDouble("leg_left"),
            rs.getDouble("leg_right"),
            rs.getDouble("foot_left"),
            rs.getDouble("foot_right"));
    }

}
