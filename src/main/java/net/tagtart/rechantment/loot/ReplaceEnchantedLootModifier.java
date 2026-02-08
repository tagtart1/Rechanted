package net.tagtart.rechantment.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.util.UtilFunctions;

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

        if (isEquipmentLootTable(context)) {
            return generatedLoot;
        }

        boolean excludeLowerTiers = RechantmentCommonConfigs.EXCLUDE_LOWER_TIER_LOOT.get();
        String lootTableId =  context.getQueriedLootTableId().toString();
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

            generatedLoot.set(i, UtilFunctions.rollModdedBook(context.getLevel().registryAccess()));
        }

        return generatedLoot;
    }

    private static boolean isEquipmentLootTable(LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        return isEquipmentParamSet(context, lootTableId);
    }

    private static boolean isEquipmentParamSet(LootContext context, ResourceLocation lootTableId) {
        HolderGetter<LootTable> lootTables = context.getResolver()
                .lookup(Registries.LOOT_TABLE)
                .orElse(null);
        if (lootTables == null) {
            return false;
        }

        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableId);
        Holder.Reference<LootTable> lootTableHolder = lootTables.get(lootTableKey).orElse(null);
        if (lootTableHolder == null) {
            return false;
        }

        LootContextParamSet paramSet = lootTableHolder.value().getParamSet();
        return paramSet == LootContextParamSets.EQUIPMENT;
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
