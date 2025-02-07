package net.tagtart.rechantment.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.entity.RechantmentTableBlockEntity;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.enchantment.*;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.networking.ModPackets;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import oshi.util.Util;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.util.*;
import java.util.List;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = Rechantment.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onShieldBlock(ShieldBlockEvent event) {

            float SHIELD_BASH_KNOCKBACK = 1.15f;
            float SHIELD_BASH_KNOCKBACK_Y = 0.4f;
            int SHIELD_COURAGE_SPEED_DURATION = 40; // Speed in ticks

            Player player = (Player) event.getEntity();
            DamageSource source = event.getDamageSource();
            Entity attacker = source.getEntity();
            ItemStack shield = player.getUseItem();

            if(!(shield.getItem() instanceof ShieldItem)) return;

            ResourceLocation bashResource = new ResourceLocation("rechantment:bash");
            ResourceLocation courageResource = new ResourceLocation("rechantment:courage");
            Map<Enchantment, Integer> shieldEnchants = EnchantmentHelper.getEnchantments(shield);
            // Handle bash enchantment
            if (shieldEnchants.containsKey(ForgeRegistries.ENCHANTMENTS.getValue(bashResource))) {
                if (!(source.getDirectEntity() instanceof Projectile)) {
                    double d0 = attacker.getX() - player.getX();
                    double d1 = attacker.getZ() - player.getZ();
                    Vec2 toAttacker = new Vec2((float) d0, (float) d1);
                    toAttacker = toAttacker.normalized();
                    toAttacker = toAttacker.scale(SHIELD_BASH_KNOCKBACK);


                    if (attacker.isPushable()) {
                        attacker.push(toAttacker.x, SHIELD_BASH_KNOCKBACK_Y, toAttacker.y);
                    }
                }
            }

            // Check for Courage enchantment
            if (shieldEnchants.containsKey(ForgeRegistries.ENCHANTMENTS.getValue(courageResource))) {
                  int enchantmentLevel = shieldEnchants.get(ForgeRegistries.ENCHANTMENTS.getValue(courageResource));
                  MobEffectInstance speedEffect = new MobEffectInstance(
                          MobEffects.MOVEMENT_SPEED,
                          SHIELD_COURAGE_SPEED_DURATION,
                          enchantmentLevel - 1
                  );
                  player.addEffect(speedEffect);
            }

        }

        @SubscribeEvent
        public static void onItemToolTip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            List<Component> tooltip = event.getToolTip();

            if (stack.isEnchanted()) {

                List<Map.Entry<Enchantment, Integer>> enchantsSorted = new ArrayList<>(EnchantmentHelper.getEnchantments(stack).entrySet());

                enchantsSorted.sort((component1, component2) -> {
                    String enchantmentRaw1 = ForgeRegistries.ENCHANTMENTS.getKey(component1.getKey()).toString();
                    String enchantmentRaw2 = ForgeRegistries.ENCHANTMENTS.getKey(component2.getKey()).toString();

                    int enchantmentLevel1 = component1.getValue();
                    int enchantmentLevel2 = component2.getValue();

                    BookRarityProperties rarity1 = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw1, enchantmentLevel1);
                    BookRarityProperties rarity2 = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw2, enchantmentLevel2);

                    float rarityValue1 = 0f;
                    float rarityValue2 = 0f;

                    if (rarity1 == null && component1.getKey().isCurse()) {
                        rarityValue1 = 99.0f;
                    } else if (rarity1 == null && component1.getKey() instanceof RebornEnchantment) {
                        rarityValue1 = 100f;
                    } else if (rarity1 != null) {
                        rarityValue1 = rarity1.rarity;
                    }

                    if (rarity2 == null && component2.getKey().isCurse()) {
                        rarityValue2 = 99.0f;
                    } else if (rarity2 == null && component2.getKey() instanceof RebornEnchantment) {
                        rarityValue2 = 100f;
                    } else if (rarity2 != null) {
                        rarityValue2 = rarity2.rarity;
                    }


                    return Float.compare(rarityValue2, rarityValue1);
                });



                for(int i = 1; i <= enchantsSorted.size(); i++) {

                    Map.Entry<Enchantment, Integer> entry = enchantsSorted.get(i - 1);

                    String enchantmentRaw = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()).toString();
                    int enchantmentLevel = entry.getValue();

                    BookRarityProperties rarityProperties = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantmentLevel);

                    Style style = Style.EMPTY;
                    if (entry.getKey().isCurse()) {
                        style = Style.EMPTY.withColor(ChatFormatting.RED);
                    }

                    else if (entry.getKey() instanceof RebornEnchantment) {
                        style = Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true);
                    }

                    else if (rarityProperties != null) {
                        style = Style.EMPTY.withColor(rarityProperties.color);
                    }

                    String fullEnchantName = entry.getKey().getFullname(entry.getValue()).getString();

                    Component modifiedText = Component.literal(fullEnchantName).withStyle(style);
                    CompoundTag nbt = stack.getTag();

                    if (nbt != null && nbt.contains("Trim")) {

                        tooltip.set(i + 3, modifiedText);

                    } else {
                        tooltip.set(i, modifiedText);
                    }

                }
            }

            if (stack.getItem() instanceof EnchantedBookItem) {
                tooltip.add(Component.literal("Vanilla books have been disabled.").withStyle(ChatFormatting.RED));
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            // Check if attacker is player
            if (event.getSource().getEntity() instanceof LivingEntity player) {
                ItemStack weapon = player.getMainHandItem();

                float bonusDamage = 0;
                Pair<VoidsBaneEnchantment, Integer> voidsBaneEnchantment = UtilFunctions
                        .getEnchantmentFromItem("rechantment:voids_bane",
                                weapon,
                                VoidsBaneEnchantment.class);
                Pair<HellsFuryEnchantment, Integer> hellsFuryEnchantment = UtilFunctions
                        .getEnchantmentFromItem("rechantment:hells_fury",
                                weapon,
                                HellsFuryEnchantment.class);
                Pair<BerserkEnchantment, Integer> berserkEnchantment = UtilFunctions
                        .getEnchantmentFromItem("rechantment:berserk",
                                weapon,
                                BerserkEnchantment.class);

                Pair<ThunderStrikeEnchantment, Integer> thunderStrikeEnchantment = UtilFunctions
                        .getEnchantmentFromItem("rechantment:thunder_strike",
                                weapon,
                                ThunderStrikeEnchantment.class);

                ResourceLocation targetId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
                if (targetId == null) return;
                String targetIdString = targetId.toString();
                if (voidsBaneEnchantment != null && voidsBaneEnchantment.getA().validTargets.contains(targetIdString) )
                {
                    int enchantmentOnWeaponLevel = voidsBaneEnchantment.getB();
                    bonusDamage += voidsBaneEnchantment.getA().getDamageBonus(enchantmentOnWeaponLevel);
                } else if (hellsFuryEnchantment != null && hellsFuryEnchantment.getA().validTargets.contains(targetIdString)) {
                    int enchantmentOnWeaponLevel = hellsFuryEnchantment.getB();
                    bonusDamage += hellsFuryEnchantment.getA().getDamageBonus(enchantmentOnWeaponLevel);
                }

                if (berserkEnchantment != null) {
                    int enchantmentOnWeaponLevel = berserkEnchantment.getB();
                    bonusDamage += berserkEnchantment.getA().getDamageBonus(player, enchantmentOnWeaponLevel);
                }

                if (thunderStrikeEnchantment != null) {
                    int enchantmentOnWeaponLevel = thunderStrikeEnchantment.getB();
                    bonusDamage += thunderStrikeEnchantment.getA().rollLightningStrike(
                            player,
                            event.getEntity(),
                            enchantmentOnWeaponLevel);
                }

                // Apply the damage effects
                event.setAmount(event.getAmount() + bonusDamage);
            }
        }



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
            Pair<TelepathyEnchantment, Integer> telepathyEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", handItem, TelepathyEnchantment.class);
            Pair<VeinMinerEnchantment, Integer> veinMinerEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:vein_miner", handItem, VeinMinerEnchantment.class);
            Pair<TimberEnchantment, Integer> timberEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:timber", handItem, TimberEnchantment.class);
            Pair<WisdomEnchantment, Integer> wisdomEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:wisdom", handItem, WisdomEnchantment.class);
            int fortuneEnchantmentLevel = handItem.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            ServerLevel level = (ServerLevel) event.getPlayer().level();


            if (veinMinerEnchantment != null && event.getState().is(Tags.Blocks.ORES)) {
                BlockPos[] oresToDestroy = UtilFunctions.BFSLevelForBlocks(level, Tags.Blocks.ORES, event.getPos(), 10, true);
                destroyBulkBlocks(event, oresToDestroy, level, handItem, (telepathyEnchantment != null) ? telepathyEnchantment.getA() : null, fortuneEnchantmentLevel);
            }

            else if (timberEnchantment != null && event.getState().is(BlockTags.LOGS)) {
                BlockPos[] woodToDestroy = UtilFunctions.BFSLevelForBlocks(level, BlockTags.LOGS, event.getPos(), 10, true);
                destroyBulkBlocks(event, woodToDestroy, level, handItem, (telepathyEnchantment != null) ? telepathyEnchantment.getA() : null, fortuneEnchantmentLevel);
            }

            // Telepathy check. Only happens when destroying a single block normally here
            else if (telepathyEnchantment != null) {

                telepathicallyDestroyBlock(event, event.getPos(), level, handItem, fortuneEnchantmentLevel);

                if (event.getState().getDestroySpeed(level, event.getPos()) != 0)
                    handItem.hurt(1, level.random, (ServerPlayer)event.getPlayer());
            }

            // Fortune by itself with vanilla enchants without other breakevent enchant
            else if (fortuneEnchantmentLevel != 0 && RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()){
                // Block info
                BlockState blockState = event.getState();
                Block block = blockState.getBlock();
                BlockPos blockPos = event.getPos();

                if (!blockState.is(Tags.Blocks.ORES) || !handItem.isCorrectToolForDrops(blockState)) return;

                level.removeBlock(blockPos, false);
                // Fetch the block drops without tool
                List<ItemStack> drops = Block.getDrops(event.getState(), level, event.getPos(), null);
               // Pop exp
               int expToPop = blockState.getExpDrop(level, RandomSource.create(), blockPos, 0, 0);
               if (wisdomEnchantment != null) {
                   float expMultiplier = wisdomEnchantment.getA().getExpMultiplier(wisdomEnchantment.getB());
                   expToPop = (int)((float)expToPop * expMultiplier);
                }
                block.popExperience(level, blockPos, expToPop);


               applyNerfedFortune(drops, fortuneEnchantmentLevel);
               Block.popResource(level, event.getPos(), ItemStack.EMPTY);


               // Pop the resource with nerfed fortune
                for(ItemStack drop : drops) {
                    Block.popResource(level, blockPos, drop);
                }

                handItem.hurt(1, level.random, (ServerPlayer)event.getPlayer());

            }

            // Wisdom by itself without the other break event enchant
            else if (wisdomEnchantment != null) {
                float expMultiplier = wisdomEnchantment.getA().getExpMultiplier(wisdomEnchantment.getB());
                int newExpToDrop = (int)((float)event.getExpToDrop() * expMultiplier);
                event.setExpToDrop(newExpToDrop);
            }
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

        private static void telepathicallyDestroyBlock(BlockEvent.BreakEvent event, BlockPos blockPos, ServerLevel level, ItemStack handItem, int fortuneEnchantmentLevel) {
            List<ItemStack> drops = Collections.emptyList();
            BlockState blockState = level.getBlockState(blockPos);

            // Prevents block from dropping a resource at this pos
            Block.popResource(level, event.getPos(), ItemStack.EMPTY);

            // checking if the state is an ore makes fortune with axe on melon and mushrooms not work but whatever, who really does that?
            if (RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()
                    && fortuneEnchantmentLevel != 0
                    && blockState.is(Tags.Blocks.ORES)) {
                drops = Block.getDrops(blockState, level, blockPos, null);
                applyNerfedFortune(drops, fortuneEnchantmentLevel);
            } else {
                drops =  Block.getDrops(blockState, level, blockPos, null, event.getPlayer(), handItem);
            }



            // Get wisdom if applied
            Pair<WisdomEnchantment, Integer> wisdomEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:wisdom", handItem, WisdomEnchantment.class);

            // This check prevents a block from "breaking" twice while also working with vein miner breaks
            if (event.getPos() != blockPos) {
                 level.destroyBlock(blockPos, false);
            } else {
                 level.removeBlock(blockPos, false);
            }

            // Prevents an axe from mining diamonds for example
            if (!blockState.canHarvestBlock(level, blockPos, event.getPlayer())) return;


            // Get silk if applied
            boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(handItem);
            int expToDrop = blockState.getExpDrop(level, RandomSource.create(), blockPos, 0, hasSilkTouch ? 1 : 0);
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
            if (expToDrop > 0) {
                Player player = event.getPlayer();

                // Multiply if we have wisdom on the tool
                if (wisdomEnchantment != null) {
                    float expMultiplier = wisdomEnchantment.getA().getExpMultiplier(wisdomEnchantment.getB());
                    expToDrop = (int) ((float) expToDrop * expMultiplier);
                }
                ExperienceOrb expOrb = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), expToDrop);
                level.addFreshEntity(expOrb);
            }
        }

        // Vein miner / timber specific
        private static void destroyBulkBlocks(BlockEvent.BreakEvent event, BlockPos[] blocksToDestroy, ServerLevel level, ItemStack handItem, @Nullable TelepathyEnchantment telepathyEnchantment, int fortuneEnchantmentLevel) {
            int destroyedSuccessfully = 0;
            Pair<WisdomEnchantment, Integer> wisdomEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:wisdom", handItem, WisdomEnchantment.class);

            for (BlockPos blockPos : blocksToDestroy) {
                BlockState blockState = level.getBlockState(blockPos);

                // Account for telepathy with each block to destroy
                if (telepathyEnchantment != null ) {
                    telepathicallyDestroyBlock(event, blockPos, level, handItem, fortuneEnchantmentLevel);
                    ++destroyedSuccessfully;
                }

                // If no telepathy, just do basic block destroy manually.
                else {
                    ++destroyedSuccessfully;
                    List<ItemStack> itemsToDrop = null;


                    // Checks for fortune nerf requirements
                    if (RechantmentCommonConfigs.FORTUNE_NERF_ENABLED.get()
                            && fortuneEnchantmentLevel != 0
                            && blockState.is(Tags.Blocks.ORES)) {
                        itemsToDrop = Block.getDrops(blockState, level, blockPos, null);
                        applyNerfedFortune(itemsToDrop, fortuneEnchantmentLevel);

                        Block.popResource(level, event.getPos(), ItemStack.EMPTY);
                    }

                    // Normal drops based off the tool in hand
                    else {
                        itemsToDrop = Block.getDrops(blockState, level, blockPos, null, event.getPlayer(), handItem);
                    }


                    // Create correct particle and noise block breaking effects
                    if (event.getPos() != blockPos ) {
                        level.destroyBlock(blockPos, false);
                    } else {
                        level.removeBlock(blockPos, false);
                    }


                    // Manually pop the resource
                    if (handItem.isCorrectToolForDrops(blockState)) {
                        for (ItemStack item : itemsToDrop) {
                            Block.popResource(level, blockPos, item);
                        }

                        Block block = blockState.getBlock();
                        boolean hasSilkTouch = EnchantmentHelper.hasSilkTouch(handItem);
                        int expToDrop = blockState.getExpDrop(level, RandomSource.create(), blockPos, 0, hasSilkTouch ? 1 : 0);

                        if (wisdomEnchantment != null) {
                            float expMultiplier = wisdomEnchantment.getA().getExpMultiplier(wisdomEnchantment.getB());
                            expToDrop = (int) ((float) expToDrop * expMultiplier);
                        }

                        block.popExperience(level, blockPos, expToDrop);
                    }
                }
            }

            handItem.hurt(destroyedSuccessfully, level.random, (ServerPlayer)event.getPlayer());
        }

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {

            // Prevents teleporting player drops
            if (event.getEntity() instanceof Player) return;

           if ((event.getSource().getEntity() instanceof Player player)) {
               ItemStack weapon = player.getMainHandItem();
               Pair<TelepathyEnchantment, Integer> telepathyEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", weapon, TelepathyEnchantment.class );
               if (telepathyEnchantment == null) return;

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
            Pair<InquisitiveEnchantment, Integer> inquisitiveEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:inquisitive", weapon, InquisitiveEnchantment.class);
            Pair<TelepathyEnchantment, Integer> telepathyEnchantment = UtilFunctions.getEnchantmentFromItem("rechantment:telepathy", weapon, TelepathyEnchantment.class);


            int expToDrop = event.getDroppedExperience();

            if (inquisitiveEnchantment != null && mobCategory == MobCategory.MONSTER) {
                InquisitiveEnchantment inquisitiveEnchantInstance = inquisitiveEnchantment.getA();
                int enchantLevel = inquisitiveEnchantment.getB();
                expToDrop = (int)((float) expToDrop * inquisitiveEnchantInstance.getExpMultiplier(enchantLevel));
            }

            if (telepathyEnchantment != null) {
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

        private static final UUID healthModifierUUID = UUID.fromString("ac527260-8c85-49fc-ab4a-7b7ed7580a18");

        @SubscribeEvent
        public static void onArmorEquip(LivingEquipmentChangeEvent event) {
            if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) return;
            if (!(event.getEntity() instanceof Player player)) return;
            ItemStack newArmor = event.getTo();
            ItemStack oldArmor = event.getFrom();
            Pair<OverloadEnchantment, Integer> overloadEnchantmentNewArmor = UtilFunctions.getEnchantmentFromItem(
                    "rechantment:overload",
                    newArmor,
                    OverloadEnchantment.class
            );

            boolean overloadJustEquipped = overloadEnchantmentNewArmor != null && !ItemStack.isSameItem(newArmor, oldArmor);

            float newMaxHealthIncrease = 0f;
            for (ItemStack armor : player.getInventory().armor) {
                Pair<OverloadEnchantment, Integer> overloadEnchantment = UtilFunctions.getEnchantmentFromItem(
                        "rechantment:overload",
                        armor,
                        OverloadEnchantment.class
                );

                if (overloadEnchantment != null) {
                    newMaxHealthIncrease += overloadEnchantment.getA().getMaxHealthTier(overloadEnchantment.getB());
                }
            }

            // Regardless of determined max health increase, remove modifier each update so that health
            // goes back to normal if no overload exists, or can be properly updated with new value relative to base value.
            AttributeModifier overloadModifier = new AttributeModifier(
                    healthModifierUUID,
                    "overload_max_health_increase",
                    newMaxHealthIncrease,
                    AttributeModifier.Operation.ADDITION
            );
            AttributeInstance currentMaxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (currentMaxHealthAttribute.hasModifier(overloadModifier)) {
                currentMaxHealthAttribute.removeModifier(overloadModifier);
            }

            if (newMaxHealthIncrease > 0f) {
                currentMaxHealthAttribute.addPermanentModifier(overloadModifier);
            }

            // Now apply health increase, and play sound if new max health has left extra hearts.
            if (player.getHealth() <= player.getMaxHealth() && newMaxHealthIncrease > 0f && overloadJustEquipped) {

                // Make sure this plays
                player.level().playSound(null, player.getOnPos(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.15f, 1f);

            } else {
                // Covers case where overload is unequipped and player would have more health than their max
                // (which is retained by default).
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

        @SubscribeEvent
        public static void onPickup(PlayerEvent.ItemPickupEvent event) {
            ItemStack pStack = event.getStack();
            if (!(event.getEntity().level() instanceof ServerLevel level)) return;
            if (!pStack.hasTag()) return;
            CompoundTag tag = pStack.getTag();
            if (tag == null) return;
            boolean shouldAnnounce = tag.getBoolean("Announce");
            if (!shouldAnnounce) return;

            tag.remove("Announce");
            int successRate = tag.getInt("SuccessRate");
            String enchantmentRaw = tag.getCompound("Enchantment").getString("id");
            int enchantLevel = tag.getCompound("Enchantment").getInt("lvl");
            String enchantmentFormatted = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentRaw)).getFullname(enchantLevel).getString();

            Component playerName = event.getEntity().getDisplayName();
            Component displayName = pStack.getDisplayName();
            Style displayHoverStyle = displayName.getStyle();

            BookRarityProperties bookProps = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantLevel);
            if (bookProps == null) return;

            for (ServerPlayer otherPlayer : level.players()) {
                otherPlayer.sendSystemMessage(Component.literal(playerName.getString() + " found ")
                        .append(Component.literal(enchantmentFormatted).withStyle(displayHoverStyle.withColor(bookProps.color).withUnderlined(true)))
                        .append(" at ")
                        .append(Component.literal(successRate + "%").withStyle(Style.EMPTY.withColor(bookProps.color)))
                        .append("!"));
            }

            level.playSound(null, event.getEntity().getOnPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);

        }

        @SubscribeEvent
        public static void onGrindstoneChange(GrindstoneEvent.OnPlaceItem event) {

            ItemStack topSlot = event.getTopItem();
            ItemStack bottomSlot = event.getBottomItem();

            boolean rechantmentBookInTopOnly = (!topSlot.isEmpty() && topSlot.is(ModItems.ENCHANTED_BOOK.get()) && bottomSlot.isEmpty());
            boolean rechantmentBookInBottomOnly = (!bottomSlot.isEmpty() && bottomSlot.is(ModItems.ENCHANTED_BOOK.get()) && topSlot.isEmpty());

            if (rechantmentBookInTopOnly || rechantmentBookInBottomOnly) {

                ItemStack currentStack = rechantmentBookInTopOnly ? topSlot : bottomSlot;
                CompoundTag rootTag = currentStack.getTag();
                CompoundTag enchantmentTag = rootTag.getCompound("Enchantment");
                String enchantmentRaw = enchantmentTag.getString("id");
                int enchantmentLevel = enchantmentTag.getInt("lvl");
                BookRarityProperties enchantRarityInfo = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantmentLevel);

                Random rand = new Random();
                event.setXp(rand.nextInt(enchantRarityInfo.minGrindstoneXP, enchantRarityInfo.maxGrindstoneXP + 1));

                ResourceLocation itemLocation = new ResourceLocation(RechantmentCommonConfigs.GRINDSTONE_RESULT_ITEM.get());
                event.setOutput(new ItemStack(ForgeRegistries.ITEMS.getValue(itemLocation)));
            }

        }
    }
}
