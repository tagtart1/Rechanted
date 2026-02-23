package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.List;

public class TastyGemItem extends Item {
    public TastyGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.tasty_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.tasty_gem.desc");
        Component loreDescription = Component.translatable("item.rechantment.tasty_gem.desc_lore");

        String itemDescriptionString = itemDescription.getString();
        String loreDescriptionString = loreDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, 165);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }

        tooltipComponents.add(Component.literal(" "));

        List<String> splitLoreText = UtilFunctions.wrapText(loreDescriptionString, 165);
        for (String s : splitLoreText) {
            tooltipComponents.add(Component.literal(s.trim()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
