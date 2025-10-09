package bodyhealth.data.storage;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import bodyhealth.data.StorageType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteStorage implements Storage {

    @Override
    public StorageType getType() {
        return StorageType.SQLite;
    }

    private static HikariDataSource dataSource;

    public SQLiteStorage() {
        setupDataSource();
        migrateTable();
        createTable();
    }

    private synchronized void setupDataSource() {
        if (dataSource != null && !dataSource.isClosed()) return;
        HikariConfig config = new HikariConfig();

        File databaseFile = new File(DataManager.getDataFolder(), "bodyHealth.sqlite");
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        config.setJdbcUrl(jdbcUrl);

        config.setPoolName("SQLitePool");
        config.setConnectionTestQuery("SELECT 1");

        config.setMaximumPoolSize(1); // SQLite handles only one write at a time
        config.setMinimumIdle(1);

        config.setMaxLifetime(300000); // 5m
        config.setInitializationFailTimeout(-1);

        dataSource = new HikariDataSource(config);
    }

    private void migrateTable() {
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "body_health", new String[]{"TABLE"})) {
                if (!tables.next()) return; // Table doesn't exist (yet)
            }
            boolean hasBody = false, hasTorso = false;
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(body_health)")) {
                while (rs.next()) {
                    String col = rs.getString("name");
                    if ("body".equalsIgnoreCase(col)) hasBody = true;
                    if ("torso".equalsIgnoreCase(col)) hasTorso = true;
                }
            }
            if (hasBody && !hasTorso) stmt.execute("ALTER TABLE body_health RENAME COLUMN body TO torso");
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    private void createTable() {
        String sqlHealth = "CREATE TABLE IF NOT EXISTS body_health ("
                + "uuid TEXT PRIMARY KEY, "
                + "head REAL, "
                + "torso REAL, "
                + "arm_left REAL, "
                + "arm_right REAL, "
                + "leg_left REAL, "
                + "leg_right REAL, "
                + "foot_left REAL, "
                + "foot_right REAL"
                + ")";
        String sqlEffects = "CREATE TABLE IF NOT EXISTS active_effects ("
                + "uuid TEXT NOT NULL, "
                + "body_part TEXT NOT NULL, "
                + "position INTEGER NOT NULL, "
                + "effect TEXT NOT NULL, "
                + "PRIMARY KEY (uuid, body_part, position)"
                + ")";
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sqlHealth);
            stmt.execute(sqlEffects);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_effects_uuid ON active_effects(uuid)");
        } catch (SQLException e) {
            Debug.logErr(e);
        }
    }

    @Override
    public boolean erase() {
        try (
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            conn.setAutoCommit(false);
            stmt.executeUpdate("DELETE FROM active_effects");
            stmt.executeUpdate("DELETE FROM body_health");
            conn.commit();
            return true;
        } catch (SQLException e) {
            Debug.logErr(e);
            return false;
        }
    }

    @Override
    public void saveBodyHealth(UUID uuid, BodyHealth bodyHealth) {
        String upsertHealth = "INSERT INTO body_health (uuid, head, torso, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET "
                + "head = excluded.head, torso = excluded.torso, arm_left = excluded.arm_left, arm_right = excluded.arm_right, "
                + "leg_left = excluded.leg_left, leg_right = excluded.leg_right, foot_left = excluded.foot_left, foot_right = excluded.foot_right";
        String deleteEffects = "DELETE FROM active_effects WHERE uuid = ?";
        String insertEffect = "INSERT INTO active_effects (uuid, body_part, position, effect) VALUES (?, ?, ?, ?)";

        try (
            Connection conn = dataSource.getConnection()
        ) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement pstmt = conn.prepareStatement(upsertHealth)
            ) {
                pstmt.setString(1, uuid.toString());
                int index = 2;
                for (BodyPart part : BodyPart.values()) {
                    pstmt.setDouble(index++, bodyHealth.getHealth(part));
                }
                pstmt.executeUpdate();
            }

            try (
                PreparedStatement del = conn.prepareStatement(deleteEffects)
            ) {
                del.setString(1, uuid.toString());
                del.executeUpdate();
            }

            Map<BodyPart, List<String[]>> effects = bodyHealth.getOngoingEffects();
            boolean hasAny = effects.values().stream().anyMatch(list -> !list.isEmpty());

            if (hasAny) {
                try (PreparedStatement ins = conn.prepareStatement(insertEffect)) {
                    for (Map.Entry<BodyPart, List<String[]>> entry : effects.entrySet()) {
                        List<String[]> list = entry.getValue();
                        if (list == null || list.isEmpty()) continue;

                        int pos = 0;
                        for (String[] arr : list) {
                            ins.setString(1, uuid.toString());
                            ins.setString(2, entry.getKey().name());
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
        String sql = "SELECT * FROM body_health WHERE uuid = ?";
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
        }
        return new BodyHealth(uuid);
    }

    @Override
    public @NotNull Map<UUID, BodyHealth> loadAllBodyHealth() {
        final String sql = "SELECT uuid, head, torso, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right FROM body_health";
        Map<UUID, BodyHealth> map = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    BodyHealth bh = getBodyHealth(uuid, rs);
                    map.put(uuid, bh);
                } catch (Exception ignored) {}
            }

            if (!map.isEmpty()) {
                String in = String.join(",", Collections.nCopies(map.size(), "?"));
                String esql = "SELECT uuid, body_part, position, effect FROM active_effects WHERE uuid IN (" + in + ") ORDER BY uuid, body_part, position";

                try (PreparedStatement eps = conn.prepareStatement(esql)) {
                    int i = 1;
                    for (UUID u : map.keySet()) eps.setString(i++, u.toString());

                    try (ResultSet ers = eps.executeQuery()) {
                        while (ers.next()) {
                            UUID u = UUID.fromString(ers.getString("uuid"));
                            BodyPart part = BodyPart.valueOf(ers.getString("body_part"));
                            String effect = ers.getString("effect");
                            BodyHealth bh = map.get(u);
                            if (bh != null) {
                                bh.addToOngoingEffects(part, effect.split("/"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Debug.logErr(e);
        }
        return map;
    }

    private void loadEffectsInto(Connection conn, UUID uuid, BodyHealth bh) throws SQLException {
        final String sql = "SELECT body_part, position, effect FROM active_effects WHERE uuid = ? ORDER BY body_part, position";
        try (
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString()
        );
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BodyPart part = BodyPart.valueOf(rs.getString("body_part"));
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
