package net.tagtart.rechantment.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.util.BookRarityProperties;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.RECHANTMENT_BOOK.get(), ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "book_rarity"),

                (stack, level, entity, seed) -> {
                    ItemEnchantments itemEnchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                    float book_rarity = 0f;

                    if (itemEnchants == null) return book_rarity;

                    // We only care about the books first enchant as they should only have one anyway
                    Object2IntMap.Entry<Holder<Enchantment>> bookEnchant = itemEnchants.entrySet().iterator().next();
                    int enchantLevel = bookEnchant.getIntValue();
                    String enchantId = bookEnchant.getKey().getRegisteredName();


                    for (BookRarityProperties bookProperties : BookRarityProperties.getAllProperties()) {
                        if (bookProperties.isEnchantmentInPool(enchantId, enchantLevel)) {
                            book_rarity = bookProperties.rarity;
                        }
                    }
                    return book_rarity;
                });
    }
}
