package bodyhealth.calculations;

import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.util.BodyHealthUtils;
import bodyhealth.config.Debug;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class BodyHealthCalculator {

    public static void calculateHitByEntity(Player player, Location damagerLocation, EntityDamageEvent.DamageCause cause, double damage) {
        Location playerLocation = player.getLocation();
        Vector damagerVector = damagerLocation.toVector();
        Vector playerVector = playerLocation.toVector();

        double yDiff = damagerVector.getY() - playerLocation.getY();
        BodyPart bodyPart = determineBodyPart(damagerVector, playerVector, yDiff);

        BodyHealthUtils.applyDamageWithConfig(BodyHealthUtils.getBodyHealth(player), cause, damage, bodyPart);
        Debug.log("Player " + player.getName() + " was hit by an entity on " + bodyPart.name() + " with " + damage + " damage.");
    }

    public static void calculateHitByBlock(Player player, Location blockLocation, EntityDamageEvent.DamageCause cause, double damage) {
        Location playerLocation = player.getLocation();
        Vector blockVector = blockLocation.add(new Vector(0.5, 0.5, 0.5)).toVector();
        Vector playerVector = playerLocation.toVector();
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);

        double yDiff = blockLocation.add(new Vector(0.5, 0.5, 0.5)).getY() - playerLocation.getY();
        float playerYaw = playerLocation.getYaw();
        Vector blockDirection = blockVector.subtract(playerVector).normalize();
        double blockYaw = Math.toDegrees(Math.atan2(blockDirection.getZ(), blockDirection.getX())) - 90;
        blockYaw = (blockYaw + 360) % 360;
        double relativeYaw = (playerYaw - blockYaw + 360) % 360;

        BodyPart[] parts = determineHitParts(relativeYaw, yDiff);

        for (BodyPart part : parts) {
            BodyHealthUtils.applyDamageWithConfig(bodyHealth, cause, damage, part);
            Debug.log("Player was hit by a block on " + part.name() + " with " + damage + " damage.");
        }
    }

    private static BodyPart[] determineHitParts(double relativeYaw, double yDiff) {
        if (relativeYaw <= 45 || relativeYaw >= 315) {
            return yDiff >= 1.9 ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.0 ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY} :
                            yDiff >= 0.3 ? new BodyPart[]{BodyPart.BODY} :
                                    yDiff >= 0.2 ? new BodyPart[]{BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                            yDiff >= -0.2 ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT, BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT};
        } else if (relativeYaw >= 135 && relativeYaw <= 225) {
            return yDiff >= 1.9 ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.0 ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY} :
                            yDiff >= 0.3 ? new BodyPart[]{BodyPart.BODY} :
                                    yDiff >= 0.2 ? new BodyPart[]{BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                            yDiff >= -0.2 ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT, BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT};
        } else if (relativeYaw > 45 && relativeYaw < 135) {
            return yDiff >= 1.9 ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.0 ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_LEFT} :
                            yDiff >= 0.3 ? new BodyPart[]{BodyPart.ARM_LEFT} :
                                    yDiff >= 0.2 ? new BodyPart[]{BodyPart.ARM_LEFT, BodyPart.LEG_LEFT} :
                                            yDiff >= -0.2 ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.FOOT_LEFT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT};
        } else {
            return yDiff >= 1.9 ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.0 ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_RIGHT} :
                            yDiff >= 0.3 ? new BodyPart[]{BodyPart.ARM_RIGHT} :
                                    yDiff >= 0.2 ? new BodyPart[]{BodyPart.ARM_RIGHT, BodyPart.LEG_RIGHT} :
                                            yDiff >= -0.2 ? new BodyPart[]{BodyPart.LEG_RIGHT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_RIGHT};
        }
    }

    private static BodyPart determineBodyPart(Vector damagerVector, Vector playerVector, double yDiff) {
        if (yDiff >= 1.6) {
            return BodyPart.HEAD;
        } else if (yDiff >= 1.2) {
            return BodyPart.BODY;
        } else if (yDiff >= 0.8) {
            return damagerVector.getX() < playerVector.getX() ? BodyPart.ARM_LEFT : BodyPart.ARM_RIGHT;
        } else if (yDiff >= 0.4) {
            return damagerVector.getX() < playerVector.getX() ? BodyPart.LEG_LEFT : BodyPart.LEG_RIGHT;
        } else {
            return damagerVector.getX() < playerVector.getX() ? BodyPart.FOOT_LEFT : BodyPart.FOOT_RIGHT;
        }
    }

}
