package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    private static final float ATTACK_STRENGTH_THRESHOLD = 0.85f; // 85% charge required
    private static final long COMBO_WINDOW_MILLIS = 1000; // 1 second between hits
    private static final long ACTIVE_COMBO_WINDOW_MILLIS = 330; // 0.33 seconds when Blitz is active
    private static final int COMBO_REQUIRED = 5; // Hits needed to activate
    private static final int BLITZ_DURATION = 10 * 20; // 10 seconds in ticks

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

        // Determine combo window based on whether Blitz is active
        long comboWindow = hasBlitzActive ? ACTIVE_COMBO_WINDOW_MILLIS : COMBO_WINDOW_MILLIS;

        // Check if within combo window
        Duration timeSinceLastHit = Duration.between(lastAttack, now);
        boolean withinComboWindow = timeSinceLastHit.toMillis() <= comboWindow;

        // Update combo count
        if (!hasBlitzActive) {
            // Building combo (not active yet)
            if (withinComboWindow && currentCombo < COMBO_REQUIRED) {
                currentCombo++;
                player.setData(ModAttachments.BLITZ_COMBO, currentCombo);
                
                // Show combo progress
                player.displayClientMessage(
                    Component.literal("Blitz Combo: " + currentCombo + "/" + COMBO_REQUIRED), 
                    true
                );

                Rechantment.LOGGER.info("Blitz combo for {}: {}/{}", 
                        player.getName().getString(), currentCombo, COMBO_REQUIRED);
            } else if (!withinComboWindow) {
                // Reset combo if too slow
                currentCombo = 1;
                player.setData(ModAttachments.BLITZ_COMBO, currentCombo);
                
                player.displayClientMessage(
                    Component.literal("Blitz Combo: " + currentCombo + "/" + COMBO_REQUIRED), 
                    true
                );
            }

            // Activate Blitz when combo reaches required hits
            if (currentCombo >= COMBO_REQUIRED) {
                // Check if on cooldown
                if (player.hasEffect(ModEffects.BLITZ_COOLDOWN_EFFECT)) {
                    player.displayClientMessage(
                        Component.literal("Blitz is on cooldown!"), 
                        true
                    );
                    player.setData(ModAttachments.LAST_BLITZ_ATTACK_AT, now);
                    return;
                }

                // Activate Blitz
                MobEffectInstance blitzEffect = new MobEffectInstance(
                        ModEffects.BLITZ_EFFECT,
                        BLITZ_DURATION,
                        0, // Start at stack 0 (will display as Blitz I)
                        false, // ambient
                        true,  // visible particles
                        true   // show icon
                );
                player.addEffect(blitzEffect);

                // Reset combo
                player.setData(ModAttachments.BLITZ_COMBO, 0);

                // Display activation message with stack level
                player.displayClientMessage(Component.literal("Blitz! x1"), true);

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

            // Stack level is amplifier + 1 (amplifier 0 = stack 1)
            int stackLevel = blitzEffect.getAmplifier() + 1;

            // Calculate damage: baseDamage = 3 + (stack - 1) + (enchantLevel - 1) * 0.5
            float baseDamage = 3.0f + (stackLevel - 1) + (enchantmentLevel - 1) * 0.5f;

            Rechantment.LOGGER.info("Blitz damage: level={}, stack={}, damage={}", 
                    enchantmentLevel, stackLevel, baseDamage);

            // Temporarily disable invulnerability to apply bonus damage
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), baseDamage);
            target.invulnerableTime = invulnerableTime;

            // Increment stack level (max 3 stacks)
            if (stackLevel < 3) {
                int newAmplifier = stackLevel; // Current stack becomes new amplifier
                int newStackLevel = newAmplifier + 1; // Display stack level
                
                MobEffectInstance newBlitzEffect = new MobEffectInstance(
                        ModEffects.BLITZ_EFFECT,
                        blitzEffect.getDuration(), // Keep remaining duration
                        newAmplifier,
                        false, // ambient
                        true,  // visible particles
                        true   // show icon
                );
                player.addEffect(newBlitzEffect); // This will replace the old effect

                // Display stack increase message
                player.displayClientMessage(Component.literal("Blitz! x" + newStackLevel), true);

                Rechantment.LOGGER.info("Blitz stack increased to {}", newStackLevel);
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}

