package bodyhealth.config;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Debug {

    /**
     * Send a debug message when debug mode is enabled.
     * Output Format: [BodyHealthDebug - ClassThatCalledThis:line] Message
     * @param message The message to log
     */
    public static void log(String message) {
        if (Config.debug_mode) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[BodyHealthDebug - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [BodyHealthError - ClassThatCalledThis:line] Message
     * @param message The error message to log
     */
    public static void logErr(String message) {
        if (Config.error_logging) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[BodyHealthError - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
        }
    }

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [BodyHealthError - ClassThatCalledThis:line] Message
     * @param throwable The error element to log, including its stack trace
     */
    public static void logErr(Throwable throwable) {
        if (Config.error_logging) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefix = "[BodyHealthError - " + className + ":" + caller.getLineNumber() + "] ";
            Bukkit.getLogger().log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
        }
    }

    /**
     * Send a debug message when developer mode is enabled.
     * Output Format: [BodyHealthDevDebug - ClassThatCalledThis:line] Message
     * @param message The message to log
     */
    public static void logDev(String message) {
        if (Config.development_mode) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
            String prefixedMessage = "[BodyHealthDevDebug - " + className + ":" + caller.getLineNumber() + "] " + message;
            Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
        }
    }

    /**
     * Send message unconditionally and without extra class info
     * Output Format: [BodyHealth] Message
     * @param message The message to log
     */
    public static void logRaw(String message) {
        String prefixedMessage = "[BodyHealth] " + message;
        Bukkit.getLogger().log(Level.WARNING, prefixedMessage);
    }

}
