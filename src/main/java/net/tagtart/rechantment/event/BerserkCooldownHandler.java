package net.tagtart.rechantment.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.effect.ModEffects;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BerserkCooldownHandler {

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        
        // Check if the expired effect is berserk
        if (event.getEffectInstance().getEffect().equals(ModEffects.BERSERK_EFFECT)) {
            // Apply 20 second cooldown
            MobEffectInstance cooldownEffect = new MobEffectInstance(
                    ModEffects.BERSERK_COOLDOWN_EFFECT,
                    20 * 20, // 20 seconds (20 ticks per second)
                    0,
                    false, // ambient
                    false, // visible particles
                    true   // show icon
            );
            entity.addEffect(cooldownEffect);
        }
    }
}

