package net.tagtart.rechantment.util;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.event.ParticleEmitter;
import net.tagtart.rechantment.networking.ModPackets;
import net.tagtart.rechantment.networking.packet.TriggerRebirthItemEffectS2CPacket;
import net.tagtart.rechantment.sound.CustomClientSoundInstanceHandler;
import net.tagtart.rechantment.sound.ModSounds;
import org.apache.logging.log4j.core.jmx.Server;
import org.joml.Matrix4f;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UtilFunctions {
    // Utility method to wrap text into lines
    public static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        Font font = Minecraft.getInstance().font; // Get the Minecraft font renderer

        for (String word : text.split(" ")) {
            // Check if adding the next word exceeds the maxWidth
            if (font.width(currentLine + word) > maxWidth) {
                lines.add(currentLine.toString()); // Add the current line to the list
                currentLine = new StringBuilder(); // Start a new line
            }
            currentLine.append(word).append(" ");
        }

        // Add the last line if it exists
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }


    public static ItemStack getItemStackFromString(String itemName) {
        ResourceLocation itemResource = new ResourceLocation(itemName);

        Item item = ForgeRegistries.ITEMS.getValue(itemResource);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }

    @Nullable
    public static BookRarityProperties getPropertiesFromEnchantment(String enchantmentRaw, int level) {
        for (BookRarityProperties bookProperties : BookRarityProperties.getAllProperties()) {
            if (bookProperties.isEnchantmentInPool(enchantmentRaw, level)) {
                return bookProperties;
            }
        }

        return null;
    }

    public static void announceDrop(String enchantmentRaw, int enchantmentLevel, ServerPlayer player, BookRarityProperties bookProperties, int successRate) {
        if (shouldAnnounceDrop(enchantmentRaw, enchantmentLevel)) {
            ServerLevel level = (ServerLevel) player.level();
            String enchantmentFormatted = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentRaw)).getFullname(enchantmentLevel).getString();
            Component announceMessage = Component.literal(player.getName().getString() + " found ")
                    .append(Component.literal(enchantmentFormatted).withStyle(Style.EMPTY.withColor(bookProperties.color)))
                    .append(" at ")
                    .append(Component.literal(successRate + "%").withStyle(Style.EMPTY.withColor(bookProperties.color)))
                    .append("!");
            for (ServerPlayer otherPlayer: level.players()) {
                otherPlayer.sendSystemMessage(announceMessage);
            }
        }
    }

    public static boolean shouldAnnounceDrop(String enchantmentRaw, int enchantmentLevel) {
        List<? extends String> enchantmentsList = RechantmentCommonConfigs.ANNOUNCEMENT_ENCHANTMENTS.get();
         for (String enchantmentRawConfig : enchantmentsList) {
             String[] parts = enchantmentRawConfig.split("\\|");
             String[] range = parts[1].split("-");


             int lowerBound = Integer.parseInt(range[0]);
             int upperBound = lowerBound;
             if (range.length > 1) {
                  upperBound = Integer.parseInt(range[1]);
             }

             if (enchantmentLevel >= lowerBound &&enchantmentLevel <= upperBound && Objects.equals(parts[0], enchantmentRaw))
             {
                 return true;
             }
         }
        return false;
    }

    // Forms a bounding box around the provided position by offsetting the corners by the provided offset values,
    // then returns an array of all block states in the level inside that bounding box. If a filter is provided,
    // only blocks matching the type provided will be returned (otherwise, all are returned). Keep in mind the offsets are absolutes,
    // as in, lowerLeft will be subtracted from given position, and upperRight will be added!
    public static Pair<BlockState[], BlockPos[]> getAllBlockInfoAroundBlock(Level level, BlockPos centerPos, Vec3i lowerLeftOffset, Vec3i upperRightOffset, @Nullable Block blockFilter) {

        Vec3i lowerLeft = centerPos.subtract(lowerLeftOffset);
        Vec3i upperRight = centerPos.offset(upperRightOffset);

        ArrayList<BlockPos> positions = new ArrayList<>();
        ArrayList<BlockState> states = new ArrayList<>();
        for (int x = lowerLeft.getX(); x <= upperRight.getX(); ++x) {
            for (int y = lowerLeft.getY(); y <= upperRight.getY(); ++y) {
                for (int z = lowerLeft.getZ(); z <= upperRight.getZ(); ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (blockFilter == null || state.is(blockFilter)) {
                        positions.add(pos);
                        states.add(state);
                    }
                }
            }
        }

        return new Pair<>(states.toArray(BlockState[]::new), positions.toArray(BlockPos[]::new));
    }

    public static boolean playerMeetsExpRequirement(BookRarityProperties bookProperties, Player player) {
        return player.totalExperience >= bookProperties.requiredExp;
    }

    // Note that is checks if the blocks match the required type for safety.
    // Recommended to pass in smaller (possibly already filtered) arrays for performance.
    public static boolean playerMeetsBookshelfRequirement(BookRarityProperties bookProperties, BlockState[] states) {
        int shelvesPresent = Arrays.stream(states).filter(blockState -> blockState.is(Blocks.BOOKSHELF)).toArray().length;
        return shelvesPresent >= bookProperties.requiredBookShelves;
    }

    // Note that is checks if the blocks match the required type for safety.
    // Recommended to pass in smaller (possibly already filtered) arrays for performance.
    public static boolean playerMeetsFloorRequirement(BookRarityProperties bookProperties, BlockState[] states) {
        int blocksPresent = Arrays.stream(states).filter(blockState -> blockState.is(bookProperties.floorBlock)).toArray().length;
        return blocksPresent >= 9;
    }

    public static boolean playerMeetsLapisRequirement(BookRarityProperties bookProperties, ItemStack inventoryItemStack) {
        return inventoryItemStack.getCount() >= bookProperties.requiredLapis;
    }

    public static Pair<BlockState[], BlockPos[]> scanAroundBlockForBookshelves(Level level, BlockPos blockPos){

        // Might read these from config instead but probably not for now?
        // Compute AABB corners for scanning level:
        Vec3i bookshelfCheckLowerLeftOffset = new Vec3i(3, 0, 3);
        Vec3i bookshelfCheckUpperRightOffset = new Vec3i(3, 2, 3);

        var bookBlockInfo = UtilFunctions.getAllBlockInfoAroundBlock(
                level,
                blockPos,
                bookshelfCheckLowerLeftOffset,
                bookshelfCheckUpperRightOffset,
                Blocks.BOOKSHELF
        );

        return bookBlockInfo;
    }

    public static Pair<BlockState[], BlockPos[]> scanAroundBlockForValidFloors(Block validBlock, Level level, BlockPos blockPos){

        Vec3i floorCheckLowerLeftOffset = new Vec3i(1, 0, 1);
        Vec3i floorCheckUpperRightOffset = new Vec3i(1, 0, 1);

         var floorBlockInfo = UtilFunctions.getAllBlockInfoAroundBlock(
                level,
                blockPos.subtract(new Vec3i(0, 1, 0)), // Checking one block under enchant table.
                floorCheckLowerLeftOffset,
                floorCheckUpperRightOffset,
                validBlock
        );

        return floorBlockInfo;
    }

    public static boolean playerMeetsAllEnchantRequirements(BookRarityProperties bookProperties, Player player, BlockState[] bookshelves, BlockState[] floorBlocks) {

        boolean expReqMet      = playerMeetsExpRequirement(bookProperties, player);
        boolean shelvesReqMet  = playerMeetsBookshelfRequirement(bookProperties, bookshelves);
        boolean floorReqMet    = playerMeetsFloorRequirement(bookProperties, floorBlocks);

        return expReqMet && shelvesReqMet && floorReqMet;
    }


    public static<T extends Enchantment> Pair<T, Integer> getEnchantmentFromItem (String enchantmentToGet, ItemStack enchantedItem, Class<T> enchantmentClass) {

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedItem);

        ResourceLocation enchantResource = new ResourceLocation(enchantmentToGet);
        Enchantment enchantmentBase = ForgeRegistries.ENCHANTMENTS.getValue(enchantResource);

        if (enchantments.containsKey(enchantmentBase)) {
            return new Pair<>(enchantmentClass.cast(enchantmentBase), enchantments.get(enchantmentBase));
        }
        return null;
    }

    // Non-recursive, 3D, Breadth-first-search through the world for a particular block type including matching ones connected to each other
    // Returns the block states and associated block positions from the provided level that match provided block tag key.
    // Not async, ideally should provide a small search limit when called so the game doesn't shit itself.
    public static BlockPos[] BFSLevelForBlocks(Level pLevel, TagKey<Block> pCheckBlock, BlockPos startPos, int searchLimit, boolean checkDiagonally) {

        HashSet<BlockPos> matchPositions = new HashSet<>();

        // BFS with queue, start at provided position, keep going until no more left to scan, or we've
        // retrieved as many as the caller wants.
        ArrayDeque<BlockPos> positionsToScan = new ArrayDeque<>();
        HashSet<BlockPos> alreadyScanned = new HashSet<>();
        positionsToScan.offer(startPos);
        while (matchPositions.size() < searchLimit && !positionsToScan.isEmpty()) {

            BlockPos scanPos = positionsToScan.poll();
            BlockState scanState = pLevel.getBlockState(scanPos);
            alreadyScanned.add(scanPos);

            if (scanState.is(pCheckBlock)) {
                matchPositions.add(scanPos);

                // Getting all positions around current one
                for (int x = scanPos.getX() - 1; x <= scanPos.getX() + 1; ++x) {
                    for (int y = scanPos.getY() - 1; y <= scanPos.getY() + 1; ++y) {
                        for (int z = scanPos.getZ() - 1; z <= scanPos.getZ() + 1; ++z) {

                            BlockPos newPos = new BlockPos(x, y, z);
                            BlockPos diffVector = scanPos.subtract(newPos);
                            int sqrDist = getSqrDistOfBlock(diffVector);

                            // Omit position we are actually scanning right now (so difference distance is zero)...
                            // ... and omit positions that already have been checked...
                            // ... also if we want to omit diagonals, skip this position if distance is greater than 1
                            // from scanPos, since adjacent blocks are always exactly 1 unit away
                            if (sqrDist == 0 || alreadyScanned.contains(newPos) || (!checkDiagonally && sqrDist > 1))
                                continue;

                            positionsToScan.add(newPos);
                        }
                    }
                }
            }
        }

        return matchPositions.toArray(BlockPos[]::new);
    }

    public static int getSqrDistOfBlock(BlockPos pos) {
        int dx = pos.getX();
        int dy = pos.getY();
        int dz = pos.getZ();

        return dx * dx + dy * dy + dz * dz;
    }

    @Nullable
    public static ShaderInstance loadShader(ResourceLocation shaderLocation) {
        try {
            return new ShaderInstance(Minecraft.getInstance().getResourceManager(), shaderLocation, DefaultVertexFormat.POSITION_TEX);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fakeInnerBlit(GuiGraphics guiGraphics, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pMaxU, pMinV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    // Derived from inverse equations for total EXP level from here:
    // https://minecraft.fandom.com/wiki/Experience#Formulas_and_Tables
    public static double getTotalLevelFromEXP(int experience) {
        if (experience < 352) {
            return Math.sqrt(experience + 9.0) - 3.0;
        }
        else if (experience < 1507) {
            return 8.1 + Math.sqrt(0.4 * (experience - 195.975));
        }
        else {
            return 18.05556 + Math.sqrt(0.22223 * (experience - 752.98611));
        }
    }

    // Mapping of Roman numerals to values.
    // Can't lie this entirely 100% copy-pasted from ChatGPT because I'm a slob
    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    public static String intToRoman(int num) {
        if (num < 1 || num > 3999) {
            throw new IllegalArgumentException("Input must be between 1 and 3999");
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (num >= ROMAN_VALUES[i]) {
                result.append(ROMAN_SYMBOLS[i]);
                num -= ROMAN_VALUES[i];
            }
        }

        return result.toString();
    }


    public static void triggerRebirthClientEffects(ServerPlayer player, ServerLevel level, ItemStack itemToActivate) {
        SimpleParticleType[] particlesArray = new SimpleParticleType[] {
                ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.FIREWORK,
                ParticleTypes.ENCHANT,
                ParticleTypes.ENCHANTED_HIT,

        };
        ParticleEmitter.emitParticlesOverTime(player, level, 100, 60, particlesArray);

        ModPackets.sentToPlayer(new TriggerRebirthItemEffectS2CPacket(itemToActivate), player);

        level.playSound(null,  player.blockPosition(), ModSounds.REBIRTH_ITEM.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
    }



    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static float inverseLerp(float a, float b, float value) {
        if (a == b) {
            return 0.0f;
        }
        return (value - a) / (b - a);
    }

    public static double inverseLerp(double a, double b, double value) {
        if (a == b) {
            return 0.0f;
        }
        return (value - a) / (b - a);
    }

    public static float lerp(float min, float max, float t) {
        return min + (max - min) * t;
    }

    public static double lerp(double min, double max, double t) {
        return min + (max - min) * t;
    }

    public static double remap(double fromMin, double fromMax, double toMin, double toMax, double value) {
        return lerp(toMin, toMax, inverseLerp(fromMin, fromMax, value));
    }

    // If EVER called on server, game will shit its pants. Do not do that.
    public static void createAndPlayAmbientSound(SoundEvent pSoundEvent, BlockPos pPos, float volume) {
        CustomClientSoundInstanceHandler.createAnyPlayAmbientSound(pSoundEvent, pPos, volume);
    }

    public static void tryStopAmbientSound(BlockPos pPos) {
        CustomClientSoundInstanceHandler.tryStopAmbientSound(pPos);
    }
}
