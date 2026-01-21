package net.tagtart.rechantment.event;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.enchantment.custom.InquisitiveEnchantmentEffect;
import net.tagtart.rechantment.enchantment.custom.WisdomEnchantmentEffect;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.networking.data.OpenEnchantTableScreenC2SPayload;
import net.tagtart.rechantment.networking.data.PlayerPurchaseEnchantedBookC2SPayload;
import net.tagtart.rechantment.networking.data.TriggerRebirthItemEffectS2CPayload;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class ModEvents {

    @EventBusSubscriber(modid = Rechantment.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);

            registrar.commonToServer(
                    OpenEnchantTableScreenC2SPayload.TYPE,
                    OpenEnchantTableScreenC2SPayload.STREAM_CODEC,
                    OpenEnchantTableScreenC2SPayload::handlePayloadOnServerNetwork
            );

            registrar.commonToServer(
                    PlayerPurchaseEnchantedBookC2SPayload.TYPE,
                    PlayerPurchaseEnchantedBookC2SPayload.STREAM_CODEC,
                    PlayerPurchaseEnchantedBookC2SPayload::handlePayloadOnServerNetwork
            );

            registrar.commonToClient(
                    TriggerRebirthItemEffectS2CPayload.TYPE,
                    TriggerRebirthItemEffectS2CPayload.STREAM_CODEC,
                    TriggerRebirthItemEffectS2CPayload::handlePayloadOnClientMain
            );
        }



        @SubscribeEvent
        public static void onShieldBlock(LivingShieldBlockEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;

            DamageSource source = event.getDamageSource();
            Entity attacker = source.getEntity();
            ItemStack shield = player.getUseItem();

            if(!(shield.getItem() instanceof ShieldItem)) return;

            HolderLookup.Provider registryAccess = player.level().registryAccess();

            // Get the bash enchantment level
            Holder<Enchantment> bashHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                    .flatMap(registry -> registry.get(ModEnchantments.BASH))
                    .orElse(null);

            // Handle bash enchantment
            if (bashHolder != null) {
                int bashLevel = shield.getEnchantmentLevel(bashHolder);

                if (bashLevel > 0 && attacker != null) {
                    // Don't bash projectiles, only melee attackers
                    if (!(source.getDirectEntity() instanceof Projectile)) {
                        // Calculate knockback direction (away from player)
                        double d0 = attacker.getX() - player.getX();
                        double d1 = attacker.getZ() - player.getZ();
                        Vec2 toAttacker = new Vec2((float) d0, (float) d1);
                        toAttacker = toAttacker.normalized();
                        toAttacker = toAttacker.scale(1.15f);

                        // Apply knockback
                        if (attacker.isPushable()) {
                            attacker.push(toAttacker.x, 0.4f, toAttacker.y);
                        }
                    }
                }
            }

            // Get the courage enchantment level
            Holder<Enchantment> courageHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                    .flatMap(registry -> registry.get(ModEnchantments.COURAGE))
                    .orElse(null);

            // Handle courage enchantment
            if (courageHolder != null) {
                int courageLevel = shield.getEnchantmentLevel(courageHolder);

                if (courageLevel > 0) {
                    int SHIELD_COURAGE_SPEED_DURATION = 40; // Speed in ticks (2 seconds)
                    MobEffectInstance speedEffect = new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            SHIELD_COURAGE_SPEED_DURATION,
                            courageLevel - 1
                    );
                    player.addEffect(speedEffect);
                }
            }

        }

        @SubscribeEvent
        public static void onItemToolTip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            List<Component> tooltip = event.getToolTip();

            if (event.getEntity() == null)
                return;

            RegistryAccess registryAccess = event.getEntity().registryAccess();

            if (stack.getItem() instanceof EnchantedBookItem) {
                tooltip.add(Component.literal("Vanilla books have been disabled.").withStyle(ChatFormatting.RED));
            }

            else if (stack.isEnchanted()) {

                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                List<Object2IntMap.Entry<Holder<Enchantment>>> enchantsSorted = new ArrayList<>(enchantments.entrySet());
                Holder<Enchantment> rebornEnchantment = UtilFunctions.getEnchantmentReferenceIfPresent(registryAccess, ModEnchantments.REBORN);

                enchantsSorted.sort((component1, component2) -> {
                    String enchantmentRaw1 = component1.getKey().unwrapKey().get().location().toString();
                    String enchantmentRaw2 = component2.getKey().unwrapKey().get().location().toString();

                    BookRarityProperties rarity1 = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw1);
                    BookRarityProperties rarity2 = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw2);


                    float rarityValue1 = 0f;
                    float rarityValue2 = 0f;

                    if (rarity1 == null && component1.getKey().is(EnchantmentTags.CURSE)) {
                        rarityValue1 = 99.0f;
                    }
                    else if (rarity1 == null && component1.getKey() == rebornEnchantment) {
                        rarityValue1 = 100f;

                    }
                    else if (rarity1 != null) {

                        rarityValue1 = rarity1.rarity;
                    }

                    if (rarity2 == null && component2.getKey().is(EnchantmentTags.CURSE)) {
                        rarityValue2 = 99.0f;
                    }
                    else if (rarity2 == null && component2.getKey() == rebornEnchantment) {
                        rarityValue1 = 100f;

                    }
                    else if (rarity2 != null) {
                        rarityValue2 = rarity2.rarity;
                    }


                    return Float.compare(rarityValue2, rarityValue1);
                });

                // First pass:
                // Only replace line indices that match an enchantment's translated name.
                // This is technically a foolproof way of doing this but is very slow and I hate to do it like this.
                // If there's a better way to accomplish the same thing then this def should be refactored.
                int enchantmentTooltipsStartIndex = tooltip.size();
                for(int i = 1; i <= enchantsSorted.size(); i++) {

                    Object2IntMap.Entry<Holder<Enchantment>> entry = enchantsSorted.get(i - 1);

                    Component fullEnchantName = Enchantment.getFullname(entry.getKey(), enchantments.getLevel(entry.getKey()));


                    MutableComponent translatedText = Component.translatable(fullEnchantName.getString());
                    String translatedString = translatedText.getString(); // avoid redundant toString calls below
                    Optional<Component> replacedComponent = tooltip.stream().filter((text) -> {
                        String existingTranslated = text.getString();
                        if (existingTranslated.equalsIgnoreCase(translatedString)) {
                            return true;
                        }
                        return false;
                    }).findFirst();
                    if (replacedComponent.isPresent()) {
                        int replaceIndex = tooltip.indexOf(replacedComponent.get());
                        enchantmentTooltipsStartIndex = Math.min(replaceIndex, enchantmentTooltipsStartIndex);
                    }
                }

                // Second pass:
                // Now actually replace tooltips of enchantments in our desired order.
                int replacementIndex = enchantmentTooltipsStartIndex;
                for(int i = 1; i <= enchantsSorted.size(); i++) {

                    Object2IntMap.Entry<Holder<Enchantment>> entry = enchantsSorted.get(i - 1);
                    String enchantmentRaw = entry.getKey().unwrapKey().get().location().toString();
                    BookRarityProperties rarityProperties = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);

                    Style style = Style.EMPTY;
                    if (entry.getKey().is(EnchantmentTags.CURSE)) {
                        style = Style.EMPTY.withColor(ChatFormatting.RED);
                    }

                    else if (entry.getKey() == rebornEnchantment) {
                        style = Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true);
                    }

                    else if (rarityProperties != null) {
                        style = Style.EMPTY.withColor(rarityProperties.color);
                    }

                    Component fullEnchantName = Enchantment.getFullname(entry.getKey(), enchantments.getLevel(entry.getKey()));
                    tooltip.set(replacementIndex++, Component.translatable(fullEnchantName.getString()).withStyle((style)));
                }
            }


        }

        // TODO: Reimplement when enchantment system is ported.
