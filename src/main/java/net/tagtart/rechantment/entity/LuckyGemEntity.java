package net.tagtart.rechantment.entity;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.item.ModItems;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class LuckyGemEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData
            .defineId(LuckyGemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final String OWNER_UUID_TAG = "OwnerUUID";
    private double tx;
    private double ty;
    private double tz;
    private static final double HORIZONTAL_RANGE = 10.0D;
    private static final double VERTICAL_RANGE = 5.0D;
    private static final int RISE_DURATION_TICKS = 15 * 2;
    private static final int PAUSE_DURATION_TICKS = 15;
    private static final int VERTICAL_RISE_DURATION_TICKS = 15;
    private static final int PAUSE_PHASE_END_TICK = RISE_DURATION_TICKS + PAUSE_DURATION_TICKS;
    private static final int DIVE_PHASE_START_TICK = PAUSE_PHASE_END_TICK + VERTICAL_RISE_DURATION_TICKS;
    private static final double VERTICAL_RISE_Y_VELOCITY = 0.10D;
    private static final double RISE_START_Y_BLEND = 0.07D;
    private static final double RISE_END_Y_BLEND = 0.22D;
    private static final double RISE_END_ACCEL_POWER = 1.35D;
    private static final double RISE_TARGET_Y_SCALE = 0.20D;
    private static final double RISE_TARGET_Y_VELOCITY_CAP = 1.0D;
    private static final double DIVE_VERTICAL_VELOCITY = -0.9D;
    private static final int VERTICAL_RISE_NOTE_INTERVAL_TICKS = 3;
    private static final float VERTICAL_RISE_NOTE_START_PITCH = 0.90F;
    private static final float VERTICAL_RISE_NOTE_PITCH_STEP = 0.12F;
    private static final float VERTICAL_RISE_NOTE_MAX_PITCH = 2.00F;
    public static final String POP_REWARD_TRAIL_ACTIVE_KEY = "rechantment_pop_reward_trail_active";
    public static final String POP_REWARD_TRAIL_IS_GEM_KEY = "rechantment_pop_reward_trail_is_gem";
    private static final DustParticleOptions TRAIL_GREEN_DUST = new DustParticleOptions(
            new Vector3f(0.25F, 0.92F, 0.20F), 1.0F);
    private static final DustParticleOptions TRAIL_YELLOW_DUST = new DustParticleOptions(
            new Vector3f(1.00F, 0.90F, 0.10F), 1.0F);
    private UUID ownerUuid;
    private int life;

    public LuckyGemEntity(EntityType<LuckyGemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LuckyGemEntity(Level level, double x, double y, double z) {
        this(ModEntities.LUCKY_GEM_ENTITY.get(), level);
        this.setPos(x, y, z);
    }

    public void setItem(ItemStack stack) {
        if (stack.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(DATA_ITEM_STACK, stack.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM_STACK, this.getDefaultItem());
    }

    /**
     * Checks if the entity is in range to render.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d0)) {
            d0 = 4.0;
        }

        d0 *= 64.0;
        return distance < d0 * d0;
    }

    public void signalInDirection(Vec3 lookDirection) {
        Vec3 horizontal = new Vec3(lookDirection.x, 0.0D, lookDirection.z);
        Vec3 direction = horizontal.lengthSqr() > 1.0E-6 ? horizontal.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        // Range to steer the thrown entity
        this.tx = this.getX() + direction.x * HORIZONTAL_RANGE;
        this.ty = this.getY() + VERTICAL_RANGE;
        this.tz = this.getZ() + direction.z * HORIZONTAL_RANGE;
        this.life = 0;
    }

    public void setOwner(Player owner) {
        this.ownerUuid = owner.getUUID();
    }

    private FlightPhase getFlightPhase() {
        if (this.life < RISE_DURATION_TICKS) {
            return FlightPhase.RISING;
        }

        if (this.life < PAUSE_PHASE_END_TICK) {
            return FlightPhase.PAUSE;
        }

        if (this.life < DIVE_PHASE_START_TICK) {
            return FlightPhase.VERTICAL_RISE;
        }

        return FlightPhase.DIVING;
    }

    private double getRiseProgress() {
        return Mth.clamp(this.life / (double) Math.max(1, RISE_DURATION_TICKS), 0.0D, 1.0D);
    }

    private BlockHitResult traceBlockHit(Vec3 from, Vec3 to) {
        HitResult hit = this.level()
                .clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) hit;
        }

        return null;
    }

    private boolean checkCeilingHIt(BlockHitResult hit) {
        return hit.getDirection() == Direction.DOWN;
    }

    private boolean shouldPopOnDiveCollision(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        return direction == Direction.UP;
    }

    private Player getOwnerPlayer() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getPlayerByUUID(this.ownerUuid);
    }

    private Vec3 computeSteeringVelocity(Vec3 currentVelocity, double targetY, double yBlend) {
        double d4 = this.tx - this.getX();
        double d5 = this.tz - this.getZ();
        float f = (float) Math.sqrt(d4 * d4 + d5 * d5);
        float f1 = (float) Mth.atan2(d5, d4);
        int remainingTicks = Math.max(1, RISE_DURATION_TICKS - this.life);
        double d6 = (double) f / (double) remainingTicks;
        double d7 = currentVelocity.y;
        double targetYVelocity = Mth.clamp((targetY - this.getY()) * RISE_TARGET_Y_SCALE, 0.0D,
                RISE_TARGET_Y_VELOCITY_CAP);
        return new Vec3(Math.cos((double) f1) * d6, Mth.lerp(yBlend, d7, targetYVelocity), Math.sin((double) f1) * d6);
    }

    private void spawnRandomFireworkExplosion() {
        if (this.level().isClientSide) {
            return;
        }

        FireworkExplosion.Shape[] shapes = FireworkExplosion.Shape.values();
        FireworkExplosion.Shape randomShape = shapes[this.random.nextInt(shapes.length)];
        int mainColor = Mth.hsvToRgb(this.random.nextFloat(), 1.0F, 1.0F);
        int fadeColor = Mth.hsvToRgb(this.random.nextFloat(), 0.85F, 1.0F);

        FireworkExplosion explosion = new FireworkExplosion(
                randomShape,
                IntList.of(mainColor),
                IntList.of(fadeColor),
                this.random.nextBoolean(),
                this.random.nextBoolean());

        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(explosion)));

        FireworkRocketEntity firework = new FireworkRocketEntity(this.level(), this.getX(), this.getY(), this.getZ(),
                fireworkStack);
        firework.setDeltaMovement(Vec3.ZERO);
        this.level().addFreshEntity(firework);
        this.level().broadcastEntityEvent(firework, (byte) 17);
        firework.discard();
    }

    private void popAndDropItem() {
        this.spawnPopRewardItems();
        this.spawnRandomFireworkExplosion();
        this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 4.0F, 1.0F);
        this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.2F, 1.0F);
        this.playSound(SoundEvents.FIREWORK_ROCKET_TWINKLE, 1.0F, 1.2F);
        this.discard();
    }

    private void spawnPopRewardItems() {
        if (this.level().isClientSide) {
            return;
        }

        for (ItemStack rewardStack : LuckyGemDropTable.rollDrops(this.random)) {
            if (rewardStack.isEmpty()) {
                continue;
            }

            ItemEntity rewardEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), rewardStack);
            double randomAngle = this.random.nextDouble() * (Math.PI * 2.0D);
            double horizontalSpeed = 0.18D + this.random.nextDouble() * 0.12D;
            double motionX = Math.cos(randomAngle) * horizontalSpeed;
            double motionY = 0.82D; // Raised arc to throw rewards about 1-2 blocks higher.
            double motionZ = Math.sin(randomAngle) * horizontalSpeed;
            rewardEntity.setDeltaMovement(motionX, motionY, motionZ);
            rewardEntity.getPersistentData().putBoolean(POP_REWARD_TRAIL_ACTIVE_KEY, true);
            boolean isGemReward = LuckyGemDropTable.isGemRewardItem(rewardStack);
            rewardEntity.getPersistentData().putBoolean(POP_REWARD_TRAIL_IS_GEM_KEY, isGemReward);
            if (isGemReward) {
                rewardEntity.setGlowingTag(true);
            }
            this.level().addFreshEntity(rewardEntity);
        }
    }

    private void spawnPhaseTrailParticles(double x, double y, double z, Vec3 velocity, FlightPhase phase) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double baseX = x - velocity.x * 0.25;
        double baseY = y - velocity.y * 0.25 - 0.5;
        double baseZ = z - velocity.z * 0.25;

        if (this.isInWater()) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE, baseX, baseY, baseZ, 4, 0.0D, 0.0D, 0.0D, 0.0D);
            return;
        }

        if (phase == FlightPhase.VERTICAL_RISE || phase == FlightPhase.PAUSE) {
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    baseX + this.random.nextDouble() * 0.4D - 0.2D,
                    baseY + 0.1D,
                    baseZ + this.random.nextDouble() * 0.4D - 0.2D,
                    2,
                    0.1D,
                    0.1D,
                    0.1D,
                    0.01D);
            return;
        }

        if (phase == FlightPhase.RISING) {
            // Stop dust particles after 90% of normal rise so the phase ends with a cleaner
            // visual.
            if (this.getRiseProgress() >= 0.90D) {
                return;
            }
        }

        serverLevel.sendParticles(
                TRAIL_GREEN_DUST,
                baseX + this.random.nextDouble() * 0.4D - 0.2D,
                baseY,
                baseZ + this.random.nextDouble() * 0.4D - 0.2D,
                1,
                0.06D,
                0.06D,
                0.06D,
                0.0D);
        serverLevel.sendParticles(
                TRAIL_YELLOW_DUST,
                baseX + this.random.nextDouble() * 0.4D - 0.2D,
                baseY,
                baseZ + this.random.nextDouble() * 0.4D - 0.2D,
                1,
                0.06D,
                0.06D,
                0.06D,
                0.0D);
    }

    private void handleConfinedSpaceFailure() {
        Player owner = this.getOwnerPlayer();
        ItemStack stack = this.getItem().copy();
        if (owner != null) {
            if (!owner.getAbilities().instabuild && !stack.isEmpty()) {
                if (!owner.getInventory().add(stack)) {
                    owner.drop(stack, false);
                }
            }

            owner.displayClientMessage(
                    Component.literal("The power should not be casted in confined spaces...")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    true);
        } else if (!stack.isEmpty()) {
            this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack));
        }

        this.discard();
    }

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    @Override
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = Math.sqrt(x * x + z * z);
            this.setYRot((float) (Mth.atan2(x, z) * 180.0F / (float) Math.PI));
            this.setXRot((float) (Mth.atan2(y, d0) * 180.0F / (float) Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    private float lerpRotation(float currentRotation, float targetRotation) {
        while (targetRotation - currentRotation < -180.0F) {
            currentRotation -= 360.0F;
        }

        while (targetRotation - currentRotation >= 180.0F) {
            currentRotation += 360.0F;
        }

        return Mth.lerp(0.2F, currentRotation, targetRotation);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 startPos = this.position();
        Vec3 vec3 = this.getDeltaMovement();
        FlightPhase phase = this.getFlightPhase();
        if (!this.level().isClientSide) {
            if (phase == FlightPhase.RISING) {
                double progress = this.getRiseProgress();
                double endRamp = Math.pow(progress, RISE_END_ACCEL_POWER);
                double yBlend = Mth.lerp(endRamp, RISE_START_Y_BLEND, RISE_END_Y_BLEND);
                vec3 = this.computeSteeringVelocity(vec3, this.ty, yBlend);
                this.setDeltaMovement(vec3);

                BlockHitResult hit = this.traceBlockHit(startPos, startPos.add(vec3));
                if (hit != null && this.checkCeilingHIt(hit)) {
                    this.handleConfinedSpaceFailure();
                    return;
                }
            } else if (phase == FlightPhase.PAUSE) {
                vec3 = Vec3.ZERO;
                this.setDeltaMovement(vec3);
            } else if (phase == FlightPhase.VERTICAL_RISE) {
                vec3 = new Vec3(0.0D, VERTICAL_RISE_Y_VELOCITY, 0.0D);
                this.setDeltaMovement(vec3);
                int verticalRiseTicks = this.life - PAUSE_PHASE_END_TICK;
                if (verticalRiseTicks >= 0 && verticalRiseTicks % VERTICAL_RISE_NOTE_INTERVAL_TICKS == 0) {
                    int noteStep = verticalRiseTicks / VERTICAL_RISE_NOTE_INTERVAL_TICKS;
                    float notePitch = Mth.clamp(
                            VERTICAL_RISE_NOTE_START_PITCH + noteStep * VERTICAL_RISE_NOTE_PITCH_STEP,
                            VERTICAL_RISE_NOTE_START_PITCH,
                            VERTICAL_RISE_NOTE_MAX_PITCH);
                    this.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.8F, notePitch);
                }
            } else {
                if (this.life == DIVE_PHASE_START_TICK) {
                    this.playSound(SoundEvents.BREEZE_WHIRL, 1.0F, 1.0F);
                }
                vec3 = new Vec3(0.0D, DIVE_VERTICAL_VELOCITY, 0.0D);
                this.setDeltaMovement(vec3);

                BlockHitResult hit = this.traceBlockHit(startPos, startPos.add(vec3));
                if (hit != null && this.shouldPopOnDiveCollision(hit)) {
                    Vec3 impact = hit.getLocation();
                    this.setPos(impact.x, impact.y, impact.z);
                    this.popAndDropItem();
                    return;
                }
            }
        }

        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        double d3 = vec3.horizontalDistance();
        this.setXRot(lerpRotation(this.xRotO, (float) (Mth.atan2(vec3.y, d3) * 180.0F / (float) Math.PI)));
        this.setYRot(lerpRotation(this.yRotO, (float) (Mth.atan2(vec3.x, vec3.z) * 180.0F / (float) Math.PI)));
        this.spawnPhaseTrailParticles(d0, d1, d2, vec3, phase);

        if (!this.level().isClientSide) {
            this.setPos(d0, d1, d2);
            this.life++;
        } else {
            this.setPosRaw(d0, d1, d2);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Item", this.getItem().save(this.registryAccess()));
        if (this.ownerUuid != null) {
            compound.putUUID(OWNER_UUID_TAG, this.ownerUuid);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Item", 10)) {
            this.setItem(
                    ItemStack.parse(this.registryAccess(), compound.getCompound("Item")).orElse(this.getDefaultItem()));
        } else {
            this.setItem(this.getDefaultItem());
        }

        if (compound.hasUUID(OWNER_UUID_TAG)) {
            this.ownerUuid = compound.getUUID(OWNER_UUID_TAG);
        } else {
            this.ownerUuid = null;
        }
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(ModItems.LUCKY_GEM.get());
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private enum FlightPhase {
        RISING,
        PAUSE,
        VERTICAL_RISE,
        DIVING
    }
}
