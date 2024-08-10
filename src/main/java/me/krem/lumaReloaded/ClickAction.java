package me.krem.lumaReloaded;

import java.util.HashMap;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public abstract class ClickAction {
    private static final HashMap<String, Class<? extends ClickAction>> SUBCLASS_TYPES = new HashMap();

    public ClickAction() {
    }

    public static ClickAction generate(String type, String[] data) {
        switch (type.toLowerCase()) {
            case "command":
                return new CommandAction(data);
            case "heal":
                return new HealAction();
            case "message":
                return new MessageAction(data);
            case "warp":
                return new WarpAction(data);
            default:
                return null;
        }
    }

    public static ClickAction generate(String type, String data) {
        return generate(type, data.split(" "));
    }

    public abstract void run(Player var1, ItemFrame var2);

    public abstract String getTypeString();

    public abstract String getData();

    static {
        SUBCLASS_TYPES.put("message", MessageAction.class);
    }
}
