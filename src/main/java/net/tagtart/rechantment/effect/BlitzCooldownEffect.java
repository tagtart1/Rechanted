package net.tagtart.rechantment.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BlitzCooldownEffect extends MobEffect {

    protected BlitzCooldownEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // This effect is purely for state tracking - no particles, no logic
    // It prevents blitz from being reactivated while on cooldown
}

