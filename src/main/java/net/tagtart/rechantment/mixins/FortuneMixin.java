package net.tagtart.rechantment.mixins;

import net.minecraft.util.RandomSource;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.util.UtilFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.storage.loot.functions.ApplyBonusCount$OreDrops")
public class FortuneMixin {

    // Literally nothing ever uses this function anywhere except for loot tables with fortune. If it turns out there are conflicts,
    // like from other mods that happen to use this function too, then the alternative is to mixin into ApplyBonusCount.run instead
    // and check specifically for if the enchantment is Fortune. That will run a lot more often though so this is the better solution for now imo.
    @Inject(method = "calculateNewCount", at = @At("HEAD"), cancellable = true)
    private void nerfFortune(RandomSource random, int originalCount, int enchantmentLevel, CallbackInfoReturnable<Integer> cir) {
        if (RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()) {
            cir.setReturnValue(UtilFunctions.calcluateOreDropsNerfed(random, originalCount, enchantmentLevel));
        }
    }
}