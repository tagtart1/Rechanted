package net.tagtart.rechantment.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.tagtart.rechantment.Rechantment;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.RECHANTMENT_BOOK.get(), ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "book_rarity"),

                // Determine property value method
                // TODO: update to check what rarity the enchant is set in the config
                (stack, level, entity, seed) ->
                        stack.get(DataComponents.STORED_ENCHANTMENTS) != null ? 1f: 0f
                );
    }

}
