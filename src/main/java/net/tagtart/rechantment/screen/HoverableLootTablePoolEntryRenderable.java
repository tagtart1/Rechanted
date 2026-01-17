package net.tagtart.rechantment.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.custom.RechantmentBookItem;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HoverableLootTablePoolEntryRenderable extends HoverableGuiRenderable{

    private static final ResourceLocation TABLE_ENTRY_BOX_LOCATION = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_entry_box.png");
    private static final ResourceLocation INFO_ICON_LOCATION = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "textures/gui/info_button.png");

    private static final ResourceLocation GRID_SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "shaders/program/ench_table_grid_shader");
    private ShaderInstance gridShader;

    private final int enchantmentInfoOffsetY = 13;
    private HoverableWithTooltipGuiRenderable nestedInfoHoverable;

    private int propertiesIndex;
    private RechantmentTablePoolDisplayScreen screen;
    private EnchantmentPoolEntry poolEntry;
    private Font renderFont;

    // All these strings are cached for rendering pool info:
    private Enchantment enchantment;
    private String iconList;
    private String enchantmentName;
    private String enchantmentDropRate;
    private ArrayList<String> levelDropRates;
    private int maxLevel;

    public int scrollOffset = 0;

    public static float globalTimeElapsed = 0f; // Will be relative to the screen being used.

    public HoverableLootTablePoolEntryRenderable(RechantmentTablePoolDisplayScreen pScreen, Font pFont, EnchantmentPoolEntry pPoolEntry, int pPropertiesIndex, int posX, int posY) {
        super(TABLE_ENTRY_BOX_LOCATION, posX, posY);
        propertiesIndex = pPropertiesIndex;
        screen = pScreen;
        renderFont = pFont;
        poolEntry = pPoolEntry;

        imageWidth = 144;
        imageHeight = 51;
        imageViewWidth = imageWidth;
        imageViewHeight = imageHeight;
        renderDefaultTexture = false;

        // Position of this adjusts with text
        nestedInfoHoverable = new HoverableWithTooltipGuiRenderable(this::infoIconTooltip, INFO_ICON_LOCATION, renderOffsetPosX, getEntryLabelOffsetY());
        nestedInfoHoverable.imageWidth = 9;
        nestedInfoHoverable.imageHeight = 9;
        nestedInfoHoverable.imageViewWidth = 9;
        nestedInfoHoverable.imageViewHeight = 9;
        nestedInfoHoverable.tooltipEnabled = false;  // Manually rendering tooltip later due to scissoring conflicts.

        this.gridShader = UtilFunctions.loadShader(GRID_SHADER_LOCATION);

        levelDropRates = new ArrayList<>();
        generatePoolEntryInfo();
    }

    protected ArrayList<Component> infoIconTooltip() {
        ArrayList<Component> retVal = new ArrayList<>();

        String[] enchantmentInfo = poolEntry.enchantment.split(":");
        Component translatable = Component.translatable("enchantment." + enchantmentInfo[0] + "." + enchantmentInfo[1] + ".description");
        String resolvedText = translatable.getString();
        List<String> splitText = UtilFunctions.wrapText(resolvedText, 165);
        for (String line : splitText) {
            retVal.add(Component.literal(line.trim()));
        }

        return retVal;
    }

    protected void generatePoolEntryInfo() {
        Holder.Reference<Enchantment> holder = UtilFunctions.getEnchantmentReferenceIfPresent(Minecraft.getInstance().player.registryAccess(), poolEntry.enchantment);

        if (holder == null) {
            enchantment = null;
            iconList = "";
            enchantmentName = "Invalid:Enchantment!";
            enchantmentDropRate = "-";
            levelDropRates.add("-");
            maxLevel = 0;
            return;
        }
        enchantment = holder.value();

        String nameID = enchantment.description().getString();
        enchantmentName = Component.translatable(nameID).getString();
        enchantmentDropRate = String.format("%6.2f%%", ((float)poolEntry.weight / getBookProperties().enchantmentPoolTotalWeights) * 100.0f);
        iconList = RechantmentBookItem.getApplicableIcons(holder).getString();
        for (int i = 0; i < poolEntry.levelWeights.size(); ++i) {
            levelDropRates.add(String.format("%6.2f%%", ((float)poolEntry.levelWeights.get(i) / poolEntry.levelWeightsSum) * 100.0f));
        }
        maxLevel = enchantment.getMaxLevel();
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        renderText(guiGraphics, mouseX, mouseY, delta);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(renderTexture, imageWidth, imageHeight, 0, 0, renderOffsetPosX, renderOffsetPosY - scrollOffset, imageWidth, imageHeight);

        if (gridShader != null) {
            gridShader.safeGetUniform("Time").set(globalTimeElapsed);
            gridShader.safeGetUniform("Resolution").set((float)imageWidth, (float)getEntryLabelBottomY());

            RenderSystem.setShader(() -> gridShader);

            Minecraft.getInstance().getTextureManager().bindForSetup(renderTexture);
            gridShader.safeGetUniform("MainDiffuse").set(0);

            gridShader.apply();
        }

        UtilFunctions.fakeInnerBlit(guiGraphics, renderOffsetPosX, renderOffsetPosX + imageWidth, renderOffsetPosY - scrollOffset, (renderOffsetPosY - scrollOffset) + getEntryLabelBottomY(), 0,
                0.0f, 1.0f, 0.0f, 1.0f);
    }

    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.pose().pushPose();
        float scaleFac = 0.75f;
        float invScaleFac = 1.0f / scaleFac;
        guiGraphics.pose().scale(scaleFac, scaleFac, scaleFac);

        int extraNameOffset = 0;
        if (displayShortenedVersion()) {
            extraNameOffset = 2;
        }
        guiGraphics.drawString(renderFont, enchantmentName, (renderOffsetPosX + 3) * invScaleFac, (getEntryLabelOffsetY() + extraNameOffset) * invScaleFac, 0xFFFFFF, true);
        renderFont.drawInBatch(enchantmentName, (renderOffsetPosX + 3) * invScaleFac, (getEntryLabelOffsetY() + extraNameOffset) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);

        renderFont.drawInBatch(enchantmentDropRate, (renderOffsetPosX + 114) * invScaleFac, (getEntryLabelOffsetY() + extraNameOffset) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
        if (!displayShortenedVersion()) {
            renderFont.drawInBatch("______________________________", (renderOffsetPosX + 3) * invScaleFac, (getEntryLabelOffsetY() + 2) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
            renderFont.drawInBatch("______________________________", (renderOffsetPosX + 4) * invScaleFac, (getEntryLabelOffsetY() + 2) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        renderFont.drawInBatch(iconList, (renderOffsetPosX + 2.7f) * invScaleFac, (getEntryLabelOffsetY() - 8) * invScaleFac, 0x222222, false, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
        renderFont.drawInBatch(iconList, (renderOffsetPosX + 2.2f) * invScaleFac, (getEntryLabelOffsetY() - 9) * invScaleFac, 0xFFFFFF, false, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
        guiGraphics.pose().popPose();


        scaleFac = 0.5f;
        invScaleFac = 1.0f / scaleFac;
        if (!displayShortenedVersion()) {

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scaleFac, scaleFac, scaleFac);
            for (int i = 0; i < levelDropRates.size(); ++i) {

                String roman = UtilFunctions.intToRoman(poolEntry.potentialLevels.get(i)) + ":";
                renderFont.drawInBatch(roman, (renderOffsetPosX + 5) * invScaleFac, (getEntryLabelOffsetY() + ((i + 2) * 5)) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
                renderFont.drawInBatch(levelDropRates.get(i), (renderOffsetPosX + 121) * invScaleFac, (getEntryLabelOffsetY() + ((i  + 2) * 5)) * invScaleFac, 0xFFFFFF, true, guiGraphics.pose().last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
            guiGraphics.pose().popPose();
        }
        guiGraphics.flush();

        guiGraphics.disableScissor();
        float infoIconOffset = renderFont.width(enchantmentName) * 0.75f;
        nestedInfoHoverable.scaleFac = scaleFac;
        nestedInfoHoverable.renderOffsetPosX = (int)((renderOffsetPosX + infoIconOffset + 4) * invScaleFac);
        nestedInfoHoverable.renderOffsetPosY = (int)((getEntryLabelOffsetY() + extraNameOffset) * invScaleFac);
        nestedInfoHoverable.renderCustomTooltip(guiGraphics, mouseX, mouseY);
        guiGraphics.flush();
        guiGraphics.enableScissor(screen.getScissorMinX(), screen.getScissorMinY(), screen.getScissorMaxX(), screen.getScissorMaxY());
        nestedInfoHoverable.render(guiGraphics, mouseX, mouseY, delta);
    }


    private boolean displayShortenedVersion() {
        return levelDropRates.size() <= 1 && maxLevel <= 1;
    }

    public int getEntryLabelBottomY() {
        int baseOffset = 25;// Based on position after where underlines "_______" are rendered.
        if (displayShortenedVersion())
            baseOffset -= 6;

        // Give 5 extra pixels for each potential level.
        return (baseOffset) + (levelDropRates.size() * 5);
    }

    private int getEntryLabelOffsetY() {
        return (enchantmentInfoOffsetY + renderOffsetPosY) - scrollOffset;
    }

    private BookRarityProperties getBookProperties() {
        return BookRarityProperties.getAllProperties()[propertiesIndex];
    }

}
