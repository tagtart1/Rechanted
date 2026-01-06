package net.tagtart.rechantment.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.tagtart.rechantment.event.ScheduledRebirthTasks;
import net.tagtart.rechantment.util.UtilFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import oshi.util.tuples.Pair;

import javax.swing.text.Utilities;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

      // TODO: Re-implement when enchantments are figured out. OR... find a way to do without dumbass mixin if possible
      // but iirc events didn't work last time. Regardless, rebirth does NOT WORK until this is re-implemented.
//
//    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
//    public <T extends LivingEntity> void hurtAndBreak(int pAmount, T pEntity, Consumer<T> pOnBroken, CallbackInfo ci) {
//        ItemStack itemstack = (ItemStack) (Object) this;
//        Pair<RebirthEnchantment, Integer> rebirthEnchantmentPair = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", itemstack, RebirthEnchantment.class);
//        if (rebirthEnchantmentPair != null) {
//            if (!pEntity.level().isClientSide && (!(pEntity instanceof Player) || !((Player) pEntity).getAbilities().instabuild) && itemstack.isDamageableItem()) {
//                pAmount = itemstack.getItem().damageItem(itemstack, pAmount, pEntity, pOnBroken);
//                int oldDamage = itemstack.getDamageValue();
//                if (itemstack.hurt(pAmount, pEntity.getRandom(), pEntity instanceof ServerPlayer ? (ServerPlayer) pEntity : null)) {
//                    Item item = itemstack.getItem();
//                    pOnBroken.accept(pEntity);
//                    if (pEntity instanceof  Player player) {
//                        player.awardStat(Stats.ITEM_BROKEN.get(item));
//
//                        RebirthEnchantment rebirthEnchantment = rebirthEnchantmentPair.getA();
//                        if (rebirthEnchantment.shouldBeReborn(rebirthEnchantmentPair.getB())){
//
//                            ItemStack newItemStack = item.getDefaultInstance();
//                            newItemStack.setTag(itemstack.getTag());
//                            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(newItemStack);
//                            enchantments.remove(rebirthEnchantment);
//                            enchantments.put(ModEnchantments.REBORN.get(), 1);
//                            EnchantmentHelper.setEnchantments(enchantments, newItemStack);
//
//                            if (newItemStack.getTag() != null)
//                                newItemStack.getTag().putInt("Damage", 0);
//                            newItemStack.removeTagKey("RepairCost");
//
//                            int freeSlot = player.getInventory().selected;
//                            boolean isOffhand = false;
//                            if (ItemStack.isSameItemSameTags(newItemStack, player.getInventory().offhand.get(0))) {
//                                isOffhand = true;
//                            }
//
//                            ScheduledRebirthTasks.EnqueueItemForRebirth((ServerPlayer)player, newItemStack, freeSlot, isOffhand);
//
//                        } else {
//                            player.sendSystemMessage(Component.literal("Your item failed to be reborn!").withStyle(ChatFormatting.RED));
//                        }
//                    }
//                    itemstack.shrink(1);
//                }
//                else {
//                    itemstack.setDamageValue(oldDamage);
//                }
//            }
//        }
//    }
}
