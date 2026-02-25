package net.tagtart.rechantment.attachments;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tagtart.rechantment.Rechantment;

import java.util.ArrayList;
import java.util.HashSet;
import java.time.Instant;
import java.util.Set;
import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Rechantment.MOD_ID);

    // Codec for serializing Instant as epoch milliseconds
    private static final Codec<Instant> INSTANT_CODEC = Codec.LONG.xmap(
            Instant::ofEpochMilli,  // Convert long to Instant
            Instant::toEpochMilli   // Convert Instant to long
    );
    private static final Codec<Set<String>> STRING_SET_CODEC = Codec.STRING.listOf().xmap(
            HashSet::new,
            ArrayList::new
    );

    // Stores the last time a player attacked (used for Blitz combo timing)
    public static final Supplier<AttachmentType<Instant>> LAST_BLITZ_ATTACK_AT = ATTACHMENT_TYPES.register(
            "last_blitz_attack_at", () -> AttachmentType.builder(() -> Instant.EPOCH)
                    .serialize(INSTANT_CODEC)
                    .build()
    );

    // Stores the current Blitz combo count (0-5)
    public static final Supplier<AttachmentType<Integer>> BLITZ_COMBO = ATTACHMENT_TYPES.register(
            "blitz_combo", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> SPRING_CHARGE_TICKS = ATTACHMENT_TYPES.register(
            "spring_charge_ticks", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> SPRING_JUMP_ACTIVE = ATTACHMENT_TYPES.register(
            "spring_jump_active", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Double>> SPRING_JUMP_START_Y = ATTACHMENT_TYPES.register(
            "spring_jump_start_y", () -> AttachmentType.builder(() -> 0.0)
                    .serialize(Codec.DOUBLE)
                    .build()
    );

    public static final Supplier<AttachmentType<Set<String>>> ARCHMAGE_DISCOVERED_ENCHANTMENTS = ATTACHMENT_TYPES.register(
            "archmage_discovered_enchantments", () -> AttachmentType.builder((Supplier<Set<String>>) HashSet::new)
                    .serialize(STRING_SET_CODEC, set -> !set.isEmpty())
                    .copyOnDeath()
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
