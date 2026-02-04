package net.tagtart.rechantment.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.ModItems;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output,  ExistingFileHelper existingFileHelper) {
        super(output, Rechantment.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        rechantmentBookItem();
        basicItem(ModItems.CHANCE_GEM.get());
        basicItem(ModItems.RETURN_GEM.get());
        basicItem(ModItems.TASTY_GEM.get());
        basicItem(ModItems.WARP_GEM.get());
        basicItem(ModItems.CLONE_GEM.get());
        basicItem(ModItems.SMITHING_GEM.get());
    }

    private void rechantmentBookItem() {
        withExistingParent("simple", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/simple"));

        withExistingParent("unique", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/unique"));

        withExistingParent("ultimate", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/ultimate"));

        withExistingParent("legendary", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/legendary"));

        withExistingParent("elite", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/elite"));

    // TODO: change to modLoc, just copy pasted so thats why its not there
        withExistingParent(ModItems.RECHANTMENT_BOOK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/simple"))
                .override()
                .predicate(modLoc("book_rarity"), 1)
                .model(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/simple")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),2)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/unique")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),3)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/elite")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),4)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/ultimate")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),5)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/legendary")));

    }
}
