package dev.mizio.mcPlugins.ocTPA;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;

@Getter
public class PluginConfig {

    public static final String PERMS_CMD_TPA = "octpa.cmd.tpa";
    public static final String PERMS_CMD_TPACCEPT = "octpa.cmd.tpaccept";
    public static final String PERMS_CMD_TPDENY = "octpa.cmd.tpdeny";
    public static final String PERMS_CMD_TPCANCEL = "octpa.cmd.tpcancel";
    public static final String PERMS_CMD_TPHERE = "octpa.cmd.tphere";
    public static final String PERMS_CMD_TPRETURN = "octpa.cmd.tpreturn";
    public static final String PERMS_CMD_OCTPA = "octpa.cmd.octpa";
    public static final String PERMS_BYPASS_COOLDOWN = "octpa.bypass.cooldown";
    public static final String PERMS_BYPASS_RETURN_TIMEOUT = "octpa.bypass.return-timeout";

    @Getter(AccessLevel.NONE)
    private FileConfiguration config;

    private boolean pluginDebug;
    private boolean configValid = false;
    private String pluginPrefix;
    private String pluginMiniPrefix;

    // Section teleportation-settings
    private boolean tpSetting_cmdEnabled_tpreturn;
    private boolean tpSetting_cmdEnabled_tphere;
    private int tpSetting_times_timeout_acceptance;
    private int tpSetting_times_timeout_return;
    private int tpSetting_time_cooldown_after_accept;
    private int tpSetting_time_cooldown_cmd_tpa;
    private int tpSetting_time_cooldown_cmd_tpaHere;
//    private List<String> tpSetting_disabledWorlds;
    private boolean tpSetting_cost_enabled;
    private String tpSetting_cost_currency;
    private double tpSetting_cost_amount;
    private double tpSetting_cost_return_amount;

    // Section teleportation-effects
    private boolean tpEffects_sound_enabled;
    private String tpEffects_sound_name;
    private double tpEffects_sound_volume;
    private double tpEffects_sound_pitch;
    private boolean tpEffects_particle_enabled;
    private String tpEffects_particle_name;
    private boolean tpEffects_potionEffect_enabled;
    private String tpEffects_potionEffect_name;
    private int tpEffects_potionEffect_duration;
    private int tpEffects_potionEffect_amplifier;
    private boolean tpEffects_potionEffect_ambient;
    private boolean tpEffects_potionEffect_particle;
    private boolean tpEffects_potionEffect_icon;

    // Section translation
    @Getter(AccessLevel.NONE)
    private HashMap<String, String> translationsMap = new HashMap<>();

    public PluginConfig() {
        loadFile();
    }

    private void loadFile() {
        File dataFolder = MainOcTPA.instance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        MainOcTPA.instance().saveDefaultConfig();
        config = MainOcTPA.instance().getConfig();

        loadData();
    }

    private void loadData() {
        pluginDebug = config.getBoolean("plugin-debug", false);
        if (pluginDebug) {
            MainOcTPA.instance().getLogger().info("[DEBUG] =!= Tryb debugowania uruchomiony! =!=");
        }
        pluginPrefix = config.getString("plugin-prefix", "<gold>[<dark_green>oc<white>TPA<gold>]");
        pluginMiniPrefix = config.getString("plugin-mini-prefix", "<gold>[<dark_green>TP</dark_green>]");

        tpSetting_cmdEnabled_tpreturn = config.getBoolean("teleportation-settings.cmd-enabled.tpreturn", true);
        tpSetting_cmdEnabled_tphere = config.getBoolean("teleportation-settings.cmd-enabled.tphere", true);

        tpSetting_times_timeout_acceptance = config.getInt("teleportation-settings.times.timeout.acceptance", 60);
        tpSetting_times_timeout_return = config.getInt("teleportation-settings.times.timeout.return", 60);
        tpSetting_time_cooldown_after_accept = config.getInt("teleportation-settings.times.cooldown.after-accept", 0);
        tpSetting_time_cooldown_cmd_tpa = config.getInt("teleportation-settings.times.cooldown.reuse-cmd-tpa", 60);
        tpSetting_time_cooldown_cmd_tpaHere = config.getInt("teleportation-settings.times.cooldown.reuse-cmd-tpaHere", 60);

//        tpSetting_disabledWorlds = config.getStringList("teleportation-settings.disabled-worlds");

        tpSetting_cost_enabled = config.getBoolean("teleportation-settings.cost.enabled", false);
        tpSetting_cost_currency = config.getString("teleportation-settings.cost.currency");
        tpSetting_cost_amount = config.getDouble("teleportation-settings.cost.amount");
        tpSetting_cost_return_amount = config.getDouble("teleportation-settings.cost.return-amount");

        tpEffects_sound_enabled = config.getBoolean("teleportation-effects.sound.enabled", true);
        tpEffects_sound_name = config.getString("teleportation-effects.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        tpEffects_sound_volume = config.getDouble("teleportation-effects.sound.volume", 3.0);
        tpEffects_sound_pitch = config.getDouble("teleportation-effects.sound.pitch", 0.5);

        tpEffects_particle_enabled = config.getBoolean("teleportation-effects.particle.enabled", false);
        tpEffects_particle_name = config.getString("teleportation-effects.particle.name", "EXPLOSION_NORMAL");

        tpEffects_potionEffect_enabled = config.getBoolean("teleportation-effects.potionEffect.enabled", false);
        tpEffects_potionEffect_name = config.getString("teleportation-effects.potionEffect.name", "BLINDNESS");
        tpEffects_potionEffect_duration = config.getInt("teleportation-effects.potionEffect.duration", 40);
        tpEffects_potionEffect_amplifier = config.getInt("teleportation-effects.potionEffect.amplifier", 1);
        tpEffects_potionEffect_ambient = config.getBoolean("teleportation-effects.potionEffect.ambient", false);
        tpEffects_potionEffect_particle = config.getBoolean("teleportation-effects.potionEffect.particle", false);
        tpEffects_potionEffect_icon = config.getBoolean("teleportation-effects.potionEffect.icon", false);

        ConfigurationSection section = config.getConfigurationSection("translation");
        if (section == null) {
            MainOcTPA.instance().getLogger().severe("=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=");
            MainOcTPA.instance().getLogger().severe("Missing configuration section for translation!");
            MainOcTPA.instance().getLogger().severe("=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=");

            this.configValid = false;
            return;
        } else {
            if (isPluginDebug()) {
                MainOcTPA.instance().getLogger().info("[DEBUG] =!=!=!=!= Załadowane tłumaczenia =!=!=!=!=");
            }
            for (String key : section.getKeys(false)) {
                String value = section.getString(key, "");
                if (isPluginDebug()) {
                    MainOcTPA.instance().getLogger().info(" - " + key + " = " + value);
                }
                translationsMap.put(key, value);
            }
            if (isPluginDebug()) {
                MainOcTPA.instance().getLogger().info("[DEBUG] =!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=!=");
            }
        }

        this.configValid = true;
    }

    public String getTranslations(String translationName) {
        if (!translationsMap.containsKey(translationName)) {
            MainOcTPA.instance().getLogger().warning("!! BRAKUJE TŁUMACZENIA!! Sprawdź klucz: " + translationName);
        }
        return translationsMap.getOrDefault(
                translationName,
                "<prefix><gradient:dark_red:red><bold>UWAGA!</bold> W configu brakuje tłumaczenia:</gradient> <gold>" + translationName
        );
    }
}
