package net.tagtart.rechanted.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tagtart.rechanted.sound.ModSounds;
import net.tagtart.rechanted.util.AdvancementHelper;
import net.tagtart.rechanted.util.BookRarityProperties;
import net.tagtart.rechanted.util.UtilFunctions;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class DustyMysteriousBookItem extends Item {

    public DustyMysteriousBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable("item.rechanted.dusty_mysterious_book").withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }

        if (level instanceof ServerLevel serverLevel) {
            ItemStack rolledBook = UtilFunctions.rollModdedBook(level.registryAccess(), false, 0);
            DustParticleOptions revealDust = getRevealDust(rolledBook);

            Vec3 look = player.getLookAngle();
            Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
            Vec3 right = look.cross(worldUp);
            if (right.lengthSqr() < 1.0E-4) {
                right = new Vec3(1.0, 0.0, 0.0);
            } else {
                right = right.normalize();
            }
            Vec3 ringUp = right.cross(look).normalize();
            ringUp = ringUp.scale(0.85).add(look.scale(-0.15)).normalize();

            double handSide = player.getMainArm() == HumanoidArm.RIGHT ? 1.0 : -1.0;
            Vec3 handCenter = player.getEyePosition().add(
                    look.scale(0.34)
                            .add(right.scale(0.30 * handSide))
                            .add(ringUp.scale(-0.18)));
            Vec3 ringRight = right;

            int haloPoints = 14;
            double haloRadius = 0.38;
            for (int i = 0; i < haloPoints; i++) {
                double angle = (Math.PI * 2.0) * (i / (double) haloPoints);
                Vec3 ringOffset = ringRight.scale(Math.cos(angle) * haloRadius)
                        .add(ringUp.scale(Math.sin(angle) * haloRadius));
                serverLevel.sendParticles(
                        revealDust,
                        handCenter.x + ringOffset.x,
                        handCenter.y + ringOffset.y,
                        handCenter.z + ringOffset.z,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.ENDER_EYE_DEATH.get(), SoundSource.PLAYERS, 0.2f, 1.6f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.STEM_BREAK, SoundSource.PLAYERS, 1.2f, 1.1f);

            player.setItemInHand(usedHand, rolledBook);
            player.awardStat(Stats.ITEM_USED.get(this));
            //AdvancementHelper.recordMysteriousBookOpenAndAward(player, serverLevel);
            return InteractionResultHolder.consume(rolledBook);
        }

        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        int maxWidthTooltip = 165;
        Component itemDescription = Component.translatable("item.rechanted.dusty_mysterious_book.desc");

        String itemDescriptionString = itemDescription.getString();

        tooltipComponents.add(Component.literal(" "));

        // Prevents the description text from making the tooltip go across the entire
        // screen like a chump
        List<String> splitText = UtilFunctions.wrapText(itemDescriptionString, maxWidthTooltip);
        for (String s : splitText) {
            tooltipComponents.add(Component.literal(s.trim()));
        }

        tooltipComponents.add(Component.literal(" "));

        tooltipComponents.add(Component.literal("→ ʀɪɢʜᴛ ᴄʟɪᴄᴋ ɪɴ ʜᴀɴᴅ ᴛᴏ ʀᴇᴠᴇᴀʟ").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    private static DustParticleOptions getRevealDust(ItemStack rolledBook) {
        BookRarityProperties rarity = getRarityFromBook(rolledBook);
        int color = rarity != null ? rarity.color : 0xD0D0D0;
        Vector3f rgb = colorToVector(color);
        return new DustParticleOptions(rgb, 0.8F);
    }

    private static BookRarityProperties getRarityFromBook(ItemStack rolledBook) {
        ItemEnchantments enchants = rolledBook.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return null;
        }

        var entry = enchants.entrySet().iterator().next();
        Holder<Enchantment> enchantmentHolder = entry.getKey();
        if (enchantmentHolder == null) {
            return null;
        }

        var key = enchantmentHolder.unwrapKey();
        if (key.isEmpty()) {
            return null;
        }

        String enchantmentRaw = key.get().location().toString();
        return UtilFunctions.getPropertiesFromEnchantment(enchantmentRaw);
    }

    private static Vector3f colorToVector(int color) {
        int rgb = color & 0xFFFFFF;
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }

}
