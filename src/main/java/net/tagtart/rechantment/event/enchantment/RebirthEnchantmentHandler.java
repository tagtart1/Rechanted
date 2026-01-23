package net.tagtart.rechantment.event.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.event.ScheduledRebirthTasks;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class RebirthEnchantmentHandler {

    private static final List<Float> REBIRTH_SUCCESS_RATES = Arrays.asList(
            0.01f,
            0.75f,
            1.00f
    );

    private static boolean shouldBeReborn(int rebirthEnchantmentLevel) {
        float successRate =  REBIRTH_SUCCESS_RATES.get(rebirthEnchantmentLevel - 1);
        Random random = new Random();
        return random.nextFloat() < successRate;
    }

    @SubscribeEvent
    public static void onItemBreak(PlayerDestroyItemEvent event) {
        ItemStack itemStack = event.getOriginal();
        ServerPlayer player = (ServerPlayer)event.getEntity();

        int rebirthEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", itemStack, event.getEntity().registryAccess());
        if (rebirthEnchantmentLevel != 0) {
            if (shouldBeReborn(rebirthEnchantmentLevel)){

                ItemStack newItemStack = itemStack.copy();
                newItemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {

                    ItemEnchantments.Mutable mutableCopy = new ItemEnchantments.Mutable(itemEnchantments);
                    mutableCopy.removeIf((enchantment) -> enchantment.getKey() == ModEnchantments.REBIRTH);

                    Holder<Enchantment> rebornEnchantment = UtilFunctions.getEnchantmentReferenceIfPresent(event.getEntity().registryAccess(), ModEnchantments.REBORN);
                    if (rebornEnchantment != null) {
                        mutableCopy.set(rebornEnchantment, 1);
                    }

                    return mutableCopy.toImmutable();
                });
                newItemStack.setDamageValue(0);
                newItemStack.remove(DataComponents.REPAIR_COST);

                int freeSlot = player.getInventory().selected;
                boolean isOffhand = event.getHand() == InteractionHand.OFF_HAND;

                ScheduledRebirthTasks.EnqueueItemForRebirth(player, newItemStack, freeSlot, isOffhand);

            } else {
                player.sendSystemMessage(Component.literal("Your item failed to be reborn!").withStyle(ChatFormatting.RED));
            }
        }

    }
}
