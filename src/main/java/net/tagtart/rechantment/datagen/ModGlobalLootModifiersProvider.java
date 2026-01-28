package net.tagtart.rechantment.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.loot.ReplaceItemModifier;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Rechantment.MOD_ID);
    }

    @Override
    protected void start() {
        add("replace_enchanted_items", new ReplaceItemModifier(new LootItemCondition[]{}));
    }
}
