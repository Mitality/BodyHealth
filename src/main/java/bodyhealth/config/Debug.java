package bodyhealth.config;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Debug {

    /**
     * Send a debug message when debug mode is enabled
     * Output Format: [BodyHealthDebug - ClassThatCalledThis] Message
     * @param message The message to log
     */
    public static void log(String message) {
        if (Config.debug_mode) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[BodyHealthDebug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled
     * Output Format: [BodyHealthError - ClassThatCalledThis] Message
     * @param message The error message to log
     */
    public static void logErr(String message) {
        if (Config.error_logging) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[BodyHealthError - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
        }
    }

    /**
     * Send a debug message when developer mode is enabled
     * Output Format: [BodyHealthDevDebug - ClassThatCalledThis] Message
     * @param message The message to log
     */
    public static void logDev(String message) {
        if (Config.development_mode) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callingClass = stackTrace[2].getClassName().split("\\.")[stackTrace[2].getClassName().split("\\.").length - 1];
            String prefixedMessage = "[BodyHealthDevDebug - " + callingClass + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }
}
