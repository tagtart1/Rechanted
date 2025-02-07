package net.tagtart.rechantment.item.custom;

import net.minecraft.sounds.SoundSource;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class EnchantedBookItem extends Item {


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

    public EnchantedBookItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (!pStack.hasTag()) return Component.literal("Empty book!");
        CompoundTag rootTag = pStack.getTag();
        if (rootTag == null) return Component.literal("Empty book!");
        CompoundTag enchantmentTag = rootTag.getCompound("Enchantment");
        String enchantmentRaw = enchantmentTag.getString("id");
        int enchantmentLevel = enchantmentTag.getInt("lvl");
        String[] enchantmentInfo = enchantmentRaw.split(":");
        String enchantmentSource = enchantmentInfo[0];
        String enchantmentName = enchantmentInfo[1];
        BookRarityProperties enchantRarityInfo = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantmentLevel);

        ResourceLocation resourceLocation = new ResourceLocation(enchantmentRaw);
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
        if (enchantment == null || enchantRarityInfo == null)
            return Component.literal("Invalid enchantment!");
        int enchantmentLvl = enchantmentTag.getInt("lvl");
        String romanLevel ="";

        if (enchantment.getMaxLevel() != 1)
            romanLevel = Component.translatable("enchantment.level." + enchantmentLvl).getString();

        String enchantFormattedName = Component.translatable("enchantment." + enchantmentSource + "." + enchantmentName).getString();
        String rarityIcon = Component.translatable("enchantment.rarity." + enchantRarityInfo.key).getString();

        return Component.literal(rarityIcon + " ")
                .append(Component.literal(enchantFormattedName + " " + romanLevel)
                        .withStyle(Style.EMPTY.withColor(enchantRarityInfo.color)));

    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (!pStack.hasTag()) return;
        if (pStack.getTag() == null) return;


        int maxWidthTooltip = 165;
        int successRate = pStack.getTag().getInt("SuccessRate");
        CompoundTag enchantmentTag = pStack.getTag().getCompound("Enchantment");
        String enchantmentRaw = enchantmentTag.getString("id");
        String[] enchantmentInfo = enchantmentRaw.split(":");

        pTooltipComponents.add(Component.literal(" "));

        Component translatable = Component.translatable("enchantment." + enchantmentInfo[0] + "." + enchantmentInfo[1] + ".description");
        String resolvedText = translatable.getString();
        List<String> splitText = UtilFunctions.wrapText(resolvedText, maxWidthTooltip);
        for (String line : splitText) {
            pTooltipComponents.add(Component.literal(line.trim()));
        }

        pTooltipComponents.add(Component.literal(" "));

        pTooltipComponents.add(Component.literal("Success Rate: ")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN))  // Green for "Success Rate: "
                .append(Component.literal(successRate + "%")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
        pTooltipComponents.add(Component.literal(" "));
        pTooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.literal("ɪᴛᴇᴍ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ʙᴏᴏᴋ").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.literal(" "));


        ResourceLocation resourceLocation = new ResourceLocation(enchantmentRaw);
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);

        if (enchantment == null) return;

        pTooltipComponents.add(getApplicableIcons(enchantment));

    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack bookStack, Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer) {

        ItemStack itemToEnchantStack = pSlot.getItem();

        if (pAction == ClickAction.PRIMARY && (itemToEnchantStack.isEnchanted() || itemToEnchantStack.isEnchantable())) {
            if (pPlayer.getAbilities().instabuild) {
                pPlayer.sendSystemMessage(Component.literal("Books cannot be applied in creative mode!").withStyle(ChatFormatting.RED));
                return true;
            }
            if (!pPlayer.level().isClientSide()) {
                // Server stuff here

                Level level = pPlayer.level();


                if (bookStack.getTag() == null) return false;
                CompoundTag enchantmentTag = bookStack.getTag().getCompound("Enchantment");
                int enchantmentLevel = enchantmentTag.getInt("lvl");
                String enchantmentRaw = enchantmentTag.getString("id");
                ResourceLocation resourceLocation = new ResourceLocation(enchantmentRaw);
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
                if (enchantment == null) return false;
                boolean canEnchantGeneral = enchantment.canEnchant(itemToEnchantStack);

                Map<Enchantment, Integer> otherStackEnchantmentsInfo = EnchantmentHelper.getEnchantments(itemToEnchantStack);
                Collection<Enchantment> otherStackEnchants = otherStackEnchantmentsInfo.keySet();


                if (canEnchantGeneral && !itemToEnchantStack.isEnchanted()) {
                    // No enchantments on the other item so it can be applied

                    otherStackEnchantmentsInfo.put(enchantment, enchantmentLevel);
                    applyEnchantsSafely(otherStackEnchantmentsInfo, itemToEnchantStack, pPlayer, level, bookStack);

                } else if (canEnchantGeneral) {
                    // Loop through each of the otherStacks enchant and make sure they are compatible with the incoming enchant

                    if (otherStackEnchants.contains(enchantment)) {
                        int otherEnchantLevel = otherStackEnchantmentsInfo.get(enchantment);
                        if (otherEnchantLevel == enchantment.getMaxLevel()) {
                            sendClientMessage(pPlayer, Component.literal("This item already has this enchantment maxed!").withStyle(ChatFormatting.RED));

                        } else if (enchantmentLevel < otherEnchantLevel) {
                            sendClientMessage(pPlayer, Component.literal("This item already has this enchantment!").withStyle(ChatFormatting.RED));

                        } else {
                            if (otherEnchantLevel == enchantmentLevel) {
                                otherStackEnchantmentsInfo.put(enchantment, otherEnchantLevel + 1 );
                            } else {
                                otherStackEnchantmentsInfo.put(enchantment, enchantmentLevel );
                            }
                            applyEnchantsSafely(otherStackEnchantmentsInfo, itemToEnchantStack, pPlayer, level, bookStack);

                        }
                    } else {
                        for (Enchantment otherEnchantment : otherStackEnchants) {
                            boolean isCompatible = otherEnchantment.isCompatibleWith(enchantment);

                            if (!isCompatible) {
                                sendClientMessage(pPlayer, Component.translatable(enchantment.getDescriptionId())
                                        .append(" is not compatible with ")
                                        .append(Component.translatable(otherEnchantment.getDescriptionId()))
                                        .withStyle(ChatFormatting.RED) );
                                return true;
                            }
                        }
                        // Enchant good to go, enchant that thing!
                        otherStackEnchantmentsInfo.put(enchantment, enchantmentLevel);
                        applyEnchantsSafely(otherStackEnchantmentsInfo, itemToEnchantStack, pPlayer, level, bookStack);

                    }
                } else {
                    sendClientMessage(pPlayer, Component.literal("Enchantment cannot be applied to this item").withStyle(ChatFormatting.RED));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return true;
    }

    private void applyEnchantsSafely(Map<Enchantment, Integer> enchants, ItemStack item, Player pPlayer, Level level, ItemStack enchantedBook) {
        if (enchantedBook.getTag() == null) return;
        int slotIndex = pPlayer.getInventory().findSlotMatchingItem(item);
        int successRate = enchantedBook.getTag().getInt("SuccessRate");
        if (isSuccessfulEnchant(successRate)) {
            EnchantmentHelper.setEnchantments(enchants, item);
            level.playSound(null, pPlayer.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
            sendClientMessage(pPlayer, Component.literal("Successfully enchanted.").withStyle(ChatFormatting.GREEN));
        } else {
            // Play bad sound
            level.playSound(null, pPlayer.getOnPos(), ModSounds.ENCHANTED_BOOK_FAIL.get(), SoundSource.PLAYERS, 1f, 1f);
            sendClientMessage(pPlayer, Component.literal("Enchantment failed to apply to item!").withStyle(ChatFormatting.RED));
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

    public static Component getApplicableIcons(Enchantment enchantment) {
        if (enchantment == null) {
            return Component.literal("");
        }

        MutableComponent text = Component.translatable("");
        for (String itemName : BASE_ICON_ITEMS) {
            ItemStack item = UtilFunctions.getItemStackFromString(itemName);
            if (enchantment.canEnchant(item)) {
                // Breaks up the itemname to only get the identify string for the icon
                String[] itemNameParts = itemName.split("[:_]");
                String coreName = itemNameParts[itemNameParts.length - 1];
                // Get the icon png from the translatable
                text.append(Component.translatable("enchantment.icon." +coreName));
            }

        }
        return text;
    }
}
