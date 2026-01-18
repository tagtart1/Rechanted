package net.tagtart.rechantment.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.tagtart.rechantment.Rechantment;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> OVERLOAD_ENCHANTABLE = tag("overload_enchantable");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, name));
        }
    }
}

