package net.tagtart.rechantment.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        super.buildRecipes(recipeOutput);

//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.STONE)
//                .pattern(" B ")
//                .pattern("DOD")
//                .pattern("OOO")
//                .define('B', Items.BOOK)
//                .define('D', Items.DIAMOND)
//                .define('O', Items.OBSIDIAN).unlockedBy("has_book", has(Items.BOOK))
//                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("minecraft", "enchanting_table"));
    }
}
