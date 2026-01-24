package net.tagtart.rechantment.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Rechantment.MOD_ID);

    public static final Supplier<SoundEvent> ENCHANTED_BOOK_FAIL = registerSoundEvents("enchanted_book_fail");
    public static final Supplier<SoundEvent> REBIRTH_ITEM = registerSoundEvents("rebirth_item");


    public static final Supplier<SoundEvent> ENCHANT_TABLE_AMBIENT = registerSoundEvents("enchant_table_ambient");
    public static final Supplier<SoundEvent> ENCHANT_TABLE_CHARGE = registerSoundEvents("enchant_table_charge");
    public static final Supplier<SoundEvent> ENCHANT_TABLE_DISCHARGE = registerSoundEvents("enchant_table_discharge");
    public static final Supplier<SoundEvent> ENCHANT_TABLE_OPEN = registerSoundEvents("enchant_table_open");
    public static final Supplier<SoundEvent> ENCHANT_TABLE_CLOSE = registerSoundEvents("enchant_table_close");

    public static final Supplier<SoundEvent> SUPER_BURP = registerSoundEvents("super_burp");

    private static Supplier<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
