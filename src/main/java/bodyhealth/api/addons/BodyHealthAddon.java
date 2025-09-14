package bodyhealth.api.addons;

import bodyhealth.Main;
import bodyhealth.commands.CommandManager;
import bodyhealth.commands.SubCommand;
import bodyhealth.effects.BodyHealthEffect;
import bodyhealth.effects.EffectHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BodyHealthAddon {

    private final List<Listener> listeners = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();
    private AddonDebug debug = null;
    private AddonFileManager fileManager = null;
    private AddonInfo addonInfo = null;
    private AddonManager addonManager = null;

    public void onAddonLoad() {
    }

    public void onAddonEnable() {
    }

    public void onAddonReload() {
    }

    public void onAddonDisable() {
    }

    @NotNull
    public Main getBodyHealthPlugin() {
        return Main.getInstance();
    }

    @NotNull
    public AddonFileManager getAddonFileManager() {
        return fileManager;
    }

    @NotNull
    public AddonDebug getAddonDebug() {
        return debug;
    }

    @NotNull
    public AddonInfo getAddonInfo() {
        return addonInfo;
    }

    @NotNull
    public AddonManager getAddonManager() {
        return addonManager;
    }

    public void registerListener(Listener listener) {
        getBodyHealthPlugin().getServer().getPluginManager().registerEvents(listener, getBodyHealthPlugin());
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
        listeners.remove(listener);
    }

    public void unregisterListeners() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
        listeners.clear();
    }

    public void registerCommand(String name, SubCommand command) {
        CommandManager.addSubCommand(name, command);
        commands.add(name);
    }

    public void unregisterCommand(String name) {
        CommandManager.removeSubCommand(name);
        commands.remove(name);
    }

    public void unregisterCommands() {
        for (String command : commands) {
            CommandManager.removeSubCommand(command);
        }
        commands.clear();
    }

    public boolean registerEffect(BodyHealthEffect effect) {
        return EffectHandler.registerEffect(effect);
    }

    public boolean unregisterEffect(BodyHealthEffect effect) {
        return EffectHandler.unregisterEffect(effect);
    }

}
