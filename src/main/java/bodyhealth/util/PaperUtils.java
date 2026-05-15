package bodyhealth.util;

import bodyhealth.config.Config;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import java.lang.reflect.Method;

public final class PaperUtils {

    private static final Method ADD_TRANSIENT_MODIFIER;

    static {
        Method m = null;
        try {
            m = AttributeInstance.class.getMethod("addTransientModifier", AttributeModifier.class);
        } catch (NoSuchMethodException ignored) {}
        ADD_TRANSIENT_MODIFIER = m;
    }

    /**
     * Adds an AttributeModifier to an AttributeInstance, using addTransientModifier (Paper/Folia)
     * when the config option is enabled and the method is available, falling back to addModifier otherwise
     * @param attribute The AttributeInstance to add the modifier to
     * @param modifier The AttributeModifier to add
     */
    public static void addAttributeModifier(AttributeInstance attribute, AttributeModifier modifier) {
        if (Config.use_transient_modifiers && ADD_TRANSIENT_MODIFIER != null) {
            try {
                ADD_TRANSIENT_MODIFIER.invoke(attribute, modifier);
                return;
            } catch (Exception ignored) {}
        }
        attribute.addModifier(modifier);
    }

}
