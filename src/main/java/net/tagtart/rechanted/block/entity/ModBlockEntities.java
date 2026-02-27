package net.tagtart.rechanted.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.block.ModBlocks;
import net.tagtart.rechanted.block.custom.RechantedTableBlock;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Rechanted.MOD_ID); // This instead of mod-id makes it replace a vanilla registry.

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RechantedTableBlockEntity>> RECHANTED_TABLE_BE =
            BLOCK_ENTITIES.register("rechanted_enchanting_table", () ->
                    BlockEntityType.Builder.of(RechantedTableBlockEntity::new,
                            ModBlocks.RECHANTED_TABLE_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RechantedTrophyBlockEntity>> RECHANTED_TROPHY_BE =
            BLOCK_ENTITIES.register("rechanted_trophy", () ->
                    BlockEntityType.Builder.of(RechantedTrophyBlockEntity::new,
                            ModBlocks.RECHANTED_TROPHY_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register((eventBus));
    }
}
