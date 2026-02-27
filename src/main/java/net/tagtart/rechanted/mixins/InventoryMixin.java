package net.tagtart.rechanted.mixins;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;

// This mixin handles announcements for rare found books
@Mixin(Inventory.class)
public class InventoryMixin {

        // TODO: If announcements don't work, re-implement this with data components.
//    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
//    public void setItem(int pSlot, ItemStack pStack, CallbackInfo ci) {
//       Inventory inventory = (Inventory)(Object)this;
//       if (pStack.hasTag()) {
//           CompoundTag tag = pStack.getTag();
//           if (tag != null) {
//            boolean shouldAnnounce = tag.getBoolean("Announce");
//
//            if (shouldAnnounce) {
//                tag.remove("Announce");
//                int successRate = tag.getInt("SuccessRate");
//                String enchantmentRaw = tag.getCompound("Enchantment").getString("id");
//                int enchantLevel = tag.getCompound("Enchantment").getInt("lvl");
//                String enchantmentFormatted = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentRaw)).getFullname(enchantLevel).getString();
//
//                if (inventory.player.level() instanceof ServerLevel serverLevel) {
//                    Component displayName = pStack.getDisplayName();
//                    Style displayHoverStyle = displayName.getStyle();
//
//                    Component playerName = inventory.player.getDisplayName();
//                    BookRarityProperties bookProps = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);
//                    if (bookProps != null) {
//                        for (ServerPlayer otherPlayer : serverLevel.players()) {
//
//                            otherPlayer.sendSystemMessage(Component.literal(playerName.getString() + " found ")
//                                    .append(Component.literal(enchantmentFormatted).withStyle(displayHoverStyle.withColor(bookProps.color).withUnderlined(true)))
//                                    .append(" at ")
//                                    .append(Component.literal(successRate + "%").withStyle(Style.EMPTY.withColor(bookProps.color)))
//                                    .append("!"));
//                        }
//                    }
//                    inventory.player.level().playSound(null, inventory.player.getOnPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
//                }
//            }
//           }
//       }
//    }
}