//        @SubscribeEvent
//        public static void onLivingHurt(LivingIncomingDamageEvent event) {
//            // Check if attacker is player
//            if (event.getSource().getEntity() instanceof LivingEntity player) {
//                ItemStack weapon = player.getMainHandItem();
//
//                float bonusDamage = 0;
//                Pair<VoidsBaneEnchantment, Integer> voidsBaneEnchantment = UtilFunctions
//                        .getEnchantmentFromItem("rechantment:voids_bane",
//                                weapon,
//                                VoidsBaneEnchantment.class);
//                Pair<HellsFuryEnchantment, Integer> hellsFuryEnchantment = UtilFunctions
//                        .getEnchantmentFromItem("rechantment:hells_fury",
//                                weapon,
//                                HellsFuryEnchantment.class);
//                Pair<BerserkEnchantment, Integer> berserkEnchantment = UtilFunctions
//                        .getEnchantmentFromItem("rechantment:berserk",
//                                weapon,
//                                BerserkEnchantment.class);
//
//                Pair<ThunderStrikeEnchantment, Integer> thunderStrikeEnchantment = UtilFunctions
//                        .getEnchantmentFromItem("rechantment:thunder_strike",
//                                weapon,
//                                ThunderStrikeEnchantment.class);
//
//                ResourceLocation targetId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
//                if (targetId == null) return;
//                String targetIdString = targetId.toString();
//                if (voidsBaneEnchantment != null && voidsBaneEnchantment.getA().validTargets.contains(targetIdString) )
//                {
//                    int enchantmentOnWeaponLevel = voidsBaneEnchantment.getB();
//                    bonusDamage += voidsBaneEnchantment.getA().getDamageBonus(enchantmentOnWeaponLevel);
//                } else if (hellsFuryEnchantment != null && hellsFuryEnchantment.getA().validTargets.contains(targetIdString)) {
//                    int enchantmentOnWeaponLevel = hellsFuryEnchantment.getB();
//                    bonusDamage += hellsFuryEnchantment.getA().getDamageBonus(enchantmentOnWeaponLevel);
//                }
//
//                if (berserkEnchantment != null) {
//                    int enchantmentOnWeaponLevel = berserkEnchantment.getB();
//                    bonusDamage += berserkEnchantment.getA().getDamageBonus(player, enchantmentOnWeaponLevel);
//                }
//
//                if (thunderStrikeEnchantment != null) {
//                    int enchantmentOnWeaponLevel = thunderStrikeEnchantment.getB();
//                    bonusDamage += thunderStrikeEnchantment.getA().rollLightningStrike(
//                            player,
//                            event.getEntity(),
//                            enchantmentOnWeaponLevel);
//                }
//
//                // Apply the damage effects
//                event.setAmount(event.getAmount() + bonusDamage);
//            }
//        }
//

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

            // TODO: REPLACE DIRECT STRING USES WHEN CHECK FOR ENCHANTMENT WITH DIRECT RESOURCE KEY REFERENCES!!!!!!!
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
                if (!event.getPlayer().addItem(drop)) {
                    event.getPlayer().drop(drop, false);
                }
                // Make pickup noise for telepathy
                else {
                    Random random = new Random();
                    float randomPitch = .9f + random.nextFloat() * (1.6f - .9f);
                    level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .25f, randomPitch);
                }
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

        // TODO: Reimplement when enchantment system is ported.
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
        @SubscribeEvent
        public static void onItemBreak(PlayerDestroyItemEvent event) {
            ItemStack itemStack = event.getOriginal();
            ServerPlayer player = (ServerPlayer)event.getEntity();

            int rebirthEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", itemStack, event.getEntity().registryAccess());
            if (rebirthEnchantmentLevel != 0) {
                if (shouldBeReborn(rebirthEnchantmentLevel)){

                    ItemStack newItemStack = itemStack.copy();
                    newItemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (itemEnchantments) -> {

                        ItemEnchantments.Mutable mutableCopy = new ItemEnchantments.Mutable(itemEnchantments);
                        mutableCopy.removeIf((enchantment) -> enchantment.getKey() == ModEnchantments.REBIRTH);

                        Holder<Enchantment> rebornEnchantment = UtilFunctions.getEnchantmentReferenceIfPresent(event.getEntity().registryAccess(), ModEnchantments.REBORN);
                        if (rebornEnchantment != null) {
                            mutableCopy.set(rebornEnchantment, 1);
                        }

                    return mutableCopy.toImmutable();
                    });
                    newItemStack.setDamageValue(0);
                    newItemStack.remove(DataComponents.REPAIR_COST);

                    int freeSlot = player.getInventory().selected;
                    boolean isOffhand = event.getHand() == InteractionHand.OFF_HAND;

                    ScheduledRebirthTasks.EnqueueItemForRebirth(player, newItemStack, freeSlot, isOffhand);

                } else {
                    player.sendSystemMessage(Component.literal("Your item failed to be reborn!").withStyle(ChatFormatting.RED));
                }
            }

        }

        private static final List<Float> REBIRTH_SUCCESS_RATES = Arrays.asList(
                0.01f,
                0.75f,
                1.00f
        );
        private static boolean shouldBeReborn(int rebirthEnchantmentLevel) {
            float successRate =  REBIRTH_SUCCESS_RATES.get(rebirthEnchantmentLevel - 1);
            Random random = new Random();
            return random.nextFloat() < successRate;
        }

        // TODO: Reimplement when enchantment system is ported.
        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {

            // Prevents teleporting player drops
            if (event.getEntity() instanceof Player) return;

           if ((event.getSource().getEntity() instanceof Player player)) {
               ItemStack weapon = player.getMainHandItem();
               int telepathyEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", weapon, event.getEntity().registryAccess());
               if (telepathyEnchantment == 0) return;

               Collection<ItemEntity> items = event.getDrops();

               for (ItemEntity item : items) {
                   if (!player.addItem(item.getItem())) {
                       ItemStack itemToDrop = item.getItem();
                       player.drop(itemToDrop, false);
                   }
               }

               event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onExpDropFromHostile(LivingExperienceDropEvent event) {
            MobCategory mobCategory = event.getEntity().getType().getCategory();
            if (event.getAttackingPlayer() == null) return;
            ItemStack weapon = event.getAttackingPlayer().getMainHandItem();
            int inquisitiveEnchantmentLevel = UtilFunctions.getEnchantmentFromItem("rechantment:inquisitive", weapon, event.getAttackingPlayer().registryAccess());
            int telepathyEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", weapon, event.getAttackingPlayer().registryAccess());


            int expToDrop = event.getDroppedExperience();

            if (inquisitiveEnchantmentLevel != 0 && mobCategory == MobCategory.MONSTER) {
                expToDrop = (int)InquisitiveEnchantmentEffect.trueProcess(inquisitiveEnchantmentLevel, RandomSource.create(), (float)expToDrop);
            }

            if (telepathyEnchantment != 0) {
                Player player = event.getAttackingPlayer();
                ExperienceOrb expOrb = new ExperienceOrb(event.getAttackingPlayer().level(), player.getX(), player.getY(), player.getZ(), expToDrop);
                event.getAttackingPlayer().level().addFreshEntity(expOrb);
                event.setDroppedExperience(0);
            } else {
                event.setDroppedExperience(expToDrop);
            }
        }

        @SubscribeEvent
        public static void onAnvilUpdate(AnvilUpdateEvent event) {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();

            // Turn off vanilla enchanted book from applying
            if (left.getItem() instanceof EnchantedBookItem || right.getItem() instanceof EnchantedBookItem) {
                event.setCanceled(true);
            }
        }

        private static final ResourceLocation OVERLOAD_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "overload_max_health");
        private static final float OVERLOAD_HEALTH_PER_LEVEL = 2.0f; // +2 HP (1 heart) per level

        @SubscribeEvent
        public static void onArmorEquip(LivingEquipmentChangeEvent event) {
            // Check if the changed equipment is an armor piece
            EquipmentSlot slot = event.getSlot();
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;
            if (!(event.getEntity() instanceof Player player)) return;

            ItemStack newArmor = event.getTo();
            ItemStack oldArmor = event.getFrom();

            HolderLookup.Provider registryAccess = player.level().registryAccess();
            Holder<Enchantment> overloadHolder = registryAccess.lookup(Registries.ENCHANTMENT)
                    .flatMap(registry -> registry.get(ModEnchantments.OVERLOAD))
                    .orElse(null);

            if (overloadHolder == null) return;

            boolean overloadJustEquipped = newArmor.getEnchantmentLevel(overloadHolder) > 0 && !ItemStack.isSameItem(newArmor, oldArmor);

            // Calculate total health increase from all armor pieces
            float newMaxHealthIncrease = 0f;
            for (ItemStack armor : player.getInventory().armor) {
                int overloadLevel = armor.getEnchantmentLevel(overloadHolder);
                if (overloadLevel > 0) {
                    newMaxHealthIncrease += overloadLevel * OVERLOAD_HEALTH_PER_LEVEL;
                }
            }

            // Remove old modifier and apply new one with updated value
            AttributeModifier overloadModifier = new AttributeModifier(
                    OVERLOAD_HEALTH_MODIFIER_ID,
                    newMaxHealthIncrease,
                    AttributeModifier.Operation.ADD_VALUE
            );

            AttributeInstance currentMaxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (currentMaxHealthAttribute == null) return;

            // Remove old modifier if present
            if (currentMaxHealthAttribute.hasModifier(OVERLOAD_HEALTH_MODIFIER_ID)) {
                currentMaxHealthAttribute.removeModifier(OVERLOAD_HEALTH_MODIFIER_ID);
            }

            // Add new modifier if health increase > 0
            if (newMaxHealthIncrease > 0f) {
                currentMaxHealthAttribute.addPermanentModifier(overloadModifier);
            }

            // Play sound when equipping armor with overload
            if (player.getHealth() <= player.getMaxHealth() && newMaxHealthIncrease > 0f && overloadJustEquipped) {
                player.level().playSound(null, player.getOnPos(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.15f, 1f);
            } else {
                // Reduce health if player has more than new max (when unequipping overload)
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                    player.level().playSound(null, player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z, SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1f, 1f);
                }
            }
        }

//        @SubscribeEvent
//        public static void onItemBreak(PlayerDestroyItemEvent event) {
//            if (event.getEntity().level().isClientSide()) {
//                return;
//            }
//            ServerPlayer player = (ServerPlayer)event.getEntity();
//
//            ItemStack brokenItem = event.getOriginal();
//
//            Pair<RebirthEnchantment, Integer> rebirthEnchantmentPair = UtilFunctions.getEnchantmentFromItem("rechantment:rebirth", brokenItem, RebirthEnchantment.class);
//
//            if (rebirthEnchantmentPair == null)  return;
//            RebirthEnchantment rebirthEnchantment = rebirthEnchantmentPair.getA();
//
//            // Item is reborn
//            if (rebirthEnchantment.shouldBeReborn(rebirthEnchantmentPair.getB())) {
//
//
//                if (event.getHand() == InteractionHand.OFF_HAND) {
//                    if(!player.addItem(brokenItem)) {
//                        player.drop(brokenItem, false);
//                    }
//                    UtilFunctions.triggerRebirthClientEffects(player,(ServerLevel) player.level(), brokenItem);
//                } else {
//
//                    int selectedSlot = player.getInventory().selected;
//                    if (player.getInventory().getItem(selectedSlot).isEmpty()) {
//                        player.getInventory().setItem(selectedSlot, brokenItem);
//                    } else {
//                        player.drop(brokenItem, false); // Drop the item if the slot is occupied
//                    }
//                    UtilFunctions.triggerRebirthClientEffects(player,(ServerLevel) player.level(), brokenItem);
//                }
//            }
//
//            // Send fail message
//            else {
//                player.sendSystemMessage(Component.literal("Your item failed to be reborn!").withStyle(ChatFormatting.RED));
//            }
//        }

        // TODO: Reimplement when enchantment system is ported.
        //  This one specifically also needs port from tags to data components figured out
//        @SubscribeEvent
//        public static void onPickup(ItemEntityPickupEvent event) {
//            ItemStack pStack = event.getItemEntity().getItem();
//            if (!(event.getPlayer().level() instanceof ServerLevel level)) return;
//            if (!pStack.hasTag()) return;
//            CompoundTag tag = pStack.getTag();
//            if (tag == null) return;
//            boolean shouldAnnounce = tag.getBoolean("Announce");
//            if (!shouldAnnounce) return;
//
//            tag.remove("Announce");
//            int successRate = tag.getInt("SuccessRate");
//            String enchantmentRaw = tag.getCompound("Enchantment").getString("id");
//            Style displayHoverStyle = pStack.getDisplayName().getStyle();
//            String displayNameString;
//            StringBuilder sb = new StringBuilder(pStack.getDisplayName().getString());
//            sb.delete(0, 3);
//            sb.deleteCharAt(sb.length() - 1);
//            displayNameString = sb.toString();
//            Component playerName = event.getPlayer().getDisplayName();
//            BookRarityProperties bookProps = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);
//            if (bookProps == null) return;
//
//            for (ServerPlayer otherPlayer : level.players()) {
//                otherPlayer.sendSystemMessage(Component.literal(playerName.getString() + " found ")
//                        .append(Component.literal(displayNameString).withStyle(displayHoverStyle.withColor(bookProps.color).withUnderlined(true)))
//                        .append(" at ")
//                        .append(Component.literal(successRate + "%").withStyle(Style.EMPTY.withColor(bookProps.color)))
//                        .append("!"));
//            }
//
//            level.playSound(null, event.getPlayer().getOnPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
//
//
//
//        }

        @SubscribeEvent
        public static void onGrindstoneChange(GrindstoneEvent.OnPlaceItem event) {

            ItemStack topSlot = event.getTopItem();
            ItemStack bottomSlot = event.getBottomItem();

            boolean rechantmentBookInTopOnly = (!topSlot.isEmpty() && topSlot.is(ModItems.RECHANTMENT_BOOK.get()) && bottomSlot.isEmpty());
            boolean rechantmentBookInBottomOnly = (!bottomSlot.isEmpty() && bottomSlot.is(ModItems.RECHANTMENT_BOOK.get()) && topSlot.isEmpty());

            if (rechantmentBookInTopOnly || rechantmentBookInBottomOnly) {

                ItemStack currentStack = rechantmentBookInTopOnly ? topSlot : bottomSlot;

                // TODO: Double check this still works and possibly refactor when enchantments are figured out.
                // Just did this to see if it could become compatible with the UtilFunctions.getPropertiesFromEnchantments method still somehow.
                ItemEnchantments enchantments = currentStack.get(DataComponents.ENCHANTMENTS);
//               CompoundTag rootTag = currentStack.getTag();
//               CompoundTag enchantmentTag = rootTag.getCompound("Enchantment");
//               String enchantmentRaw = enchantmentTag.getString("id");
                Holder<Enchantment> enchantmentHolder = enchantments.entrySet().iterator().next().getKey();
                String enchantmentRaw = enchantmentHolder.unwrapKey().orElseThrow().location().toString();
                BookRarityProperties enchantRarityInfo = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);

                Random rand = new Random();
                event.setXp(rand.nextInt(enchantRarityInfo.minGrindstoneXP, enchantRarityInfo.maxGrindstoneXP + 1));

                ResourceLocation itemLocation = ResourceLocation.parse(RechantmentCommonConfigs.GRINDSTONE_RESULT_ITEM.get());
                event.setOutput(new ItemStack(BuiltInRegistries.ITEM.get(itemLocation)));
            }

        }

        @SubscribeEvent
        public static void onChunkLoad(ChunkEvent.Load event) {
            if (event.isNewChunk()) {
                event.getChunk().findBlocks((blockState -> blockState.is(Blocks.ENCHANTING_TABLE)), ((blockPos, blockState) -> {
                    event.getChunk().setBlockState(blockPos, ModBlocks.RECHANTMENT_TABLE_BLOCK.get().defaultBlockState(), false);
                }));
            }
        }
    }
}
