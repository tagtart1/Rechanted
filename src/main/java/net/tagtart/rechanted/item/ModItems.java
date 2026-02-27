package net.tagtart.rechanted.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.item.custom.*;

public class  ModItems {
     public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Rechanted.MOD_ID);

     public static final DeferredItem<Item> CHANCE_GEM = ITEMS.register("chance_gem",
             () -> new ChanceGemItem(new Item.Properties()));

     public static final DeferredItem<Item> SHINY_CHANCE_GEM = ITEMS.register("shiny_chance_gem",
             () -> new ShinyChanceGemItem(new Item.Properties()));

     public static final DeferredItem<Item> RETURN_GEM = ITEMS.register("return_gem",
             () -> new ReturnGemItem(new Item.Properties()));

     public static final DeferredItem<Item> TASTY_GEM = ITEMS.register("tasty_gem",
             () -> new TastyGemItem(new Item.Properties().food(ModFoodProperties.TASTY_GEM)));

     public static final DeferredItem<Item> WARP_GEM = ITEMS.register("warp_gem",
             () -> new WarpGemItem(new Item.Properties().durability(24)));

     public static final DeferredItem<Item> LUCKY_GEM = ITEMS.register("lucky_gem",
             () -> new LuckyGemItem(new Item.Properties()));

     public static final DeferredItem<Item> CLONE_GEM = ITEMS.register("clone_gem",
             () -> new CloneGemItem(new Item.Properties()));

     public static final DeferredItem<Item> SMITHING_GEM = ITEMS.register("smithing_gem",
             SmithingGemItem::new);

     public static final DeferredItem<Item> RECHANTED_BOOK = ITEMS.register("rechanted_book", () -> new RechantedBookItem(new Item.Properties()));

     public static final DeferredItem<Item> MYSTERIOUS_BOOK = ITEMS.register("mysterious_book", () -> new MysteriousBookItem(new Item.Properties()));

     public static final DeferredItem<Item> LEGENDARY_PULL_ICON = ITEMS.register("advancement_legendary_pull",
             () -> new Item(new Item.Properties()));

     public static final DeferredItem<Item> ARCHMAGE_ICON = ITEMS.register("advancement_archmage",
             () -> new Item(new Item.Properties()));
     public static final DeferredItem<Item> CHRONICLER_ICON = ITEMS.register("advancement_chronicler",
             () -> new Item(new Item.Properties()));

     public static void register(IEventBus eventBus) {
         ITEMS.register(eventBus);
     }
}
