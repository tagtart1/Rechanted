package net.tagtart.rechantment.event.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.event.TickDelayedTasks;
import net.tagtart.rechantment.util.UtilFunctions;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class RebirthEnchantmentHandler {

    @SubscribeEvent
    public static void onItemBreak(PlayerDestroyItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        int freeSlot = player.getInventory().selected;
        boolean isOffhand = event.getHand() == InteractionHand.OFF_HAND;
        tryRebirth(player, event.getOriginal(), freeSlot, isOffhand, null);
    }

    @SubscribeEvent
    public static void onArmorBreak(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) {
            return;
        }

        ItemStack oldStack = event.getFrom();
        ItemStack newStack = event.getTo();
        if (oldStack.isEmpty() || !newStack.isEmpty()) {
            return;
        }
        if (!oldStack.isDamageableItem() || oldStack.getDamageValue() < oldStack.getMaxDamage()) {
            return;
        }

        tryRebirth(player, oldStack, -1, false, slot);
    }

    private static void tryRebirth(ServerPlayer player, ItemStack itemStack, int inventorySlot, boolean isOffhand, EquipmentSlot equipmentSlot) {
        int rebirthEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", itemStack, player.registryAccess());
        if (rebirthEnchantmentLevel == 0) {
            return;
        }

        ItemStack newItemStack = itemStack.copy();
        newItemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {
            ItemEnchantments.Mutable mutableCopy = new ItemEnchantments.Mutable(itemEnchantments);
            mutableCopy.removeIf((enchantment) -> enchantment.getKey() == ModEnchantments.REBIRTH);
            return mutableCopy.toImmutable();
        });
        newItemStack.set(ModDataComponents.REBORN, true);
        newItemStack.setDamageValue(0);
        newItemStack.remove(DataComponents.REPAIR_COST);

        TickDelayedTasks.EnqueueItemForRebirth(player, newItemStack, inventorySlot, isOffhand, equipmentSlot);
    }
}
