package bodyhealth.migrations;

import bodyhealth.Main;
import bodyhealth.migrations.migration.BodyToTorsoMigration;

import java.util.ArrayList;
import java.util.List;

public class Migrator {

    private final Main main;

    private static final List<Migration> migrations = new ArrayList<>();

    public static List<Migration> getMigrations() {
        return migrations;
    }

    public static void registerMigration(Migration migration) {
        migrations.add(migration);
    }

    public static void unregisterMigration(Migration migration) {
        migrations.remove(migration);
    }

    public Migrator(Main main) {
        this.main = main;
        registerMigration(new BodyToTorsoMigration());
    }

    public void onLoad() {
        for (Migration migration : migrations) {
            migration.onLoad(main);
        }
    }

    public void onEnable() {
        for (Migration migration : migrations) {
            migration.onEnable(main);
        }
    }

    public void onReload() {
        for (Migration migration : migrations) {
            migration.onReload(main);
        }
    }

    public void onDisable() {
        for (Migration migration : migrations) {
            migration.onDisable(main);
        }
    }

}
