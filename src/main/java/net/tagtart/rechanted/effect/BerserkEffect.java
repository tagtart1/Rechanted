package net.tagtart.rechanted.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BerserkEffect extends MobEffect {

    protected BerserkEffect(MobEffectCategory category, int color, ParticleOptions particle) {
        super(category, color, particle);
    }

    // This effect is now purely cosmetic for UX purposes (particles, visual indicator)
    // No gameplay logic here - just for showing the player they have berserk active
    // The cooldown effect is applied via event handler when this effect expires
}
