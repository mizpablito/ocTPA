package dev.mizio.mcPlugins.ocTPA.services;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TeleportEffectService {
    private final MainOcTPA plugin;

    public TeleportEffectService() {
        this.plugin = MainOcTPA.instance();
    }

    public void run(Player player) {
        var cfg = plugin.getPluginConfig();

        if (cfg.isTpEffects_sound_enabled()) {
            playSound(player,
                    cfg.getTpEffects_sound_name(),
                    (float) cfg.getTpEffects_sound_volume(),
                    (float) cfg.getTpEffects_sound_pitch());
        }

        if (cfg.isTpEffects_particle_enabled()) {
            spawnParticle(player,
                    cfg.getTpEffects_particle_name(),
                    30, 0.5, 0.5, 0.5, 0.01);
        }

        if (cfg.isTpEffects_potionEffect_enabled()) {
            applyPotionEffect(player,
                    cfg.getTpEffects_potionEffect_name(),
                    cfg.getTpEffects_potionEffect_duration(),
                    cfg.getTpEffects_potionEffect_amplifier(),
                    cfg.isTpEffects_potionEffect_ambient(),
                    cfg.isTpEffects_potionEffect_particle(),
                    cfg.isTpEffects_potionEffect_icon());
        }
    }

    public void playSound(Player player, String soundKey, float volume, float pitch) {
        try {
            Sound sound = Registry.SOUNDS.getOrThrow(NamespacedKey.minecraft(soundKey.toLowerCase()));
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[TeleportEffectService] Nieprawidłowa nazwa dźwięku: " + soundKey);
        }
    }

    public void spawnParticle(Player player, String particleName,
                              int count, double offsetX, double offsetY, double offsetZ, double speed) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            player.getWorld().spawnParticle(
                    particle,
                    player.getLocation(),
                    count,
                    offsetX, offsetY, offsetZ,
                    speed
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[TeleportEffectService] Nieprawidłowa nazwa cząsteczki: " + particleName);
        }
    }

    public void applyPotionEffect(Player player, String effectName,
                                  int duration, int amplifier,
                                  boolean ambient, boolean particles, boolean icon) {
        PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
        if (type != null) {
            PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
            player.addPotionEffect(effect);
        } else {
            plugin.getLogger().warning("[TeleportEffectService] Nieprawidłowa nazwa efektu mikstury: " + effectName);
        }
    }
}
