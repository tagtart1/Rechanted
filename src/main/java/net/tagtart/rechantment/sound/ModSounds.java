package net.tagtart.rechantment.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Rechantment.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANTED_BOOK_FAIL = registerSoundEvents("enchanted_book_fail");
    public static final DeferredHolder<SoundEvent, SoundEvent> REBIRTH_ITEM = registerSoundEvents("rebirth_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANT_TABLE_AMBIENT = registerSoundEvents("enchant_table_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANT_TABLE_CHARGE = registerSoundEvents("enchant_table_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANT_TABLE_DISCHARGE = registerSoundEvents("enchant_table_discharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANT_TABLE_OPEN = registerSoundEvents("enchant_table_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENCHANT_TABLE_CLOSE = registerSoundEvents("enchant_table_close");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
