package net.tagtart.rechantment.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Rechantment.MOD_ID);

    public static DeferredHolder<EntityType<?> ,EntityType<ReturnGemBeamEntity>> RETURN_GEM_BEAM_ENTITY = ENTITY_TYPES.register("return_gem_beam", () ->
        EntityType.Builder.of(ReturnGemBeamEntity::new, MobCategory.MISC).build("return_gem_beam"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
