package me.krem.lumaReloaded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CanvasManager {
    private static final HashMap<String, LumaCanvas> CANVASES_BY_NAME = new HashMap();
    private static final HashMap<Integer, LumaCanvas> CANVASES_BY_MAP_ID = new HashMap();
    private static final HashMap<Integer, LumaMap> MAPS_BY_MAP_ID = new HashMap();
    private static final ArrayList<LumaCanvas> CANVASES = new ArrayList();
    private static int tickId = 0;
    public static int taskId;

    public CanvasManager() {
    }

    public static int createCanvasForData(String name, byte[] data, int width, int height, int frames) {
        ArrayList<MapView> views = new ArrayList();

        int baseId;
        for(baseId = 0; baseId < width * height; ++baseId) {
            MapView view = Bukkit.createMap((World)Bukkit.getWorlds().get(0));
            views.add(view);
        }

        baseId = ((MapView)views.get(0)).getId();
        ArrayList<LumaMap> newMaps = new ArrayList();
        LumaCanvas canvas = new LumaCanvas(name, baseId, width, height, 20, newMaps);
        canvas.setData(frames, data);

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                MapView mapView = (MapView)views.get(width * y + x);
                LumaMap lumaMap = new LumaMap(canvas, x, y, mapView);
                newMaps.add(lumaMap);
                CANVASES_BY_MAP_ID.put(mapView.getId(), canvas);
                MAPS_BY_MAP_ID.put(mapView.getId(), lumaMap);
                stripRenderers(mapView);
                mapView.addRenderer(lumaMap);
            }
        }

        CANVASES.add(canvas);
        CANVASES_BY_NAME.put(name, canvas);
        saveDataYml();
        return baseId;
    }

    public static LinkedList<String> getCanvasIds() {
        LinkedList<String> nameList = new LinkedList();
        Iterator var1 = CANVASES_BY_NAME.keySet().iterator();

        while(var1.hasNext()) {
            String name = (String)var1.next();
            nameList.add(name);
        }

        return nameList;
    }

    private static void preloadCanvas(String name, int baseId, int width, int height, int delay) {
        ArrayList<LumaMap> newMaps = new ArrayList();
        LumaCanvas canvas = new LumaCanvas(name, baseId, width, height, delay, newMaps);
        canvas.setDelay(delay);

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                MapView mapView = Bukkit.getMap((short)(baseId + y * width + x));
                LumaMap lumaMap = new LumaMap(canvas, x, y, mapView);
                newMaps.add(lumaMap);
                CANVASES_BY_MAP_ID.put(mapView.getId(), canvas);
                MAPS_BY_MAP_ID.put(mapView.getId(), lumaMap);
                stripRenderers(mapView);
                mapView.addRenderer(lumaMap);
            }
        }

        CANVASES.add(canvas);
        CANVASES_BY_NAME.put(name, canvas);
    }

    public static boolean hasCanvasByName(String name) {
        return CANVASES_BY_NAME.containsKey(name);
    }

    public static LumaCanvas getCanvasByName(String name) {
        return (LumaCanvas)CANVASES_BY_NAME.get(name);
    }

    private static LumaCanvas getCanvasByMapId(int mapId) {
        return (LumaCanvas)CANVASES_BY_MAP_ID.get(mapId);
    }

    public static LumaCanvas getCanvasByMap(ItemStack is) {
        int mapId = getMapId(is);
        return getCanvasByMapId(mapId);
    }

    public static int getNumberOfLoadedCanvases() {
        int loaded = 0;
        Iterator var1 = CANVASES.iterator();

        while(var1.hasNext()) {
            LumaCanvas canvas = (LumaCanvas)var1.next();
            if (canvas.isLoaded()) {
                ++loaded;
            }
        }

        return loaded;
    }

    public static int getNumberOfCanvases() {
        return CANVASES.size();
    }

    public static int getNumberOfLoadedTiles() {
        int tiles = 0;
        Iterator var1 = CANVASES.iterator();

        while(var1.hasNext()) {
            LumaCanvas canvas = (LumaCanvas)var1.next();
            if (canvas.isLoaded()) {
                tiles += canvas.getWidth() * canvas.getHeight();
            }
        }

        return tiles;
    }

    public static int getNumberOfTiles() {
        int tiles = 0;

        LumaCanvas canvas;
        for(Iterator var1 = CANVASES.iterator(); var1.hasNext(); tiles += canvas.getWidth() * canvas.getHeight()) {
            canvas = (LumaCanvas)var1.next();
        }

        return tiles;
    }

    public static int getNetMemoryLoad() {
        int bytes = 0;
        Iterator var1 = CANVASES.iterator();

        while(var1.hasNext()) {
            LumaCanvas canvas = (LumaCanvas)var1.next();
            if (canvas.isLoaded()) {
                bytes += 16384 * canvas.getWidth() * canvas.getHeight() * canvas.getFrames();
            }
        }

        return bytes;
    }

    private static boolean hasMapId(int mapId) {
        return MAPS_BY_MAP_ID.containsKey(mapId);
    }

    public static boolean hasMap(ItemStack is) {
        int mapId = getMapId(is);
        return hasMapId(mapId);
    }

    private static LumaMap getMapById(int mapId) {
        return (LumaMap)MAPS_BY_MAP_ID.get(mapId);
    }

    public static LumaMap getMapByItem(ItemStack is) {
        int mapId = getMapId(is);
        return getMapById(mapId);
    }

    private static int getMapId(ItemStack is) {
        if (is != null && is.getType() == Material.FILLED_MAP) {
            MapMeta map = (MapMeta)is.getItemMeta();
            if (map == null) {
                return -1;
            } else {
                int mapId = map.getMapId();
                return mapId;
            }
        } else {
            return -1;
        }
    }

    public static LumaMap getMapInItemFrame(ItemFrame itemFrame) {
        ItemStack is = itemFrame.getItem();
        return is != null && is.getType() == Material.FILLED_MAP && hasMap(is) ? getMapByItem(is) : null;
    }

    private static boolean isMapIdRangeAvailable(int baseId, int length) {
        for(int i = baseId; i < baseId + length; ++i) {
            if (hasMapId(i) || Bukkit.getMap((short)i) == null) {
                return false;
            }
        }

        return true;
    }

    public static void advanceFrames() {
        long nanos = System.nanoTime();
        ++tickId;
        Iterator var2 = CANVASES.iterator();

        while(var2.hasNext()) {
            LumaCanvas canvas = (LumaCanvas)var2.next();
            if (canvas.getDelay() != 0 && tickId % canvas.getDelay() == 0) {
                canvas.advanceFrame();
            }
        }

        LoadStatistics.updateFrameAdvanceNanos(System.nanoTime() - nanos);
    }

    public static void loadDataYml() {
        File dataFile = new File(LumaReloaded.instance.getDataFolder(), "data.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        List<Map<?, ?>> canvasList = config.getMapList("canvases");
        List<Map<?, ?>> actionList = config.getMapList("actions");
        boolean dirty = false;
        if (canvasList == null) {
            canvasList = new ArrayList();
            System.out.println("No canvases section in data.yml. Creating");
            config.set("canvases", canvasList);
            dirty = true;
        }

        if (actionList == null) {
            actionList = new ArrayList();
            System.out.println("No actions section in data.yml. Creating");
            config.set("actions", actionList);
            dirty = true;
        }

        if (dirty) {
            try {
                config.save(dataFile);
            } catch (IOException var14) {
                return;
            }
        }

        Iterator var5 = ((List)canvasList).iterator();

        Map map;
        while(var5.hasNext()) {
            map = (Map)var5.next();

            try {
                String name = (String)map.get("name");
                int width = (Integer)map.get("width");
                int height = (Integer)map.get("height");
                int delay = (Integer)map.get("delay");
                int baseId = (Integer)map.get("baseId");
                if (hasCanvasByName(name)) {
                    System.out.println("Duplicate definition of image \"" + name + "\". Skipping");
                } else if (!isMapIdRangeAvailable(baseId, width * height)) {
                    System.out.println("Duplicate claim of map IDs by image \"" + name + "\". Skipping");
                } else if (baseId + width * height > 32767) {
                    System.out.println("Map ID claim of image \"" + name + "\" out of bounds. Skipping");
                } else {
                    System.out.println("Loading image: " + name + "@" + baseId + "ff. " + width + "x" + height + " " + delay);
                    preloadCanvas(name, baseId, width, height, delay);
                }
            } catch (Exception var13) {
                System.err.println("Invalid canvas entry in the data.yml. Skipping");
                var13.printStackTrace();
            }
        }

        var5 = ((List)actionList).iterator();

        while(var5.hasNext()) {
            map = (Map)var5.next();

            try {
                int mapId = (Integer)map.get("mapId");
                String name = (String)map.get("type");
                String data = (String)map.get("data");
                if (!hasMapId(mapId)) {
                    System.out.println("Click Action defined for unregistered mapId " + mapId + ". Skipping");
                } else {
                    LumaMap lumaMap = getMapById(mapId);
                    lumaMap.setAction(ClickAction.generate(name, data));
                }
            } catch (Exception var12) {
                System.err.println("Invalid canvas entry in the data.yml. Skipping");
                var12.printStackTrace();
            }
        }

    }

    public static void saveDataYml() {
        ArrayList<Map<?, ?>> canvasList = new ArrayList();
        ArrayList<Map<?, ?>> actionList = new ArrayList();
        Iterator var2 = CANVASES.iterator();

        while(var2.hasNext()) {
            LumaCanvas canvas = (LumaCanvas)var2.next();
            Map<String, Object> canvasParams = new HashMap();
            canvasParams.put("name", canvas.getName());
            canvasParams.put("width", canvas.getWidth());
            canvasParams.put("height", canvas.getHeight());
            canvasParams.put("delay", canvas.getDelay());
            canvasParams.put("baseId", canvas.getBaseId());
            canvasList.add(canvasParams);
            Iterator var5 = canvas.getMaps().iterator();

            while(var5.hasNext()) {
                LumaMap map = (LumaMap)var5.next();
                if (map.hasClickAction()) {
                    ClickAction ca = map.getClickAction();
                    HashMap<String, Object> clickActionParams = new HashMap();
                    clickActionParams.put("mapId", map.getMapId());
                    clickActionParams.put("type", ca.getTypeString());
                    clickActionParams.put("data", ca.getData());
                    actionList.add(clickActionParams);
                }
            }
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(new File(""));
            config.set("canvases", canvasList);
            config.set("actions", actionList);
            config.save(new File(LumaReloaded.instance.getDataFolder(), "data.yml"));
        } catch (IOException var9) {
        }

    }

    private static void stripRenderers(MapView mapView) {
        Iterator var1 = mapView.getRenderers().iterator();

        while(var1.hasNext()) {
            MapRenderer renderer = (MapRenderer)var1.next();
            mapView.removeRenderer(renderer);
        }

    }
}
