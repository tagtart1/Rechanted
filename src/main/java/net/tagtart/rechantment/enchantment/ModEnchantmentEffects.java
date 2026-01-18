package net.tagtart.rechantment.enchantment;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.custom.*;

public class ModEnchantmentEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_ENCHANTMENT_EFFECTS = DeferredRegister
            .create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Rechantment.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends EnchantmentValueEffect>> VALUE_ENCHANTMENT_EFFECTS = DeferredRegister
            .create(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, Rechantment.MOD_ID);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> THUNDER_STRIKE = ENTITY_ENCHANTMENT_EFFECTS
            .register("thunder_strike", () -> ThunderStrikeEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> HELLS_FURY = ENTITY_ENCHANTMENT_EFFECTS
            .register("hells_fury", () -> HellsFuryEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> VOIDS_BANE = ENTITY_ENCHANTMENT_EFFECTS
            .register("voids_bane", () -> VoidsBaneEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> ICE_ASPECT = ENTITY_ENCHANTMENT_EFFECTS
            .register("ice_aspect", () -> IceAspectEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentValueEffect>> BASH = VALUE_ENCHANTMENT_EFFECTS
            .register("bash", () -> BashEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentValueEffect>> COURAGE = VALUE_ENCHANTMENT_EFFECTS
            .register("courage", () -> CourageEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentValueEffect>> WISDOM = VALUE_ENCHANTMENT_EFFECTS
            .register("wisdom", () -> WisdomEnchantmentEffect.CODEC);

    public static final Supplier<MapCodec<? extends EnchantmentValueEffect>> INQUISITIVE = VALUE_ENCHANTMENT_EFFECTS
            .register("inquisitive", () -> InquisitiveEnchantmentEffect.CODEC);

    public static void register(IEventBus eventBus) {
        VALUE_ENCHANTMENT_EFFECTS.register(eventBus);
        ENTITY_ENCHANTMENT_EFFECTS.register(eventBus);
    }
}
