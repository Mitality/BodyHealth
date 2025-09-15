package bodyhealth.data;

import org.jetbrains.annotations.NotNull;

public enum StorageType {

    YAML, MySQL, SQLite;

    public static @NotNull StorageType fromString(String type) {
        if (type == null) return SQLite;
        return switch (type.trim().toLowerCase()) {
            case "mysql" -> MySQL;
            case "yaml" -> YAML;
            default -> SQLite;
        };
    }
}
