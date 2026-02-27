package net.tagtart.rechanted.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechanted.config.RechantedCommonConfigs;

public class RemoveMendingLootModifier extends LootModifier {

    public static final MapCodec<RemoveMendingLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, RemoveMendingLootModifier::new));

    public RemoveMendingLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RechantedCommonConfigs.REMOVE_MENDING_ENABLED.get()) {
            return generatedLoot;
        }

        for (int i = 0; i < generatedLoot.size(); ++i) {
            ItemStack itemStack = generatedLoot.get(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            if (itemStack.has(DataComponents.ENCHANTMENTS)) {
                itemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {
                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
                    mutable.removeIf((enchantment) -> enchantment.getKey() == Enchantments.MENDING);
                    return mutable.toImmutable();
                });
            }

            if (itemStack.has(DataComponents.STORED_ENCHANTMENTS)) {
                itemStack.update(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {
                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
                    mutable.removeIf((enchantment) -> enchantment.getKey() == Enchantments.MENDING);
                    return mutable.toImmutable();
                });
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
