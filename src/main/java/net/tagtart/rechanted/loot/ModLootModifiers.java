package net.tagtart.rechanted.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tagtart.rechanted.Rechanted;

import java.util.function.Supplier;

public class ModLootModifiers {

    public static DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Rechanted.MOD_ID
    );

    public static final Supplier<MapCodec<ReplaceVanillaEnchantedBookModifier>> REPLACE_VANILLA_ENCHANTED_BOOK =
            LOOT_MODIFIERS.register("replace_vanilla_enchanted_book", () -> ReplaceVanillaEnchantedBookModifier.CODEC);

    public static final Supplier<MapCodec<ReplaceEnchantedLootModifier>> REPLACE_ENCHANTED_LOOT =
            LOOT_MODIFIERS.register("replace_enchanted_loot", () -> ReplaceEnchantedLootModifier.CODEC);

    public static final Supplier<MapCodec<FishingNerfLootModifier>> FISHING_NERF_LOOT =
            LOOT_MODIFIERS.register("fishing_nerf_loot", () -> FishingNerfLootModifier.CODEC);

    public static final Supplier<MapCodec<RemoveMendingLootModifier>> REMOVE_MENDING_LOOT =
            LOOT_MODIFIERS.register("remove_mending_loot", () -> RemoveMendingLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}
