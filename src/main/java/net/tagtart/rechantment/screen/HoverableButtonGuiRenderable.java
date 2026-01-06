package net.tagtart.rechantment.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HoverableButtonGuiRenderable extends HoverableGuiRenderable{
    public HoverableButtonGuiRenderable(ResourceLocation textureResource, int posX, int posY) {
        super(textureResource, posX, posY);
        imageWidth = 48;
        imageHeight = 16;

        imageViewWidth = 16;
        imageViewHeight = 16;

        renderUVOffsetU = 16;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Clicked and hovered
        if (leftMouseClicked) {
            renderUVOffsetU = 0;
        }

        // Hovered but not clicked
        else if (hoveredThisFrame) {
            renderUVOffsetU = 32;
        }
    }

    @Override
    protected void onHoverEnd() {
        super.onHoverEnd();
        renderUVOffsetU = 16;
    }

    @Override
    protected void onReleaseMouse(double pMouseX, double pMouseY, int pButton) {
        super.onReleaseMouse(pMouseX, pMouseY, pButton);
        renderUVOffsetU = 16;
    }
}
