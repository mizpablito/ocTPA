package dev.mizio.mcPlugins.ocTPA.listenings;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        MainOcTPA.instance().teleportationService().playerQuitEvent(event.getPlayer());
    }
}
