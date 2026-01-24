package net.tagtart.rechantment.event.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.attachments.ModAttachments;
import net.tagtart.rechantment.event.ParticleEmitter;
import net.tagtart.rechantment.util.UtilFunctions;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class SpringJumpHandler {
    private static final int SPRING_CHARGE_TICKS = 50;
    private static final double[] SPRING_VERTICAL_BOOST = {0.40D, 0.50D, .70D};
    private static final int SPRING_TRAIL_PARTICLES = 60;
    private static final int SPRING_TRAIL_TICKS = 20;
    private static final double SPRING_LANDING_EPSILON = 0.01D;
    private static final float SPRING_READY_VOLUME = 1.2F;
    private static final float SPRING_READY_PITCH = 1.2F;
    private static final SimpleParticleType[] SPRING_TRAIL_PARTICLE_TYPES = new SimpleParticleType[] {
            ParticleTypes.FIREWORK
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        int springLevel = UtilFunctions.getEnchantmentFromItem("rechantment:spring", leggings, player.registryAccess());
        int chargeTicks = player.getData(ModAttachments.SPRING_CHARGE_TICKS);

        if (springLevel <= 0) {
            if (chargeTicks != 0) {
                player.setData(ModAttachments.SPRING_CHARGE_TICKS, 0);
            }
            return;
        }

        if (player.isCrouching() && player.onGround()) {
            int nextChargeTicks = Math.min(chargeTicks + 1, SPRING_CHARGE_TICKS);
            if (nextChargeTicks != chargeTicks) {
                player.setData(ModAttachments.SPRING_CHARGE_TICKS, nextChargeTicks);
            }
            if (nextChargeTicks == SPRING_CHARGE_TICKS) {
                if (chargeTicks < SPRING_CHARGE_TICKS) {
                    player.displayClientMessage(Component.literal("Spring is ready!")
                            .withStyle(ChatFormatting.GREEN), true);
                    spawnSpringHalo(player);
                    player.level().playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.NOTE_BLOCK_BELL.value(),
                            SoundSource.PLAYERS,
                            SPRING_READY_VOLUME,
                            SPRING_READY_PITCH
                    );

                } else if (player.tickCount % 10 == 0) {
                    spawnSpringHalo(player);
                }
            }
        } else if (chargeTicks != 0) {
            player.setData(ModAttachments.SPRING_CHARGE_TICKS, 0);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        int springLevel = UtilFunctions.getEnchantmentFromItem("rechantment:spring", leggings, player.registryAccess());
        if (springLevel <= 0 || !player.isCrouching()) {
            return;
        }

        int chargeTicks = player.getData(ModAttachments.SPRING_CHARGE_TICKS);
        if (chargeTicks < SPRING_CHARGE_TICKS) {
            Rechantment.LOGGER.info("Not enough charge ticks!");
            return;
        }

        double boost = SPRING_VERTICAL_BOOST[Math.min(springLevel, SPRING_VERTICAL_BOOST.length) - 1];
        Vec3 movement = player.getDeltaMovement();
        Vec3 jumpVec = new Vec3(movement.x, movement.y + boost, movement.z);
        player.setDeltaMovement(jumpVec);

        // Calculate food cost on jump
        if (!player.level().isClientSide()) {
            var foodData = player.getFoodData();
            float cost = 4.0F;
            float saturation = foodData.getSaturationLevel();
            float saturationUsed = Math.min(saturation, cost);
            if (saturationUsed > 0.0F) {
                foodData.setSaturation(saturation - saturationUsed);
                cost -= saturationUsed;
            }
            if (cost > 0.0F) {
                foodData.setFoodLevel(Math.max(foodData.getFoodLevel() - (int) cost, 0));
            }
        }
        player.setData(ModAttachments.SPRING_CHARGE_TICKS, 0);
        player.setData(ModAttachments.SPRING_JUMP_ACTIVE, true);
        player.setData(ModAttachments.SPRING_JUMP_START_Y, player.getY());
        spawnSpringTrail(player);
        player.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.getData(ModAttachments.SPRING_JUMP_ACTIVE)) {
            return;
        }

        double startY = player.getData(ModAttachments.SPRING_JUMP_START_Y);
        double landingY = player.getY();
        player.setData(ModAttachments.SPRING_JUMP_ACTIVE, false);
        player.setData(ModAttachments.SPRING_JUMP_START_Y, 0.0);

        double landingDiff = startY - landingY;
        if (landingDiff <= SPRING_LANDING_EPSILON) {
            event.setDistance(0.0F);
        } else {
            event.setDistance((float) landingDiff);
        }
    }


    private static void spawnSpringHalo(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        double radius = 0.6D;
        double y = player.getY() + 0.05D;
        int points = 12;
        for (int i = 0; i < points; i++) {
            double angle = (2.0D * Math.PI * i) / points;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnSpringTrail(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        ParticleEmitter.emitParticlesOverTime(player, level, SPRING_TRAIL_PARTICLES, SPRING_TRAIL_TICKS, SPRING_TRAIL_PARTICLE_TYPES);
    }
}
