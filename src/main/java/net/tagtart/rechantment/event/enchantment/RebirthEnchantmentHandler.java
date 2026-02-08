package net.tagtart.rechantment.event.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        ItemStack itemStack = event.getOriginal();

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        int rebirthEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", itemStack, event.getEntity().registryAccess());
        if (rebirthEnchantmentLevel != 0) {
            ItemStack newItemStack = itemStack.copy();
            newItemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {

                ItemEnchantments.Mutable mutableCopy = new ItemEnchantments.Mutable(itemEnchantments);
                mutableCopy.removeIf((enchantment) -> enchantment.getKey() == ModEnchantments.REBIRTH);
                return mutableCopy.toImmutable();
            });
            newItemStack.set(ModDataComponents.REBORN, true);
            newItemStack.setDamageValue(0);
            newItemStack.remove(DataComponents.REPAIR_COST);

            int freeSlot = player.getInventory().selected;
            boolean isOffhand = event.getHand() == InteractionHand.OFF_HAND;

            TickDelayedTasks.EnqueueItemForRebirth(player, newItemStack, freeSlot, isOffhand);
        }

    }
}
