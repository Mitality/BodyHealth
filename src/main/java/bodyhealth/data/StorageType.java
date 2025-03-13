package bodyhealth.data;

public enum StorageType {

    YAML, MySQL, SQLite;

    public static StorageType fromString(String type) {
        if (type == null) return SQLite;
        return switch (type.trim().toLowerCase()) {
            case "mysql" -> MySQL;
            case "yaml" -> YAML;
            default -> SQLite;
        };
    }
}
