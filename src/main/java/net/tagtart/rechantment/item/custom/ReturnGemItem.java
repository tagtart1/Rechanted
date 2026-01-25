package net.tagtart.rechantment.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.entity.ModEntities;
import net.tagtart.rechantment.entity.ReturnGemBeamEntity;
import net.tagtart.rechantment.event.TickDelayedTasks;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReturnGemItem extends Item {
    public ReturnGemItem(Properties properties) {
        super(properties);
    }

    private static final int MAX_TOOLTIP_WIDTH = 165;

    private final SoundInstance chargeSound = SimpleSoundInstance.forLocalAmbience(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), 1.0f, 1.0f);
    private static final Random rand = new Random();

    public record ReturnGemTransition(ReturnGemBeamEntity entity, DimensionTransition transition) {}

    private static final Map<UUID, ReturnGemTransition> aliveTransitions = new HashMap<>();

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechantment.return_gem").withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component itemDescription = Component.translatable("item.rechantment.return_gem.description");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

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
        origin = new Vec3(origin.x, livingEntity.getEyeY() - 0.25, origin.z);
        origin = origin.add(livingEntity.getLookAngle().scale(0.2));

        double dx1 = rand.nextDouble(-1.0, 1.0);
        double dy1 = rand.nextDouble(-1.0, 1.0);
        double dz1 = rand.nextDouble(-1.0, 1.0);

        double dx2 = rand.nextDouble(-0.1, 0.1);
        double dy2 = rand.nextDouble(-0.1, 0.1);
        double dz2 = rand.nextDouble(-0.1, 0.1);

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

        if (livingEntity instanceof ServerPlayer player)    {
            BlockPos respawnPoint = player.getRespawnPosition();
            Level respawnLevel = level.getServer().getLevel(player.getRespawnDimension());

            // Do nothing if no respawn point.
            if (respawnPoint == null || respawnLevel == null) {
                return stack;
            }

            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(true, (entity) -> {

                ServerLevel serverLevel = (ServerLevel)player.level();
                Vec3 pos = player.position();

                TickDelayedTasks.enqueuedTasks.add(new TickDelayedTasks.TickDelayedTask(4) {
                       @Override
                       public void onTicksDelayElapsed() {
                           Random rand = new Random();
                           for (int i = 0; i < 50; i++) {

                               double xOffset = rand.nextDouble(-1, 1) * 2.0;
                               double zOffset = rand.nextDouble(-1, 1) * 2.0;

                               serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.x + xOffset, pos.y + 0.5, pos.z + zOffset, 2, 0, 0.02, 0, 0.1);
                           }
                       }
                   });

                serverLevel.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS);
            });

            if (transition.missingRespawnBlock()) {
                return stack;
            }

            spawnReturnEntity(player, transition);

            player.getCooldowns().addCooldown(this, 40);
            stack.setCount(0);
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


    public static void spawnReturnEntity(ServerPlayer player, DimensionTransition transition) {
        ReturnGemBeamEntity newEntity = new ReturnGemBeamEntity(ModEntities.RETURN_GEM_BEAM_ENTITY.get(), player.level());
        newEntity.setPlayer(player);
        newEntity.setPos(new Vec3(player.getX(), player.getY() - 10.0, player.getZ()));

        player.level().addFreshEntity(newEntity);
        aliveTransitions.put(player.getUUID(), new ReturnGemTransition(newEntity, transition));

        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = (ServerLevel)player.level();
            Vec3 pos = player.position();

            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_ACTIVATE, SoundSource.PLAYERS);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS);
        }
    }

    public static void removeReturnEntity(Player player) {
        ReturnGemTransition remTransition = aliveTransitions.remove(player.getUUID());
        if (remTransition != null) {
            DimensionTransition transition = remTransition.transition;

            if (player instanceof ServerPlayer serverPlayer) {
                //serverPlayer.teleportTo(transition.newLevel(), transition.pos().x, transition.pos().y, transition.pos().z, transition.yRot(), transition.xRot());
                serverPlayer.serverLevel().getChunkSource().addRegionTicket(
                        TicketType.POST_TELEPORT,
                        new ChunkPos(new BlockPos((int)transition.pos().x, (int)transition.pos().y, (int)transition.pos().z)),
                        1,
                        player.getId()
                );
                player.changeDimension(transition);
            }
        }
    }
}
