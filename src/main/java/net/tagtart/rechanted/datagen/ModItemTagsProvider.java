package net.tagtart.rechanted.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.Tags;
import net.tagtart.rechanted.Rechanted;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {

        public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                        CompletableFuture<TagLookup<Block>> blockLookup, ExistingFileHelper existingFileHelper) {
                super(output, lookupProvider, blockLookup, Rechanted.MOD_ID, existingFileHelper);
        }

        public static final TagKey<Item> DIGGER_ITEM = TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "digger_items"));
        public static final TagKey<Item> OVERLOAD_ENCHANTABLE = TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "overload_enchantable"));
        public static final TagKey<Item> REACH_TOOLS = TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "reach_tools"));
        public static final TagKey<Item> REBIRTH_ENCHANTABLE = TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "rebirth_enchantable"));

        @Override
        protected void addTags(HolderLookup.Provider provider) {
                this.tag(DIGGER_ITEM)
                                .addTag(ItemTags.PICKAXES)
                                .addTag(ItemTags.AXES)
                                .addTag(ItemTags.SWORD_ENCHANTABLE)
                                .addTag(ItemTags.HOES);

                this.tag(OVERLOAD_ENCHANTABLE)
                                .addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                                .addOptional(ResourceLocation.withDefaultNamespace("elytra"));

                this.tag(REACH_TOOLS)
                                .addTag(ItemTags.PICKAXES)
                                .addTag(ItemTags.AXES)
                                .addTag(ItemTags.SHOVELS)
                                .addTag(ItemTags.HOES);

                this.tag(REBIRTH_ENCHANTABLE)
                                .addTag(Tags.Items.TOOLS)
                                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                                .addTag(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                                .addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                                .addTag(ItemTags.LEG_ARMOR_ENCHANTABLE)
                                .addTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                                .addOptional(ResourceLocation.withDefaultNamespace("elytra"));
        }
}
