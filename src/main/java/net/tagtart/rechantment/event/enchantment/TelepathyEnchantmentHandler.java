package net.tagtart.rechantment.event.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tagtart.rechantment.Rechantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public final class TelepathyEnchantmentHandler {
    private static final String TELEPATHY_DROP_TAG = "rechantment_telepathy_drop";
    private static final String TELEPATHY_EXP_TAG = "rechantment_telepathy_exp";
    private static final String TELEPATHY_OWNER_PREFIX = "rechantment_telepathy_owner_";
    private static final String TELEPATHY_START_TICK_KEY = "rechantment_telepathy_start_tick";
    private static final String TELEPATHY_EXP_START_TICK_KEY = "rechantment_telepathy_exp_start_tick";
    private static final int TELEPATHY_PULL_INTERVAL_TICKS = 2;
    private static final int TELEPATHY_TIMEOUT_TICKS = 40; // 2 seconds
    private static final double TELEPATHY_PULL_RADIUS = 12.0D;
    private static final double TELEPATHY_PICKUP_DISTANCE_SQR = 1.44D; // 1.2 blocks
    private static final int TELEPATHY_FULL_INVENTORY_WARNING_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Integer> TELEPATHY_WARNING_TICK_BY_PLAYER = new HashMap<>();

    private TelepathyEnchantmentHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % TELEPATHY_PULL_INTERVAL_TICKS != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB searchBox = player.getBoundingBox().inflate(TELEPATHY_PULL_RADIUS);

        List<ItemEntity> telepathyDrops = level.getEntitiesOfClass(ItemEntity.class, searchBox, item ->
                isTelepathyItemForPlayer(item, player));
        List<ExperienceOrb> telepathyExperienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, searchBox, orb ->
                isTelepathyExperienceForPlayer(orb, player));

        for (ItemEntity itemEntity : telepathyDrops) {
            pullTelepathyDropToPlayer(itemEntity, player, level);
        }
        for (ExperienceOrb experienceOrb : telepathyExperienceOrbs) {
            pullTelepathyExpToPlayer(experienceOrb, player);
        }
    }

    public static void markItemEntityForTelepathy(ItemEntity itemEntity, Player player) {
        itemEntity.addTag(TELEPATHY_DROP_TAG);
        itemEntity.addTag(getOwnerTagForPlayer(player));
        itemEntity.setInvulnerable(true);
        itemEntity.setPickUpDelay(0);
    }

    public static void markExperienceOrbForTelepathy(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.addTag(TELEPATHY_EXP_TAG);
        experienceOrb.addTag(getOwnerTagForPlayer(player));
        experienceOrb.setInvulnerable(true);
    }

    public static void clearItemEntityTelepathy(ItemEntity itemEntity, Player player) {
        itemEntity.setInvulnerable(false);
        itemEntity.removeTag(TELEPATHY_DROP_TAG);
        itemEntity.removeTag(getOwnerTagForPlayer(player));
    }

    public static void clearExperienceOrbTelepathy(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.setInvulnerable(false);
        experienceOrb.removeTag(TELEPATHY_EXP_TAG);
        experienceOrb.removeTag(getOwnerTagForPlayer(player));
    }

    public static boolean isTelepathyItemForPlayer(ItemEntity itemEntity, Player player) {
        String ownerTag = getOwnerTagForPlayer(player);
        return itemEntity.getTags().contains(TELEPATHY_DROP_TAG) && itemEntity.getTags().contains(ownerTag);
    }

    public static boolean isTelepathyExperienceForPlayer(ExperienceOrb experienceOrb, Player player) {
        String ownerTag = getOwnerTagForPlayer(player);
        return experienceOrb.getTags().contains(TELEPATHY_EXP_TAG) && experienceOrb.getTags().contains(ownerTag);
    }

    private static String getOwnerTagForPlayer(Player player) {
        return TELEPATHY_OWNER_PREFIX + player.getStringUUID();
    }

    private static void pullTelepathyDropToPlayer(ItemEntity itemEntity, Player player, ServerLevel level) {
        if (!itemEntity.getPersistentData().contains(TELEPATHY_START_TICK_KEY)) {
            itemEntity.getPersistentData().putInt(TELEPATHY_START_TICK_KEY, itemEntity.tickCount);
        }
        int startTick = itemEntity.getPersistentData().getInt(TELEPATHY_START_TICK_KEY);
        if (itemEntity.tickCount - startTick >= TELEPATHY_TIMEOUT_TICKS) {
            tryPickupTelepathyDrop(itemEntity, player, level, true);
            return;
        }

        itemEntity.setNoGravity(true);

        Vec3 targetPos = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 currentPos = itemEntity.position();
        Vec3 direction = targetPos.subtract(currentPos);
        double distance = direction.length();
        if (distance > 0.0001D) {
            double speed = Math.min(0.55D, 0.12D + distance * 0.08D);
            itemEntity.setDeltaMovement(direction.normalize().scale(speed));
        }

        if (itemEntity.distanceToSqr(player) <= TELEPATHY_PICKUP_DISTANCE_SQR) {
            tryPickupTelepathyDrop(itemEntity, player, level, false);
        }
    }

    private static void tryPickupTelepathyDrop(ItemEntity itemEntity, Player player, ServerLevel level, boolean releaseIfInsertFails) {
        ItemStack dropStack = itemEntity.getItem();
        if (player.addItem(dropStack)) {
            Random random = new Random();
            float randomPitch = 0.9f + random.nextFloat() * (1.6f - 0.9f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.25f, randomPitch);
            itemEntity.discard();
        } else if (releaseIfInsertFails) {
            warnInventoryFull(player);
            itemEntity.setPos(player.getX(), player.getY() + 0.2D, player.getZ());
            itemEntity.setDeltaMovement(Vec3.ZERO);
            releaseTelepathyDrop(itemEntity, player);
        }
    }

    private static void releaseTelepathyDrop(ItemEntity itemEntity, Player player) {
        itemEntity.setNoGravity(false);
        clearItemEntityTelepathy(itemEntity, player);
        itemEntity.getPersistentData().remove(TELEPATHY_START_TICK_KEY);
    }

    private static void pullTelepathyExpToPlayer(ExperienceOrb experienceOrb, Player player) {
        if (!experienceOrb.getPersistentData().contains(TELEPATHY_EXP_START_TICK_KEY)) {
            experienceOrb.getPersistentData().putInt(TELEPATHY_EXP_START_TICK_KEY, experienceOrb.tickCount);
        }
        int startTick = experienceOrb.getPersistentData().getInt(TELEPATHY_EXP_START_TICK_KEY);
        if (experienceOrb.tickCount - startTick >= TELEPATHY_TIMEOUT_TICKS) {
            releaseTelepathyExp(experienceOrb, player);
            return;
        }

        experienceOrb.setNoGravity(true);

        Vec3 targetPos = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 currentPos = experienceOrb.position();
        Vec3 direction = targetPos.subtract(currentPos);
        double distance = direction.length();
        if (distance > 0.0001D) {
            double speed = Math.min(0.65D, 0.16D + distance * 0.08D);
            experienceOrb.setDeltaMovement(direction.normalize().scale(speed));
        }

        if (experienceOrb.distanceToSqr(player) <= TELEPATHY_PICKUP_DISTANCE_SQR) {
            experienceOrb.playerTouch(player);
        }
    }

    private static void releaseTelepathyExp(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.setNoGravity(false);
        clearExperienceOrbTelepathy(experienceOrb, player);
        experienceOrb.getPersistentData().remove(TELEPATHY_EXP_START_TICK_KEY);
    }

    private static void warnInventoryFull(Player player) {
        UUID playerUuid = player.getUUID();
        int nextAllowedTick = TELEPATHY_WARNING_TICK_BY_PLAYER.getOrDefault(playerUuid, 0);
        if (player.tickCount < nextAllowedTick) {
            return;
        }

        player.sendSystemMessage(Component.literal("Warning: inventory is full.").withStyle(ChatFormatting.RED));
        TELEPATHY_WARNING_TICK_BY_PLAYER.put(playerUuid, player.tickCount + TELEPATHY_FULL_INVENTORY_WARNING_COOLDOWN_TICKS);
    }
}
