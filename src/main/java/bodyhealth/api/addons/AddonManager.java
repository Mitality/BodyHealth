package bodyhealth.api.addons;

import bodyhealth.Main;
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
            unloadAddon(addon);
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
            Debug.logErr("Error while disabling addon " + addon.getClass().getName() + ": " + t.getMessage());
        }
        addons.remove(addon);
    }

    /**
     * Reloads all BodyHealthAddons
     */
    public void reloadAddons() {
        for (BodyHealthAddon addon : addons) {
            reloadAddon(addon);
        }
        if (!addons.isEmpty()) Debug.log("Reloaded " + addons.size() + " addon(s)");
    }

    /**
     * Reloads a specific BodyHealthAddon
     * @param addon The addon to reload
     */
    public void reloadAddon(BodyHealthAddon addon) {
        try {
            addon.onAddonReload();
        } catch (Throwable t) {
            Debug.logErr("Error while reloading addon " + addon.getClass().getName() + ": " + t.getMessage());
        }
    }

    /**
     * Retrieves a list of all currently loaded BodyHealthAddons
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

        if (!addons.isEmpty()) Debug.log("Loaded " + addons.size() + " addon(s)");
    }

    /**
     * Loads a specific BodyHealthAddon
     * @param file The addon to load
     */
    public void loadAddon(File file) {
        try {
            List<Class<?>> classes = loadAllLoadableClassesFromJar(file);

            for (Class<?> clazz : classes) {

                // Check if the class is a concrete subclass of BodyHealthAddon
                if (!BodyHealthAddon.class.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                    continue; // Skip interfaces and abstract classes
                }

                Class<? extends BodyHealthAddon> addonClass = clazz.asSubclass(BodyHealthAddon.class);
                try {
                    BodyHealthAddon addon = addonClass.getConstructor().newInstance();
                    Class<BodyHealthAddon> bodyHealthAddonClass = BodyHealthAddon.class;

                    Field debugField = bodyHealthAddonClass.getDeclaredField("debug");
                    Field fileManagerField = bodyHealthAddonClass.getDeclaredField("fileManager");
                    Field infoField = bodyHealthAddonClass.getDeclaredField("addonInfo");
                    Field managerField = bodyHealthAddonClass.getDeclaredField("addonManager");

                    debugField.setAccessible(true);
                    fileManagerField.setAccessible(true);
                    infoField.setAccessible(true);
                    managerField.setAccessible(true);

                    if (addonClass.getAnnotation(AddonInfo.class) == null) {
                        Debug.logErr("Addon " + addonClass.getSimpleName() + " could not be loaded due to it missing the AddonInfo annotation!");
                        continue;
                    }

                    debugField.set(addon, new AddonDebug(addonClass));
                    fileManagerField.set(addon, new AddonFileManager(addon, file));
                    infoField.set(addon, addonClass.getAnnotation(AddonInfo.class));
                    managerField.set(addon, this);

                    Debug.log("Loading addon " + addon.getAddonInfo().name() + " (v" + addon.getAddonInfo().version() + ") by " + addon.getAddonInfo().author());

                    addons.add(addon);
                    addon.onAddonLoad();
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
     * Enables all BodyHealthAddons
     */
    public void enableAddons() {
        for (BodyHealthAddon addon : addons) {
            enableAddon(addon);
        }
        if (!addons.isEmpty()) Debug.log("Enabled " + addons.size() + " addon(s)");
    }

    /**
     * Enables a specific BodyHealthAddon
     * @param addon The addon to enable
     */
    public void enableAddon(BodyHealthAddon addon) {
        try {
            addon.onAddonEnable();
        } catch (Throwable t) {
            Debug.logErr("Error while enabling addon " + addon.getClass().getName() + ": " + t.getMessage());
        }
    }

    /**
     * Finds and loads all classes from a given JAR file that are assignable to a specified base class
     * @param file the JAR file to scan for classes
     * @param clazz the superclass or interface to match against
     * @param <T> the base type to which all returned classes must be assignable
     * @return A list of classes from the JAR that extend or implement {@code clazz}, or an empty list if none found
     * @throws CompletionException If an I/O or class loading error occurs during processing
     */
    private static <T> @NotNull List<Class<? extends T>> findClasses(@NotNull File file, @NotNull Class<T> clazz) throws CompletionException {
        if (!file.exists()) return Collections.emptyList();

        List<Class<? extends T>> classes = new ArrayList<>();
        List<String> matches = matchingNames(file);

        for (String match : matches) {
            try {
                URL jarUrl = file.toURI().toURL();
                try (URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, clazz.getClassLoader())) {
                    Class<? extends T> addonClass = loadClass(loader, match, clazz);
                    if (addonClass != null) classes.add(addonClass);
                }
            } catch (VerifyError ignored) {
                // silently ignore invalid class bytecode verification
            } catch (IOException | ClassNotFoundException e) {
                Debug.logErr(e);
            }
        }

        return classes;
    }

    /**
     * Loads all classes from the given JAR file
     * @param jarFile the JAR file to scan and load classes from
     * @return A list of successfully loaded classes from the JAR
     */
    private List<Class<?>> loadAllLoadableClassesFromJar(File jarFile) {
        List<Class<?>> classes = new ArrayList<>();

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                getClass().getClassLoader());
             JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {

            String basePackage = "";

            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.getName().endsWith(".class")) continue;

                String className = entry.getName()
                        .replace('/', '.')
                        .replaceAll("\\.class$", "");

                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);

                    if (BodyHealthAddon.class.isAssignableFrom(clazz)) {
                        basePackage = className.substring(0, className.lastIndexOf('.'));
                    }

                    if (!basePackage.isEmpty() && !clazz.getName().startsWith(basePackage)) continue;

                    classes.add(clazz);

                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Debug.logErr("Failed to load class " + className + ": " + e.getMessage());
                    // Class not loadable (e.g. META-INF, module-info)
                }
            }

            // Ensure all collected classes are actually loaded
            for (Class<?> clazz : classes) {
                try {
                    classLoader.loadClass(clazz.getName());
                } catch (ClassNotFoundException e) {
                    Debug.logErr("Deferred load failed for " + clazz.getName() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Debug.logErr("Error loading classes from JAR: " + e.getMessage());
        }

        return classes;
    }

    /**
     * Scans the given JAR file and returns a list of all fully qualified class names found within it
     * @param file the JAR file to scan
     * @return A list of class names (e.g., "com.example.MyClass") or an empty list if an error occurs
     */
    private static @NotNull List<String> matchingNames(@NotNull File file) {
        List<String> matches = new ArrayList<>();

        try (JarInputStream stream = new JarInputStream(file.toURI().toURL().openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                if (!entry.getName().endsWith(".class")) continue;

                String className = entry.getName()
                        .substring(0, entry.getName().lastIndexOf('.'))
                        .replace('/', '.');

                matches.add(className);
            }
        } catch (IOException e) {
            Debug.logErr("Error loading classes from JAR: " + e.getMessage());
            return Collections.emptyList();
        }

        return matches;
    }

    /**
     * Attempts to load a class by name from a given class loader and cast it to the specified type
     * @param loader the class loader to use
     * @param className the fully qualified class name
     * @param clazz the base class or interface to check compatibility with
     * @param <T> the expected type
     * @return The class if successfully loaded and assignable to {@code clazz}, or {@code null} otherwise
     * @throws ClassNotFoundException If the class cannot be found
     */
    private static <T> @Nullable Class<? extends T> loadClass(@NotNull URLClassLoader loader, @NotNull String className, @NotNull Class<T> clazz) throws ClassNotFoundException {
        try {
            Class<?> loaded = loader.loadClass(className);
            return clazz.isAssignableFrom(loaded) ? loaded.asSubclass(clazz) : null;
        } catch (NoClassDefFoundError ignored) {
            return null;
        }
    }

}
