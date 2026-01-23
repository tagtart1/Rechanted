package net.tagtart.rechantment.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.Rechantment;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VolleySpawnArrowEnchantmentEffect implements EnchantmentEntityEffect {
    public static final MapCodec<VolleySpawnArrowEnchantmentEffect> CODEC = MapCodec
            .unit(VolleySpawnArrowEnchantmentEffect::new);

    private static final double[][] CHANCES = {
            {0.33, 0.15, 0.0050}, // Level 1
            {0.50, 0.20, 0.0100},  // Level 2
            {0.60, 0.25, 0.0200} // Level 3
    };



    @Override
    public void apply(ServerLevel level, int enchLevel, EnchantedItemInUse item, Entity target, Vec3 pos) {
        Rechantment.LOGGER.info("Shooting arrows!");
        if (!(target instanceof AbstractArrow baseArrow)) return;
        Rechantment.LOGGER.info("Passed abstract test!");
        // Prevent infinite recursion: our spawned arrows will also trigger PROJECTILE_SPAWNED
        if (baseArrow.getTags().contains("volley_extra")) return;

        Entity arrowOwner = baseArrow.getOwner();
        LivingEntity shooter = null;
        if (arrowOwner instanceof LivingEntity livingOwner) {
            shooter = livingOwner;
        } else if (item.owner() instanceof LivingEntity livingOwner) {
            shooter = livingOwner;
        }
        if (shooter == null) return;

        // Mark the original so we only expand once
        baseArrow.addTag("volley_extra");

        Vec3 baseVel = baseArrow.getDeltaMovement();
        double speed = baseVel.length();
        if (speed <= 0.0001D) {
            baseVel = shooter.getLookAngle().normalize().scale(3.0D);
            speed = baseVel.length();
        }

        Vec3 dir = baseVel.normalize();

        double[] odds = CHANCES[enchLevel - 1];

        int extraArrowsToFire = 0;
        for (double odd : odds) {
            if (level.random.nextDouble() < odd) {
                extraArrowsToFire += 1;
            } else {
                break;
            }
        }


        // Spawn extra arrows around the original direction
        Rechantment.LOGGER.info("Creating extra arrows: {}", extraArrowsToFire);
        for (int i = 0; i < extraArrowsToFire; i++) {
            AbstractArrow extra = (AbstractArrow) baseArrow.getType().create(level);

            if (extra == null) continue;

            extra.addTag("volley_extra");
            extra.setOwner(shooter);

            // spawn at same position
            extra.moveTo(baseArrow.getX(), baseArrow.getY(), baseArrow.getZ(),
                    baseArrow.getYRot(), baseArrow.getXRot());

            // copy common arrow properties you care about
            extra.setBaseDamage(baseArrow.getBaseDamage());
            extra.setCritArrow(baseArrow.isCritArrow());

            // compute angle offset (example: symmetric fan)
            float fanIndex = (i + 1) - (extraArrowsToFire + 1) / 2f;
            float angleRad = (float) Math.toRadians(fanIndex * 10);

            // rotate direction around Y (simple horizontal fan)
            Vec3 newDir = dir.yRot(angleRad);

            // shoot with same speed, no extra inaccuracy
            extra.shoot(newDir.x, newDir.y, newDir.z, (float) speed, 0.0f);

            level.addFreshEntity(extra);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
