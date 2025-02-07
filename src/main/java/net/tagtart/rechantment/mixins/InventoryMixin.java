package net.tagtart.rechantment.mixins;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

// This mixin handles announcements for rare found books
@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    public void setItem(int pSlot, ItemStack pStack, CallbackInfo ci) {
       Inventory inventory = (Inventory)(Object)this;
       if (pStack.hasTag()) {
           CompoundTag tag = pStack.getTag();
           if (tag != null) {
            boolean shouldAnnounce = tag.getBoolean("Announce");

            if (shouldAnnounce) {
                tag.remove("Announce");
                int successRate = tag.getInt("SuccessRate");
                String enchantmentRaw = tag.getCompound("Enchantment").getString("id");
                int enchantLevel = tag.getCompound("Enchantment").getInt("lvl");
                String enchantmentFormatted = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentRaw)).getFullname(enchantLevel).getString();

                if (inventory.player.level() instanceof ServerLevel serverLevel) {
                    Component displayName = pStack.getDisplayName();
                    Style displayHoverStyle = displayName.getStyle();

                    Component playerName = inventory.player.getDisplayName();
                    BookRarityProperties bookProps = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantLevel);
                    if (bookProps != null) {
                        for (ServerPlayer otherPlayer : serverLevel.players()) {

                            otherPlayer.sendSystemMessage(Component.literal(playerName.getString() + " found ")
                                    .append(Component.literal(enchantmentFormatted).withStyle(displayHoverStyle.withColor(bookProps.color).withUnderlined(true)))
                                    .append(" at ")
                                    .append(Component.literal(successRate + "%").withStyle(Style.EMPTY.withColor(bookProps.color)))
                                    .append("!"));
                        }
                    }
                    inventory.player.level().playSound(null, inventory.player.getOnPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
                }
            }
           }
       }
    }
}