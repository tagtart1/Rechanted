package net.tagtart.rechantment.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.item.custom.WarpGemItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class BonusItemResolver {

    private BonusItemResolver() {
    }

    public static Optional<ItemStack> resolveRandomBonusItem(BookRarityProperties bookProperties, Random random) {
        Optional<BookRarityProperties.BonusPoolEntryType> randomBonusPoolEntryType = bookProperties.getRandomBonusPoolEntryWeighted(random);
        return resolveRandomBonusItem(randomBonusPoolEntryType, random);
    }

    public static Optional<ItemStack> resolveRandomBonusItem(Optional<BookRarityProperties.BonusPoolEntryType> entryType, Random random) {

        return entryType.map(bonusPoolEntryType -> switch (bonusPoolEntryType) {
            case MYSTERIOUS_BOOK -> Optional.of(new ItemStack(ModItems.MYSTERIOUS_BOOK.get()));
            case COMMON_GEM_POOL -> resolveGemPoolReward(RechantmentCommonConfigs.COMMON_GEM_POOL.get(),
                    "Bonus Item Pools.common_gem_pool", random);
            case RARE_GEM_POOL -> resolveGemPoolReward(RechantmentCommonConfigs.RARE_GEM_POOL.get(),
                    "Bonus Item Pools.rare_gem_pool", random);
        }).orElseGet(() -> Optional.of(new ItemStack(ModItems.MYSTERIOUS_BOOK.get())));
    }

    private static Optional<ItemStack> resolveGemPoolReward(List<? extends String> configuredPool, String sourceConfig,
            Random random) {
        ArrayList<Item> weightedItems = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (String configEntry : configuredPool) {
            String[] splitConfig = configEntry.split("\\|");
            if (splitConfig.length != 2) {
                Rechantment.LOGGER.warn("Invalid item pool entry format in {}: '{}'. Expected <item_id>|<weight>.",
                        sourceConfig, configEntry);
                continue;
            }

            String itemIdRaw = splitConfig[0].trim();
            ResourceLocation itemId = ResourceLocation.tryParse(itemIdRaw);
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                Rechantment.LOGGER.warn("Invalid or unknown item id in {}: '{}'.", sourceConfig, itemIdRaw);
                continue;
            }

            int weight;
            try {
                weight = Integer.parseInt(splitConfig[1].trim());
            } catch (NumberFormatException ex) {
                Rechantment.LOGGER.warn("Invalid item pool weight in {}: '{}'.", sourceConfig, splitConfig[1].trim());
                continue;
            }

            if (weight <= 0) {
                Rechantment.LOGGER.warn("Skipping item pool entry in {} because weight must be positive: '{}'.",
                        sourceConfig, configEntry);
                continue;
            }

            weightedItems.add(BuiltInRegistries.ITEM.get(itemId));
            weights.add(weight);
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            Rechantment.LOGGER.warn("No valid entries found in {}. Falling back to mysterious book.", sourceConfig);
            return Optional.of(new ItemStack(ModItems.MYSTERIOUS_BOOK.get()));
        }

        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < weightedItems.size(); ++i) {
            cumulativeWeight += weights.get(i);
            if (randomWeight < cumulativeWeight) {
                ItemStack rolledItem = new ItemStack(weightedItems.get(i));
                WarpGemItem.initializeRandomizedDurability(rolledItem, random);
                return Optional.of(rolledItem);
            }
        }

        Rechantment.LOGGER.warn("Weighted roll failed in {}. Falling back to mysterious book.", sourceConfig);
        return Optional.of(new ItemStack(ModItems.MYSTERIOUS_BOOK.get()));
    }
}
