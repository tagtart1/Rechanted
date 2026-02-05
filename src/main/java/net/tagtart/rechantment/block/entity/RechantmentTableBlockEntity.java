package net.tagtart.rechantment.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.tagtart.rechantment.screen.RechantmentTableMenu;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

public class RechantmentTableBlockEntity extends EnchantingTableBlockEntity implements MenuProvider {

    // For super basic state-machine-esque logic; mainly allows the block renderer/clients to know
    // how to render the book based on custom logic happening server side.
    public enum CustomRechantmentTableState {
        Normal,         // Normal state. In this state 99.99% of the time.
        GemPending,     // Server rolled gem, book will begin floating up in the air before it's earned.
        GemEarned,      // After gem pending is done, this just makes book float back down to normal position.
    }

    public static final int GEM_PENDING_ANIMATION_LENGTH_TICKS = 60;
    public static final int GEM_EARNED_ANIMATION_LENGTH_TICKS = 20;

    public static final double GEM_EARNED_ITEM_SPAWN_Y_OFFSET = 1.5;
    public static final double GEM_EARNED_ITEM_MOVE_SPEED_ON_SPAWN = 0.3;  // Speed gem will move when spawned by table; moves in facing direction of lapis holder.

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    //private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private BlockState[] cachedBookshelvesInRange;
    private BlockState[] cachedFloorBlocksInRange;

    private long totalTicks = 0;
    private int currentIndexRequirementsMet = -1;

    public CustomRechantmentTableState tableState = CustomRechantmentTableState.Normal;
    public long lastStateChangeTime = 0;
    private ItemStack pendingGemItem = null;



