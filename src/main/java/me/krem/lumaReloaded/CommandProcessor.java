package me.krem.lumaReloaded;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class CommandProcessor {
    public CommandProcessor() {
    }

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(LumaReloaded.logo + " Available commands:");
            sender.sendMessage(ChatColor.GOLD + "/lu create [name] [dims] [URL]");
            sender.sendMessage(ChatColor.GOLD + "/lu info ([name])");
            sender.sendMessage(ChatColor.GOLD + "/lu print [name]");
            sender.sendMessage(ChatColor.GOLD + "/lu set-action [id] [type] {data}");
            sender.sendMessage(ChatColor.GOLD + "/lu update [name] [URL]");
            sender.sendMessage(ChatColor.GOLD + "/lu set-speed [name] [speed]");
            sender.sendMessage(ChatColor.GOLD + "/lu stats");
            sender.sendMessage("");
            return true;
        } else {
            LumaCanvas canvas;
            int height;
            Player player;
            int newDelay;
            switch (args[0]) {
                case "create":
                    if (!sender.hasPermission("luma.create")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    if (args.length != 4) {
                        sender.sendMessage(ChatColor.GOLD + "/lu create [name] [dims] [URL]");
                        sender.sendMessage(ChatColor.GRAY + "Generates a new image with the given dimensions from the given web resource.");
                        sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu create creeperface 2x2 http://i.imgur.com/KtVdxxX.jpg");
                        sender.sendMessage("");
                    } else if (!args[1].matches("^[a-zA-Z0-9_]+$")) {
                        sender.sendMessage(ChatColor.GOLD + "Invalid image name! " + ChatColor.GRAY + "Must be alphanumeric");
                    } else if (CanvasManager.hasCanvasByName(args[1])) {
                        sender.sendMessage(ChatColor.GOLD + "Image already exists! " + ChatColor.GRAY + "Use update to change");
                    } else {
                        String[] dims = args[2].split("x");
                        if (dims.length == 2) {
                            try {
                                int width = Integer.parseInt(dims[0]);
                                height = Integer.parseInt(dims[1]);
                                if (width > 0 && height > 0 && width <= 10 && height <= 10) {
                                    sender.sendMessage(ChatColor.GOLD + "Loading...");
                                    (new CanvasURLLoader(sender, args[1], width, height, args[3])).start();
                                } else {
                                    sender.sendMessage(ChatColor.GOLD + "Invalid dimensions! " + ChatColor.GRAY + " Width and height must be 1-10.");
                                }
                                break;
                            } catch (NumberFormatException var23) {
                            }
                        }

                        sender.sendMessage(ChatColor.GOLD + "Invalid dimensions! " + ChatColor.GRAY + "Must be [width]x[height]");
                        sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu create creeperface 2x2 http://i.imgur.com/KtVdxxX.jpg");
                        sender.sendMessage("");
                    }
                    break;
                case "info":
                    if (!sender.hasPermission("luma.info")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    canvas = null;
                    if (args.length == 2) {
                        if (!CanvasManager.hasCanvasByName(args[1])) {
                            sender.sendMessage(ChatColor.GOLD + "Image does not exist! " + ChatColor.GRAY + "Hold a piece of the picture or look at the picture to get information about it");
                            break;
                        }

                        canvas = CanvasManager.getCanvasByName(args[1]);
                    } else if (sender instanceof Player) {
                        player = (Player) sender;
                        ItemStack is = player.getInventory().getItemInMainHand();
                        if (is != null && is.getType() == Material.FILLED_MAP && CanvasManager.hasMap(is)) {
                            canvas = CanvasManager.getCanvasByMap(is);
                        } else {
                            ItemFrame itemFrame = LongRangeAimUtil.getMapInView(player);
                            if (itemFrame != null) {
                                is = itemFrame.getItem();
                                if (is.getType() == Material.FILLED_MAP && CanvasManager.hasMap(is)) {
                                    canvas = CanvasManager.getCanvasByMap(is);
                                }
                            }
                        }
                    }

                    if (canvas != null) {
                        int Cwidth = canvas.getWidth();
                        int Cheight = canvas.getHeight();
                        int Cframes = canvas.getFrames();
                        int Cid = canvas.getBaseId();
                        sender.sendMessage(LumaReloaded.logo + " About this image:");
                        sender.sendMessage(ChatColor.GOLD + "  Name: " + ChatColor.GRAY + canvas.getName());
                        sender.sendMessage(ChatColor.GOLD + "  Loaded: " + ChatColor.GRAY + (canvas.isLoaded() ? ChatColor.GREEN + "Yes " + ChatColor.GRAY + "(" + 16 * Cwidth * Cheight * Cframes + "K)" : "No"));
                        sender.sendMessage(ChatColor.GOLD + "  Size: " + ChatColor.GRAY + Cwidth + "x" + Cheight + " tiles" + (canvas.isLoaded() && Cframes > 1 ? ", " + Cframes + " frames" : ""));
                        sender.sendMessage(ChatColor.GOLD + "  Map IDs: " + ChatColor.GRAY + Cid + "-" + (Cid + Cwidth * Cheight - 1));
                        if (Cframes > 1) {
                            sender.sendMessage(ChatColor.GOLD + "  Refresh Rate: " + ChatColor.GRAY + canvas.getDelay() + " ticks per frame");
                        }

                        sender.sendMessage(ChatColor.GOLD + "  Click Actions: ");
                        boolean hasActions = false;
                        Iterator var32 = canvas.getMaps().iterator();

                        while(var32.hasNext()) {
                            LumaMap map = (LumaMap)var32.next();
                            if (map.hasClickAction()) {
                                hasActions = true;
                                ClickAction ca = map.getClickAction();
                                sender.sendMessage(ChatColor.GOLD + "    - " + map.getMapId() + ": " + ChatColor.GRAY + ca.getTypeString() + " \"" + ca.getData() + "\"");
                            }
                        }

                        if (!hasActions) {
                            sender.sendMessage(ChatColor.GRAY + "    (None)");
                        }

                        sender.sendMessage("");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "/lu info ([name])");
                        sender.sendMessage(ChatColor.GRAY + "Displays information about an existing image.");
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.GRAY + "Hold a part of an image or look at it to see information without knowing its name");
                        }
                    }
                    break;
                case "list":
                    if (!sender.hasPermission("luma.list")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    height = 0;
                    if (args.length >= 2) {
                        try {
                            height = Integer.parseInt(args[1]) - 1;
                        } catch (NumberFormatException var21) {
                        }
                    }

                    List<String> imageNames = CanvasManager.getCanvasIds();
                    if (height < 0 || height * 20 >= imageNames.size()) {
                        height = 0;
                    }

                    Iterator<String> nameIterator = imageNames.iterator();

                    for(int i = 0; i < 20 * height; ++i) {
                        nameIterator.next();
                    }

                    StringBuilder sb = new StringBuilder();
                    boolean comma = false;

                    for(int i = 0; i < 20 && nameIterator.hasNext(); ++i) {
                        if (comma) {
                            sb.append(", ");
                        }

                        String name = (String)nameIterator.next();
                        canvas = CanvasManager.getCanvasByName(name);
                        sb.append(ChatColor.GOLD).append(name).append(ChatColor.GRAY).append(" (").append(canvas.getWidth()).append("x").append(canvas.getHeight()).append(")");
                        comma = true;
                    }

                    sender.sendMessage(LumaReloaded.logo + "Page " + ChatColor.GOLD + (height + 1) + ChatColor.GRAY + " of " + ChatColor.GOLD + (imageNames.size() / 20 + 1) + ChatColor.GRAY + ":");
                    sender.sendMessage(sb.toString());
                    break;
                case "print":
                    if (!sender.hasPermission("luma.print")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.GOLD + "Only works ingame!");
                    } else {
                        player = (Player)sender;
                        if (args.length != 2) {
                            sender.sendMessage(ChatColor.GOLD + "/lu print [name]");
                            sender.sendMessage(ChatColor.GRAY + "Spawns all the maps belonging to an image into your inventory.");
                            sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu print creeperface");
                            sender.sendMessage("");
                        } else if (!CanvasManager.hasCanvasByName(args[1])) {
                            sender.sendMessage(ChatColor.GOLD + "Image does not exist! " + ChatColor.GRAY + "Use " + ChatColor.ITALIC + "create" + ChatColor.GRAY + " to create it.");
                        } else {
                            canvas = CanvasManager.getCanvasByName(args[1]);
                            int requiredSlots = canvas.getWidth() * canvas.getHeight();
                            int freeSlots = 0;
                            ItemStack[] var37 = player.getInventory().getStorageContents();
                            int var38 = var37.length;

                            for(newDelay = 0; newDelay < var38; ++newDelay) {
                                ItemStack stack = var37[newDelay];
                                if (stack == null) {
                                    ++freeSlots;
                                }
                            }

                            if (requiredSlots > freeSlots) {
                                sender.sendMessage(ChatColor.GOLD + "Not enough space! " + ChatColor.GRAY + "You need enough inventory space to hold " + requiredSlots + " items.");
                            } else {
                                for(int i = canvas.getBaseId(); i < canvas.getBaseId() + requiredSlots; ++i) {
                                    MapView mapView = Bukkit.getMap((short)i);
                                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
                                    ItemMeta meta = mapItem.getItemMeta();
                                    MapMeta mapMeta = (MapMeta)meta;
                                    mapMeta.setMapView(mapView);
                                    mapItem.setItemMeta(meta);
                                    player.getInventory().addItem(new ItemStack[]{mapItem});
                                }

                                sender.sendMessage(ChatColor.GOLD + "Maps printed! ");
                            }
                        }
                    }
                    break;
                case "action":
                case "set-action":
                    if (!sender.hasPermission("luma.action")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    if (args.length <= 2) {
                        sender.sendMessage(ChatColor.GOLD + "/lu set-action [type] [data]");
                        sender.sendMessage(ChatColor.GRAY + "Sets a click action for the map held or looked at");
                        sender.sendMessage(ChatColor.GOLD + "Options: " + ChatColor.GRAY + "message {message}, command {command}, warp {warpname}, heal");
                        sender.sendMessage("");
                    } else {
                        player = (Player)sender;
                        LumaMap lumaMap = null;
                        ItemStack is = player.getInventory().getItemInMainHand();
                        if (is != null && is.getType() == Material.FILLED_MAP && CanvasManager.hasMap(is)) {
                            lumaMap = CanvasManager.getMapByItem(is);
                        } else {
                            ItemFrame itemFrame = LongRangeAimUtil.getMapInView(player);
                            if (itemFrame != null) {
                                lumaMap = CanvasManager.getMapInItemFrame(itemFrame);
                            }
                        }

                        if (lumaMap == null) {
                            sender.sendMessage(ChatColor.GOLD + "No map selected! " + ChatColor.GRAY + " Hold the map or look directly at it while setting an action");
                        } else {
                            ClickAction action = ClickAction.generate(args[1], (String[])Arrays.copyOfRange(args, 2, args.length));
                            if (action == null) {
                                sender.sendMessage(ChatColor.GOLD + "Invalid Action Type! " + ChatColor.GRAY + " See /lu set-action for a list of options.");
                                sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu set-action message &lBOOM You are dead");
                                sender.sendMessage("");
                            } else {
                                lumaMap.setAction(action);
                                CanvasManager.saveDataYml();
                                sender.sendMessage(ChatColor.GOLD + "Click action updated!");
                            }
                        }
                    }
                    break;
                case "update":
                case "set-source":
                    if (!sender.hasPermission("luma.update")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    if (args.length != 3) {
                        sender.sendMessage(ChatColor.GOLD + "/lu update [name] [URL]");
                        sender.sendMessage(ChatColor.GRAY + "Assigns a new resource to the given image. Aspect ratio cannot be changed!");
                        sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu update creeperface http://i.imgur.com/KtVdxxX.jpg");
                        sender.sendMessage("");
                    } else if (!CanvasManager.hasCanvasByName(args[1])) {
                        sender.sendMessage(ChatColor.GOLD + "Image does not exist! " + ChatColor.GRAY + "Use " + ChatColor.ITALIC + "create" + ChatColor.GRAY + " to create it.");
                    } else {
                        canvas = CanvasManager.getCanvasByName(args[1]);
                        sender.sendMessage(ChatColor.GOLD + "Loading...");
                        (new CanvasURLLoader(sender, canvas, args[2])).start();
                    }
                    break;
                case "set-speed":
                case "speed":
                case "set-delay":
                case "delay":
                    if (!sender.hasPermission("luma.setspeed")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    if (args.length != 3) {
                        sender.sendMessage(ChatColor.GOLD + "/lu set-speed [name] [speed]");
                        sender.sendMessage(ChatColor.GRAY + "Sets an animation's refresh rate in ticks per frame.");
                        sender.sendMessage(ChatColor.GRAY + "One second = 20 ticks. Maps redraw every 10 ticks on a wall and every 4 ticks in a player's inventory.");
                        sender.sendMessage(ChatColor.GRAY + "Changing the refresh rate on a static image has no apparent effect, but will be applied when update is used to load an animation.");
                        sender.sendMessage(ChatColor.GOLD + "Example: " + ChatColor.GRAY + "/lu set-speed creeperface 20");
                        sender.sendMessage("");
                    } else if (!CanvasManager.hasCanvasByName(args[1])) {
                        sender.sendMessage(ChatColor.GOLD + "Image does not exist! " + ChatColor.GRAY + "Use " + ChatColor.ITALIC + "create" + ChatColor.GRAY + " to create it.");
                    } else {
                        canvas = CanvasManager.getCanvasByName(args[1]);

                        try {
                            newDelay = Integer.parseInt(args[2]);
                            if (newDelay > 0) {
                                canvas.setDelay(Integer.parseInt(args[2]));
                                if (canvas.getFrames() <= 1) {
                                    sender.sendMessage(ChatColor.GRAY + "Changing the refresh rate on a static image has no apparent effect, but will be applied when update is used to load an animation.");
                                }

                                if (newDelay < 10) {
                                    sender.sendMessage(ChatColor.GOLD + "Fast refresh rate! " + ChatColor.GRAY + "This image may skip frames when on a wall.");
                                }

                                CanvasManager.saveDataYml();
                                sender.sendMessage(ChatColor.GOLD + "Changed refresh rate to " + newDelay + " ticks per frame.");
                                break;
                            }
                        } catch (NumberFormatException var22) {
                        }

                        sender.sendMessage(ChatColor.GOLD + "Invalid refresh rate! " + ChatColor.GRAY + "Must be a natural number of ticks (1/20 second)");
                    }
                    break;
                case "stats":
                    if (!sender.hasPermission("luma.stats")) {
                        sender.sendMessage(ChatColor.GOLD + "Luma: " + ChatColor.GRAY + "You don't have permission to do this!");
                        return false;
                    }

                    sender.sendMessage(LumaReloaded.logo + " Load statistics:");
                    sender.sendMessage(ChatColor.GOLD + "  Images: " + ChatColor.GRAY + CanvasManager.getNumberOfCanvases() + " (" + CanvasManager.getNumberOfLoadedCanvases() + " loaded)");
                    sender.sendMessage(ChatColor.GOLD + "  Tiles: " + ChatColor.GRAY + CanvasManager.getNumberOfTiles() + " (" + CanvasManager.getNumberOfLoadedTiles() + " loaded)");
                    sender.sendMessage(ChatColor.GOLD + "  FPS (cum.): " + ChatColor.GRAY + LoadStatistics.averageCumulativeFPS());
                    sender.sendMessage(ChatColor.GOLD + "  RAM (est.): " + ChatColor.GRAY + (double)Math.round((double)CanvasManager.getNetMemoryLoad() / 100000.0) / 10.0 + "M");
                    long cpu = LoadStatistics.averageCanvasDrawNanos() + LoadStatistics.averageFrameAdvanceNanos();
                    sender.sendMessage(ChatColor.GOLD + "  CPU (est.): " + ChatColor.GRAY + (double)Math.round((double)cpu / 10000.0) / 100.0 + "ms/tick (" + (double)Math.round((double)cpu / 5000.0) / 100.0 + "%)");
                    sender.sendMessage("");
            }

            return true;
        }
    }
}
