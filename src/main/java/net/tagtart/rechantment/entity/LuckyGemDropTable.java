package net.tagtart.rechantment.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.item.custom.WarpGemItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class LuckyGemDropTable {
    private static final int MYSTERIOUS_BOOK_MIN = 2;
    private static final int MYSTERIOUS_BOOK_MAX = 5;
    private static final double BONUS_GEM_DROP_CHANCE = 0.02D;
    private static final List<Supplier<Item>> GEM_DROP_ITEMS = List.of(
            () -> ModItems.CHANCE_GEM.get(),
            () -> ModItems.SHINY_CHANCE_GEM.get(),
            () -> ModItems.RETURN_GEM.get(),
            () -> ModItems.TASTY_GEM.get(),
            () -> ModItems.WARP_GEM.get(),
            () -> ModItems.LUCKY_GEM.get(),
            () -> ModItems.CLONE_GEM.get(),
            () -> ModItems.SMITHING_GEM.get());
    private static final DropPool MATERIALS_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.IRON_BLOCK, 1, 3, 41),
            DropEntry.weighted(() -> Items.GOLD_BLOCK, 1, 3, 41),
            DropEntry.weighted(() -> Items.EMERALD_BLOCK, 1, 3, 10),
            DropEntry.weighted(() -> Items.DIAMOND_BLOCK, 1, 3, 4),
            DropEntry.weighted(() -> Items.ANCIENT_DEBRIS, 1, 3, 4)));

    private static final DropPool GOLDEN_FOOD_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.GOLDEN_CARROT, 3, 12, 48),
            DropEntry.weighted(() -> Items.GOLDEN_APPLE, 1, 2, 48),
            DropEntry.weighted(() -> Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 4)));

    private static final DropPool UTILITY_POOL = new DropPool(List.of(
            DropEntry.weighted(() -> Items.ENDER_PEARL, 2, 4, 20),
            DropEntry.weighted(() -> Items.SLIME_BALL, 3, 5, 20),
            DropEntry.weighted(() -> Items.GLOW_INK_SAC, 3, 5, 20),
            DropEntry.weighted(() -> Items.TURTLE_SCUTE, 3, 5, 20),
            DropEntry.weighted(() -> Items.ARMADILLO_SCUTE, 3, 5, 20)));

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
            DropEntry.weighted(() -> Items.PINK_WOOL, 5, 12, 1)));

    private LuckyGemDropTable() {
    }

    public static List<ItemStack> rollDrops(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        int mysteriousBookCount = random.nextIntBetweenInclusive(MYSTERIOUS_BOOK_MIN, MYSTERIOUS_BOOK_MAX);
        for (int i = 0; i < mysteriousBookCount; i++) {
            drops.add(new ItemStack(ModItems.MYSTERIOUS_BOOK.get(), 1));
        }
        drops.add(new ItemStack(Items.EXPERIENCE_BOTTLE, random.nextIntBetweenInclusive(1, 32)));
        drops.add(MATERIALS_POOL.roll(random));
        drops.add(GOLDEN_FOOD_POOL.roll(random));
        drops.add(UTILITY_POOL.roll(random));
        drops.add(WOOL_POOL.roll(random));

        if (random.nextDouble() < BONUS_GEM_DROP_CHANCE) {
            Supplier<Item> randomGem = GEM_DROP_ITEMS.get(random.nextInt(GEM_DROP_ITEMS.size()));
            ItemStack bonusGem = new ItemStack(randomGem.get(), 1);
            WarpGemItem.initializeRandomizedDurability(bonusGem, random);
            drops.add(bonusGem);
        }

        return drops;
    }

    public static boolean isGemRewardItem(ItemStack stack) {
        for (Supplier<Item> gemItemSupplier : GEM_DROP_ITEMS) {
            if (stack.is(gemItemSupplier.get())) {
                return true;
            }
        }

        return false;
    }

    private record DropEntry(Supplier<Item> itemSupplier, int minCount, int maxCount, int weight) {
        static DropEntry weighted(Supplier<Item> itemSupplier, int minCount, int maxCount, int weight) {
            return new DropEntry(itemSupplier, minCount, maxCount, weight);
        }

        ItemStack roll(RandomSource random) {
            int count = random.nextIntBetweenInclusive(this.minCount, this.maxCount);
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
