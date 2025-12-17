package dev.mizio.mcPlugins.ocTPA.commands;


import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.strict.StrictMode;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPRETURN;

/**
 * Komenda pozwalająca na powrót do poprzedniej lokalizacji
 */
@Command(name = "tppowrot", strict = StrictMode.ENABLED)
@Permission(PERMS_CMD_TPRETURN)
public class TpreturnCommand {

    @Execute
    public void execute(@Context Player player) {
        if (!MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tpreturn()) {
            player.sendMessage(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("feature-disabled")
            ));
            return;
        }
        MainOcTPA.instance().teleportationService().tpReturn(player);
    }
}
