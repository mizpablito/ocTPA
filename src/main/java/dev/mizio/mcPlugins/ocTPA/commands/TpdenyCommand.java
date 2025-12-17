package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPDENY;
/**
 * Komenda odrzucająca teleportację przez docelowego gracza
 */
@Command(name = "tpodrzucam")
@Permission(PERMS_CMD_TPDENY)
public class TpdenyCommand {

    @Execute
    public void execute(@Context Player player) {
        MainOcTPA.instance().teleportationService().tpDeny(player);
    }

    @Execute
    public void execute(@Context Player who, @Arg("od_kogo_prośba") Player target) {
        if (who.getUniqueId().equals(target.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpDenyFromCurrentPlayer(who, target);
    }
}
