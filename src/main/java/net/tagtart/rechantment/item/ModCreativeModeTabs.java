package net.tagtart.rechantment.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rechantment.MOD_ID);

    public static final Supplier<CreativeModeTab> RECHANTMENT_TAB = CREATIVE_MODE_TAB.register("rechantment_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CHANCE_GEM.get()))
                    .title(Component.translatable("creative.rechantment.title"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.CHANCE_GEM.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
