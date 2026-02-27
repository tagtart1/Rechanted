package net.tagtart.rechanted.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.util.BookRarityProperties;
import net.tagtart.rechanted.util.UtilFunctions;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.RECHANTED_BOOK.get(), ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "book_rarity"),

                (stack, level, entity, seed) -> {
                    ItemEnchantments itemEnchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                    float book_rarity = 0f;

                    if (itemEnchants == null) return book_rarity;
                    if (itemEnchants.entrySet().isEmpty()) return book_rarity;

                    // We only care about the books first enchant as they should only have one anyway
                    Object2IntMap.Entry<Holder<Enchantment>> bookEnchant = itemEnchants.entrySet().iterator().next();
                    int enchantLevel = bookEnchant.getIntValue();
                    String enchantId = bookEnchant.getKey().getRegisteredName();


                    BookRarityProperties bookRarityProperties = UtilFunctions.getPropertiesFromEnchantment(enchantId);
                    if (bookRarityProperties != null) {
                        book_rarity = bookRarityProperties.rarity;
                    }

                    return book_rarity;
                });
    }
}
