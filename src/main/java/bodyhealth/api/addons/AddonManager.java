package bodyhealth.api.addons;

import bodyhealth.Main;
import bodyhealth.config.Config;
import bodyhealth.config.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class AddonManager extends ClassLoader {

    private final File addonsDir;
    private final static List<BodyHealthAddon> addons = new ArrayList<>();

    public AddonManager(Main main) {
        this.addonsDir = new File(main.getDataFolder(), "addons");
        if (!addonsDir.exists()) addonsDir.mkdirs();
    }

    /**
     * Unloads all BodyHealthAddons
     */
    public void unloadAddons() {
        for (BodyHealthAddon addon : addons) {
            try {
                addon.onAddonDisable();
                addon.unregisterListeners();
                addon.unregisterCommands();
                addon.getAddonDebug().log("Addon successfully disabled");
            } catch (Throwable t) {
                Debug.logErr("Failed to disable addon " + addon.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }

    /**
     * Unloads a specific BodyHealthAddon
     * @param addon The addon to unload
     */
    public void unloadAddon(BodyHealthAddon addon) {
        try {
            addon.onAddonDisable();
            addon.unregisterListeners();
            addon.unregisterCommands();
            addon.getAddonDebug().log("Addon successfully disabled");
        } catch (Throwable t) {
            Debug.logErr("Failed to disable addon " + addon.getClass().getSimpleName() + ": " + t.getMessage());
        }
        addons.remove(addon);
    }

    /**
     * Reloads all BodyHealthAddons
     */
    public void reloadAddons() {
        for (BodyHealthAddon addon : addons) {
            try {
                addon.onBodyHealthReload();
            } catch (Throwable t) {
                Debug.logErr("Failed to reload addon " + addon.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
        if (!addons.isEmpty()) Debug.log("Reloaded " + addons.size() + " addon(s)");
    }

    /**
     * Retrieves a list of all currently enabled BodyHealthAddons
     * @return All currently enabled BodyHealthAddons
     */
    public List<BodyHealthAddon> getAddons() {
        return addons;
    }

    /**
     * Loads all BodyHealthAddons
     */
    public void loadAddons() {
        File[] files = addonsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            loadAddon(file);
        }

        for (BodyHealthAddon addon : addons) {
            try {
                addon.onAddonEnable();
            } catch (Throwable t) {
                Debug.logErr("Failed to enable addon " + addon.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
        if (!addons.isEmpty()) Debug.log("Loaded " + addons.size() + " addon(s)");
    }

    /**
     * Loads a specific BodyHealthAddon
     * @param file The addon to load
     */
    public void loadAddon(File file) {
        try {
            List<Class<?>> classes = loadAllClassesFromJar(file);

            for (Class<?> clazz : classes) {

                // Check if the class is a concrete subclass of BodyHealthAddon
                if (!BodyHealthAddon.class.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                    continue; // Skip interfaces and abstract classes
                }

                Class<? extends BodyHealthAddon> addonClass = clazz.asSubclass(BodyHealthAddon.class);
                try {
                    BodyHealthAddon addon = addonClass.getConstructor().newInstance();
                    Class<BodyHealthAddon> bodyHealthAddonClass = BodyHealthAddon.class;
                    // Set the debug class and file manager
                    Field debugField = bodyHealthAddonClass.getDeclaredField("debug");
                    Field fileManagerField = bodyHealthAddonClass.getDeclaredField("fileManager");
                    Field infoField = bodyHealthAddonClass.getDeclaredField("addonInfo");
                    Field managerField = bodyHealthAddonClass.getDeclaredField("addonManager");

                    debugField.setAccessible(true);
                    fileManagerField.setAccessible(true);
                    infoField.setAccessible(true);
                    managerField.setAccessible(true);

                    debugField.set(addon, new AddonDebug(addonClass));
                    fileManagerField.set(addon, new AddonFileManager(addon, file));
                    infoField.set(addon, addonClass.getAnnotation(AddonInfo.class));
                    managerField.set(addon, this);


                    if (addon.getAddonInfo() == null) {
                        Debug.logErr("Addon " + addonClass.getSimpleName() + " could not be loaded due to missing the AddonInfo annotation!");
                        continue;
                    }

                    addon.getAddonDebug().log("Loading addon " + addon.getAddonInfo().name() + " (v" + addon.getAddonInfo().version() + ") by " + addon.getAddonInfo().author());

                    addons.add(addon);
                    addon.onAddonPreEnable();
                } catch (Exception e) {
                    Debug.logErr("Failed to load addon class " + clazz + ": " + e.getMessage());
                    Debug.logErr(e);
                }
            }
        } catch (Throwable t) {
            Debug.logErr("Failed to load addon classes from jar " + file.getName() + ": " + t.getMessage());
            Debug.logErr(t);
        }
    }

    /**
     * Warning, intrepid coder! Beyond this point is uncharted territory, rife with unpredictable challenges.
     * Save yourself the trouble and turn back while you can - what's ahead is not for the faint of heart !!!
     */

    private static <T> @NotNull List<Class<? extends T>> findClasses(@NotNull final File file, @NotNull final Class<T> clazz) throws CompletionException {
        if (!file.exists()) {
            return Collections.emptyList();
        }

        final List<Class<? extends T>> classes = new ArrayList<>();

        final List<String> matches = matchingNames(file);

        for (final String match : matches) {
            try {
                final URL jar = file.toURI().toURL();
                try (final URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader())) {
                    Class<? extends T> addonClass = loadClass(loader, match, clazz);
                    if (addonClass != null) {
                        classes.add(addonClass);
                    }
                }
            } catch (final VerifyError ignored) {
            } catch (IOException | ClassNotFoundException e) {
                throw new CompletionException(e.getCause());
            }
        }
        return classes;
    }

    private List<Class<?>> loadAllClassesFromJar(File jarFile) {
        List<Class<?>> classes = new ArrayList<>();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, getClass().getClassLoader())) {

            try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
                JarEntry jarEntry;
                String mainDir = "";
                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    if (jarEntry.getName().endsWith(".class")) {
                        String className = jarEntry.getName().replaceAll("/", ".").replace(".class", "");
                        try {
                            Class<?> clazz;
                            try {
                                clazz = Class.forName(className, false, classLoader);
                            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                                continue;
                            }
                            if (BodyHealthAddon.class.isAssignableFrom(clazz)) {
                                classLoader.loadClass(className);
                                mainDir = className.substring(0, className.lastIndexOf('.'));
                            }
                            if (!clazz.getName().contains(mainDir)) {
                                continue;
                            }
                            classes.add(clazz);

                        } catch (ClassNotFoundException e) {
                            Debug.logErr("Failed to load class " + className + ": " + e.getMessage());
                        }
                    }
                }
                for (Class<?> clazz : classes) {
                    if (!BodyHealthAddon.class.isAssignableFrom(clazz)) {
                        try {
                            classLoader.loadClass(clazz.getName());
                        } catch (ClassNotFoundException e) {
                            Debug.logErr("Failed to load class " + clazz.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            Debug.logErr("Error loading classes from JAR: " + e.getMessage());
        }
        return classes;
    }

    private static @NotNull List<String> matchingNames(final File file) {
        final List<String> matches = new ArrayList<>();
        try {
            final URL jar = file.toURI().toURL();
            try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    final String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }

                    matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return matches;
    }

    private static <T> @Nullable Class<? extends T> loadClass(final @NotNull URLClassLoader loader, final String match, @NotNull final Class<T> clazz) throws ClassNotFoundException {
        try {
            final Class<?> loaded = loader.loadClass(match);
            if (clazz.isAssignableFrom(loaded)) {
                return (loaded.asSubclass(clazz));
            }
        } catch (final NoClassDefFoundError ignored) {
        }
        return null;
    }

}
