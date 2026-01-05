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
    }


// TODO: make this dynamic now based on actual enchantment rarities, refer to ModItemProperties not here
    private void rechantmentBookItem() {
        withExistingParent("unique", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/unique"));

        withExistingParent("ultimate", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/ultimate"));


        withExistingParent(ModItems.RECHANTMENT_BOOK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/simple"))
                .override()
                .predicate(modLoc("book_rarity"), 1)
                .model(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "item/unique")));

    }
}
