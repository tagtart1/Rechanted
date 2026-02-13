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
public final class TelekinesisEnchantmentHandler {
    private static final String TELEKINESIS_DROP_TAG = "rechantment_telekinesis_drop";
    private static final String TELEKINESIS_EXP_TAG = "rechantment_telekinesis_exp";
    private static final String TELEKINESIS_OWNER_PREFIX = "rechantment_telekinesis_owner_";
    private static final String TELEKINESIS_START_TICK_KEY = "rechantment_telekinesis_start_tick";
    private static final String TELEKINESIS_EXP_START_TICK_KEY = "rechantment_telekinesis_exp_start_tick";
    private static final int TELEKINESIS_PULL_INTERVAL_TICKS = 2;
    private static final int TELEKINESIS_TIMEOUT_TICKS = 40; // 2 seconds
    private static final double TELEKINESIS_PULL_RADIUS = 12.0D;
    private static final double TELEKINESIS_PICKUP_DISTANCE_SQR = 1.44D; // 1.2 blocks
    private static final int TELEKINESIS_FULL_INVENTORY_WARNING_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Integer> TELEKINESIS_WARNING_TICK_BY_PLAYER = new HashMap<>();

    private TelekinesisEnchantmentHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % TELEKINESIS_PULL_INTERVAL_TICKS != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB searchBox = player.getBoundingBox().inflate(TELEKINESIS_PULL_RADIUS);

        List<ItemEntity> telekinesisDrops = level.getEntitiesOfClass(ItemEntity.class, searchBox, item ->
                isTelekinesisItemForPlayer(item, player));
        List<ExperienceOrb> telekinesisExperienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, searchBox, orb ->
                isTelekinesisExperienceForPlayer(orb, player));

        for (ItemEntity itemEntity : telekinesisDrops) {
            pullTelekinesisDropToPlayer(itemEntity, player, level);
        }
        for (ExperienceOrb experienceOrb : telekinesisExperienceOrbs) {
            pullTelekinesisExpToPlayer(experienceOrb, player);
        }
    }

    public static void markItemEntityForTelekinesis(ItemEntity itemEntity, Player player) {
        itemEntity.addTag(TELEKINESIS_DROP_TAG);
        itemEntity.addTag(getOwnerTagForPlayer(player));
        itemEntity.setInvulnerable(true);
        itemEntity.setPickUpDelay(0);
    }

    public static void markExperienceOrbForTelekinesis(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.addTag(TELEKINESIS_EXP_TAG);
        experienceOrb.addTag(getOwnerTagForPlayer(player));
        experienceOrb.setInvulnerable(true);
    }

    public static void clearItemEntityTelekinesis(ItemEntity itemEntity, Player player) {
        itemEntity.setInvulnerable(false);
        itemEntity.removeTag(TELEKINESIS_DROP_TAG);
        itemEntity.removeTag(getOwnerTagForPlayer(player));
    }

    public static void clearExperienceOrbTelekinesis(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.setInvulnerable(false);
        experienceOrb.removeTag(TELEKINESIS_EXP_TAG);
        experienceOrb.removeTag(getOwnerTagForPlayer(player));
    }

    public static boolean isTelekinesisItemForPlayer(ItemEntity itemEntity, Player player) {
        String ownerTag = getOwnerTagForPlayer(player);
        return itemEntity.getTags().contains(TELEKINESIS_DROP_TAG) && itemEntity.getTags().contains(ownerTag);
    }

    public static boolean isTelekinesisExperienceForPlayer(ExperienceOrb experienceOrb, Player player) {
        String ownerTag = getOwnerTagForPlayer(player);
        return experienceOrb.getTags().contains(TELEKINESIS_EXP_TAG) && experienceOrb.getTags().contains(ownerTag);
    }

    private static String getOwnerTagForPlayer(Player player) {
        return TELEKINESIS_OWNER_PREFIX + player.getStringUUID();
    }

    private static void pullTelekinesisDropToPlayer(ItemEntity itemEntity, Player player, ServerLevel level) {
        if (!itemEntity.getPersistentData().contains(TELEKINESIS_START_TICK_KEY)) {
            itemEntity.getPersistentData().putInt(TELEKINESIS_START_TICK_KEY, itemEntity.tickCount);
        }
        int startTick = itemEntity.getPersistentData().getInt(TELEKINESIS_START_TICK_KEY);
        if (itemEntity.tickCount - startTick >= TELEKINESIS_TIMEOUT_TICKS) {
            tryPickupTelekinesisDrop(itemEntity, player, level, true);
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

        if (itemEntity.distanceToSqr(player) <= TELEKINESIS_PICKUP_DISTANCE_SQR) {
            tryPickupTelekinesisDrop(itemEntity, player, level, false);
        }
    }

    private static void tryPickupTelekinesisDrop(ItemEntity itemEntity, Player player, ServerLevel level, boolean releaseIfInsertFails) {
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
            releaseTelekinesisDrop(itemEntity, player);
        }
    }

    private static void releaseTelekinesisDrop(ItemEntity itemEntity, Player player) {
        itemEntity.setNoGravity(false);
        clearItemEntityTelekinesis(itemEntity, player);
        itemEntity.getPersistentData().remove(TELEKINESIS_START_TICK_KEY);
    }

    private static void pullTelekinesisExpToPlayer(ExperienceOrb experienceOrb, Player player) {
        if (!experienceOrb.getPersistentData().contains(TELEKINESIS_EXP_START_TICK_KEY)) {
            experienceOrb.getPersistentData().putInt(TELEKINESIS_EXP_START_TICK_KEY, experienceOrb.tickCount);
        }
        int startTick = experienceOrb.getPersistentData().getInt(TELEKINESIS_EXP_START_TICK_KEY);
        if (experienceOrb.tickCount - startTick >= TELEKINESIS_TIMEOUT_TICKS) {
            releaseTelekinesisExp(experienceOrb, player);
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

        if (experienceOrb.distanceToSqr(player) <= TELEKINESIS_PICKUP_DISTANCE_SQR) {
            experienceOrb.playerTouch(player);
        }
    }

    private static void releaseTelekinesisExp(ExperienceOrb experienceOrb, Player player) {
        experienceOrb.setNoGravity(false);
        clearExperienceOrbTelekinesis(experienceOrb, player);
        experienceOrb.getPersistentData().remove(TELEKINESIS_EXP_START_TICK_KEY);
    }

    private static void warnInventoryFull(Player player) {
        UUID playerUuid = player.getUUID();
        int nextAllowedTick = TELEKINESIS_WARNING_TICK_BY_PLAYER.getOrDefault(playerUuid, 0);
        if (player.tickCount < nextAllowedTick) {
            return;
        }

        player.sendSystemMessage(Component.literal("Warning: inventory is full.").withStyle(ChatFormatting.RED));
        TELEKINESIS_WARNING_TICK_BY_PLAYER.put(playerUuid, player.tickCount + TELEKINESIS_FULL_INVENTORY_WARNING_COOLDOWN_TICKS);
    }
}

