package net.tagtart.rechantment.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModReplacementBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "minecraft"); // This instead of mod-id makes it replace a vanilla registry.

    public static final Supplier<BlockEntityType<RechantmentTableBlockEntity>> RECHANTMENT_TABLE_BE =
            BLOCK_ENTITIES.register("enchanting_table", () ->
                    BlockEntityType.Builder.of(RechantmentTableBlockEntity::new,
                            Blocks.ENCHANTING_TABLE).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register((eventBus));
    }
}
