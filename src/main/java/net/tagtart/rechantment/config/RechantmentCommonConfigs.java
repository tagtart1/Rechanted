package net.tagtart.rechantment.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class RechantmentCommonConfigs {

        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec SPEC;

        // Simple tier configs.
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_0_KEY;
        public static final ModConfigSpec.IntValue RARITY_0_COLOR;
        public static final ModConfigSpec.IntValue RARITY_0_EXP_COST;
        public static final ModConfigSpec.IntValue RARITY_0_WORLD_SPAWN_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_0_MIN_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_0_MAX_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_0_FORCED_BOOK_BREAKS;
        public static final ModConfigSpec.IntValue RARITY_0_FORCED_FLOOR_BREAKS;
        public static final ModConfigSpec.DoubleValue RARITY_0_BOOK_BREAK_CHANCE;
        public static final ModConfigSpec.DoubleValue RARITY_0_FLOOR_BREAK_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_0_REQUIRED_BOOKSHELVES;
        public static final ModConfigSpec.IntValue RARITY_0_REQUIRED_LAPIS;
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_0_FLOOR_BLOCK_TYPE;
        public static final ModConfigSpec.DoubleValue RARITY_0_BONUS_ITEM_ROLL_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_0_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_0_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_0_BONUS_ITEM_RARE_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_0_GRINDSTONE_XP_MIN;
        public static final ModConfigSpec.IntValue RARITY_0_GRINDSTONE_XP_MAX;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_0_ENCHANTMENTS;

        // Unique tier configs.
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_1_KEY;
        public static final ModConfigSpec.IntValue RARITY_1_COLOR;
        public static final ModConfigSpec.IntValue RARITY_1_EXP_COST;
        public static final ModConfigSpec.IntValue RARITY_1_WORLD_SPAWN_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_1_MIN_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_1_MAX_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_1_FORCED_BOOK_BREAKS;
        public static final ModConfigSpec.IntValue RARITY_1_FORCED_FLOOR_BREAKS;
        public static final ModConfigSpec.DoubleValue RARITY_1_BOOK_BREAK_CHANCE;
        public static final ModConfigSpec.DoubleValue RARITY_1_FLOOR_BREAK_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_1_REQUIRED_BOOKSHELVES;
        public static final ModConfigSpec.IntValue RARITY_1_REQUIRED_LAPIS;
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_1_FLOOR_BLOCK_TYPE;
        public static final ModConfigSpec.DoubleValue RARITY_1_BONUS_ITEM_ROLL_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_1_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_1_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_1_BONUS_ITEM_RARE_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_1_GRINDSTONE_XP_MIN;
        public static final ModConfigSpec.IntValue RARITY_1_GRINDSTONE_XP_MAX;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_1_ENCHANTMENTS;

        // Elite tier configs.
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_2_KEY;
        public static final ModConfigSpec.IntValue RARITY_2_COLOR;
        public static final ModConfigSpec.IntValue RARITY_2_EXP_COST;
        public static final ModConfigSpec.IntValue RARITY_2_WORLD_SPAWN_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_2_MIN_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_2_MAX_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_2_FORCED_BOOK_BREAKS;
        public static final ModConfigSpec.IntValue RARITY_2_FORCED_FLOOR_BREAKS;
        public static final ModConfigSpec.DoubleValue RARITY_2_BOOK_BREAK_CHANCE;
        public static final ModConfigSpec.DoubleValue RARITY_2_FLOOR_BREAK_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_2_REQUIRED_BOOKSHELVES;
        public static final ModConfigSpec.IntValue RARITY_2_REQUIRED_LAPIS;
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_2_FLOOR_BLOCK_TYPE;
        public static final ModConfigSpec.DoubleValue RARITY_2_BONUS_ITEM_ROLL_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_2_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_2_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_2_BONUS_ITEM_RARE_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_2_GRINDSTONE_XP_MIN;
        public static final ModConfigSpec.IntValue RARITY_2_GRINDSTONE_XP_MAX;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_2_ENCHANTMENTS;

        // Ultimate tier configs.
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_3_KEY;
        public static final ModConfigSpec.IntValue RARITY_3_COLOR;
        public static final ModConfigSpec.IntValue RARITY_3_EXP_COST;
        public static final ModConfigSpec.IntValue RARITY_3_WORLD_SPAWN_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_3_MIN_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_3_MAX_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_3_FORCED_BOOK_BREAKS;
        public static final ModConfigSpec.IntValue RARITY_3_FORCED_FLOOR_BREAKS;
        public static final ModConfigSpec.DoubleValue RARITY_3_BOOK_BREAK_CHANCE;
        public static final ModConfigSpec.DoubleValue RARITY_3_FLOOR_BREAK_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_3_REQUIRED_BOOKSHELVES;
        public static final ModConfigSpec.IntValue RARITY_3_REQUIRED_LAPIS;
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_3_FLOOR_BLOCK_TYPE;
        public static final ModConfigSpec.DoubleValue RARITY_3_BONUS_ITEM_ROLL_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_3_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_3_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_3_BONUS_ITEM_RARE_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_3_GRINDSTONE_XP_MIN;
        public static final ModConfigSpec.IntValue RARITY_3_GRINDSTONE_XP_MAX;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_3_ENCHANTMENTS;

        // Legendary tier configs.
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_4_KEY;
        public static final ModConfigSpec.IntValue RARITY_4_COLOR;
        public static final ModConfigSpec.IntValue RARITY_4_EXP_COST;
        public static final ModConfigSpec.IntValue RARITY_4_WORLD_SPAWN_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_4_MIN_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_4_MAX_SUCCESS;
        public static final ModConfigSpec.IntValue RARITY_4_FORCED_BOOK_BREAKS;
        public static final ModConfigSpec.IntValue RARITY_4_FORCED_FLOOR_BREAKS;
        public static final ModConfigSpec.DoubleValue RARITY_4_BOOK_BREAK_CHANCE;
        public static final ModConfigSpec.DoubleValue RARITY_4_FLOOR_BREAK_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_4_REQUIRED_BOOKSHELVES;
        public static final ModConfigSpec.IntValue RARITY_4_REQUIRED_LAPIS;
        public static final ModConfigSpec.ConfigValue<? extends String> RARITY_4_FLOOR_BLOCK_TYPE;
        public static final ModConfigSpec.DoubleValue RARITY_4_BONUS_ITEM_ROLL_CHANCE;
        public static final ModConfigSpec.IntValue RARITY_4_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_4_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_4_BONUS_ITEM_RARE_GEM_POOL_WEIGHT;
        public static final ModConfigSpec.IntValue RARITY_4_GRINDSTONE_XP_MIN;
        public static final ModConfigSpec.IntValue RARITY_4_GRINDSTONE_XP_MAX;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_4_ENCHANTMENTS;

        public static final ModConfigSpec.ConfigValue<? extends String> GRINDSTONE_RESULT_ITEM;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> COMMON_GEM_POOL;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> RARE_GEM_POOL;
        public static final ModConfigSpec.DoubleValue SHINY_CHANCE_GEM_BREAK_CHANCE;

        public static final ModConfigSpec.ConfigValue<List<? extends String>> ANNOUNCEMENT_ENCHANTMENTS;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_CHANCE_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_SHINY_CHANCE_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_RETURN_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_TASTY_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_WARP_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_LUCKY_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_CLONE_GEM;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> ANNOUNCE_SMITHING_GEM;

        public static final ModConfigSpec.ConfigValue<? extends Boolean> REMOVE_MENDING_ENABLED;

        public static final ModConfigSpec.ConfigValue<? extends Boolean> REPLACE_ENCHANTED_LOOT;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> EXCLUDE_LOWER_TIER_LOOT;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> NERF_FISHING_LOOT;
        public static final ModConfigSpec.ConfigValue<? extends Boolean> MODIFY_VILLAGER_TRADES;
        public static final ModConfigSpec.IntValue VILLAGER_MYSTERIOUS_BOOK_EMERALD_COST;
        // Fortune nerf configs
        public static final ModConfigSpec.ConfigValue<? extends Boolean> FORTUNE_NERF_ENABLED;
        public static final ModConfigSpec.DoubleValue FORTUNE_1_CHANCE;
        public static final ModConfigSpec.DoubleValue FORTUNE_2_CHANCE;
        public static final ModConfigSpec.DoubleValue FORTUNE_3_CHANCE;

        static {

                // Simple rarity builder default config
                BUILDER.translation("config.rechantment.book_rarities.title").comment("Configs for Each Book Rarity")
                                .push("Book Rarities");

                // Simple rarity properties.
                BUILDER.translation("config.rechantment.simple.name").push("Simple");
                RARITY_0_KEY = BUILDER.translation("config.rechantment.simple.key.title").define("key", "simple");
                RARITY_0_COLOR = BUILDER.translation("config.rechantment.simple.color.title").defineInRange("color",
                                11184810, 0, Integer.MAX_VALUE);
                RARITY_0_EXP_COST = BUILDER.translation("config.rechantment.simple.exp_cost.title")
                                .defineInRange("exp_cost", 75, 0, Integer.MAX_VALUE);
                RARITY_0_WORLD_SPAWN_WEIGHT = BUILDER.translation("config.rechantment.simple.world_spawn_weight.title")
                                .defineInRange("world_spawn_weight", 40, 0, Integer.MAX_VALUE);
                RARITY_0_MIN_SUCCESS = BUILDER.translation("config.rechantment.simple.min_success.title")
                                .defineInRange("min_success", 25, 0, 100);
                RARITY_0_MAX_SUCCESS = BUILDER.translation("config.rechantment.simple.max_success.title")
                                .defineInRange("max_success", 90, 0, 100);
                RARITY_0_FORCED_BOOK_BREAKS = BUILDER
                                .translation("config.rechantment.simple.guaranteed_bookshelf_breaks.title")
                                .defineInRange("guaranteed_bookshelf_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_0_FORCED_FLOOR_BREAKS = BUILDER
                                .translation("config.rechantment.simple.guaranteed_floor_breaks.title")
                                .defineInRange("guaranteed_floor_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_0_BOOK_BREAK_CHANCE = BUILDER.translation("config.rechantment.simple.book_break_chance.title")
                                .defineInRange("book_break_chance", 0.0, 0.0, 1.0);
                RARITY_0_FLOOR_BREAK_CHANCE = BUILDER.translation("config.rechantment.simple.floor_break_chance.title")
                                .defineInRange("floor_break_chance", 0.05, 0.0, 1.0);
                RARITY_0_REQUIRED_BOOKSHELVES = BUILDER
                                .translation("config.rechantment.simple.required_bookshelves.title")
                                .defineInRange("required_bookshelves", 4, 0, Integer.MAX_VALUE);
                RARITY_0_REQUIRED_LAPIS = BUILDER.translation("config.rechantment.simple.required_lapis.title")
                                .defineInRange("required_lapis", 2, 0, 64);
                RARITY_0_FLOOR_BLOCK_TYPE = BUILDER.translation("config.rechantment.simple.floor_block_type.title")
                                .define("floor_block_type", "minecraft:iron_block");
                RARITY_0_BONUS_ITEM_ROLL_CHANCE = BUILDER
                                .translation("config.rechantment.simple.bonus_item_roll_chance.title")
                                .defineInRange("bonus_item_roll_chance", 0.01, 0.0, 1.0);
                RARITY_0_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT = BUILDER
                                .translation("config.rechantment.simple.bonus_item_mysterious_book_weight.title")
                                .defineInRange("bonus_item_mysterious_book_weight", 78, 0, Integer.MAX_VALUE);
                RARITY_0_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.simple.bonus_item_common_gem_pool_weight.title")
                                .defineInRange("bonus_item_common_gem_pool_weight", 20, 0, Integer.MAX_VALUE);
                RARITY_0_BONUS_ITEM_RARE_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.simple.bonus_item_rare_gem_pool_weight.title")
                                .defineInRange("bonus_item_rare_gem_pool_weight", 2, 0, Integer.MAX_VALUE);

                BUILDER.comment(
                                "Minimum and maximum amount of xp orbs this tier provides when put into a grindstone",
                                "Make sure min is less than max, or problems may occur");
                RARITY_0_GRINDSTONE_XP_MIN = BUILDER.translation("config.rechantment.simple.grindstone_xp_min.title")
                                .defineInRange("grindstone_xp_min", 3, 0, Integer.MAX_VALUE);
                RARITY_0_GRINDSTONE_XP_MAX = BUILDER.translation("config.rechantment.simple.grindstone_xp_max.title")
                                .defineInRange("grindstone_xp_max", 7, 0, Integer.MAX_VALUE);
                BUILDER.comment(
                                "List of potential enchantments with weights, levels, and per-level weights.",
                                "Format: <enchantment>|<weight>|<level-range>|<level-weights>",
                                "Example: minecraft:unbreaking|10|1-3|1,2,3");
                ArrayList<String> rarity_0_default_enchantments = new ArrayList<>();
                rarity_0_default_enchantments.add("minecraft:sharpness|1|1-4|3,3,2,1");
                rarity_0_default_enchantments.add("minecraft:efficiency|1|1-4|3,3,2,1");
                rarity_0_default_enchantments.add("minecraft:knockback|1|1-2|3,2");
                rarity_0_default_enchantments.add("minecraft:power|1|1-4|3,3,2,1");
                rarity_0_default_enchantments.add("minecraft:smite|1|3-4|1,1");
                rarity_0_default_enchantments.add("minecraft:lure|1|2-3|2,1");
                rarity_0_default_enchantments.add("minecraft:protection|1|2-4|3,2,1");
                rarity_0_default_enchantments.add("minecraft:aqua_affinity|1|1|1");
                rarity_0_default_enchantments.add("minecraft:punch|1|1-2|3,1");
                rarity_0_default_enchantments.add("minecraft:piercing|1|3-4|2,1");
                rarity_0_default_enchantments.add("minecraft:density|1|1-5|4,3,2,2,1");
                rarity_0_default_enchantments.add("minecraft:breach|1|1-4|4,3,2,1");
                rarity_0_default_enchantments.add("minecraft:wind_burst|1|1-3|3,2,1");

                RARITY_0_ENCHANTMENTS = BUILDER.translation("config.rechantment.simple.enchantments.title").defineList(
                                "enchantments", rarity_0_default_enchantments,
                                () -> "minecraft:sharpness|1|1-4|3,3,2,1", s -> s instanceof String);
                BUILDER.pop();
                // Unique rarity builder default config
                BUILDER.translation("config.rechantment.unique.name").comment("Configs for Unique Rarity Enchantments")
                                .push("unique");
                RARITY_1_KEY = BUILDER.translation("config.rechantment.unique.key.title").define("key", "unique");
                RARITY_1_COLOR = BUILDER.translation("config.rechantment.unique.color.title").defineInRange("color",
                                5635925, 0, Integer.MAX_VALUE);
                RARITY_1_EXP_COST = BUILDER.translation("config.rechantment.unique.exp_cost.title")
                                .defineInRange("exp_cost", 100, 0, Integer.MAX_VALUE);
                RARITY_1_WORLD_SPAWN_WEIGHT = BUILDER.translation("config.rechantment.unique.world_spawn_weight.title")
                                .defineInRange("world_spawn_weight", 30, 0, Integer.MAX_VALUE);
                RARITY_1_MIN_SUCCESS = BUILDER.translation("config.rechantment.unique.min_success.title")
                                .defineInRange("min_success", 25, 0, 100);
                RARITY_1_MAX_SUCCESS = BUILDER.translation("config.rechantment.unique.max_success.title")
                                .defineInRange("max_success", 90, 0, 100);
                RARITY_1_FORCED_BOOK_BREAKS = BUILDER
                                .translation("config.rechantment.unique.guaranteed_bookshelf_breaks.title")
                                .defineInRange("guaranteed_bookshelf_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_1_FORCED_FLOOR_BREAKS = BUILDER
                                .translation("config.rechantment.unique.guaranteed_floor_breaks.title")
                                .defineInRange("guaranteed_floor_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_1_BOOK_BREAK_CHANCE = BUILDER.translation("config.rechantment.unique.book_break_chance.title")
                                .defineInRange("book_break_chance", 0.0, 0.0, 1.0);
                RARITY_1_FLOOR_BREAK_CHANCE = BUILDER.translation("config.rechantment.unique.floor_break_chance.title")
                                .defineInRange("floor_break_chance", 0.045, 0.0, 1.0);
                RARITY_1_REQUIRED_BOOKSHELVES = BUILDER
                                .translation("config.rechantment.unique.required_bookshelves.title")
                                .defineInRange("required_bookshelves", 8, 0, Integer.MAX_VALUE);
                RARITY_1_REQUIRED_LAPIS = BUILDER.translation("config.rechantment.unique.required_lapis.title")
                                .defineInRange("required_lapis", 3, 0, 64);
                RARITY_1_FLOOR_BLOCK_TYPE = BUILDER.translation("config.rechantment.unique.floor_block_type.title")
                                .define("floor_block_type", "minecraft:gold_block");
                RARITY_1_BONUS_ITEM_ROLL_CHANCE = BUILDER
                                .translation("config.rechantment.unique.bonus_item_roll_chance.title")
                                .defineInRange("bonus_item_roll_chance", 0.01, 0.0, 1.0);
                RARITY_1_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT = BUILDER
                                .translation("config.rechantment.unique.bonus_item_mysterious_book_weight.title")
                                .defineInRange("bonus_item_mysterious_book_weight", 72, 0, Integer.MAX_VALUE);
                RARITY_1_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.unique.bonus_item_common_gem_pool_weight.title")
                                .defineInRange("bonus_item_common_gem_pool_weight", 24, 0, Integer.MAX_VALUE);
                RARITY_1_BONUS_ITEM_RARE_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.unique.bonus_item_rare_gem_pool_weight.title")
                                .defineInRange("bonus_item_rare_gem_pool_weight", 4, 0, Integer.MAX_VALUE);
                RARITY_1_GRINDSTONE_XP_MIN = BUILDER.translation("config.rechantment.unique.grindstone_xp_min.title")
                                .defineInRange("grindstone_xp_min", 5, 0, Integer.MAX_VALUE);
                RARITY_1_GRINDSTONE_XP_MAX = BUILDER.translation("config.rechantment.unique.grindstone_xp_max.title")
                                .defineInRange("grindstone_xp_max", 10, 0, Integer.MAX_VALUE);
                ArrayList<String> rarity_1_default_enchantments = new ArrayList<>();
                rarity_1_default_enchantments.add("minecraft:bane_of_arthropods|1|4|1");
                rarity_1_default_enchantments.add("minecraft:loyalty|1|3|1");
                rarity_1_default_enchantments.add("minecraft:projectile_protection|1|4|1");
                rarity_1_default_enchantments.add("minecraft:fire_protection|1|4|1");
                rarity_1_default_enchantments.add("minecraft:thorns|1|2-3|2,1");
                rarity_1_default_enchantments.add("minecraft:unbreaking|1|1-3|3,2,1");
                rarity_1_default_enchantments.add("minecraft:feather_falling|1|3-4|2,1");
                rarity_1_default_enchantments.add("minecraft:quick_charge|1|3|1");
                rarity_1_default_enchantments.add("minecraft:riptide|1|3|1");
                rarity_1_default_enchantments.add("minecraft:respiration|1|1-3|3,2,1");
                rarity_1_default_enchantments.add("minecraft:blast_protection|1|4|1");
                rarity_1_default_enchantments.add("minecraft:impaling|1|3-4|2,1");
                RARITY_1_ENCHANTMENTS = BUILDER.translation("config.rechantment.unique.enchantments.title").defineList(
                                "enchantments", rarity_1_default_enchantments,
                                () -> "minecraft:sharpness|1|1-4|3,3,2,1", s -> s instanceof String);

                BUILDER.pop();

                BUILDER.translation("config.rechantment.elite.name").comment("Configs for Elite Rarity Enchantments")
                                .push("elite");
                RARITY_2_KEY = BUILDER.translation("config.rechantment.elite.key.title").define("key", "elite");
                RARITY_2_COLOR = BUILDER.translation("config.rechantment.elite.color.title").defineInRange("color",
                                5636095, 0, Integer.MAX_VALUE);
                RARITY_2_EXP_COST = BUILDER.translation("config.rechantment.elite.exp_cost.title")
                                .defineInRange("exp_cost", 200, 0, Integer.MAX_VALUE);
                RARITY_2_WORLD_SPAWN_WEIGHT = BUILDER.translation("config.rechantment.elite.world_spawn_weight.title")
                                .defineInRange("world_spawn_weight", 15, 0, Integer.MAX_VALUE);
                RARITY_2_MIN_SUCCESS = BUILDER.translation("config.rechantment.elite.min_success.title")
                                .defineInRange("min_success", 25, 0, 100);
                RARITY_2_MAX_SUCCESS = BUILDER.translation("config.rechantment.elite.max_success.title")
                                .defineInRange("max_success", 90, 0, 100);
                RARITY_2_FORCED_BOOK_BREAKS = BUILDER
                                .translation("config.rechantment.elite.guaranteed_bookshelf_breaks.title")
                                .defineInRange("guaranteed_bookshelf_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_2_FORCED_FLOOR_BREAKS = BUILDER
                                .translation("config.rechantment.elite.guaranteed_floor_breaks.title")
                                .defineInRange("guaranteed_floor_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_2_BOOK_BREAK_CHANCE = BUILDER.translation("config.rechantment.elite.book_break_chance.title")
                                .defineInRange("book_break_chance", 0.015, 0.0, 1.0);
                RARITY_2_FLOOR_BREAK_CHANCE = BUILDER.translation("config.rechantment.elite.floor_break_chance.title")
                                .defineInRange("floor_break_chance", 0.03, 0.0, 1.0);
                RARITY_2_REQUIRED_BOOKSHELVES = BUILDER
                                .translation("config.rechantment.elite.required_bookshelves.title")
                                .defineInRange("required_bookshelves", 16, 0, Integer.MAX_VALUE);
                RARITY_2_REQUIRED_LAPIS = BUILDER.translation("config.rechantment.elite.required_lapis.title")
                                .defineInRange("required_lapis", 3, 0, 64);
                RARITY_2_FLOOR_BLOCK_TYPE = BUILDER.translation("config.rechantment.elite.floor_block_type.title")
                                .define("floor_block_type", "minecraft:diamond_block");
                RARITY_2_BONUS_ITEM_ROLL_CHANCE = BUILDER
                                .translation("config.rechantment.elite.bonus_item_roll_chance.title")
                                .defineInRange("bonus_item_roll_chance", 0.02, 0.0, 1.0);
                RARITY_2_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT = BUILDER
                                .translation("config.rechantment.elite.bonus_item_mysterious_book_weight.title")
                                .defineInRange("bonus_item_mysterious_book_weight", 66, 0, Integer.MAX_VALUE);
                RARITY_2_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.elite.bonus_item_common_gem_pool_weight.title")
                                .defineInRange("bonus_item_common_gem_pool_weight", 28, 0, Integer.MAX_VALUE);
                RARITY_2_BONUS_ITEM_RARE_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.elite.bonus_item_rare_gem_pool_weight.title")
                                .defineInRange("bonus_item_rare_gem_pool_weight", 6, 0, Integer.MAX_VALUE);
                RARITY_2_GRINDSTONE_XP_MIN = BUILDER.translation("config.rechantment.elite.grindstone_xp_min.title")
                                .defineInRange("grindstone_xp_min", 10, 0, Integer.MAX_VALUE);
                RARITY_2_GRINDSTONE_XP_MAX = BUILDER.translation("config.rechantment.elite.grindstone_xp_max.title")
                                .defineInRange("grindstone_xp_max", 20, 0, Integer.MAX_VALUE);
                ArrayList<String> rarity_2_default_enchantments = new ArrayList<>();
                rarity_2_default_enchantments.add("minecraft:looting|1|1-3|3,2,1");
                rarity_2_default_enchantments.add("minecraft:flame|1|1|1");
                rarity_2_default_enchantments.add("minecraft:frost_walker|1|2|1");
                rarity_2_default_enchantments.add("minecraft:depth_strider|1|3|1");
                rarity_2_default_enchantments.add("minecraft:soul_speed|1|3|1");
                rarity_2_default_enchantments.add("rechantment:hells_fury|1|4|1");
                rarity_2_default_enchantments.add("rechantment:voids_bane|1|4|1");
                rarity_2_default_enchantments.add("minecraft:fire_aspect|1|1-2|2,1");
                rarity_2_default_enchantments.add("minecraft:fortune|1|1-3|2,2,1");
                rarity_2_default_enchantments.add("rechantment:telekinesis|1|1|1");
                rarity_2_default_enchantments.add("rechantment:vein_miner|1|1|1");
                rarity_2_default_enchantments.add("rechantment:berserk|1|3|1");
                rarity_2_default_enchantments.add("rechantment:bash|1|1|1");
                RARITY_2_ENCHANTMENTS = BUILDER.translation("config.rechantment.elite.enchantments.title").defineList(
                                "enchantments", rarity_2_default_enchantments,
                                () -> "minecraft:sharpness|1|1-4|3,3,2,1", s -> s instanceof String);

                BUILDER.pop();

                // Ultimate rarity builder default config
                BUILDER.translation("config.rechantment.ultimate.name")
                                .comment("Configs for Ultimate Rarity Enchantments").push("ultimate");
                RARITY_3_KEY = BUILDER.translation("config.rechantment.ultimate.key.title").define("key", "ultimate");
                RARITY_3_COLOR = BUILDER.translation("config.rechantment.ultimate.color.title").defineInRange("color",
                                16777045, 0, Integer.MAX_VALUE);
                RARITY_3_EXP_COST = BUILDER.translation("config.rechantment.ultimate.exp_cost.title")
                                .defineInRange("exp_cost", 500, 0, Integer.MAX_VALUE);
                RARITY_3_WORLD_SPAWN_WEIGHT = BUILDER
                                .translation("config.rechantment.ultimate.world_spawn_weight.title")
                                .defineInRange("world_spawn_weight", 10, 0, Integer.MAX_VALUE);
                RARITY_3_MIN_SUCCESS = BUILDER.translation("config.rechantment.ultimate.min_success.title")
                                .defineInRange("min_success", 30, 0, 100);
                RARITY_3_MAX_SUCCESS = BUILDER.translation("config.rechantment.ultimate.max_success.title")
                                .defineInRange("max_success", 90, 0, 100);
                RARITY_3_FORCED_BOOK_BREAKS = BUILDER
                                .translation("config.rechantment.ultimate.guaranteed_bookshelf_breaks.title")
                                .defineInRange("guaranteed_bookshelf_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_3_FORCED_FLOOR_BREAKS = BUILDER
                                .translation("config.rechantment.ultimate.guaranteed_floor_breaks.title")
                                .defineInRange("guaranteed_floor_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_3_BOOK_BREAK_CHANCE = BUILDER.translation("config.rechantment.ultimate.book_break_chance.title")
                                .defineInRange("book_break_chance", 0.015, 0.0, 1.0);
                RARITY_3_FLOOR_BREAK_CHANCE = BUILDER
                                .translation("config.rechantment.ultimate.floor_break_chance.title")
                                .defineInRange("floor_break_chance", 0.075, 0.0, 1.0);
                RARITY_3_REQUIRED_BOOKSHELVES = BUILDER
                                .translation("config.rechantment.ultimate.required_bookshelves.title")
                                .defineInRange("required_bookshelves", 32, 0, Integer.MAX_VALUE);
                RARITY_3_REQUIRED_LAPIS = BUILDER.translation("config.rechantment.ultimate.required_lapis.title")
                                .defineInRange("required_lapis", 4, 0, 64);
                RARITY_3_FLOOR_BLOCK_TYPE = BUILDER.translation("config.rechantment.ultimate.floor_block_type.title")
                                .define("floor_block_type", "minecraft:emerald_block");
                RARITY_3_BONUS_ITEM_ROLL_CHANCE = BUILDER
                                .translation("config.rechantment.ultimate.bonus_item_roll_chance.title")
                                .defineInRange("bonus_item_roll_chance", 0.03, 0.0, 1.0);
                RARITY_3_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT = BUILDER
                                .translation("config.rechantment.ultimate.bonus_item_mysterious_book_weight.title")
                                .defineInRange("bonus_item_mysterious_book_weight", 60, 0, Integer.MAX_VALUE);
                RARITY_3_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.ultimate.bonus_item_common_gem_pool_weight.title")
                                .defineInRange("bonus_item_common_gem_pool_weight", 32, 0, Integer.MAX_VALUE);
                RARITY_3_BONUS_ITEM_RARE_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.ultimate.bonus_item_rare_gem_pool_weight.title")
                                .defineInRange("bonus_item_rare_gem_pool_weight", 8, 0, Integer.MAX_VALUE);
                RARITY_3_GRINDSTONE_XP_MIN = BUILDER.translation("config.rechantment.ultimate.grindstone_xp_min.title")
                                .defineInRange("grindstone_xp_min", 20, 0, Integer.MAX_VALUE);
                RARITY_3_GRINDSTONE_XP_MAX = BUILDER.translation("config.rechantment.ultimate.grindstone_xp_max.title")
                                .defineInRange("grindstone_xp_max", 50, 0, Integer.MAX_VALUE);
                ArrayList<String> rarity_3_default_enchantments = new ArrayList<>();
                rarity_3_default_enchantments.add("minecraft:swift_sneak|1|1-3|3,2,1");
                rarity_3_default_enchantments.add("minecraft:luck_of_the_sea|1|2-3|2,1");
                rarity_3_default_enchantments.add("minecraft:sweeping_edge|1|2-3|2,1");
                rarity_3_default_enchantments.add("minecraft:silk_touch|1|1|1");
                rarity_3_default_enchantments.add("rechantment:ice_aspect|1|1-2|2,1");
                rarity_3_default_enchantments.add("rechantment:wisdom|1|1-2|2,1");
                rarity_3_default_enchantments.add("minecraft:multishot|1|1|1");
                rarity_3_default_enchantments.add("rechantment:wisdom|1|1-2|2,1");
                rarity_3_default_enchantments.add("rechantment:courage|1|1|1");
                rarity_3_default_enchantments.add("rechantment:spring|1|1-3|3,2,1");
                rarity_3_default_enchantments.add("rechantment:timber|1|1-3|3,2,1");
                rarity_3_default_enchantments.add("rechantment:reach|1|1-3|3,2,1");
                rarity_3_default_enchantments.add("minecraft:channeling|1|1|1");
                RARITY_3_ENCHANTMENTS = BUILDER.translation("config.rechantment.ultimate.enchantments.title")
                                .defineList("enchantments", rarity_3_default_enchantments,
                                                () -> "minecraft:sharpness|1|1-4|3,3,2,1", s -> s instanceof String);

                BUILDER.pop();

                // Legendary rarity builder default config
                BUILDER.translation("config.rechantment.legendary.name")
                                .comment("Configs for Legendary Rarity Enchantments").push("legendary");
                RARITY_4_KEY = BUILDER.translation("config.rechantment.legendary.key.title").define("key", "legendary");
                RARITY_4_COLOR = BUILDER.translation("config.rechantment.legendary.color.title").defineInRange("color",
                                16755200, 0, Integer.MAX_VALUE);
                RARITY_4_EXP_COST = BUILDER.translation("config.rechantment.legendary.exp_cost.title")
                                .defineInRange("exp_cost", 1500, 0, Integer.MAX_VALUE);
                RARITY_4_WORLD_SPAWN_WEIGHT = BUILDER
                                .translation("config.rechantment.legendary.world_spawn_weight.title")
                                .defineInRange("world_spawn_weight", 5, 0, Integer.MAX_VALUE);
                RARITY_4_MIN_SUCCESS = BUILDER.translation("config.rechantment.legendary.min_success.title")
                                .defineInRange("min_success", 35, 0, 100);
                RARITY_4_MAX_SUCCESS = BUILDER.translation("config.rechantment.legendary.max_success.title")
                                .defineInRange("max_success", 90, 0, 100);
                RARITY_4_FORCED_BOOK_BREAKS = BUILDER
                                .translation("config.rechantment.legendary.guaranteed_bookshelf_breaks.title")
                                .defineInRange("guaranteed_bookshelf_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_4_FORCED_FLOOR_BREAKS = BUILDER
                                .translation("config.rechantment.legendary.guaranteed_floor_breaks.title")
                                .defineInRange("guaranteed_floor_breaks", 0, 0, Integer.MAX_VALUE);
                RARITY_4_BOOK_BREAK_CHANCE = BUILDER.translation("config.rechantment.legendary.book_break_chance.title")
                                .defineInRange("book_break_chance", 0.015, 0.0, 1.0);
                RARITY_4_FLOOR_BREAK_CHANCE = BUILDER
                                .translation("config.rechantment.legendary.floor_break_chance.title")
                                .defineInRange("floor_break_chance", 0.08, 0.0, 1.0);
                RARITY_4_REQUIRED_BOOKSHELVES = BUILDER
                                .translation("config.rechantment.legendary.required_bookshelves.title")
                                .defineInRange("required_bookshelves", 45, 0, Integer.MAX_VALUE);
                RARITY_4_REQUIRED_LAPIS = BUILDER.translation("config.rechantment.legendary.required_lapis.title")
                                .defineInRange("required_lapis", 5, 0, 64);
                RARITY_4_FLOOR_BLOCK_TYPE = BUILDER.translation("config.rechantment.legendary.floor_block_type.title")
                                .define("floor_block_type", "minecraft:ancient_debris");
                RARITY_4_BONUS_ITEM_ROLL_CHANCE = BUILDER
                                .translation("config.rechantment.legendary.bonus_item_roll_chance.title")
                                .defineInRange("bonus_item_roll_chance", 0.05, 0.0, 1.0);
                RARITY_4_BONUS_ITEM_MYSTERIOUS_BOOK_WEIGHT = BUILDER
                                .translation("config.rechantment.legendary.bonus_item_mysterious_book_weight.title")
                                .defineInRange("bonus_item_mysterious_book_weight", 54, 0, Integer.MAX_VALUE);
                RARITY_4_BONUS_ITEM_COMMON_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.legendary.bonus_item_common_gem_pool_weight.title")
                                .defineInRange("bonus_item_common_gem_pool_weight", 36, 0, Integer.MAX_VALUE);
                RARITY_4_BONUS_ITEM_RARE_GEM_POOL_WEIGHT = BUILDER
                                .translation("config.rechantment.legendary.bonus_item_rare_gem_pool_weight.title")
                                .defineInRange("bonus_item_rare_gem_pool_weight", 10, 0, Integer.MAX_VALUE);
                RARITY_4_GRINDSTONE_XP_MIN = BUILDER.translation("config.rechantment.legendary.grindstone_xp_min.title")
                                .defineInRange("grindstone_xp_min", 75, 0, Integer.MAX_VALUE);
                RARITY_4_GRINDSTONE_XP_MAX = BUILDER.translation("config.rechantment.legendary.grindstone_xp_max.title")
                                .defineInRange("grindstone_xp_max", 150, 0, Integer.MAX_VALUE);
                ArrayList<String> rarity_4_default_enchantments = new ArrayList<>();
                rarity_4_default_enchantments.add("minecraft:infinity|1|1|1");
                rarity_4_default_enchantments.add("rechantment:inquisitive|1|1-4|4,3,2,1");
                rarity_4_default_enchantments.add("rechantment:thunder_strike|1|1-2|2,1");
                rarity_4_default_enchantments.add("rechantment:overload|1|1-3|4,2,1");
                rarity_4_default_enchantments.add("rechantment:blitz|1|3|1");
                rarity_4_default_enchantments.add("rechantment:volley|1|1-2|2,1");
                rarity_4_default_enchantments.add("rechantment:rebirth|1|1|1");
                RARITY_4_ENCHANTMENTS = BUILDER.translation("config.rechantment.legendary.enchantments.title")
                                .defineList("enchantments", rarity_4_default_enchantments,
                                                () -> "minecraft:sharpness|1|1-4|3,3,2,1", s -> s instanceof String);

                BUILDER.pop();
                BUILDER.pop();

                BUILDER.translation("config.rechantment.configs_for_all_rarities.name")
                                .comment("Global settings that apply to all rarity tiers")
                                .push("Configs for all rarities");
                GRINDSTONE_RESULT_ITEM = BUILDER.translation("config.rechantment.grindstone_result_item.title")
                                .define("grindstone_result_item", "minecraft:paper");
                BUILDER.comment("Global weighted pools used by bonus item rewards.");
                ArrayList<String> common_gem_pool_defaults = new ArrayList<>();
                common_gem_pool_defaults.add("rechantment:chance_gem|45");
                common_gem_pool_defaults.add("rechantment:return_gem|25");
                common_gem_pool_defaults.add("rechantment:tasty_gem|20");
                common_gem_pool_defaults.add("rechantment:smithing_gem|10");
                COMMON_GEM_POOL = BUILDER.translation("config.rechantment.common_gem_pool.title").defineList(
                                "common_gem_pool", common_gem_pool_defaults, () -> "rechantment:chance_gem|45",
                                s -> s instanceof String);

                ArrayList<String> rare_gem_pool_defaults = new ArrayList<>();
                rare_gem_pool_defaults.add("rechantment:shiny_chance_gem|35");
                rare_gem_pool_defaults.add("rechantment:warp_gem|30");
                rare_gem_pool_defaults.add("rechantment:clone_gem|20");
                rare_gem_pool_defaults.add("rechantment:lucky_gem|15");
                RARE_GEM_POOL = BUILDER.translation("config.rechantment.rare_gem_pool.title").defineList(
                                "rare_gem_pool", rare_gem_pool_defaults, () -> "rechantment:shiny_chance_gem|35",
                                s -> s instanceof String);
                BUILDER.comment("The game will broadcast a message to all players if a player gets any listed enchantments within the level range to drop from the enchantment table",
                                "Format: <enchantment>|<level-range>",
                                "Example: minecraft:unbreaking|1-3");
                ArrayList<String> announce_enchantments = new ArrayList<>();
                announce_enchantments.add("minecraft:fortune|3");
                ANNOUNCEMENT_ENCHANTMENTS = BUILDER.translation("config.rechantment.announce_enchantments.title")
                                .defineList("announce_enchantments", announce_enchantments,
                                                () -> "rechantment:sharpness|3-5", s -> s instanceof String);

                BUILDER.translation("config.rechantment.announce_gem_drop_list.name")
                                .comment("Settings for gem drop announcement toggles")
                                .push("Announce Gem Drop List");
                BUILDER.comment("Gem drop announcement toggles.");
                ANNOUNCE_CHANCE_GEM = BUILDER.translation("config.rechantment.announce_chance_gem.title")
                                .define("announce_chance_gem", true);
                ANNOUNCE_SHINY_CHANCE_GEM = BUILDER.translation("config.rechantment.announce_shiny_chance_gem.title")
                                .define("announce_shiny_chance_gem", true);
                ANNOUNCE_RETURN_GEM = BUILDER.translation("config.rechantment.announce_return_gem.title")
                                .define("announce_return_gem", true);
                ANNOUNCE_TASTY_GEM = BUILDER.translation("config.rechantment.announce_tasty_gem.title")
                                .define("announce_tasty_gem", true);
                ANNOUNCE_WARP_GEM = BUILDER.translation("config.rechantment.announce_warp_gem.title")
                                .define("announce_warp_gem", true);
                ANNOUNCE_LUCKY_GEM = BUILDER.translation("config.rechantment.announce_lucky_gem.title")
                                .define("announce_lucky_gem", true);
                ANNOUNCE_CLONE_GEM = BUILDER.translation("config.rechantment.announce_clone_gem.title")
                                .define("announce_clone_gem", true);
                ANNOUNCE_SMITHING_GEM = BUILDER.translation("config.rechantment.announce_smithing_gem.title")
                                .define("announce_smithing_gem", true);
                BUILDER.pop();
                BUILDER.pop();

                BUILDER.translation("config.rechantment.chance_gems.name").comment("Settings for Chance Gems")
                                .push("Chance Gems");
                BUILDER.comment("Chance for the shiny chance gem to shatter when applied.");
                SHINY_CHANCE_GEM_BREAK_CHANCE = BUILDER
                                .translation("config.rechantment.shiny_chance_gem_break_chance.title")
                                .defineInRange("shiny_chance_gem_break_chance", 0.25, 0.0, 1.0);
                BUILDER.pop();

                BUILDER.translation("config.rechantment.loot_table_enhancements.name").comment(
                                "Configurations for all things related to generated loot drops. Ex: end_city_treasure")
                                .push("Loot Table Enhancements");

                BUILDER.comment("Replace all enchanted loot into Rechantment books based on world_spawn_weight config from each rarity section");
                BUILDER.comment("Example: A chest plate found with enchants will be replaced entirely with a rolled enchanted book based on the rarity configs");
                REPLACE_ENCHANTED_LOOT = BUILDER.translation("config.rechantment.replace_enchanted_loot.title")
                                .define("replace_enchanted_loot", true);

                BUILDER.comment("Removes mending enchantment from found enchanted loot from generated world loot. Ex: end_city_treasure");
                BUILDER.comment("Mending books can be found only in book form as long as you have minecraft:mending set in a rarity pool.");
                BUILDER.comment("Having REPLACE_ENCHANTED_LOOT set to true defaults this to true.");
                REMOVE_MENDING_ENABLED = BUILDER.translation("config.rechantment.remove_mending.title")
                                .define("remove_mending", true);

                BUILDER.comment("Excludes gold, leather, stone, wood enchanted drops from being affected by the REPLACE_ENCHANTED_LOOT configuration");
                BUILDER.comment("Example: Gold tools and armor from nether portal ruins will remain and not be replaced by Rechantment books");
                EXCLUDE_LOWER_TIER_LOOT = BUILDER.translation("config.rechantment.exclude_lower_tier_loot.title")
                                .define("exclude_lower_tier_loot", true);

                BUILDER.comment("Makes enchanted fished treasure have weaker enchants (level 5 enchants) and excludes it from REPLACE_ENCHANTED_LOOT");
                BUILDER.comment("Example: Enchanted bows and fishing rods will commonly have Power I or Lure I respectively");
                BUILDER.comment("This setting is applied before REPLACE_ENCHANTED_LOOT");
                BUILDER.comment("Without this enabled, fishing loot becomes an overpowered source for enchanted books with REPLACE_ENCHANTED_LOOT");
                NERF_FISHING_LOOT = BUILDER.translation("config.rechantment.nerf_fishing_loot.title")
                                .define("nerf_fishing_loot", true);

                BUILDER.translation("config.rechantment.villager_trades.name")
                                .comment("Settings that control villager trade modifications").push("Villager Trades");
                BUILDER.comment("If false, villager trades are left untouched (no enchanted book replacement or enchant stripping).");
                MODIFY_VILLAGER_TRADES = BUILDER.translation("config.rechantment.modify_villager_trades.title")
                                .define("modify_villager_trades", true);
                BUILDER.comment("Cost in emeralds for the mysterious book trade when replacing enchanted books.");
                VILLAGER_MYSTERIOUS_BOOK_EMERALD_COST = BUILDER
                                .translation("config.rechantment.mysterious_book_emerald_cost.title")
                                .defineInRange("mysterious_book_emerald_cost", 32, 1, 64);
                BUILDER.pop();

                BUILDER.translation("config.rechantment.fortune_nerf.name")
                                .comment("Settings to control Fortune enchantment drop multipliers")
                                .push("Fortune Nerf");
                BUILDER.comment(
                                "If enabled, fortune will only double drops based on the chances defined at each level");
                FORTUNE_NERF_ENABLED = BUILDER.translation("config.rechantment.nerf_enabled.title")
                                .define("nerf_enabled", false);
                FORTUNE_1_CHANCE = BUILDER.translation("config.rechantment.fortune_1_chance.title")
                                .defineInRange("fortune_1_chance", 0.33, 0.0, 1.0);
                FORTUNE_2_CHANCE = BUILDER.translation("config.rechantment.fortune_2_chance.title")
                                .defineInRange("fortune_2_chance", 0.5, 0.0, 1.0);
                FORTUNE_3_CHANCE = BUILDER.translation("config.rechantment.fortune_3_chance.title")
                                .defineInRange("fortune_3_chance", 0.65, 0.0, 1.0);

                BUILDER.pop();

                SPEC = BUILDER.build();
        }
}
