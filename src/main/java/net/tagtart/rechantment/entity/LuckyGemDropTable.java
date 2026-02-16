package net.tagtart.rechantment.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tagtart.rechantment.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class LuckyGemDropTable {
    private static final DropPool MATERIALS_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.IRON_BLOCK, 1, 3, 41),
            DropEntry.weighted(() -> Items.GOLD_BLOCK, 1, 3, 41),
            DropEntry.weighted(() -> Items.EMERALD_BLOCK, 1, 3, 10),
            DropEntry.weighted(() -> Items.DIAMOND_BLOCK, 1, 3, 4),
            DropEntry.weighted(() -> Items.ANCIENT_DEBRIS, 1, 3, 4)
    ));

    private static final DropPool GOLDEN_FOOD_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.GOLDEN_CARROT, 3, 12, 48),
            DropEntry.weighted(() -> Items.GOLDEN_APPLE, 1, 2, 48),
            DropEntry.weighted(() -> Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 4)
    ));

    private static final DropPool UTILITY_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.ENDER_PEARL, 2, 4, 20),
            DropEntry.weighted(() -> Items.SLIME_BALL, 3, 5, 20),
            DropEntry.weighted(() -> Items.GLOW_INK_SAC, 3, 5, 20),
            DropEntry.weighted(() -> Items.TURTLE_SCUTE, 3, 5, 20),
            DropEntry.weighted(() -> Items.ARMADILLO_SCUTE, 3, 5, 20)
    ));

    private static final DropPool WOOL_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.RED_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.ORANGE_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.YELLOW_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.LIME_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.GREEN_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.CYAN_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.LIGHT_BLUE_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.BLUE_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.PURPLE_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.MAGENTA_WOOL, 5, 12, 1),
            DropEntry.weighted(() -> Items.PINK_WOOL, 5, 12, 1)
    ));

    private LuckyGemDropTable() {
    }

    public static List<ItemStack> rollDrops(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();

        drops.add(DropEntry.weighted(ModItems.MYSTERIOUS_BOOK::get, 2, 5, 1).roll(random));
        drops.add(MATERIALS_POOL.roll(random));
        drops.add(GOLDEN_FOOD_POOL.roll(random));
        drops.add(UTILITY_POOL.roll(random));
        drops.add(WOOL_POOL.roll(random));

        if (random.nextDouble() < 0.015D) {
            // TODO: Replace with weighted random gem roll once the shared gem-weight table is available.
            drops.add(new ItemStack(ModItems.CHANCE_GEM.get(), 1));
        }

        return drops;
    }

    private record DropEntry(Supplier<Item> itemSupplier, int minCount, int maxCount, int weight) {
        static DropEntry weighted(Supplier<Item> itemSupplier, int minCount, int maxCount, int weight) {
            return new DropEntry(itemSupplier, minCount, maxCount, weight);
        }

        ItemStack roll(RandomSource random) {
            int count = random.nextInt(this.maxCount - this.minCount + 1) + this.minCount;
            return new ItemStack(this.itemSupplier.get(), count);
        }
    }

    private record DropPool(List<DropEntry> entries) {
        ItemStack roll(RandomSource random) {
            int totalWeight = this.entries.stream().mapToInt(DropEntry::weight).sum();
            int roll = random.nextInt(totalWeight);
            int cumulativeWeight = 0;
            for (DropEntry entry : this.entries) {
                cumulativeWeight += entry.weight();
                if (roll < cumulativeWeight) {
                    return entry.roll(random);
                }
            }

            return this.entries.get(this.entries.size() - 1).roll(random);
        }
    }
}
