package net.tagtart.rechantment.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.tagtart.rechantment.Rechantment;

import java.util.List;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class VillagerTradeEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();
        Int2ObjectMap<List<ItemListing>> trades = event.getTrades();

        for (int level : trades.keySet()) {
            List<ItemListing> listings = trades.get(level);
            int listingCount = listings == null ? 0 : listings.size();
            Rechantment.LOGGER.info("Villager trades: profession={}, level={}, entries={}", profession, level, listingCount);



        }

        // Modify offers by adding/removing ItemListing entries in event.getTrades().
        // Example: trades.get(1).add(new BasicItemListing(...));
    }
}
