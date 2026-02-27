package net.tagtart.rechanted.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BlitzEffect extends MobEffect {

    protected BlitzEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // This effect is purely cosmetic for UX purposes (visual indicator)
    // No gameplay logic here - just for showing the player they have blitz active
    // No particles are rendered for this effect
    // Can have multiple amplifiers based on enchantment level
}

