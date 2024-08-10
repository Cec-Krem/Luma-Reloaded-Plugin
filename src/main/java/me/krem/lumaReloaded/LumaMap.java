package me.krem.lumaReloaded;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Material;
// import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack; <- NEED UPDATE
// import org.bukkit.craftbukkit.v1_20_R3.map.CraftMapView; <- NEED UPDATE
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R1.map.CraftMapView;
import org.bukkit.map.MapView;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;

public class LumaMap extends MapRenderer {
    private final LumaCanvas lumaCanvas;
    private final int x;
    private final int y;
    private final int mapId;
    private int frameIndex = -1;
    private ClickAction action;
    private final MapView view;
    private final ItemStack mapItem;
    private static Field worldMapField;
    private static boolean nmsEnabled = false;

    public LumaMap(LumaCanvas luCa, int x, int y, MapView view) {
        this.lumaCanvas = luCa;
        this.x = x;
        this.y = y;
        this.view = view;
        this.mapId = view.getId();
        org.bukkit.inventory.ItemStack map = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        ItemMeta meta = (ItemMeta)Objects.requireNonNull(map.getItemMeta());
        MapMeta mapMeta = (MapMeta)meta;
        mapMeta.setMapView(view);
        map.setItemMeta(meta);
        this.mapItem = CraftItemStack.asNMSCopy(map);
    }

    public void render(MapView view, MapCanvas mapCanvas, Player player) {
        if (this.lumaCanvas.getFrameIndex() != this.frameIndex) {
            long nanos = System.nanoTime();
            this.frameIndex = this.lumaCanvas.getFrameIndex();
            this.lumaCanvas.drawTile(this.x, this.y, mapCanvas);
            LoadStatistics.updateCanvasDrawNanos(System.nanoTime() - nanos);
            LoadStatistics.countFrame();
        }

    }

    public void forceRedraw() {
        this.frameIndex = -1;
    }

    public void setAction(ClickAction action) {
        this.action = action;
    }

    public void clickAction(Player player, ItemFrame itemFrame) {
        if (this.action != null) {
            this.action.run(player, itemFrame);
        }

    }

    public int getMapId() {
        return this.mapId;
    }

    public boolean hasClickAction() {
        return this.action != null;
    }

    public ClickAction getClickAction() {
        return this.action;
    }

    public void forceDirty() {
        if (nmsEnabled) {
            try {
                WorldMap worldMap = (WorldMap)worldMapField.get(this.view);
                // Iterator var2 = worldMap.o.entrySet().iterator();
                Iterator var2 = worldMap.o.listIterator();

                while(var2.hasNext()) {
                    Map.Entry<EntityHuman, WorldMap.WorldMapHumanTracker> ent = (Map.Entry)var2.next();
                    WorldMap.WorldMapHumanTracker tracker = (WorldMap.WorldMapHumanTracker)ent.getValue();
                    EntityHuman human = (EntityHuman)ent.getKey();
                    MapId MId = worldMap.id;
                    Packet<?> packet = worldMap.a(MId, human);
                    if (packet != null && human instanceof EntityPlayer) {
                        EntityPlayer ep = (EntityPlayer)human;
                        ep.c.a(packet);
                    }
                }
            } catch (Exception var8) {
            }

        }
    }

    static {
        try {
            worldMapField = CraftMapView.class.getDeclaredField("worldMap");
            worldMapField.setAccessible(true);
            nmsEnabled = true;
        } catch (Exception var1) {
        }

    }
}
