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

public record HellsFuryEnchantmentEffect(float baseDamage, float damagePerLevel) implements EnchantmentEntityEffect {

    public static final MapCodec<HellsFuryEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("base_damage").forGetter(HellsFuryEnchantmentEffect::baseDamage),
                    Codec.FLOAT.fieldOf("damage_per_level").forGetter(HellsFuryEnchantmentEffect::damagePerLevel)
            ).apply(instance, HellsFuryEnchantmentEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {

        // Enchantment only works in the nether dimension
        if (level.dimension() != Level.NETHER) {
            return;
        }

        // Apply bonus damage to the target
        if (entity instanceof LivingEntity target) {
            Entity attacker = item.owner();
            float bonusDamage = damagePerLevel * enchantmentLevel + baseDamage;

            // Apply damage attributed to attacker (for looting)
            if (attacker instanceof Player player) {
                target.hurt(level.damageSources().playerAttack(player), bonusDamage);
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
