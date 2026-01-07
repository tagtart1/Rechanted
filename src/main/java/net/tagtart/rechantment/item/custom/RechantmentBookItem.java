package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        Enchantment enchantment =  enchantmentHolder.value();

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

        tooltipComponents.add(getApplicableIcons(enchantment));

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

//    @Override
//    public @NotNull InteractionResult useOn(UseOnContext context) {
//        Level level = context.getLevel();
//        if (!level.isClientSide) {
//
//            // EXAMPLE way of getting enchants stored on something
//            ItemStack item = context.getItemInHand();
//
//            ItemEnchantments.Mutable storedEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
//
//            Holder<Enchantment> sharpness = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
//            storedEnchants.set(sharpness, 5);
//
//            item.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants.toImmutable());
//        }
//
//        return InteractionResult.SUCCESS;
//    }

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
