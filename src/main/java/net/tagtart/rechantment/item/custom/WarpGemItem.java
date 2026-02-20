package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
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
    private static final int TRUE_WARP_GEM_MAX_DURABILITY = 24;
    private static final int[] RANDOM_WARP_GEM_DURABILITY = new int[]{12, 16, 20, 24};

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
        Component itemDescription = Component.translatable("item.rechantment.warp_gem.desc");
        Component loreDescription = Component.translatable("item.rechantment.warp_gem.desc_lore");

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
        EquipmentSlot usedSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        itemstack.hurtAndBreak(1, player, usedSlot);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }


    // Used for whenever a warp gem comes from the enchanting table and we want it to start with random damage
    public static void initializeRandomizedDurability(ItemStack stack, RandomSource random) {
        if (stack.isEmpty()) return;
        int remainingDurability = RANDOM_WARP_GEM_DURABILITY[random.nextInt(RANDOM_WARP_GEM_DURABILITY.length)];
        int startingDamage = Math.max(0, TRUE_WARP_GEM_MAX_DURABILITY - remainingDurability);
        stack.setDamageValue(startingDamage);
    }
}
