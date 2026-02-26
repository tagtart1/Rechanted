package net.tagtart.rechantment.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.entity.LuckyGemEntity;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.util.AnimHelper;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static net.tagtart.rechantment.entity.LuckyGemEntity.*;

public class LuckyGemEntityRenderer extends EntityRenderer<LuckyGemEntity> {

    public static final ItemStack staticRenderItemStack = new ItemStack(ModItems.LUCKY_GEM.get());

    private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Vector3f NORTH = new Vector3f(1.0f, 0.0f, 0.0f);

    public static final ArrayList<AnimHelper.FloatKeyframe> VERTICAL_RISE_ROTATION_Y_OFFSET_KEYFRAMES = new ArrayList(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 0f, AnimHelper::easeInOutQuad),
                    new AnimHelper.FloatKeyframe(14.5f, (float)Math.PI * 1.2f, AnimHelper::linear)
            ));

    public static final ArrayList<AnimHelper.FloatKeyframe> DIVING_ROTATION_Y_OFFSET_KEYFRAMES = new ArrayList(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, (float)Math.PI * 1.2f, AnimHelper::linear),
                    new AnimHelper.FloatKeyframe(200.0f, -(float)Math.PI * 90f, AnimHelper::linear)
            ));

    public static final ArrayList<AnimHelper.FloatKeyframe> DIVING_SCALE_Y_MULT_KEYFRAMES = new ArrayList(
            List.of(
                    new AnimHelper.FloatKeyframe(0f, 1f, AnimHelper::easeInOutQuad),
                    new AnimHelper.FloatKeyframe(20.0f, 1.85f, AnimHelper::linear)
            ));

    private ItemRenderer itemRenderer;

    public LuckyGemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public ResourceLocation getTextureLocation(LuckyGemEntity entity) {
        return null;
    }

    @Override
    public void render(LuckyGemEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
            poseStack.pushPose();

            float phaseTime = 0.0f;

            Vec3 camPos = this.entityRenderDispatcher.camera.getPosition();
            Vec3 entityPos = entity.getPosition(partialTick);
            Vec3 toCam = camPos.subtract(entityPos);
            toCam = new Vec3(toCam.x, 0.0f, toCam.z).normalize();

            float yRotationBase = -toCam.toVector3f().angleSigned(NORTH, UP);

            float yRotationOffset = 0f;
            float yScaleMult = 1.0f;

            // Evaluate animation offsets based on flight phase; can't get it directly since that's server side
            // only, but we can still calculate them.
            // Vertical Rise Phase: Rotate slowly on y-axis.
            if (entity.tickCount < DIVE_PHASE_START_TICK && entity.tickCount > PAUSE_PHASE_END_TICK) {
                phaseTime = (entity.tickCount - PAUSE_PHASE_END_TICK);
                phaseTime += partialTick;

                yRotationOffset = AnimHelper.evaluateKeyframes(VERTICAL_RISE_ROTATION_Y_OFFSET_KEYFRAMES, phaseTime);
            }

            // Diving Phase: Rotate rapidly in opposite direction from vertical rise; scale in y for some
            // cool squash and stretch action.
            else if (entity.tickCount > DIVE_PHASE_START_TICK) {
                phaseTime = (entity.tickCount - DIVE_PHASE_START_TICK);
                phaseTime += partialTick;

                yRotationOffset = AnimHelper.evaluateKeyframes(DIVING_ROTATION_Y_OFFSET_KEYFRAMES, phaseTime);
                yScaleMult = AnimHelper.evaluateKeyframes(DIVING_SCALE_Y_MULT_KEYFRAMES, phaseTime);
            }

            poseStack.mulPose(Axis.YP.rotation((yRotationBase + yRotationOffset) - ((float)Math.PI * 0.5f)));
            poseStack.scale(1.0f, yScaleMult, 1.0f);

            poseStack.scale(0.8f, 0.8f, 0.8f);
            this.itemRenderer
                    .renderStatic(
                            entity.getItem(),
                            ItemDisplayContext.FIXED,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            bufferSource,
                            entity.level(),
                            entity.getId()
                    );
            poseStack.popPose();
        }
    }
}
