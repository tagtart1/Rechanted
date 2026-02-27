package net.tagtart.rechanted.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.tagtart.rechanted.item.ModItems;
import net.tagtart.rechanted.util.UtilFunctions;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class HoverableGuiRenderable implements Renderable {


    public record AnimatedTextureData(int textureWidth, int textureHeight, int ticksPerFrame, int frameCount, boolean verticalSheetDir) {

    }

    protected ResourceLocation renderTexture;
    protected AnimatedTextureData animatedTextureData = null;

    // Item icons are 16x16 by default, but these can be set to any
    // texture size. Entire image will render by default.
    protected int imageWidth = 16;
    protected int imageHeight = 16;

    protected int imageViewWidth;
    protected int imageViewHeight;

    // Offset positions relative to parent gui origin.
    protected int renderOffsetPosX;
    protected int renderOffsetPosY;

    // UV offset for rendering
    protected int renderUVOffsetU = 0;
    protected int renderUVOffsetV = 0;

    protected float timeElapsed = 0f;

    // A uniform scale factor in all axes
    public float scaleFac = 1.0f;

    protected ArrayList<Component> customTooltipLines;
    protected boolean hoveredLastFrame  = false;
    protected boolean hoveredThisFrame  = false;
    protected boolean leftMouseClicked  = false;    // True if left mouse is currently clicked and is on top of renderable.
    protected boolean rightMouseClicked = false;    // True if right mouse is currently clicked and is on top of renderable.

    public Runnable onHoverStartEvent;
    public Runnable onHoverEndEvent;

    // Events/Function to run to mouse is clicked/released while on top of this renderable.
    // Double: pMouseX, Double: pMouseY, Integer: pMouseButton
    public TriConsumer<Double, Double, Integer> onClickMouseEvent;
    public TriConsumer<Double, Double, Integer> onReleaseMouseEvent;

    protected boolean renderDefaultTexture = true;

    public HoverableGuiRenderable(ResourceLocation textureResource, int posX, int posY) {
        renderOffsetPosX = posX;
        renderOffsetPosY = posY;
        imageViewWidth = imageWidth;
        imageViewHeight = imageHeight;
        renderTexture = textureResource;

        customTooltipLines = new ArrayList<>();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        timeElapsed += delta;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scaleFac, scaleFac, scaleFac);

        if (renderDefaultTexture) {
            if (animatedTextureData != null) {

                float animationTick = timeElapsed;
                int frame = (int)(animationTick / animatedTextureData.ticksPerFrame) % animatedTextureData.frameCount;

                boolean isVertical = animatedTextureData.verticalSheetDir;
                int frameWidth = (isVertical) ? animatedTextureData.textureWidth : animatedTextureData.textureWidth / animatedTextureData.frameCount;
                int frameHeight = (!isVertical) ? animatedTextureData.textureHeight : animatedTextureData.textureHeight / animatedTextureData.frameCount;
                int frameU = (isVertical) ? 0 : frame * frameWidth;
                int frameV = (!isVertical) ? 0 : frame * frameHeight;

//                guiGraphics.blit(
//                        renderTexture,
//                        renderOffsetPosX,
//                        renderOffsetPosY,
//                        renderUVOffsetU + frameU,
//                        renderUVOffsetV + frameV,
//                        animatedTextureData.frameWidth,
//                        animatedTextureData.frameHeight,
//                        animatedTextureData.textureWidth,
//                        animatedTextureData.textureHeight
//                );
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, renderTexture);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                float frameUVSizeU = ((float)frameWidth / animatedTextureData.textureWidth);
                float frameUVSizeV = ((float)frameHeight / animatedTextureData.textureHeight);
                float trueU = renderUVOffsetU + ((float)frameU / (float)animatedTextureData.textureWidth);
                float trueV = renderUVOffsetV + ((float)frameV / (float)animatedTextureData.textureHeight);

                UtilFunctions.fakeInnerBlit(null, renderOffsetPosX, renderOffsetPosX + frameWidth,
                        renderOffsetPosY, renderOffsetPosY + frameHeight,
                        0,
                        trueU, trueU + frameUVSizeU,
                        trueV, trueV + frameUVSizeV);
            }
            else {
                guiGraphics.blit(renderTexture, renderOffsetPosX, renderOffsetPosY, renderUVOffsetU, renderUVOffsetV, imageViewWidth, imageViewHeight, imageWidth, imageHeight);
            }
        }

        guiGraphics.pose().popPose();

        hoveredThisFrame = isMouseOverlapped(mouseX, mouseY);
        if (hoveredThisFrame) {
            if (!hoveredLastFrame) {
                onHoverStart();
            }
        }
        else {
            if (hoveredLastFrame) {
                onHoverEnd();
            }
        }

        hoveredLastFrame = hoveredThisFrame;
    }

    public void setAnimatedData(AnimatedTextureData data) {
        if (data == null) {
            animatedTextureData = null;
            return;
        }

        animatedTextureData = data;
    }

    protected void onHoverStart() {
        if (onHoverStartEvent != null)
            onHoverStartEvent.run();
    }

    protected void onHoverEnd() {
        if (onHoverEndEvent != null)
            onHoverEndEvent.run();
    }

    protected void onClickMouse(double pMouseX, double pMouseY, int pButton) {
        if (onClickMouseEvent != null)
            onClickMouseEvent.accept(pMouseX, pMouseY, pButton);
    }

    protected void onReleaseMouse(double pMouseX, double pMouseY, int pButton) {
        if (onReleaseMouseEvent != null)
            onReleaseMouseEvent.accept(pMouseX, pMouseY, pButton);
    }

    private void clickMouse(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && !leftMouseClicked) {
            leftMouseClicked = true;
        }
        if (pButton == 1 && !rightMouseClicked) {
            rightMouseClicked = true;
        }

        onClickMouse(pMouseX, pMouseY, pButton);
    }

    private void releaseMouse(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && leftMouseClicked) {
            leftMouseClicked = false;
        }
        if (pButton == 1 && rightMouseClicked) {
            rightMouseClicked = false;
        }

        onReleaseMouse(pMouseX, pMouseY, pButton);
    }


    public boolean tryClickMouse(double pMouseX, double pMouseY, int pButton) {
        if (isMouseOverlapped((int)Math.round(pMouseX), (int)Math.round(pMouseY))) {
            clickMouse(pMouseX, pMouseY, pButton);
            return true;
        }

        return false;
    }

    public boolean tryReleaseMouse(double pMouseX, double pMouseY, int pButton) {
        if ((pButton == 0 && leftMouseClicked) || (pButton == 1 && rightMouseClicked)) {
            releaseMouse(pMouseX, pMouseY, pButton);
            return true;
        }

        return false;
    }

    // Uses point intersection with 2D AABB to determine if mouse is over the renderable.
    public boolean isMouseOverlapped(int mouseX, int mouseY) {
        float minX = renderOffsetPosX * scaleFac;
        float minY = renderOffsetPosY * scaleFac;
        float maxX = (renderOffsetPosX + imageViewHeight) * scaleFac;
        float maxY = (renderOffsetPosY + imageViewWidth) * scaleFac;

        return minX <= mouseX && maxX >= mouseX && minY <= mouseY && maxY >= mouseY;
    }

}

