package net.tagtart.rechantment.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

public class HoverableWithTooltipGuiRenderable extends HoverableGuiRenderable {

    public Supplier<ArrayList<Component>> tooltipSupplier; // So that a screen can supply a custom tooltip and change how it does that.
    public boolean tooltipEnabled = true;

    public HoverableWithTooltipGuiRenderable(Supplier<ArrayList<Component>> pTooltipSupplier, ResourceLocation pResourceLocation, int posX, int posY) {
        super(pResourceLocation, posX, posY);
        tooltipSupplier = pTooltipSupplier;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        if (hoveredThisFrame && tooltipEnabled) {
            renderCustomTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void onHoverStart() {
        super.onHoverStart();
        updateTooltipLines();
    }

    @Override
    protected void onHoverEnd() {
        super.onHoverEnd();
        customTooltipLines.clear();
    }

    public void updateTooltipLines() {
        // If we just hovered over this object, create tooltip info (ensure it's updated if info, like exp, changed at any point)
        customTooltipLines = tooltipSupplier.get();
    }

    public void renderCustomTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!customTooltipLines.isEmpty()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, customTooltipLines, Optional.empty(), mouseX, mouseY);
        }
    }
}
