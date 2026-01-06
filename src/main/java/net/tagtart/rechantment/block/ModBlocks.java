package net.tagtart.rechantment.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.createBlocks(Rechantment.MOD_ID);

//    private static DeferredHolder<Block, Block> registerBlock(String name, Supplier<Block> block) {
//        // Registers the block into the registry, similar to items
//        DeferredHolder<Block, Block> toReturn = BLOCKS.register(name, block);
//        // Registers the block item
//        registerBlockItem(name, toReturn);
//        // Returns the block
//        return toReturn;
//    }

    // Register the actual block ITEM into the registry, like what you see in the inventory
//    private static DeferredHolder<Block, Block> registerBlockItem(String name, DeferredHolder<Block, Block> block) {
//        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
//    }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
