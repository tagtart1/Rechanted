package net.tagtart.rechantment.mixins;

import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.screen.RechantmentTableMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantingTableBlock.class)
public class EnchantmentTableBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void useWithoutItem(BlockState state, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (pPlayer.level().isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        } else {
            BlockEntity entity = pLevel.getBlockEntity(pPos);

            if (entity instanceof EnchantingTableBlockEntity) {
//                SimpleMenuProvider menuToOpen = new SimpleMenuProvider(
//                    (id, inventory, player) -> new RechantmentTableMenu(id, inventory, entity), (((EnchantingTableBlockEntity) entity).getName()));
//                ((ServerPlayer)pPlayer).openMenu(menuToOpen);
                pPlayer.displayClientMessage(Component.literal("A mysterious force is preventing the book from opening..."), true);
            }
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {

        // No longer animates text symbols near bookshelves.
        ci.cancel();
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    public <T extends BlockEntity> void getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {

        // Enchantment table only ticks on client-side unlike a lot of other stuff
        if(!pLevel.isClientSide()) {
            cir.setReturnValue(null);
        }
        if (pState.getBlock() == Blocks.ENCHANTING_TABLE) {
            BlockEntityTicker<EnchantingTableBlockEntity> ticker = (pLevel1, pPos, pState1, pBlockEntity) -> {
                // Ticking now does nothing; no animation
                //((RechantmentTableBlockEntity)pBlockEntity).tick(pLevel1, pPos, pState1);
                EnchantingTableBlockEntity be = pBlockEntity;
            };
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


