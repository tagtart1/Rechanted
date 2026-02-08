package net.tagtart.rechantment.event.enchantment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.enchantment.custom.WisdomEnchantmentEffect;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BlockBreakEnchantmentsHandler {
    private static final String TELEPATHY_DROP_TAG = "rechantment_telepathy_drop";
    private static final String TELEPATHY_OWNER_PREFIX = "rechantment_telepathy_owner_";
    private static final String TELEPATHY_START_TICK_KEY = "rechantment_telepathy_start_tick";
    private static final int TELEPATHY_PULL_INTERVAL_TICKS = 2;
    private static final int TELEPATHY_TIMEOUT_TICKS = 40; // 2 seconds
    private static final double TELEPATHY_PULL_RADIUS = 12.0D;
    private static final double TELEPATHY_PICKUP_DISTANCE_SQR = 1.44D; // 1.2 blocks
    private static final int TELEPATHY_FULL_INVENTORY_WARNING_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Integer> TELEPATHY_WARNING_TICK_BY_PLAYER = new HashMap<>();

    private static final List<Integer> TIMBER_BLOCKS_PER_LEVEL = Arrays.asList(
            10,
            20,
            30
    );

    // Telepathy, Vein Miner, Timber, Wisdom Enchantments - Blocks
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level().isClientSide()) return;

        // For enchantment table replacement inventory, this is necessary since it isn't tied to a custom block, just an entity.
        BlockEntity blockEntity = event.getPlayer().level().getBlockEntity(event.getPos());
        if (blockEntity instanceof RechantmentTableBlockEntity) {
            RechantmentTableBlockEntity rechantmentBE = (RechantmentTableBlockEntity)blockEntity;
            rechantmentBE.onBreak();

        }

        ItemStack handItem = event.getPlayer().getMainHandItem();
        int telepathyEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", handItem, event.getLevel().registryAccess());
        int veinMinerEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:vein_miner", handItem, event.getLevel().registryAccess());
        int timberEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:timber", handItem, event.getLevel().registryAccess());
        int fortuneEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("minecraft:fortune", handItem, event.getPlayer().registryAccess());
        ServerLevel level = (ServerLevel) event.getPlayer().level();

        if (veinMinerEnchantmentLevel != 0 && event.getState().is(Tags.Blocks.ORES)) {
            BlockPos[] oresToDestroy = UtilFunctions.BFSLevelForBlocks(level, Tags.Blocks.ORES, event.getPos(), 10, true);
            destroyBulkBlocks(event, oresToDestroy, level, handItem, telepathyEnchantmentLevel, fortuneEnchantmentLevel);
        }

        else if (timberEnchantmentLevel != 0 && event.getState().is(BlockTags.LOGS)) {
            int searchLimit = TIMBER_BLOCKS_PER_LEVEL.get(timberEnchantmentLevel - 1);

            BlockPos[] woodToDestroy = UtilFunctions.BFSLevelForBlocks(level, BlockTags.LOGS, event.getPos(), searchLimit, true);
            destroyBulkBlocks(event, woodToDestroy, level, handItem, telepathyEnchantmentLevel, fortuneEnchantmentLevel);
        }

        // Telepathy check. Only happens when destroying a single block normally here
        else if (telepathyEnchantmentLevel != 0) {

            telepathicallyDestroyBlock(event, event.getPos(), level, handItem, fortuneEnchantmentLevel);

//                if (event.getState().getDestroySpeed(level, event.getPos()) != 0)
//                    handItem.hurtAndBreak(1, level, (ServerPlayer)event.getPlayer(), (item) -> {});
        }

        // Fortune by itself with vanilla enchants without other breakevent enchant
