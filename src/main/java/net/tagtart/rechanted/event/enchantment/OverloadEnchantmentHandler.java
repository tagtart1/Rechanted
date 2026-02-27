package net.tagtart.rechanted.event.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.enchantment.ModEnchantments;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class OverloadEnchantmentHandler {

    private static final ResourceLocation OVERLOAD_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "overload_max_health");
    private static final float OVERLOAD_HEALTH_PER_LEVEL = 2.0f; // +2 HP (1 heart) per level

    @SubscribeEvent
    public static void onArmorEquip(LivingEquipmentChangeEvent event) {
        // Check if the changed equipment is an armor piece
        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack newArmor = event.getTo();
        ItemStack oldArmor = event.getFrom();

        HolderLookup.Provider registryAccess = player.level().registryAccess();
        Holder<Enchantment> overloadHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(ModEnchantments.OVERLOAD))
                .orElse(null);

        if (overloadHolder == null) return;

        boolean overloadJustEquipped = newArmor.getEnchantmentLevel(overloadHolder) > 0 && !ItemStack.isSameItem(newArmor, oldArmor);

        // Calculate total health increase from all armor pieces
        float newMaxHealthIncrease = 0f;
        for (ItemStack armor : player.getInventory().armor) {
            int overloadLevel = armor.getEnchantmentLevel(overloadHolder);
            if (overloadLevel > 0) {
                newMaxHealthIncrease += overloadLevel * OVERLOAD_HEALTH_PER_LEVEL;
            }
        }

        // Remove old modifier and apply new one with updated value
        AttributeModifier overloadModifier = new AttributeModifier(
                OVERLOAD_HEALTH_MODIFIER_ID,
                newMaxHealthIncrease,
                AttributeModifier.Operation.ADD_VALUE
        );

        AttributeInstance currentMaxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (currentMaxHealthAttribute == null) return;

        // Remove old modifier if present
        if (currentMaxHealthAttribute.hasModifier(OVERLOAD_HEALTH_MODIFIER_ID)) {
            currentMaxHealthAttribute.removeModifier(OVERLOAD_HEALTH_MODIFIER_ID);
        }

        // Add new modifier if health increase > 0
        if (newMaxHealthIncrease > 0f) {
            currentMaxHealthAttribute.addPermanentModifier(overloadModifier);
        }

        // Play sound when equipping armor with overload
        if (player.getHealth() <= player.getMaxHealth() && newMaxHealthIncrease > 0f && overloadJustEquipped) {
            player.level().playSound(null, player.getOnPos(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.15f, 1f);
        } else {
            // Reduce health if player has more than new max (when unequipping overload)
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
                player.level().playSound(null, player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z, SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }


}
