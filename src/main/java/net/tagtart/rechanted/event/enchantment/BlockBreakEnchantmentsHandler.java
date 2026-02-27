package net.tagtart.rechanted.event.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.config.RechantedCommonConfigs;
import net.tagtart.rechanted.enchantment.custom.WisdomEnchantmentEffect;
import net.tagtart.rechanted.util.UtilFunctions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class BlockBreakEnchantmentsHandler {
    private static final List<Integer> TIMBER_BLOCKS_PER_LEVEL = Arrays.asList(
            10,
            20,
            30
    );

    // Telekinesis, Vein Miner, Timber, Wisdom Enchantments - Blocks
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level().isClientSide()) return;

        // For enchantment table replacement inventory, this is necessary since it isn't tied to a custom block, just an entity.
        BlockEntity blockEntity = event.getPlayer().level().getBlockEntity(event.getPos());
        if (blockEntity instanceof RechantedTableBlockEntity) {
            RechantedTableBlockEntity rechantedBE = (RechantedTableBlockEntity)blockEntity;
            rechantedBE.onBreak();

        }

        ItemStack handItem = event.getPlayer().getMainHandItem();
        int telekinesisEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechanted:telekinesis", handItem, event.getLevel().registryAccess());
        int veinMinerEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechanted:vein_miner", handItem, event.getLevel().registryAccess());
        int timberEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechanted:timber", handItem, event.getLevel().registryAccess());
        int fortuneEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("minecraft:fortune", handItem, event.getPlayer().registryAccess());
        ServerLevel level = (ServerLevel) event.getPlayer().level();

        if (veinMinerEnchantmentLevel != 0 && event.getState().is(Tags.Blocks.ORES)) {
            BlockPos[] oresToDestroy = UtilFunctions.BFSLevelForBlocks(level, Tags.Blocks.ORES, event.getPos(), 10, true);
            destroyBulkBlocks(event, oresToDestroy, level, handItem, telekinesisEnchantmentLevel, fortuneEnchantmentLevel);
        }

        else if (timberEnchantmentLevel != 0 && event.getState().is(BlockTags.LOGS)) {
            int searchLimit = TIMBER_BLOCKS_PER_LEVEL.get(timberEnchantmentLevel - 1);

            BlockPos[] woodToDestroy = UtilFunctions.BFSLevelForBlocks(level, BlockTags.LOGS, event.getPos(), searchLimit, true);
            destroyBulkBlocks(event, woodToDestroy, level, handItem, telekinesisEnchantmentLevel, fortuneEnchantmentLevel);
        }

        // Telekinesis check. Only happens when destroying a single block normally here
        else if (telekinesisEnchantmentLevel != 0) {

            telepathicallyDestroyBlock(event, event.getPos(), level, handItem, fortuneEnchantmentLevel);

//                if (event.getState().getDestroySpeed(level, event.getPos()) != 0)
//                    handItem.hurtAndBreak(1, level, (ServerPlayer)event.getPlayer(), (item) -> {});
        }

        // Fortune by itself with vanilla enchants without other breakevent enchant
//            else if (fortuneEnchantmentLevel != 0 && RechantedCommonConfigs.FORTUNE_NERF_ENABLED.get()){
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
                    chanceToDouble = RechantedCommonConfigs.FORTUNE_1_CHANCE.get(); // Config later
                    break;
                }
                case 2: {
                    chanceToDouble = RechantedCommonConfigs.FORTUNE_2_CHANCE.get(); // Config later
                    break;
                }
                case 3: {
                    chanceToDouble = RechantedCommonConfigs.FORTUNE_3_CHANCE.get(); // Config later
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

    // Note this has to manually check logic with some synergistic enchantments due to how telekinesis
    // changes block breaking logic.
    private static void telepathicallyDestroyBlock(BlockEvent.BreakEvent event, BlockPos blockPos, ServerLevel level, ItemStack handItem, int fortuneEnchantmentLevel) {
        BlockState blockState = level.getBlockState(blockPos);
        List<ItemStack> drops = Block.getDrops(blockState, level, blockPos, null, event.getPlayer(), handItem);

        // Prevents block from dropping a resource at this pos
        Block.popResource(level, event.getPos(), ItemStack.EMPTY);

        // checking if the state is an ore makes fortune with axe on melon and mushrooms not work but whatever, who really does that?
//            if (RechantedCommonConfigs.FORTUNE_NERF_ENABLED.get()
//                    && fortuneEnchantmentLevel != 0
//                    && blockState.is(Tags.Blocks.ORES)) {
//                drops = Block.getDrops(blockState, level, blockPos, null);
//                applyNerfedFortune(drops, fortuneEnchantmentLevel);
//            } else {
//            }

        // Get wisdom if applied
        int wisdomEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechanted:wisdom", handItem, event.getPlayer().registryAccess());

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
            TelekinesisEnchantmentHandler.markItemEntityForTelekinesis(itemEntity, event.getPlayer());
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
            ExperienceOrb expOrb = new ExperienceOrb(
                    level,
                    blockPos.getX() + 0.5D,
                    blockPos.getY() + 0.5D,
                    blockPos.getZ() + 0.5D,
                    expToDrop
            );
            TelekinesisEnchantmentHandler.markExperienceOrbForTelekinesis(expOrb, player);
            level.addFreshEntity(expOrb);
        }
    }

    // Vein miner / timber specific
    private static void destroyBulkBlocks(BlockEvent.BreakEvent event, BlockPos[] blocksToDestroy, ServerLevel level, ItemStack handItem, int telekinesisEnchantmentLevel, int fortuneEnchantmentLevel) {
        int destroyedSuccessfully = 0;
        //int wisdomEnchantment = UtilFunctions.getEnchantmentFromItem("rechanted:wisdom", handItem, event.getPlayer().registryAccess());

        for (BlockPos blockPos : blocksToDestroy) {
            BlockState blockState = level.getBlockState(blockPos);

            // Account for telekinesis with each block to destroy
            if (telekinesisEnchantmentLevel != 0) {
                telepathicallyDestroyBlock(event, blockPos, level, handItem, fortuneEnchantmentLevel);
                ++destroyedSuccessfully;
            }

            // If no telekinesis, just do basic block destroy manually.
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

}

