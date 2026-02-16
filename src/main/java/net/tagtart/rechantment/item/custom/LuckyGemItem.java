package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.entity.LuckyGemEntity;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.List;

public class LuckyGemItem extends Item {
    private static final int MAX_TOOLTIP_WIDTH = 165;

    public LuckyGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.lucky_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.lucky_gem.description");
        tooltipComponents.add(Component.literal(" "));

        List<String> splitText = UtilFunctions.wrapText(itemDescription.getString(), MAX_TOOLTIP_WIDTH);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDER_EYE_LAUNCH,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            LuckyGemEntity luckyGemEntity = new LuckyGemEntity(level, player.getX(), player.getY(0.5D), player.getZ());
            luckyGemEntity.setItem(itemStack.copyWithCount(1));
            luckyGemEntity.signalInDirection(player.getLookAngle());
            level.addFreshEntity(luckyGemEntity);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
