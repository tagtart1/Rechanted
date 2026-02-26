package net.tagtart.rechantment.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.attachments.ModAttachments;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;

import java.util.Locale;
import java.util.List;
import java.util.Set;

public final class ModCommands {
    private static final int ORANGE_COLOR = 0xFFA500;
    private static final int SPACE_PIXEL_WIDTH = 4;
    private static final int MIN_GAP_SPACES = 2;
    private static final ResourceLocation OBTAIN_ALL_GEMS_ADVANCEMENT_ID = ResourceLocation
            .fromNamespaceAndPath(Rechantment.MOD_ID, "obtain_all_gems");
    private static final GemProgressRow[] GEM_ROWS = new GemProgressRow[] {
            new GemProgressRow("item.rechantment.chance_gem", "held_chance_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.shiny_chance_gem", "held_shiny_chance_gem", ChatFormatting.LIGHT_PURPLE),
            new GemProgressRow("item.rechantment.return_gem", "held_return_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.tasty_gem", "held_tasty_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.warp_gem", "held_warp_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.lucky_gem", "held_lucky_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.clone_gem", "held_clone_gem", ChatFormatting.AQUA),
            new GemProgressRow("item.rechantment.smithing_gem", "held_smithing_gem", ChatFormatting.AQUA)
    };

