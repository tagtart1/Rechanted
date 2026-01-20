  package net.tagtart.rechantment.event;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.effect.ModEffects;
import net.tagtart.rechantment.enchantment.ModEnchantments;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BerserkActivationHandler {

    private static final float BERSERK_ACTIVATION_THRESHOLD = 12f; // 6 hearts
    private static final int BERSERK_DURATION = 15 * 20; // 15 seconds in ticks

    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        // Only handle player damage on server side
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Check if player already has berserk effect or cooldown
        if (player.hasEffect(ModEffects.BERSERK_EFFECT)) return;
        if (player.hasEffect(ModEffects.BERSERK_COOLDOWN_EFFECT)) return;

        // Calculate missing health after damage
        float missingHealth = player.getMaxHealth() - player.getHealth();

        // Check if missing health meets threshold (6 hearts = 12 health)
        if (missingHealth < BERSERK_ACTIVATION_THRESHOLD) return;

        // Check if player has an item with berserk enchantment
        if (!hasBerserkEnchantment(player)) return;

        // Activate berserk effect
        MobEffectInstance berserkEffect = new MobEffectInstance(
                ModEffects.BERSERK_EFFECT,
                BERSERK_DURATION,
                0,
                false, // ambient
                true,  // visible particles
                true   // show icon
        );
        player.addEffect(berserkEffect);

        // Display action bar message
        player.displayClientMessage(Component.literal("Berserk!"), true);

        Rechantment.LOGGER.info("Berserk activated for {} (missing {} health)", 
                player.getName().getString(), missingHealth);
    }

    @SubscribeEvent
    public static void onBerserkCooldownExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        
        // Only handle on server side
        if (entity.level().isClientSide()) return;
        
        // Check if the expired effect is berserk cooldown
        if (!event.getEffectInstance().getEffect().equals(ModEffects.BERSERK_COOLDOWN_EFFECT)) return;
        
        // Only auto-activate for players
        if (!(entity instanceof ServerPlayer player)) return;
        
        // Check if player already has berserk effect (shouldn't happen, but safety check)
        if (player.hasEffect(ModEffects.BERSERK_EFFECT)) return;
        
        // Calculate missing health
        float missingHealth = player.getMaxHealth() - player.getHealth();
        
        // Check if missing health still meets threshold
        if (missingHealth < BERSERK_ACTIVATION_THRESHOLD) {
            Rechantment.LOGGER.info("Berserk cooldown expired for {} but health recovered (missing {} health)", 
                    player.getName().getString(), missingHealth);
            return;
        }
        
        // Check if player still has an item with berserk enchantment
        if (!hasBerserkEnchantment(player)) {
            Rechantment.LOGGER.info("Berserk cooldown expired for {} but no Berserk enchantment found", 
                    player.getName().getString());
            return;
        }
        
        // Auto-activate berserk effect
        MobEffectInstance berserkEffect = new MobEffectInstance(
                ModEffects.BERSERK_EFFECT,
                BERSERK_DURATION,
                0,
                false, // ambient
                true,  // visible particles
                true   // show icon
        );
        player.addEffect(berserkEffect);
        
        // Display action bar message
        player.displayClientMessage(Component.literal("Berserk has activated!"), true);
        
        Rechantment.LOGGER.info("Berserk auto-activated for {} after cooldown (missing {} health)", 
                player.getName().getString(), missingHealth);
    }

    /**
     * Checks if the player has any item with the Berserk enchantment
     */
    private static boolean hasBerserkEnchantment(Player player) {
        HolderLookup.Provider registryAccess = player.level().registryAccess();
        
        // Get the berserk enchantment holder
        Holder<Enchantment> berserkHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(ModEnchantments.BERSERK))
                .orElse(null);
        
        if (berserkHolder == null) return false;

        // Check all inventory slots and offhand slot
        for (ItemStack stack : player.getInventory().items) {
            if (hasBerserkOnItem(stack, berserkHolder)) return true;
        }
    
        if (hasBerserkOnItem(player.getInventory().offhand.get(0), berserkHolder)) return true;

        return false;
    }

    /**
     * Checks if a specific ItemStack has the Berserk enchantment
     */
    private static boolean hasBerserkOnItem(ItemStack stack, Holder<Enchantment> berserkHolder) {
        if (stack.isEmpty()) return false;
        return stack.getEnchantmentLevel(berserkHolder) > 0;
    }
}

