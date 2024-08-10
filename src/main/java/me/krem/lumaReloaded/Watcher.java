package me.krem.lumaReloaded;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Watcher implements Listener {
    private static final Watcher instance = new Watcher();

    public static Watcher instance() {
        return instance;
    }

    private Watcher() {
    }

    @EventHandler
    public void onItemFrameClick(PlayerInteractEntityEvent evt) {
        if (evt.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            ItemFrame itemFrame = (ItemFrame)evt.getRightClicked();
            ItemStack stack = itemFrame.getItem();
            if (stack != null && stack.getType() == Material.FILLED_MAP && CanvasManager.hasMap(stack)) {
                evt.setCancelled(true);
                LumaMap lumaMap = CanvasManager.getMapByItem(stack);
                if (lumaMap.hasClickAction()) {
                    lumaMap.clickAction(evt.getPlayer(), itemFrame);
                }
            }
        }

    }

    @EventHandler
    public void onItemFrameClick(PlayerInteractEvent evt) {
        if (evt.getAction() == Action.RIGHT_CLICK_AIR || evt.getAction() == Action.LEFT_CLICK_AIR && evt.getItem() == null) {
            ItemFrame itemFrame = LongRangeAimUtil.getMapInView(evt.getPlayer());
            if (itemFrame != null) {
                LumaMap lumaMap = CanvasManager.getMapInItemFrame(itemFrame);
                if (lumaMap != null) {
                    lumaMap.clickAction(evt.getPlayer(), itemFrame);
                }
            }
        }

    }
}
