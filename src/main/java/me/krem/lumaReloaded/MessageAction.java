package me.krem.lumaReloaded;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public class MessageAction extends ClickAction {
    private final String message;

    public MessageAction(String[] message) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < message.length; ++i) {
            sb.append(message[i].replace("&", "§")).append(" ");
        }

        this.message = sb.toString();
    }

    public String getTypeString() {
        return "Message";
    }

    public String getData() {
        return this.message;
    }

    public void run(Player player, ItemFrame itemFrame) {
        player.sendMessage(this.message);
    }
}
