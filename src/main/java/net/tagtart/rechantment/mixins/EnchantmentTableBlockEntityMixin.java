package net.tagtart.rechantment.mixins;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.tagtart.rechantment.attachments.ModAttachments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantmentTableBlockEntityMixin
{
//
//    private EnchantingTableBlockEntity self() {
//        return (EnchantingTableBlockEntity)(Object)this;
//    }
//
//    private RechantmentTableBlockEntityData data() {
//        return self().getData(ModAttachments.RECHANTMENT_TABLE_ENTITY_DATA);
//    }
//
//    @Inject(method = "saveAdditional", at = @At("TAIL"))
//    private void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries, CallbackInfo ci) {
//        data().save(pTag, registries);
//    }
//
//    @Inject(method = "loadAdditional", at = @At("TAIL"))
//    private void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries, CallbackInfo ci) {
//        data().load(pTag, registries);
//    }
//
//    // why why why why why in god's name can't mixins just allow this.
////    @Unique public void setRemoved() {
////        EnchantingTableBlockEntity self = self();
////
////    }
}
