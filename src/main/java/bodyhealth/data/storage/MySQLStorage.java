package bodyhealth.data.storage;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.data.Storage;
import org.jetbrains.annotations.NotNull;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;

public class MySQLStorage implements Storage {

    private Connection connection;

    public MySQLStorage() {
        connect();
        createTable();
    }

    private void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + Config.storage_mysql_host + ":" + Config.storage_mysql_port + "/" + Config.storage_mysql_database);
            config.setUsername(Config.storage_mysql_user);
            config.setPassword(Config.storage_mysql_password);
            
            try (HikariDataSource dataSource = new HikariDataSource(config)) {
                connection = dataSource.getConnection();
            }

        } catch (SQLException e) {
            Debug.logErr(e.getMessage());
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
        String sql = "CREATE TABLE IF NOT EXISTS " + Config.storage_mysql_prefix + "body_health ("
            + "uuid VARCHAR(36) PRIMARY KEY, "
            + "head DOUBLE, "
            + "body DOUBLE, "
            + "arm_left DOUBLE, "
            + "arm_right DOUBLE, "
            + "leg_left DOUBLE, "
            + "leg_right DOUBLE, "
            + "foot_left DOUBLE, "
            + "foot_right DOUBLE"
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
        String sql = "INSERT INTO " + Config.storage_mysql_prefix + "body_health (uuid, head, body, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "head = VALUES(head), body = VALUES(body), arm_left = VALUES(arm_left), arm_right = VALUES(arm_right), "
            + "leg_left = VALUES(leg_left), leg_right = VALUES(leg_right), foot_left = VALUES(foot_left), foot_right = VALUES(foot_right)";
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
        String sql = "SELECT * FROM " + Config.storage_mysql_prefix + "body_health WHERE uuid = ?";
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
