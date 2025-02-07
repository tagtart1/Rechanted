package net.tagtart.rechantment.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.item.ModItems;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.ENCHANTED_BOOK.get(),
                new ResourceLocation(Rechantment.MOD_ID, "book_rarity" ),
                (itemStack, clientLevel, livingEntity, i) -> {
                    CompoundTag nbt = itemStack.getTag();
                    float rarity = 0f;
                    if (nbt == null) {return rarity;}

                    CompoundTag enchantmentTag = nbt.getCompound("Enchantment");
                    String enchantmentId = enchantmentTag.getString("id");
                    int enchantmentLevel = enchantmentTag.getInt("lvl");

                    if (nbt.getBoolean("IconOnly")) {
                        rarity = 5f;
                        return rarity;
                    }

                    for (BookRarityProperties bookProperties : BookRarityProperties.getAllProperties()) {
                        if (bookProperties.isEnchantmentInPool(enchantmentId, enchantmentLevel)) {
                            rarity = bookProperties.rarity;
                        }
                    }
                    return rarity;
                });
    }
}
