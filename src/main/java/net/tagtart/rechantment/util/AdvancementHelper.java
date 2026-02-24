package net.tagtart.rechantment.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.ModItems;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public final class AdvancementHelper {
    private static final ResourceLocation UPGRADE_ENCHANT_TABLE_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "upgrade_enchanting_table");
    private static final String UPGRADE_ENCHANT_TABLE_CRITERION = "use_emerald_on_enchanting_table";

    private static final ResourceLocation POWER_ENCHANT_TABLE_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "power_enchanting_table");
    private static final String POWER_ENCHANT_TABLE_CRITERION = "power_up_enchanting_table";

    private static final ResourceLocation LEGENDARY_PULL_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "legendary_pull");
    private static final String LEGENDARY_PULL_CRITERION = "hold_legendary_book";

    private static final ResourceLocation EXCALIBUR_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "excalibur");
    private static final String EXCALIBUR_CRITERION = "hold_max_sword_enchants";

    private static final ResourceLocation GEM_FROM_LUCKY_GEM_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "gem_from_lucky_gem");
    private static final String GEM_FROM_LUCKY_GEM_CRITERION = "gem_from_lucky_gem";

    private AdvancementHelper() {
    }

    public static void awardUpgradeEnchantingTableAdvancement(Player player, ServerLevel level) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(UPGRADE_ENCHANT_TABLE_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, UPGRADE_ENCHANT_TABLE_CRITERION);
        }
    }

    public static void awardExcaliburAdvancementIfEligible(Player player, ServerLevel level) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack heldItem = serverPlayer.getMainHandItem();
        if (heldItem.isEmpty() || !heldItem.is(ItemTags.SWORDS)) {
            return;
        }

        Set<ResourceKey<Enchantment>> requiredSwordEnchantments = getConfiguredSwordEnchantmentsForItem(heldItem,
                level);
        if (requiredSwordEnchantments.isEmpty()) {
            return;
        }

        ItemEnchantments enchantments = heldItem.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<ResourceKey<Enchantment>> presentEnchantments = new HashSet<>();
        for (var entry : enchantments.entrySet()) {
            entry.getKey().unwrapKey().ifPresent(presentEnchantments::add);
        }

        if (!presentEnchantments.containsAll(requiredSwordEnchantments)) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(EXCALIBUR_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, EXCALIBUR_CRITERION);
        }
    }

    private static Set<ResourceKey<Enchantment>> getConfiguredSwordEnchantmentsForItem(ItemStack item,
            ServerLevel level) {
        Set<ResourceKey<Enchantment>> requiredEnchantments = new LinkedHashSet<>();

        for (BookRarityProperties rarityProperties : BookRarityProperties.getAllProperties()) {
            for (EnchantmentPoolEntry poolEntry : rarityProperties.enchantmentPool) {
                Holder.Reference<Enchantment> enchantmentHolder = UtilFunctions
                        .getEnchantmentReferenceIfPresent(level.registryAccess(), poolEntry.enchantment);
                if (enchantmentHolder == null) {
                    continue;
                }

                if (!item.supportsEnchantment(enchantmentHolder)) {
                    continue;
                }

                enchantmentHolder.unwrapKey().ifPresent(requiredEnchantments::add);
            }
        }

        return requiredEnchantments;
    }

    public static void awardLegendaryPullAdvancementIfEligible(Player player, ServerLevel level) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BookRarityProperties legendaryRarity = Arrays.stream(BookRarityProperties.getAllProperties())
                .max(Comparator.comparingDouble(properties -> properties.rarity))
                .orElse(null);
        if (legendaryRarity == null) {
            return;
        }

        if (!hasLegendaryBookInInventory(serverPlayer.getInventory(), legendaryRarity)) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(LEGENDARY_PULL_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, LEGENDARY_PULL_CRITERION);
        }
    }

    public static void awardPowerUpEnchantTableAdvancementNearPos(ServerLevel serverLevel, BlockPos centerPos) {
        AABB area = new AABB(
                centerPos.getX() - 2, centerPos.getY() - 1, centerPos.getZ() - 2,
                centerPos.getX() + 2, centerPos.getY() + 1, centerPos.getZ() + 2);

        var advancement = serverLevel.getServer().getAdvancements().get(POWER_ENCHANT_TABLE_ADVANCEMENT_ID);
        if (advancement == null) {
            return;
        }

        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.getAdvancements().award(advancement, POWER_ENCHANT_TABLE_CRITERION);
        }
    }

    private static boolean hasLegendaryBookInInventory(Inventory inventory, BookRarityProperties legendaryRarity) {
        for (ItemStack stack : inventory.items) {
            if (isLegendaryRechantmentBook(stack, legendaryRarity)) {
                return true;
            }
        }

        for (ItemStack stack : inventory.offhand) {
            if (isLegendaryRechantmentBook(stack, legendaryRarity)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isLegendaryRechantmentBook(ItemStack stack, BookRarityProperties legendaryRarity) {
        if (stack.isEmpty() || !stack.is(ModItems.RECHANTMENT_BOOK.get())) {
            return false;
        }

        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }

        if (enchantments.isEmpty()) {
            return false;
        }

        String enchantmentRaw = enchantments.entrySet().iterator().next().getKey()
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("");

        return legendaryRarity.isEnchantmentInPool(enchantmentRaw);
    }

    public static void awardGemFromLuckyGemAdvancement(Player player, ServerLevel level) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(GEM_FROM_LUCKY_GEM_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, GEM_FROM_LUCKY_GEM_CRITERION);
        }
    }
}
