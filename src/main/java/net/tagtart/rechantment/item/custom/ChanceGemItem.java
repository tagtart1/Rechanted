package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChanceGemItem extends Item {
    public ChanceGemItem(Properties properties) {
        super(properties);
    }

    private static final int MAX_TOOLTIP_WIDTH = 165;

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.chance_gem.description");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));
        // pTooltipComponents.add(itemDescription);
        // Prevents the description text from making the tooltip go across the entire screen like a chump
        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, MAX_TOOLTIP_WIDTH);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }

        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("ʙᴏᴏᴋ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ɢᴇᴍ").withStyle(ChatFormatting.GRAY));
    }



    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}
