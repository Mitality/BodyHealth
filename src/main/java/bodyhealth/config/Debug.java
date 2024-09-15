package bodyhealth.config;

import bodyhealth.config.Config;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Debug {
    public static void log(String message) {
        if (Config.debug_mode) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[BodyHealthDebug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    public static void logErr(String message) {
        if (Config.error_logging) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[BodyHealthDebug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
        }
    }
}
