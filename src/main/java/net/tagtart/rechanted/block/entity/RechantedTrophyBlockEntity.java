package net.tagtart.rechanted.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RechantedTrophyBlockEntity extends BlockEntity {
    public RechantedTrophyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RECHANTED_TROPHY_BE.get(), pos, blockState);
    }
}
