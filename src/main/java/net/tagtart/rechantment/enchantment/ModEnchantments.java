package net.tagtart.rechantment.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.custom.ThunderStrikeEnchantmentEffect;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> THUNDER_STRIKE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "thunder_strike"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);

        register(context, THUNDER_STRIKE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                4,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2,
                EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new ThunderStrikeEnchantmentEffect()));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
            Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
