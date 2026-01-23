package net.tagtart.rechantment.event.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.attachments.ModAttachments;
import net.tagtart.rechantment.effect.ModEffects;

import java.time.Instant;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BlitzCooldownHandler {

    private static final int BLITZ_COOLDOWN_DURATION = 10 * 20; // 10 seconds in ticks

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        
        // Check if the expired effect is Blitz
        if (event.getEffectInstance().getEffect().equals(ModEffects.BLITZ_EFFECT)) {
            // Apply cooldown
            MobEffectInstance cooldownEffect = new MobEffectInstance(
                    ModEffects.BLITZ_COOLDOWN_EFFECT,
                    BLITZ_COOLDOWN_DURATION,
                    0,
                    false, // ambient
                    false, // visible particles
                    true   // show icon
            );
            entity.addEffect(cooldownEffect);
            
            // Reset combo data if this is a player
            if (entity instanceof ServerPlayer player) {
                player.setData(ModAttachments.BLITZ_COMBO, 0);
                player.setData(ModAttachments.LAST_BLITZ_ATTACK_AT, Instant.EPOCH);
                
                Rechantment.LOGGER.info("Blitz cooldown applied and combo reset for {}", 
                        player.getName().getString());
            } else {
                Rechantment.LOGGER.info("Blitz cooldown applied to {}", entity.getName().getString());
            }
        }
    }


}

