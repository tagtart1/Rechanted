package net.tagtart.rechantment.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Rechantment.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SUCCESS_RATE = register("success_rate",
            builder -> builder.persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REROLLED_SUCCESS_RATE = register("rerolled_success_rate",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ANNOUNCE_ON_FOUND = register("announce_on_found",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SHOULD_ANNOUNCE_GEM = register("should_announce_gem",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> GEM_OBTAINED = register("gem_obtained",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_CLONE = register("is_clone",
            builder -> builder.persistent(Codec.BOOL));

    // Distinct from IS_CLONE in that it represents items that have been used to create clone already,
    // which will not have a special shader effect unlike items that ARE clones.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HAS_BEEN_CLONED = register("is_clone_visually",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REBORN = register("reborn",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHINY_CHANCE_GEM_USES = register("shiny_chance_gem_uses",
            builder -> builder.persistent(Codec.INT));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register (String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
