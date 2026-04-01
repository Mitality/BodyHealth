package bodyhealth.effects.effect;

import bodyhealth.Main;
import bodyhealth.core.BodyHealth;
import bodyhealth.core.BodyPart;
import bodyhealth.config.Debug;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectType;
import bodyhealth.util.BodyHealthUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;

public class ATTRIBUTE_MODIFIER implements BodyHealthEffect {

    @Override
    public EffectType getEffectType() {
        return EffectType.PERSISTENT;
    }

    @Override
    public String getIdentifier() {
        return "ATTRIBUTE_MODIFIER";
    }

    @Override
    public String getUsage() {
        return "ATTRIBUTE_MODIFIER / <ATTRIBUTE> / <VALUE> / [OPERATION] / [KEY]";
    }

    @Override
    public void onApply(Player player, BodyPart part, String[] args, boolean isRecovery) {
        if (args.length < 3) {
            Debug.logErr("Effect \"" + args[0].trim() + "\" is missing arguments, check syntax!");
            return;
        }

        Attribute attribute = resolveAttribute(args[1]);
        if (attribute == null) {
            Debug.logErr("Attribute \"" + args[1].trim() + "\" is invalid, check syntax!");
            return;
        }

        double value;
        try {
            value = Double.parseDouble(args[2].trim());
        } catch (NumberFormatException e) {
            Debug.logErr("Attribute value \"" + args[2].trim() + "\" is invalid, check syntax!");
            return;
        }

        AttributeModifier.Operation operation = resolveOperation(args.length > 3 ? args[3] : "");
        NamespacedKey modifierKey = buildModifierKey(args, part, attribute, operation, value);

        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance == null) {
            Debug.logErr("Attribute '" + attribute.getKey() + "' is not applicable to players.");
            return;
        }

        if (attributeInstance.getModifiers().stream().anyMatch(mod -> mod.getKey().equals(modifierKey))) {
            Debug.log("(" + part.name() + ") Attribute modifier '" + modifierKey.getKey() + "' is already active on " + player.getName() + ".");
            return;
        }

        BodyHealthUtils.addAttributeModifier(attributeInstance, new AttributeModifier(modifierKey, value, operation, EquipmentSlotGroup.ANY));
        Debug.log("(" + part.name() + ") Applied attribute modifier '" + modifierKey.getKey() +
                "' to " + player.getName() + "'s " + attribute.getKey().getKey() + " attribute.");
    }

    @Override
    public void onRemove(Player player, BodyPart part, String[] args, boolean isRecovery) {
        if (args.length < 3) return;

        Attribute attribute = resolveAttribute(args[1]);
        if (attribute == null) return;

        double value;
        try {
            value = Double.parseDouble(args[2].trim());
        } catch (NumberFormatException e) {
            return;
        }

        AttributeModifier.Operation operation = resolveOperation(args.length > 3 ? args[3] : "");
        NamespacedKey modifierKey = buildModifierKey(args, part, attribute, operation, value);

        if (isModifierStillNeeded(player, part, modifierKey)) {
            Debug.log("(" + part.name() + ") Keeping attribute modifier '" + modifierKey.getKey() + "' on " + player.getName() + " (still needed by another body part).");
            return;
        }

        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance == null) return;

        attributeInstance.removeModifier(new AttributeModifier(modifierKey, value, operation, EquipmentSlotGroup.ANY));
        Debug.log("(" + part.name() + ") Removed attribute modifier '" + modifierKey.getKey() +
                "' from " + player.getName() + "'s " + attribute.getKey().getKey() + " attribute.");
    }

    private static Attribute resolveAttribute(String raw) {
        String trimmed = raw.trim();

        try { return Attribute.valueOf(trimmed.toUpperCase()); }
        catch (IllegalArgumentException ignored) {}

        String lower = trimmed.toLowerCase();
        String keyPart = lower.startsWith("minecraft:") ? lower.substring("minecraft:".length()) : lower;

        for (Attribute attr : Attribute.values()) {
            if (attr.getKey().getKey().equalsIgnoreCase(keyPart)) return attr;
            if (attr.getKey().toString().equalsIgnoreCase(lower)) return attr;
        }

        return null;
    }

    private static AttributeModifier.Operation resolveOperation(String raw) {
        String upper = raw.trim().toUpperCase();
        if (upper.isEmpty()) return AttributeModifier.Operation.ADD_NUMBER;

        try { return AttributeModifier.Operation.valueOf(upper); }
        catch (IllegalArgumentException ignored) {}

        return switch (upper) {
            case "ADD_VALUE" -> AttributeModifier.Operation.ADD_NUMBER;
            case "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.ADD_SCALAR;
            case "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> {
                Debug.logErr("Attribute modifier operation '" + raw.trim() + "' is invalid, defaulting to add_number!");
                yield AttributeModifier.Operation.ADD_NUMBER;
            }
        };
    }

    private static NamespacedKey buildModifierKey(String[] args, BodyPart part, Attribute attribute, AttributeModifier.Operation operation, double value) {
        if (args.length > 4 && !args[4].trim().isEmpty()) {
            return NamespacedKey.fromString(sanitizeForKey(args[4].trim()), Main.getInstance());
        }
        String attrKey = sanitizeForKey(attribute.getKey().getKey());
        String opKey = sanitizeForKey(operation.name());
        String valKey = sanitizeForKey(String.valueOf(value));
        return NamespacedKey.fromString(part.name().toLowerCase() + "_" + attrKey + "_" + opKey + "_" + valKey, Main.getInstance());
    }

    private static String sanitizeForKey(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9_.]", "_");
    }

    // Returns true if another body part's ongoing effects still require this modifier key.
    private static boolean isModifierStillNeeded(Player player, BodyPart part, NamespacedKey modifierKey) {
        BodyHealth bodyHealth = BodyHealthUtils.getBodyHealth(player);
        for (List<String[]> effectsList : bodyHealth.getOngoingEffects().values()) {
            for (String[] effectParts : effectsList) {
                if (effectParts.length < 3 || !effectParts[0].trim().equalsIgnoreCase("ATTRIBUTE_MODIFIER")) continue;
                Attribute attr = resolveAttribute(effectParts[1]);
                if (attr == null) continue;
                double val;
                try { val = Double.parseDouble(effectParts[2].trim()); } catch (NumberFormatException e) { continue; }
                AttributeModifier.Operation op = resolveOperation(effectParts.length > 3 ? effectParts[3] : "");
                if (modifierKey.equals(buildModifierKey(effectParts, part, attr, op, val))) return true;
            }
        }
        return false;
    }
}
