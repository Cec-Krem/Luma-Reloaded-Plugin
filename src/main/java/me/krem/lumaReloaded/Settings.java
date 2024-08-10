package me.krem.lumaReloaded;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {
    public static int MAX_CLICK_RANGE = 25;
    public static boolean ANIMATIONS = true;
    public static int IMAGE_IDLE_UNLOAD_TIME = 1800;
    public static int STATISTICS_AVERAGE_TIME = 100;

    public Settings() {
    }

    public static void loadConfigYml() {
        LumaReloaded.instance.saveDefaultConfig();
        FileConfiguration fc = LumaReloaded.instance.getConfig();
        MAX_CLICK_RANGE = fc.getInt("max-click-range", 25);
        ANIMATIONS = fc.getBoolean("animations", true);
        IMAGE_IDLE_UNLOAD_TIME = 1000 * fc.getInt("image-idle-unload-time", 1800);
        STATISTICS_AVERAGE_TIME = 20 * fc.getInt("statistics-average-time", 100);
    }
}
