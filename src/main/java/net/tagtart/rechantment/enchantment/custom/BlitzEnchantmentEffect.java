package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.attachments.ModAttachments;
import net.tagtart.rechantment.effect.ModEffects;

import java.time.Duration;
import java.time.Instant;
// TODO: understand and opitmize this code, cleanup comments and add logs!
public record BlitzEnchantmentEffect() implements EnchantmentEntityEffect {

    private static final float ATTACK_STRENGTH_THRESHOLD = 0.50f; // 50% charge required
    private static final long FIRST_STACK_COMBO_WINDOW_MILLIS = 1250; // 1.25 seconds between hits for first stack
    private static final long ACTIVE_COMBO_WINDOW_MILLIS = 1500; // 1.5 seconds when Blitz is active
    private static final int COMBO_REQUIRED = 6; // Hits needed to activate
    private static final int BLITZ_I_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int BLITZ_II_DURATION = 12 * 20; // 12 seconds in ticks
    private static final int BLITZ_III_DURATION = 15 * 20; // 15 seconds in ticks
    private static final int MAX_BLITZ_STACKS = 3;

    public static final MapCodec<BlitzEnchantmentEffect> CODEC = MapCodec
            .unit(BlitzEnchantmentEffect::new);

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos) {
        Entity attacker = item.owner();

        if (!(attacker instanceof ServerPlayer player)) return;

        // Check attack strength (85% or higher)
        float attackStrength = player.getAttackStrengthScale(0.5f);
        if (attackStrength < ATTACK_STRENGTH_THRESHOLD) return;

        // Get current time and combo data
        Instant now = Instant.now();
        Instant lastAttack = player.getData(ModAttachments.LAST_BLITZ_ATTACK_AT);
        int currentCombo = player.getData(ModAttachments.BLITZ_COMBO);
        boolean hasBlitzActive = player.hasEffect(ModEffects.BLITZ_EFFECT);

        // First stack has its own timing window; active Blitz keeps its existing combo timing.
        long comboWindow = hasBlitzActive ? ACTIVE_COMBO_WINDOW_MILLIS : FIRST_STACK_COMBO_WINDOW_MILLIS;

        // Check if within combo window
        Duration timeSinceLastHit = Duration.between(lastAttack, now);
        boolean withinComboWindow = timeSinceLastHit.toMillis() <= comboWindow;

        // Check if on cooldown
        if (player.hasEffect(ModEffects.BLITZ_COOLDOWN_EFFECT)) {
            player.setData(ModAttachments.LAST_BLITZ_ATTACK_AT, now);
            return;
        }

        // Update combo count

        // Building combo (not active yet)
        if (withinComboWindow && currentCombo <= COMBO_REQUIRED) {
            currentCombo++;
            player.setData(ModAttachments.BLITZ_COMBO, currentCombo);


            Rechantment.LOGGER.info("Blitz combo for {}: {}/{}",
                    player.getName().getString(), currentCombo, COMBO_REQUIRED);
        } else if (!withinComboWindow) {
            // Reset combo if too slow
            currentCombo = 1;
            player.setData(ModAttachments.BLITZ_COMBO, currentCombo);

        }

        // Proc Blitz stack when combo reaches required hits
        if (currentCombo >= COMBO_REQUIRED) {

            MobEffectInstance currentBlitz = player.getEffect(ModEffects.BLITZ_EFFECT);
            int amplifierToApply = currentBlitz != null ? currentBlitz.getAmplifier() + 1: 0;

            if (amplifierToApply < MAX_BLITZ_STACKS) {
                // Activate Blitz
                MobEffectInstance blitzEffect = new MobEffectInstance(
                        ModEffects.BLITZ_EFFECT,
                        getBlitzDurationTicksForAmplifier(amplifierToApply),
                        amplifierToApply, // will display as Blitz I, II, III
                        false, // ambient
                        false, // visible particles
                        true   // show icon
                );
                player.addEffect(blitzEffect);
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ENDER_DRAGON_HURT,
                        SoundSource.PLAYERS,
                        1F,
                        1.15F
                );

                // Reset combo
                player.setData(ModAttachments.BLITZ_COMBO, 0);

                // Display activation message with stack level
                player.displayClientMessage(Component.literal(String.format("Blitz! x%d", amplifierToApply + 1)).withStyle(ChatFormatting.GREEN), true);

                Rechantment.LOGGER.info("Blitz activated for {}", player.getName().getString());
            }
        }


        // Update last attack time
        player.setData(ModAttachments.LAST_BLITZ_ATTACK_AT, now);

        // Apply damage if Blitz is active
        if (hasBlitzActive && entity instanceof LivingEntity target) {
            // Get current Blitz effect to determine stack level
            MobEffectInstance blitzEffect = player.getEffect(ModEffects.BLITZ_EFFECT);
            if (blitzEffect == null) return;

            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    target.getX(),
                    target.getY(0.55),
                    target.getZ(),
                    12,
                    target.getBbWidth() * 0.35,
                    target.getBbHeight() * 0.45,
                    target.getBbWidth() * 0.35,
                    0.05
            );

            // Stack level is amplifier + 1 (amplifier 0 = stack 1)
            int stackLevel = blitzEffect.getAmplifier() + 1;

            // Level 4 has a bigger boost from level 3
            float baseDamage = enchantmentLevel == 4 ? stackLevel + 4 : stackLevel + 2 + .5f * (enchantmentLevel - 1);

            Rechantment.LOGGER.info("Blitz damage: level={}, stack={}, damage={}", 
                    enchantmentLevel, stackLevel, baseDamage);

            // Temporarily disable invulnerability to apply bonus damage
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), baseDamage);
            target.invulnerableTime = invulnerableTime;
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }

    private static int getBlitzDurationTicksForAmplifier(int amplifier) {
        return switch (amplifier) {
            case 0 -> BLITZ_I_DURATION;
            case 1 -> BLITZ_II_DURATION;
            default -> BLITZ_III_DURATION;
        };
    }
}

