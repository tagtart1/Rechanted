package net.tagtart.rechantment.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.item.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class JEIRechantmentPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Register item subtypes for JEI to distinguish by NBT for our books
        ISubtypeInterpreter<ItemStack> interpreter = new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack itemStack, @NotNull UidContext context) {

                if (itemStack.has(DataComponents.STORED_ENCHANTMENTS)) {
                    return itemStack.get(DataComponents.STORED_ENCHANTMENTS).toString();
                }

                return null;
            }

            @Override
            public @NotNull String getLegacyStringSubtypeInfo(@NotNull ItemStack ingredient, @NotNull UidContext context) {
                return "";
            }
        };
        registration.registerSubtypeInterpreter(ModItems.RECHANTMENT_BOOK.get(), interpreter);
    }
}
