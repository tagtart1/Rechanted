package net.tagtart.rechantment.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.networking.ModPackets;
import net.tagtart.rechantment.networking.packet.OpenEnchantTableScreenC2SPacket;
import net.tagtart.rechantment.networking.packet.PurchaseEnchantedBookC2SPacket;
import net.tagtart.rechantment.sound.ModSounds;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.UtilFunctions;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class RechantmentTableScreen extends AbstractContainerScreen<RechantmentTableMenu> {

    private static final ResourceLocation LINE_SHADER_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "shaders/program/ench_table_line_shader");
    private static final ResourceLocation SIMPLE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_simple.png");
    private static final ResourceLocation UNIQUE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_unique.png");
    private static final ResourceLocation ELITE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_elite.png");
    private static final ResourceLocation ULTIMATE_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_ultimate.png");
    private static final ResourceLocation LEGENDARY_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_legendary.png");
    private static final ResourceLocation EMPTY_LINE_LOCATION = new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchant_table_effect_empty.png");
    private ResourceLocation[] shaderEffectsByBookID;
    private ShaderInstance lineShader;

    public static final Style PINK_COLOR_STYLE = Style.EMPTY.withColor(0xFCB4B4);
    public static final Style MID_GRAY_COLOR_STYLE = Style.EMPTY.withColor(0xA8A8A8);

    private static final Component grayHyphen = Component.literal("- ").withStyle(MID_GRAY_COLOR_STYLE);
    private static final Component whiteArrow = Component.literal("→ ").withStyle(ChatFormatting.WHITE);
    private static final Component redX = Component.literal("✘ ").withStyle(ChatFormatting.DARK_RED);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Rechantment.MOD_ID, "textures/gui/enchantment_table.png");

    private ArrayList<HoverableEnchantedBookGuiRenderable> hoverables;

    public Inventory playerInventory;

    private BlockState[] cachedBookshelvesInRange;
    private BlockState[] cachedFloorBlocksInRange;

    private float timeElapsed = 0.0f;

    private final float REQ_CHECK_RATE = 0.2f;  // How often shader will check if requirements are met to display effect.
    private float timeSinceLastReqCheck = 0.0f; // Time since last requirement check was made for shader effect.
    private int currentIndexRequirementsMet = 0; // Which book has its current requirements met; if -1, none are met.

    public RechantmentTableScreen(RechantmentTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        playerInventory = pPlayerInventory;
    }

    @Override
    protected void init() {
        super.init();

        this.cachedBookshelvesInRange = new BlockState[0];
        this.cachedFloorBlocksInRange = new BlockState[0];

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        // Why isn't there an easy to just read these from the PNG?
        this.imageWidth = 176;
        this.imageHeight = 217;

        this.leftPos = (width - imageWidth) / 2;
        this.topPos = (height - imageHeight) / 2;

        hoverables = new ArrayList<>();

        hoverables.add(new HoverableEnchantedBookGuiRenderable(() -> getEnchantTableTooltipLines(0), 0, leftPos + 10, topPos + 44));
        hoverables.add(new HoverableEnchantedBookGuiRenderable(() -> getEnchantTableTooltipLines(1),1,leftPos + 150, topPos + 44));
        hoverables.add(new HoverableEnchantedBookGuiRenderable(() -> getEnchantTableTooltipLines(2),2, leftPos + 43,  topPos + 41));
        hoverables.add(new HoverableEnchantedBookGuiRenderable(() -> getEnchantTableTooltipLines(3),3, leftPos + 116, topPos + 41));
        hoverables.add(new HoverableEnchantedBookGuiRenderable(() -> getEnchantTableTooltipLines(4),4,  leftPos + 79, topPos + 38));

        // Diffuse textures for line shader effect.
        // Indices are offset by 1 for efficiency; empty/no effect is index zero.
        this.shaderEffectsByBookID = new ResourceLocation[6];
        this.shaderEffectsByBookID[0] = EMPTY_LINE_LOCATION;
        this.shaderEffectsByBookID[1] = SIMPLE_LINE_LOCATION;
        this.shaderEffectsByBookID[2] = UNIQUE_LINE_LOCATION;
        this.shaderEffectsByBookID[3] = ELITE_LINE_LOCATION;
        this.shaderEffectsByBookID[4] = ULTIMATE_LINE_LOCATION;
        this.shaderEffectsByBookID[5] = LEGENDARY_LINE_LOCATION;

        this.lineShader = UtilFunctions.loadShader(LINE_SHADER_LOCATION);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {

        // Normal blitting of GUI is first. Custom shader effect is applied on top.
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        renderBGEffect(guiGraphics, pPartialTick, pMouseX, pMouseY);
    }

    protected void renderBGEffect(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        timeElapsed += pPartialTick;
        timeSinceLastReqCheck += pPartialTick;

        // Check if requirements are met currently after certain interval passed,
        // then set which book index can currently be crafted for use elsewhere.
        if (timeSinceLastReqCheck >= REQ_CHECK_RATE)
        {
            currentIndexRequirementsMet = 0;
            for (int i = 0; i < 5; ++i) {
                BookRarityProperties properties = BookRarityProperties.getAllProperties()[i];
                refreshCachedBlockStates(properties);
                if (playerMeetsAllEnchantRequirements(properties, cachedBookshelvesInRange, cachedFloorBlocksInRange)) {
                    currentIndexRequirementsMet = i + 1;
                    break;
                }

                // Update tooltip line if it's being hovered over currently as well
                // This will account for destroyed blocks shortly after destroyed if user is still hovering for tooltip.
                if (i < hoverables.size()) {
                    HoverableEnchantedBookGuiRenderable hoverable = hoverables.get(i);
                    if (hoverable.isMouseOverlapped(pMouseX, pMouseY)) {
                        hoverable.updateTooltipLines();
                    }
                }
            }

            timeSinceLastReqCheck = 0.0f;
        }

        lineShader.safeGetUniform("Time").set(timeElapsed);
        lineShader.safeGetUniform("Resolution").set((float)imageWidth, (float)imageHeight);

        RenderSystem.setShader(() -> lineShader);
        /*RenderSystem.activeTexture(GL_TEXTURE0);
        Minecraft.getInstance().getTextureManager().bindForSetup(TEXTURE);
        lineShader.safeGetUniform("MainDiffuse").set(0);*/

        Minecraft.getInstance().getTextureManager().bindForSetup(shaderEffectsByBookID[currentIndexRequirementsMet]);
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

        // If mouse overlaps a custom Hoverable and was left-clicked:
        if (pButton == 0) {
            Player player = playerInventory.player;
            Level level = player.level();

            for (HoverableEnchantedBookGuiRenderable hoverable : hoverables) {
                if (hoverable.tryClickMouse(pMouseX, pMouseY, pButton)) {
                    BookRarityProperties properties = BookRarityProperties.getAllProperties()[hoverable.propertiesIndex];

                    if (!floorRequirementsMet(properties, cachedFloorBlocksInRange) && !bookshelfRequirementsMet(properties, cachedBookshelvesInRange) && !lapisRequirementsMet(properties)) {
                        level.playSound(null, player.getOnPos(), ModSounds.ENCHANTED_BOOK_FAIL.get(), SoundSource.PLAYERS, 10f, 1f);
                        break;
                    }

                    if (playerInventory.getFreeSlot() == -1) {
                        // Send warning message to player, and close screen.
                        player.closeContainer();
                        level.playSound(null, player.getOnPos(), ModSounds.ENCHANTED_BOOK_FAIL.get(), SoundSource.PLAYERS, 10f, 1f);

                        Component translatedMsg = Component.translatable("message.rechantment.inventory_full").withStyle(ChatFormatting.RED);
                        player.sendSystemMessage(translatedMsg);
                        break;
                    }

                    if (!expRequirementMet(properties)) {
                        // Also send warning message to player, and close screen.
                        player.closeContainer();
                        level.playSound(null, player.getOnPos(), ModSounds.ENCHANTED_BOOK_FAIL.get(), SoundSource.PLAYERS, 10f, 1f);

                        String translatedMsg = Component.translatable("message.rechantment.insufficient_exp").getString();
                        String argsAdded = String.format(translatedMsg, player.totalExperience, properties.requiredExp);
                        player.sendSystemMessage(Component.literal(argsAdded).withStyle(ChatFormatting.RED));

                        break;
                    }

                    // At this point, they meet the requirements. Can try to send a packet to server!
                    ModPackets.sentToServer(new PurchaseEnchantedBookC2SPacket(hoverable.propertiesIndex, menu.blockEntity.getBlockPos()));
                    hoverable.updateTooltipLines();
                    return true;
                }
            }
        }
        else if (pButton == 1) {

            // On right click, open loot table display screen.
            for (HoverableEnchantedBookGuiRenderable hoverable : hoverables) {
                if (hoverable.tryClickMouse(pMouseX, pMouseY, pButton)) {

                    // Need to open from server so that menu info is based on server.
                    ModPackets.sentToServer(new OpenEnchantTableScreenC2SPacket(1, hoverable.propertiesIndex, menu.blockEntity.getBlockPos()));
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public ArrayList<Component> getEnchantTableTooltipLines(int propertiesIndex) {
        BookRarityProperties properties = BookRarityProperties.getAllProperties()[propertiesIndex];

        // VERY IMPORTANT to note. On client side, we only recompute if requirements are valid when tooltip is refreshed,
        // which only happens when a new item is initially hovered in this case. Server-side checks are in PurchaseEnchantedBookC2SPacket.
        refreshCachedBlockStates(properties);

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

        // Experience requirement
        Component experienceTitle = Component.translatable("tooltip.rechantment.enchantment_table.experience").withStyle(MID_GRAY_COLOR_STYLE);
        Component experienceFrac = Component.literal(playerInventory.player.totalExperience + "/" + properties.requiredExp + " EXP").withStyle(ChatFormatting.WHITE);
        tooltipLines.add(experienceTitle.copy().append(": ").append(experienceFrac));
        tooltipLines.add(Component.literal(" "));

        // Other requirements:
        Component requirementsTitle = Component.translatable("tooltip.rechantment.enchantment_table.requirements").append(": ").withStyle(MID_GRAY_COLOR_STYLE);
        tooltipLines.add(requirementsTitle);

        // Lapis Lazuli count requirement.
        String lapisCount = String.valueOf(properties.requiredLapis);
        String lapisName = Component.translatable("tooltip.rechantment.enchantment_table.lapis").getString();
        ChatFormatting lapisReqMetColor = lapisRequirementsMet(properties) ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component fullLapisRequirementColored = Component.literal(lapisCount + " " + lapisName).withStyle(lapisReqMetColor);
        tooltipLines.add(grayHyphen.copy().append(fullLapisRequirementColored));

        // Bookshelf count requirement. Green color if requirement met.
        String bookshelfCount = String.valueOf(properties.requiredBookShelves);
        String bookshelvesName = Component.translatable("tooltip.rechantment.enchantment_table.bookshelves").getString();
        ChatFormatting bookReqMetColor = bookshelfRequirementsMet(properties, cachedBookshelvesInRange) ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component fullBookRequirementColored = Component.literal(bookshelfCount + " " + bookshelvesName).withStyle(bookReqMetColor);
        tooltipLines.add(grayHyphen.copy().append(fullBookRequirementColored));

        // Floor block requirement. Green color if requirement met.
        String floorBlockName = properties.floorBlock.getName().getString();
        String floorTranslated = Component.translatable("tooltip.rechantment.enchantment_table.floor").getString();
        ChatFormatting floorReqMetColor = floorRequirementsMet(properties, cachedFloorBlocksInRange) ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component fullFloorRequirementColored = Component.literal(floorBlockName + " " + floorTranslated).withStyle(floorReqMetColor);
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

        // Shows left-click prompt if met, otherwise shows warning that requirements not met.
        if (playerMeetsAllEnchantRequirements(properties, cachedBookshelvesInRange, cachedFloorBlocksInRange)) {
            Component leftClickPrompt = Component.translatable("tooltip.rechantment.enchantment_table.left_click").withStyle(properties.colorAsStyle());
            tooltipLines.add(whiteArrow.copy().append(leftClickPrompt));
        }
        else {
            Component reqNotMet = Component.translatable("tooltip.rechantment.enchantment_table.requirements_not_met").withStyle(ChatFormatting.RED);
            tooltipLines.add(redX.copy().append(reqNotMet));
        }

        // Final line, show right click prompt.
        Component rightClickPrompt = Component.translatable("tooltip.rechantment.enchantment_table.right_click").withStyle(properties.colorAsStyle());
        tooltipLines.add(whiteArrow.copy().append(rightClickPrompt));

        return tooltipLines;
    }

    public void refreshCachedBlockStates(BookRarityProperties bookProperties) {
        var reqBlockStates = getReqBlockStates(bookProperties);
        cachedBookshelvesInRange = reqBlockStates.getA();
        cachedFloorBlocksInRange = reqBlockStates.getB();
    }

    private Pair<BlockState[], BlockState[]> getReqBlockStates(BookRarityProperties bookProperties) {
        Level level = playerInventory.player.level();
        BlockPos enchantTablePos = menu.blockEntity.getBlockPos();

        BlockState[] shelfStates = UtilFunctions.scanAroundBlockForBookshelves(level, enchantTablePos).getA();
        BlockState[] floorStates = UtilFunctions.scanAroundBlockForValidFloors(bookProperties.floorBlock, level, enchantTablePos).getA();
        return new Pair<>(shelfStates, floorStates);
    }

    // These extra requirement checks are kinda pointless but uh yeah whatever
    protected boolean expRequirementMet(BookRarityProperties bookProperties) {
        return UtilFunctions.playerMeetsExpRequirement(bookProperties, playerInventory.player);
    }

    protected boolean bookshelfRequirementsMet (BookRarityProperties bookProperties, BlockState[] shelfStates) {
        return UtilFunctions.playerMeetsBookshelfRequirement(bookProperties, shelfStates);
    }

    protected boolean floorRequirementsMet(BookRarityProperties bookProperties, BlockState[] floorStates) {
        return UtilFunctions.playerMeetsFloorRequirement(bookProperties, floorStates);
    }

    protected boolean lapisRequirementsMet(BookRarityProperties bookProperties) {
        return UtilFunctions.playerMeetsLapisRequirement(bookProperties, menu.getLapisSlotStack());
    }

    protected boolean playerMeetsAllEnchantRequirements(BookRarityProperties bookProperties, BlockState[] shelfStates, BlockState[] floorStates) {
        return  bookshelfRequirementsMet(bookProperties, shelfStates) &&
                expRequirementMet(bookProperties) &&
                floorRequirementsMet(bookProperties, floorStates) &&
                lapisRequirementsMet(bookProperties);
    }
}
