package net.tagtart.rechantment.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.screen.ModRenderTypes;
import net.tagtart.rechantment.screen.ModShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    // For cloned items, prevent vanilla item renderer logic. Here, we ONLY run our clone shader on each model
    // for the item, instead of it checking for enchantment glint like it does in vanilla.
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCloneEffect(
            ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel p_model, CallbackInfo ci) {

        if (itemStack.getOrDefault(ModDataComponents.IS_CLONE, false) || itemStack.is(ModItems.CLONE_GEM)) {
            poseStack.pushPose();

            // Required for item and shader to render in correct coordinate space
            p_model = net.neoforged.neoforge.client.ClientHooks.handleCameraTransforms(poseStack, p_model, displayContext, leftHand);
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            VertexConsumer vertexconsumer;
            for (var model : p_model.getRenderPasses(itemStack, true)) {
                for (var rendertype : model.getRenderTypes(itemStack, true)) {

                    ModShaders.CLONED_ITEM_SHADER.safeGetUniform("Time").set(Minecraft.getInstance().level.getGameTime());
                    vertexconsumer = VertexMultiConsumer.create(bufferSource.getBuffer(ModRenderTypes.CLONED_ITEM));

                    Minecraft.getInstance().getItemRenderer().renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexconsumer);
                }
            }

            poseStack.popPose();
            ci.cancel();
        }
    }
}
