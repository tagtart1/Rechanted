package net.tagtart.rechantment.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechantment.Rechantment;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Rechantment.MOD_ID);


    public static final Holder<MobEffect> BERSERK_EFFECT = MOB_EFFECTS.register("berserk",
            () -> new BerserkEffect(MobEffectCategory.NEUTRAL, 0xFF0000, ParticleTypes.FIREWORK));

    public static final Holder<MobEffect> BERSERK_COOLDOWN_EFFECT = MOB_EFFECTS.register("berserk_cooldown",
            () -> new BerserkCooldownEffect(MobEffectCategory.NEUTRAL, 0x808080));

    public static final Holder<MobEffect> BLITZ_EFFECT = MOB_EFFECTS.register("blitz",
            () -> new BlitzEffect(MobEffectCategory.NEUTRAL, 0x00FFFF));

    public static final Holder<MobEffect> BLITZ_COOLDOWN_EFFECT = MOB_EFFECTS.register("blitz_cooldown",
            () -> new BlitzCooldownEffect(MobEffectCategory.NEUTRAL, 0x404040));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
