package net.tagtart.rechanted.enchantment.custom;

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
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public record InquisitiveEnchantmentEffect() implements EnchantmentValueEffect {

    public static final MapCodec<InquisitiveEnchantmentEffect> CODEC = MapCodec
            .unit(InquisitiveEnchantmentEffect::new);

    private static final List<Float> InquisitiveMultipliers = Arrays.asList(
            1.50f,     // Level 1
            1.75f,
            2.00f,    // Level 2
            2.25f
    );


    @Override
    public float process(int enchantmentLevel, RandomSource random, float value) {
        return trueProcess(enchantmentLevel, random, value);
    }

    // shit workaround for needing to call same process code outside the effect
    public static float trueProcess(int enchantmentLevel, RandomSource random, float value) {
        float multi = InquisitiveMultipliers.get(enchantmentLevel - 1);

        return value * multi;
    }

    @Override
    public MapCodec<? extends EnchantmentValueEffect> codec() {
        return CODEC;
    }
}
