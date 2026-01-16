package net.tagtart.rechantment.mixins;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

//    @Unique
//    @Nullable
//    private IRechantmentTableAccess selfAsEnchantmentTableAccess() {
//        BlockEntity be = (BlockEntity)(Object)this;
//        if (be instanceof EnchantingTableBlockEntity) {
//            return (IRechantmentTableAccess)be;
//        }
//
//        return null;
//    }
//
//    // This definitely impacts performance slightly and is really bad but it has to be done.
//    // If we can find an alternative method later we should do it instead, since this runs literally
//    // any time a block is removed and performs multiple casts and an instanceof. SLOW AS FUCK!!!!!!!
//    // OR we can remove custom sounds from the enchanting table but that is lame.
//    @Inject(method = "setRemoved", at = @At("HEAD"))
//    private void setRemoved(CallbackInfo ci) {
//        IRechantmentTableAccess rechantmentTableAccess = this.selfAsEnchantmentTableAccess();
//        if (rechantmentTableAccess != null) {
//            rechantmentTableAccess.onSetRemoved();
//        }
//    }

}
