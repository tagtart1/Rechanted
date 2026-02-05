package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tagtart.rechantment.effect.ModEffects;
import net.tagtart.rechantment.effect.SatiatedEffect;
import net.tagtart.rechantment.sound.ModSounds;
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
        Component itemDescription = Component.translatable("item.rechantment.tasty_gem.description");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, 165);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }
    }
}
