package dev.mizio.mcPlugins.ocTPA;

import dev.mizio.mcPlugins.ocTPA.commands.*;
import dev.mizio.mcPlugins.ocTPA.commands.handlers.PendingRequestsSuggester;
import dev.mizio.mcPlugins.ocTPA.commands.handlers.PluginInvalidUsageCmdHandler;
import dev.mizio.mcPlugins.ocTPA.listenings.PlayerDamageListener;
import dev.mizio.mcPlugins.ocTPA.listenings.PlayerMoveListener;
import dev.mizio.mcPlugins.ocTPA.listenings.PlayerQuitListener;
import dev.mizio.mcPlugins.ocTPA.services.EconomyService;
import dev.mizio.mcPlugins.ocTPA.services.TeleportEffectService;
import dev.mizio.mcPlugins.ocTPA.services.TeleportationService;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.argument.ArgumentKey;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.time.DurationParser;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MainOcTPA extends JavaPlugin {

    @Getter @Accessors(fluent = true)
    private static MainOcTPA instance;
    @Getter @Accessors(fluent = true)
    private TeleportationService teleportationService;
    @Getter @Accessors(fluent = true)
    private TeleportEffectService teleportEffectService;
    @Getter @Accessors(fluent = true)
    private EconomyService economyService;

    @Getter
    private PluginConfig pluginConfig;

    private LiteCommands<CommandSender> liteCommands;


    public MainOcTPA() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        pluginConfig = new PluginConfig();
        if (!pluginConfig.isConfigValid()) {
            getLogger().severe("=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=");
            getLogger().severe("Configuration not loaded correctly! Plugin disabled!");
            getLogger().severe("=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerServices();
        registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        unregisterServices();
        unregisterCommands();
        pluginConfig = null;
    }

    public void debugInfo(String msg) {
        if (pluginConfig.isPluginDebug()) {
            this.getLogger().info("[DEBUG] " + msg);
        }
    }

    private void registerIf(List<Object> commands, boolean condition, Supplier<Object> supplier) {
        if (condition) {
            commands.add(supplier.get());
        }
    }

    private void registerCommands() {
        List<Object> commands = new ArrayList<>();
        commands.add(new TpaCommand());

        registerIf(commands, pluginConfig.isTpSetting_cmdAliases_tpaccept(), TpacceptCommand::new);
        registerIf(commands, pluginConfig.isTpSetting_cmdAliases_tpcancel(), TpcancelCommand::new);
        registerIf(commands, pluginConfig.isTpSetting_cmdAliases_tpdeny(), TpdenyCommand::new);
        registerIf(commands, pluginConfig.isTpSetting_cmdAliases_tphere(), TphereCommand::new);
        registerIf(commands, pluginConfig.isTpSetting_cmdAliases_tpreturn(), TpreturnCommand::new);


        this.liteCommands = LiteBukkitFactory.builder("octpa", this)
                .commands(commands.toArray(new Object[0]))
                .argumentSuggester(Player.class, ArgumentKey.of("od_kogo_prośba"), new PendingRequestsSuggester())
                .invalidUsage(new PluginInvalidUsageCmdHandler())
                .extension(new LiteAdventureExtension<>(), config -> config
                        .miniMessage(true)
                        .legacyColor(true)
                        .colorizeArgument(true)
                        .serializer(MiniMessage.builder()
                                .tags(TagResolver.builder()
                                        .resolver(StandardTags.defaults()).build())
                                .editTags(b -> b.tag("prefix", StringUtil::prefixTag))
                                .editTags(b -> b.tag("miniprefix", StringUtil::miniPrefixTag))
                                .build())
                )
                .message(LiteBukkitMessages.PLAYER_ONLY, pluginConfig.getTranslations("player-only"))
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, player -> String.format(pluginConfig.getTranslations("player-not-found"), player))
                .message(LiteBukkitMessages.OFFLINE_PLAYER_NOT_FOUND, player -> String.format(pluginConfig.getTranslations("offlinePlayer-not-found"), player))
                .message(LiteBukkitMessages.NAMESPACED_KEY_INVALID, pluginConfig.getTranslations("namespaced-key-invalid"))
                .message(LiteBukkitMessages.CONSOLE_ONLY, pluginConfig.getTranslations("console-only"))
                .message(LiteBukkitMessages.LOCATION_INVALID_FORMAT, input -> String.format(pluginConfig.getTranslations("location-invalid-format"), input))
                .message(LiteBukkitMessages.WORLD_NOT_EXIST, input -> String.format(pluginConfig.getTranslations("world-not-exist"),input))
                .message(LiteBukkitMessages.MISSING_PERMISSIONS, perms -> String.format(pluginConfig.getTranslations("missing-permission"), perms.asJoinedText()))
                .message(LiteBukkitMessages.INVALID_NUMBER, input -> String.format(pluginConfig.getTranslations("invalid-number"), input))
                .message(LiteBukkitMessages.INVALID_USAGE, String.format(pluginConfig.getTranslations("invalid-usage")))
                .message(LiteBukkitMessages.INSTANT_INVALID_FORMAT, input -> String.format(pluginConfig.getTranslations("instant-invalid-format"), input))
                .message(LiteBukkitMessages.COMMAND_COOLDOWN, state -> String.format(pluginConfig.getTranslations("command-cooldown"), DurationParser.DATE_TIME_UNITS.format(state.getRemainingDuration())))
                .message(LiteBukkitMessages.UUID_INVALID_FORMAT, input -> String.format(pluginConfig.getTranslations("uuid-invalid-format"), input))
                .build();
    }

    private void unregisterCommands() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
            this.liteCommands = null;
        }
    }

    private void registerServices() {
        economyService = new EconomyService();
        teleportEffectService = new TeleportEffectService();
        teleportationService = new TeleportationService();
    }

    private void unregisterServices() {
        if (teleportationService != null) {
            teleportationService.closeService();
            teleportationService = null;
        }
        if (teleportEffectService != null) {
            teleportEffectService = null;
        }
        if (economyService != null) {
            economyService.closeService();
            economyService = null;
        }
    }
}