//            else if (fortuneEnchantmentLevel != 0 && RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()){
//                // Block info
//                BlockState blockState = event.getState();
//                Block block = blockState.getBlock();
//                BlockPos blockPos = event.getPos();
//                if (!blockState.is(Tags.Blocks.ORES) || !handItem.isCorrectToolForDrops(blockState)) return;
//
//                level.removeBlock(blockPos, false);
//                // Fetch the block drops without tool
//                List<ItemStack> drops = Block.getDrops(event.getState(), level, event.getPos(), null);
//               // Pop exp
//
//               //applyNerfedFortune(drops, fortuneEnchantmentLevel);
//               //Block.popResource(level, event.getPos(), ItemStack.EMPTY);
//
//
//               // Pop the resource with nerfed fortune
//                for(ItemStack drop : drops) {
//                    Block.popResource(level, blockPos, drop);
//                }
//
//                hurtAndBreakWithRebirthLogic(1, level, (ServerPlayer)event.getPlayer(), handItem);
//            }
    }

    private static void applyNerfedFortune(List<ItemStack> items, int eLevel) {
        for(ItemStack item : items) {
            double chanceToDouble = 0.0;
            switch(eLevel) {
                case 1: {
                    chanceToDouble = RechantmentCommonConfigs.FORTUNE_1_CHANCE.get(); // Config later
                    break;
                }
                case 2: {
                    chanceToDouble = RechantmentCommonConfigs.FORTUNE_2_CHANCE.get(); // Config later
                    break;
                }
                case 3: {
                    chanceToDouble = RechantmentCommonConfigs.FORTUNE_3_CHANCE.get(); // Config later
                    break;
                }
                default:
                    break;
            }
            Random random = new Random();
            if (random.nextDouble() < chanceToDouble) {
                item.setCount(item.getCount() * 2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % TELEPATHY_PULL_INTERVAL_TICKS != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB searchBox = player.getBoundingBox().inflate(TELEPATHY_PULL_RADIUS);
        String ownerTag = getOwnerTagForPlayer(player);

        List<ItemEntity> telepathyDrops = level.getEntitiesOfClass(ItemEntity.class, searchBox, item ->
                item.getTags().contains(TELEPATHY_DROP_TAG) && item.getTags().contains(ownerTag));

        for (ItemEntity itemEntity : telepathyDrops) {
            pullTelepathyDropToPlayer(itemEntity, player, level);
        }
    }

    // Note this has to manually check logic with some synergistic enchantments due to how telepathy
    // changes block breaking logic.
    private static void telepathicallyDestroyBlock(BlockEvent.BreakEvent event, BlockPos blockPos, ServerLevel level, ItemStack handItem, int fortuneEnchantmentLevel) {
        BlockState blockState = level.getBlockState(blockPos);
        List<ItemStack> drops = Block.getDrops(blockState, level, blockPos, null, event.getPlayer(), handItem);

        // Prevents block from dropping a resource at this pos
        Block.popResource(level, event.getPos(), ItemStack.EMPTY);

        // checking if the state is an ore makes fortune with axe on melon and mushrooms not work but whatever, who really does that?
//            if (RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()
//                    && fortuneEnchantmentLevel != 0
//                    && blockState.is(Tags.Blocks.ORES)) {
//                drops = Block.getDrops(blockState, level, blockPos, null);
//                applyNerfedFortune(drops, fortuneEnchantmentLevel);
//            } else {
//            }

        // Get wisdom if applied
        int wisdomEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:wisdom", handItem, event.getPlayer().registryAccess());

        // This check prevents a block from "breaking" twice while also working with vein miner breaks
        if (event.getPos() != blockPos) {
            level.destroyBlock(blockPos, false);
        } else {
            level.removeBlock(blockPos, false);
        }

        // Prevents an axe from mining diamonds for example
        if (!blockState.canHarvestBlock(level, blockPos, event.getPlayer())) return;

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;

            ItemEntity itemEntity = new ItemEntity(
                    level,
                    blockPos.getX() + 0.5D,
                    blockPos.getY() + 0.5D,
                    blockPos.getZ() + 0.5D,
                    drop.copy()
            );
            itemEntity.addTag(TELEPATHY_DROP_TAG);
            itemEntity.addTag(getOwnerTagForPlayer(event.getPlayer()));
            itemEntity.setInvulnerable(true);
            itemEntity.setPickUpDelay(0);
            level.addFreshEntity(itemEntity);
        }


        // Teleports the exp orb to player
        int expToDrop = blockState.getExpDrop(level, blockPos, event.getLevel().getBlockEntity(blockPos), event.getPlayer(), handItem);
        int silkTouchLevel = UtilFunctions.getEnchantmentFromItem("minecraft:silk_touch", handItem, level.registryAccess());
        if (expToDrop > 0 && silkTouchLevel == 0) {
            Player player = event.getPlayer();

            // Multiply if we have wisdom on the tool
            if (wisdomEnchantmentLevel != 0) {
                expToDrop = (int)manuallyApplyWisdom(wisdomEnchantmentLevel, (float)expToDrop);
            }
            ExperienceOrb expOrb = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), expToDrop);
            level.addFreshEntity(expOrb);
        }
    }

    // Vein miner / timber specific
    private static void destroyBulkBlocks(BlockEvent.BreakEvent event, BlockPos[] blocksToDestroy, ServerLevel level, ItemStack handItem, int telepathyEnchantmentLevel, int fortuneEnchantmentLevel) {
        int destroyedSuccessfully = 0;
        //int wisdomEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:wisdom", handItem, event.getPlayer().registryAccess());

        for (BlockPos blockPos : blocksToDestroy) {
            BlockState blockState = level.getBlockState(blockPos);

            // Account for telepathy with each block to destroy
            if (telepathyEnchantmentLevel != 0) {
                telepathicallyDestroyBlock(event, blockPos, level, handItem, fortuneEnchantmentLevel);
                ++destroyedSuccessfully;
            }

            // If no telepathy, just do basic block destroy manually.
            else {
                ++destroyedSuccessfully;

                // Create correct particle and noise block breaking effects
                if (event.getPos() != blockPos ) {
                    level.destroyBlock(blockPos, false);
                } else {
                    level.removeBlock(blockPos, false);
                }


                // Manually pop the resource
                if (handItem.isCorrectToolForDrops(blockState)) {
                    blockState.getBlock().playerDestroy(level, event.getPlayer(), blockPos, blockState, level.getBlockEntity(blockPos), handItem);
                }
            }
        }

        handItem.hurtAndBreak(destroyedSuccessfully - 1, level, (ServerPlayer)event.getPlayer(), (item) -> {});
    }

    private static float manuallyApplyWisdom(int wisdomEnchantmentLevel, float originalExp) {
        return WisdomEnchantmentEffect.trueProcess(wisdomEnchantmentLevel, RandomSource.create(), originalExp);
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
        itemEntity.setInvulnerable(false);
        itemEntity.removeTag(TELEPATHY_DROP_TAG);
        itemEntity.removeTag(getOwnerTagForPlayer(player));
        itemEntity.getPersistentData().remove(TELEPATHY_START_TICK_KEY);
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
