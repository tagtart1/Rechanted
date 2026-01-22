package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record VolleyCountEnchantmentEffect() implements EnchantmentValueEffect {

    public static final MapCodec<VolleyCountEnchantmentEffect> CODEC = MapCodec
            .unit(VolleyCountEnchantmentEffect::new);



    private static final double[][] CHANCES = {
            {0.50, 0.50, 0.20, 0.20}, // Level 1
            {0.60, 0.60, 0.25, 0.25},  // Level 2
            {0.70, 0.70, 0.30, 0.30} // Level 3
    };

    @Override
    public float process(int enchantmentLevel, RandomSource randomSource, float originalValue) {
        // For Volley, we modify the projectile count based on chance
        double[] odds = CHANCES[enchantmentLevel - 1];

       int extraArrowsToFire = 0;
        for (double odd : odds) {
            if (randomSource.nextDouble() < odd) {
                extraArrowsToFire++;
            } else {
                break;
            }
        }

       return extraArrowsToFire;
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }
}
