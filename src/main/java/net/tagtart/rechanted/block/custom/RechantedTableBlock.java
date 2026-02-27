package net.tagtart.rechanted.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
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
import net.tagtart.rechanted.block.ModBlocks;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.block.renderer.RechantedTableRenderer;
import net.tagtart.rechanted.screen.RechantedTableMenu;
import net.tagtart.rechanted.util.AnimHelper;

import javax.annotation.Nullable;
import java.util.List;

public class RechantedTableBlock extends BaseEntityBlock {

    public static final MapCodec<RechantedTableBlock> CODEC = simpleCodec(RechantedTableBlock::new);
    public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, 0, -3, 3, 2, 3).filter((pos) -> (Math.abs(pos.getX()) <= 3 && Math.abs(pos.getX()) != 2) ||
            Math.abs(pos.getZ()) <= 3 && Math.abs(pos.getZ()) != 2).map(BlockPos::immutable).toList();


    public RechantedTableBlock(Properties properties) {
        super(properties);
    }

    public static boolean rechanted$isValidBookShelf(Level pLevel, BlockPos p_207911_, BlockPos p_207912_) {
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
        if (be instanceof RechantedTableBlockEntity rbe && ((RechantedTableBlockEntity) be).getIsCharged()) {
            for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
                if (pRandom.nextInt(16) == 0 && rechanted$isValidBookShelf(pLevel, pPos, blockpos)) {

                    float yOffset = 2.0f;
                    if (rbe.tableState == RechantedTableBlockEntity.CustomRechantedTableState.BonusPending) {
                        long gameTime = Minecraft.getInstance().level.getGameTime();
                        long stateStartTime = gameTime - (RechantedTableBlockEntity.BONUS_PENDING_ANIMATION_LENGTH_TICKS - rbe.currentStateTimeRemaining);
                        float time = (gameTime - stateStartTime);

                        yOffset += AnimHelper.evaluateKeyframes(RechantedTableRenderer.BONUS_PENDING_Y_TRANSLATION_KEYFRAMES, time);
                    }
                    pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + 0.5, (double)pPos.getY() + (double)yOffset, (double)pPos.getZ() + 0.5, (blockpos.getX() + pRandom.nextDouble()) - 0.5, (double)(blockpos.getY() - pRandom.nextDouble() - 1.0), (blockpos.getZ() + pRandom.nextDouble()) - 0.5);
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
        return new RechantedTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getBlock() == ModBlocks.RECHANTED_TABLE_BLOCK.get()) {
            BlockEntityTicker<EnchantingTableBlockEntity> ticker = (pLevel1, pPos, pState1, pBlockEntity) -> {
                ((RechantedTableBlockEntity)pBlockEntity).tick(pLevel1, pPos, pState1);
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

            if (entity instanceof RechantedTableBlockEntity be) {
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
            if (level.getBlockEntity(pos) instanceof RechantedTableBlockEntity rechantedTableBlockEntity) {
                rechantedTableBlockEntity.dropInventory();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
