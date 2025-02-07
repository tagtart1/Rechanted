package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChanceGem extends Item {
    public ChanceGem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.chance_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        Component itemDescription = Component.translatable("item.rechantment.chance_gem.description");

        String itemDescriptionString = itemDescription.getString();
        int maxWidthTooltip = 165;
        pTooltipComponents.add(Component.literal(" "));
        // pTooltipComponents.add(itemDescription);
        // Prevents the description text from making the tooltip go across the entire screen like a chump
         List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, maxWidthTooltip);
        for (String s : splitText) {
            pTooltipComponents.add(Component.literal(s.trim()));
        }

        pTooltipComponents.add(Component.literal(" "));

        pTooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.literal("ʙᴏᴏᴋ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ɢᴇᴍ").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pStack, Slot otherSlot, ClickAction pAction, Player pPlayer) {
        ItemStack stack = otherSlot.getItem();
        Item item = stack.getItem();

        if (!(item instanceof EnchantedBookItem)) {return super.overrideStackedOnOther(pStack, otherSlot, pAction, pPlayer);}
        if (!stack.hasTag()) {return super.overrideStackedOnOther(pStack, otherSlot, pAction, pPlayer);}

        CompoundTag tag = stack.getTag();
        if (tag == null) return true;

        // Check if the book has already been randomized
        if (tag.contains("Randomized")) {
            pPlayer.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            if (pPlayer.level().isClientSide)
                 pPlayer.sendSystemMessage(Component.literal("This book has already been randomized!").withStyle(ChatFormatting.RED));
        } else {

            CompoundTag enchantmentTag = stack.getTag().getCompound("Enchantment");
            String enchantmentRaw = enchantmentTag.getString("id");
            int enchantmentLevel = enchantmentTag.getInt("lvl");
            BookRarityProperties appliedBookProperties = UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw, enchantmentLevel);

            if (appliedBookProperties == null) return false;

            // Randomize the rate
            int upperBound = appliedBookProperties.maxSuccess;
            int lowerBound = appliedBookProperties.minSuccess;
            Random rand = new Random();

            // Generate a new SuccessRate, inclusive bounds
            int newSuccessRate = rand.nextInt(lowerBound, upperBound + 1);
            tag.putInt("SuccessRate", newSuccessRate );

            // Prevents this book from being randomized again
            tag.putBoolean("Randomized", true);

            pPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);

            // Delete gem
            pStack.shrink(1);
        }

        return true;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}
