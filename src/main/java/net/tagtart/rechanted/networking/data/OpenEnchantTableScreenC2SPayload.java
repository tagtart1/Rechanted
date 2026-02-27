package net.tagtart.rechanted.networking.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tagtart.rechanted.Rechanted;
import net.tagtart.rechanted.block.entity.RechantedTableBlockEntity;
import net.tagtart.rechanted.screen.RechantedTableMenu;
import net.tagtart.rechanted.screen.RechantedTablePoolDisplayMenu;

// This entire packet is only necessary because you can't open sub-menus without send packets LITERALLY only because
// you can't open menus without a reference to a ServerPlayer. Wtf.
public record OpenEnchantTableScreenC2SPayload(int screenIndex, int bookPropertiesIndex, BlockPos enchantTablePos) implements CustomPacketPayload {


    public static final CustomPacketPayload.Type<OpenEnchantTableScreenC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "open_rechanted_table_screen"));

    public static final StreamCodec<ByteBuf, OpenEnchantTableScreenC2SPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            OpenEnchantTableScreenC2SPayload::screenIndex,
            ByteBufCodecs.INT,
            OpenEnchantTableScreenC2SPayload::bookPropertiesIndex,
            BlockPos.STREAM_CODEC,
            OpenEnchantTableScreenC2SPayload::enchantTablePos,
            OpenEnchantTableScreenC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayloadOnServerNetwork(final OpenEnchantTableScreenC2SPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {


            ServerPlayer serverPlayer = null;
            if (!(context.player() instanceof ServerPlayer p)) {
                return;
            }
            serverPlayer = (ServerPlayer) context.player();

            ServerLevel level = serverPlayer.serverLevel();

            BlockEntity baseBlockEntity = level.getBlockEntity(payload.enchantTablePos);
            if (!(baseBlockEntity instanceof RechantedTableBlockEntity)) return;

            RechantedTableBlockEntity blockEntity = (RechantedTableBlockEntity) baseBlockEntity;

            if (payload.screenIndex == 0) {
                SimpleMenuProvider openMenu = new SimpleMenuProvider(
                        (id, inventory, player) -> new RechantedTableMenu(id, inventory, blockEntity), (blockEntity).getDisplayName());
                serverPlayer.openMenu(openMenu, payload.enchantTablePos);
            }
            if (payload.screenIndex == 1) {
                SimpleMenuProvider openMenu = new SimpleMenuProvider(
                        (id, inventory, player) -> {
                            return new RechantedTablePoolDisplayMenu(id, inventory, level.getBlockEntity(payload.enchantTablePos), payload.bookPropertiesIndex);
                        },
                        blockEntity.getDisplayName());
                serverPlayer.openMenu(openMenu, friendlyByteBuf -> {
                    friendlyByteBuf.writeBlockPos(payload.enchantTablePos);
                    friendlyByteBuf.writeInt(payload.bookPropertiesIndex);
                });
            }
        });
    }
}
