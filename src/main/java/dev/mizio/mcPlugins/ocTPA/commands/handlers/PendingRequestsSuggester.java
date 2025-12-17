package dev.mizio.mcPlugins.ocTPA.commands.handlers;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.suggester.Suggester;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PendingRequestsSuggester implements Suggester<CommandSender, Player> {

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Player> argument, SuggestionContext context) {

        if (!(invocation.sender() instanceof Player receiver)) {
            return SuggestionResult.empty();
        }

        // Pobieramy listę próśb skierowanych do tego gracza
        return MainOcTPA.instance().teleportationService().getRequestsForPlayer(receiver.getUniqueId());
    }
}