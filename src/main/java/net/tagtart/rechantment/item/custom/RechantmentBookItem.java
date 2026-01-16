package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class RechantmentBookItem extends Item {
    // Holds the item names for each icon on the tooltip
    public static final String[] BASE_ICON_ITEMS = {
            "minecraft:iron_helmet",
            "minecraft:iron_chestplate",
            "minecraft:iron_leggings",
            "minecraft:iron_boots",
            "minecraft:iron_pickaxe",
            "minecraft:iron_axe",
            "minecraft:iron_shovel",
            "minecraft:iron_hoe",
            "minecraft:iron_sword",
            "minecraft:fishing_rod",
            "minecraft:trident",
            "minecraft:shield",
            "minecraft:bow",
            "minecraft:crossbow",
            "minecraft:elytra",
    };

    public RechantmentBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(ItemStack pStack) {

        ItemEnchantments enchants  = pStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants == null) return Component.literal("Empty book!");

        Holder<Enchantment> enchantmentHolder =  enchants.entrySet().iterator().next().getKey();
        if (enchantmentHolder == null) return Component.literal("Invalid enchantment!");

        Enchantment enchantment = enchantmentHolder.value();

        int enchantmentLevel =  enchants.entrySet().iterator().next().getIntValue();
        String enchantmentRaw = enchantmentHolder.unwrapKey().orElseThrow().location().toString();


        String[] enchantmentInfo = enchantmentRaw.split(":");
        String enchantmentSource = enchantmentInfo[0];
        String enchantmentName = enchantmentInfo[1];
        BookRarityProperties enchantRarityInfo = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);

        if (enchantRarityInfo == null)
            return Component.literal("Invalid enchantment!");

        String romanLevel ="";

        if (enchantment.getMaxLevel() != 1)
            romanLevel = Component.translatable("enchantment.level." + enchantmentLevel).getString();

        String enchantFormattedName = Component.translatable("enchantment." + enchantmentSource + "." + enchantmentName).getString();
        String rarityIcon = Component.translatable("enchantment.rarity." + enchantRarityInfo.key).getString();

        return Component.literal(rarityIcon + " ")
                .append(Component.literal(enchantFormattedName + " " + romanLevel)
                        .withStyle(Style.EMPTY.withColor(enchantRarityInfo.color)));

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int maxWidthTooltip = 165;
        int successRate = stack.getOrDefault(ModDataComponents.SUCCESS_RATE, 0);

        var storedEnchant = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchant == null || storedEnchant.isEmpty()) {
            return;
        }
        Holder<Enchantment> enchantmentHolder =  storedEnchant.entrySet().iterator().next().getKey();
        String enchantmentRaw  = enchantmentHolder.unwrapKey().orElseThrow().location().toString();

        String[] enchantmentInfo = enchantmentRaw.split(":");

        tooltipComponents.add(Component.literal(" "));

        Component translatable = Component.translatable("enchantment." + enchantmentInfo[0] + "." + enchantmentInfo[1] + ".description");
        String resolvedText = translatable.getString();
        List<String> splitText = UtilFunctions.wrapText(resolvedText, maxWidthTooltip);
        for (String line : splitText) {
            tooltipComponents.add(Component.literal(line.trim()));
        }

        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(Component.literal("Success Rate: ")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN))  // Green for "Success Rate: "
                .append(Component.literal(successRate + "%")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
        tooltipComponents.add(Component.literal(" "));
        tooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("ɪᴛᴇᴍ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ʙᴏᴏᴋ").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(getApplicableIcons(enchantmentHolder));

    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }


    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return true;
    }


    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        ItemStack itemToEnchantStack = slot.getItem();

        boolean shouldAttemptEnchant = action == ClickAction.PRIMARY && (itemToEnchantStack.isEnchanted() || itemToEnchantStack.isEnchantable());
        if (!shouldAttemptEnchant) { return false; }

        // Dont allow creative mode enchants due to weird behavior with this method
        if (player.getAbilities().instabuild) {
            player.sendSystemMessage(Component.literal("Books cannot be applied in creative mode!").withStyle(ChatFormatting.RED));
            return true;
        }

        // Only allow server side logic
        if (player.level().isClientSide()) { return true; }

        // Server level
        Level level = player.level();

        // Get the stored enchantment from the book using data components
        ItemEnchantments storedEnchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchants == null || storedEnchants.isEmpty()) return false;

        // Get the first (and should be only) enchantment from the book
        var bookEnchantEntry = storedEnchants.entrySet().iterator().next();
        Holder<Enchantment> enchantmentHolder = bookEnchantEntry.getKey();
        int enchantmentLevel = bookEnchantEntry.getIntValue();
        Enchantment enchantment = enchantmentHolder.value();

        boolean canEnchantGeneral = itemToEnchantStack.supportsEnchantment(enchantmentHolder);

        // Get current enchantments on the target item
        ItemEnchantments itemEnchants = itemToEnchantStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(itemEnchants);

        if (canEnchantGeneral && !itemToEnchantStack.isEnchanted()) {
            // No enchantments on the other item so it can be applied
            mutableEnchants.set(enchantmentHolder, enchantmentLevel);
            applyEnchantsSafely(mutableEnchants, itemToEnchantStack, player, level, stack);
        } else if (canEnchantGeneral) {
            // Check if item already has this enchantment
            if (itemEnchants.getLevel(enchantmentHolder) > 0) {
                int otherEnchantLevel = itemEnchants.getLevel(enchantmentHolder);
                if (otherEnchantLevel == enchantment.getMaxLevel()) {
                        sendClientMessage(player, Component.literal("This item already has this enchantment maxed!").withStyle(ChatFormatting.RED));

                } else if (enchantmentLevel < otherEnchantLevel) {
                    sendClientMessage(player, Component.literal("This item already has this enchantment!").withStyle(ChatFormatting.RED));

                } else {
                    if (otherEnchantLevel == enchantmentLevel) {
                        mutableEnchants.set(enchantmentHolder, otherEnchantLevel + 1);
                    } else {
                        mutableEnchants.set(enchantmentHolder, enchantmentLevel);
                    }
                    applyEnchantsSafely(mutableEnchants, itemToEnchantStack, player, level, stack);
                }
            } else {
                // Check compatibility with other enchantments
                boolean allCompatible = true;
                for (var otherEnchantEntry : itemEnchants.entrySet()) {
                    Holder<Enchantment> otherEnchantHolder = otherEnchantEntry.getKey();
                    Enchantment otherEnchantment = otherEnchantHolder.value();
                            
                    if (!Enchantment.areCompatible(enchantmentHolder, otherEnchantHolder)) {
                        sendClientMessage(player, Component.translatable(enchantment.description().getString())
                                .append(" is not compatible with ")
                                .append(Component.translatable(otherEnchantment.description().getString()))
                                .withStyle(ChatFormatting.RED));
                        allCompatible = false;
                        break;
                    }
                }
                        
                if (allCompatible) {
                    // Enchant good to go, enchant that thing!
                    mutableEnchants.set(enchantmentHolder, enchantmentLevel);
                    applyEnchantsSafely(mutableEnchants, itemToEnchantStack, player, level, stack);
                }
            }
        } else {
            sendClientMessage(player, Component.literal("Enchantment cannot be applied to this item").withStyle(ChatFormatting.RED));
        }

        return true;
    }

    private void applyEnchantsSafely(ItemEnchantments.Mutable enchants, ItemStack item, Player player, Level level, ItemStack enchantedBook) {
        int successRate = enchantedBook.getOrDefault(ModDataComponents.SUCCESS_RATE, 0);

        if (isSuccessfulEnchant(successRate)) {
            // Apply the enchantments to the item using data components
            item.set(DataComponents.ENCHANTMENTS, enchants.toImmutable());
            level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
            sendClientMessage(player, Component.literal("Successfully enchanted.").withStyle(ChatFormatting.GREEN));
        } else {
            // Play bad sound
            level.playSound(null, player.getOnPos(), ModSounds.ENCHANTED_BOOK_FAIL.get(), SoundSource.PLAYERS, 1f, 1f);
            sendClientMessage(player, Component.literal("Enchantment failed to apply to item!").withStyle(ChatFormatting.RED));
        }
        // Break the book regardless of success or not
        enchantedBook.shrink(1);
    }


    private void sendClientMessage(Player pPlayer, Component textComponent) {
        pPlayer.sendSystemMessage(textComponent);
    }

    private boolean isSuccessfulEnchant(int successRate) {
        Random random = new Random();
        return random.nextInt(100) < successRate;
    }
    

    public static Component getApplicableIcons(Holder<Enchantment> enchantment) {
        if (enchantment == null) {
            return Component.literal("");
        }

        MutableComponent text = Component.translatable("");
        for (String itemName : BASE_ICON_ITEMS) {
            ItemStack item = UtilFunctions.getItemStackFromString(itemName);
            if (item.supportsEnchantment(enchantment)) {
                // Breaks up the item name to only get the identify string for the icon
                String[] itemNameParts = itemName.split("[:_]");
                String coreName = itemNameParts[itemNameParts.length - 1];
                // Get the icon png from the translatable
                text.append(Component.translatable("enchantment.icon." +coreName));
            }

        }
        return text;
    }
}
