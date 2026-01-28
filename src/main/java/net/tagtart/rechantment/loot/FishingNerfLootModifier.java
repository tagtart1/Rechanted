package net.tagtart.rechantment.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;

import java.util.Optional;

public class FishingNerfLootModifier extends LootModifier {

    public static final MapCodec<FishingNerfLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, FishingNerfLootModifier::new));

    public FishingNerfLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RechantmentCommonConfigs.NERF_FISHING_LOOT.get()) {
            return generatedLoot;
        }

        String lootTableId = context.getQueriedLootTableId().toString();
        if (!lootTableId.contains("minecraft:gameplay/fishing")) {
            return generatedLoot;
        }

        RegistryAccess registryAccess = context.getLevel().registryAccess();
        Optional<? extends HolderSet<Enchantment>> possibleEnchantments = registryAccess
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.IN_ENCHANTING_TABLE);

        for (int i = 0; i < generatedLoot.size(); ++i) {
            ItemStack itemStack = generatedLoot.get(i);
            if (!itemStack.isEnchanted()) {
                continue;
            }

            itemStack.set(DataComponents.ENCHANTMENTS, null);
            EnchantmentHelper.enchantItem(RandomSource.create(), itemStack, 5, registryAccess, possibleEnchantments);
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
