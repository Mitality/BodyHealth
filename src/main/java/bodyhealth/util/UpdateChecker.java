package bodyhealth.util;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private volatile boolean updateAvailable = false;
    private String updateLink = null;
    private String latestVer = null;

    private final String currentVersion;
    private final String resourceName;
    private final String projectId;
    private final Gson gson;

    public UpdateChecker(String resourceName, String projectId, String currentVersion) {
        this.currentVersion = currentVersion;
        this.resourceName = resourceName;
        this.projectId = projectId;
        this.gson = new Gson();
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectId + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "BodyHealth Update Checker");
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                JsonArray versions = gson.fromJson(jsonBuilder.toString(), JsonArray.class);

                if (versions.isEmpty()) {
                    Debug.logErr("No versions for resource with id '" + projectId + "' found on Modrinth!");
                    return;
                }

                JsonObject latest = versions.get(0).getAsJsonObject();
                String latestVersion = latest.get("version_number").getAsString();

                if (isNewerVersion(this.currentVersion, latestVersion)) {

                    this.updateAvailable = true;
                    this.latestVer = latestVersion;
                    this.updateLink = "https://modrinth.com/plugin/" + this.projectId + "/version/latest";

                    // Synchronized to prevent interference with addons
                    synchronized (Main.getMutexObj()) {
                        Component message = Component.text()
                            .append(Component.text("[BodyHealth] Update available!", NamedTextColor.GRAY))
                            .appendNewline()
                            .appendNewline()
                            .append(Component.text("  A new version of ", NamedTextColor.GREEN))
                            .append(Component.text(this.resourceName, NamedTextColor.GOLD))
                            .append(Component.text(" is available: ", NamedTextColor.GREEN))
                            .append(Component.text("v" + latestVersion, NamedTextColor.GOLD))
                            .appendNewline()
                            .append(Component.text("  Please update at your earliest convenience to prevent known issues.", NamedTextColor.DARK_GREEN))
                            .appendNewline()
                            .append(Component.text("  https://modrinth.com/plugin/" + this.projectId + "/version/latest", NamedTextColor.GRAY))
                            .appendNewline()
                            .append(Component.text(" ")) // Some consoles would ignore the last line without this
                            .build();
                        Main.getAdventure().console().sendMessage(message);
                    }

                } else {
                    Bukkit.getLogger().log(Level.INFO, "[BodyHealth] You are running the latest version of " + resourceName + " (v" + currentVersion + ")!");
                }

            } catch (Exception e) {
                Debug.logErr("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Checks for updates every X hours
     * @param hours how often to check
     */
    public UpdateChecker checkEveryXHours(int hours) {
        int ticks = hours * 60 * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this::checkForUpdates, ticks, ticks);
        return this;
    }

    /**
     * Checks for updates once
     */
    public UpdateChecker checkNow() {
        checkForUpdates();
        return this;
    }

    /**
     * Extracts the main version from given version strings and checks if one version is newer than another
     * @param currentVersion The current version of a resource (e.g. "ResourceName-v1.0.0-dev3" -> 1.0.0)
     * @param latestVersion The latest retrieved version of a resource (e.g. "v1.0.1-pre1" -> 1.0.1)
     * @return true if the latest retrieved version is deemed newer than the current version
     */
    private static boolean isNewerVersion(String currentVersion, String latestVersion) {
        String current = extractMainVersion(currentVersion);
        String latest = extractMainVersion(latestVersion);

        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");

        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int curr = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int lat = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (lat > curr) return true;
            if (lat < curr) return false;
        }

        // If base versions are equal, compare suffixes
        String currSuffix = extractSuffix(currentVersion);
        String latSuffix = extractSuffix(latestVersion);
        if (currSuffix.equals(latSuffix)) return false;

        // No suffix (=stable release)
        if (currSuffix.isEmpty() && !latSuffix.isEmpty()) return false;
        if (!currSuffix.isEmpty() && latSuffix.isEmpty()) return true;

        if (Config.releases_only) return false;

        // Deem "pre" newer than "dev"
        if (currSuffix.startsWith("dev") && latSuffix.startsWith("pre")) return true;
        if (currSuffix.startsWith("pre") && latSuffix.startsWith("dev")) return false;

        // Same type, compare numbers
        int currNum = extractSuffixNumber(currSuffix);
        int latNum = extractSuffixNumber(latSuffix);

        return latNum > currNum;
    }

    /**
     * Extracts the first digit-based version (e.g., 1.2, 1.2.3.4) from a string
     * @param input the version string to extract the main version from
     * @return the main version, extracted from the given string
     */
    private static String extractMainVersion(String input) {
        Pattern pattern = Pattern.compile("(\\d+)(\\.\\d+){0,3}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return "0"; // fallback
    }

    /**
     * Extracts the suffix part of a version string (e.g. "dev1" from PluginName-v1.0.0-dev1)
     * @param version A full version string (e.g. "PluginName_1.0.0", or "Test1234pre1")
     * @return The suffix part of the given version string or an empty string if none
     */
    private static String extractSuffix(String version) {
        Matcher m = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?(?:[-_]?([a-zA-Z]+\\d*))?").matcher(version);
        return m.find() && m.group(1) != null ? m.group(1).toLowerCase() : "";
    }

    /**
     * Extracts the suffix number of a given suffix string (e.g. 3 from "dev3" or "pre3")
     * @param suffix A suffix string like dev_3, pre1, or dev-2
     * @return The extracted suffix number
     */
    private static int extractSuffixNumber(String suffix) {
        Matcher m = Pattern.compile("\\d+").matcher(suffix);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }

    /**
     * Checks whether an update is available
     * @return whether an update is available
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Retrieves the name of the resource this UpdateChecker instance belongs to
     * @return the name of the resource this UpdateChecker instance belongs to
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Retrieves a link to the latest version of the resource on Modrinth
     * @return A link where the latest version can be obtained
     */
    public String getUpdateLink() {
        return updateLink;
    }

    /**
     * Retrieves the latest retrieved version of the resource
     * @return the latest retrieved version of the resource
     */
    public String getLatestVersion() {
        return latestVer;
    }

}
