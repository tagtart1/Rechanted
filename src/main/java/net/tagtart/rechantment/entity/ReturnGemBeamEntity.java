package net.tagtart.rechantment.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.item.custom.ReturnGemItem;

public class ReturnGemBeamEntity extends Entity {

    public Player player = null;

    public static final int BEAM_DURATION_TICKS = 60;

    private int ticksRemaining = BEAM_DURATION_TICKS;

    public float glowRadius = 0.05f;
    public float beamRadius = 0.02f;

    public ReturnGemBeamEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }



    @Override
    public void tick() {
        super.tick();

        if (this.player != null && player.level() instanceof ServerLevel level) {
            Vec3 pos = player.position();
            double xOffset = Math.sin(level.getGameTime());
            double zOffset = Math.cos(level.getGameTime());

            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x + xOffset, pos.y + 0.5, pos.z, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x - xOffset, pos.y + 0.5, pos.z, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x, pos.y + 0.5, pos.z + zOffset, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x, pos.y + 0.5, pos.z - zOffset, 1,0, 0.02, 0, 0.1);
        }

        if (--ticksRemaining <= 0) {
            if (this.player != null) {
                ReturnGemItem.removeReturnEntity(player);
            }
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        double maxBeamRadius = 3.0f;
        double beamHeight = 100.0f;
        return new AABB(
                getX() - maxBeamRadius, getY() - beamHeight, getZ() - maxBeamRadius,
                getX() + maxBeamRadius, getY() + beamHeight, getZ() + maxBeamRadius
        );
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
