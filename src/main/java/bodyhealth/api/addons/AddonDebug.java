package bodyhealth.api.addons;

import bodyhealth.config.Config;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class AddonDebug {

    private final String addonName;

    public AddonDebug(Class<? extends BodyHealthAddon> clazz) {
        this.addonName = clazz.getSimpleName();
    }

    /**
     * Send a debug message when debug mode is enabled
     * Output Format: [<AddonName>Debug - ClassThatCalledThis] Message
     * @param message The message to log
     */
    public void log(String message) {
        if (Config.debug_mode) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[" + addonName + "Debug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled
     * Output Format: [<AddonName>Error - ClassThatCalledThis] Message
     * @param message The error message to log
     */
    public void logErr(String message) {
        if (Config.error_logging) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[" + addonName + "Error - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
        }
    }

    /**
     * Send a debug message when developer mode is enabled
     * Output Format: [<AddonName>DevDebug - ClassThatCalledThis] Message
     * @param message The message to log
     */
    public void logDev(String message) {
        if (Config.development_mode) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[" + addonName + "DevDebug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

}
