package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.strict.StrictMode;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPHERE;

/**
 * Komenda tworząca żądanie teleportacji wskazanego gracza do siebie
 */
@Command(name = "tptutaj", strict = StrictMode.ENABLED)
@Permission(PERMS_CMD_TPHERE)
public class TphereCommand {

    @Execute
    public void execute(@Context Player who, @Arg("nazwa_gracza") Player target) {
        if (!MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tphere()) {
            who.sendMessage(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("feature-disabled")
            ));
            return;
        }
        if (who.getUniqueId().equals(target.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpHereRequest(who, target);
    }
}
