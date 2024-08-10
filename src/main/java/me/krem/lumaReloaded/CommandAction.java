package me.krem.lumaReloaded;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public class CommandAction extends ClickAction {
    private final String command;

    public CommandAction(String[] message) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < message.length; ++i) {
            sb.append(message[i]).append(" ");
        }

        this.command = sb.toString();
    }

    public String getTypeString() {
        return "Command";
    }

    public String getData() {
        return this.command;
    }

    public void run(Player player, ItemFrame itemFrame) {
        player.performCommand(this.command);
    }
}
