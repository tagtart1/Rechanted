package net.tagtart.rechantment.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.HashSet;

// There are a lot of weird edge cases when items break in different scenarios.
// All this does is makes an item rebirth occur one tick after the item breaks.
@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class TickDelayedTasks {

    public static final HashSet<TickDelayedTask> enqueuedTasks = new HashSet<>();

    public static void EnqueueItemForRebirth(ServerPlayer player, ItemStack rebornItem, int inventorySlot, boolean isOffhand) {
        enqueuedTasks.add(new EnqueuedRebirthEvent(player, rebornItem, inventorySlot, isOffhand));
    }

    // NOTE: If rebirth isn't working properly, can try making this a ServerTickEvent.Pre.
    // ServerTickEvent is an abstract class now and I picked ServerTickEvent.Post arbitrarily for now.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (TickDelayedTask task : enqueuedTasks) {
            task.ticksRemaining--;
            if (task.ticksRemaining <= 0) {
                task.onTicksDelayElapsed();
                enqueuedTasks.remove(task);
            }
        }
    }

    public abstract static class TickDelayedTask {
        public int ticksRemaining;

        public abstract void onTicksDelayElapsed();

        protected TickDelayedTask(int ticksRemaining) {
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static class EnqueuedRebirthEvent extends TickDelayedTask {

        public boolean isOffhand;
        public int inventorySlot;
        public ServerPlayer player;
        public ItemStack rebornItem;

        public EnqueuedRebirthEvent(ServerPlayer player, ItemStack rebornItem, int inventorySlot, boolean isOffhand) {
            super(1);
            this.inventorySlot = inventorySlot;
            this.player = player;
            this.rebornItem = rebornItem;
            this.isOffhand = isOffhand;
        }

        @Override
        public void onTicksDelayElapsed() {
            UtilFunctions.triggerRebirthClientEffects(player, (ServerLevel) player.level(), this.rebornItem.getItem().getDefaultInstance());

            if (this.isOffhand) {
                player.getInventory().offhand.set(0, this.rebornItem);
            }
            else if (this.rebornItem.getItem() instanceof ArmorItem armorItem) {
                EquipmentSlot armorSlot = armorItem.getEquipmentSlot();
                player.setItemSlot(armorSlot, this.rebornItem);
            }
            else if (this.inventorySlot != -1) {
                player.getInventory().setItem(this.inventorySlot, this.rebornItem);
            }
            else {
                player.drop(this.rebornItem, false); // Drop the item if the slot is occupied
            }
        }
    }
}