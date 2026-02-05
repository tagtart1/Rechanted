package net.tagtart.rechantment.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
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
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.screen.RechantmentTableMenu;

import javax.annotation.Nullable;
import java.util.List;

public class RechantmentTableBlock extends BaseEntityBlock {

    public static final MapCodec<RechantmentTableBlock> CODEC = simpleCodec(RechantmentTableBlock::new);
    public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, 0, -3, 3, 2, 3).filter((pos) -> (Math.abs(pos.getX()) <= 3 && Math.abs(pos.getX()) != 2) ||
            Math.abs(pos.getZ()) <= 3 && Math.abs(pos.getZ()) != 2).map(BlockPos::immutable).toList();


    public RechantmentTableBlock(Properties properties) {
        super(properties);
    }

    public static boolean rechantment$isValidBookShelf(Level pLevel, BlockPos p_207911_, BlockPos p_207912_) {
        return pLevel.getBlockState(p_207911_.offset(p_207912_)).getEnchantPowerBonus(pLevel, p_207911_.offset(p_207912_)) != 0.0F && pLevel.getBlockState(p_207911_.offset(p_207912_.getX() / 2, p_207912_.getY(), p_207912_.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    /*
        --- THE FOLLOWING METHODS SHOULD BE EXACTLY LIKE EnchantingTableBlock OVERRIDES MEMBERS ---
        (unless we decide to make it different)
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof RechantmentTableBlockEntity && ((RechantmentTableBlockEntity) be).getIsCharged()) {
            for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
                if (pRandom.nextInt(16) == 0 && rechantment$isValidBookShelf(pLevel, pPos, blockpos)) {
                    pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + (double)0.5F, (double)pPos.getY() + (double)2.0F, (double)pPos.getZ() + (double)0.5F, (double)((float)blockpos.getX() + pRandom.nextFloat()) - (double)0.5F, (double)((float)blockpos.getY() - pRandom.nextFloat() - 1.0F), (double)((float)blockpos.getZ() + pRandom.nextFloat()) - (double)0.5F);
                }
            }
        }
    }

    /*
        --- END METHODS THAT SHOULD BE LIKE EnchantingTableBlockEntity ---
     */

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getCounterClockWise());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RechantmentTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getBlock() == ModBlocks.RECHANTMENT_TABLE_BLOCK.get()) {
            BlockEntityTicker<EnchantingTableBlockEntity> ticker = (pLevel1, pPos, pState1, pBlockEntity) -> {
                ((RechantmentTableBlockEntity)pBlockEntity).tick(pLevel1, pPos, pState1);
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

            if (entity instanceof RechantmentTableBlockEntity be) {
                if (!pLevel.isClientSide()) {
                    SimpleMenuProvider menuToOpen = new SimpleMenuProvider(be, be.getDisplayName());
                    ((ServerPlayer)pPlayer).openMenu(menuToOpen, pPos);
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof RechantmentTableBlockEntity rechantmentTableBlockEntity) {
                rechantmentTableBlockEntity.dropInventory();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
