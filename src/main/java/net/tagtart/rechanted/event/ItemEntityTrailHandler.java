package net.tagtart.rechanted.event;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.tagtart.rechanted.Rechanted;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class ItemEntityTrailHandler {
    private static final String TRAIL_ACTIVE_KEY = "rechanted_item_trail_active";
    private static final String TRAIL_PARTICLE_KEY = "rechanted_item_trail_particle";
    private static final String TRAIL_INTERVAL_TICKS_KEY = "rechanted_item_trail_interval_ticks";
    private static final String TRAIL_PARTICLE_COUNT_KEY = "rechanted_item_trail_particle_count";

    public static void enableTrailUntilGround(ItemEntity itemEntity, SimpleParticleType particleType, int intervalTicks,
            int particleCount) {
        CompoundTag data = itemEntity.getPersistentData();
        data.putBoolean(TRAIL_ACTIVE_KEY, true);
        data.putString(TRAIL_PARTICLE_KEY, BuiltInRegistries.PARTICLE_TYPE.getKey(particleType).toString());
        data.putInt(TRAIL_INTERVAL_TICKS_KEY, Math.max(1, intervalTicks));
        data.putInt(TRAIL_PARTICLE_COUNT_KEY, Math.max(1, particleCount));
    }

    public static void clearTrail(ItemEntity itemEntity) {
        CompoundTag data = itemEntity.getPersistentData();
        data.remove(TRAIL_ACTIVE_KEY);
        data.remove(TRAIL_PARTICLE_KEY);
        data.remove(TRAIL_INTERVAL_TICKS_KEY);
        data.remove(TRAIL_PARTICLE_COUNT_KEY);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        if (!(itemEntity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag data = itemEntity.getPersistentData();
        if (!data.getBoolean(TRAIL_ACTIVE_KEY)) {
            return;
        }

        if (itemEntity.onGround()) {
            clearTrail(itemEntity);
            return;
        }

        int intervalTicks = Math.max(1, data.getInt(TRAIL_INTERVAL_TICKS_KEY));
        if (itemEntity.tickCount % intervalTicks != 0) {
            return;
        }

        int particleCount = Math.max(1, data.getInt(TRAIL_PARTICLE_COUNT_KEY));
        SimpleParticleType particleType = getTrailParticleType(data);
        serverLevel.sendParticles(
                particleType,
                itemEntity.getX(),
                itemEntity.getY() + 0.1D,
                itemEntity.getZ(),
                particleCount,
                0.04D,
                0.04D,
                0.04D,
                0.0D);
    }

    private static SimpleParticleType getTrailParticleType(CompoundTag data) {
        if (!data.contains(TRAIL_PARTICLE_KEY)) {
            return ParticleTypes.FIREWORK;
        }

        ResourceLocation particleId = ResourceLocation.tryParse(data.getString(TRAIL_PARTICLE_KEY));
        if (particleId == null || !BuiltInRegistries.PARTICLE_TYPE.containsKey(particleId)) {
            return ParticleTypes.FIREWORK;
        }

        ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleId);
        if (particleType instanceof SimpleParticleType simpleParticleType) {
            return simpleParticleType;
        }

        return ParticleTypes.FIREWORK;
    }
}
