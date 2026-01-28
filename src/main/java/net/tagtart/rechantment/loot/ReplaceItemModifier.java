package net.tagtart.rechantment.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.Optional;
import java.util.Random;

public class ReplaceItemModifier extends LootModifier {

    public static final MapCodec<ReplaceItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, ReplaceItemModifier::new));


    public ReplaceItemModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }


    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for(int i = 0; i < generatedLoot.size(); ++i) {
            ItemStack itemStack = generatedLoot.get(i);

            boolean replaceEnchantedLoot = RechantmentCommonConfigs.REPLACE_ENCHANTED_LOOT.get();
            boolean excludeLowerTiers = RechantmentCommonConfigs.EXCLUDE_LOWER_TIER_LOOT.get();
            boolean nerfFishing = RechantmentCommonConfigs.NERF_FISHING_LOOT.get();
            String lootTableId = context.getQueriedLootTableId().toString();
            // Replace vanilla enchantment books
            if (itemStack.getItem() instanceof EnchantedBookItem) {
                ItemStack replacementBook = rollModdedBook(context);
                generatedLoot.set(i, replacementBook);
                continue;
            }


            // TODO: move the fishing nerf into a seperate modifier with loot condition.
            if (replaceEnchantedLoot && itemStack.isEnchanted()) {
                Item item = itemStack.getItem();

                if ((lootTableId.contains("minecraft:gameplay/fishing") && nerfFishing)) {
                    itemStack.set(DataComponents.ENCHANTMENTS, null);
                    RegistryAccess registryAccess = context.getLevel().registryAccess();
                    Optional<? extends HolderSet<Enchantment>> possibleEnchantments = registryAccess
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .get(EnchantmentTags.IN_ENCHANTING_TABLE);
                    EnchantmentHelper.enchantItem(RandomSource.create(), itemStack, 5, registryAccess, possibleEnchantments);
                    continue;
                }

                if (excludeLowerTiers) {
                    // Excludes gold, leather, and chain armor
                    if (item instanceof ArmorItem armorItem) {
                        ArmorMaterial material = armorItem.getMaterial().value();
                        if (material == ArmorMaterials.LEATHER.value() ||
                                material == ArmorMaterials.GOLD.value()) {
                            continue;
                        }
                    }

                    // Excludes gold tool
                    else if (item instanceof TieredItem tieredItem) {
                        Tier tier = tieredItem.getTier();
                        if (tier == Tiers.GOLD ||
                                tier == Tiers.WOOD ||
                                tier == Tiers.STONE) {
                            continue;
                        }
                    }
                }


                // The item is an enchanted piece of gear so replace it with a rolled book
                ItemStack replacementBook = rollModdedBook(context);
                generatedLoot.set(i, replacementBook);
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    private ItemStack rollModdedBook(LootContext context) {
        ItemStack replacementBook = new ItemStack(ModItems.RECHANTMENT_BOOK.get());
        BookRarityProperties bookRarityProperties = BookRarityProperties.getRandomRarityWeighted();
        EnchantmentPoolEntry randomEnchantment = bookRarityProperties.getRandomEnchantmentWeighted();
        int enchantmentLevel = randomEnchantment.getRandomEnchantLevelWeighted();

        Random random = new Random();
        int successRate = random.nextInt(bookRarityProperties.minSuccess, bookRarityProperties.maxSuccess);

        Holder.Reference<Enchantment> enchantment = UtilFunctions.getEnchantmentReferenceIfPresent(
                context.getLevel().registryAccess(),
                randomEnchantment.enchantment
        );
        if (enchantment == null) {
            return replacementBook;
        }

        ItemEnchantments.Mutable storedEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        storedEnchants.set(enchantment, enchantmentLevel);

        replacementBook.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants.toImmutable().withTooltip(false));
        replacementBook.set(ModDataComponents.SUCCESS_RATE, successRate);

        if (UtilFunctions.shouldAnnounceDrop(randomEnchantment.enchantment, enchantmentLevel)) {
            replacementBook.set(ModDataComponents.ANNOUNCE_ON_FOUND, true);
        }

        return replacementBook;
    }
}
