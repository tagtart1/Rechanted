package net.tagtart.rechanted.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechanted.config.RechantedCommonConfigs;
import net.tagtart.rechanted.item.ModItems;

public class AddDustyMysteriousBookToFishingLootModifier extends LootModifier {

    public static final MapCodec<AddDustyMysteriousBookToFishingLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, AddDustyMysteriousBookToFishingLootModifier::new));

    public AddDustyMysteriousBookToFishingLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {

        String lootTableId = context.getQueriedLootTableId().toString();
        if (!lootTableId.contains("minecraft:gameplay/fishing/junk")) {
            return generatedLoot;
        }

        for (int i = 0; i < generatedLoot.size(); ++i) {
            double randomRoll = context.getRandom().nextDouble();
            if (randomRoll < RechantedCommonConfigs.RARITY_0_FISHING_JUNK_DROP_CHANCE.get()) {
                ItemStack dustyMysteriousBook = new ItemStack(ModItems.DUSTY_MYSTERIOUS_BOOK.get());
                generatedLoot.set(i, dustyMysteriousBook);
            }
        }

        return generatedLoot;
    }


}
