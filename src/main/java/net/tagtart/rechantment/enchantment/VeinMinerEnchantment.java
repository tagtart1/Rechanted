package net.tagtart.rechantment.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VeinMinerEnchantment extends Enchantment {

    protected VeinMinerEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem() instanceof PickaxeItem;
    }
}
