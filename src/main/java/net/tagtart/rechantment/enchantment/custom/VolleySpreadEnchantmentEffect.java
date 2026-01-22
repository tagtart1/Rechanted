package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record VolleySpreadEnchantmentEffect() implements EnchantmentValueEffect {

    public static final MapCodec<VolleySpreadEnchantmentEffect> CODEC = MapCodec
            .unit(VolleySpreadEnchantmentEffect::new);


    @Override
    public float process(int enchantmentLevel, RandomSource randomSource, float originalValue) {
        return 10;
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }
}
