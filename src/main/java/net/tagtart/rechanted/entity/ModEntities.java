package net.tagtart.rechanted.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechanted.Rechanted;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Rechanted.MOD_ID);

    public static DeferredHolder<EntityType<?> ,EntityType<ReturnGemBeamEntity>> RETURN_GEM_BEAM_ENTITY = ENTITY_TYPES.register("return_gem_beam", () ->
        EntityType.Builder.of(ReturnGemBeamEntity::new, MobCategory.MISC).build("return_gem_beam"));

    public static DeferredHolder<EntityType<?>, EntityType<ThrownWarpGemEntity>> THROWN_WARP_GEM_ENTITY = ENTITY_TYPES.register(
        "thrown_warp_gem",
        () -> EntityType.Builder.<ThrownWarpGemEntity>of(ThrownWarpGemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(1).build("thrown_warp_gem")
    );

    public static DeferredHolder<EntityType<?>, EntityType<LuckyGemEntity>> LUCKY_GEM_ENTITY = ENTITY_TYPES.register(
        "lucky_gem",
        () -> EntityType.Builder.<LuckyGemEntity>of(LuckyGemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(1).build("lucky_gem")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
