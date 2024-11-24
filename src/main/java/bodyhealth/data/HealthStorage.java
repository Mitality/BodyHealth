package bodyhealth.data;

import bodyhealth.Main;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.UUID;

public class HealthStorage {

    /**
     * Saves the current health per BodyPart for all BodyParts and all players
     */
    public static void savePlayerHealthData() {
        FileConfiguration data = DataManager.getData();

        for (Map.Entry<UUID, BodyHealth> entry : Main.playerBodyHealthMap.entrySet()) {
            UUID playerUUID = entry.getKey();
            BodyHealth bodyHealth = entry.getValue();
            for (BodyPart part : BodyPart.values()) {
                double health = bodyHealth.getHealth(part);
                data.set("players." + playerUUID + ".health." + part.name(), health);
            }
        }

        DataManager.saveData();
    }

    /**
     * Loads up the current health per BodyPart for all BodyParts and all players from the yaml storage system
     * @return A number representing for how many players data was found and loaded
     */
    public static int loadPlayerHealthData() {
        FileConfiguration data = DataManager.getData();

        if (data != null && data.contains("players")) {
            int counter = 0;
            for (String playerUUID : data.getConfigurationSection("players").getKeys(false)) {

                double head = data.getDouble("players." + playerUUID + ".health." + BodyPart.HEAD.name());
                double body = data.getDouble("players." + playerUUID + ".health." + BodyPart.BODY.name());
                double arm_left = data.getDouble("players." + playerUUID + ".health." + BodyPart.ARM_LEFT.name());
                double arm_right = data.getDouble("players." + playerUUID + ".health." + BodyPart.ARM_RIGHT.name());
                double leg_left = data.getDouble("players." + playerUUID + ".health." + BodyPart.LEG_LEFT.name());
                double leg_right = data.getDouble("players." + playerUUID + ".health." + BodyPart.LEG_RIGHT.name());
                double foot_left = data.getDouble("players." + playerUUID + ".health." + BodyPart.FOOT_LEFT.name());
                double foot_right = data.getDouble("players." + playerUUID + ".health." + BodyPart.FOOT_RIGHT.name());
                BodyHealth bodyHealth = new BodyHealth(UUID.fromString(playerUUID), head, body, arm_left, arm_right, leg_left, leg_right, foot_left, foot_right);

                Main.playerBodyHealthMap.put(UUID.fromString(playerUUID), bodyHealth);
                counter++;
            }
            return counter;
        } else {
            return -1;
        }
    }
}
