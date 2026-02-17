package net.tagtart.rechantment.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.entity.LuckyGemEntity;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class LuckyGemPopTrailHandler {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        if (!(itemEntity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag persistentData = itemEntity.getPersistentData();
        if (!persistentData.getBoolean(LuckyGemEntity.POP_REWARD_TRAIL_ACTIVE_KEY)) {
            return;
        }

        if (itemEntity.onGround()) {
            persistentData.remove(LuckyGemEntity.POP_REWARD_TRAIL_ACTIVE_KEY);
            persistentData.remove(LuckyGemEntity.POP_REWARD_TRAIL_IS_GEM_KEY);
            return;
        }

        if (itemEntity.tickCount % 2 != 0) {
            return;
        }

        boolean isGemTrail = persistentData.getBoolean(LuckyGemEntity.POP_REWARD_TRAIL_IS_GEM_KEY);
        serverLevel.sendParticles(
                isGemTrail ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.FIREWORK,
                itemEntity.getX(),
                itemEntity.getY() + 0.1D,
                itemEntity.getZ(),
                2,
                0.04D,
                0.04D,
                0.04D,
                0.0D
        );
    }
}
