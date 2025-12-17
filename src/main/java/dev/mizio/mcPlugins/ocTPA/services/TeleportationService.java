package dev.mizio.mcPlugins.ocTPA.services;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.PluginConfig;
import dev.mizio.mcPlugins.ocTPA.repositories.TeleportationRepository;
import dev.mizio.mcPlugins.ocTPA.services.entities.ReturnRequest;
import dev.mizio.mcPlugins.ocTPA.services.entities.TeleportRequest;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class TeleportationService {

    private final MainOcTPA plugin;
    protected TeleportationRepository repository;
    protected final String coolDownTPA = "tpa";
    protected final String coolDownTpaHere = "tpaHere";

    public TeleportationService() {
        this.plugin = MainOcTPA.instance();
        repository = new TeleportationRepository();
        repository.start();
    }

    public void closeService() {
        repository.stop();
        repository = null;
    }

    public void tpRequest(Player requester, Player target) {
        if (repository.isOnCooldown(requester.getUniqueId(), coolDownTPA) && !requester.hasPermission(PluginConfig.PERMS_BYPASS_COOLDOWN)) {
            requester.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-cooldown"),
                    Map.of("seconds", Integer.toString(repository.getCooldown(requester.getUniqueId(), coolDownTPA)))
            ));
            return;
        }

        if (repository.getRequestBySender(requester.getUniqueId()) != null) {
            requester.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-pending")
            ));
            return;
        }

        if (plugin.economyService().isFreeTeleport(false)) {
            plugin.debugInfo("Teleportacja darmowa, ponieważ w configu koszt teleportacji wynosi <= 0 albo Repo Ekonomii nie zstało załadowane!");
        } else {
            plugin.debugInfo("Koszt teleportacji jest dodatni, sprawdzanie czy gracz ma wystarczająco kasy...");
            if (!plugin.economyService().hasEnough(requester,false)) {
                requester.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-not-enough-money"),
                        Map.of("cost", plugin.economyService().getCostOfTeleportation(false))
                ));
                return;
            }
        }

        repository.addCooldown(requester.getUniqueId(), coolDownTPA, plugin.getPluginConfig().getTpSetting_time_cooldown_cmd_tpa());

        repository.createRequest(
                requester.getUniqueId(),
                target.getUniqueId(),
                false
        );

        requester.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("header-line")));
        requester.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-sent"),
                Map.of(
                        "playername", target.getName(),
                        "seconds", String.valueOf(plugin.getPluginConfig().getTpSetting_times_timeout_acceptance())
                )
        ));
        requester.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("footer-line")));

        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("header-line")));
        target.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-received"),
                Map.of(
                        "playername", requester.getName(),
                        "seconds", String.valueOf(plugin.getPluginConfig().getTpSetting_times_timeout_acceptance())
                )
        ));
        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-accept-deny")));
        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("footer-line")));
    }

    public void tpHereRequest(Player requester, Player target) {
        if (repository.isOnCooldown(requester.getUniqueId(), coolDownTpaHere) && !requester.hasPermission(PluginConfig.PERMS_BYPASS_COOLDOWN)) {
            requester.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-cooldown"),
                    Map.of("seconds", Integer.toString(repository.getCooldown(requester.getUniqueId(), coolDownTpaHere)))
            ));
            return;
        }

        if (repository.getRequestBySender(requester.getUniqueId()) != null) {
            requester.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-pending")
            ));
            return;
        }

        if (plugin.economyService().isFreeTeleport(false)) {
            plugin.debugInfo("Teleportacja darmowa, ponieważ w configu koszt teleportacji wynosi <= 0 albo Repo Ekonomii nie zstało załadowane!");
        } else {
            plugin.debugInfo("Koszt teleportacji jest dodatni, sprawdzanie czy gracz ma wystarczająco kasy...");
            if (!plugin.economyService().hasEnough(requester,false)) {
                requester.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-not-enough-money"),
                        Map.of("cost", plugin.economyService().getCostOfTeleportation(false))
                ));
                return;
            }
        }

        repository.addCooldown(requester.getUniqueId(), coolDownTpaHere, plugin.getPluginConfig().getTpSetting_time_cooldown_cmd_tpaHere());

        repository.createRequest(
                requester.getUniqueId(),
                target.getUniqueId(),
                true
        );

        requester.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("header-line")));
        requester.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-sent"),
                Map.of(
                        "playername", target.getName(),
                        "seconds", String.valueOf(plugin.getPluginConfig().getTpSetting_times_timeout_acceptance())
                )
        ));
        requester.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("footer-line")));


        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("header-line")));
        target.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-received"),
                Map.of(
                        "playername", requester.getName(),
                        "seconds", String.valueOf(plugin.getPluginConfig().getTpSetting_times_timeout_acceptance())
                )
        ));
        target.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-warning-tpa-here"),
                Map.of("playername",  requester.getName())
        ));
        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-accept-deny")));
        target.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("footer-line")));
    }

    public void tpDeny(Player player) {
        List<TeleportRequest> requests = repository.getRequestsForPlayer(player.getUniqueId());
        if (requests.isEmpty()) {
            player.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-not-found")
            ));
            return;
        }

        TeleportRequest request = requests.get(0);
        Player requestSender = plugin.getServer().getPlayer(request.getSender());
        String senderName = (requestSender != null ? requestSender.getName() : "Nieznany Gracz");

        repository.cancelRequest(
                requests.get(0),
                StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-denied-by"),
                        Map.of("playername", player.getName())
                ),
                StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-denied"),
                        Map.of("playername", senderName))
        );
    }

    public void tpDenyFromCurrentPlayer(Player player, Player requester) {
        List<TeleportRequest> requests = repository.getRequestsForPlayer(player.getUniqueId());
        if (requests.isEmpty()) {
            player.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-not-found")
            ));
            return;
        }

        Player requestSender = plugin.getServer().getPlayer(requester.getUniqueId());

        if (requestSender == null) {
            player.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-errors-player-not-found")));
            return;
        }

        TeleportRequest request = repository.getRequest(requestSender.getUniqueId(), player.getUniqueId());

        if (request == null) {
            player.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-not-found-by"),
                    Map.of("playername", requestSender.getName())
            ));
            return;
        }

        repository.cancelRequest(
                request,
                StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-denied-by"),
                        Map.of("playername", requestSender.getName())
                ),
                StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-denied"),
                Map.of("playername", player.getName())
                ));
    }

    public void tpAccept(Player player) {
        List<TeleportRequest> requests = repository.getRequestsForPlayer(player.getUniqueId());

        if (requests.isEmpty()) {
            player.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-error-not-found")));
            return;
        }

        TeleportRequest request = null;
        Player requester = null;
        for (int i = requests.size() - 1; i >= 0; i--) {
            requester = plugin.getServer().getPlayer(requests.get(i).getSender());
            if (requester != null) {
                request = requests.get(i);
                break;
            }
        }

        if (request == null) {
            player.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-error-not-found")));
            return;
        }

        if (plugin.economyService().isFreeTeleport(false)) {
            plugin.debugInfo("[tpAccept] Teleportacja darmowa, ponieważ w configu koszt teleportacji wynosi <= 0 albo Repo Ekonomii nie zstało załadowane!");
        } else {
            plugin.debugInfo("[tpAccept] Koszt teleportacji jest dodatni, sprawdzanie czy gracz ma wystarczająco kasy...");
            if (!plugin.economyService().hasEnough(requester, false)) {
                requester.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-not-enough-money-after-accepted"),
                        Map.of("cost", plugin.economyService().getCostOfTeleportation(false))
                ));
                repository.cancelRequest(
                        request,
                        StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("request-canceled-not-enough-money-to"),
                                Map.of("playername", (request.isHereRequest() ? "Ciebie" : player.getName()))
                                ),
                        StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled-not-enough-money-from"),
                                Map.of("playername", requester.getName()))
                        );
                plugin.debugInfo("[tpAccept] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało anulowane z braku wystarczającej ilości kasy!");
                return;
            }
        }

        repository.acceptRequest(request);
        player.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-accepted"),
                Map.of("playername", requester.getName())
        ));
    }

    public void tpAcceptFromCurrentPlayer(Player player, Player requester) {
        Player requestSender = plugin.getServer().getPlayer(requester.getUniqueId());
        if (requestSender == null) {
            player.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-errors-player-not-found")));
            return;
        }

        TeleportRequest request = repository.getRequest(requestSender.getUniqueId(), player.getUniqueId());
        if (request == null) {
            player.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-not-found-by"),
                    Map.of("playername", requestSender.getName())
            ));
            return;
        }
        if (plugin.economyService().isFreeTeleport(false)) {
            plugin.debugInfo("[tpAccept] Teleportacja darmowa, ponieważ w configu koszt teleportacji wynosi <= 0 albo Repo Ekonomii nie zstało załadowane!");
        } else {
            plugin.debugInfo("[tpAccept] Koszt teleportacji jest dodatni, sprawdzanie czy gracz ma wystarczająco kasy...");
            if (!plugin.economyService().hasEnough(requester, false)) {
                requestSender.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-not-enough-money-after-accepted"),
                        Map.of("cost", plugin.economyService().getCostOfTeleportation(false))
                ));
                repository.cancelRequest(
                        request,
                        StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("request-canceled-not-enough-money-to"),
                                Map.of("playername", player.getName())
                        ),
                        StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled-not-enough-money-from"),
                                Map.of("playername", requester.getName()))
                );
                plugin.debugInfo("[tpAccept] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało anulowane z braku wystarczającej ilości kasy!");
                return;
            }
        }
        repository.acceptRequest(request);
        player.sendMessage(StringUtil.textFormatting(
                plugin.getPluginConfig().getTranslations("request-accepted"),
                Map.of("playername", requestSender.getName())
        ));
    }

    public void tpCancel(Player player) {
        TeleportRequest teleportRequest = repository.getRequestBySender(player.getUniqueId());
        if (teleportRequest == null) {
            player.sendMessage(StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-error-not-found")));
        } else {
            repository.cancelRequest(
                    teleportRequest,
                    StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled")),
                    StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled-by"), Map.of("playername", player.getName()))
            );
        }
    }

    public void moveEvent(@NotNull Player player) {
        TeleportRequest teleportRequest = repository.getRequestBySender(player.getUniqueId());
        if ( teleportRequest != null && teleportRequest.isTeleporting() ) {
            plugin.debugInfo("Gracz " + player.getUniqueId() + " wykonał ruch podczas procesu teleportacji.");
            Player receiver = plugin.getServer().getPlayer(teleportRequest.getReceiver());

            repository.cancelRequest(
                    teleportRequest,
                    plugin.getPluginConfig().getTranslations("request-moved-to"),
                    Map.of("playername", receiver != null ? receiver.getName() : "[Offline]"),
                    plugin.getPluginConfig().getTranslations("request-moved-from"),
                    Map.of("playername", player.getName())
            );
        }

        ReturnRequest returnRequest = repository.getPlayerReturnRequest(player.getUniqueId());
        if ( returnRequest != null && returnRequest.isTeleporting() ) {
            plugin.debugInfo("Gracz " + player.getName() + " (" + player.getUniqueId() + ") wykonał ruch podczas procesu powrotu.");

            repository.cancelRequest(
                    returnRequest,
                    StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-moved-return"))
            );
        }
    }

    public void playerDamageEvent(@NotNull Player player) {
        TeleportRequest teleportRequest = repository.getRequestBySender(player.getUniqueId());
        if ( teleportRequest != null && teleportRequest.isTeleporting() ) {
            plugin.debugInfo("Gracz " + player.getName() + " (" + player.getUniqueId() + ") otrzymał obrażenia w trakcie teleportacji.");
            Player receiver = plugin.getServer().getPlayer(teleportRequest.getReceiver());

            repository.cancelRequest(
                    teleportRequest,
                    plugin.getPluginConfig().getTranslations("request-moved-to"),
                    Map.of("playername", receiver != null ? receiver.getName() : "[Offline]"),
                    plugin.getPluginConfig().getTranslations("request-moved-from"),
                    Map.of("playername", player.getName())
            );
        }

        ReturnRequest returnRequest = repository.getPlayerReturnRequest(player.getUniqueId());
        if(returnRequest != null && returnRequest.isTeleporting()) {
            plugin.debugInfo("Gracz " + player.getName() + " (" + player.getUniqueId() + ") otrzymał obrażenia w trakcie procesu powrotu.");

            repository.cancelRequest(
                    returnRequest,
                    StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-moved-return"))
            );
        }
    }

    public void playerQuitEvent(@NotNull Player player) {
        plugin.debugInfo("Usuwanie cooldownów i żądań dla " + player.getName() + ", ponieważ gracz opuścił serwer.");
        // Może być podatne na wykorzystanie, jeśli gracz opuści serwer i dołączy do niego ponownie przed upływem czasu odnowienia.
        // Ale nie sądzę, żeby to był duży problem.
        // Zużycie pamięci jest nieco ważniejsze niż upewnienie się, że nigdy nie uda się ominąć czasu odnowienia.
        repository.removeCooldowns(player.getUniqueId());

        // Usuń wszystkie oczekujące żądania
        repository.removeRequests(player.getUniqueId());
    }

    public void tpReturn(Player player) {
        ReturnRequest returnRequest = repository.getPlayerReturnRequest(player.getUniqueId());

        if (returnRequest == null) {
            player.sendMessage(StringUtil.textFormatting(
                    plugin.getPluginConfig().getTranslations("request-error-return-not-found")
            ));
            return;
        }

        returnRequest.setRequested();
    }

    public SuggestionResult getRequestsForPlayer(UUID playerId) {
        return repository.getRequestsForPlayer(playerId).stream()
                .map(TeleportRequest::getSender)       // Pobieramy UUID nadawcy
                .map(Bukkit::getPlayer)                // Zamieniamy na Playera (może być null jeśli offline)
                .filter(Objects::nonNull)              // Filtrujemy tylko graczy online
                .map(Player::getName)                  // Pobieramy nick
                .collect(SuggestionResult.collector()); // Zwracamy jako wynik podpowiedzi
    }
}
