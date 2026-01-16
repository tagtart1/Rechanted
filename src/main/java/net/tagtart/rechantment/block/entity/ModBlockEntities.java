package net.tagtart.rechantment.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.block.custom.RechantmentTableBlock;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Rechantment.MOD_ID); // This instead of mod-id makes it replace a vanilla registry.

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RechantmentTableBlockEntity>> RECHANTMENT_TABLE_BE =
            BLOCK_ENTITIES.register("rechantment_enchanting_table", () ->
                    BlockEntityType.Builder.of(RechantmentTableBlockEntity::new,
                            ModBlocks.RECHANTMENT_TABLE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register((eventBus));
    }
}
