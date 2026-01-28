package net.tagtart.rechantment.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tagtart.rechantment.Rechantment;

import java.util.function.Supplier;

public class ModLootModifiers {

    public static DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Rechantment.MOD_ID
    );

    public static final Supplier<MapCodec<ReplaceItemModifier>> REPLACE_ITEM_MODIFER =
            LOOT_MODIFIERS.register("replace_item_modifer", () -> ReplaceItemModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}