    private ModCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("rechantment")
                        .then(Commands.literal("collection")
                                .then(Commands.literal("books")
                                .executes(ModCommands::printOutBookProgress))
                                .then(Commands.literal("gems")
                                        .executes(ModCommands::printOutGemProgress))));
    }

    private static int printOutBookProgress(CommandContext<CommandSourceStack> ctx)  {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 1;
        }

        Set<String> discoveredEnchantments = player.getData(ModAttachments.DISCOVERED_ENCHANTMENTS);
        BookRarityProperties[] allProperties = BookRarityProperties.getAllProperties();

        MutableComponent outputMsg = Component.empty();
        int obtainedCount = 0;
        int totalCount = 0;
        int maxNamePixelWidth = 0;

        for (BookRarityProperties bookRarityProperties : allProperties) {
            for (EnchantmentPoolEntry enchantmentPoolEntry : bookRarityProperties.enchantmentPool) {
                String configuredEnchantId = enchantmentPoolEntry.enchantment;
                String displayName = resolveTranslatedNameOrFallback(configuredEnchantId);
                maxNamePixelWidth = Math.max(maxNamePixelWidth, getTextPixelWidth(displayName));
            }
        }

        boolean firstRarity = true;
        for (BookRarityProperties bookRarityProperties : allProperties) {
            if (!firstRarity) {
                outputMsg.append(Component.literal("-----------------\n").withStyle(ChatFormatting.WHITE));
            }
            firstRarity = false;

            List<EnchantmentPoolEntry> enchantmentPoolEntries = bookRarityProperties.enchantmentPool;
            for (EnchantmentPoolEntry enchantmentPoolEntry : enchantmentPoolEntries) {
                String configuredEnchantId = enchantmentPoolEntry.enchantment;
                String enchantPath = getEnchantmentPath(configuredEnchantId);
                String displayName = resolveTranslatedNameOrFallback(configuredEnchantId);

                boolean isFound = discoveredEnchantments.contains(configuredEnchantId)
                        || discoveredEnchantments.contains(enchantPath);
                if (isFound) {
                    obtainedCount++;
                }
                totalCount++;

                int currentNamePixelWidth = getTextPixelWidth(displayName);
                int targetPixelWidth = maxNamePixelWidth + (MIN_GAP_SPACES * SPACE_PIXEL_WIDTH);
                int spacesNeeded = (int) Math.ceil((targetPixelWidth - currentNamePixelWidth) / (double) SPACE_PIXEL_WIDTH);
                if (spacesNeeded < MIN_GAP_SPACES) {
                    spacesNeeded = MIN_GAP_SPACES;
                }
                String spacing = " ".repeat(spacesNeeded);

                MutableComponent row = Component.empty();
                row.append(Component.literal(displayName).withStyle(style -> style.withColor(bookRarityProperties.color)));
                row.append(Component.literal(spacing));
                row.append(Component.literal(isFound ? "Obtained" : "Not Found")
                        .withStyle(style -> style.withColor(isFound ? ChatFormatting.DARK_GREEN : ChatFormatting.RED)));
                row.append(Component.literal("\n"));
                outputMsg.append(row);
            }
        }

        if (outputMsg.getSiblings().isEmpty()) {
            outputMsg.append(Component.literal("No enchantments configured."));
        }

        int notFoundCount = totalCount - obtainedCount;
        outputMsg.append(Component.literal("-----------------\n").withStyle(ChatFormatting.WHITE));

        MutableComponent summary = Component.literal("Obtained: ");
        summary.append(Component.literal(String.valueOf(obtainedCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        summary.append(Component.literal(" | Not Found: "));
        summary.append(Component.literal(String.valueOf(notFoundCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        summary.append(Component.literal(" | Total: "));
        summary.append(Component.literal(String.valueOf(totalCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        outputMsg.append(summary);

        ctx.getSource().sendSuccess(() -> outputMsg, false);

        return 1;
    }

    private static int printOutGemProgress(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 1;
        }

        var advancement = ctx.getSource().getServer().getAdvancements().get(OBTAIN_ALL_GEMS_ADVANCEMENT_ID);
        AdvancementProgress progress = advancement == null ? null : player.getAdvancements().getOrStartProgress(advancement);

        MutableComponent outputMsg = Component.empty();
        int maxNamePixelWidth = 0;
        int obtainedCount = 0;
        int totalCount = GEM_ROWS.length;

        for (GemProgressRow row : GEM_ROWS) {
            String displayName = Component.translatable(row.translationKey()).getString();
            maxNamePixelWidth = Math.max(maxNamePixelWidth, getTextPixelWidth(displayName));
        }

        for (GemProgressRow row : GEM_ROWS) {
            String displayName = Component.translatable(row.translationKey()).getString();
            boolean isFound = progress != null
                    && progress.getCriterion(row.criterion()) != null
                    && progress.getCriterion(row.criterion()).isDone();
            if (isFound) {
                obtainedCount++;
            }

            int currentNamePixelWidth = getTextPixelWidth(displayName);
            int targetPixelWidth = maxNamePixelWidth + (MIN_GAP_SPACES * SPACE_PIXEL_WIDTH);
            int spacesNeeded = (int) Math.ceil((targetPixelWidth - currentNamePixelWidth) / (double) SPACE_PIXEL_WIDTH);
            if (spacesNeeded < MIN_GAP_SPACES) {
                spacesNeeded = MIN_GAP_SPACES;
            }
            String spacing = " ".repeat(spacesNeeded);

            MutableComponent line = Component.empty();
            line.append(Component.literal(displayName).withStyle(row.color()));
            line.append(Component.literal(spacing));
            line.append(Component.literal(isFound ? "Obtained" : "Not Found")
                    .withStyle(isFound ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
            line.append(Component.literal("\n"));
            outputMsg.append(line);
        }

        int notFoundCount = totalCount - obtainedCount;
        outputMsg.append(Component.literal("-----------------\n").withStyle(ChatFormatting.WHITE));

        MutableComponent summary = Component.literal("Obtained: ");
        summary.append(Component.literal(String.valueOf(obtainedCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        summary.append(Component.literal(" | Not Found: "));
        summary.append(Component.literal(String.valueOf(notFoundCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        summary.append(Component.literal(" | Total: "));
        summary.append(Component.literal(String.valueOf(totalCount)).withStyle(style -> style.withColor(ORANGE_COLOR)));
        outputMsg.append(summary);

        ctx.getSource().sendSuccess(() -> outputMsg, false);
        return 1;
    }

    private static String resolveTranslatedNameOrFallback(String enchantmentId) {
        String[] split = enchantmentId.split(":", 2);
        if (split.length != 2) {
            return toTitleCase(enchantmentId);
        }

        String translationKey = "enchantment." + split[0] + "." + split[1];
        String translated = Component.translatable(translationKey).getString();
        if (!translated.equals(translationKey)) {
            return translated;
        }

        return toTitleCase(split[1]);
    }

    private static String getEnchantmentPath(String enchantmentId) {
        String[] split = enchantmentId.split(":", 2);
        return split.length == 2 ? split[1] : enchantmentId;
    }

    private static String toTitleCase(String input) {
        String[] words = input.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }

            String lower = words[i].toLowerCase(Locale.ROOT);
            String formatted = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append(formatted);
        }

        return builder.isEmpty() ? input : builder.toString();
    }

    private static int getTextPixelWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += getGlyphPixelWidth(text.charAt(i));
        }
        return width;
    }

    private static int getGlyphPixelWidth(char c) {
        return switch (c) {
            case 'i', '!', '|', ':', ';', '\'', '.', ',' -> 2;
            case 'l', '`' -> 3;
            case 'I', 't', '[', ']', '(', ')', '{', '}', '*', '"' -> 4;
            case 'f', 'k', '<', '>', ' ' -> 5;
            case '@', '~' -> 7;
            default -> 6;
        };
    }

    private record GemProgressRow(String translationKey, String criterion, ChatFormatting color) {
    }
}
