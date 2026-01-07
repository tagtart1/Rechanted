package net.tagtart.rechantment.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BookRarityProperties {

    public String key;
    public float rarity;
    public int color;
    public int requiredExp;
    public int worldSpawnWeight;
    public int minSuccess;
    public int maxSuccess;
    public int forcedBookBreaks;
    public int forcedFloorBreaks;
    public double bookBreakChance;
    public double floorBreakChance;
    public int requiredBookShelves;
    public int requiredLapis;
    public Block floorBlock;
    public double rerollGemChance;
    public int minGrindstoneXP;
    public int maxGrindstoneXP;
    public List<EnchantmentPoolEntry> enchantmentPool;
    public int enchantmentPoolTotalWeights;
    public ResourceLocation iconResourceLocation;

    private BookRarityProperties()
    {

    }

    public boolean isEnchantmentInPool(String pEnchantment) {
        for (EnchantmentPoolEntry entry : enchantmentPool) {
            if (entry.enchantment.equals(pEnchantment)) {
                return true;
            }
        }

        return false;
    }

    public Style colorAsStyle() {
        return Style.EMPTY.withColor(color);
    }

    public EnchantmentPoolEntry getRandomEnchantmentWeighted() {
        Random rand = new Random();
        int randVal = rand.nextInt(enchantmentPoolTotalWeights);
        int cumulativeWeight = 0;
        for (EnchantmentPoolEntry entry : enchantmentPool) {
            cumulativeWeight += entry.weight;
            if (randVal < cumulativeWeight) {
                return entry;
            }
        }

        throw new IllegalStateException("Failed to select an enchantment based on weight.\nEnsure Rechantment config is set up properly.");
    }

    // For caching the weight sum later.
    private static int getEnchantmentPoolTotalWeight(List<EnchantmentPoolEntry> entries) {
        int currentSum = 0;
        for (int i = 0; i < entries.size(); ++i) {
            currentSum += entries.get(i).weight;
        }

        return currentSum;
    }



    private static BookRarityProperties[] ALL_BOOK_PROPERTIES;


    public static BookRarityProperties getRandomRarityWeighted() {

        BookRarityProperties[] properties = getAllProperties();

        int totalWeight = Arrays.stream(properties).mapToInt(property -> property.worldSpawnWeight).sum();

        Random random = new Random();
        int roll = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (BookRarityProperties property : properties) {
            cumulativeWeight += property.worldSpawnWeight;
            if (roll < cumulativeWeight) {
                return property;
            }
        }
        throw new IllegalStateException("Failed to select a rarity based on weight.\nEnsure Rechantment config is set up properly.");
    }



    public static BookRarityProperties[] getAllProperties() {
        if (ALL_BOOK_PROPERTIES != null) {
            return ALL_BOOK_PROPERTIES;
        }

        BookRarityProperties simpleProperties = new BookRarityProperties();
        BookRarityProperties uniqueProperties = new BookRarityProperties();
        BookRarityProperties eliteProperties = new BookRarityProperties();
        BookRarityProperties ultimateProperties = new BookRarityProperties();
        BookRarityProperties legendaryProperties = new BookRarityProperties();

        // Binding simple tier configs with an accessible properties class.
        simpleProperties.rarity = 1.0f;
        simpleProperties.key =                  RechantmentCommonConfigs.RARITY_0_KEY.get();
        simpleProperties.color =                RechantmentCommonConfigs.RARITY_0_COLOR.get();
        simpleProperties.requiredExp =          RechantmentCommonConfigs.RARITY_0_EXP_COST.get();
        simpleProperties.worldSpawnWeight =     RechantmentCommonConfigs.RARITY_0_WORLD_SPAWN_WEIGHT.get();
        simpleProperties.minSuccess =           RechantmentCommonConfigs.RARITY_0_MIN_SUCCESS.get();
        simpleProperties.maxSuccess =           RechantmentCommonConfigs.RARITY_0_MAX_SUCCESS.get();
        simpleProperties.forcedBookBreaks =     RechantmentCommonConfigs.RARITY_0_FORCED_BOOK_BREAKS.get();
        simpleProperties.forcedFloorBreaks =    RechantmentCommonConfigs.RARITY_0_FORCED_FLOOR_BREAKS.get();
        simpleProperties.bookBreakChance =      RechantmentCommonConfigs.RARITY_0_BOOK_BREAK_CHANCE.get();
        simpleProperties.floorBreakChance =     RechantmentCommonConfigs.RARITY_0_FLOOR_BREAK_CHANCE.get();
        simpleProperties.requiredBookShelves =  RechantmentCommonConfigs.RARITY_0_REQUIRED_BOOKSHELVES.get();
        simpleProperties.requiredLapis =        RechantmentCommonConfigs.RARITY_0_REQUIRED_LAPIS.get();
        simpleProperties.rerollGemChance =      RechantmentCommonConfigs.RARITY_0_REROLL_GEM_CHANCE.get();
        simpleProperties.minGrindstoneXP =      RechantmentCommonConfigs.RARITY_0_GRINDSTONE_XP_MIN.get();
        simpleProperties.maxGrindstoneXP =      RechantmentCommonConfigs.RARITY_0_GRINDSTONE_XP_MAX.get();
        simpleProperties.enchantmentPool =      EnchantmentPoolEntry.listFromString(RechantmentCommonConfigs.RARITY_0_ENCHANTMENTS.get());
        simpleProperties.enchantmentPoolTotalWeights = getEnchantmentPoolTotalWeight(simpleProperties.enchantmentPool);
        simpleProperties.iconResourceLocation = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/item/simple.png");

        ResourceLocation blockLocation =  ResourceLocation.parse(RechantmentCommonConfigs.RARITY_0_FLOOR_BLOCK_TYPE.get());
        simpleProperties.floorBlock = BuiltInRegistries.BLOCK.get(blockLocation);

        // Unique properties.
        uniqueProperties.rarity = 2.0f;
        uniqueProperties.key =                  RechantmentCommonConfigs.RARITY_1_KEY.get();
        uniqueProperties.color =                RechantmentCommonConfigs.RARITY_1_COLOR.get();
        uniqueProperties.requiredExp =          RechantmentCommonConfigs.RARITY_1_EXP_COST.get();
        uniqueProperties.worldSpawnWeight =     RechantmentCommonConfigs.RARITY_1_WORLD_SPAWN_WEIGHT.get();
        uniqueProperties.minSuccess =           RechantmentCommonConfigs.RARITY_1_MIN_SUCCESS.get();
        uniqueProperties.maxSuccess =           RechantmentCommonConfigs.RARITY_1_MAX_SUCCESS.get();
        uniqueProperties.forcedBookBreaks =     RechantmentCommonConfigs.RARITY_1_FORCED_BOOK_BREAKS.get();
        uniqueProperties.forcedFloorBreaks =    RechantmentCommonConfigs.RARITY_1_FORCED_FLOOR_BREAKS.get();
        uniqueProperties.bookBreakChance =      RechantmentCommonConfigs.RARITY_1_BOOK_BREAK_CHANCE.get();
        uniqueProperties.floorBreakChance =     RechantmentCommonConfigs.RARITY_1_FLOOR_BREAK_CHANCE.get();
        uniqueProperties.requiredLapis =        RechantmentCommonConfigs.RARITY_1_REQUIRED_LAPIS.get();
        uniqueProperties.requiredBookShelves =  RechantmentCommonConfigs.RARITY_1_REQUIRED_BOOKSHELVES.get();
        uniqueProperties.rerollGemChance =      RechantmentCommonConfigs.RARITY_1_REROLL_GEM_CHANCE.get();
        uniqueProperties.minGrindstoneXP =      RechantmentCommonConfigs.RARITY_1_GRINDSTONE_XP_MIN.get();
        uniqueProperties.maxGrindstoneXP =      RechantmentCommonConfigs.RARITY_1_GRINDSTONE_XP_MAX.get();
        uniqueProperties.enchantmentPool =      EnchantmentPoolEntry.listFromString(RechantmentCommonConfigs.RARITY_1_ENCHANTMENTS.get());
        uniqueProperties.enchantmentPoolTotalWeights = getEnchantmentPoolTotalWeight(uniqueProperties.enchantmentPool);
        uniqueProperties.iconResourceLocation = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/item/unique.png");



        blockLocation = ResourceLocation.parse(RechantmentCommonConfigs.RARITY_1_FLOOR_BLOCK_TYPE.get());
        uniqueProperties.floorBlock = BuiltInRegistries.BLOCK.get(blockLocation);

        // Elite properties.
        eliteProperties.rarity = 3.0f;
        eliteProperties.key =                  RechantmentCommonConfigs.RARITY_2_KEY.get();
        eliteProperties.color =                RechantmentCommonConfigs.RARITY_2_COLOR.get();
        eliteProperties.requiredExp =          RechantmentCommonConfigs.RARITY_2_EXP_COST.get();
        eliteProperties.worldSpawnWeight =     RechantmentCommonConfigs.RARITY_2_WORLD_SPAWN_WEIGHT.get();
        eliteProperties.minSuccess =           RechantmentCommonConfigs.RARITY_2_MIN_SUCCESS.get();
        eliteProperties.maxSuccess =           RechantmentCommonConfigs.RARITY_2_MAX_SUCCESS.get();
        eliteProperties.forcedBookBreaks =     RechantmentCommonConfigs.RARITY_2_FORCED_BOOK_BREAKS.get();
        eliteProperties.forcedFloorBreaks =    RechantmentCommonConfigs.RARITY_2_FORCED_FLOOR_BREAKS.get();
        eliteProperties.bookBreakChance =      RechantmentCommonConfigs.RARITY_2_BOOK_BREAK_CHANCE.get();
        eliteProperties.floorBreakChance =     RechantmentCommonConfigs.RARITY_2_FLOOR_BREAK_CHANCE.get();
        eliteProperties.requiredBookShelves =  RechantmentCommonConfigs.RARITY_2_REQUIRED_BOOKSHELVES.get();
        eliteProperties.requiredLapis =        RechantmentCommonConfigs.RARITY_2_REQUIRED_LAPIS.get();
        eliteProperties.rerollGemChance =      RechantmentCommonConfigs.RARITY_2_REROLL_GEM_CHANCE.get();
        eliteProperties.minGrindstoneXP =      RechantmentCommonConfigs.RARITY_2_GRINDSTONE_XP_MIN.get();
        eliteProperties.maxGrindstoneXP =      RechantmentCommonConfigs.RARITY_2_GRINDSTONE_XP_MAX.get();
        eliteProperties.enchantmentPool =      EnchantmentPoolEntry.listFromString(RechantmentCommonConfigs.RARITY_2_ENCHANTMENTS.get());
        eliteProperties.enchantmentPoolTotalWeights = getEnchantmentPoolTotalWeight(eliteProperties.enchantmentPool);
        eliteProperties.iconResourceLocation = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/item/elite.png");

        blockLocation = ResourceLocation.parse(RechantmentCommonConfigs.RARITY_2_FLOOR_BLOCK_TYPE.get());
        eliteProperties.floorBlock = BuiltInRegistries.BLOCK.get(blockLocation);

        // Ultimate properties.
        ultimateProperties.rarity = 4.0f;
        ultimateProperties.key =                  RechantmentCommonConfigs.RARITY_3_KEY.get();
        ultimateProperties.color =                RechantmentCommonConfigs.RARITY_3_COLOR.get();
        ultimateProperties.requiredExp =          RechantmentCommonConfigs.RARITY_3_EXP_COST.get();
        ultimateProperties.worldSpawnWeight =     RechantmentCommonConfigs.RARITY_3_WORLD_SPAWN_WEIGHT.get();
        ultimateProperties.minSuccess =           RechantmentCommonConfigs.RARITY_3_MIN_SUCCESS.get();
        ultimateProperties.maxSuccess =           RechantmentCommonConfigs.RARITY_3_MAX_SUCCESS.get();
        ultimateProperties.forcedBookBreaks =     RechantmentCommonConfigs.RARITY_3_FORCED_BOOK_BREAKS.get();
        ultimateProperties.forcedFloorBreaks =    RechantmentCommonConfigs.RARITY_3_FORCED_FLOOR_BREAKS.get();
        ultimateProperties.bookBreakChance =      RechantmentCommonConfigs.RARITY_3_BOOK_BREAK_CHANCE.get();
        ultimateProperties.floorBreakChance =     RechantmentCommonConfigs.RARITY_3_FLOOR_BREAK_CHANCE.get();
        ultimateProperties.requiredBookShelves =  RechantmentCommonConfigs.RARITY_3_REQUIRED_BOOKSHELVES.get();
        ultimateProperties.requiredLapis =        RechantmentCommonConfigs.RARITY_3_REQUIRED_LAPIS.get();
        ultimateProperties.rerollGemChance =      RechantmentCommonConfigs.RARITY_3_REROLL_GEM_CHANCE.get();
        ultimateProperties.minGrindstoneXP =      RechantmentCommonConfigs.RARITY_3_GRINDSTONE_XP_MIN.get();
        ultimateProperties.maxGrindstoneXP =      RechantmentCommonConfigs.RARITY_3_GRINDSTONE_XP_MAX.get();
        ultimateProperties.enchantmentPool =      EnchantmentPoolEntry.listFromString(RechantmentCommonConfigs.RARITY_3_ENCHANTMENTS.get());
        ultimateProperties.enchantmentPoolTotalWeights = getEnchantmentPoolTotalWeight(ultimateProperties.enchantmentPool);
        ultimateProperties.iconResourceLocation = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/item/ultimate.png");


        blockLocation = ResourceLocation.parse(RechantmentCommonConfigs.RARITY_3_FLOOR_BLOCK_TYPE.get());
        ultimateProperties.floorBlock =  BuiltInRegistries.BLOCK.get(blockLocation);

        // Legendary properties.
        legendaryProperties.rarity = 5.0f;
        legendaryProperties.key =                  RechantmentCommonConfigs.RARITY_4_KEY.get();
        legendaryProperties.color =                RechantmentCommonConfigs.RARITY_4_COLOR.get();
        legendaryProperties.requiredExp =          RechantmentCommonConfigs.RARITY_4_EXP_COST.get();
        legendaryProperties.worldSpawnWeight =     RechantmentCommonConfigs.RARITY_4_WORLD_SPAWN_WEIGHT.get();
        legendaryProperties.minSuccess =           RechantmentCommonConfigs.RARITY_4_MIN_SUCCESS.get();
        legendaryProperties.maxSuccess =           RechantmentCommonConfigs.RARITY_4_MAX_SUCCESS.get();
        legendaryProperties.forcedBookBreaks =     RechantmentCommonConfigs.RARITY_4_FORCED_BOOK_BREAKS.get();
        legendaryProperties.forcedFloorBreaks =    RechantmentCommonConfigs.RARITY_4_FORCED_FLOOR_BREAKS.get();
        legendaryProperties.bookBreakChance =      RechantmentCommonConfigs.RARITY_4_BOOK_BREAK_CHANCE.get();
        legendaryProperties.floorBreakChance =     RechantmentCommonConfigs.RARITY_4_FLOOR_BREAK_CHANCE.get();
        legendaryProperties.requiredBookShelves =  RechantmentCommonConfigs.RARITY_4_REQUIRED_BOOKSHELVES.get();
        legendaryProperties.requiredLapis =        RechantmentCommonConfigs.RARITY_4_REQUIRED_LAPIS.get();
        legendaryProperties.rerollGemChance =      RechantmentCommonConfigs.RARITY_4_REROLL_GEM_CHANCE.get();
        legendaryProperties.minGrindstoneXP =      RechantmentCommonConfigs.RARITY_4_GRINDSTONE_XP_MIN.get();
        legendaryProperties.maxGrindstoneXP =      RechantmentCommonConfigs.RARITY_4_GRINDSTONE_XP_MAX.get();
        legendaryProperties.enchantmentPool =      EnchantmentPoolEntry.listFromString(RechantmentCommonConfigs.RARITY_4_ENCHANTMENTS.get());
        legendaryProperties.enchantmentPoolTotalWeights = getEnchantmentPoolTotalWeight(legendaryProperties.enchantmentPool);
        legendaryProperties.iconResourceLocation = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/item/legendary.png");

        blockLocation = ResourceLocation.parse(RechantmentCommonConfigs.RARITY_4_FLOOR_BLOCK_TYPE.get());
        legendaryProperties.floorBlock =  BuiltInRegistries.BLOCK.get(blockLocation);

        // Now add them all to hardcoded list.
        ALL_BOOK_PROPERTIES = new BookRarityProperties[5];
        ALL_BOOK_PROPERTIES[0] = simpleProperties;
        ALL_BOOK_PROPERTIES[1] = uniqueProperties;
        ALL_BOOK_PROPERTIES[2] = eliteProperties;
        ALL_BOOK_PROPERTIES[3] = ultimateProperties;
        ALL_BOOK_PROPERTIES[4] = legendaryProperties;

        return ALL_BOOK_PROPERTIES;
    }
}
