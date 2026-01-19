package net.tagtart.rechantment.attachments;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tagtart.rechantment.Rechantment;

import java.time.Instant;
import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Rechantment.MOD_ID);

    // Codec for serializing Instant as epoch milliseconds (long)
    private static final Codec<Instant> INSTANT_CODEC = Codec.LONG.xmap(
            Instant::ofEpochMilli,  // Convert long to Instant
            Instant::toEpochMilli   // Convert Instant to long
    );

    // Stores the last time a player attacked (used for cooldown calculations)
    public static final Supplier<AttachmentType<Instant>> LAST_BLITZ_ATTACK_AT = ATTACHMENT_TYPES.register(
            "last_blitz_attack_at", () -> AttachmentType.builder(() -> Instant.EPOCH)
                    .serialize(INSTANT_CODEC)
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
