package bodyhealth.api.addons;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

public class DummyPluginWrapper implements Plugin {
    private final File jarFile;

    public DummyPluginWrapper(File jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public InputStream getResource(@NotNull String name) {
        try (JarInputStream jar = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;

            while ((entry = jar.getNextJarEntry()) != null) {
                if (entry.getName().equals(name)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = jar.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    return new ByteArrayInputStream(out.toByteArray());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public void saveConfig() {}
    @Override public void saveDefaultConfig() {}
    @Override public void saveResource(@NotNull String s, boolean b) {}
    @Override public void reloadConfig() {}
    @Override public @NotNull JavaPluginLoader getPluginLoader() { throw new UnsupportedOperationException(); }
    @Override public @NotNull Server getServer() { throw new UnsupportedOperationException(); }
    @Override public boolean isEnabled() { return true; }
    @Override public @NotNull String getName() { return "DummyAddon"; }
    @Override public @NotNull File getDataFolder() { return new File("plugins/BodyHealth/addons/data"); }
    @Override public @NotNull PluginDescriptionFile getDescription() { return new PluginDescriptionFile("DummyAddon", "1.0", "dummy.Main"); }
    @Override public @NotNull FileConfiguration getConfig() { return YamlConfiguration.loadConfiguration(jarFile); }
    @Override public void onDisable() {}
    @Override public void onLoad() {}
    @Override public void onEnable() {}
    @Override public boolean isNaggable() { return false; }
    @Override public void setNaggable(boolean b) {}
    @Override public ChunkGenerator getDefaultWorldGenerator(@NotNull String s, String s1) { return null; }
    @Override public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull String s, @Nullable String s1) { return null; }
    @Override public @NotNull Logger getLogger() { return Logger.getLogger("DummyAddon"); }
    @Override public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) { return false; }
    @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) { return List.of(); }
}
