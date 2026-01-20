package net.tagtart.rechantment.enchantment;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.common.Tags;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.datagen.ModItemTagsProvider;
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

    public static final ResourceKey<Enchantment> OVERLOAD = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "overload"));

    public static final ResourceKey<Enchantment> BERSERK = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "berserk"));

    public static final ResourceKey<Enchantment> BLITZ = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "blitz"));

    public static final ResourceKey<Enchantment> TIMBER = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "timber"));

    public static final ResourceKey<Enchantment> VEIN_MINER = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "vein_miner"));

    public static final ResourceKey<Enchantment> TELEPATHY = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "telepathy"));

    public static final ResourceKey<Enchantment> REBIRTH =ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "rebirth"));

    public static final ResourceKey<Enchantment> REBORN =ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Rechantment.MOD_ID, "reborn"));

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
                        EnchantmentTarget.VICTIM, new HellsFuryEnchantmentEffect(1, 1)));

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
                        EnchantmentTarget.VICTIM, new VoidsBaneEnchantmentEffect(1, 1)));

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
        register(context, TIMBER, Enchantment.enchantment(Enchantment.definition(
            items.getOrThrow(ItemTags.AXES),
                5,
                3,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND)));

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

        register(context, OVERLOAD, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTagsProvider.OVERLOAD_ENCHANTABLE),
                items.getOrThrow(ModItemTagsProvider.OVERLOAD_ENCHANTABLE),
                5,
                3,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2,
                EquipmentSlotGroup.CHEST)));
        // Note: Overload max health effect is triggered via event handler in ModEvents
        // Uses custom tag: includes all chest armor + elytra, supports modded armor

        register(context, BERSERK, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                4,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                3,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(BLITZ)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new BerserkEnchantmentEffect()));

        register(context, BLITZ, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5,
                4,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                3,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(BERSERK)))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new BlitzEnchantmentEffect()));

        register(context, VEIN_MINER, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.PICKAXES),
                items.getOrThrow(ItemTags.PICKAXES),
                5,
                1,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND)));

        register(context, TELEPATHY, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTagsProvider.DIGGER_ITEM),
                items.getOrThrow(ModItemTagsProvider.DIGGER_ITEM),
                5,
                1,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND)));

        register(context, REBIRTH, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(Tags.Items.TOOLS),
                items.getOrThrow(Tags.Items.TOOLS),
                5,
                3,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND)));

        register(context, REBORN, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(Tags.Items.TOOLS),
                5,
                1,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(60, 20),
                2, EquipmentSlotGroup.MAINHAND)));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
            Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
