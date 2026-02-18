package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ShinyChanceGemItem extends Item {
    public ShinyChanceGemItem(Properties properties) {
        super(properties);
    }

    private static final int MAX_TOOLTIP_WIDTH = 165;

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.shiny_chance_gem").withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.shiny_chance_gem.desc");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        // Prevents the description text from making the tooltip go across the entire
        // screen like a chump
        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, MAX_TOOLTIP_WIDTH);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }

        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("ʙᴏᴏᴋ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ɢᴇᴍ").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot otherSlot, ClickAction action, Player player) {
        ItemStack otherItemStack = otherSlot.getItem();
        Item item = otherItemStack.getItem();

        if (!(item instanceof RechantmentBookItem)) {
            return super.overrideStackedOnOther(stack, otherSlot, action, player);
        }

        if (player.getAbilities().instabuild) {
            applyCreative(stack, otherItemStack, player);
            return true;
        }

        if (hasAlreadyBeenRerolled(otherItemStack)) {
            player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            if (player.level().isClientSide) {
                player.sendSystemMessage(
                        Component.literal("This book has already been randomized!").withStyle(ChatFormatting.RED));
            }
        } else {
            BookRarityProperties appliedBookProperties = getBookPropertiesForReroll(otherItemStack);
            if (appliedBookProperties == null) {
                return false;
            }

            int currentSuccessRate = getCurrentSuccessRate(otherItemStack, appliedBookProperties);
            if (isAtOrAboveMaxSuccessRate(otherItemStack, appliedBookProperties)) {
                player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
                if (player.level().isClientSide) {
                    player.sendSystemMessage(Component
                            .literal("This book's success rate (" + currentSuccessRate
                                    + "%) is already at maximum reroll result!")
                            .withStyle(ChatFormatting.RED));
                }
                return true; // Return true to prevent further processing but don't consume the gem
            }

            Random rand = new Random();
            rerollSuccessRate(otherItemStack, appliedBookProperties, rand);

            if (!player.level().isClientSide) {
                boolean shouldShatter = shouldShatter(rand);
                if (shouldShatter) {
                    stack.shrink(1);

                    if (player instanceof ServerPlayer sp) {
                        sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 4.0f, 1.0f);
                    }
                    player.sendSystemMessage(Component.literal("Rerolled, but the Shiny Chance Gem has shattered!")
                            .withStyle(ChatFormatting.RED));
                } else {
                    // On Apply
                    player.sendSystemMessage(Component.literal("Rerolled!").withStyle(ChatFormatting.GREEN));

                    // REMEMBER THIS FREAKING PATTERN FOR PLAYING SOUNDS, MAYBE USEFUL, CHECK SERVER
                    // PLAYER FIRST
                    if (player instanceof ServerPlayer sp) {
                        sp.playNotifySound(ModSounds.ENDER_EYE_DEATH.get(), SoundSource.PLAYERS, 0.9f, 1.6f);
                    }

                }
            }
        }

        return true;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    private static boolean shouldShatter(Random rand) {
        return rand.nextDouble() < RechantmentCommonConfigs.SHINY_CHANCE_GEM_BREAK_CHANCE.get();
    }

    private void applyCreative(ItemStack stack, ItemStack otherItemStack, Player player) {
        if (hasAlreadyBeenRerolled(otherItemStack)) {
            player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            player.sendSystemMessage(Component.literal("This book has already been randomized!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        BookRarityProperties appliedBookProperties = getBookPropertiesForReroll(otherItemStack);
        if (appliedBookProperties == null) {
            return;
        }

        int currentSuccessRate = getCurrentSuccessRate(otherItemStack, appliedBookProperties);
        if (isAtOrAboveMaxSuccessRate(otherItemStack, appliedBookProperties)) {
            player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            player.sendSystemMessage(Component.literal("This book's success rate (" + currentSuccessRate
                    + "%) is already at maximum reroll result!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        Random rand = new Random();
        rerollSuccessRate(otherItemStack, appliedBookProperties, rand);

        boolean shouldShatter = shouldShatter(rand);
        if (shouldShatter) {
            stack.shrink(1);
            player.sendSystemMessage(Component.literal("Rerolled, but the Shiny Chance Gem has shattered!")
                    .withStyle(ChatFormatting.RED));
            player.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 4.0f, 1.0f);
        } else {
            player.sendSystemMessage(Component.literal("Rerolled!")
                    .withStyle(ChatFormatting.GREEN));
            player.playSound(ModSounds.ENDER_EYE_DEATH.get(), 0.9f, 1.6f);
        }
    }

    private static boolean hasAlreadyBeenRerolled(ItemStack otherItemStack) {
        return otherItemStack.getOrDefault(ModDataComponents.REROLLED_SUCCESS_RATE, false);
    }

    private static BookRarityProperties getBookPropertiesForReroll(ItemStack otherItemStack) {
        ItemEnchantments storedEnchants = otherItemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS,
                ItemEnchantments.EMPTY);
        if (storedEnchants.isEmpty()) {
            return null;
        }

        var bookEnchantEntry = storedEnchants.entrySet().iterator().next();
        Holder<Enchantment> enchantmentHolder = bookEnchantEntry.getKey();
        String enchantmentRaw = enchantmentHolder.unwrapKey().orElseThrow().location().toString();
        return UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);
    }

    private static int getCurrentSuccessRate(ItemStack otherItemStack, BookRarityProperties appliedBookProperties) {
        return otherItemStack.getOrDefault(ModDataComponents.SUCCESS_RATE, appliedBookProperties.minSuccess);
    }

    private static boolean isAtOrAboveMaxSuccessRate(ItemStack otherItemStack,
            BookRarityProperties appliedBookProperties) {
        return getCurrentSuccessRate(otherItemStack, appliedBookProperties) >= appliedBookProperties.maxSuccess;
    }

    private static void rerollSuccessRate(ItemStack otherItemStack, BookRarityProperties appliedBookProperties,
            Random rand) {
        int newSuccessRate = rand.nextInt(appliedBookProperties.minSuccess, appliedBookProperties.maxSuccess + 1);
        otherItemStack.set(ModDataComponents.SUCCESS_RATE, newSuccessRate);
        otherItemStack.set(ModDataComponents.REROLLED_SUCCESS_RATE, true);
    }
}
