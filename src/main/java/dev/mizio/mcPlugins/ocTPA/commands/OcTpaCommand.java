package dev.mizio.mcPlugins.ocTPA.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.PERMS_CMD_OCTPA;
/**
 * Komenda administracyjna
 */
@Command(name = "/octpa")
@Permission(PERMS_CMD_OCTPA)
@Deprecated(forRemoval = true)
public class OcTpaCommand {

    @Execute
    void execute(@Sender Player sender, @Arg("ile_kasy_wariacie") double amount) {
        sender.sendMessage("Jeśli będzie potrzeba to się doda taką komendę ;)");
//        MainOcTPA.instance().economyService().deposit(sender, amount);
    }

}
