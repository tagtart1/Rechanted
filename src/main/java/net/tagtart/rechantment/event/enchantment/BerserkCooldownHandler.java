package net.tagtart.rechantment.event.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.effect.ModEffects;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BerserkCooldownHandler {

    private static final int BERSERK_COOLDOWN_DURATION = 10 * 20; // 10 seconds in ticks

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        
        // Check if the expired effect is berserk
        if (event.getEffectInstance().getEffect().equals(ModEffects.BERSERK_EFFECT)) {
            // Apply cooldown
            MobEffectInstance cooldownEffect = new MobEffectInstance(
                    ModEffects.BERSERK_COOLDOWN_EFFECT,
                    BERSERK_COOLDOWN_DURATION,
                    0,
                    false, // ambient
                    false, // visible particles
                    true   // show icon
            );
            entity.addEffect(cooldownEffect);
        }
    }
}

