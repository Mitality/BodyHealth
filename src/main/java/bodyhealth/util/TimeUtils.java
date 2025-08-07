package bodyhealth.util;

import bodyhealth.config.Debug;

public class TimeUtils {

    /**
     * Converts a given time string into a number of ticks (e.g. "10s" -> 200)
     * @param timeString A time string to parse, containing a time interval like 2m
     * @return The number of ticks equal to the given time string (e.g. 20 for "1s")
     */
    public static long convertToTicks(String timeString) {

        if (timeString == null || timeString.trim().isEmpty()) {
            Debug.logErr("Failed to convert '" + timeString + "' into ticks! Invalid time interval?");
            return 0;
        }

        String input = timeString.trim().toLowerCase();
        String numberPart = input.replaceAll("[^0-9]", "");
        String unitPart = input.replaceAll("[0-9\\s]", "");

        if (numberPart.isEmpty()) {
            Debug.logErr("Failed to convert '" + timeString + "' into ticks! Invalid time interval?");
            return 0;
        }

        long number = Long.parseLong(numberPart);
        if (unitPart.isEmpty() || unitPart.equals("t") || unitPart.equals("tick") || unitPart.equals("ticks")) {
            return number;
        }

        return switch (unitPart) {
            case "s", "sec", "secs", "second", "seconds" -> number * 20;
            case "m", "min", "mins", "minute", "minutes" -> number * 60 * 20;
            case "h", "hr", "hrs", "hour", "hours" -> number * 60 * 60 * 20;
            case "d", "day", "days" -> number * 24 * 60 * 60 * 20; // I could stop here, but where's the fun in that
            case "y", "year", "years" -> number * 365 * 24 * 60 * 60 * 20;
            case "dec", "decades" -> number * 10 * 365 * 24 * 60 * 60 * 20;
            default -> number;
        };
    }
}
