package dev.mizio.mcPlugins.ocTPA.commands.handlers;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import org.bukkit.command.CommandSender;

import java.util.Map;


public class PluginInvalidUsageCmdHandler implements InvalidUsageHandler<CommandSender> {


    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        CommandSender sender = invocation.sender();
        Schematic schematic = result.getSchematic();

        if (schematic.isOnlyFirst()) {
            sender.sendMessage(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("help-cmd-oneline"),
                    Map.of("command", schematic.first())
            ));
            return;
        }
        sender.sendMessage(StringUtil.textFormatting(
                MainOcTPA.instance().getPluginConfig().getTranslations("help-cmd-multiline-header")
        ));
        for (String scheme : schematic.all()) {
            sender.sendMessage(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("help-cmd-multiline-command"),
                    Map.of("command", scheme)
            ));
        }

    }
}
