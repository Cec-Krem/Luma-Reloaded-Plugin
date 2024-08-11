package me.krem.lumaReloaded;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public final class LumaReloaded extends JavaPlugin {
    public static final int LUMA_MAGIC_CONSTANT = 1280658753;
    public static final String logo;
    public static LumaReloaded instance;
    public static LumaCanvas brokenFileIcon;
    public static LumaCanvas loadingIcon;
    public static boolean[] fontMap;
    public static ThreadAsyncLazyFileLoader lazyFileLoader;

    public LumaReloaded() {
    }

    @Override
    public void onEnable() {
        instance = this;
        lazyFileLoader = new ThreadAsyncLazyFileLoader();
        this.getDataFolder().mkdir();
        (new File(this.getDataFolder(), "images")).mkdir();
        if (false) {
            Bukkit.getPluginManager().disablePlugin(this); // I don't know why when I delete this condition the plugin doesn't work ?
        } else {
            Settings.loadConfigYml();
            CanvasManager.loadDataYml();
            int taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, Synchronizer.instance(), 1L, 1L);
            Synchronizer.setTaskId(taskid);
            lazyFileLoader.start();
            Bukkit.getPluginManager().registerEvents(Watcher.instance(), this);
            CanvasManager.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, CanvasManager::advanceFrames, 1L, 1L);
            LoadStatistics.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, LoadStatistics::tick, 1L, 1L);
        }
    }

    @Override
    public void onDisable() {
        lazyFileLoader.shutdown();
        lazyFileLoader = null;
        HandlerList.unregisterAll(Watcher.instance());
        Bukkit.getScheduler().cancelTask(Synchronizer.getTaskId());
        Bukkit.getScheduler().cancelTask(CanvasManager.taskId);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandProcessor.onCommand(sender, command, label, args);
    }

    static {
        logo = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Luma" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    }
}
