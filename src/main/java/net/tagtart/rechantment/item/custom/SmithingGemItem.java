package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SmithingGemItem extends SmithingTemplateItem {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final Component NETHERITE_UPGRADE_APPLIES_TO = Component.translatable("item.minecraft.smithing_template.netherite_upgrade.applies_to")
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_INGREDIENTS = Component.translatable("item.minecraft.smithing_template.netherite_upgrade.ingredients")
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_TITLE = Component.translatable("upgrade.minecraft.netherite_upgrade")
            .withStyle(TITLE_FORMAT);
    private static final Component NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
            "item.minecraft.smithing_template.netherite_upgrade.base_slot_description"
    );
    private static final Component NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
            "item.minecraft.smithing_template.netherite_upgrade.additions_slot_description"
    );

    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.withDefaultNamespace("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");

    private static final List<ResourceLocation> NETHERITE_UPGRADE_BASE_SLOT_ICONS = List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_HOE,
            EMPTY_SLOT_SHOVEL
    );
    private static final List<ResourceLocation> NETHERITE_UPGRADE_ADDITION_ICONS = List.of(EMPTY_SLOT_INGOT);

    public SmithingGemItem() {
        super(
                NETHERITE_UPGRADE_APPLIES_TO,
                NETHERITE_UPGRADE_INGREDIENTS,
                NETHERITE_UPGRADE_TITLE,
                NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION,
                NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
                NETHERITE_UPGRADE_BASE_SLOT_ICONS,
                NETHERITE_UPGRADE_ADDITION_ICONS
        );
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.smithing_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal(" "));
        tooltipComponents.add(Component.translatable("item.rechantment.smithing_gem.desc").withStyle(ChatFormatting.WHITE));
        tooltipComponents.add(Component.literal(" "));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
