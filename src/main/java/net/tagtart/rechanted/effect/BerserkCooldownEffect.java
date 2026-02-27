package net.tagtart.rechanted.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BerserkCooldownEffect extends MobEffect {

    protected BerserkCooldownEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // This effect is purely for state tracking - no particles, no logic
    // It prevents berserk from being reactivated while on cooldown
}

