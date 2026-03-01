package net.tagtart.rechanted.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.attachments.ModAttachments;
import net.tagtart.rechanted.block.ModBlocks;
import net.tagtart.rechanted.item.ModItems;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AdvancementHelper {
    private static final ResourceLocation UPGRADE_ENCHANT_TABLE_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "upgrade_enchanting_table");
    private static final String UPGRADE_ENCHANT_TABLE_CRITERION = "use_emerald_on_enchanting_table";

    private static final ResourceLocation POWER_ENCHANT_TABLE_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "power_enchanting_table");
    private static final String POWER_ENCHANT_TABLE_CRITERION = "power_up_enchanting_table";

    private static final ResourceLocation LEGENDARY_PULL_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "legendary_pull");
    private static final String LEGENDARY_PULL_CRITERION = "hold_legendary_book";

    private static final ResourceLocation EXCALIBUR_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "excalibur");
    private static final String EXCALIBUR_CRITERION = "hold_max_sword_enchants";

    private static final ResourceLocation GEM_FROM_LUCKY_GEM_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "gem_from_lucky_gem");
    private static final String GEM_FROM_LUCKY_GEM_CRITERION = "gem_from_lucky_gem";

    private static final ResourceLocation FIRST_GEM_HELD_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "first_gem_held");
    private static final String FIRST_GEM_HELD_CRITERION = "hold_first_gem";

    private static final ResourceLocation OBTAIN_ALL_GEMS_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "obtain_all_gems");
    private static final String HELD_CHANCE_GEM_CRITERION = "held_chance_gem";
    private static final String HELD_SHINY_CHANCE_GEM_CRITERION = "held_shiny_chance_gem";
    private static final String HELD_RETURN_GEM_CRITERION = "held_return_gem";
    private static final String HELD_TASTY_GEM_CRITERION = "held_tasty_gem";
    private static final String HELD_WARP_GEM_CRITERION = "held_warp_gem";
    private static final String HELD_LUCKY_GEM_CRITERION = "held_lucky_gem";
    private static final String HELD_CLONE_GEM_CRITERION = "held_clone_gem";
    private static final String HELD_SMITHING_GEM_CRITERION = "held_smithing_gem";

    private static final ResourceLocation ARCHMAGE_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "archmage");
    private static final String ARCHMAGE_CRITERION = "discover_all_enchanted_books";

    private static final ResourceLocation UNBOXING_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "unboxing");
    private static final String UNBOXING_CRITERION = "open_5_mysterious_books_in_10s";

    private static final ResourceLocation SO_MYSTERIOUS_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "so_mysterious");
    private static final String SO_MYSTERIOUS_CRITERION = "open_one_mysterious_book";

    private static final ResourceLocation EXCEPTIONAL_LEVELS_OF_MYSTERY_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "exceptional_levels_of_mystery");
    private static final String EXCEPTIONAL_LEVELS_OF_MYSTERY_CRITERION = "open_ten_mysterious_books";

    private static final ResourceLocation FIRST_DUSTY_BOOK_HELD_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechanted.MOD_ID, "unforgotten_magic");
    private static final String FIRST_DUSTY_BOOK_HELD_CRITERION = "hold_first_dusty_mysterious_book";

    private static final int SO_MYSTERIOUS_REQUIRED_OPENS = 1;
    private static final int EXCEPTIONAL_LEVELS_OF_MYSTERY_REQUIRED_OPENS = 10;
    private static final int UNBOXING_REQUIRED_OPENS = 5;
    private static final long UNBOXING_WINDOW_TICKS = 200L;
    private static final Map<UUID, ArrayDeque<Long>> UNBOXING_OPEN_TIMES_BY_PLAYER = new HashMap<>();

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

    public static void awardUnforgottenAdvancementIfEligible(Player player, ServerLevel level, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (stack.isEmpty() || !stack.is(ModItems.DUSTY_MYSTERIOUS_BOOK.get())) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(FIRST_DUSTY_BOOK_HELD_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, FIRST_DUSTY_BOOK_HELD_CRITERION);
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

        Map<ResourceKey<Enchantment>, Holder.Reference<Enchantment>> requiredSwordEnchantments = getConfiguredSwordEnchantmentsForItem(
                heldItem,
                level);
        if (requiredSwordEnchantments.isEmpty()) {
            return;
        }

        ItemEnchantments enchantments = heldItem.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<ResourceKey<Enchantment>> presentConfiguredEnchantments = new HashSet<>();
        List<Holder<Enchantment>> presentConfiguredHolders = new ArrayList<>();
        for (var entry : enchantments.entrySet()) {
            entry.getKey().unwrapKey().ifPresent(key -> {
                if (requiredSwordEnchantments.containsKey(key)) {
                    presentConfiguredEnchantments.add(key);
                    presentConfiguredHolders.add(entry.getKey());
                }
            });
        }

        // Excalibur = no configured sword enchantment remains addable to this sword right now.
        for (Map.Entry<ResourceKey<Enchantment>, Holder.Reference<Enchantment>> configuredEnchantEntry : requiredSwordEnchantments
                .entrySet()) {
            if (presentConfiguredEnchantments.contains(configuredEnchantEntry.getKey())) {
                continue;
            }

            if (isCompatibleWithCurrentConfiguredEnchantments(configuredEnchantEntry.getValue(), presentConfiguredHolders)) {
                return;
            }
        }

        var advancement = level.getServer().getAdvancements().get(EXCALIBUR_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, EXCALIBUR_CRITERION);
        }
    }

    private static Map<ResourceKey<Enchantment>, Holder.Reference<Enchantment>> getConfiguredSwordEnchantmentsForItem(
            ItemStack item,
            ServerLevel level) {
        Map<ResourceKey<Enchantment>, Holder.Reference<Enchantment>> requiredEnchantments = new LinkedHashMap<>();

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

                enchantmentHolder.unwrapKey().ifPresent(key -> requiredEnchantments.putIfAbsent(key, enchantmentHolder));
            }
        }

        return requiredEnchantments;
    }

    private static boolean isCompatibleWithCurrentConfiguredEnchantments(
            Holder<Enchantment> candidateEnchantment,
            List<Holder<Enchantment>> currentConfiguredEnchantments) {
        for (Holder<Enchantment> currentEnchantment : currentConfiguredEnchantments) {
            boolean compatibleForward = Enchantment.areCompatible(candidateEnchantment, currentEnchantment);
            boolean compatibleReverse = Enchantment.areCompatible(currentEnchantment, candidateEnchantment);
            if (!compatibleForward || !compatibleReverse) {
                return false;
            }
        }

        return true;
    }

    public static void awardLegendaryPullAdvancementIfEligible(Player player, ServerLevel level, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (stack.isEmpty()) {
            return;
        }

        BookRarityProperties legendaryRarity = Arrays.stream(BookRarityProperties.getAllProperties())
                .max(Comparator.comparingDouble(properties -> properties.rarity))
                .orElse(null);
        if (legendaryRarity == null) {
            return;
        }

        if (!isLegendaryRechantedBook(stack, legendaryRarity)) {
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

    private static boolean isLegendaryRechantedBook(ItemStack stack, BookRarityProperties legendaryRarity) {
        if (stack.isEmpty() || !stack.is(ModItems.RECHANTED_BOOK.get())) {
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

    public static boolean isTrackedGem(ItemStack stack) {
        return stack.is(ModItems.CHANCE_GEM.get())
                || stack.is(ModItems.SHINY_CHANCE_GEM.get())
                || stack.is(ModItems.RETURN_GEM.get())
                || stack.is(ModItems.TASTY_GEM.get())
                || stack.is(ModItems.WARP_GEM.get())
                || stack.is(ModItems.LUCKY_GEM.get())
                || stack.is(ModItems.CLONE_GEM.get())
                || stack.is(ModItems.SMITHING_GEM.get());
    }

    public static void awardGemPickupAdvancements(Player player, ServerLevel level, ItemStack pickedStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (pickedStack.isEmpty() || !isTrackedGem(pickedStack)) {
            return;
        }

        var firstGemAdvancement = level.getServer().getAdvancements().get(FIRST_GEM_HELD_ADVANCEMENT_ID);
        if (firstGemAdvancement != null) {
            serverPlayer.getAdvancements().award(firstGemAdvancement, FIRST_GEM_HELD_CRITERION);
        }

        String allGemsCriterion = getAllGemsCriterionForStack(pickedStack);
        if (allGemsCriterion == null) {
            return;
        }

        var obtainAllGemsAdvancement = level.getServer().getAdvancements().get(OBTAIN_ALL_GEMS_ADVANCEMENT_ID);
        if (obtainAllGemsAdvancement != null) {
            serverPlayer.getAdvancements().award(obtainAllGemsAdvancement, allGemsCriterion);
        }
    }

    private static String getAllGemsCriterionForStack(ItemStack stack) {
        if (stack.is(ModItems.CHANCE_GEM.get())) {
            return HELD_CHANCE_GEM_CRITERION;
        }
        if (stack.is(ModItems.SHINY_CHANCE_GEM.get())) {
            return HELD_SHINY_CHANCE_GEM_CRITERION;
        }
        if (stack.is(ModItems.RETURN_GEM.get())) {
            return HELD_RETURN_GEM_CRITERION;
        }
        if (stack.is(ModItems.TASTY_GEM.get())) {
            return HELD_TASTY_GEM_CRITERION;
        }
        if (stack.is(ModItems.WARP_GEM.get())) {
            return HELD_WARP_GEM_CRITERION;
        }
        if (stack.is(ModItems.LUCKY_GEM.get())) {
            return HELD_LUCKY_GEM_CRITERION;
        }
        if (stack.is(ModItems.CLONE_GEM.get())) {
            return HELD_CLONE_GEM_CRITERION;
        }
        if (stack.is(ModItems.SMITHING_GEM.get())) {
            return HELD_SMITHING_GEM_CRITERION;
        }

        return null;
    }

    public static boolean isTrackedBook(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModItems.RECHANTED_BOOK.get()) || stack.is(ModItems.DUSTY_MYSTERIOUS_BOOK.get()));
    }

    public static void awardArchmageProgressFromBook(Player player, ServerLevel level, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!isTrackedBook(stack)) {
            return;
        }

        String enchantmentId = getBookBaseEnchantmentId(stack);
        if (enchantmentId == null) {
            return;
        }

        Set<String> requiredEnchantments = getConfiguredArchmageEnchantments(level);
        if (requiredEnchantments.isEmpty() || !requiredEnchantments.contains(enchantmentId)) {
            return;
        }

        if (!recordDiscoveredArchmageEnchantment(serverPlayer, enchantmentId)) {
            return;
        }

        if (hasDiscoveredAllArchmageEnchantments(serverPlayer, requiredEnchantments)) {
            var advancement = level.getServer().getAdvancements().get(ARCHMAGE_ADVANCEMENT_ID);
            if (advancement != null) {
                boolean awarded = serverPlayer.getAdvancements().award(advancement, ARCHMAGE_CRITERION);
                if (awarded) {
                    ItemStack trophy = new ItemStack(ModBlocks.RECHANTED_TROPHY_BLOCK);

                    if (!serverPlayer.addItem(trophy)) {
                        serverPlayer.drop(trophy, false);
                    }
                }
            }
        }
    }

    private static String getBookBaseEnchantmentId(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        if (enchantments.isEmpty()) {
            return null;
        }

        return enchantments.entrySet().iterator().next().getKey()
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse(null);
    }

    private static Set<String> getConfiguredArchmageEnchantments(ServerLevel level) {
        Set<String> configured = new HashSet<>();
        for (BookRarityProperties rarityProperties : BookRarityProperties.getAllProperties()) {
            for (EnchantmentPoolEntry poolEntry : rarityProperties.enchantmentPool) {
                Holder.Reference<Enchantment> enchantmentHolder = UtilFunctions
                        .getEnchantmentReferenceIfPresent(level.registryAccess(), poolEntry.enchantment);
                if (enchantmentHolder == null) {
                    continue;
                }

                enchantmentHolder.unwrapKey().ifPresent(key -> {
                    String enchantmentId = key.location().toString();
                    configured.add(enchantmentId);
                });
            }
        }

        return configured;
    }

    private static boolean recordDiscoveredArchmageEnchantment(ServerPlayer player, String enchantmentId) {
        Set<String> discoveredEnchantments = new HashSet<>(player.getData(ModAttachments.DISCOVERED_ENCHANTMENTS));
        boolean addedNew = discoveredEnchantments.add(enchantmentId);
        if (addedNew) {
            player.setData(ModAttachments.DISCOVERED_ENCHANTMENTS, discoveredEnchantments);
        }
        return addedNew;
    }

    private static boolean hasDiscoveredAllArchmageEnchantments(ServerPlayer player, Set<String> requiredEnchantments) {
        Set<String> discoveredEnchantments = player.getData(ModAttachments.DISCOVERED_ENCHANTMENTS);
        return discoveredEnchantments.containsAll(requiredEnchantments);
    }

    public static void recordMysteriousBookOpenAndAward(Player player, ServerLevel level) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        awardMysteriousBookUsageAdvancements(serverPlayer, level);

        long currentGameTime = level.getGameTime();
        long minAllowedTick = currentGameTime - UNBOXING_WINDOW_TICKS;

        ArrayDeque<Long> openTimes = UNBOXING_OPEN_TIMES_BY_PLAYER.computeIfAbsent(
                serverPlayer.getUUID(),
                ignored -> new ArrayDeque<>());

        while (!openTimes.isEmpty() && openTimes.peekFirst() < minAllowedTick) {
            openTimes.pollFirst();
        }

        openTimes.addLast(currentGameTime);
        if (openTimes.size() < UNBOXING_REQUIRED_OPENS) {
            return;
        }

        var advancement = level.getServer().getAdvancements().get(UNBOXING_ADVANCEMENT_ID);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, UNBOXING_CRITERION);
        }
        openTimes.clear();
    }

    public static void clearMysteriousBookOpenTracker(UUID playerId) {
        UNBOXING_OPEN_TIMES_BY_PLAYER.remove(playerId);
    }

    private static void awardMysteriousBookUsageAdvancements(ServerPlayer player, ServerLevel level) {
        int mysteriousBookUseCount = player.getStats().getValue(Stats.ITEM_USED.get(ModItems.MYSTERIOUS_BOOK.get()));

        if (mysteriousBookUseCount >= SO_MYSTERIOUS_REQUIRED_OPENS) {
            var advancement = level.getServer().getAdvancements().get(SO_MYSTERIOUS_ADVANCEMENT_ID);
            if (advancement != null) {
                player.getAdvancements().award(advancement, SO_MYSTERIOUS_CRITERION);
            }
        }

        if (mysteriousBookUseCount >= EXCEPTIONAL_LEVELS_OF_MYSTERY_REQUIRED_OPENS) {
            var advancement = level.getServer().getAdvancements().get(EXCEPTIONAL_LEVELS_OF_MYSTERY_ADVANCEMENT_ID);
            if (advancement != null) {
                player.getAdvancements().award(advancement, EXCEPTIONAL_LEVELS_OF_MYSTERY_CRITERION);
            }
        }
    }
}
