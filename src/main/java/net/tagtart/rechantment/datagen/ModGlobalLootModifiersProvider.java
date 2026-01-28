package net.tagtart.rechantment.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.loot.FishingNerfLootModifier;
import net.tagtart.rechantment.loot.ReplaceEnchantedLootModifier;
import net.tagtart.rechantment.loot.ReplaceVanillaEnchantedBookModifier;
import net.tagtart.rechantment.loot.RemoveMendingLootModifier;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Rechantment.MOD_ID);
    }

    @Override
    protected void start() {
        add("replace_vanilla_enchanted_books", new ReplaceVanillaEnchantedBookModifier(new LootItemCondition[]{}));
        add("replace_enchanted_loot", new ReplaceEnchantedLootModifier(new LootItemCondition[]{}));
        add("fishing_nerf_loot", new FishingNerfLootModifier(new LootItemCondition[]{}));
        add("remove_mending_loot", new RemoveMendingLootModifier(new LootItemCondition[]{}));
    }
}
