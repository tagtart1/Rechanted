package net.tagtart.rechanted.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.item.ModItems;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output,  ExistingFileHelper existingFileHelper) {
        super(output, Rechanted.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        rechantedBookItem();
        basicItem(ModItems.CHANCE_GEM.get());
        basicItem(ModItems.SHINY_CHANCE_GEM.get());
        basicItem(ModItems.RETURN_GEM.get());
        basicItem(ModItems.TASTY_GEM.get());
        basicItem(ModItems.WARP_GEM.get());
        basicItem(ModItems.LUCKY_GEM.get());
        basicItem(ModItems.CLONE_GEM.get());
        basicItem(ModItems.SMITHING_GEM.get());
        basicItem(ModItems.MYSTERIOUS_BOOK.get());
        basicItem(ModItems.DUSTY_MYSTERIOUS_BOOK.get());

        genericIconItem(ModItems.LEGENDARY_PULL_ICON.getId().getPath(), "item/legendary");
        genericIconItem(ModItems.ARCHMAGE_ICON.getId().getPath(), "item/advancement_archmage");
        genericIconItem(ModItems.CHRONICLER_ICON.getId().getPath(), "item/advancement_chronicler");
    }

    private void rechantedBookItem() {
        withExistingParent("dusty", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/dusty"));

        withExistingParent("simple", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/simple"));

        withExistingParent("unique", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/unique"));

        withExistingParent("ultimate", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/ultimate"));

        withExistingParent("legendary", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/legendary"));

        withExistingParent("elite", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/elite"));

    // TODO: change to modLoc, just copy pasted so thats why its not there
        withExistingParent(ModItems.RECHANTED_BOOK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/dusty"))
                .override()
                .predicate(modLoc("book_rarity"), 0)
                .model(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/dusty")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"), 1)
                .model(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/simple")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),2)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/unique")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),3)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/elite")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),4)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/ultimate")))
                .end()
                .override()
                .predicate(modLoc("book_rarity"),5)
                .model(new ModelFile.UncheckedModelFile( ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "item/legendary")));

    }

    private void genericIconItem(String modelPath, String texturePath) {
        withExistingParent(modelPath, mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, texturePath));
    }
}
