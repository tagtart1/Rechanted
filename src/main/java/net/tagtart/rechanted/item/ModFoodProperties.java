package net.tagtart.rechanted.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.tagtart.rechanted.effect.ModEffects;
import net.tagtart.rechanted.effect.SatiatedEffect;

public class ModFoodProperties {
    public static final FoodProperties TASTY_GEM = new FoodProperties.Builder().nutrition(20).saturationModifier(1f)
            .effect(() -> new MobEffectInstance(
                    MobEffects.REGENERATION,
                    SatiatedEffect.DEFAULT_LENGTH_TICKS,
                    0,
                    false,
                    true,
                    true
            ), 1f)
            .effect(() -> new MobEffectInstance(
                    ModEffects.SATIATED_EFFECT,
                    SatiatedEffect.DEFAULT_LENGTH_TICKS,
                    0,
                    false,
                    true,
                    true
            ), 1f).alwaysEdible().build();
}
