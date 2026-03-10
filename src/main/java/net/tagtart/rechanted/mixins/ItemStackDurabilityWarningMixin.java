package net.tagtart.rechanted.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tagtart.rechanted.config.RechantedCommonConfigs;
import net.tagtart.rechanted.datagen.ModItemTagsProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityWarningMixin {
    @Unique
    private int rechanted$durabilityBeforeDamage = -1;

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void rechanted$captureDurabilityBeforeDamage(
            int amount,
            ServerLevel level,
            @Nullable LivingEntity entity,
            Consumer<Item> onBreak,
            CallbackInfo ci
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        rechanted$durabilityBeforeDamage = stack.isDamageableItem() ? stack.getDamageValue() : -1;
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void rechanted$warnGearAboutToBreak(
            int amount,
            ServerLevel level,
            @Nullable LivingEntity entity,
            Consumer<Item> onBreak,
            CallbackInfo ci
    ) {
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!RechantedCommonConfigs.GEAR_BREAK_WARNING_ENABLED.get()) {
            return;
        }

        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty() || !stack.isDamageableItem() || !stack.isEnchanted() || !stack.is(ModItemTagsProvider.REBIRTH_ENCHANTABLE)) {
            return;
        }

        int damageAfter = stack.getDamageValue();
        if (rechanted$durabilityBeforeDamage < 0 || damageAfter <= rechanted$durabilityBeforeDamage) {
            return;
        }

        int remainingUses = stack.getMaxDamage() - damageAfter;
        if (remainingUses <= 0 || remainingUses >= 20) {
            return;
        }

        serverPlayer.displayClientMessage(
                Component.translatable(
                                "message.rechanted.gear_about_to_break",
                                stack.getHoverName(),
                                remainingUses
                        )
                        .withStyle(ChatFormatting.RED),
                true
        );
    }
}
