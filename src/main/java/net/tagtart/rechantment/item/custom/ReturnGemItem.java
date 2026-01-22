package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ReturnGemItem extends Item {
    public ReturnGemItem(Properties properties) {
        super(properties);
    }

    private static final int MAX_TOOLTIP_WIDTH = 165;

    private final SoundInstance chargeSound = SimpleSoundInstance.forLocalAmbience(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), 1.0f, 1.0f);
    private static final Random rand = new Random();

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.return_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.return_gem.description");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        // Prevents the description text from making the tooltip go across the entire screen like a chump
        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, MAX_TOOLTIP_WIDTH);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 90;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        player.startUsingItem(usedHand);
        playSound(level, player);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {

        // Place particle slightly in front of player so they can see them better
        Vec3 origin = livingEntity.position();
        origin = new Vec3(origin.x, livingEntity.getEyeY() + 0.15, origin.z);
        origin = origin.add(livingEntity.getLookAngle().scale(0.2));

        double dx1 = rand.nextDouble(0.0, 1.1);
        double dy1 = rand.nextDouble(0.0, 1.1);
        double dz1 = rand.nextDouble(0.0, 1.1);

        double dx2 = rand.nextDouble(0.0, 0.1);
        double dy2 = rand.nextDouble(0.0, 0.1);
        double dz2 = rand.nextDouble(0.0, 0.1);

        level.addParticle(ParticleTypes.ENCHANT, origin.x, origin.y, origin.z, dx1, dy1, dz1);
        level.addParticle(ParticleTypes.SMOKE, origin.x, origin.y, origin.z, dx2, dy2, dz2);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide()) {
            Minecraft.getInstance().getSoundManager().stop(chargeSound);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {

        // Todo: make a pure white beam with a half transparent, slightly larger copy of it that grows and rotates on y-axis over time.
        // - Could use beacon texture on it; can copy how beacon is rendered but do it in your own renderer class.
        // - Add sound event when first used here, and add sound that plays when player spawns at their bed.
        // - Add particles when player spawns at bed as well.

        if (livingEntity instanceof ServerPlayer player)    {
            BlockPos respawnPoint = player.getRespawnPosition();
            Level respawnLevel = level.getServer().getLevel(player.getRespawnDimension());

            // Do nothing if no respawn point.
            if (respawnPoint == null || respawnLevel == null) {
                return stack;
            }

            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(true, (entity) -> {
                // Idk if this is needed but this will get called after the transition occurs.
            });
            if (transition.missingRespawnBlock()) {
                return stack;
            }

            // Safe to teleport player now.
            player.getCooldowns().addCooldown(this, 40);
            stack.setCount(0);
            player.teleportTo(transition.newLevel(), transition.pos().x, transition.pos().y, transition.pos().z, transition.yRot(), transition.xRot());
        }
            

        return stack;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    private void playSound(Level level, Player player) {
        if (level.isClientSide()) {
            Minecraft.getInstance().getSoundManager().play(chargeSound);
        }
    }
}
