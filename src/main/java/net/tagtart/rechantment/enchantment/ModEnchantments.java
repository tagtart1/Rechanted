package net.tagtart.rechantment.enchantment;

import net.minecraft.core.HolderSet;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.custom.HellsFuryEnchantmentEffect;
import net.tagtart.rechantment.enchantment.custom.IceAspectEnchantmentEffect;
import net.tagtart.rechantment.enchantment.custom.ThunderStrikeEnchantmentEffect;
import net.tagtart.rechantment.enchantment.custom.VoidsBaneEnchantmentEffect;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> THUNDER_STRIKE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "thunder_strike"));

    public static final ResourceKey<Enchantment> HELLS_FURY = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "hells_fury"));

    public static final ResourceKey<Enchantment> VOIDS_BANE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "voids_bane"));

    public static final ResourceKey<Enchantment> ICE_ASPECT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "ice_aspect"));

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


        register(context, HELLS_FURY, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                5,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(VOIDS_BANE)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new HellsFuryEnchantmentEffect(2, 1)));

        register(context, VOIDS_BANE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                5,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(HELLS_FURY)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new VoidsBaneEnchantmentEffect(2, 1)));

        register(context, ICE_ASPECT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                2,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new IceAspectEnchantmentEffect(
                                30,   // Base duration in ticks (5 seconds = 100 ticks)
                                10    // Additional duration per level
                        )));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
            Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
