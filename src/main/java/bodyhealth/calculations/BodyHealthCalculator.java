package bodyhealth.calculations;

import bodyhealth.config.Config;
import bodyhealth.core.BodyPart;
import bodyhealth.config.Debug;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.Objects;

public class BodyHealthCalculator {

    /**
     * Calculates what BodyPart of a player was hit by an arrow
     * @param player The player that was hit by an arrow
     * @param arrow The arrow that hit the player
     * @return The hit BodyPart
     */
    public static BodyPart calculateHitByArrow(Player player, Arrow arrow) {
        return calculateHitByEntityLegacy(player, arrow); // Extremely precise for arrows
    }

    /**
     * Calculates what BodyPart of a player was hit by an entity via raytracing
     * @param player The player that was hit by an entity
     * @param entity The entity that hit the player
     * @return The hit BodyPart
     */
    public static BodyPart calculateHitByEntity(Player player, Entity entity) {

        if (!(entity instanceof LivingEntity)) {
            Debug.log("Damaging entity is not an instance of LivingEntity, applying damage to all body parts...");
            return null;
        }

        if (!Config.raytracing_enabled) {
            Debug.log("Ray tracing is disabled, defaulting to legacy calculation...");
            return calculateHitByEntityLegacy(player, entity);
        }

        AttributeInstance scaleAttribute = player.getAttribute(Attribute.GENERIC_SCALE);
        double scale = (scaleAttribute != null) ? scaleAttribute.getValue() : 1.0;

        Location entityEyeLocation = ((LivingEntity) entity).getEyeHeight() > 1.0 ?
                ((LivingEntity) entity).getEyeLocation().subtract(0.0, 0.3, 0.0) :
                ((LivingEntity) entity).getEyeLocation().add(0.0, 0.1, 0.0);
        if (entity.getType() == EntityType.PLAYER) entityEyeLocation = ((LivingEntity) entity).getEyeLocation();

        Location playerLocation = player.getLocation();
        Vector direction = entityEyeLocation.getDirection().normalize();

        Location rayHitLocation = traceRay(entityEyeLocation, direction, player, scale);

        if (rayHitLocation != null) {
            double hitY = rayHitLocation.getY();
            double relativeHitY = hitY - playerLocation.getY();
            double relativeYaw = getRelativeYaw(player, rayHitLocation);
            return getHitBodyPart(relativeHitY, relativeYaw, scale);
        }

        else {
            Debug.log("Ray tracing failed to determine what BodyPart was hit, defaulting to legacy calculation...");
            return calculateHitByEntityLegacy(player, entity);
        }

    }

    /**
     * Calculates what BodyPart of a player was hit by an entity via vector calculations
     * @param player The player that was hit by an entity
     * @param entity The entity that hit the player
     * @return The hit BodyPart
     */
    public static BodyPart calculateHitByEntityLegacy(Player player, Entity entity) {
        AttributeInstance scaleAttribute = player.getAttribute(Attribute.GENERIC_SCALE);
        double scale = (scaleAttribute != null) ? scaleAttribute.getValue() : 1.0;
        double relativeHitY = entity.getLocation().getY() - player.getLocation().getY();
        double relativeYaw = getRelativeYaw(player, entity.getLocation());
        return getHitBodyPart(relativeHitY, relativeYaw, scale);
    }

    /**
     * Calculates what BodyPart(s) of a player were hit by a block (e.g. cactus)
     * @param player The player that was hit by a block
     * @param block The block that hit the player
     * @return The hit BodyPart(s)
     */
    public static BodyPart[] calculateHitByBlock(Player player, Block block) {
        AttributeInstance scaleAttribute = player.getAttribute(Attribute.GENERIC_SCALE);
        double scale = (scaleAttribute != null) ? scaleAttribute.getValue() : 1.0;
        double yDiff = (block.getLocation().getY() + 1) - player.getLocation().getY();
        double relativeYaw = getRelativeYaw(player, block.getLocation());
        return determineHitParts(relativeYaw, yDiff, scale);
    }

