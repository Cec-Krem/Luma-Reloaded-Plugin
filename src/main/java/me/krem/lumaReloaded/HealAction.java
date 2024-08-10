package me.krem.lumaReloaded;

import java.util.Iterator;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class HealAction extends ClickAction {
    public HealAction() {
    }

    public String getTypeString() {
        return "Heal";
    }

    public String getData() {
        return "";
    }

    public void run(Player player, ItemFrame itemFrame) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        Iterator var3 = player.getActivePotionEffects().iterator();

        while(var3.hasNext()) {
            PotionEffect pe = (PotionEffect)var3.next();
            player.removePotionEffect(pe.getType());
        }

    }
}
