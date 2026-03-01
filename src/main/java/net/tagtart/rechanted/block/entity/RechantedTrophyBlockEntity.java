package net.tagtart.rechanted.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.tagtart.rechanted.util.AnimHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RechantedTrophyBlockEntity extends BlockEntity {


    public static float TAU = (float)Math.PI * 2.0f;
    public static float ROTATION_OFFSET_BASE_DECCELERATION = TAU / 20f;
    public static float MAX_ROTATION_OFFSET = TAU * 3.0f;

    public float rotationOffset = 0.0f;
    public float oldRotationOffset = 0.0f;

    public RechantedTrophyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RECHANTED_TROPHY_BE.get(), pos, blockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        oldRotationOffset = rotationOffset;
        rotationOffset -= ROTATION_OFFSET_BASE_DECCELERATION * getExponentialSpinFactor();
        rotationOffset = Math.clamp(rotationOffset, 0.0f, TAU * 3.0F);
    }

    public void onInteraction(Level pLevel, Player pPlayer) {

        if (pLevel == null || pLevel.isClientSide || pPlayer == null) return;

        if (rotationOffset < (TAU * 2.0f) - 0.001f) {
            rotationOffset += TAU;
            rotationOffset = Math.clamp(rotationOffset, 0.0f, MAX_ROTATION_OFFSET);
        }

        // This seems weird to do, but without this there's an issue where the book will
        // interpolate to an offset that is very large for one frame. This line essentially prevents
        // any rotation for the first frame after interacting. Then it can interpolate per-frame like normal.
        oldRotationOffset = rotationOffset;

        ServerLevel serverLevel = (ServerLevel)level;
        BlockPos pos = getBlockPos();

        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 0.6f, pos.getZ() + 0.5f, 3, 0, 0, 0, 0.25);
        serverLevel.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.5F, 1.25f);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.25F, 1.25f);

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private float getExponentialSpinFactor() {
        float t = Mth.inverseLerp(rotationOffset, 0.0f, MAX_ROTATION_OFFSET);
        t = AnimHelper.easeOutExpo(t);
        return Mth.lerp(t, 0.5f, 3.0f);
    }

    // For saving the data of what is inside the block when the game is saved.
    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);

        pTag.putFloat("RotationOffset", rotationOffset);
        pTag.putFloat("OldRotationOffset", oldRotationOffset);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);

        rotationOffset = pTag.getFloat("RotationOffset");
        oldRotationOffset = pTag.getFloat("OldRotationOffset");
    }

    @Override
    public void handleUpdateTag(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(pTag, lookupProvider);

        rotationOffset = pTag.getFloat("RotationOffset");
        oldRotationOffset = pTag.getFloat("OldRotationOffset");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
