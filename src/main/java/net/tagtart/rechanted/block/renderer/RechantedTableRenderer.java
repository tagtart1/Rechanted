package net.tagtart.rechanted.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.util.AnimHelper;
import net.tagtart.rechanted.util.UtilFunctions;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RechantedTableRenderer implements BlockEntityRenderer<RechantedTableBlockEntity> {

    public static final Material BOOK_LOCATION = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.withDefaultNamespace("entity/enabled_enchanting_table_book"));

    public static final int MAX_LAPIS_ITEMS_TO_RENDER = 10;
    public static final float LAPIS_HOLDER_LENGTH = 1.4f;
    public static final float LAPIS_HOLDER_PADDING = 0.05f;

    public static final ItemStack staticRenderItemStack = new ItemStack(Items.LAPIS_LAZULI);
    public static final Vec3 UP = new Vec3(0, 1, 0); // Whole ass production level Vec3 class but no constants for
                                                     // directions smh.
    public static final Vec3 NORTH = new Vec3(0, 0, 1);

    private final BookModel bookModel;

    // --- GEM PENDING KEYFRAMES ---
    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_PENDING_Y_TRANSLATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0f, AnimHelper::easeOutBack),
                    new AnimHelper.FloatKeyframe(110f, 1.15f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_PENDING_Y_ROTATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0f, AnimHelper::easeInOutQuad),
                    new AnimHelper.FloatKeyframe(100.0f, (float) Math.PI * 20.2f, AnimHelper::easeInOutBack),
                    new AnimHelper.FloatKeyframe(110.0f, (float) Math.PI * 20.0f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_PENDING_Z_DEG_ROTATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, -70f, AnimHelper::linear),
                    new AnimHelper.FloatKeyframe(45.0f, 0f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_PENDING_BOOK_OPEN_KEYFRAMES = new ArrayList<>(List.of(
            new AnimHelper.FloatKeyframe(0f, 1.0f, AnimHelper::linear),
            new AnimHelper.FloatKeyframe(115.0f, 1.0f, AnimHelper::easeInBack),
            new AnimHelper.FloatKeyframe(129.0f, 0.0f, AnimHelper::easeOutBack)));

    public static final ArrayList<AnimHelper.FloatKeyframe> SUPER_BONUS_PENDING_Z_DEG_ROTATION_KEYFRAMES = new ArrayList<>(List.of(
            new AnimHelper.FloatKeyframe(0f, -70f, AnimHelper::linear),
            new AnimHelper.FloatKeyframe(45.0f, 0f, AnimHelper::easeInOutQuad),
            new AnimHelper.FloatKeyframe(110.0f, -360f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> SUPER_BONUS_PENDING_X_DEG_ROTATION_KEYFRAMES = new ArrayList<>(List.of(
            new AnimHelper.FloatKeyframe(0f, 0f, AnimHelper::linear),
            new AnimHelper.FloatKeyframe(20.0f, 0f, AnimHelper::easeInOutQuad),
            new AnimHelper.FloatKeyframe(55.0f, -360f, AnimHelper::linear)));

    // --- GEM EARNED KEYFRAMES ---
    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_EARNED_Y_TRANSLATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(8f, BONUS_PENDING_Y_TRANSLATION_KEYFRAMES.getLast().value,
                            AnimHelper::easeInBack), // So it starts at same y-offset last anim. ended.
                    new AnimHelper.FloatKeyframe(21.0f, 0f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_EARNED_BOOK_OPEN_KEYFRAMES = new ArrayList<>(List.of(
            new AnimHelper.FloatKeyframe(0f, 0.0f, AnimHelper::easeOutBack),
            new AnimHelper.FloatKeyframe(8.0f, 1.0f, AnimHelper::linear),
            new AnimHelper.FloatKeyframe(17.0f, 0.4f, AnimHelper::linear),
            new AnimHelper.FloatKeyframe(20.0f, 1.0f, AnimHelper::easeInBack)));

    public static final ArrayList<AnimHelper.FloatKeyframe> BONUS_EARNED_Z_DEG_ROTATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0f, AnimHelper::easeInOutQuad),
                    new AnimHelper.FloatKeyframe(20.0f, -60f, AnimHelper::linear)));

    public static final ArrayList<AnimHelper.FloatKeyframe> LIGHT_BONUS_PENDING_BOOK_OPEN_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 1.0f, AnimHelper::linear),
                    new AnimHelper.FloatKeyframe(25.0f, 0.0f, AnimHelper::easeOutBack)));

    public static final ArrayList<AnimHelper.FloatKeyframe> LIGHT_BONUS_PENDING_BOOK_Y_DEG_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0.0f, AnimHelper::easeInOutBack),
                    new AnimHelper.FloatKeyframe(72.0f, (float)Math.PI * 2.0f, AnimHelper::easeOutBack)));

    public static final ArrayList<AnimHelper.FloatKeyframe> LIGHT_BONUS_EARNED_BOOK_OPEN_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0.0f, AnimHelper::easeOutBack),
                    new AnimHelper.FloatKeyframe(8.0f, 1.0f, AnimHelper::linear),
                    new AnimHelper.FloatKeyframe(16.0f, 1.0f, AnimHelper::easeInBack)));

    public static final ArrayList<AnimHelper.FloatKeyframe> SUPER_BONUS_EARNED_Z_DEG_ROTATION_KEYFRAMES = new ArrayList<>(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 360f, AnimHelper::easeInOutQuad),
                    new AnimHelper.FloatKeyframe(14.0f, -60f, AnimHelper::linear)));


    public static final ArrayList<AnimHelper.FloatKeyframe> SUPER_BONUS_EARNED_X_DEG_ROTATION_KEYFRAMES = new ArrayList<>(List.of(
            new AnimHelper.FloatKeyframe(0f, -360f, AnimHelper::easeOutBack),
            new AnimHelper.FloatKeyframe(6.0f, 0F, AnimHelper::easeInOutQuad)));

    public RechantedTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    public void render(RechantedTableBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderBook(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        renderLapisItems(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderBook(RechantedTableBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        long gameTime = Minecraft.getInstance().level.getGameTime();

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.Normal) {

            float time = (float) blockEntity.time + partialTick;
            poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);
            float yRotationBase = blockEntity.rot - blockEntity.oRot;

            while (yRotationBase >= (float) Math.PI) {
                yRotationBase -= (float) (Math.PI * 2);
            }

            while (yRotationBase < (float) -Math.PI) {
                yRotationBase += (float) (Math.PI * 2);
            }

            float yRotation = (blockEntity.oRot + yRotationBase * partialTick) + 3.14f;
            poseStack.mulPose(Axis.YP.rotation(-yRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(100.0F));
            float f3 = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
            float rightPageFlipAmount = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
            float leftPageFlipAmount = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
            float f6 = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
            this.bookModel.setupAnim(time, Mth.clamp(rightPageFlipAmount, 0.0F, 1.0F),
                    Mth.clamp(leftPageFlipAmount, 0.0F, 1.0F), f6);
        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.BonusPending) {

            long stateStartTime = gameTime - (RechantedTableBlockEntity.BONUS_PENDING_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float yOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_Y_TRANSLATION_KEYFRAMES, time);
            float yRotationOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_Y_ROTATION_KEYFRAMES, time);
            float zRotationOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_Z_DEG_ROTATION_KEYFRAMES, time);
            float bookOpenOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_BOOK_OPEN_KEYFRAMES, time);
            float facingRotation = getCorrectBookFacingYRotation(blockEntity);

            // System.out.println(yOffset);

            poseStack.translate(0f, yOffset, 0f);
            poseStack.mulPose(Axis.YP.rotation(facingRotation + yRotationOffset));
            poseStack.mulPose(Axis.ZP.rotationDegrees(170.0F + zRotationOffset));

            this.bookModel.setupAnim(time, 0, 0, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.BonusEarned) {

            long stateStartTime = gameTime - (RechantedTableBlockEntity.BONUS_EARNED_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float yOffset = AnimHelper.evaluateKeyframes(BONUS_EARNED_Y_TRANSLATION_KEYFRAMES, time);
            float zRotationOffset = AnimHelper.evaluateKeyframes(BONUS_EARNED_Z_DEG_ROTATION_KEYFRAMES, time);
            float bookOpenOffset = AnimHelper.evaluateKeyframes(BONUS_EARNED_BOOK_OPEN_KEYFRAMES, time);
            float facingRotation = getCorrectBookFacingYRotation(blockEntity);

            poseStack.translate(0f, yOffset, 0f);
            poseStack.mulPose(Axis.YP.rotation(facingRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(170F + zRotationOffset));

            this.bookModel.setupAnim(time, 0f, 0f, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.LightBonusPending) {
            long stateStartTime = gameTime - (RechantedTableBlockEntity.LIGHT_BONUS_PENDING_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float bookOpenOffset = AnimHelper.evaluateKeyframes(LIGHT_BONUS_PENDING_BOOK_OPEN_KEYFRAMES, time);
            float facingRotationOffset = AnimHelper.evaluateKeyframes(LIGHT_BONUS_PENDING_BOOK_Y_DEG_KEYFRAMES, time);

            float startFacingRotation = getCorrectBookFacingYRotation(blockEntity);
            float yRotation = startFacingRotation + facingRotationOffset;

            poseStack.translate(0f, 0.1f, 0f);
            poseStack.mulPose(Axis.YP.rotation(yRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(100.0F));

            this.bookModel.setupAnim(time, 0, 0, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.LightBonusEarned) {
            long stateStartTime = gameTime - (RechantedTableBlockEntity.LIGHT_BONUS_EARNED_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float bookOpenOffset = AnimHelper.evaluateKeyframes(LIGHT_BONUS_EARNED_BOOK_OPEN_KEYFRAMES, time);
            float facingRotation = getCorrectBookFacingYRotation(blockEntity);

            poseStack.translate(0f, 0.1f, 0f);
            poseStack.mulPose(Axis.YP.rotation(facingRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(100.0F));

            this.bookModel.setupAnim(time, 0, 0, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);

        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.SuperBonusPending) {
            long stateStartTime = gameTime - (RechantedTableBlockEntity.SUPER_BONUS_PENDING_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float yOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_Y_TRANSLATION_KEYFRAMES, time);
            float yRotationOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_Y_ROTATION_KEYFRAMES, time);
            float zRotationOffset = AnimHelper.evaluateKeyframes(SUPER_BONUS_PENDING_Z_DEG_ROTATION_KEYFRAMES, time);
            float xRotationOffset = AnimHelper.evaluateKeyframes(SUPER_BONUS_PENDING_X_DEG_ROTATION_KEYFRAMES, time);

            float bookOpenOffset = AnimHelper.evaluateKeyframes(BONUS_PENDING_BOOK_OPEN_KEYFRAMES, time);
            float facingRotation = getCorrectBookFacingYRotation(blockEntity);

            poseStack.translate(0f, yOffset, 0f);
            poseStack.mulPose(Axis.YP.rotation(facingRotation + yRotationOffset));
            poseStack.mulPose(Axis.ZP.rotationDegrees(170.0F + zRotationOffset));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRotationOffset));

            this.bookModel.setupAnim(time, 0, 0, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        }

        if (blockEntity.tableState == RechantedTableBlockEntity.CustomRechantedTableState.SuperBonusEarned) {
            long stateStartTime = gameTime - (RechantedTableBlockEntity.SUPER_BONUS_EARNED_ANIMATION_LENGTH_TICKS
                    - blockEntity.currentStateTimeRemaining);
            float time = (gameTime + partialTick - stateStartTime);

            float yOffset = AnimHelper.evaluateKeyframes(BONUS_EARNED_Y_TRANSLATION_KEYFRAMES, time);
            float zRotationOffset = AnimHelper.evaluateKeyframes(SUPER_BONUS_EARNED_Z_DEG_ROTATION_KEYFRAMES, time);
            float xRotationOffset = AnimHelper.evaluateKeyframes(SUPER_BONUS_EARNED_X_DEG_ROTATION_KEYFRAMES, time);
            float bookOpenOffset = AnimHelper.evaluateKeyframes(BONUS_EARNED_BOOK_OPEN_KEYFRAMES, time);
            float facingRotation = getCorrectBookFacingYRotation(blockEntity);

            poseStack.translate(0f, yOffset, 0f);
            poseStack.mulPose(Axis.YP.rotation(facingRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(170F + zRotationOffset));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRotationOffset));

            this.bookModel.setupAnim(time, 0f, 0f, bookOpenOffset);
            VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
            this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);

        }

        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    // DID ALL THIS MATH OFF THE DOME NO GOOGLE OR AI RAHHHHHH!!!!
    private void renderLapisItems(RechantedTableBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Level level = blockEntity.getLevel();

        ItemStack lapisStack = blockEntity.getItemHandlerLapisStack();
        if (lapisStack.is(Items.AIR))
            return;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);

        // Each multiple of these renders an additional lapis
        float itemsPerRender = lapisStack.getMaxStackSize() / (float) MAX_LAPIS_ITEMS_TO_RENDER;
        int lapisToRender = Math.min(
                Math.round(0.5f + ((float) blockEntity.getItemHandlerLapisStack().getCount() / itemsPerRender)),
                MAX_LAPIS_ITEMS_TO_RENDER);

        Direction facing = blockEntity.getLapisHolderFacingDirection();
        Vec3 facingDir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

        // Adjusts distance from block center towards facing dir of base lapis pos.
        Vec3 facingToLapisHolder = facingDir.multiply(0.45f, 1.0f, 0.45f);

        // Create direction along the side of block; should go to the player's RIGHT if
        // looking at lapis holder.
        Vec3 toBasePos = facingToLapisHolder.cross(UP).normalize();

        // Adjusts how far from center, in direction of toBasePos, the final base
        // position will be.
        Vec3 baseRenderPos = facingToLapisHolder.add(toBasePos.multiply(0.17, 1.0f, 0.17));
        poseStack.translate(baseRenderPos.x, baseRenderPos.y + 0.1, baseRenderPos.z);

        // Resize arbitrarily for looks, rotate correctly based on facing dir (rotation
        // is later so that it doesn't affect translation).
        poseStack.scale(0.25f, 0.25f, 0.25f);
        float facingRotation = facingDir.toVector3f().angleSigned(NORTH.toVector3f(), UP.toVector3f());
        facingRotation += (float) (Math.PI * 0.5f);
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            facingRotation += (float) (Math.PI);
        }

        // Fills in opposite direction used to get towards base position early
        Vec3 toFillItemDir = toBasePos.multiply(-1, -1, -1);

        // Space between each lapis item
        Vec3 toFillPadding = toFillItemDir.multiply(LAPIS_HOLDER_PADDING, LAPIS_HOLDER_PADDING, LAPIS_HOLDER_PADDING);

        for (int i = 0; i < lapisToRender; ++i) {
            poseStack.pushPose();

            double lapisFrac = (double) i / MAX_LAPIS_ITEMS_TO_RENDER;
            double x = (toFillItemDir.x * LAPIS_HOLDER_LENGTH * lapisFrac) + toFillPadding.x;
            double y = (toFillItemDir.y * LAPIS_HOLDER_LENGTH * lapisFrac) + toFillPadding.y;
            double z = (toFillItemDir.z * LAPIS_HOLDER_LENGTH * lapisFrac) + toFillPadding.z;
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotation(facingRotation));
            poseStack.scale(0.8f, 0.8f, 0.8f);

            itemRenderer.renderStatic(staticRenderItemStack, ItemDisplayContext.FIXED, getLightLevel(level,
                    blockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 1);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    // thx kaupenjoe
    private static int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }

    private static float getVanillaBookFacingYRotation(RechantedTableBlockEntity be, float partialTick) {
        float yRotationBase = be.rot - be.oRot;

        yRotationBase = wrapRadians(yRotationBase);

        float yRotation = (be.oRot + yRotationBase * partialTick) + 3.14f;
        return -yRotation;
    }

    private static float wrapRadians(float angle) {
        while (angle >= (float) Math.PI) {
            angle -= (float) (Math.PI * 2);
        }

        while (angle < (float) -Math.PI) {
            angle += (float) (Math.PI * 2);
        }

        return angle;
    }

    private static float getCorrectBookFacingYRotation(RechantedTableBlockEntity be) {
        Direction facing = be.getLapisHolderFacingDirection();
        Vec3 facingDir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

        float facingRotation = facingDir.toVector3f().angleSigned(NORTH.toVector3f(), UP.toVector3f());
        facingRotation -= (float) (Math.PI * 0.5f);
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            facingRotation += (float) (Math.PI);
        }

        return facingRotation;
    }

    // Same as enchantment table still; copy-pasted
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(RechantedTableBlockEntity blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1., pos.getY() + 1.5,
                pos.getZ() + 1.);
    }
}
