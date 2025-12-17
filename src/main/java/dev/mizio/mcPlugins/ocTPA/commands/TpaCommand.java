package dev.mizio.mcPlugins.ocTPA.commands;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.execute.ExecuteDefault;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.strict.StrictMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static dev.mizio.mcPlugins.ocTPA.PluginConfig.*;

/**
 * Komenda tworząca żądanie teleportacji do wskazanego gracza
 */
@Command(name = "teleportacja", strict = StrictMode.ENABLED)
@Permission(PERMS_CMD_TPA)
public class TpaCommand {

    @Execute( name = "do")
    @Permission(PERMS_CMD_TPA)
    public void execute(@Context Player who, @Arg("nazwa_gracza") Player target) {
        if (who.getUniqueId().equals(target.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpRequest(who, target);
    }

    @Execute(name = "tak")
    @Permission(PERMS_CMD_TPACCEPT)
    public void tpAccept(@Context Player who) {
        MainOcTPA.instance().teleportationService().tpAccept(who);
    }

    @Execute(name = "tak")
    @Permission(PERMS_CMD_TPACCEPT)
    public void tpAcceptFromWhom(@Context Player who, @Arg("od_kogo_prośba") Player from) {
        if (who.getUniqueId().equals(from.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpAcceptFromCurrentPlayer(who, from);
    }

    @Execute(name = "nie")
    @Permission(PERMS_CMD_TPDENY)
    public void tpDeny(@Context Player who) {
        MainOcTPA.instance().teleportationService().tpDeny(who);
    }

    @Execute(name = "nie")
    @Permission(PERMS_CMD_TPDENY)
    public void tpDenyFromWhom(@Context Player who, @Arg("od_kogo_prośba") Player from) {
        if (who.getUniqueId().equals(from.getUniqueId())) {
            who.sendMessage(StringUtil.textFormatting(MainOcTPA.instance().getPluginConfig().getTranslations("tp-to-himself")));
            return;
        }
        MainOcTPA.instance().teleportationService().tpDenyFromCurrentPlayer(who, from);
    }

    @Execute(name = "anuluj")
    @Permission(PERMS_CMD_TPCANCEL)
    public void tpCancel(@Context Player who) {
        MainOcTPA.instance().teleportationService().tpCancel(who);
    }

    @Execute(name = "powrot")
    @Permission(PERMS_CMD_TPRETURN)
    public void tpReturn(@Context Player who) {
        if (!MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tpreturn()) {
            who.sendMessage(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("feature-disabled")
            ));
            return;
        }
        MainOcTPA.instance().teleportationService().tpReturn(who);
    }

    @Execute(name = "do-mnie")
    @Permission(PERMS_CMD_TPHERE)
    public void tpHere(@Context Player who, @Arg("nazwa_gracza") Player target) {
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

    @Execute(name = "koszt")
    @Permission(PERMS_CMD_TPA)
    public void tpCost(@Context Player who) {
        who.sendMessage(StringUtil.textFormatting(
                MainOcTPA.instance().getPluginConfig().getTranslations("header-line")
        ));

        TextComponent.Builder costsInfo = Component.text();

        boolean isTpaCostFree = MainOcTPA.instance().economyService().isFreeTeleport(false);
        boolean isReturnCostFree = MainOcTPA.instance().economyService().isFreeTeleport(true);
        boolean isReturnEnabled = MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tpreturn();

        boolean costsActive = false;

        if (!isTpaCostFree) {
            String cost = MainOcTPA.instance().economyService().getCostOfTeleportation(false);

            costsInfo.append(Component.text("Koszt teleportacji do gracza: ", NamedTextColor.GRAY))
                    .append(Component.text(cost, NamedTextColor.GOLD));
            costsActive = true;
        }

        if (!isReturnCostFree && isReturnEnabled) {
            String cost = MainOcTPA.instance().economyService().getCostOfTeleportation(true);
            int timeReturn = MainOcTPA.instance().getPluginConfig().getTpSetting_times_timeout_return();

            costsInfo.appendNewline().append(Component.text("Koszt powrotu (Limit czasu: " + timeReturn + "s): ", NamedTextColor.GRAY))
                    .append(Component.text(cost, NamedTextColor.GOLD));
            costsActive = true;
        }

        if (!costsActive) {
            // Jeśli nie wyświetlono żadnego kosztu (koszty są 0 lub usługa jest wyłączona)
            costsInfo.append(StringUtil.textFormatting(
                    MainOcTPA.instance().getPluginConfig().getTranslations("economy-free-teleportation")
            ));
        }

        who.sendMessage(costsInfo.build());
//        who.sendMessage(StringUtil.textFormatting(
//                MainOcTPA.instance().getPluginConfig().getTranslations("footer-line")
//        ));
    }

    @ExecuteDefault
    @Execute(name = "pomoc") //, aliases = {"help"})
    public void showPluginHelp(@Context CommandSender sender) {
        sender.sendMessage(StringUtil.textFormatting(
                MainOcTPA.instance().getPluginConfig().getTranslations("header-line-help")
        ));
        sender.sendMessage(StringUtil.PLUGIN_INFO_COMPONENT);

        TextComponent.Builder helpBuilder = Component.text()
                .append(Component.text("--- Dostępne komendy ---", NamedTextColor.GRAY).appendNewline());

        // --- Lista komend ---

        // Główna komenda (TPA)
        helpBuilder.append(Component.text("/teleportacja do <gracz>", NamedTextColor.YELLOW))
                .append(Component.text(" - Prośba o teleport do gracza.", NamedTextColor.GRAY).appendNewline());

        // Akceptacja
        helpBuilder.append(Component.text("/teleportacja tak [gracz]", NamedTextColor.YELLOW))
                .append(Component.text(" - Akceptuje prośbę TP (konkretną lub ostatnią).", NamedTextColor.GRAY).appendNewline());

        // Odrzucenie
        helpBuilder.append(Component.text("/teleportacja nie [gracz]", NamedTextColor.YELLOW))
                .append(Component.text(" - Odrzuca prośbę TP (konkretną lub ostatnią).", NamedTextColor.GRAY).appendNewline());

        // Anulowanie
        helpBuilder.append(Component.text("/teleportacja anuluj", NamedTextColor.YELLOW))
                .append(Component.text(" - Anuluje oczekującą/trwającą prośbę wysłaną przez Ciebie.", NamedTextColor.GRAY).appendNewline());

        // TP HERE (tylko jeśli włączone)
        if (MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tphere()) {
            helpBuilder.append(Component.text("/teleportacja do-mnie <gracz>", NamedTextColor.YELLOW))
                    .append(Component.text(" - Prośba, aby gracz przeteleportował się do Ciebie.", NamedTextColor.GRAY).appendNewline());
        } else {
            helpBuilder.append(Component.text("/teleportacja do-mnie", NamedTextColor.DARK_RED))
                    .append(Component.text(" - Funkcja wyłączona.", NamedTextColor.DARK_GRAY).appendNewline());
        }

        // TP RETURN (tylko jeśli włączone)
        if (MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tpreturn()) {
            int timeReturn = MainOcTPA.instance().getPluginConfig().getTpSetting_times_timeout_return();
            helpBuilder.append(Component.text("/teleportacja powrot", NamedTextColor.YELLOW))
                    .append(Component.text(" - Powrót do ostatniej lokalizacji przed TP w limicie czasu: " + timeReturn + "s.", NamedTextColor.GRAY).appendNewline());
        } else {
            helpBuilder.append(Component.text("/teleportacja powrot", NamedTextColor.DARK_RED))
                    .append(Component.text(" - Funkcja wyłączona.", NamedTextColor.DARK_GRAY).appendNewline());
        }

        // Dodajemy komendę KOSZT
        helpBuilder.append(Component.text("/teleportacja koszt", NamedTextColor.YELLOW))
                .append(Component.text(" - Wyświetla aktualne koszty teleportacji.", NamedTextColor.GRAY).appendNewline());

        // Pomoc
        helpBuilder.append(Component.text("/teleportacja pomoc", NamedTextColor.YELLOW))
                .append(Component.text(" - Wyświetla tę listę komend.", NamedTextColor.GRAY).appendNewline());

        // --- SEKACJA KOSZTÓW ---

        boolean isTpaCostFree = MainOcTPA.instance().economyService().isFreeTeleport(false);
        boolean isReturnCostFree = MainOcTPA.instance().economyService().isFreeTeleport(true);
        boolean isReturnEnabled = MainOcTPA.instance().getPluginConfig().isTpSetting_functionEnabled_tpreturn();

        // Sprawdzamy, czy potrzebujemy pokazać koszty (czy TPA/TPHERE jest płatne LUB czy /tppowrot jest płatny i włączony)
        if (!isTpaCostFree || (!isReturnCostFree && isReturnEnabled)) {

            helpBuilder.appendNewline().append(Component.text("--- Koszty teleportacji ---", NamedTextColor.GRAY).appendNewline());

            // Koszt teleportacji do gracza (tylko jeśli płatna)
            if (!isTpaCostFree) {
                String cost = MainOcTPA.instance().economyService().getCostOfTeleportation(false);

                helpBuilder.append(Component.text("Koszt teleportacji: ", NamedTextColor.AQUA))
                        .append(Component.text(cost, NamedTextColor.GOLD)).appendNewline();
            } else {
                helpBuilder.append(Component.text("Koszt teleportacji: ", NamedTextColor.AQUA))
                        .append(Component.text("Darmowy", NamedTextColor.GREEN)).appendNewline();
            }

            // Koszt powrotu (tylko jeśli włączony i płatny)
            if (isReturnEnabled) {
                if (!isReturnCostFree) {
                    String cost = MainOcTPA.instance().economyService().getCostOfTeleportation(true);
                    int timeReturn = MainOcTPA.instance().getPluginConfig().getTpSetting_times_timeout_return();

                    helpBuilder.append(Component.text("Koszt powrotu ", NamedTextColor.AQUA))
                            .append(Component.text("(Limit czasu: " + timeReturn + "s) ", NamedTextColor.GRAY))
                            .append(Component.text(":", NamedTextColor.AQUA))
                            .append(Component.text(cost, NamedTextColor.GOLD));
                } else {
                    helpBuilder.append(Component.text("Koszt powrotu ", NamedTextColor.AQUA))
                            .append(Component.text(":", NamedTextColor.AQUA))
                            .append(Component.text("Darmowy", NamedTextColor.GREEN));
                }
            }
        }

        // Wyślij skompilowaną wiadomość
        sender.sendMessage(helpBuilder.build());

//        // Stopka
//        sender.sendMessage(StringUtil.textFormatting(
//                MainOcTPA.instance().getPluginConfig().getTranslations("footer-line")
//        ));
    }
}

/* TODO:
*   - Zrobić pod argument `od_kogo_prośba` własny typ zwracający nazwy graczy, którzy wysłali żądania
*     ~ https://docs.rollczi.dev/documentation/litecommands/arguments/arg/arg-custom.html
*
*/