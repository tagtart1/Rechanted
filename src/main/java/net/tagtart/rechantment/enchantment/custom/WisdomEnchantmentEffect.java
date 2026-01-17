package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public record WisdomEnchantmentEffect() implements EnchantmentValueEffect {

    public static final MapCodec<WisdomEnchantmentEffect> CODEC = MapCodec
            .unit(WisdomEnchantmentEffect::new);

    private static final List<Float> WisdomMultipliers = Arrays.asList(
            2f,     // Level 1
            2.5f    // Level 2
    );


    @Override
    public float process(int enchantmentLevel, RandomSource random, float value) {
            float multi = WisdomMultipliers.get(enchantmentLevel - 1);

            return value * multi;
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }
}
