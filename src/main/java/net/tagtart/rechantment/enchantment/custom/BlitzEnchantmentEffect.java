package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record BlitzEnchantmentEffect() implements EnchantmentEntityEffect {

    public static final MapCodec<BlitzEnchantmentEffect> CODEC = MapCodec
            .unit(BlitzEnchantmentEffect::new);

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos) {
        // TODO: Implement blitz logic
        // This enchantment will be implemented later
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}

