package bodyhealth.data.storage;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLiteStorage implements Storage {

    private static HikariDataSource dataSource;

    public SQLiteStorage() {
        setupDataSource();
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

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS body_health ("
            + "uuid TEXT PRIMARY KEY, "
            + "head REAL, "
            + "body REAL, "
            + "arm_left REAL, "
            + "arm_right REAL, "
            + "leg_left REAL, "
            + "leg_right REAL, "
            + "foot_left REAL, "
            + "foot_right REAL"
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
    public void saveBodyHealth(UUID uuid, BodyHealth bodyHealth) {
        String sql = "INSERT INTO body_health (uuid, head, body, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(uuid) DO UPDATE SET "
            + "head = excluded.head, body = excluded.body, arm_left = excluded.arm_left, arm_right = excluded.arm_right, "
            + "leg_left = excluded.leg_left, leg_right = excluded.leg_right, foot_left = excluded.foot_left, foot_right = excluded.foot_right";

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
        String sql = "SELECT * FROM body_health WHERE uuid = ?";

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) return new BodyHealth(uuid);
            return new BodyHealth(uuid,
                rs.getDouble("head"),
                rs.getDouble("body"),
                rs.getDouble("arm_left"),
                rs.getDouble("arm_right"),
                rs.getDouble("leg_left"),
                rs.getDouble("leg_right"),
                rs.getDouble("foot_left"),
                rs.getDouble("foot_right"));
        } catch (SQLException e) {
            Debug.logErr(e);
        }
        return new BodyHealth(uuid);
    }
}
