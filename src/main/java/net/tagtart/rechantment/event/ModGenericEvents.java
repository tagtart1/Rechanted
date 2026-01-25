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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.enchantment.custom.InquisitiveEnchantmentEffect;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.networking.data.OpenEnchantTableScreenC2SPayload;
import net.tagtart.rechantment.networking.data.PlayerPurchaseEnchantedBookC2SPayload;
import net.tagtart.rechantment.networking.data.TriggerRebirthItemEffectS2CPayload;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.*;
import java.util.List;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class ModGenericEvents {

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
    public static void onItemUsedOnBlock(UseItemOnBlockEvent event) {
        Level level = event.getUseOnContext().getLevel();
        BlockPos useOnPos = event.getUseOnContext().getClickedPos();

        if (!level.isClientSide()) {

            ItemStack usedItem = event.getItemStack();
            boolean isVanillaEnchantingTable = level.getBlockState(useOnPos).is(Blocks.ENCHANTING_TABLE);

            if (usedItem.is(Items.EMERALD) && isVanillaEnchantingTable) {

                //level.destroyBlock(useOnPos, false);
                usedItem.setCount(usedItem.getCount() - 1);
                level.setBlock(useOnPos, ModBlocks.RECHANTMENT_TABLE_BLOCK.get().defaultBlockState(), 3);
                level.playSound(null, useOnPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,1.5f, 1.0f);
                level.playSound(null, useOnPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,0.4f, 2.5f);
                event.cancelWithResult(ItemInteractionResult.CONSUME);
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
