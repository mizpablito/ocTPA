package dev.mizio.mcPlugins.ocTPA.listenings;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class PlayerMoveListener implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        // Interesują nas tylko zmiany pozycji
        // TODO: do weryfikacji zużycie zasobów przez ten check
        if(
                Objects.requireNonNull(event.getFrom()).getBlockX() == Objects.requireNonNull(event.getTo()).getBlockX()
                        && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                        && event.getFrom().getBlockY() == event.getTo().getBlockY()
        ) {
            return;
        }

        MainOcTPA.instance().teleportationService().moveEvent(event.getPlayer());
    }
}
