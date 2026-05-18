package bodyhealth.effects.effect;

import bodyhealth.config.Debug;
import bodyhealth.core.BodyPart;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PARTICLE implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.ONE_TIME;
    }

    @Override
    public String getIdentifier() {
        return "PARTICLE";
    }

    @Override
    public String getUsage() {
        return "PARTICLE / <PARTICLE> / [POS] / [COUNT] / [OFFSET] / [SPEED] / [FORCE] / [GLOBAL] / [DATA]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {
        if (args.length <= 1) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        Particle particle;
        try {
            particle = Particle.valueOf(args[1].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            Debug.logErr("Particle \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        Location location = args.length >= 3 && !args[2].trim().isEmpty()
                ? parsePosition(args[2].trim(), player)
                : player.getLocation();
        if (location == null) {
            Debug.logErr("Position \"" + args[2].trim() + "\" is invalid for PARTICLE effect, check syntax!");
            return;
        }

        int count = 1;
        if (args.length >= 4 && !args[3].trim().isEmpty()) {
            try { count = Integer.parseInt(args[3].trim()); }
            catch (NumberFormatException e) {
                Debug.logErr("Count \"" + args[3].trim() + "\" is invalid for PARTICLE effect, check syntax!");
                return;
            }
        }

        double offsetX = 0, offsetY = 0, offsetZ = 0;
        if (args.length >= 5 && !args[4].trim().isEmpty()) {
            String[] offParts = args[4].trim().split("[,\\s]+");
            if (offParts.length != 3) {
                Debug.logErr("Offset \"" + args[4].trim() + "\" is invalid for PARTICLE effect, use format \"x y z\"!");
                return;
            }
            try {
                offsetX = Double.parseDouble(offParts[0]);
                offsetY = Double.parseDouble(offParts[1]);
                offsetZ = Double.parseDouble(offParts[2]);
            } catch (NumberFormatException e) {
                Debug.logErr("Offset \"" + args[4].trim() + "\" is invalid for PARTICLE effect, check syntax!");
                return;
            }
        }

        double speed = 0;
        if (args.length >= 6 && !args[5].trim().isEmpty()) {
            try { speed = Double.parseDouble(args[5].trim()); }
            catch (NumberFormatException e) {
                Debug.logErr("Speed \"" + args[5].trim() + "\" is invalid for PARTICLE effect, check syntax!");
                return;
            }
        }

        boolean force = args.length >= 7 && Boolean.parseBoolean(args[6].trim());
        boolean global = args.length >= 8 && Boolean.parseBoolean(args[7].trim());

        Object data = null;
        if (args.length >= 9 && !args[8].trim().isEmpty()) {
            data = parseData(particle, args[8].trim());
        }

        try {
            if (global && location.getWorld() != null) {
                location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data, force);
            } else {
                player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
            }
        } catch (Exception e) {
            Debug.logErr("Failed to spawn particle \"" + args[1].trim() + "\": " + e.getMessage());
        }
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
    }

    private Location parsePosition(String pos, Player player) {
        String[] tokens = pos.split("[,\\s]+");
        if (tokens.length != 3) return null;

        Location playerLoc = tokens[1].startsWith("^") ? player.getEyeLocation() : player.getLocation();
        boolean hasCarets = false;
        for (String token : tokens) if (token.startsWith("^")) { hasCarets = true; break; }

        if (hasCarets) {
            double yaw = Math.toRadians(playerLoc.getYaw());
            double pitch = Math.toRadians(playerLoc.getPitch());

            Vector left = new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
            Vector forward = new Vector(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
            Vector up = left.clone().crossProduct(forward);

            double localLeft = 0, localUp = 0, localForward = 0;
            double relX = 0, relY = 0, relZ = 0;

            for (int i = 0; i < 3; i++) {
                String token = tokens[i];
                try {
                    if (token.startsWith("^")) {
                        double val = token.length() == 1 ? 0 : Double.parseDouble(token.substring(1));
                        if (i == 0) localLeft = val;
                        else if (i == 1) localUp = val;
                        else localForward = val;
                    } else if (token.startsWith("~")) {
                        double val = token.length() == 1 ? 0 : Double.parseDouble(token.substring(1));
                        if (i == 0) relX = val;
                        else if (i == 1) relY = val;
                        else relZ = val;
                    } else {
                        double absVal = Double.parseDouble(token);
                        if (i == 0) relX = absVal - playerLoc.getX();
                        else if (i == 1) relY = absVal - playerLoc.getY();
                        else relZ = absVal - playerLoc.getZ();
                    }
                } catch (NumberFormatException e) { return null; }
            }

            Vector localOffset = left.clone().multiply(localLeft)
                    .add(up.clone().multiply(localUp))
                    .add(forward.clone().multiply(localForward));
            return playerLoc.clone().add(localOffset.getX() + relX, localOffset.getY() + relY, localOffset.getZ() + relZ);

        } else {
            double[] coords = new double[3];
            double[] base = { playerLoc.getX(), playerLoc.getY(), playerLoc.getZ() };
            for (int i = 0; i < 3; i++) {
                String token = tokens[i];
                try {
                    if (token.startsWith("~")) {
                        coords[i] = base[i] + (token.length() == 1 ? 0 : Double.parseDouble(token.substring(1)));
                    } else {
                        coords[i] = Double.parseDouble(token);
                    }
                } catch (NumberFormatException e) { return null; }
            }
            return new Location(playerLoc.getWorld(), coords[0], coords[1], coords[2]);
        }
    }

    // DUST / REDSTONE ········· → "R G B SIZE" ··············· e.g. "255 0 0 1.0"
    // DUST_COLOR_TRANSITION ··· → "R1 G1 B1 R2 G2 B2 SIZE" ··· e.g. "255 0 0 0 255 0 1.0"
    // ITEM / ITEM_CRACK ······· → "material" ················· e.g. "diamond_sword"
    // BLOCK_CRACK etc. ········ → "block_type" ··············· e.g. "stone"
    // SCULK_CHARGE ············ → "roll" ····················· e.g. "0.5"
    // SHRIEK ·················· → "delay" ···················· e.g. "10"
    @SuppressWarnings("unchecked")
    private <T> T parseData(Particle particle, String raw) {
        Class<?> dataType = particle.getDataType();
        String[] parts = raw.split("[,\\s]+");

        if (dataType == Particle.DustOptions.class) {
            if (parts.length < 4) {
                Debug.logErr("DUST data \"" + raw + "\" is invalid, use format \"R G B SIZE\"!");
                return null;
            }
            try {
                int r = Integer.parseInt(parts[0]);
                int g = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                float size = Float.parseFloat(parts[3]);
                return (T) new Particle.DustOptions(Color.fromRGB(r, g, b), size);
            } catch (NumberFormatException e) {
                Debug.logErr("DUST data \"" + raw + "\" is invalid, use format \"R G B SIZE\"!");
                return null;
            }
        }

        if (dataType == Particle.DustTransition.class) {
            if (parts.length < 7) {
                Debug.logErr("DUST_COLOR_TRANSITION data \"" + raw + "\" is invalid, use format \"R1 G1 B1 R2 G2 B2 SIZE\"!");
                return null;
            }
            try {
                int r1 = Integer.parseInt(parts[0]), g1 = Integer.parseInt(parts[1]), b1 = Integer.parseInt(parts[2]);
                int r2 = Integer.parseInt(parts[3]), g2 = Integer.parseInt(parts[4]), b2 = Integer.parseInt(parts[5]);
                float size = Float.parseFloat(parts[6]);
                return (T) new Particle.DustTransition(Color.fromRGB(r1, g1, b1), Color.fromRGB(r2, g2, b2), size);
            } catch (NumberFormatException e) {
                Debug.logErr("DUST_COLOR_TRANSITION data \"" + raw + "\" is invalid, use format \"R1 G1 B1 R2 G2 B2 SIZE\"!");
                return null;
            }
        }

        if (dataType == ItemStack.class) {
            try {
                Material material = Material.valueOf(parts[0].toUpperCase());
                return (T) new ItemStack(material);
            } catch (IllegalArgumentException e) {
                Debug.logErr("Item data \"" + raw + "\" is invalid, use a valid material name!");
                return null;
            }
        }

        if (dataType == org.bukkit.block.data.BlockData.class) {
            try {
                return (T) Bukkit.createBlockData(parts[0].toLowerCase());
            } catch (IllegalArgumentException e) {
                Debug.logErr("Block data \"" + raw + "\" is invalid, use a valid block type!");
                return null;
            }
        }

        if (dataType == Float.class) {
            try {
                return (T) Float.valueOf(parts[0]);
            } catch (NumberFormatException e) {
                Debug.logErr("SCULK_CHARGE data \"" + raw + "\" is invalid, use a float value!");
                return null;
            }
        }

        if (dataType == Integer.class) {
            try {
                return (T) Integer.valueOf(parts[0]);
            } catch (NumberFormatException e) {
                Debug.logErr("SHRIEK data \"" + raw + "\" is invalid, use an integer value!");
                return null;
            }
        }

        return null;
    }

}
