package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.entity.ThrownWarpGemEntity;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.List;

public class WarpGemItem  extends Item {
    public WarpGemItem(Properties properties) {
        super(properties);
    }

    private static final int THROWN_COOLDOWN = 20;


    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.warp_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.warp_gem.description");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, 165);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDER_PEARL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        player.getCooldowns().addCooldown(this, THROWN_COOLDOWN);
        if (!level.isClientSide) {
            ThrownWarpGemEntity thrownWarpGem = new ThrownWarpGemEntity(level, player);
            thrownWarpGem.setItem(itemstack);
            thrownWarpGem.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(thrownWarpGem);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
