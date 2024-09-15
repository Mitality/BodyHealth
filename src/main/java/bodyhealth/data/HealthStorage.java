package bodyhealth.data;

import bodyhealth.Main;
import bodyhealth.debug.Debug;
import bodyhealth.object.BodyHealth;
import bodyhealth.object.BodyPart;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.UUID;

public class HealthStorage {

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

    public static int loadPlayerHealthData() {
        FileConfiguration data = DataManager.getData();

        if (data != null && data.contains("players")) {
            int counter = 0;
            for (String playerUUID : data.getConfigurationSection("players").getKeys(false)) {

                BodyHealth bodyHealth = new BodyHealth(UUID.fromString(playerUUID));

                for (BodyPart part : BodyPart.values()) {
                    double health = data.getDouble("players." + playerUUID + ".health." + part.name());
                    bodyHealth.setHealth(part, health);
                }

                Main.playerBodyHealthMap.put(UUID.fromString(playerUUID), bodyHealth);
                counter++;
            }
            return counter;
        } else {
            return -1;
        }
    }
}