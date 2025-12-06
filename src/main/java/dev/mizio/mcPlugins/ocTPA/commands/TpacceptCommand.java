package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPACCEPT;
/**
 * Komenda akceptująca teleportację przez docelowego gracza
 */
@Command(name = "tpakceptuje", aliases = {"tpaccept"} )
@Permission(PERMS_CMD_TPACCEPT)
public class TpacceptCommand {

    @Execute
    public void execute(@Context Player player) {
        MainOcTPA.instance().teleportationService().tpAccept(player);
    }

    @Execute
    public void execute(@Context Player who, @Arg("nazwa_gracza") Player target) {
        if (who.getUniqueId().equals(target.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpAcceptFromCurrentPlayer(who, target);
    }
}
