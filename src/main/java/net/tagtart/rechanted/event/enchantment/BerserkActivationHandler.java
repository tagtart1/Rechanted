package net.tagtart.rechanted.event.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.effect.ModEffects;
import net.tagtart.rechanted.enchantment.ModEnchantments;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
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
                false, // visible particles
                true   // show icon
        );
        player.addEffect(berserkEffect);

        announceBerserkProc(player);

        Rechanted.LOGGER.info("Berserk activated for {} (missing {} health)", 
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
            Rechanted.LOGGER.info("Berserk cooldown expired for {} but health recovered (missing {} health)", 
                    player.getName().getString(), missingHealth);
            return;
        }
        
        // Check if player still has an item with berserk enchantment
        if (!hasBerserkEnchantment(player)) {
            Rechanted.LOGGER.info("Berserk cooldown expired for {} but no Berserk enchantment found", 
                    player.getName().getString());
            return;
        }
        
        // Auto-activate berserk effect
        MobEffectInstance berserkEffect = new MobEffectInstance(
                ModEffects.BERSERK_EFFECT,
                BERSERK_DURATION,
                0,
                false, // ambient
                false, // visible particles
                true   // show icon
        );
        player.addEffect(berserkEffect);

        announceBerserkProc(player);
        
        Rechanted.LOGGER.info("Berserk auto-activated for {} after cooldown (missing {} health)", 
                player.getName().getString(), missingHealth);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!player.hasEffect(ModEffects.BERSERK_EFFECT)) {
            return;
        }

        spawnBerserkParticles(level, player);
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

    private static void spawnBerserkParticles(ServerLevel level, Player player) {
        double centerX = player.getX();
        double centerY = player.getY() + player.getBbHeight() - .25;
        double centerZ = player.getZ();
        double randomX = level.random.nextDouble() * 0.2D;
        double randomZ = level.random.nextDouble() * 0.2D;

        if (level.random.nextBoolean()) {
            randomX = -randomX;
        }
        if (level.random.nextBoolean()) {
            randomZ = -randomZ;
        }

        level.sendParticles(ParticleTypes.CRIMSON_SPORE, centerX + randomX, centerY, centerZ + randomZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void announceBerserkProc(ServerPlayer player) {
        player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.displayClientMessage(Component.literal("Berserk!").withStyle(ChatFormatting.DARK_RED), true);
    }
}

