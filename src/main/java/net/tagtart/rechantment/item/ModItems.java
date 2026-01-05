package net.tagtart.rechantment.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.custom.ChanceGemItem;
import net.tagtart.rechantment.item.custom.RechantmentBookItem;

public class  ModItems {
     public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Rechantment.MOD_ID);

     public static final DeferredItem<Item> CHANCE_GEM = ITEMS.register("chance_gem",
             () -> new ChanceGemItem(new Item.Properties()));

     public static final DeferredItem<Item> RECHANTMENT_BOOK = ITEMS.register("rechantment_book", () -> new RechantmentBookItem(new Item.Properties()));

     public static void register(IEventBus eventBus) {
         ITEMS.register(eventBus);
     }
}
