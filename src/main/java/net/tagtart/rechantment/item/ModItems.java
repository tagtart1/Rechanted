package net.tagtart.rechantment.item;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.custom.*;

public class  ModItems {
     public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Rechantment.MOD_ID);

     public static final DeferredItem<Item> CHANCE_GEM = ITEMS.register("chance_gem",
             () -> new ChanceGemItem(new Item.Properties()));

     public static final DeferredItem<Item> RETURN_GEM = ITEMS.register("return_gem",
             () -> new ReturnGemItem(new Item.Properties()));

     public static final DeferredItem<Item> TASTY_GEM = ITEMS.register("tasty_gem",
             () -> new TastyGemItem(new Item.Properties().food(ModFoodProperties.TASTY_GEM)));

     public static final DeferredItem<Item> WARP_GEM = ITEMS.register("warp_gem",
             () -> new WarpGemItem(new Item.Properties().durability(64)));

     public static final DeferredItem<Item> CLONE_GEM = ITEMS.register("clone_gem",
             () -> new CloneGemItem(new Item.Properties()));

     public static final DeferredItem<Item> SMITHING_GEM = ITEMS.register("smithing_gem",
             SmithingGemItem::new);

     public static final DeferredItem<Item> RECHANTMENT_BOOK = ITEMS.register("rechantment_book", () -> new RechantmentBookItem(new Item.Properties()));

     public static void register(IEventBus eventBus) {
         ITEMS.register(eventBus);
     }
}
