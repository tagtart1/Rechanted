package net.tagtart.rechantment.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;

public class ReplaceEnchantedLootModifier extends LootModifier {

    public static final MapCodec<ReplaceEnchantedLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, ReplaceEnchantedLootModifier::new));

    public ReplaceEnchantedLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RechantmentCommonConfigs.REPLACE_ENCHANTED_LOOT.get()) {
            return generatedLoot;
        }

        boolean excludeLowerTiers = RechantmentCommonConfigs.EXCLUDE_LOWER_TIER_LOOT.get();
        String lootTableId = context.getQueriedLootTableId().toString();
        boolean isFishingLoot = lootTableId.contains("minecraft:gameplay/fishing");
        boolean nerfFishing = RechantmentCommonConfigs.NERF_FISHING_LOOT.get();

        for (int i = 0; i < generatedLoot.size(); ++i) {
            ItemStack itemStack = generatedLoot.get(i);
            if (itemStack.getItem() instanceof EnchantedBookItem) {
                continue;
            }

            if (!itemStack.isEnchanted()) {
                continue;
            }

            if (isFishingLoot && nerfFishing) {
                continue;
            }

            if (excludeLowerTiers && shouldExcludeLowerTier(itemStack.getItem())) {
                continue;
            }

            generatedLoot.set(i, LootModifierUtils.rollModdedBook(context));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    private static boolean shouldExcludeLowerTier(Item item) {
        if (item instanceof ArmorItem armorItem) {
            ArmorMaterial material = armorItem.getMaterial().value();
            return material == ArmorMaterials.LEATHER.value()
                    || material == ArmorMaterials.GOLD.value();
        }

        if (item instanceof TieredItem tieredItem) {
            Tier tier = tieredItem.getTier();
            return tier == Tiers.GOLD || tier == Tiers.WOOD || tier == Tiers.STONE;
        }

        return false;
    }
}
