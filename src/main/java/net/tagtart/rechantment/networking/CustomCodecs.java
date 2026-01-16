package net.tagtart.rechantment.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class CustomCodecs {

    // Had to add this because NeoForge reworked the networking api to not use FriendlyByteBuf but didn't
    // port the encoding/decoding compatibility for ItemStacks correctly for some reason??? The built-in
    // codec for ItemStack doesn't work with registering payloads so yeah, had to add this.
    public static final StreamCodec<ByteBuf, ItemStack> ITEM_STACK_CODEC =
            StreamCodec.of(
                    (buf, stack) -> ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, stack),
                    buf -> ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf)
            );
}
