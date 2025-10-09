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
import java.util.List;
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
        String prefix = Config.storage_mysql_prefix;

        String sqlHealth = "CREATE TABLE IF NOT EXISTS " + prefix + "body_health ("
                + "uuid VARCHAR(36) PRIMARY KEY, "
                + "head DOUBLE, "
                + "torso DOUBLE, "
                + "arm_left DOUBLE, "
                + "arm_right DOUBLE, "
                + "leg_left DOUBLE, "
                + "leg_right DOUBLE, "
                + "foot_left DOUBLE, "
                + "foot_right DOUBLE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String sqlEffects = "CREATE TABLE IF NOT EXISTS " + prefix + "active_effects ("
                + "uuid VARCHAR(36) NOT NULL, "
                + "body_part VARCHAR(32) NOT NULL, "
                + "position INT NOT NULL, "
                + "effect TEXT NOT NULL, "
                + "PRIMARY KEY (uuid, body_part, position), "
                + "INDEX idx_effects_uuid (uuid)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sqlHealth);
            stmt.execute(sqlEffects);
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
        String prefix = Config.storage_mysql_prefix;

        String upsertHealth = "INSERT INTO " + prefix + "body_health "
                + "(uuid, head, torso, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "head=VALUES(head), torso=VALUES(torso), arm_left=VALUES(arm_left), arm_right=VALUES(arm_right), "
                + "leg_left=VALUES(leg_left), leg_right=VALUES(leg_right), foot_left=VALUES(foot_left), foot_right=VALUES(foot_right)";
        String deleteEffects = "DELETE FROM " + prefix + "active_effects WHERE uuid = ?";
        String insertEffect = "INSERT INTO " + prefix + "active_effects "
                + "(uuid, body_part, position, effect) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(upsertHealth)) {
                pstmt.setString(1, uuid.toString());
                int idx = 2;
                for (BodyPart part : BodyPart.values()) {
                    pstmt.setDouble(idx++, bodyHealth.getHealth(part));
                }
                pstmt.executeUpdate();
            }

            try (PreparedStatement del = conn.prepareStatement(deleteEffects)) {
                del.setString(1, uuid.toString());
                del.executeUpdate();
            }

            Map<BodyPart, List<String[]>> effects = bodyHealth.getOngoingEffects();
            boolean hasAny = effects.values().stream().anyMatch(list -> list != null && !list.isEmpty());

            if (hasAny) {
                try (PreparedStatement ins = conn.prepareStatement(insertEffect)) {
                    for (Map.Entry<BodyPart, List<String[]>> e : effects.entrySet()) {
                        List<String[]> list = e.getValue();
                        if (list == null || list.isEmpty()) continue;

                        int pos = 0;
                        for (String[] arr : list) {
                            ins.setString(1, uuid.toString());
                            ins.setString(2, e.getKey().name());
                            ins.setInt(3, pos++);
                            ins.setString(4, String.join("/", arr));
                            ins.addBatch();
                        }
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    @Override
    public @NotNull BodyHealth loadBodyHealth(UUID uuid) {
        String prefix = Config.storage_mysql_prefix;
        String sql = "SELECT * FROM " + prefix + "body_health WHERE uuid = ?";

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return new BodyHealth(uuid);
                BodyHealth bh = getBodyHealth(uuid, rs);
                loadEffectsInto(conn, uuid, bh);
                return bh;
            }
        } catch (SQLException e) {
            Debug.logErr(e);
            return new BodyHealth(uuid);
        }
    }

    @Override
    public @NotNull Map<UUID, BodyHealth> loadAllBodyHealth() {
        String prefix = Config.storage_mysql_prefix;
        final String sql = "SELECT uuid, head, torso, arm_left, arm_right, "
                + "leg_left, leg_right, foot_left, foot_right FROM " + prefix + "body_health";

        Map<UUID, BodyHealth> map = new HashMap<>();
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {

            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    BodyHealth bh = getBodyHealth(uuid, rs);
                    map.put(uuid, bh);
                } catch (Exception ignored) {}
            }

            if (!map.isEmpty()) {
                String esql = "SELECT uuid, body_part, position, effect "
                        + "FROM " + prefix + "active_effects "
                        + "ORDER BY uuid, body_part, position";

                try (
                    PreparedStatement eps = conn.prepareStatement(esql);
                    ResultSet ers = eps.executeQuery()
                ) {

                    while (ers.next()) {
                        UUID uuid;
                        try { uuid = UUID.fromString(ers.getString("uuid")); }
                        catch (Exception e) { continue; }

                        BodyHealth bh = map.get(uuid);
                        if (bh == null) continue;

                        BodyPart part;
                        try { part = BodyPart.valueOf(ers.getString("body_part")); }
                        catch (IllegalArgumentException ex) { continue; }

                        String effect = ers.getString("effect");
                        bh.addToOngoingEffects(part, effect.split("/"));
                    }
                }
            }
        } catch (SQLException e) {
            Debug.logErr(e);
        }
        return map;
    }

    private void loadEffectsInto(Connection conn, UUID uuid, BodyHealth bh) throws SQLException {
        String prefix = Config.storage_mysql_prefix;
        final String sql = "SELECT body_part, position, effect "
                + "FROM " + prefix + "active_effects WHERE uuid = ? "
                + "ORDER BY body_part, position";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BodyPart part;
                    try { part = BodyPart.valueOf(rs.getString("body_part")); }
                    catch (IllegalArgumentException ex) { continue; }
                    String effect = rs.getString("effect");
                    bh.addToOngoingEffects(part, effect.split("/"));
                }
            }
        }
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
