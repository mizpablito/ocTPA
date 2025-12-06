package dev.mizio.mcPlugins.ocTPA.listenings;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener  implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            MainOcTPA.instance().teleportationService().playerDamageEvent(player);
        }
    }
}
