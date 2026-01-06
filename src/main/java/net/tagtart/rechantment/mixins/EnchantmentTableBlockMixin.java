package net.tagtart.rechantment.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.tagtart.rechantment.block.entity.ModReplacementBlockEntities;
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.screen.RechantmentTableMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(EnchantingTableBlock.class)
public class EnchantmentTableBlockMixin
{
    @Unique
    private static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, 0, -3, 3, 2, 3).filter((pos) -> (Math.abs(pos.getX()) <= 3 && Math.abs(pos.getX()) != 2) ||
                                                                                                                                                                                Math.abs(pos.getZ()) <= 3 && Math.abs(pos.getZ()) != 2).map(BlockPos::immutable).toList();

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void useWithoutItem(BlockState state, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {

        if (pPlayer.level().isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        } else {
            BlockEntity entity = pLevel.getBlockEntity(pPos);

            if (entity instanceof RechantmentTableBlockEntity) {
                SimpleMenuProvider menuToOpen = new SimpleMenuProvider(
                    (id, inventory, player) -> new RechantmentTableMenu(id, inventory, entity), ((RechantmentTableBlockEntity)entity).getDisplayName());
                ((ServerPlayer)pPlayer).openMenu(menuToOpen);

            }
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    public void newBlockEntity(BlockPos pPos, BlockState pState, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(new RechantmentTableBlockEntity(pPos, pState));
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {

        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof RechantmentTableBlockEntity && ((RechantmentTableBlockEntity) be).getIsCharged()) {
            for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
                if (pRandom.nextInt(16) == 0 && rechantment$isValidBookShelf(pLevel, pPos, blockpos)) {
                    pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + (double)0.5F, (double)pPos.getY() + (double)2.0F, (double)pPos.getZ() + (double)0.5F, (double)((float)blockpos.getX() + pRandom.nextFloat()) - (double)0.5F, (double)((float)blockpos.getY() - pRandom.nextFloat() - 1.0F), (double)((float)blockpos.getZ() + pRandom.nextFloat()) - (double)0.5F);
                }
            }
        }

        ci.cancel();
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    public <T extends BlockEntity> void getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {

        // Enchantment table only ticks on client-side unlike a lot of other stuff
        if(!pLevel.isClientSide()) {
            cir.setReturnValue(null);
        }
        if (pState.getBlock() == Blocks.ENCHANTING_TABLE) {
            BlockEntityTicker<RechantmentTableBlockEntity> ticker = (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1);
            cir.setReturnValue((BlockEntityTicker<T>)ticker);
        }
    }

    @Unique
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>) pTicker : null;
    }


    @Unique
    private static boolean rechantment$isValidBookShelf(Level pLevel, BlockPos p_207911_, BlockPos p_207912_) {
        return pLevel.getBlockState(p_207911_.offset(p_207912_)).getEnchantPowerBonus(pLevel, p_207911_.offset(p_207912_)) != 0.0F && pLevel.getBlockState(p_207911_.offset(p_207912_.getX() / 2, p_207912_.getY(), p_207912_.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }
}


