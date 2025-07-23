package bodyhealth.util;

import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import bodyhealth.config.Lang;
import bodyhealth.data.DataManager;
import bodyhealth.depend.BetterHud;
import bodyhealth.depend.WorldGuard;
import bodyhealth.effects.EffectHandler;
import bodyhealth.core.BodyHealth;
import bodyhealth.Main;
import bodyhealth.core.BodyPart;
import bodyhealth.core.BodyPartState;
import com.tchristofferson.configupdater.ConfigUpdater;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BodyHealthUtils {

    /**
     * Reloads the plugin
     */
    public static boolean reloadSystem() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            EffectHandler.removeEffectsFromPlayer(player);
        }

        try {

            // Reload and update config
            Main.getInstance().saveDefaultConfig();
            File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
            try {
                ConfigUpdater.update(Main.getInstance(), "config.yml", configFile);
            } catch (IOException e) {
                Debug.logErr("Could not update your configuration: " + e.getMessage());
                return false;
            }
            Main.getInstance().reloadConfig();

            // Load config internally
            Config.load(Main.getInstance().getConfig());

            // Ensure language directory exists
            File languageDir = new File(Main.getInstance().getDataFolder(), "language");
            if (!languageDir.exists()) languageDir.mkdirs();

            // Save and update language files
            for (String langFileName : Main.getLanguages()) {
                if (!new File(languageDir, langFileName).exists()) Main.getInstance().saveResource("language/" + langFileName, false);
                ConfigUpdater.update(Main.getInstance(), "language/" + langFileName, new File(languageDir, langFileName));
            }

            // Get the selected language file
            File languageFile = new File(languageDir, Config.language + ".yml");
            if (!languageFile.exists()) {
                Debug.logErr("Language " + Config.language + " doesn't exist in your language folder! Defaulting to English (en).");
                languageFile = new File(languageDir, "en.yml");
                if (!languageFile.exists()) Main.getInstance().saveResource("language/en.yml", false); // Should not be necessary
            }

            // Load language configuration
            FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);

            // Load language internally
            Lang.load(languageConfig);

            // Reload DataManager
            DataManager.load();

        } catch (Exception e) {
            Debug.logErr("Could not reload your configuration!");
            Debug.logErr(e);
            return false;
        }

        // Reload addons
        Main.getAddonManager().reloadAddons();

        for (Player player : Bukkit.getOnlinePlayers()) {
            EffectHandler.addEffectsToPlayer(player);
        }

        return true;
    }

    /**
     * Checks if BodyHealth should be enabled for a given player
     * @param player The player for which to check if BodyHealth should be enabled
     * @return A boolean representing if BodyHealth should be enabled for the given player
     */
    public static boolean isSystemEnabled(Player player) {
        if (!isSystemEnabled(player.getWorld())) return false;
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null
                && Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled())
            return WorldGuard.isSystemEnabled(player);
        return true;
    }

    /**
     * Checks if BodyHealth should be enabled in a given world
     * @param world The world for which to check if BodyHealth should be enabled
     * @return A boolean representing if BodyHealth should be enabled in the given world
     */
    public static boolean isSystemEnabled(World world) {
        if (world == null) return false; // Shouldn't ever happen
        return Config.wold_blacklist_whitelist_mode == Config.world_blacklist_worlds.contains(world.getName());
    }

    /**
     * Retrieves a given players BodyHealth object
     * @param player The player of which the BodyHealth object should be retrieved
     * @return The given players BodyHealth object
     */
    public static BodyHealth getBodyHealth(Player player) {
        return DataManager.getBodyHealth(player.getUniqueId());
    }

    /**
     * Applies damage to all BodyParts for a BodyHealth object while taking the fine-tuning section of BodyHealth's config into account
     * @param bodyHealth The BodyHealth object to which the damage should be applied to
     * @param cause The DamageCause that caused the player to take damage
     * @param damage The final amount of damage the player took
     */
    public static void applyDamageWithConfig(BodyHealth bodyHealth, EntityDamageEvent.DamageCause cause, double damage, boolean force) {
        for (BodyPart part : BodyPart.values()) {
            applyDamageWithConfig(bodyHealth, cause, damage, part, force);
        }
    }

    /**
     * Applies damage to a specific BodyPart for a BodyHealth object while taking the fine-tuning section of BodyHealth's config into account
     * @param bodyHealth The BodyHealth object to which the damage should be applied to
     * @param cause The DamageCause that caused the player to take damage
     * @param damage The final amount of damage the player took
     * @param part The BodyPart to which the damage should be applied
     */
    public static void applyDamageWithConfig(BodyHealth bodyHealth, EntityDamageEvent.DamageCause cause, double damage, BodyPart part, boolean force) {

        Player player = Bukkit.getPlayer(bodyHealth.getPlayerUUID());
        if (player == null) {
            Debug.logErr("Tried to modify data of a player that isn't online. This should never happen automatically and is a bug!");
            return;
        }

        ConfigurationSection config = Config.body_damage;
        double finalPercentage = -1;

        List<Map<?, ?>> overrides = config.getMapList("overrides");
        for (Map<?, ?> override : overrides) {
            String permission = (String) override.get("permission");
            if (permission == null || !player.hasPermission(permission)) continue;

            Object partEntries = override.get(part.name());
            if (!(partEntries instanceof List<?> entries)) continue;

            for (Object obj : entries) {
                if (!(obj instanceof String)) continue;

                String[] data = ((String) obj).split(" ");
                if (data.length < 2) continue;

                String damageType = data[0];
                if (!cause.name().equalsIgnoreCase(damageType)) continue;

                try {
                    finalPercentage = Double.parseDouble(data[1].replace("%", ""));
                } catch (NumberFormatException e) {
                    Debug.logErr("Invalid percentage in body-damage override: " + data[1]);
                }
            }
        }

        if (finalPercentage == -1 && config.getKeys(false).contains(part.name())) {
            for (String entry : config.getStringList(part.name())) {
                String[] data = entry.split(" ");
                if (data.length < 2) continue;

                String damageType = data[0];
                if (!cause.name().equalsIgnoreCase(damageType)) continue;

                try {
                    finalPercentage = Double.parseDouble(data[1].replace("%", ""));
                } catch (NumberFormatException e) {
                    Debug.logErr("Invalid percentage in body-damage default config: " + data[1]);
                }
            }
        }

        if (finalPercentage >= 0) {
            bodyHealth.applyDamage(part, damage * (finalPercentage / 100.0), force);
        } else {
            bodyHealth.applyDamage(part, damage, force); // Not configured - default to 100%
        }
    }

    /**
     * Calculates the current BodyPartState for a given Part in a given BodyHealth object
     * @param bodyHealth The BodyHealth object to get the parts health from
     * @param part The BodyPart from which the BodyPartState should be calculated
     * @return The current BodyHealthState of the given BodyPart
     */
    public static BodyPartState getBodyHealthState(BodyHealth bodyHealth, BodyPart part) {
        double currentHealth = bodyHealth.getHealth(part);
        if (currentHealth == 0) {
            return BodyPartState.BROKEN; // No health left
        } else if (currentHealth <= 25) {
            return BodyPartState.DAMAGED; // Health is low, below 25%
        } else if (currentHealth <= 50) {
            return BodyPartState.INTERMEDIATE; // Health is between 25% and 50%
        } else if (currentHealth < 100) {
            return BodyPartState.NEARLYFULL; // Health is between 50% and full
        } else {
            return BodyPartState.FULL; // Health is at maximum
        }
    }

    /**
     * Checks if the given value represents a valid BodyPart
     * @param value The value to analyze - who could have thought
     * @return A Boolean representing if the given value represents a valid BodyPart
     */
    public static boolean isValidBodyPart(String value) {
        for (BodyPart part : BodyPart.values()) {
            if (part.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the maximum amount of health that a players BodyPart should be able to have
     * @param part The BodyPart to calculate the maximum amount of health for
     * @param player The player to which the BodyPart belongs
     * @return The maximum amount of health for the given BodyPart
     */
    public static double getMaxHealth(BodyPart part, Player player) {
        double maxHealth = -1;

        List<Map<?, ?>> overrides = Config.body_health.getMapList("overrides");
        for (Map<?, ?> override : overrides) {

            String permission = (String) override.get("permission");
            if (permission == null || !player.hasPermission(permission)) continue;

            Object overrideValue = override.get(part.name());
            if (overrideValue == null) continue;

            maxHealth = parseHealthExpression(player, overrideValue);
        }

        if (maxHealth == -1) {
            Object defaultValue = Config.body_health.get(part.name());
            if (defaultValue != null) {
                maxHealth = parseHealthExpression(player, defaultValue);
            }
        }

        if (maxHealth > 0) return maxHealth;
        Debug.logErr("Invalid maxHealth for part " + part.name() + "! Defaulting to '%PlayerMaxHealth% / 2'");
        return player.getMaxHealth() / 2;
    }

    /**
     * Parses a health value from Integer/Double/String to a Double, evaluating expressions
     * @param player The player to which the BodyPart belongs to which the health value belongs
     * @param value The health value of a BodyPart in Integer, Double or String (expression) form
     * @return A Double representing how much health a BodyPart should be able to have at best
     */
    public static double parseHealthExpression(Player player, Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            String expression = ((String) value)
                .replace("%PlayerMaxHealth%", String.valueOf(player.getMaxHealth()))
                .replace("%PlayerCurrentHealth%", String.valueOf(player.getHealth()));
                // Why on earth did I add %PlayerCurrentHealth%... Don't you dare, silly!

            double result = evaluateExpression(expression);
            if (result == -1) {
                Debug.logErr("Invalid health expression: \"" + expression + "\". Defaulting to %PlayerMaxHealth% / 2");
                return player.getMaxHealth() / 2;
            }
            return result;
        } else {
            return -1;
        }
    }

    /**
     * Utility method to evaluate a mathematical expression
     * @param expression The expression to evaluate
     * @return The result of the evaluation
     */
    public static double evaluateExpression(String expression) {
        try {
            Expression e = new ExpressionBuilder(expression).build();
            return e.evaluate();
        } catch (Exception e) {
            //Debug.logErr(e);
            return -1;
        }
    }

    /**
     * Utility method to check if a player should be able to sprint
     * @param player The player to calculate this for
     * @return A boolean representing if the player should currently be able to sprint normally
     */
    public static boolean canPlayerSprint(Player player) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0
                        && effectParts[0].trim().equalsIgnoreCase("PREVENT_SPRINT")) return false;
            }
        }
        return true;
    }

    /**
     * Utility method to check if a player should be able to walk
     * @param player The player to calculate this for
     * @return A boolean representing if the player should currently be able to walk
     */
    public static boolean canPlayerWalk(Player player) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0
                        && effectParts[0].trim().equalsIgnoreCase("PREVENT_WALK")) return false;
            }
        }
        return true;
    }

    /**
     * Utility method to check if a player should be able to jump
     * @param player The player to calculate this for
     * @return A boolean representing if the player should currently be able to jump
     */
    public static boolean canPlayerJump(Player player) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0
                        && effectParts[0].trim().equalsIgnoreCase("PREVENT_JUMP")) return false;
            }
        }
        return true;
    }

    /**
     * Utility method to check if a player should be able to interact with something
     * @param player The player to calculate this for
     * @param hand The equipment slot to calculate this for
     * @return A boolean representing if the player should currently be able to interact with something
     */
    public static boolean canPlayerInteract(Player player, EquipmentSlot hand) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return true;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase("PREVENT_INTERACT")
                        && effectParts[1].trim().equalsIgnoreCase(hand.name())) return false;
            }
        }
        return true;
    }

    /**
     * Utility method to check if a player currently has a specific potion effect type
     * @param player The player to calculate this for
     * @param effect The effect type to check for
     * @return A boolean representing if the player currently has the given potion effect type
     */
    public static boolean hasPlayerPotionEffect(Player player, PotionEffectType effect) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return false;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0 && effectParts[0].trim().equalsIgnoreCase("POTION_EFFECT")
                        && effectParts[1].trim().equalsIgnoreCase(effect.getKey().getKey())) return true;
            }
        }
        return false;
    }

    /**
     * Utility method to retrieve the highest amplifier of a potion effect that a player should currently have
     * @param player The player to calculate this for
     * @param effect The effect to calculate this for
     * @return The highest amplifier of thew given potion effect that the given player should currently have
     */
    public static int getHighestPotionEffectAmplifier(Player player, PotionEffectType effect) {
        BodyHealth bodyHealth = getBodyHealth(player);
        if (bodyHealth.getOngoingEffects().isEmpty()) return -1;
        int highestAmplifier = -1;
        for (Map.Entry<BodyPart, List<String[]>> entry : bodyHealth.getOngoingEffects().entrySet()) {
            List<String[]> effectsList = entry.getValue();
            for (String[] effectParts : effectsList) {
                if (effectParts.length > 0
                        && effectParts[0].trim().equalsIgnoreCase("POTION_EFFECT")
                        && effectParts[1].trim().equalsIgnoreCase(effect.getKey().getKey())
                        && Integer.parseInt(effectParts[2].trim()) > highestAmplifier)
                    highestAmplifier = Integer.parseInt(effectParts[2].trim());
            }
        }
        return highestAmplifier;
    }

    /**
     * Unnecessary method to remove invalid (leftover) effects from a players BodyHealth -> ongoingEffects.
     * This should never be necessary, meaning if this method does catch something, I messed up, but I'll
     * leave it here as an extra layer of safety in case that should ever happen ¯\_(ツ)_/¯
     */
    public static void validateEffects(Player player) {
        for (Map.Entry<BodyPart, List<String[]>> entry : getBodyHealth(player).getOngoingEffects().entrySet()) {

            BodyPart bodyPart = entry.getKey();
            List<String[]> effectsList = entry.getValue();

            ConfigurationSection bodyPartConfig = Config.effects.getConfigurationSection(bodyPart.name());
            if (bodyPartConfig == null) continue; // Nothing configured for this BodyPart

            List<String[]> validEffects = new ArrayList<>();
            for (String healthState : bodyPartConfig.getKeys(false)) {
                List<String> configEffects = bodyPartConfig.getStringList(healthState);
                for (String effect : configEffects) {
                    validEffects.add(Arrays.stream(effect.split("/"))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toArray(String[]::new));
                }
            }

            for (String[] effectParts : new ArrayList<>(effectsList)) {
                String[] normalizedEffect = Arrays.stream(effectParts)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toArray(String[]::new);

                if (validEffects.stream().anyMatch(valid -> Arrays.equals(valid, normalizedEffect))) continue;
                getBodyHealth(player).removeFromOngoingEffects(bodyPart, effectParts);
                Debug.log("Removing invalid effect \"" + Arrays.toString(effectParts) + "\" from player " + player.getName() + " for body part " + bodyPart.name());
            }
        }
    }

}
