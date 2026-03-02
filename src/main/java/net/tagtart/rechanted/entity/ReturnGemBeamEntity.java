package net.tagtart.rechanted.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechanted.item.custom.ReturnGemItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ReturnGemBeamEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(ReturnGemBeamEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final String OWNER_UUID_TAG = "OwnerUUID";

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
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        Optional<UUID> uuid = this.entityData.get(OWNER_UUID);
        uuid.ifPresent(value -> compound.putUUID(OWNER_UUID_TAG, value));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {

        if (compound.hasUUID(OWNER_UUID_TAG)) {
            this.entityData.set(OWNER_UUID, Optional.of(compound.getUUID(OWNER_UUID_TAG)));
        }
    }

    @Override
    public void tick() {
        super.tick();

        Player player = this.getOwner();
        if (player != null && player.level() instanceof ServerLevel level) {
            Vec3 pos = player.position();
            double xOffset = Math.sin(level.getGameTime());
            double zOffset = Math.cos(level.getGameTime());

            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x + xOffset, pos.y + 0.5, pos.z, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x - xOffset, pos.y + 0.5, pos.z, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x, pos.y + 0.5, pos.z + zOffset, 1,0, 0.02, 0, 0.1);
            level.sendParticles(ParticleTypes.DUST_PLUME, pos.x, pos.y + 0.5, pos.z - zOffset, 1,0, 0.02, 0, 0.1);

            // This does not guarantee interpolation; and using setDeltaMovement doesn't really
            // do that either. See ReturnGemBeamEntityRenderer for how position is correctly interpolated client side per-frame.
            this.setPos(pos);

            if (--ticksRemaining <= 0) {
                ReturnGemItem.removeReturnEntity(player);
                this.discard();
            }
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

    @Nullable
    public Player getOwner() {
        Optional<UUID> uuid = this.entityData.get(OWNER_UUID);
        return uuid.map(value -> this.level().getPlayerByUUID(value)).orElse(null);
    }

    public void setOwner(Player player) {
        this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
    }
}
