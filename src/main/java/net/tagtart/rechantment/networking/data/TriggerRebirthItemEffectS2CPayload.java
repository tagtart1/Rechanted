package net.tagtart.rechantment.networking.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.networking.CustomCodecs;

public record TriggerRebirthItemEffectS2CPayload(ItemStack rebirthItem) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TriggerRebirthItemEffectS2CPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "trigger_rebirth_effect"));

    public static final StreamCodec<ByteBuf, TriggerRebirthItemEffectS2CPayload> STREAM_CODEC = StreamCodec.composite(
            CustomCodecs.ITEM_STACK_CODEC,
            TriggerRebirthItemEffectS2CPayload::rebirthItem,
            TriggerRebirthItemEffectS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayloadOnClientMain(final TriggerRebirthItemEffectS2CPayload payload, final IPayloadContext context) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(payload.rebirthItem);
    }
}
