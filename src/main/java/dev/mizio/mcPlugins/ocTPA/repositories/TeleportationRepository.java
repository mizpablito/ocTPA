package dev.mizio.mcPlugins.ocTPA.repositories;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.PluginConfig;
import dev.mizio.mcPlugins.ocTPA.services.entities.ReturnRequest;
import dev.mizio.mcPlugins.ocTPA.services.entities.TeleportRequest;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TeleportationRepository {

    private final MainOcTPA plugin;

    public TeleportationRepository() {
        this.plugin = MainOcTPA.instance();
    }

    /*  ------------------------  */
    /*  Teleport Request Manager  */
    /*  ------------------------  */
    private final List<TeleportRequest> requests = new ArrayList<>();
    private final Map<UUID, ReturnRequest>  returnRequests = new HashMap<>();
    public BukkitTask scheduler;

    public void start() {
        if (this.scheduler != null && !this.scheduler.isCancelled()) {
            plugin.getLogger().warning("Harmonogram teleportacji został uruchomiony, podczas gdy inny już działał.");
            this.scheduler.cancel();
        }

        this.scheduler = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            var warmUpTime = plugin.getPluginConfig().getTpSetting_time_cooldown_after_accept();
            var ignoredPlayersForThisRun = processReturnRequests(warmUpTime);
            processTeleportRequests(ignoredPlayersForThisRun, warmUpTime);
        }, 0, 20);
        plugin.getLogger().info("Harmonogram teleportacji został uruchomiony.");
    }

    public void stop() {
        if (scheduler != null && !scheduler.isCancelled()) {
            plugin.getLogger().info("Zatrzymanie zadania harmonogramu teleportacji.");
            scheduler.cancel();
        } else {
            plugin.getLogger().warning("Zadanie harmonogramu teleportacji zostało już zatrzymane.");
        }
    }

    private void processTeleportRequests(List<UUID> ignoredPlayersForThisRun, long warmUpTime) {
        var tempRequests = new ArrayList<>(requests);

        for (var request : tempRequests) {
            var sender = plugin.getServer().getPlayer(request.getSender());
            var receiver = plugin.getServer().getPlayer(request.getReceiver());

            var teleportPlayer = request.isHereRequest() ? receiver : sender;
            var teleportPlayerTo = request.isHereRequest() ? sender : receiver;

            if (sender == null || receiver == null) {
                plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało usunięte, ponieważ nadawca lub odbiorca zniknął.");
                cancelRequest(request);
                continue;
            }

            if (ignoredPlayersForThisRun.contains(request.getSender())) {
                plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało zignorowane, ponieważ nadawca wcześniej zażądał powrotu.");
                continue;
            }

            var timeoutValue = plugin.getPluginConfig().getTpSetting_times_timeout_acceptance();
            if (request.isTimedOut(timeoutValue) && !request.isAccepted() && !request.isTeleporting()) {
                plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " wygasło (przekroczono czas).");

                cancelRequest(request,
                        plugin.getPluginConfig().getTranslations("request-timeout-to"), Map.of("playername", receiver.getName()),
                        plugin.getPluginConfig().getTranslations("request-timeout-from"), Map.of("playername", sender.getName())
                );

                continue;
            }

            if (request.isAccepted() && !request.isTeleporting()) {
                plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało zaakceptowane.");
                if (warmUpTime > 0 && !sender.hasPermission(PluginConfig.PERMS_BYPASS_COOLDOWN)) {
                    sender.sendMessage(StringUtil.textFormatting(
                            request.isHereRequest() ?
                                    plugin.getPluginConfig().getTranslations("request-cooldown-to-here")
                                    : plugin.getPluginConfig().getTranslations("request-cooldown-to"),
                            Map.of("playername", receiver.getName(), "time", String.valueOf(warmUpTime))
                            )
                    );

                    receiver.sendMessage(StringUtil.textFormatting(
                            request.isHereRequest() ?
                                    plugin.getPluginConfig().getTranslations("request-cooldown-from-here")
                                    : plugin.getPluginConfig().getTranslations("request-cooldown-from"),
                            Map.of("playername", sender.getName(), "time", String.valueOf(warmUpTime))
                            )
                    );
                }
                request.setTeleporting(true);
                request.setWarmUpSinceTimestamp(System.currentTimeMillis());
            }

            if (request.isTeleporting() && (request.getWarmUpSinceTimestamp() + (warmUpTime * 1000) <= System.currentTimeMillis()
                    || sender.hasPermission(PluginConfig.PERMS_BYPASS_COOLDOWN))) {

                // Safe position (Start)
                Location finalLocation = teleportPlayerTo.getLocation();

                if (plugin.getPluginConfig().isTpSetting_safePosition_enabled()) {
                    plugin.debugInfo("[processTeleport] Szukanie bezpiecznej pozycji...");
                    Location safeLoc = tryFindSafeLocation(finalLocation);

                    if (safeLoc == null) {
                        plugin.debugInfo("[processTeleport] Nie udało się znaleźć bezpiecznego miejsca dla " + teleportPlayer.getName());

                        teleportPlayer.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("request-unsafe-destination")
                        ));

                        // Opcjonalnie powiadom drugą stronę
                        teleportPlayerTo.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("request-unsafe-destination-other")
                        ));

                        requests.remove(request);
                        continue;
                    }
                    finalLocation = safeLoc;
                }
                // Safe position (End)

                // Koszty teleportacji
                Player costSource = request.isHereRequest() ? sender : teleportPlayer;

                if (plugin.economyService().isFreeTeleport(false)) {
                    plugin.debugInfo("[processTeleport] Teleportacja darmowa, ponieważ koszt <= 0 albo brak ekonomii!");
                } else {
                    plugin.debugInfo("[processTeleport] Koszt teleportacji jest dodatni, sprawdzanie czy gracz ma wystarczająco kasy...");
                    if (!plugin.economyService().hasEnough(costSource, false)) {
                        costSource.sendMessage(StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("economy-not-enough-money-after-accepted"),
                                Map.of("cost", plugin.economyService().getCostOfTeleportation(false))
                        ));
                        requests.remove(request);
                        plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało anulowane z braku wystarczającej ilości kasy!");
                        continue;
                    }
                }

                if (!plugin.economyService().withdraw(costSource, false)) {
                    costSource.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("economy-error-while-processing-costs")
                    ));
                    plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało anulowane z powodu problemu pobrania kosztów teleportacji z konta.");
                    continue;
                } else {
                    costSource.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("economy-withdraw-success"),
                            Map.of(
                                    "cost", plugin.economyService().getCostOfTeleportation(false),
                                    "balance", plugin.economyService().getActuallyAccountBalance(costSource)
                            )
                    ));
                }

                teleportPlayer.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("request-teleported-to"),
                        Map.of("playername", teleportPlayerTo.getName())
                ));
                teleportPlayerTo.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("request-teleported-from"),
                        Map.of("playername", teleportPlayer.getName())
                ));

                Location teleportTarget = finalLocation;

                new BukkitRunnable() {
                    public void run() {
                        var returnRequest = new ReturnRequest(
                                teleportPlayer.getUniqueId(),
                                teleportPlayer.getLocation(),
                                System.currentTimeMillis()
                        );
                        returnRequests.put(teleportPlayer.getUniqueId(), returnRequest);

                        teleportPlayer.teleport(teleportTarget);
                        plugin.teleportEffectService().run(teleportPlayer);
                    }
                }.runTask(plugin);

                plugin.debugInfo("[processTeleport] Żądanie od " + request.getSender() + " do " + request.getReceiver() + " zostało zrealizowane.");
                requests.remove(request);
            }
        }
    }

    private List<UUID> processReturnRequests(long warmUpTime) {
        var returnedPlayers = new ArrayList<UUID>();

        for (var playerId : new ArrayList<>(returnRequests.keySet())) {
            var request = returnRequests.get(playerId);
            var teleportPlayer = Bukkit.getPlayer(request.getPlayerId());

            if (teleportPlayer == null) {
                returnRequests.remove(playerId);
                continue;
            }

            var hasBypassReturnTimeout = teleportPlayer.hasPermission(PluginConfig.PERMS_BYPASS_RETURN_TIMEOUT);
            var hasBypassWait = teleportPlayer.hasPermission(PluginConfig.PERMS_BYPASS_COOLDOWN);
            var isTeleporting = request.isTeleporting();

            if (!request.getRequested()) {
                var timeout = plugin.getPluginConfig().getTpSetting_times_timeout_return();

                if (!hasBypassReturnTimeout && timeout > 0 && request.isTimedOut(timeout)) {
                    plugin.debugInfo("[processReturn] Żądanie powrotu dla " + teleportPlayer.getName() + " (" + request.getPlayerId() + ") wygasło (przekroczono czas na wpisanie /teleportacja powrot).");

//                    // Opcjonalnie: wyślij wiadomość, że szansa na powrót przepadła
//                    teleportPlayer.sendMessage(StringUtil.textFormatting(
//                            plugin.getPluginConfig().getTranslations("request-return-timeout")
//                    ));

                    returnRequests.remove(playerId);
                }
                continue;
            }

            if (!isTeleporting) {
                request.setTeleporting(true);
                request.setWarmUpSinceTimestamp(System.currentTimeMillis());
                if (warmUpTime > 0 && !hasBypassWait) {
                    teleportPlayer.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("request-cooldown-return"),
                            Map.of("time", String.valueOf(warmUpTime))
                    ));
                }
            }

            long fulfillRequestAt = request.getWarmUpSinceTimestamp() + (warmUpTime * 1000);
            if (isTeleporting && (fulfillRequestAt <= System.currentTimeMillis() || hasBypassWait)) {

                // Safe position (Start)
                Location finalLocation = request.getLocation();

                if (plugin.getPluginConfig().isTpSetting_safePosition_enabled()) {
                    plugin.debugInfo("[processReturn] Szukanie bezpiecznej pozycji dla powrotu...");
                    Location safeLoc = tryFindSafeLocation(finalLocation);

                    if (safeLoc == null) {
                        plugin.debugInfo("[processReturn] Nie udało się znaleźć bezpiecznego miejsca powrotu dla " + teleportPlayer.getName());

                        teleportPlayer.sendMessage(StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("request-unsafe-destination")
                        ));

                        returnRequests.remove(playerId);
                        continue;
                    }
                    finalLocation = safeLoc;
                }
                // Safe position (End)


                if (plugin.economyService().isFreeTeleport(true)) {
                    plugin.debugInfo("[processReturn] Teleportacja darmowa (config lub brak repo).");
                } else {
                    plugin.debugInfo("[processReturn] Sprawdzanie stanu konta...");
                    if (!plugin.economyService().hasEnough(teleportPlayer, true)) {
                        teleportPlayer.sendMessage(StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("economy-not-enough-money-after-accepted"),
                                Map.of("cost", plugin.economyService().getCostOfTeleportation(true))
                        ));
                        returnRequests.remove(playerId);
                        plugin.debugInfo("[processReturn] Żądanie powrotu anulowane - brak środków.");
                        continue;
                    }
                }

                if (!plugin.economyService().withdraw(teleportPlayer, true)) {
                    teleportPlayer.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("economy-error-while-processing-costs")
                    ));
                    plugin.debugInfo("[processReturn] Błąd podczas pobierania środków.");
                    continue;
                    // Tutaj NIE usuwamy requestu od razu, może to błąd chwilowy bazy danych?
                    // Albo usuń returnRequests.remove(playerId) jeśli chcesz anulować.
                } else {
                    teleportPlayer.sendMessage(StringUtil.textFormatting(
                            plugin.getPluginConfig().getTranslations("economy-withdraw-success"),
                            Map.of(
                                    "cost", plugin.economyService().getCostOfTeleportation(true),
                                    "balance", plugin.economyService().getActuallyAccountBalance(teleportPlayer)
                            )
                    ));
                }

                returnRequests.remove(playerId);
                returnedPlayers.add(request.getPlayerId());

                Location teleportTarget = finalLocation;

                new BukkitRunnable() {
                    public void run() {
                        teleportPlayer.teleport(teleportTarget);
                        plugin.teleportEffectService().run(teleportPlayer);

                        // Opcjonalnie komunikat o sukcesie
                        teleportPlayer.sendMessage(StringUtil.textFormatting(
                                plugin.getPluginConfig().getTranslations("request-return")
                        ));
                    }
                }.runTask(plugin);

                plugin.debugInfo("[processReturn] Gracz " + teleportPlayer.getName() + " wrócił na poprzednią pozycję.");
            }
        }

        return returnedPlayers;
    }
    /**
     * Creates a new teleport request.
     */
    public void createRequest(UUID sender, UUID receiver, long timestamp, boolean isHereRequest) {
        requests.add(new TeleportRequest(sender, receiver, timestamp, isHereRequest));
    }

    /**
     * Creates a new teleport request with current timestamp.
     */
    public void createRequest(UUID sender, UUID receiver, boolean isHereRequest) {
        createRequest(sender, receiver, System.currentTimeMillis(), isHereRequest);
    }

    /**
     * Get all requests for a specific player (receiver).
     */
    public List<TeleportRequest> getRequestsForPlayer(UUID playerId) {
        return requests.stream()
                .filter(request -> request.getReceiver().equals(playerId))
                .collect(Collectors.toList());
    }

    /**
     * Get a request by the sender.
     */
    public TeleportRequest getRequestBySender(UUID playerId) {
        var requestTimeout = plugin.getPluginConfig().getTpSetting_times_timeout_acceptance();

        for (Iterator<TeleportRequest> teleportRequestIterator = requests.iterator(); teleportRequestIterator.hasNext(); ) {
            var request = teleportRequestIterator.next();

            if (request.getSender().equals(playerId)) {
                if (request.isTimedOut(requestTimeout)) {
                    teleportRequestIterator.remove();

                    return null;
                }

                return request;
            }
        }
        return null;
    }

    /**
     * Get a request between two players.
     */
    public TeleportRequest getRequest(UUID sender, UUID receiver) {
        for (var request : requests) {
            if (request.getSender().equals(sender) && request.getReceiver().equals(receiver)) {
                return request;
            }
        }

        return null;
    }

    public ReturnRequest getPlayerReturnRequest(UUID playerId) {
        return returnRequests.get(playerId);
    }

    /**
     * Remove all requests from or to a specific player.
     */
    public void removeRequests(UUID playerId) {
        var toRemove = requests.stream()
                .filter(request -> request.getSender().equals(playerId) || request.getReceiver().equals(playerId))
                .toList();

        toRemove.forEach(this::cancelRequest);
        requests.removeAll(toRemove);
        returnRequests.remove(playerId);
    }

    public void acceptRequest(TeleportRequest request) {
        request.setAccepted(true);
    }

    public void cancelRequest(TeleportRequest request, Component senderMessage, Component receiverMessage) {
        var sender = plugin.getServer().getPlayer(request.getSender());
        var receiver = plugin.getServer().getPlayer(request.getReceiver());

        if (sender == null && receiver == null) {
            requests.remove(request);
            return;
        }

        if (sender != null) {
            sender.sendMessage(senderMessage);
        }

        if (receiver != null && receiverMessage != null) {
            receiver.sendMessage(receiverMessage);
        }

        requests.remove(request);
    }

    public void cancelRequest(TeleportRequest request,
                              String senderMessage, Map<String, String> senderPlaceholders,
                              String receiverMessage, Map<String, String> receiverPlaceholders) {
        cancelRequest(request,
                StringUtil.textFormatting(senderMessage, senderPlaceholders),
                StringUtil.textFormatting(receiverMessage, receiverPlaceholders));
    }

    public void cancelRequest(TeleportRequest request, Component reason) {
        cancelRequest(request, reason, reason);
    }

    public void cancelRequest(ReturnRequest request, Component reason) {
        var sender = plugin.getServer().getPlayer(request.getPlayerId());

        if (sender != null) {
            sender.sendMessage(reason);
        }

        returnRequests.remove(request.getPlayerId());
    }

    public void cancelRequest(TeleportRequest request) {
        cancelRequest(request, StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled")));
    }

    public void cancelRequest(ReturnRequest request) {
        cancelRequest(request, StringUtil.textFormatting(plugin.getPluginConfig().getTranslations("request-canceled")));
    }

    // Technical functions
    private Location tryFindSafeLocation(Location origin) {
        int radius = plugin.getPluginConfig().getTpSetting_safePosition_maxRadius();
        int tries = plugin.getPluginConfig().getTpSetting_safePosition_maxTries();

        if (isSafeLocation(origin)) {
            return origin;
        }

        for (int i = 0; i < tries; i++) {
            int xOffset = getRandomPos(-radius, radius);
            int zOffset = getRandomPos(-radius, radius);

            Location candidate = origin.clone().add(xOffset, 0, zOffset);

            int highestY = candidate.getWorld().getHighestBlockYAt(candidate);
            candidate.setY(highestY + 1);

            if (isSafeLocation(candidate)) {
                return candidate;
            }
        }

        return null;
    }
    private int getRandomPos(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private boolean isSafeLocation(Location location) {
        Material material = location.getBlock().getType();

        if (material.equals(Material.AIR)) {
            material = location.getBlock().getRelative(BlockFace.DOWN).getType();
            // DEBUG
            plugin.debugInfo("RelativeBlock(DOWN) is " + material.name());
        }

        if (plugin.getPluginConfig().getTpSetting_safePosition_dangerousBlocks().contains(material)) {
            return false;
        }

        return true;
    }


    /*  ----------------  */
    /*  Cooldown Manager  */
    /*  ----------------  */
    private final HashMap<UUID, HashMap<String, Integer>> coolDowns = new HashMap<>();

    public void addCooldown(UUID uuid, String cooldownName, long cooldown) {
        if (!coolDowns.containsKey(uuid)) {
            coolDowns.put(uuid, new HashMap<>());
        }
        coolDowns.get(uuid).put(cooldownName, (int) (System.currentTimeMillis() / 1000 + cooldown));
    }

    public boolean isOnCooldown(UUID uuid, String cooldownName) {
        if (!coolDowns.containsKey(uuid)) {
            return false;
        }
        if (!coolDowns.get(uuid).containsKey(cooldownName)) {
            return false;
        }

        return coolDowns.get(uuid).get(cooldownName) > System.currentTimeMillis() / 1000;
    }

    public int getCooldown(UUID uuid, String cooldownName) {
        if (!coolDowns.containsKey(uuid)) {
            return 0;
        }
        if (!coolDowns.get(uuid).containsKey(cooldownName)) {
            return 0;
        }

        return coolDowns.get(uuid).get(cooldownName) - (int) (System.currentTimeMillis() / 1000);
    }

    public void removeCooldown(UUID uuid, String cooldownName) {
        if (!coolDowns.containsKey(uuid)) {
            return;
        }
        if (!coolDowns.get(uuid).containsKey(cooldownName)) {
            return;
        }

        coolDowns.get(uuid).remove(cooldownName);
    }

    public void removeCooldowns(UUID uuid) {
        if (!coolDowns.containsKey(uuid)) {
            return;
        }

        coolDowns.remove(uuid);
    }

}
