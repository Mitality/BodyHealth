package bodyhealth.api.addons;

import bodyhealth.config.Config;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class AddonDebug {

    private final String addonName;

    public AddonDebug(Class<? extends BodyHealthAddon> clazz) {
        this.addonName = clazz.getAnnotation(AddonInfo.class).name();
    }

    /**
     * Send a debug message when debug mode is enabled.
     * Output Format: [<AddonName>Debug - ClassThatCalledThis:line] Message
     * @param message The message to log
     */
    public void log(String message) {
        if (Config.debug_mode) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[" + addonName + "Debug - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [<AddonName>Error - ClassThatCalledThis:line] Message
     * @param message The error message to log
     */
    public void logErr(String message) {
        if (Config.error_logging) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[" + addonName + "Error - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [<AddonName>Error - ClassThatCalledThis:line] Message
     * @param throwable The error element to log, including its stack trace
     */
    public void logErr(Throwable throwable) {
        if (Config.error_logging) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefix = "[" + addonName + "Error - " + className + ":" + caller.getLineNumber() + "] ";
            Bukkit.getLogger().log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
        }
    }

    /**
     * Send a debug message when developer mode is enabled.
     * Output Format: [<AddonName>DevDebug - ClassThatCalledThis:line] Message
     * @param message The message to log
     */
    public void logDev(String message) {
        if (Config.development_mode) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[" + addonName + "DevDebug - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

}
