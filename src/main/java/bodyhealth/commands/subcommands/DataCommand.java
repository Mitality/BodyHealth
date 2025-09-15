package bodyhealth.commands.subcommands;

import bodyhealth.commands.SubCommand;
import bodyhealth.config.Config;
import bodyhealth.config.Lang;
import bodyhealth.core.BodyHealth;
import bodyhealth.data.DataManager;
import bodyhealth.data.Storage;
import bodyhealth.data.StorageType;
import bodyhealth.data.storage.YAMLStorage;
import bodyhealth.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataCommand implements SubCommand {

    private static final Map<String, PendingAction> pending = new ConcurrentHashMap<>();
    private static class PendingAction {

        final String operation;
        final StorageType type1;
        final StorageType type2;
        final Instant createdAt = Instant.now();

        PendingAction(String operation, StorageType type1, StorageType type2) {
            this.operation = operation;
            this.type1 = type1;
            this.type2 = type2;
        }

        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plusSeconds(Config.confirmation_expiration));
        }

        boolean matches(String op, StorageType t1, StorageType t2) {
            return operation.equalsIgnoreCase(op) && type1 == t1 && type2 == t2;
        }
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            int index = 1;
            if (args.length <= index) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage);
                return true;
            }

            String operation = args[index++];
            StorageType storageType1 = null, storageType2 = null;

            // Parse subcommand-specific arguments
            switch (operation.toLowerCase()) {

                case "save":
                    return handleSave(sender);

                case "dump": {
                    if (args.length <= index) {
                        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage_dump);
                        return true;
                    }
                    storageType1 = StorageType.fromString(args[index]);
                    return handleDump(sender, storageType1);
                }

                case "erase": {
                    if (args.length <= index) {
                        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage_erase);
                        return true;
                    }
                    storageType1 = StorageType.fromString(args[index]);
                    return handleEraseWithConfirmation(sender, storageType1);
                }

                case "move": {
                    if (args.length <= index + 0) {
                        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage_move);
                        return true;
                    }
                    storageType1 = StorageType.fromString(args[index++]);
                    storageType2 = StorageType.fromString(args[index]);
                    return handleMoveWithConfirmation(sender, storageType1, storageType2);
                }

                default:
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage);
                    return true;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_usage);
            return true;
        }
    }

    private boolean handleSave(CommandSender sender) {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                DataManager.saveBodyHealth(player.getUniqueId());
                count++;
            } catch (Exception ignored) {
            }
        }
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_success_save
                .replace("{Count}", String.valueOf(count)));
        return true;
    }

    private boolean handleDump(CommandSender sender, StorageType storageType) {
        try {
            Map<UUID, BodyHealth> data = DataManager.getStorage(storageType).loadAllBodyHealth();
            String file = YAMLStorage.dump(data, storageType);
            if (file.isEmpty()) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_fail_dump
                        .replace("{Type}", String.valueOf(storageType)));
            } else {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_success_dump
                        .replace("{Type}", String.valueOf(storageType))
                        .replace("{File}", file)
                        .replace("{Count}", String.valueOf(data.size())));
            }
            return true;
        } catch (Exception e) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_fail_dump
                    .replace("{Type}", String.valueOf(storageType)));
            return true;
        }
    }

    private boolean handleEraseWithConfirmation(CommandSender sender, StorageType type) {
        String key = sender.getName();
        PendingAction existing = pending.get(key);

        if (existing != null && !existing.isExpired() && existing.matches("erase", type, null)) {
            pending.remove(key);
            try {
                if (DataManager.getStorage(type).erase()) {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_success_erase
                            .replace("{Type}", String.valueOf(type)));
                } else {
                    MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_fail_erase
                            .replace("{Type}", String.valueOf(type)));
                }
            } catch (Exception e) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_fail_erase
                        .replace("{Type}", String.valueOf(type)));
            }
            return true;
        }

        pending.put(key, new PendingAction("erase", type, null));
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_confirmation_erase
                .replace("{Type}", String.valueOf(type)));
        return true;
    }

    private boolean handleMoveWithConfirmation(CommandSender sender, StorageType type1, StorageType type2) {
        String key = sender.getName();
        PendingAction existing = pending.get(key);

        if (existing != null && !existing.isExpired() && existing.matches("move", type1, type2)) {
            pending.remove(key);
            try {
                Map<UUID, BodyHealth> data = DataManager.getStorage(type1).loadAllBodyHealth();
                Storage targetStorage = DataManager.getStorage(type2);

                int moved = 0;
                for (Map.Entry<UUID, BodyHealth> entry : data.entrySet()) {
                    try {
                        targetStorage.saveBodyHealth(entry.getKey(), entry.getValue());
                        moved++;
                    } catch (Exception ignored) {
                    }
                }

                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_success_move
                        .replace("{Type1}", String.valueOf(type1))
                        .replace("{Type2}", String.valueOf(type2))
                        .replace("{Count}", String.valueOf(moved)));
            } catch (Exception e) {
                MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_fail_move
                        .replace("{Type1}", String.valueOf(type1))
                        .replace("{Type2}", String.valueOf(type2)));
            }
            return true;
        }

        pending.put(key, new PendingAction("move", type1, type2));
        MessageUtils.notifySender(sender, Config.prefix + Lang.bodyhealth_data_confirmation_move
                .replace("{Type1}", String.valueOf(type1))
                .replace("{Type2}", String.valueOf(type2)));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            for (String opt : Arrays.asList("save", "dump", "erase", "move")) {
                if (opt.startsWith(partial)) out.add(opt);
            }
            return out;
        }
        if (args.length == 3) {
            String op = args[1].toLowerCase();
            if (!op.equals("save")) {
                String partial = args[2].toLowerCase();
                for (StorageType t : StorageType.values()) {
                    String s = t.toString();
                    if (s.toLowerCase().startsWith(partial)) out.add(s);
                }
            }
            return out;
        }
        if (args.length == 4) {
            String op = args[1].toLowerCase();
            if (op.equals("move")) {
                String partial = args[3].toLowerCase();
                for (StorageType t : StorageType.values()) {
                    String s = t.toString();
                    if (s.toLowerCase().startsWith(partial)) out.add(s);
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "bodyhealth.data";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
