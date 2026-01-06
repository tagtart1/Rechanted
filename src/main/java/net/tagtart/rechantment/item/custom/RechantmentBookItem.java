package net.tagtart.rechantment.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RechantmentBookItem extends Item {
    public RechantmentBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }


    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {

            // EXAMPLE way of getting enchants stored on something
            ItemStack item = context.getItemInHand();

            ItemEnchantments.Mutable storedEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            Holder<Enchantment> sharpness = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
            storedEnchants.set(sharpness, 5);

            item.set(DataComponents.STORED_ENCHANTMENTS, storedEnchants.toImmutable());
        }

        return InteractionResult.SUCCESS;
    }
}