    public RechantmentTableBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(pPos, pBlockState);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getItemHandlerLapisStack() { return itemHandler.getStackInSlot(0); }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.RECHANTMENT_TABLE_BE.get();
    }

    // Makes the object drop the items currently inside the itemHandler slots.
    public void dropInventory() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void onBreak() {
        dropInventory();
        stopAmbientSound();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory inventory, Player player) {
        return new RechantmentTableMenu(pContainerId, inventory, this);
    }

    // For saving the data of what is inside the block when the game is saved.
    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.put("inventory", itemHandler.serializeNBT(registries));
    }

    // For loading the data of what is inside the block when the game is loaded.
    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        itemHandler.deserializeNBT(registries, pTag.getCompound("inventory"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

        long stateTimeElapsed = level.getGameTime() - lastStateChangeTime;

        if (!pLevel.isClientSide) {
            System.out.println("penises");
        }

        switch (tableState) {
            case Normal:
                if (totalTicks == 0) {
                    soundLogicOnTick(pPos, pLevel);
                }
                totalTicks++;
                newBookAnimationTick(pLevel, pPos, pState);

                // This is so that any sound that needs to be playing when level is loaded gives player time to load.
                // There's not really an elegant way to do this but most loading takes less than 3 seconds.
                if (totalTicks < 60) {
                    return;
                }

                // Check if requirements are met currently after certain interval passed,
                // then set which book index can currently be crafted for use elsewhere.
                if (totalTicks % 4 == 0)
                {
                    soundLogicOnTick(pPos, pLevel);
                }
                break;
            case GemPending:
                System.out.println(tableState.toString());

                if (stateTimeElapsed >= GEM_PENDING_ANIMATION_LENGTH_TICKS) {
                    completePendingGemAnimation();
                }
                break;
            case GemEarned:
                System.out.println(tableState.toString());

                if (stateTimeElapsed >= GEM_EARNED_ANIMATION_LENGTH_TICKS) {
                    gemEarnedToDefaultState();
                }
                break;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Enchanting Table");
    }

    public void stopAmbientSound() {
        if (level.isClientSide()) {
            UtilFunctions.tryStopAmbientSound(getBlockPos());
        }
    }

    public boolean getIsCharged() {
        return currentIndexRequirementsMet >= 0;
    }

    public Direction getLapisHolderFacingDirection() {
        return getBlockState().getValue(BlockStateProperties.FACING).getCounterClockWise();
    }

    // Starts the gem earning animation process; once this state is complete and GemEarned state
    // completes as well, the provided ItemStack will be spawned by the table.
    public void startGemPendingAnimation(ItemStack bonusGem) {
        if (tableState != CustomRechantmentTableState.Normal) return;

        tableState = CustomRechantmentTableState.GemPending;
        lastStateChangeTime = level.getGameTime();
        pendingGemItem = bonusGem.copy();

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // If state is GemPending, this will complete the animation and spawn pendingGemItem.
    // The rendered book will return to its resting position.
    public void completePendingGemAnimation() {

        if (tableState != CustomRechantmentTableState.GemPending) return;

        tableState = CustomRechantmentTableState.GemEarned;
        lastStateChangeTime = level.getGameTime();

        ItemEntity item = new ItemEntity(
            level,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + GEM_EARNED_ITEM_SPAWN_Y_OFFSET,
            worldPosition.getZ() + 0.5,
            pendingGemItem
        );
        item.setDefaultPickUpDelay();

        Direction facing = getLapisHolderFacingDirection();
        Vec3 moveDir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ()).normalize();
        moveDir = moveDir.scale(GEM_EARNED_ITEM_MOVE_SPEED_ON_SPAWN);
        item.setDeltaMovement(moveDir);

        level.addFreshEntity(item);
        level.playSound(null, getBlockPos(),SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.BLOCKS, 1.0f, 1.0f);

        pendingGemItem = null;

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // If state is GemEarned, this will return the entity back to the default state.
    public void gemEarnedToDefaultState() {
        if (tableState != CustomRechantmentTableState.GemEarned) return;

        tableState = CustomRechantmentTableState.Normal;
        lastStateChangeTime = level.getGameTime();
        pendingGemItem = null;

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }


    public void refreshCachedBlockStates(BookRarityProperties bookProperties, BlockPos pPos) {
        var reqBlockStates = getReqBlockStates(bookProperties, pPos);
        cachedBookshelvesInRange = reqBlockStates.getA();
        cachedFloorBlocksInRange = reqBlockStates.getB();
    }

    private Pair<BlockState[], BlockState[]> getReqBlockStates(BookRarityProperties bookProperties, BlockPos pPos) {

        BlockState[] shelfStates = UtilFunctions.scanAroundBlockForBookshelves(level, pPos).getA();
        BlockState[] floorStates = UtilFunctions.scanAroundBlockForValidFloors(bookProperties.floorBlock, level, pPos).getA();
        return new Pair<>(shelfStates, floorStates);
    }

    // Copy-pasted from enchantment table entity, but changing the "random" calls to prevent an IllegalStateException that can
    // occasionally occur when the original implementation uses the legacy global RANDOM.
    public void newBookAnimationTick(Level pLevel, BlockPos pPos, BlockState pState) {
        this.oOpen = this.open;
        this.oRot = this.rot;

        Player nearestPlayer = pLevel.getNearestPlayer((double)pPos.getX() + (double)0.5F, (double)pPos.getY() + (double)0.5F, (double)pPos.getZ() + (double)0.5F, (double)3.0F, false);
        if (this.getIsCharged()) {
            if (nearestPlayer != null) {
                double $$5 = nearestPlayer.getX() - ((double)pPos.getX() + (double)0.5F);
                double $$6 = nearestPlayer.getZ() - ((double)pPos.getZ() + (double)0.5F);
                this.tRot = (float) Mth.atan2($$6, $$5);
            }
            else {
                this.tRot += 0.02F;
            }

            this.open += 0.075F;
            if (this.open < 0.5F || pLevel.random.nextInt(40) == 0) {
                float $$7 = this.flipT;

                do {
                    this.flipT += (float)(pLevel.random.nextInt(4) - pLevel.random.nextInt(4));
                } while($$7 == this.flipT);
            }
        } else {
            this.tRot += 0.02F;
            this.open -= 0.35F;
        }

        while(this.rot >= (float)Math.PI) {
            this.rot -= ((float)Math.PI * 2F);
        }

        while(this.rot < -(float)Math.PI) {
            this.rot += ((float)Math.PI * 2F);
        }

        while(this.tRot >= (float)Math.PI) {
            this.tRot -= ((float)Math.PI * 2F);
        }

        while(this.tRot < -(float)Math.PI) {
            this.tRot += ((float)Math.PI * 2F);
        }

        float $$8;
        for($$8 = this.tRot - this.rot; $$8 >= (float)Math.PI; $$8 -= ((float)Math.PI * 2F)) {
        }

        while($$8 < -(float)Math.PI) {
            $$8 += ((float)Math.PI * 2F);
        }

        this.rot += $$8 * 0.4F;
        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        ++this.time;
        this.oFlip = this.flip;
        float $$9 = (this.flipT - this.flip) * 0.4F;
        float $$10 = 0.2F;
        $$9 = Mth.clamp($$9, -0.2F, 0.2F);
        this.flipA += ($$9 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }

    protected int checkAllRequirements(BlockPos pPos) {
        int reqMet = -1;
        for (int i = 0; i < 5; ++i) {
            BookRarityProperties properties = BookRarityProperties.getAllProperties()[i];
            refreshCachedBlockStates(properties, pPos);
            if (meetsAllChargedEffectRequirements(properties, cachedBookshelvesInRange, cachedFloorBlocksInRange)) {
                reqMet = i;
                break;
            }
        }

        return reqMet;
    }

    protected void soundLogicOnTick(BlockPos pPos, Level pLevel) {

        int prevIndexRequirementsMet = currentIndexRequirementsMet;
        currentIndexRequirementsMet = checkAllRequirements(pPos);

        // Requirements have started being met on this tick.
        if (currentIndexRequirementsMet >= 0 && prevIndexRequirementsMet == -1) {
            if (pLevel.isClientSide()) {
                if (totalTicks != 0) {
                    pLevel.playLocalSound(pPos, ModSounds.ENCHANT_TABLE_CHARGE.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
                    pLevel.playLocalSound(pPos, ModSounds.ENCHANT_TABLE_OPEN.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
                }

                UtilFunctions.createAndPlayAmbientSound(ModSounds.ENCHANT_TABLE_AMBIENT.get(), pPos, 0.5f);
            }
        }

        // Requirements no longer being met on this tick.
        else if (currentIndexRequirementsMet == -1 && prevIndexRequirementsMet != -1) {
            if (totalTicks != 0) {
                if (pLevel.isClientSide()) {
                    pLevel.playLocalSound(pPos, ModSounds.ENCHANT_TABLE_DISCHARGE.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
                    pLevel.playLocalSound(pPos, ModSounds.ENCHANT_TABLE_CLOSE.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
                    stopAmbientSound();
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        stopAmbientSound();
    }

    protected boolean bookshelfRequirementsMet (BookRarityProperties bookProperties, BlockState[] shelfStates) {
        return UtilFunctions.playerMeetsBookshelfRequirement(bookProperties, shelfStates);
    }

    protected boolean floorRequirementsMet(BookRarityProperties bookProperties, BlockState[] floorStates) {
        return UtilFunctions.playerMeetsFloorRequirement(bookProperties, floorStates);
    }

    protected boolean meetsAllChargedEffectRequirements(BookRarityProperties bookProperties, BlockState[] shelfStates, BlockState[] floorStates) {
        return  bookshelfRequirementsMet(bookProperties, shelfStates) &&
                floorRequirementsMet(bookProperties, floorStates);
    }
}
