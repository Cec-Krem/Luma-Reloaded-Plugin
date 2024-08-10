package me.krem.lumaReloaded;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LongRangeAimUtil {
    public LongRangeAimUtil() {
    }

    public static ItemFrame getMapInView(Player player) {
        List<Block> lastTwoBlocks = player.getLastTwoTargetBlocks((HashSet)null, Settings.MAX_CLICK_RANGE);
        if (lastTwoBlocks.size() < 2) {
            return null;
        } else {
            Block baseBlock = (Block)lastTwoBlocks.get(1);
            Block searchBlock = (Block)lastTwoBlocks.get(0);
            BlockFace bf = searchBlock.getFace(baseBlock);
            Location loc = getBlockCenterLocation(searchBlock.getLocation());
            Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5);
            Iterator var7 = entities.iterator();

            while(var7.hasNext()) {
                Entity ent = (Entity)var7.next();
                if (ent instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame)ent;
                    if (itemFrame.getAttachedFace() == bf) {
                        ItemStack is = itemFrame.getItem();
                        if (is.getType() == Material.FILLED_MAP && CanvasManager.hasMap(is)) {
                            return itemFrame;
                        }
                    }
                }
            }

            return null;
        }
    }

    private static Location getBlockCenterLocation(Location loc) {
        return new Location(loc.getWorld(), (double)loc.getBlockX() + 0.5, (double)loc.getBlockY() + 0.5, (double)loc.getBlockZ() + 0.5);
    }
}
