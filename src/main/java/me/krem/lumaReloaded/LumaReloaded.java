package me.krem.lumaReloaded;

import org.bukkit.plugin.java.JavaPlugin;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
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
        if /*(!loadResources())*/ (false) {
            Bukkit.getPluginManager().disablePlugin(this);
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

    private static boolean loadResources() {
        try {
            BufferedImage fontMapImg = ImageIO.read(LumaReloaded.class.getResourceAsStream("/fontmap.png"));
            fontMap = new boolean[fontMapImg.getWidth() * fontMapImg.getHeight()];

            for(int j = 0; j < fontMapImg.getHeight(); ++j) {
                for(int i = 0; i < fontMapImg.getWidth(); ++i) {
                    if (fontMapImg.getRGB(i, j) == -16777216) {
                        fontMap[fontMapImg.getWidth() * j + i] = true;
                    } else {
                        fontMap[fontMapImg.getWidth() * j + i] = false;
                    }
                }
            }

            byte[] temp = new byte[16384];
            DataInputStream dis = new DataInputStream(LumaReloaded.class.getResourceAsStream("/error"));
            dis.readFully(temp);
            dis.close();
            brokenFileIcon = new LumaCanvas("Error", 1, 1, 1, 20, new ArrayList());
            brokenFileIcon.setData(1, temp);
            temp = new byte[65536];
            dis = new DataInputStream(LumaReloaded.class.getResourceAsStream("/loading"));
            dis.readFully(temp);
            loadingIcon = new LumaCanvas("Loading", 1, 1, 4, 10, new ArrayList());
            loadingIcon.setData(4, temp);
            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    static {
        logo = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Luma" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    }
}
