package net.tagtart.rechantment.event.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.util.UtilFunctions;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class VolleyEnchantmentHandler {
    private static final float FAN_ANGLE_DEG = 7.0f;
    private static final double[][] CHANCES = {
            { 0.25, 0.15, 0.10, 0.10, 0.02, 0.005 }, // Level 1
            { 0.30, 0.20, 0.15, 0.15, 0.03, 0.01 }, // Level 2
            { 0.40, 0.30, 0.20, 0.20, 0.04, 0.02 } // Level 3
    };

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;
        if (event.loadedFromDisk())
            return;
        if (!(event.getEntity() instanceof AbstractArrow baseArrow))
            return;
        if (baseArrow.getTags().contains("volley_extra"))
            return;

        Entity owner = baseArrow.getOwner();
        if (!(owner instanceof LivingEntity shooter))
            return;

        int volleyLevel = getVolleyLevel(shooter);
        if (volleyLevel <= 0)
            return;

        spawnExtraArrows(level, baseArrow, shooter, volleyLevel);
    }

    private static int getVolleyLevel(LivingEntity shooter) {
        ItemStack mainHand = shooter.getMainHandItem();
        ItemStack offhand = shooter.getOffhandItem();

        int mainLevel = 0;
        if (!mainHand.isEmpty() && mainHand.is(ItemTags.BOW_ENCHANTABLE)) {
            mainLevel = UtilFunctions.getEnchantmentFromItem(
                    "rechantment:volley",
                    mainHand,
                    shooter.registryAccess());
        }

        int offhandLevel = 0;
        if (!offhand.isEmpty() && offhand.is(ItemTags.BOW_ENCHANTABLE)) {
            offhandLevel = UtilFunctions.getEnchantmentFromItem(
                    "rechantment:volley",
                    offhand,
                    shooter.registryAccess());
        }

        return Math.max(mainLevel, offhandLevel);
    }

    public static void spawnExtraArrows(ServerLevel level, AbstractArrow baseArrow, LivingEntity shooter,
            int enchLevel) {
        if (baseArrow.getTags().contains("volley_extra"))
            return;
        if (enchLevel <= 0)
            return;

        int levelIndex = Math.min(enchLevel, CHANCES.length) - 1;

        double[] odds = CHANCES[levelIndex];

        int extraArrowsToFire = 0;
        for (double odd : odds) {
            if (level.random.nextDouble() < odd) {
                extraArrowsToFire += 1;
            }
        }

        if (extraArrowsToFire <= 0)
            return;

        Rechantment.LOGGER.info("Extra arrows to fire: {}", extraArrowsToFire);

        Vec3 baseVel = baseArrow.getDeltaMovement();
        double speed = baseVel.length();
        if (speed <= 0.0001D) {
            baseVel = shooter.getLookAngle().normalize().scale(3.0D);
            speed = baseVel.length();
        }

        Vec3 dir = baseVel.normalize();

        boolean mirrorFan = (extraArrowsToFire % 2 == 1) && level.random.nextBoolean();
        for (int i = 0; i < extraArrowsToFire; i++) {
            AbstractArrow extra = createExtraArrowWithVanillaWeaponData(level, shooter, baseArrow);
            if (extra == null)
                continue;

            Rechantment.LOGGER.info("Fire Extra arrows to fire: {}", i + 1);

            extra.addTag("volley_extra");
            extra.setOwner(shooter);

            extra.moveTo(baseArrow.getX(), baseArrow.getY(), baseArrow.getZ(),
                    baseArrow.getYRot(), baseArrow.getXRot());

            extra.setBaseDamage(baseArrow.getBaseDamage());
            extra.setCritArrow(baseArrow.isCritArrow());
            extra.pickup = AbstractArrow.Pickup.DISALLOWED;

            int fanIndex = getVolleyFanIndex(i, extraArrowsToFire);
            if (mirrorFan) {
                fanIndex = -fanIndex;
            }
            float angleDeg = fanIndex * FAN_ANGLE_DEG;
            float angleRad = (float) Math.toRadians(angleDeg);

            Vec3 newDir = dir.yRot(angleRad);
            extra.shoot(newDir.x, newDir.y, newDir.z, (float) speed, 0.0f);

            level.addFreshEntity(extra);
        }
    }

    private static AbstractArrow createExtraArrowWithVanillaWeaponData(ServerLevel level, LivingEntity shooter,
            AbstractArrow baseArrow) {
        ItemStack pickup = baseArrow.getPickupItemStackOrigin();
        ItemStack weapon = baseArrow.getWeaponItem();

        // These constructors populate AbstractArrow's firedFromWeapon field, which is
        // what
        // vanilla doKnockback reads for Punch/knockback behavior.
        if (baseArrow instanceof Arrow) {
            return new Arrow(level, shooter, pickup, weapon);
        }
        if (baseArrow instanceof SpectralArrow) {
            return new SpectralArrow(level, shooter, pickup, weapon);
        }

        return (AbstractArrow) baseArrow.getType().create(level);
    }

    private static int getVolleyFanIndex(int arrowIndex, int extraArrowsToFire) {
        if (extraArrowsToFire % 2 == 1) {
            int step = arrowIndex + 1;
            int magnitude = (step + 1) / 2;
            return (step % 2 == 1) ? magnitude : -magnitude;
        }
        int step = arrowIndex + 1;
        int magnitude = (step + 1) / 2;
        return (step % 2 == 1) ? -magnitude : magnitude;
    }
}
