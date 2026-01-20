package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.effect.ModEffects;

import java.util.Arrays;
import java.util.List;

public record BerserkEnchantmentEffect() implements EnchantmentEntityEffect {

    private static final List<Float> DAMAGER_PER_LEVEL = Arrays.asList(
            4.00f,    // Level 1
            4.50f,    // Level 2
            5.00f,    // Level 3
            6.00f     // Level 4
    );

    public static final MapCodec<BerserkEnchantmentEffect> CODEC = MapCodec
            .unit(BerserkEnchantmentEffect::new);

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos) {

        Entity attacker = item.owner();

        if (attacker instanceof Player player) {

            // Only apply damage if player has berserk effect active (no cooldown check needed here)
            if (!player.hasEffect(ModEffects.BERSERK_EFFECT)) { return; }

            // Can't apply damage if we're on cooldown
            if (player.hasEffect(ModEffects.BERSERK_COOLDOWN_EFFECT)) { return; }

            // Apply bonus damage
            float damageToApply = DAMAGER_PER_LEVEL.get(enchantmentLevel - 1);

            if (entity instanceof LivingEntity target) {
                // Temporarily disable invulnerability to apply bonus damage
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                target.hurt(level.damageSources().playerAttack(player), damageToApply);
                target.invulnerableTime = invulnerableTime;
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}

