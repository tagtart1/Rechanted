package net.tagtart.rechantment.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.util.BookRarityProperties;
import net.tagtart.rechantment.util.EnchantmentPoolEntry;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rechantment.MOD_ID);

    public static final Supplier<CreativeModeTab> RECHANTMENT_TAB = CREATIVE_MODE_TAB.register("rechantment_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CHANCE_GEM.get()))
                    .title(Component.translatable("creative.rechantment.title"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.RECHANTMENT_TABLE_BLOCK);
                        output.accept(ModBlocks.RECHANTMENT_TROPHY_BLOCK);

                        output.accept(ModItems.CHANCE_GEM.get());
                        output.accept(ModItems.SHINY_CHANCE_GEM.get());
                        output.accept(ModItems.RETURN_GEM.get());
                        output.accept(ModItems.TASTY_GEM.get());
                        output.accept(ModItems.CLONE_GEM.get());
                        output.accept(ModItems.WARP_GEM.get());
                        output.accept(ModItems.LUCKY_GEM.get());
                        output.accept(ModItems.SMITHING_GEM.get());
                        output.accept(ModItems.MYSTERIOUS_BOOK.get());

                        BookRarityProperties[] bookRarityProperties = BookRarityProperties.getAllProperties();
                        for(BookRarityProperties bookRarityProperty : bookRarityProperties) {
                            for(EnchantmentPoolEntry enchantPoolEntry : bookRarityProperty.enchantmentPool) {

                                String enchantmentRaw = enchantPoolEntry.enchantment;
                                ResourceLocation enchantmentResourceLocation = ResourceLocation.parse(enchantmentRaw);

                                HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = itemDisplayParameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                                ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantmentResourceLocation);

                                enchantmentRegistry.get(key).ifPresent(enchantment -> {
                                    // Use the enchantment's actual max level, not the config's max level
                                    int actualMaxLevel = enchantment.value().getMaxLevel();
                                    
                                    for(int i = 1 ; i <= actualMaxLevel ; i++) {
                                        ItemStack book = new ItemStack(ModItems.RECHANTMENT_BOOK.get());

                                        ItemEnchantments.Mutable storedEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                                        storedEnchants.set(enchantment, i);

                                        book.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants.toImmutable().withTooltip(false));
                                        book.set(ModDataComponents.SUCCESS_RATE, 100);
                                        output.accept(book);
                                    }
                                });


                            }
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
