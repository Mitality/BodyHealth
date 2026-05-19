package bodyhealth.commands.subcommands;

import bodyhealth.Main;
import bodyhealth.api.addons.BodyHealthAddon;
import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.util.MessageUtils;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AddonCommand implements SubCommand {

    private static final Map<String, PendingAddonAction> pending = new ConcurrentHashMap<>();
    public static void resetPendingActions() {
        pending.clear();
    }
    private static class PendingAddonAction {

        final String operation;
        final String identifier;
        final Instant createdAt = Instant.now();

        PendingAddonAction(String operation, String identifier) {
            this.operation = operation;
            this.identifier = identifier;
        }

        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plusSeconds(Config.confirmation_expiration));
        }

        boolean matches(String op, String id) {
            return operation.equalsIgnoreCase(op) && identifier.equalsIgnoreCase(id);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_usage);
            return true;
        }

        String sub = args[1].toLowerCase();
        String nameArg = args.length >= 3 ? joinArgs(args, 2) : null;
        return switch (sub) {
            case "list" -> handleList(sender);
            case "info" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_info_usage);
                    yield true;
                }
                yield handleInfo(sender, nameArg);
            }
            case "enable" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_enable_usage);
                    yield true;
                }
                yield handleEnable(sender, nameArg);
            }
            case "disable" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_disable_usage);
                    yield true;
                }
                yield handleDisable(sender, nameArg);
            }
            case "reload" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_reload_usage);
                    yield true;
                }
                yield handleReload(sender, nameArg);
            }
            case "load" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_usage);
                    yield true;
                }
                yield handleLoad(sender, nameArg);
            }
            case "unload" -> {
                if (nameArg == null) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_unload_usage);
                    yield true;
                }
                yield handleUnload(sender, nameArg);
            }
            default -> {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_usage);
                yield true;
            }
        };
    }

    private boolean handleList(CommandSender sender) {
        List<BodyHealthAddon> addons = Main.getAddonManager().getAddons();

        if (addons.isEmpty()) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_list_none);
            return true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Lang.bodyhealth_addon_list_header.replace("{Count}", String.valueOf(addons.size())));

        for (int i = 0; i < addons.size(); i++) {
            BodyHealthAddon addon = addons.get(i);
            String name = addon.getAddonInfo().name();
            if (addon.isEnabled()) {
                sb.append(Lang.bodyhealth_addon_list_entry_enabled.replace("{Name}", name));
            } else {
                sb.append(Lang.bodyhealth_addon_list_entry_disabled.replace("{Name}", name));
            }
            if (i < addons.size() - 1) {
                sb.append(Lang.bodyhealth_addon_list_separator);
            }
        }

        MessageUtils.notifySender(sender, Config.prefix + sb.toString());
        return true;
    }

    private boolean handleInfo(CommandSender sender, String addonName) {
        BodyHealthAddon addon = findAddon(addonName);
        if (addon == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
            return true;
        }

        String status = addon.isEnabled() ? Lang.bodyhealth_addon_status_enabled : Lang.bodyhealth_addon_status_disabled;
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_info_display
                .replace("{Name}", addon.getAddonInfo().name())
                .replace("{Version}", addon.getAddonInfo().version())
                .replace("{Author}", addon.getAddonInfo().author())
                .replace("{Description}", addon.getAddonInfo().description())
                .replace("{Status}", status));
        return true;
    }

    private boolean handleEnable(CommandSender sender, String addonName) {
        BodyHealthAddon addon = findAddon(addonName);
        if (addon == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
            return true;
        }
        if (addon.isEnabled()) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_enable_already.replace("{Addon}", addonName));
            return true;
        }
        try {
            Main.getAddonManager().enableAddon(addon);
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_enable_success.replace("{Addon}", addonName));
        } catch (Exception e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_enable_fail.replace("{Addon}", addonName));
        }
        return true;
    }

    private boolean handleDisable(CommandSender sender, String addonName) {
        BodyHealthAddon addon = findAddon(addonName);
        if (addon == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
            return true;
        }
        if (!addon.isEnabled()) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_disable_already.replace("{Addon}", addonName));
            return true;
        }
        try {
            Main.getAddonManager().disableAddon(addon);
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_disable_success.replace("{Addon}", addonName));
        } catch (Exception e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_disable_fail.replace("{Addon}", addonName));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender, String addonName) {
        BodyHealthAddon addon = findAddon(addonName);
        if (addon == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
            return true;
        }
        try {
            Main.getAddonManager().reloadAddon(addon);
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_reload_success.replace("{Addon}", addonName));
        } catch (Exception e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_reload_fail.replace("{Addon}", addonName));
        }
        return true;
    }

    private boolean handleLoad(CommandSender sender, String fileName) {
        String key = sender.getName();
        PendingAddonAction existing = pending.get(key);

        if (existing != null && !existing.isExpired() && existing.matches("load", fileName)) {
            pending.remove(key);

            File addonsDir = new File(Main.getInstance().getDataFolder(), "addons");
            String jarName = fileName.endsWith(".jar") ? fileName : fileName + ".jar";
            File file = new File(addonsDir, jarName);

            if (!file.exists()) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_not_found.replace("{File}", fileName));
                return true;
            }

            try {
                List<BodyHealthAddon> before = new ArrayList<>(Main.getAddonManager().getAddons());
                Main.getAddonManager().loadAddon(file);
                List<BodyHealthAddon> newAddons = Main.getAddonManager().getAddons().stream()
                        .filter(a -> !before.contains(a))
                        .collect(Collectors.toList());
                if (newAddons.isEmpty()) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_already.replace("{File}", fileName));
                    return true;
                }
                for (BodyHealthAddon addon : newAddons) {
                    if (!addon.isEnabled()) Main.getAddonManager().enableAddon(addon);
                }
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_success.replace("{File}", fileName));
            } catch (Exception e) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_fail.replace("{File}", fileName));
            }
            return true;
        }

        pending.put(key, new PendingAddonAction("load", fileName));
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_load_warning.replace("{File}", fileName));
        return true;
    }

    private boolean handleUnload(CommandSender sender, String addonName) {
        if (findAddon(addonName) == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
            return true;
        }

        String key = sender.getName();
        PendingAddonAction existing = pending.get(key);

        if (existing != null && !existing.isExpired() && existing.matches("unload", addonName)) {
            pending.remove(key);

            BodyHealthAddon addon = findAddon(addonName);
            if (addon == null) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_not_found.replace("{Addon}", addonName));
                return true;
            }
            try {
                Main.getAddonManager().unloadAddon(addon);
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_unload_success.replace("{Addon}", addonName));
            } catch (Exception e) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_unload_fail.replace("{Addon}", addonName));
            }
            return true;
        }

        pending.put(key, new PendingAddonAction("unload", addonName));
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_addon_unload_warning.replace("{Addon}", addonName));
        return true;
    }

    private BodyHealthAddon findAddon(String name) {
        for (BodyHealthAddon addon : Main.getAddonManager().getAddons()) {
            String addonName = addon.getAddonInfo().name();
            if (addonName.equalsIgnoreCase(name) || addonName.equalsIgnoreCase(name.replace('_', ' '))) {
                return addon;
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return Arrays.asList("list", "info", "enable", "disable", "reload", "load", "unload")
                    .stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            String partial = args[2].toLowerCase();

            if (sub.equals("load")) {
                File addonsDir = new File(Main.getInstance().getDataFolder(), "addons");
                File[] files = addonsDir.listFiles((dir, name) -> name.endsWith(".jar"));
                if (files == null) return Collections.emptyList();
                Set<String> loadedJarNames = new HashSet<>();
                for (BodyHealthAddon addon : Main.getAddonManager().getAddons()) {
                    loadedJarNames.add(addon.getAddonFileManager().getJarFile().getName());
                }
                List<String> result = new ArrayList<>();
                for (File f : files) {
                    if (f.getName().toLowerCase().startsWith(partial) && !loadedJarNames.contains(f.getName())) {
                        result.add(f.getName());
                    }
                }
                return result;
            }

            if (sub.equals("list")) return Collections.emptyList();

            List<BodyHealthAddon> addons = Main.getAddonManager().getAddons();
            List<String> result = new ArrayList<>();
            for (BodyHealthAddon addon : addons) {
                if (sub.equals("enable") && addon.isEnabled()) continue;
                if (sub.equals("disable") && !addon.isEnabled()) continue;
                String name = addon.getAddonInfo().name();
                if (name.toLowerCase().startsWith(partial)) result.add(name);
            }
            return result;
        }
        return Collections.emptyList();
    }

    private String joinArgs(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    @Override
    public String permission() {
        return "bodyhealth.addon";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
