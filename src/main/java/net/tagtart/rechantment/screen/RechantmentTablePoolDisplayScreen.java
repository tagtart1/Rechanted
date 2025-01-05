package net.tagtart.rechantment.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Inventory;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.networking.ModPackets;
import net.tagtart.rechantment.networking.packet.OpenEnchantTableScreenC2SPacket;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;
import net.tagtart.rechantment.util.UtilFunctions;

import java.util.ArrayList;
import java.util.List;

public class RechantmentTablePoolDisplayScreen extends AbstractContainerScreen<RechantmentTablePoolDisplayMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_screen.png");

    private static final ResourceLocation LEFT_ARROW_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/arrow_button_left.png");
    private static final ResourceLocation RIGHT_ARROW_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/arrow_button_right.png");
    private static final ResourceLocation BACK_ARROW_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/arrow_button_back.png");
    private static final ResourceLocation SCROLL_BAR_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/scrollbar_rect.png");

    private static final ResourceLocation LINE_SHADER_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "shaders/program/ench_table_line_shader");
    private static final ResourceLocation SIMPLE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_effect_simple.png");
    private static final ResourceLocation UNIQUE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_effect_unique.png");
    private static final ResourceLocation ELITE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_effect_elite.png");
    private static final ResourceLocation ULTIMATE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_effect_ultimate.png");
    private static final ResourceLocation LEGENDARY_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_loot_pool_effect_legendary.png");
    private ResourceLocation[] shaderEffectsByBookID;
    private ShaderInstance lineShader;

    // Hoverables for controlling the menu.
    private ArrayList<HoverableGuiRenderable> hoverables;
    private HoverableButtonGuiRenderable leftArrow;
    private HoverableButtonGuiRenderable rightArrow;
    private HoverableButtonGuiRenderable backArrow;
    private HoverableWithTooltipGuiRenderable bookIcon;

    // All entries in the table to view.
    private ArrayList<HoverableLootTablePoolEntryRenderable> entry_hoverables;
    private static int VISIBLE_HEIGHT = 165;
    private static int VISIBLE_WIDTH = 144;
    private int entry_base_posX;
    private int entry_base_posY;

    public static final Style PINK_COLOR_STYLE = Style.EMPTY.withColor(0xFCB4B4);
    public static final Style MID_GRAY_COLOR_STYLE = Style.EMPTY.withColor(0xA8A8A8);

    private static final Component grayHyphen = Component.literal("- ").withStyle(MID_GRAY_COLOR_STYLE);
    private static final Component whiteArrow = Component.literal("→ ").withStyle(ChatFormatting.WHITE);

    private int viewingPropertyIndex = 0;
    private float timeElapsed = 0.0f;
    private double scrollPosition = 0.0f;
    private double maxScrollPosition = 0.0f;
    private int maxEntryOffset = 0;
    private boolean draggingScrollbar = false;

    private int scissorMinX;
    private int scissorMaxX;
    private int scissorMinY;
    private int scissorMaxY;

    public RechantmentTablePoolDisplayScreen(RechantmentTablePoolDisplayMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        this.viewingPropertyIndex = menu.startingPropertiesIndex;

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        this.imageWidth = 182;
        this.imageHeight = 222;

        this.leftPos = (width - imageWidth) / 2;
        this.topPos = (height - imageHeight) / 2;

        this.entry_base_posX = this.leftPos + 9;
        this.entry_base_posY = this.topPos + 48;

        // Main hoverables/buttons in gui
        hoverables = new ArrayList<>();
        leftArrow = new HoverableButtonGuiRenderable(LEFT_ARROW_LOCATION, leftPos + 17, topPos + 26);
        leftArrow.onClickMouseEvent = this::onClickLeftArrowEvent;
        hoverables.add(leftArrow);

        rightArrow = new HoverableButtonGuiRenderable(RIGHT_ARROW_LOCATION,leftPos + 129, topPos + 26);
        rightArrow.onClickMouseEvent = this::onClickRightArrowEvent;
        hoverables.add(rightArrow);

        backArrow = new HoverableButtonGuiRenderable(BACK_ARROW_LOCATION, leftPos - 20, topPos);
        backArrow.onReleaseMouseEvent = this::onReleaseBackArrowEvent;
        hoverables.add(backArrow);

        bookIcon = new HoverableWithTooltipGuiRenderable(this::getBookTooltipLines, BookRarityProperties.getAllProperties()[viewingPropertyIndex].iconResourceLocation, leftPos + 73, topPos + 23);
        bookIcon.onClickMouseEvent = this::onClickBookIconEvent;
        hoverables.add(bookIcon);

        // Hoverables that represent a loot table entry.
        entry_hoverables = new ArrayList<>();
        generateTableEntries();

        this.shaderEffectsByBookID = new ResourceLocation[5];
        this.shaderEffectsByBookID[0] = SIMPLE_LINE_LOCATION;
        this.shaderEffectsByBookID[1] = UNIQUE_LINE_LOCATION;
        this.shaderEffectsByBookID[2] = ELITE_LINE_LOCATION;
        this.shaderEffectsByBookID[3] = ULTIMATE_LINE_LOCATION;
        this.shaderEffectsByBookID[4] = LEGENDARY_LINE_LOCATION;

        this.lineShader = UtilFunctions.loadShader(LINE_SHADER_LOCATION);
    }

    protected void generateTableEntries() {
        entry_hoverables.clear();
        int nextEntryBasePosY = entry_base_posY;
        for (EnchantmentPoolEntry entry : getCurrentViewingProperties().enchantmentPool) {
            HoverableLootTablePoolEntryRenderable hoverable = new HoverableLootTablePoolEntryRenderable(this, this.font, entry, viewingPropertyIndex, entry_base_posX, nextEntryBasePosY);
            nextEntryBasePosY += hoverable.getEntryLabelBottomY();
            entry_hoverables.add(hoverable);
        }
        maxEntryOffset = nextEntryBasePosY;
        maxScrollPosition = (maxEntryOffset - VISIBLE_HEIGHT) - entry_base_posY;
        scrollPosition = 0.0;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        timeElapsed += pPartialTick;

        // Main GUI texture + shader effect on top of it.
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Scroll-bar; if all content fits within visible section, display "off" version of texture
        if (maxEntryOffset - entry_base_posY < VISIBLE_HEIGHT) {
            guiGraphics.blit(SCROLL_BAR_LOCATION, this.leftPos + 163, this.topPos + 46, 12, 0, 12, 15, 24, 15);
        }
        else {
            float scrollFrac = (float)(scrollPosition / maxScrollPosition);
            guiGraphics.blit(SCROLL_BAR_LOCATION, this.leftPos + 163, this.topPos + (int)(UtilFunctions.lerp(46, 200, scrollFrac)), 0, 0, 12, 15, 24, 15);
        }
        renderBGShaderEffect(guiGraphics);

        scissorMinX = entry_base_posX;
        scissorMinY = entry_base_posY;
        scissorMaxX = scissorMinX + VISIBLE_WIDTH;
        scissorMaxY = scissorMinY + VISIBLE_HEIGHT;

        // Render all entries within scissored view
        guiGraphics.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);
        for (HoverableLootTablePoolEntryRenderable table_entry : entry_hoverables) {
            table_entry.scrollOffset = (int)scrollPosition;
            table_entry.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        guiGraphics.disableScissor();
    }

    protected void renderBGShaderEffect(GuiGraphics guiGraphics) {
        lineShader.safeGetUniform("Time").set(timeElapsed);
        lineShader.safeGetUniform("Resolution").set((float)imageWidth, (float)imageHeight);

        RenderSystem.setShader(() -> lineShader);

        Minecraft.getInstance().getTextureManager().bindForSetup(shaderEffectsByBookID[viewingPropertyIndex]);
        lineShader.safeGetUniform("LineEffectMap").set(0);

        lineShader.apply();

        UtilFunctions.fakeInnerBlit(guiGraphics, this.leftPos, this.leftPos + imageWidth, this.topPos, this.topPos + imageHeight, 0,
                0.0f, 1.0f, 0.0f, 1.0f);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {

        renderBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, delta);

        // Render all hoverables. They will handle tooltip rendering as well.
        for (HoverableGuiRenderable hoverable : hoverables) {
            hoverable.render(guiGraphics, mouseX, mouseY, delta);
        }

        renderTooltip(guiGraphics, mouseX, mouseY);

    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        for (HoverableGuiRenderable hoverable : hoverables) {
            hoverable.tryClickMouse(pMouseX, pMouseY, pButton);
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        scrollPosition -= scrollDelta * 6.0f;
        scrollPosition = (float)UtilFunctions.clamp(scrollPosition, 0.0, maxScrollPosition);
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        if (pButton == 0 && (draggingScrollbar || isMouseWithinScrollArea(pMouseX, pMouseY))) {
            draggingScrollbar = true;
            double t = UtilFunctions.inverseLerp(0.0, maxScrollPosition, scrollPosition);
            double scrollBarLocation = UtilFunctions.lerp(46, 200, t);
            scrollBarLocation += pDragY;

            double newT = UtilFunctions.inverseLerp(46, 200, scrollBarLocation);
            scrollPosition = UtilFunctions.lerp(0.0, maxScrollPosition, newT);
            scrollPosition = (float)UtilFunctions.clamp(scrollPosition, 0.0, maxScrollPosition);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    private boolean isMouseWithinScrollArea(double mouseX, double mouseY) {
        int minX = this.leftPos + 163;
        int minY = this.topPos + 46;
        int maxX = this.leftPos + 177;
        int maxY = this.topPos + 215;

        return minX <= mouseX && maxX >= mouseX && minY <= mouseY && maxY >= mouseY;
    }

    private void onClickLeftArrowEvent(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            setViewingPropertyIndex(viewingPropertyIndex - 1);
        }
    }

    private void onClickRightArrowEvent(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            setViewingPropertyIndex(viewingPropertyIndex + 1);
        }
    }

    private void onReleaseBackArrowEvent(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && backArrow.isMouseOverlapped((int)Math.round(pMouseX), (int)Math.round(pMouseY))) {
            ModPackets.sentToServer(new OpenEnchantTableScreenC2SPacket(0, 0, menu.blockEntity.getBlockPos()));
        }
    }

    private void onClickBookIconEvent(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1 && bookIcon.isMouseOverlapped((int)Math.round(pMouseX), (int)Math.round(pMouseY))) {
            ModPackets.sentToServer(new OpenEnchantTableScreenC2SPacket(0, 0, menu.blockEntity.getBlockPos()));
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {

        for (HoverableGuiRenderable hoverable : hoverables) {
            hoverable.tryReleaseMouse(pMouseX, pMouseY, pButton);
        }

        if (pButton == 0) {
            draggingScrollbar = false;
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private ArrayList<Component> getBookTooltipLines() {
        BookRarityProperties properties = BookRarityProperties.getAllProperties()[viewingPropertyIndex];
        Style currentBookStyle = (viewingPropertyIndex == 0) ? Style.EMPTY.withColor(0xFFFFFF) : properties.colorAsStyle();

        ArrayList<Component> tooltipLines = new ArrayList<>();

        // Tooltip "title", with rarity icon and generic book name.
        Component rarityIcon = Component.translatable("enchantment.rarity." + properties.key);
        Component bookTitle = Component.translatable("item.rechantment.enchanted_book").withStyle(properties.colorAsStyle());
        tooltipLines.add(Component.literal(rarityIcon.getString()).append(" ").append(bookTitle));
        tooltipLines.add(Component.literal(" "));

        String description = Component.translatable("tooltip.rechantment.enchantment_table.enchanted_book_desc").getString();
        List<String> splitText = UtilFunctions.wrapText(description, 165);
        for (String line : splitText) {
            tooltipLines.add(Component.literal(line.trim()));
        }
        tooltipLines.add(Component.literal(" "));

        // Experience cost
        Component experienceTitle = Component.translatable("tooltip.rechantment.enchantment_table.experience_cost").withStyle(MID_GRAY_COLOR_STYLE);
        tooltipLines.add(experienceTitle);

        // Experience cost value, including number of levels to reach that amount.
        String totalExperienceLvl = String.format("%.1f", UtilFunctions.getTotalLevelFromEXP(properties.requiredExp));
        Component experienceAmt = Component.literal(properties.requiredExp + " EXP (" + totalExperienceLvl + " Levels)").withStyle(currentBookStyle);
        tooltipLines.add(grayHyphen.copy().append(experienceAmt));
        tooltipLines.add(Component.literal(" "));

        // Other requirements:
        Component requirementsTitle = Component.translatable("tooltip.rechantment.enchantment_table.requirements").append(": ").withStyle(MID_GRAY_COLOR_STYLE);
        tooltipLines.add(requirementsTitle);

        // Lapis Lazuli count requirement.
        String lapisCount = String.valueOf(properties.requiredLapis);
        String lapisName = Component.translatable("tooltip.rechantment.enchantment_table.lapis").getString();
        Component fullLapisRequirementColored = Component.literal(lapisCount + " " + lapisName).withStyle(currentBookStyle);
        tooltipLines.add(grayHyphen.copy().append(fullLapisRequirementColored));

        // Bookshelf count requirement. Green color if requirement met.
        String bookshelfCount = String.valueOf(properties.requiredBookShelves);
        String bookshelvesName = Component.translatable("tooltip.rechantment.enchantment_table.bookshelves").getString();
        Component fullBookRequirementColored = Component.literal(bookshelfCount + " " + bookshelvesName).withStyle(currentBookStyle);
        tooltipLines.add(grayHyphen.copy().append(fullBookRequirementColored));

        // Floor block requirement. Green color if requirement met.
        String floorBlockName = properties.floorBlock.getName().getString();
        String floorTranslated = Component.translatable("tooltip.rechantment.enchantment_table.floor").getString();
        Component fullFloorRequirementColored = Component.literal(floorBlockName + " " + floorTranslated).withStyle(currentBookStyle);
        tooltipLines.add(grayHyphen.copy().append(fullFloorRequirementColored));
        tooltipLines.add(Component.literal(" "));

        // Break chances:
        String chancePerBlock = Component.translatable("tooltip.rechantment.enchantment_table.chance_per").getString();
        Component breakChanceTitle = Component.translatable("tooltip.rechantment.enchantment_table.break_chance").append(":").withStyle(MID_GRAY_COLOR_STYLE);
        tooltipLines.add(breakChanceTitle);

        // Bookshelves break chance
        if (properties.bookBreakChance > 0.0001f) {
            String bookBreakChance = String.format("%.1f%%", properties.bookBreakChance * 100f);
            tooltipLines.add(Component.literal("- " + bookshelvesName + ":"));
            tooltipLines.add(Component.literal("    - " + bookBreakChance + " " + chancePerBlock).withStyle(PINK_COLOR_STYLE));
        }

        // Floor break chance
        if (properties.floorBreakChance > 0.0001f) {
            String floorBreakChance = String.format("%.1f%%", properties.floorBreakChance * 100f);
            tooltipLines.add(Component.literal("- " + floorTranslated + ":"));
            tooltipLines.add(Component.literal("    - " + floorBreakChance + " " + chancePerBlock).withStyle(PINK_COLOR_STYLE));
            tooltipLines.add(Component.literal(" "));
        }

        // Final line, show right click prompt.
        String rightClickPrompt = Component.translatable("tooltip.rechantment.enchantment_table.right_click_return").getString();
        List<String> splitTextDesc = UtilFunctions.wrapText(rightClickPrompt, 165);
        boolean firstLine = true;
        for (String line : splitTextDesc) {
            Component toAdd = Component.literal(line.trim()).withStyle(properties.colorAsStyle());
            if (firstLine)
                tooltipLines.add(whiteArrow.copy().append(toAdd));
            else
                tooltipLines.add(toAdd);

            firstLine = false;
        }

        return tooltipLines;
    }

    private void setViewingPropertyIndex(int index) {
        if (index < 0)
            index = BookRarityProperties.getAllProperties().length - 1;
        index = index % BookRarityProperties.getAllProperties().length;
        viewingPropertyIndex = index;

        bookIcon.renderTexture = BookRarityProperties.getAllProperties()[viewingPropertyIndex].iconResourceLocation;
        generateTableEntries();
    }

    private BookRarityProperties getCurrentViewingProperties() {
        return BookRarityProperties.getAllProperties()[viewingPropertyIndex];
    }

    public int getScissorMinX() {
        return scissorMinX;
    }

    public int getScissorMinY() {
        return scissorMinY;
    }

    public int getScissorMaxX() {
        return scissorMaxX;
    }

    public int getScissorMaxY() {
        return scissorMaxY;
    }
}
