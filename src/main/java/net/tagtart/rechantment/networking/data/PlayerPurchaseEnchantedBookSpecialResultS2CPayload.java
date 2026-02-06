package net.tagtart.rechantment.networking.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.networking.CustomCodecs;
import net.tagtart.rechantment.screen.RechantmentTableMenu;


// This is literally just to
public record PlayerPurchaseEnchantedBookSpecialResultS2CPayload(int rewardedGem) implements CustomPacketPayload {


    public static final CustomPacketPayload.Type<PlayerPurchaseEnchantedBookSpecialResultS2CPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "purchase_book_special_result"));

    public static final StreamCodec<ByteBuf, PlayerPurchaseEnchantedBookSpecialResultS2CPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerPurchaseEnchantedBookSpecialResultS2CPayload::rewardedGem,
            PlayerPurchaseEnchantedBookSpecialResultS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayloadOnClientMain(final PlayerPurchaseEnchantedBookSpecialResultS2CPayload payload, final IPayloadContext context) {

        Player player = context.player();

        if (player.containerMenu instanceof RechantmentTableMenu rechantmentTableMenu) {
            //rechantmentTableMenu.gemEarnedEffectQueued = true;
        }
    }
}
