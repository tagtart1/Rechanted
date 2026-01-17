package net.tagtart.rechantment.enchantment;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.enchantment.custom.*;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> THUNDER_STRIKE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "thunder_strike"));

    public static final ResourceKey<Enchantment> HELLS_FURY = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "hells_fury"));

    public static final ResourceKey<Enchantment> VOIDS_BANE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "voids_bane"));

    public static final ResourceKey<Enchantment> ICE_ASPECT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "ice_aspect"));

    public static final ResourceKey<Enchantment> WISDOM = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "wisdom"));

    public static final ResourceKey<Enchantment> INQUISITIVE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "inquisitive"));

    public static final ResourceKey<Enchantment> BASH = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "bash"));

    public static final ResourceKey<Enchantment> COURAGE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "courage"));

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

        register(context, WISDOM, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.PICKAXES),
                        items.getOrThrow(ItemTags.PICKAXES),
                5,
                2,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE,
                        new WisdomEnchantmentEffect()));

        register(context, INQUISITIVE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                4,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.MOB_EXPERIENCE,
                        new InquisitiveEnchantmentEffect()));

        register(context, BASH, Enchantment.enchantment(Enchantment.definition(
                HolderSet.direct(items.getOrThrow(ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shield")))),
                HolderSet.direct(items.getOrThrow(ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shield")))),
                5,
                1,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.OFFHAND))
                .withEffect(EnchantmentEffectComponents.ITEM_DAMAGE,
                        new BashEnchantmentEffect()));
        // Note: Bash knockback effect is triggered via event handler in ModEvents

        register(context, COURAGE, Enchantment.enchantment(Enchantment.definition(
                HolderSet.direct(items.getOrThrow(ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shield")))),
                HolderSet.direct(items.getOrThrow(ResourceKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shield")))),
                5,
                2,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.OFFHAND))
                .withEffect(EnchantmentEffectComponents.ITEM_DAMAGE,
                        new CourageEnchantmentEffect()));
        // Note: Courage speed effect is triggered via event handler in ModEvents


    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
            Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
