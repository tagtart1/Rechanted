package net.tagtart.rechantment.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.item.ModItems;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class VillagerTradeEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (!RechantmentCommonConfigs.MODIFY_VILLAGER_TRADES.get()) {
            return;
        }

        VillagerProfession profession = event.getType();
        Int2ObjectMap<List<ItemListing>> trades = event.getTrades();

        for (int level : trades.keySet()) {
            List<ItemListing> listings = trades.get(level);
            int listingCount = listings == null ? 0 : listings.size();
            Rechantment.LOGGER.info("Villager trades: profession={}, level={}, entries={}", profession, level, listingCount);

            if (listings == null || listings.isEmpty()) {
                continue;
            }

            for (int i = 0; i < listings.size(); i++) {
                ItemListing original = listings.get(i);
                int tradeLevel = level;

                listings.set(i, (entity, random) -> {
                    MerchantOffer offer = original.getOffer(entity, random);
                    if (offer == null) {
                        return null;
                    }

                    ItemStack result = offer.getResult();
                    if (result.getItem() instanceof EnchantedBookItem) {
                        ItemStack mysteriousBook = new ItemStack(ModItems.MYSTERIOUS_BOOK.get());
                        int emeraldCount = RechantmentCommonConfigs.VILLAGER_MYSTERIOUS_BOOK_EMERALD_COST.get();
                        ItemCost emeraldCost = new ItemCost(Items.EMERALD, emeraldCount);
                        ItemCost bookCost = new ItemCost(Items.BOOK, 1);

                        Rechantment.LOGGER.info(
                                "Replaced enchanted book trade with mysterious book: profession={}, level={}",
                                profession,
                                tradeLevel
                        );

                        return new MerchantOffer(
                                emeraldCost,
                                Optional.of(bookCost),
                                mysteriousBook,
                                offer.getMaxUses(),
                                offer.getXp(),
                                offer.getPriceMultiplier()
                        );
                    }


                    if (!result.isEnchanted()) {
                        return offer;
                    }

                    result.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                    Rechantment.LOGGER.info(
                            "Stripped enchantments from villager trade: profession={}, level={}, originalResult={}",
                            profession,
                            tradeLevel,
                            result
                    );

                    return offer;
                });
            }
        }

        // Modify offers by adding/removing ItemListing entries in event.getTrades().
        // Example: trades.get(1).add(new BasicItemListing(...));
    }
}
