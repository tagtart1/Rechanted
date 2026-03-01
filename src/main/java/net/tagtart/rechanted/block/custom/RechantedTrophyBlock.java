package net.tagtart.rechanted.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tagtart.rechanted.block.ModBlocks;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.block.entity.RechantedTrophyBlockEntity;
import org.jetbrains.annotations.Nullable;

public class RechantedTrophyBlock extends BaseEntityBlock {

    public static final MapCodec<RechantedTrophyBlock> CODEC = simpleCodec(RechantedTrophyBlock::new);
    public static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

    public RechantedTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RechantedTrophyBlockEntity(pos, state);
    }

    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getCounterClockWise());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getBlock() == ModBlocks.RECHANTED_TROPHY_BLOCK.get()) {
            BlockEntityTicker<RechantedTrophyBlockEntity> ticker = (pLevel1, pPos, pState1, pBlockEntity) -> {
                pBlockEntity.tick(pLevel1, pPos, pState1);
            };
            return (BlockEntityTicker<T>)ticker;
        }

        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult hitResult) {
        if (pPlayer.level().isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity entity = pLevel.getBlockEntity(pPos);

            if (entity instanceof RechantedTrophyBlockEntity be) {
                be.onInteraction(pLevel, pPlayer);
            }
            return InteractionResult.CONSUME;
        }
    }
}
