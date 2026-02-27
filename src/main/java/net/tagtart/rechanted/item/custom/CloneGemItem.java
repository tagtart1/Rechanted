package net.tagtart.rechanted.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.tagtart.rechanted.component.ModDataComponents;
import net.tagtart.rechanted.item.ModItems;
import net.tagtart.rechanted.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CloneGemItem extends Item {
    public CloneGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechanted.clone_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechanted.clone_gem.desc");
        Component loreDescription = Component.translatable("item.rechanted.clone_gem.desc_lore");

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

        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(Component.literal("→ ᴅʀᴀɢ ɴ ᴅʀᴏᴘ ᴏɴᴛᴏ ʏᴏᴜʀ").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("ʙᴏᴏᴋ ᴛᴏ ᴀᴘᴘʟʏ ᴛʜɪꜱ ɢᴇᴍ").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot otherSlot, ClickAction action, Player player) {
        ItemStack otherItemStack = otherSlot.getItem();
        Item item = otherItemStack.getItem();

        if (!(item instanceof RechantedBookItem)) { return super.overrideStackedOnOther(stack, otherSlot, action, player);}

        // Check if the book is clone; can't clone a clone.
        boolean cloned = otherItemStack.getOrDefault(ModDataComponents.IS_CLONE, false);
        boolean hasBeenCloned = otherItemStack.getOrDefault(ModDataComponents.HAS_BEEN_CLONED, false);
        if (cloned) {
            player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            if (player.level().isClientSide)
                player.sendSystemMessage(Component.literal("Cannot clone a cloned book!").withStyle(ChatFormatting.RED));
        } else if (hasBeenCloned) {
            player.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
            if (player.level().isClientSide)
                player.sendSystemMessage(Component.literal("This book has already been used make a clone!").withStyle(ChatFormatting.RED));
        } else {

            ItemEnchantments storedEnchants = otherItemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (storedEnchants.isEmpty()) {
                return false;
            }

            otherItemStack.set(ModDataComponents.HAS_BEEN_CLONED.get(), true);

            ItemStack clonedBookStack = new ItemStack(ModItems.RECHANTED_BOOK.get());
            clonedBookStack.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants);
            clonedBookStack.set(ModDataComponents.IS_CLONE, true);
            clonedBookStack.set(ModDataComponents.SUCCESS_RATE, otherItemStack.get(ModDataComponents.SUCCESS_RATE));

            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 1.6f);

            stack.setCount(0);

            player.containerMenu.setCarried(clonedBookStack);
        }

        return true;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }
}
