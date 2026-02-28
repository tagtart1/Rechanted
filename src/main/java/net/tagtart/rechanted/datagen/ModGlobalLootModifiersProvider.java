package net.tagtart.rechanted.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.loot.*;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Rechanted.MOD_ID);
    }

    @Override
    protected void start() {
        add("replace_vanilla_enchanted_books", new ReplaceVanillaEnchantedBookModifier(new LootItemCondition[]{}));
        add("fishing_nerf_loot", new FishingNerfLootModifier(new LootItemCondition[]{}));
        add("replace_enchanted_loot", new ReplaceEnchantedLootModifier(new LootItemCondition[]{}));
        add("remove_mending_loot", new RemoveMendingLootModifier(new LootItemCondition[]{}));
        add("add_dusty_mysterious_book_to_fishing", new AddDustyMysteriousBookToFishingLootModifier(new LootItemCondition[]{}));
    }
}
