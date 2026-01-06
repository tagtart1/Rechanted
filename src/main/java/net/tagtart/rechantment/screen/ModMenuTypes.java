package net.tagtart.rechantment.screen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

import java.util.function.Supplier;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, Rechantment.MOD_ID);

    public static final Supplier<MenuType<RechantmentTableMenu>> RECHANTMENT_TABLE_MENU =
            MENUS.register("rechantment_table_menu", () -> IMenuTypeExtension.create(RechantmentTableMenu::new));

    public static final Supplier<MenuType<RechantmentTablePoolDisplayMenu>> RECHANTMENT_TABLE_POOL_DISPLAY_MENU =
            MENUS.register("rechantment_table_pool_display_menu", () -> IMenuTypeExtension.create(RechantmentTablePoolDisplayMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
