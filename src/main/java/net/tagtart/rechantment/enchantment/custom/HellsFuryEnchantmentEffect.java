package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.Rechantment;

public record HellsFuryEnchantmentEffect(float baseDamage, float damagePerLevel) implements EnchantmentEntityEffect {

    public static final MapCodec<HellsFuryEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("base_damage").forGetter(HellsFuryEnchantmentEffect::baseDamage),
                    Codec.FLOAT.fieldOf("damage_per_level").forGetter(HellsFuryEnchantmentEffect::damagePerLevel)
            ).apply(instance, HellsFuryEnchantmentEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {

        Rechantment.LOGGER.info("Hell's Fury triggered! Current dimension: {}, Is Nether: {}", 
            level.dimension().location(), level.dimension() == Level.NETHER);

        // Enchantment only works in the nether dimension
        if (level.dimension() != Level.NETHER) {
            return;
        }

        // Apply bonus damage to the target
        if (entity instanceof LivingEntity target) {
            Entity attacker = item.owner();
            float bonusDamage = damagePerLevel * enchantmentLevel + baseDamage;

            Rechantment.LOGGER.info("Hell's Fury applying {} damage (level {})", bonusDamage, enchantmentLevel);

            // Apply damage attributed to attacker (for looting)
            if (attacker instanceof Player player) {
                // Log attack strength (0.0 = no charge, 1.0 = fully charged)
                // TODO: use this to only apply when we are at 85% or higher for BLITZ, and every damage enchantment! remove this comment when implemented
                float attackStrength = player.getAttackStrengthScale(0.5F);
                Rechantment.LOGGER.info("Hell's Fury attack strength: {} (0.0=spam, 1.0=full charge)", attackStrength);
                
                // Temporarily disable invulnerability to apply bonus damage
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                target.hurt(level.damageSources().playerAttack(player), bonusDamage);
                target.invulnerableTime = invulnerableTime;
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
