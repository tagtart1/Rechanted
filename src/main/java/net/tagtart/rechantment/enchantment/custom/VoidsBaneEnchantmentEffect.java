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

public record VoidsBaneEnchantmentEffect(float baseDamage, float damagePerLevel) implements EnchantmentEntityEffect {

    public static final MapCodec<VoidsBaneEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("base_damage").forGetter(VoidsBaneEnchantmentEffect::baseDamage),
                    Codec.FLOAT.fieldOf("damage_per_level").forGetter(VoidsBaneEnchantmentEffect::damagePerLevel)
            ).apply(instance, VoidsBaneEnchantmentEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {

        // Enchantment only works in the End dimension
        if (level.dimension() != Level.END) {
            return;
        }

        // Apply bonus damage to the target
        if (entity instanceof LivingEntity target) {
            Entity attacker = item.owner();
            float bonusDamage = damagePerLevel * enchantmentLevel + baseDamage;

            // Apply damage attributed to attacker (for looting)
            if (attacker instanceof Player player) {
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