    /**
     * Utility method to determine what BodyPart(s) were hit by a block
     * @param relativeYaw The relative yaw between player and damager
     * @param yDiff The height difference between player and damager
     * @param scale The scale of the player
     * @return The hit BodyParts
     */
    private static BodyPart[] determineHitParts(double relativeYaw, double yDiff, double scale) {
        Debug.logDev("Relative yaw: " + relativeYaw + ", Height difference: " + yDiff + ", Scale: " + scale);
        if (relativeYaw <= 45 || relativeYaw >= 315) {
            return yDiff >= 2.4 * scale ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.7 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY} :
                            yDiff >= 1.4 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                    yDiff >= 1.25 * scale ? new BodyPart[]{BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                            yDiff >= 0.25 * scale ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT, BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT};
        } else if (relativeYaw >= 135 && relativeYaw <= 225) {
            return yDiff >= 2.4 * scale ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.7 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY} :
                            yDiff >= 1.4 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                    yDiff >= 1.25 * scale ? new BodyPart[]{BodyPart.BODY, BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT} :
                                            yDiff >= 0.25 * scale ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.LEG_RIGHT, BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT, BodyPart.FOOT_RIGHT};
        } else if (relativeYaw > 45 && relativeYaw < 135) {
            return yDiff >= 2.4 * scale ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.7 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_LEFT} :
                            yDiff >= 1.4 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_LEFT, BodyPart.LEG_LEFT} :
                                    yDiff >= 1.25 * scale ? new BodyPart[]{BodyPart.ARM_LEFT, BodyPart.LEG_LEFT} :
                                            yDiff >= 0.25 * scale ? new BodyPart[]{BodyPart.LEG_LEFT, BodyPart.FOOT_LEFT} :
                                                    new BodyPart[]{BodyPart.FOOT_LEFT};
        } else {
            return yDiff >= 2.4 * scale ? new BodyPart[]{BodyPart.HEAD} :
                    yDiff >= 1.7 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_RIGHT} :
                            yDiff >= 1.4 * scale ? new BodyPart[]{BodyPart.HEAD, BodyPart.ARM_RIGHT, BodyPart.LEG_RIGHT} :
                                    yDiff >= 1.25 * scale ? new BodyPart[]{BodyPart.ARM_RIGHT, BodyPart.LEG_RIGHT} :
                                            yDiff >= 0.25 * scale ? new BodyPart[]{BodyPart.LEG_RIGHT, BodyPart.FOOT_RIGHT} :
                                                    new BodyPart[]{BodyPart.FOOT_RIGHT};
        }
    }

    /**
     * Utility method to calculate the relative yaw between an entity and a location
     * @param entity The entity to calculate the relative yaw to a location for
     * @param location The location to which the relative yaw should be calculated
     * @return The relative yaw between the entity and the location
     */
    private static double getRelativeYaw(Entity entity, Location location) {
        float playerYaw = entity.getLocation().getYaw();
        Vector locationVector = location.toVector();
        Vector directionVector = locationVector.subtract(entity.getLocation().toVector()).normalize();
        double locationYaw = Math.toDegrees(Math.atan2(directionVector.getZ(), directionVector.getX())) - 90;
        locationYaw = (locationYaw + 360) % 360;
        double relativeYaw = playerYaw - locationYaw;
        relativeYaw = (relativeYaw + 360) % 360;
        return relativeYaw;
    }

    /**
     * Utility method to determine what BodyPart was hit by an entity based on relative positioning
     * @param relativeHitY The height difference between the player and the damaging entity
     * @param relativeYaw The relative yaw between the player and the damaging entity
     * @param scale The scale of the player
     * @return
     */
    private static BodyPart getHitBodyPart(double relativeHitY, double relativeYaw, double scale) {
        Debug.logDev("Relative hit height: " + relativeHitY + ", Relative yaw: " + relativeYaw + ", Scale: " + scale);

        if (relativeHitY > 1.4 * scale) {
            return BodyPart.HEAD;
        }

        else if (relativeHitY > 0.7 * scale) {
            if (relativeYaw > 45 && relativeYaw < 135) return BodyPart.ARM_LEFT;
            if (relativeYaw > 225 && relativeYaw < 315) return BodyPart.ARM_RIGHT;
            return BodyPart.BODY;
        }

        else if (relativeHitY > 0.25 * scale) {
            if (relativeYaw > 0 && relativeYaw < 180) return BodyPart.LEG_LEFT;
            return BodyPart.LEG_RIGHT;
        }

        else {
            if (relativeYaw > 0 && relativeYaw < 180) return BodyPart.FOOT_LEFT;
            return BodyPart.FOOT_RIGHT;
        }
    }

    /**
     * Utility method to trace a ray from the damaging entities eyes to the damaged player
     * @param start The location where the ray to trace should start (e.g. the damaging entities eyes)
     * @param direction A Vector representing the direction in which the damaging entity is looking
     * @param player The player that was hit by the damaging entity
     * @param scale The scale of the player that was hit
     * @return The location where the traced ray collides with the hit players hitbox
     */
    public static Location traceRay(Location start, Vector direction, Player player, double scale) {
        for (double i = 0; i < Config.raytracing_max_distance; i += Config.raytracing_step_size) {
            Location currentPosition = start.clone().add(direction.clone().multiply(i));
            if (Config.development_mode) Objects.requireNonNull(currentPosition.getWorld()).spawnParticle(Particle.SMALL_FLAME, currentPosition, 1, 0, 0, 0, 0);
            if (isWithinPlayerHitbox(currentPosition, player, scale)) return currentPosition;
        }

        return null;
    }

    /**
     * Utility method to check if a location is within a players hitbox
     * @param location The location that potentially is within the players hitbox
     * @param player The player of which the hitbox should be calculated
     * @param scale The scale of the player
     * @return A boolean representing if the given location is within the given players hitbox
     */
    public static boolean isWithinPlayerHitbox(Location location, Player player, double scale) {
        double playerMinX = player.getLocation().getX() - 0.3 * scale;
        double playerMaxX = player.getLocation().getX() + 0.3 * scale;
        double playerMinY = player.getLocation().getY();
        double playerMaxY = playerMinY + 1.8 * scale;
        double playerMinZ = player.getLocation().getZ() - 0.3 * scale;
        double playerMaxZ = player.getLocation().getZ() + 0.3 * scale;

        double locX = location.getX();
        double locY = location.getY();
        double locZ = location.getZ();

        return (locX >= playerMinX && locX <= playerMaxX) &&
                (locY >= playerMinY && locY <= playerMaxY) &&
                (locZ >= playerMinZ && locZ <= playerMaxZ);
    }

}
