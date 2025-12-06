package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.strict.StrictMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_TPA;
/**
 * Komenda tworząca żądanie teleportacji do wskazanego gracza
 */
@Command(name = "tpa", aliases = {"teleportacja"}, strict = StrictMode.ENABLED)
@Permission(PERMS_CMD_TPA)
public class TpaCommand {

    @Execute
    public void execute(@Context Player who, @Arg("nazwa_gracza") Player target) {
        if (who.getUniqueId().equals(target.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpRequest(who, target);
    }

    //TODO: pomoc
    @Execute(name = "pomoc") //, aliases = {"help"})
    public void showPluginHelp(@Context CommandSender sender) {
        //TODO: zwróć info o kosztach i dostępnych komendach uwzględniajać wyłączone
        sender.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("header-line")));

    }

}
