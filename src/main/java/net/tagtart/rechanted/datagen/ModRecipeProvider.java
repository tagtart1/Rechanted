package net.tagtart.rechanted.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.tagtart.rechanted.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        netheriteUpgrade(recipeOutput, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, RecipeCategory.COMBAT, "rechanted:netherite_sword_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, RecipeCategory.TOOLS, "rechanted:netherite_pickaxe_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_AXE, Items.NETHERITE_AXE, RecipeCategory.TOOLS, "rechanted:netherite_axe_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL, RecipeCategory.TOOLS, "rechanted:netherite_shovel_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_HOE, Items.NETHERITE_HOE, RecipeCategory.TOOLS, "rechanted:netherite_hoe_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, RecipeCategory.COMBAT, "rechanted:netherite_helmet_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE, RecipeCategory.COMBAT, "rechanted:netherite_chestplate_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS, RecipeCategory.COMBAT, "rechanted:netherite_leggings_smithing_gem");
        netheriteUpgrade(recipeOutput, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS, RecipeCategory.COMBAT, "rechanted:netherite_boots_smithing_gem");
    }

    private void netheriteUpgrade(RecipeOutput recipeOutput, Item base, Item result, RecipeCategory category, String recipeId) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.SMITHING_GEM),
                        Ingredient.of(base),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        category,
                        result
                )
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(recipeOutput, recipeId);
    }
}
