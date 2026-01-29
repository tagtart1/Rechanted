package net.tagtart.rechantment.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.event.TickDelayedTasks;
import net.tagtart.rechantment.item.ModItems;
import org.joml.Vector3f;

import java.util.Random;

public class ThrownWarpGemEntity extends ThrownEnderpearl {
    private static final double TRAIL_BACK_OFFSET = 0.25;
    private static final double TRAIL_SPREAD = 0.015;
    private static final int TRAIL_SEGMENTS = 3;
    private static final double TRAIL_SEGMENT_SPACING = 0.2;
    private static final DustParticleOptions TRAIL_COLOR = new DustParticleOptions(new Vector3f(0.7F, 0.1F, 0.9F), 0.7F);
    private static final int LANDING_RING_POINTS = 64;
    private static final double LANDING_RING_RADIUS = 1.25;
    private static final double LANDING_RING_SPEED = 10.35;
    private static final double LANDING_RING_Y_SPEED = 0.02;

    public ThrownWarpGemEntity(EntityType<ThrownWarpGemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownWarpGemEntity(Level level, LivingEntity owner) {
        super(ModEntities.THROWN_WARP_GEM_ENTITY.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WARP_GEM.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level() instanceof ServerLevel serverlevel && !this.isRemoved()) {
            spawnTrailParticles(serverlevel);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = this.getOwner();
            Vec3 center = this.position();
            TickDelayedTasks.enqueuedTasks.add(new TickDelayedTasks.TickDelayedTask(4) {
                final Vec3 spawnPos = center;
                @Override
                public void onTicksDelayElapsed() {

                   spawnLandingRing(serverlevel, spawnPos);
                }
            });

            if (entity != null && isAllowedToTeleportOwner(entity, serverlevel)) {
                if (entity.isPassenger()) {
                    entity.unRide();
                }

                if (entity instanceof ServerPlayer serverplayer) {
                    Rechantment.LOGGER.info("is server player!");
                    if (serverplayer.connection.isAcceptingMessages()) {
                        Rechantment.LOGGER.info("accepting messages!");
                        var event = EventHooks.onEnderPearlLand(serverplayer, this.getX(), this.getY(), this.getZ(), this, 0.0F, hitResult);
                        if (!event.isCanceled()) {
                            entity.changeDimension(
                                new DimensionTransition(
                                    serverlevel, event.getTarget(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING
                                )
                            );
                            Rechantment.LOGGER.info("playing sounds and effects!");
                            entity.resetFallDistance();
                            serverplayer.resetCurrentImpulseContext();
                            serverplayer.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5));
                            playWarpGemSound(serverlevel, this.position());
                        }
                    }
                } else {
                    entity.changeDimension(
                        new DimensionTransition(
                            serverlevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING
                        )
                    );
                    entity.resetFallDistance();
                    playWarpGemSound(serverlevel, this.position());
                }
            }

            this.discard();
        }
    }

    private static boolean isAllowedToTeleportOwner(Entity entity, Level level) {
        if (entity.level().dimension() == level.dimension()) {
            return !(entity instanceof LivingEntity livingEntity) ? entity.isAlive() : livingEntity.isAlive() && !livingEntity.isSleeping();
        } else {
            return entity.canUsePortal(true);
        }
    }



    private void spawnTrailParticles(ServerLevel serverlevel) {
        Vec3 motion = this.getDeltaMovement();
        Vec3 dir = motion.lengthSqr() > 1.0E-6 ? motion.normalize() : Vec3.ZERO;
        for (int i = 0; i < TRAIL_SEGMENTS; i++) {
            double backOffset = TRAIL_BACK_OFFSET + (TRAIL_SEGMENT_SPACING * i);
            Vec3 pos = this.position().subtract(dir.scale(backOffset));

            serverlevel.sendParticles(
                TRAIL_COLOR,
                pos.x,
                pos.y,
                pos.z,
                1,
                TRAIL_SPREAD,
                TRAIL_SPREAD,
                TRAIL_SPREAD,
                0.0
            );
        }
    }

    private void spawnLandingRing(ServerLevel serverlevel, Vec3 spawnPos) {


        double baseY = spawnPos.y + 0.1;
        for (int i = 0; i < LANDING_RING_POINTS; i++) {
            double angle = (Math.PI * 2.0) * (i / (double) LANDING_RING_POINTS);
            double xOffset = Math.cos(angle) * LANDING_RING_RADIUS;
            double zOffset = Math.sin(angle) * LANDING_RING_RADIUS;
            double xSpeed = Math.cos(angle) * LANDING_RING_SPEED ;
            double zSpeed = Math.sin(angle) * LANDING_RING_SPEED;

            serverlevel.sendParticles(
                TRAIL_COLOR,
                    spawnPos.x + xOffset,
                baseY,
                    spawnPos.z + zOffset,
                0,
                xSpeed,
                LANDING_RING_Y_SPEED,
                zSpeed,
                0.0
            );
        }
    }

    private void playWarpGemSound(Level level, Vec3 pos) {
        // Replace this sound with your custom landing sound.
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_FALL, SoundSource.PLAYERS, 2.0F, 1.0F);
    }
}
