package me.krem.lumaReloaded;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WarpAction extends ClickAction {
    private static Essentials es;
    private final String warpName;

    public WarpAction(String[] message) {
        this.warpName = message[0];
        if (es == null) {
            Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin != null && essentialsPlugin instanceof Essentials) {
                es = (Essentials)essentialsPlugin;
                System.out.println("Essentials hooked!");
            }
        }

    }

    public String getTypeString() {
        return "Warp";
    }

    public String getData() {
        return this.warpName;
    }

    public void run(Player player, ItemFrame itemFrame) {
        if (es != null) {
            try {
                Location warpLocation = es.getWarps().getWarp(this.warpName);
                player.teleport(warpLocation);
            } catch (WarpNotFoundException var4) {
            } catch (InvalidWorldException var5) {
                Logger.getLogger(WarpAction.class.getName()).log(Level.SEVERE, (String)null, var5);
            }

        }
    }
}
