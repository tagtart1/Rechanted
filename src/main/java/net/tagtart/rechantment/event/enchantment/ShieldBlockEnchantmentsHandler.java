package net.tagtart.rechantment.event.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.ModEnchantments;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class ShieldBlockEnchantmentsHandler {

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        DamageSource source = event.getDamageSource();
        Entity attacker = source.getEntity();
        ItemStack shield = player.getUseItem();

        if(!(shield.getItem() instanceof ShieldItem)) return;

        HolderLookup.Provider registryAccess = player.level().registryAccess();

        // Get the bash enchantment level
        Holder<Enchantment> bashHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(ModEnchantments.BASH))
                .orElse(null);

        // Handle bash enchantment
        if (bashHolder != null) {
            int bashLevel = shield.getEnchantmentLevel(bashHolder);

            if (bashLevel > 0 && attacker != null) {
                // Don't bash projectiles, only melee attackers
                if (!(source.getDirectEntity() instanceof Projectile)) {
                    // Calculate knockback direction (away from player)
                    double d0 = attacker.getX() - player.getX();
                    double d1 = attacker.getZ() - player.getZ();
                    Vec2 toAttacker = new Vec2((float) d0, (float) d1);
                    toAttacker = toAttacker.normalized();
                    toAttacker = toAttacker.scale(1.15f);

                    // Apply knockback
                    if (attacker.isPushable()) {
                        attacker.push(toAttacker.x, 0.4f, toAttacker.y);
                    }
                }
            }
        }

        // Get the courage enchantment level
        Holder<Enchantment> courageHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(ModEnchantments.COURAGE))
                .orElse(null);

        // Handle courage enchantment
        if (courageHolder != null) {
            int courageLevel = shield.getEnchantmentLevel(courageHolder);

            if (courageLevel > 0) {
                int SHIELD_COURAGE_SPEED_DURATION = 40; // Speed in ticks (2 seconds)
                MobEffectInstance speedEffect = new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        SHIELD_COURAGE_SPEED_DURATION,
                        courageLevel - 1
                );
                player.addEffect(speedEffect);
            }
        }

    }
}
