package net.tagtart.rechanted.event;

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
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.config.RechantedCommonConfigs;
import net.tagtart.rechanted.item.ModItems;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class VillagerTradeEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (!shouldModifyTrades()) {
            return;
        }

        Int2ObjectMap<List<ItemListing>> trades = event.getTrades();

        for (int level : trades.keySet()) {
            List<ItemListing> listings = trades.get(level);

            if (listings == null || listings.isEmpty()) {
                continue;
            }

            replaceListingsForLevel(listings, level);
        }
    }

    private static boolean shouldModifyTrades() {
        return RechantedCommonConfigs.MODIFY_VILLAGER_TRADES.get();
    }


    private static void replaceListingsForLevel(List<ItemListing> listings, int level) {
        for (int i = 0; i < listings.size(); i++) {
            ItemListing original = listings.get(i);
            listings.set(i, wrapListing(original, level));
        }
    }

    private static ItemListing wrapListing(ItemListing original, int tradeLevel) {
        return (entity, random) -> {
            MerchantOffer offer = original.getOffer(entity, random);
            if (offer == null) {
                return null;
            }

            return adjustOffer(offer, tradeLevel);
        };
    }

    private static MerchantOffer adjustOffer(MerchantOffer offer, int tradeLevel) {
        ItemStack result = offer.getResult();
        if (result.getItem() instanceof EnchantedBookItem) {
            return replaceEnchantedBookOffer(offer, tradeLevel);
        }

        if (!result.isEnchanted()) {
            return offer;
        }

        stripEnchantments(result);
        return offer;
    }

    private static MerchantOffer replaceEnchantedBookOffer(MerchantOffer offer, int tradeLevel) {
        if (isApprenticeLevel(tradeLevel)) {
            return createCandleOffer(offer);
        }

        if (isBelowJourneyman(tradeLevel)) {
            return createAmethystOffer(offer);
        }

        return createMysteriousBookOffer(offer);
    }

    private static boolean isApprenticeLevel(int tradeLevel) {
        return tradeLevel == 2;
    }

    private static boolean isBelowJourneyman(int tradeLevel) {
        return tradeLevel < 3;
    }

    private static MerchantOffer createMysteriousBookOffer(MerchantOffer originalOffer) {
        ItemStack mysteriousBook = new ItemStack(ModItems.MYSTERIOUS_BOOK.get());
        int emeraldCount = RechantedCommonConfigs.VILLAGER_MYSTERIOUS_BOOK_EMERALD_COST.get();
        ItemCost emeraldCost = new ItemCost(Items.EMERALD, emeraldCount);
        ItemCost bookCost = new ItemCost(Items.BOOK, 1);
        
        return buildOffer(originalOffer, emeraldCost, Optional.of(bookCost), mysteriousBook, 3);
    }

    private static MerchantOffer createAmethystOffer(MerchantOffer originalOffer) {
        ItemCost emeraldCost = new ItemCost(Items.EMERALD, 9);
        ItemStack amethyst = new ItemStack(Items.AMETHYST_SHARD, 3);

        return buildOffer(originalOffer, emeraldCost, Optional.empty(), amethyst, originalOffer.getMaxUses());
    }

    private static MerchantOffer createCandleOffer(MerchantOffer originalOffer) {
        ItemCost emeraldCost = new ItemCost(Items.EMERALD, 4);
        ItemStack candle = new ItemStack(Items.CANDLE, 1);

        return buildOffer(originalOffer, emeraldCost, Optional.empty(), candle, originalOffer.getMaxUses());
    }

    private static MerchantOffer buildOffer(
            MerchantOffer originalOffer,
            ItemCost primaryCost,
            Optional<ItemCost> secondaryCost,
            ItemStack result,
            int maxUses
    ) {
        return new MerchantOffer(
                primaryCost,
                secondaryCost,
                result,
                maxUses,
                originalOffer.getXp(),
                originalOffer.getPriceMultiplier()
        );
    }

    private static void stripEnchantments(ItemStack result) {
        result.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

    }
}
