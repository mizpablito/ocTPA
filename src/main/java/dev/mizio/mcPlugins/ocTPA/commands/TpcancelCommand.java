package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPCANCEL;
/**
 * Komenda anulująca żądanie teleportacji do gracza
 */
@Command(name = "tpanuluj")
@Permission(PERMS_CMD_TPCANCEL)
public class TpcancelCommand {

    @Execute
    public void execute(@Context Player player) {
        MainOcTPA.instance().teleportationService().tpCancel(player);
    }
}
