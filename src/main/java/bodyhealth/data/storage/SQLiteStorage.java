package bodyhealth.data.storage;

import bodyhealth.config.Config;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLiteStorage implements Storage {

    private final File databaseFile;
    private Connection connection;

    public SQLiteStorage() {
        databaseFile = new File(DataManager.getDataFolder(), "bodyHealth.sqlite");
        connect();
        createTable();
    }

    private void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (SQLException e) {
            if (Config.error_logging) e.printStackTrace();
        }
    }

    private void validateConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException e) {
            if (Config.error_logging) e.printStackTrace();
        }
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            if (Config.error_logging) e.printStackTrace();
        }
    }

    @Override
    public void saveBodyHealth(UUID uuid, BodyHealth bodyHealth) {
        validateConnection();
        String sql = "INSERT INTO body_health (uuid, head, body, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(uuid) DO UPDATE SET "
            + "head = excluded.head, body = excluded.body, arm_left = excluded.arm_left, arm_right = excluded.arm_right, "
            + "leg_left = excluded.leg_left, leg_right = excluded.leg_right, foot_left = excluded.foot_left, foot_right = excluded.foot_right";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setDouble(2, bodyHealth.getHealth(BodyPart.HEAD));
            pstmt.setDouble(3, bodyHealth.getHealth(BodyPart.BODY));
            pstmt.setDouble(4, bodyHealth.getHealth(BodyPart.ARM_LEFT));
            pstmt.setDouble(5, bodyHealth.getHealth(BodyPart.ARM_RIGHT));
            pstmt.setDouble(6, bodyHealth.getHealth(BodyPart.LEG_LEFT));
            pstmt.setDouble(7, bodyHealth.getHealth(BodyPart.LEG_RIGHT));
            pstmt.setDouble(8, bodyHealth.getHealth(BodyPart.FOOT_LEFT));
            pstmt.setDouble(9, bodyHealth.getHealth(BodyPart.FOOT_RIGHT));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (Config.error_logging) e.printStackTrace();
        }
    }

    @Override
    public @NotNull BodyHealth loadBodyHealth(UUID uuid) {
        validateConnection();
        String sql = "SELECT * FROM body_health WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new BodyHealth(uuid,
                    rs.getDouble("head"),
                    rs.getDouble("body"),
                    rs.getDouble("arm_left"),
                    rs.getDouble("arm_right"),
                    rs.getDouble("leg_left"),
                    rs.getDouble("leg_right"),
                    rs.getDouble("foot_left"),
                    rs.getDouble("foot_right"));
            }
        } catch (SQLException e) {
            if (Config.error_logging) e.printStackTrace();
        }
        return new BodyHealth(uuid);
    }
}
