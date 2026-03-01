package net.tagtart.rechanted.block.renderer;

import com.jcraft.jorbis.Block;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.block.entity.RechantedTrophyBlockEntity;
import net.tagtart.rechanted.entity.ModEntityModelLayers;

public class RechantedTrophyRenderer implements BlockEntityRenderer<RechantedTrophyBlockEntity> {

    public static class TrophyBookModel extends BookModel {

        private final ModelPart ribbon;
        private final ModelPart ribbonTop;
        private final ModelPart ribbonBottom;

        public TrophyBookModel(ModelPart root) {
            super(root);
            this.ribbon = root.getChild("ribbon");
            this.ribbonTop = root.getChild("ribbon_top");
            this.ribbonBottom = root.getChild("ribbon_bottom");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            partdefinition.addOrReplaceChild(
                    "left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F)
            );
            partdefinition.addOrReplaceChild(
                    "right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F)
            );
            partdefinition.addOrReplaceChild(
                    "seam",
                    CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F),
                    PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F)
            );
            partdefinition.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
            partdefinition.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
            CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
            partdefinition.addOrReplaceChild("flip_page1", cubelistbuilder, PartPose.ZERO);
            partdefinition.addOrReplaceChild("flip_page2", cubelistbuilder, PartPose.ZERO);

            partdefinition.addOrReplaceChild("ribbon", CubeListBuilder.create().texOffs(34, 3).addBox(0.8F, -4.0F, -0.9F, 1.5F, 8.0F, 0.2F), PartPose.ZERO);
            partdefinition.addOrReplaceChild("ribbon_top", CubeListBuilder.create().texOffs(36, 3).addBox(0.8F, -4.18F, -3.45F, 1.5F, 2.0F, 0.05F), PartPose.ZERO);
            partdefinition.addOrReplaceChild("ribbon_bottom", CubeListBuilder.create().texOffs(36, 3).addBox(0.8F, 2.15F, -3.50F, 1.5F, 2.0F, 0.05F), PartPose.ZERO);

            return LayerDefinition.create(meshdefinition, 64, 32);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
            this.render(poseStack, buffer, packedLight, packedOverlay, color);
        }

        @Override
        public void setupAnim(float time, float rightPageFlipAmount, float leftPageFlipAmount, float bookOpenAmount) {
            super.setupAnim(time, rightPageFlipAmount, leftPageFlipAmount, bookOpenAmount);

            float f = (Mth.sin(time * 0.02F) * 0.1F + 1.25F) * bookOpenAmount;
            this.ribbon.yRot = -f;

            this.ribbonTop.yRot = -f;
            this.ribbonTop.xRot = (float)-Math.PI * 0.25f;

            this.ribbonBottom.yRot = -f;
            this.ribbonBottom.xRot = (float)Math.PI * 0.25f;
        }
    }


    public static final Material BOOK_LOCATION = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.withDefaultNamespace("entity/trophy_enchanting_table_book"));

    // If I ever need to hardcode these again I'll add a global constant somewhere...
    public static final Vec3 UP = new Vec3(0, 1, 0);
    public static final Vec3 NORTH = new Vec3(0, 0, 1);

    private final TrophyBookModel bookModel;

    public RechantedTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new TrophyBookModel(context.bakeLayer(ModEntityModelLayers.TROPHY_BOOK));
    }

    @Override
    public void render(RechantedTrophyBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.3F, 0.5F);
        long gameTime = Minecraft.getInstance().level.getGameTime();

        float time = (float) gameTime + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);
        float yRotationBase = getCorrectBookFacingYRotation(blockEntity);
        float yRotationOffset = Mth.lerp(partialTick, blockEntity.oldRotationOffset, blockEntity.rotationOffset);

        float yRotation = yRotationBase + 3.14f;
        poseStack.mulPose(Axis.YP.rotation(-yRotation + yRotationOffset));
        poseStack.mulPose(Axis.ZP.rotationDegrees(100.0F));
        this.bookModel.setupAnim(time, 0.0f, 0.0f, 0.9f);

        poseStack.scale(0.65f, 0.65f, 0.65f);

        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    public static float getCorrectBookFacingYRotation(RechantedTrophyBlockEntity be) {
        Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING).getCounterClockWise();
        Vec3 facingDir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

        float facingRotation = facingDir.toVector3f().angleSigned(NORTH.toVector3f(), UP.toVector3f());
        facingRotation += (float) (Math.PI * 0.5f);
//        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
//            facingRotation += (float) (Math.PI);
//        }

        return facingRotation;
    }
}
