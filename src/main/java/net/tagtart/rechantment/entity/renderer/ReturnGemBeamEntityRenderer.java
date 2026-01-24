package net.tagtart.rechantment.entity.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechantment.entity.ReturnGemBeamEntity;
import net.tagtart.rechantment.util.UtilFunctions;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class ReturnGemBeamEntityRenderer extends EntityRenderer<ReturnGemBeamEntity> {

    // Default beacon beam render type culls the back face of the beam, so this is just that but with culling disabled.
    // Easier to just use this than trying to render two quads with flipped normals or some shit.
    public static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM_NO_CULL = Util.memoize(
            (location, colorFlag) -> {
                RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                        .setTransparencyState(colorFlag ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false);
                return RenderType.create("beacon_beam_no_cull", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, false, true, rendertype$compositestate);
            }
    );

    public ReturnGemBeamEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ReturnGemBeamEntity entity) {
        return null;
    }

    @Override
    public void render(ReturnGemBeamEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        p_entity.glowRadius += 0.004f;
        p_entity.beamRadius += 0.004f;

        int color1 = 0xccccff;
        int color2 = 0xffffff;

        int color = FastColor.ARGB32.lerp(((float)Math.sin(p_entity.tickCount / 10.0f) + 1.0f) * 0.5f, color1, color2);

        renderBeaconBeam(poseStack, bufferSource, p_entity,partialTick, p_entity.beamRadius, p_entity.glowRadius, p_entity.level().getGameTime(), 0, 100, color);
    }


    private void renderBeaconBeam(
            PoseStack poseStack, MultiBufferSource bufferSource, ReturnGemBeamEntity entity, float partialTick, float beamRadius, float glowRadius, long gameTime, int yOffset, int height, int color
    ) {
        renderBeaconBeam(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION, entity, Minecraft.getInstance().player, partialTick, 1.0F, gameTime, yOffset, height, color, beamRadius, glowRadius);
    }

    /*
        ALL THE STUFF BELOW IS COPIED STRAIGHT FROM BeaconBeamRenderer BUT WITH VERY MINOR CHANGES SO THAT
        MIXINS WEREN'T NEEDED. BLAME MOJANG FOR IT LOOKING MESSY.
     */
    public static void renderBeaconBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation beamLocation,
            ReturnGemBeamEntity entity,
            Player player,
            float partialTick,
            float textureScale,
            long gameTime,
            int yOffset,
            int height,
            int color,
            float beamRadius,
            float glowRadius
    ) {

        UtilFunctions.translatePoseByInterpolatedPlayerPos(poseStack, player, entity, partialTick);
        poseStack.pushPose();

        int i = yOffset + height;
        float f = (float)Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -beamRadius;
        float f7 = 0.0F;
        float f8 = 0.0F;
        float f9 = -beamRadius;
        float f10 = 0.0F;
        float f11 = 1.0F;
        float f12 = -1.0F + f2;
        float f13 = (float)height * textureScale * (0.5F / beamRadius) + f12;
        renderPart(
                poseStack,
                bufferSource.getBuffer(BEACON_BEAM_NO_CULL.apply(beamLocation, true)),
                FastColor.ARGB32.color(230, color),
                yOffset,
                i,
                0.0F,
                beamRadius,
                beamRadius,
                0.0F,
                f6,
                0.0F,
                0.0F,
                f9,
                0.0F,
                1.0F,
                f13,
                f12
        );
        poseStack.popPose();
        f3 = -glowRadius;
        float f4 = -glowRadius;
        f5 = -glowRadius;
        f6 = -glowRadius;
        f10 = 0.0F;
        f11 = 1.0F;
        f12 = -1.0F + f2;
        f13 = (float)height * textureScale + f12;
        renderPart(
                poseStack,
                bufferSource.getBuffer(BEACON_BEAM_NO_CULL.apply(beamLocation, true)),
                FastColor.ARGB32.color(32, color),
                yOffset,
                i,
                f3,
                f4,
                glowRadius,
                f5,
                f6,
                glowRadius,
                glowRadius,
                glowRadius,
                0.0F,
                1.0F,
                f13,
                f12
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderPart(
            PoseStack poseStack,
            VertexConsumer consumer,
            int color,
            int minY,
            int maxY,
            float x1,
            float z1,
            float x2,
            float z2,
            float x3,
            float z3,
            float x4,
            float z4,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        PoseStack.Pose pose = poseStack.last();
        renderQuad(pose, consumer, color, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
    }

    private static void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int color,
            int minY,
            int maxY,
            float minX,
            float minZ,
            float maxX,
            float maxZ,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        addVertex(pose, consumer, color, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, color, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, color, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(
            PoseStack.Pose pose, VertexConsumer consumer, int color, int y, float x, float z, float u, float v
    ) {
        consumer.addVertex(pose, x, (float)y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
