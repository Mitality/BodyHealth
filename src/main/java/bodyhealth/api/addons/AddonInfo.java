package bodyhealth.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Format proudly stolen from BreweryX, check it out:
// https://www.spigotmc.org/resources/breweryx.114777/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddonInfo {
    String author() default "Unknown";
    String version() default "0";
    String description() default "";
    String name() default "UnknownAddon";
}