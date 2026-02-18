package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public record ThunderStrikeEnchantmentEffect() implements EnchantmentEntityEffect {

    public static final MapCodec<ThunderStrikeEnchantmentEffect> CODEC = MapCodec
            .unit(ThunderStrikeEnchantmentEffect::new);

    private static final float LIGHTNING_DAMAGE = 5.0f;
    private static final float LIGHTNING_RADIUS = 3.0f;
    private static final float LIGHTNING_KNOCKBACK = 1.10f;
    private static final float LIGHTNING_Y_KNOCKBACK = 0.3f;
    // Maps enchantment level to a success rate of spawning lightning
    private static final List<Float> SUCCESS_RATES = Arrays.asList(
            0.10f,    // Level 1
            1.00f,    // Level 2
            0.17f,    // Level 3
            0.20f     // Level 4
    );

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos) {
        // Check if lightning strike is successful based on level
        if (!isSuccess(enchantmentLevel)) {
            return;
        }

        // Spawn visual lightning bolt at target entity
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);

        // Define area of effect around the target
        AABB area = new AABB(
                entity.getX() - LIGHTNING_RADIUS, entity.getY() - LIGHTNING_RADIUS, entity.getZ() - LIGHTNING_RADIUS,
                entity.getX() + LIGHTNING_RADIUS, entity.getY() + LIGHTNING_RADIUS, entity.getZ() + LIGHTNING_RADIUS
        );

        // Get the attacker (user of the enchanted item)
        Entity attacker = item.owner();

        // Apply lightning damage to the directly hit target first (base hit + lightning can stack).
        // Direct hit bypasses friendly/self/mount checks by design.
        if (entity instanceof LivingEntity directTarget) {
            applyLightningDamage(level, lightningBolt, attacker, directTarget);
        }

        // Damage and knockback all entities in the area
        level.getEntities(entity, area, e -> e instanceof LivingEntity).forEach(target -> {
            LivingEntity livingTarget = (LivingEntity) target;
            if (shouldSkipTarget(livingTarget, attacker)) {
                return;
            }
            applyLightningDamage(level, lightningBolt, attacker, livingTarget);

            // Calculate knockback direction from center of lightning strike
            double d0 = livingTarget.getX() - entity.getX();
            double d1 = livingTarget.getZ() - entity.getZ();
            Vec2 knockbackDirection = new Vec2((float) d0, (float) d1);
            knockbackDirection = knockbackDirection.normalized();
            knockbackDirection = knockbackDirection.scale(LIGHTNING_KNOCKBACK);

            // Apply knockback
            if (livingTarget.isPushable()) {
                livingTarget.push(knockbackDirection.x, LIGHTNING_Y_KNOCKBACK, knockbackDirection.y);
            }
        });
    }

    private boolean shouldSkipTarget(LivingEntity livingTarget, Entity attacker) {
        // Don't damage the attacker
        if (attacker != null && livingTarget == attacker) {
            return true;
        }

        // Don't damage the attacker's mount (horse they're riding)
        if (attacker != null && attacker.getVehicle() != null && livingTarget == attacker.getVehicle()) {
            return true;
        }

        // Don't damage the attacker's tamed animals (dogs, cats, parrots, etc.)
        if (attacker != null && livingTarget instanceof TamableAnimal tamable) {
            return tamable.isTame() && tamable.getOwner() == attacker;
        }

        return false;
    }

    private void applyLightningDamage(ServerLevel level, LightningBolt lightningBolt, Entity attacker, LivingEntity livingTarget) {
        // Temporarily disable invulnerability to apply lightning bonus damage.
        int invulnerableTime = livingTarget.invulnerableTime;
        livingTarget.invulnerableTime = 0;
        if (attacker != null) {
            livingTarget.hurt(level.damageSources().indirectMagic(lightningBolt, attacker), LIGHTNING_DAMAGE);
        } else {
            livingTarget.hurt(level.damageSources().lightningBolt(), LIGHTNING_DAMAGE);
        }
        livingTarget.invulnerableTime = invulnerableTime;
    }

    private boolean isSuccess(int level) {
        if (level < 1 || level > SUCCESS_RATES.size()) {
            return false;
        }
        float successRate = SUCCESS_RATES.get(level - 1);
        Random random = new Random();
        return random.nextFloat() < successRate;
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
