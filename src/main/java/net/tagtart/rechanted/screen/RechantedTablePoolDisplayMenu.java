package net.tagtart.rechanted.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tagtart.rechanted.block.ModBlocks;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.util.BookRarityProperties;

public class RechantedTablePoolDisplayMenu extends AbstractContainerMenu {

    public final RechantedTableBlockEntity blockEntity;
    public final Level level;

    public final int startingPropertiesIndex;

    public RechantedTablePoolDisplayMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), extraData.readInt()); // Normal to first properties index
    }

    public RechantedTablePoolDisplayMenu(int pContainerId, Inventory inv, BlockEntity entity, int bookPropertiesIndex) {
        super(ModMenuTypes.RECHANTED_TABLE_POOL_DISPLAY_MENU.get(), pContainerId);

        blockEntity = (RechantedTableBlockEntity) entity;
        level = inv.player.level();
        startingPropertiesIndex = bookPropertiesIndex % BookRarityProperties.getAllProperties().length;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.RECHANTED_TABLE_BLOCK.get());
    }
}
