package net.tagtart.rechantment.event.enchantment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.util.UtilFunctions;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class ReachEnchantmentHandler {
    private static final ResourceLocation BLOCK_REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "reach_block");
    private static final double REACH_PER_LEVEL = 0.5D;

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return;
        if (!(event.getEntity() instanceof Player player)) return;

        updateReach(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateReach(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        updateReach(event.getEntity());
    }

    private static void updateReach(Player player) {
        int reachLevel = Math.max(
                getReachLevel(player, player.getMainHandItem()),
                getReachLevel(player, player.getOffhandItem())
        );

        AttributeInstance blockReach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockReach == null) return;

        blockReach.removeModifier(BLOCK_REACH_MODIFIER_ID);

        if (reachLevel <= 0) return;

        double bonus = reachLevel * REACH_PER_LEVEL;
        blockReach.addTransientModifier(new AttributeModifier(
                BLOCK_REACH_MODIFIER_ID,
                bonus,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static int getReachLevel(Player player, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return UtilFunctions.getEnchantmentFromItem("rechantment:reach", stack, player.registryAccess());
    }
}
