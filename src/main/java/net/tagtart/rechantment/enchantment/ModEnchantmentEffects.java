package net.tagtart.rechantment.enchantment;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.custom.LightningStrikerEnchantmentEffect;

public class ModEnchantmentEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_ENCHANTMENT_EFFECTS = DeferredRegister
            .create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Rechantment.MOD_ID);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> LIGHTNING_STRIKER = ENTITY_ENCHANTMENT_EFFECTS
            .register("lightning_striker", () -> LightningStrikerEnchantmentEffect.CODEC);

    public static void register(IEventBus eventBus) {
        ENTITY_ENCHANTMENT_EFFECTS.register(eventBus);
    }
}
