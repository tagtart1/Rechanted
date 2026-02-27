package net.tagtart.rechanted.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record CourageEnchantmentEffect() implements EnchantmentValueEffect {

    public static final MapCodec<CourageEnchantmentEffect> CODEC = MapCodec
            .unit(CourageEnchantmentEffect::new);

    private static final float DURABILITY_SAVE_CHANCE = 0.33f;

    @Override
    public float process(int enchantmentLevel, RandomSource random, float value) {

        if (random.nextFloat() < DURABILITY_SAVE_CHANCE) {
            return 0f; // No durability damage
        }
        return value; // Normal durability damage
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }
}

