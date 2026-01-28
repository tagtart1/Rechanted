package net.tagtart.rechantment.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.Random;

final class LootModifierUtils {
    private LootModifierUtils() {
    }

    static ItemStack rollModdedBook(LootContext context) {
        ItemStack replacementBook = new ItemStack(ModItems.RECHANTMENT_BOOK.get());
        BookRarityProperties bookRarityProperties = BookRarityProperties.getRandomRarityWeighted();
        EnchantmentPoolEntry randomEnchantment = bookRarityProperties.getRandomEnchantmentWeighted();
        int enchantmentLevel = randomEnchantment.getRandomEnchantLevelWeighted();

        Random random = new Random();
        int successRate = random.nextInt(bookRarityProperties.minSuccess, bookRarityProperties.maxSuccess);

        Holder.Reference<Enchantment> enchantment = UtilFunctions.getEnchantmentReferenceIfPresent(
                context.getLevel().registryAccess(),
                randomEnchantment.enchantment
        );
        if (enchantment == null) {
            return replacementBook;
        }

        ItemEnchantments.Mutable storedEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        storedEnchants.set(enchantment, enchantmentLevel);

        replacementBook.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants.toImmutable().withTooltip(false));
        replacementBook.set(ModDataComponents.SUCCESS_RATE, successRate);

        if (UtilFunctions.shouldAnnounceDrop(randomEnchantment.enchantment, enchantmentLevel)) {
            replacementBook.set(ModDataComponents.ANNOUNCE_ON_FOUND, true);
        }

        return replacementBook;
    }
}
