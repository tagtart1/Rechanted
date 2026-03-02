package net.tagtart.rechanted.event.enchantment;

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
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.component.ModDataComponents;
import net.tagtart.rechanted.enchantment.ModEnchantments;
import net.tagtart.rechanted.event.TickDelayedTasks;
import net.tagtart.rechanted.util.UtilFunctions;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class RebirthEnchantmentHandler {

    @SubscribeEvent
    public static void onItemBreak(PlayerDestroyItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Rechanted.LOGGER.info(
                "Rebirth onItemBreak: player={}, item={}, hand={}",
                player.getName().getString(),
                event.getOriginal(),
                event.getHand()
        );

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
            //Rechanted.LOGGER.info("Rebirth armor guard: ignoring non-armor slot {}", slot);
            return;
        }

        ItemStack oldStack = event.getFrom();
        ItemStack newStack = event.getTo();
        if (oldStack.isEmpty() || !newStack.isEmpty()) {
            Rechanted.LOGGER.info(
                    "Rebirth armor guard: slot={} rejected by stack transition, fromEmpty={}, toEmpty={}, from={}, to={}",
                    slot,
                    oldStack.isEmpty(),
                    newStack.isEmpty(),
                    oldStack,
                    newStack
            );
            return;
        }
        // LivingEquipmentChangeEvent can fire with pre-break durability (maxDamage - 1) when the slot is cleared.
        // Accept both exact max and one-before-max so armor break reliably triggers rebirth.
        int damage = oldStack.getDamageValue();
        int maxDamage = oldStack.getMaxDamage();
        int breakThreshold = Math.max(0, maxDamage - 1);
        if (!oldStack.isDamageableItem() || damage < breakThreshold) {
            Rechanted.LOGGER.info(
                    "Rebirth armor guard: slot={} rejected by damage check, damageable={}, damage={}, maxDamage={}, threshold={}, item={}",
                    slot,
                    oldStack.isDamageableItem(),
                    damage,
                    maxDamage,
                    breakThreshold,
                    oldStack
            );
            return;
        }

        Rechanted.LOGGER.info(
                "Rebirth armor accepted: player={}, slot={}, item={}, damage={}, maxDamage={}",
                player.getName().getString(),
                slot,
                oldStack,
                oldStack.getDamageValue(),
                oldStack.getMaxDamage()
        );

        tryRebirth(player, oldStack, -1, false, slot);
    }

    private static void tryRebirth(ServerPlayer player, ItemStack itemStack, int inventorySlot, boolean isOffhand, EquipmentSlot equipmentSlot) {
        int rebirthEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechanted:rebirth", itemStack, player.registryAccess());
        if (rebirthEnchantmentLevel == 0) {
            Rechanted.LOGGER.info(
                    "Rebirth tryRebirth guard: missing enchantment, player={}, item={}, slot={}, inventorySlot={}, offhand={}",
                    player.getName().getString(),
                    itemStack,
                    equipmentSlot,
                    inventorySlot,
                    isOffhand
            );
            return;
        }

        Rechanted.LOGGER.info(
                "Rebirth tryRebirth accepted: player={}, item={}, slot={}, inventorySlot={}, offhand={}, level={}",
                player.getName().getString(),
                itemStack,
                equipmentSlot,
                inventorySlot,
                isOffhand,
                rebirthEnchantmentLevel
        );

        ItemStack newItemStack = itemStack.copy();
        newItemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {
            ItemEnchantments.Mutable mutableCopy = new ItemEnchantments.Mutable(itemEnchantments);
            mutableCopy.removeIf((enchantment) -> enchantment.getKey() == ModEnchantments.REBIRTH);
            return mutableCopy.toImmutable();
        });
        newItemStack.set(ModDataComponents.REBORN, true);
        newItemStack.setDamageValue(0);
        newItemStack.remove(DataComponents.REPAIR_COST);

        Rechanted.LOGGER.info(
                "Rebirth enqueue: player={}, rebornItem={}, slot={}, inventorySlot={}, offhand={}",
                player.getName().getString(),
                newItemStack,
                equipmentSlot,
                inventorySlot,
                isOffhand
        );

        TickDelayedTasks.EnqueueItemForRebirth(player, newItemStack, inventorySlot, isOffhand, equipmentSlot);
    }
}
