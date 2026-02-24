package net.tagtart.rechantment.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RechantmentTrophyBlockEntity extends BlockEntity {
    public RechantmentTrophyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RECHANTMENT_TROPHY_BE.get(), pos, blockState);
    }
}
