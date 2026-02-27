package net.tagtart.rechanted.enchantment.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record IceAspectEnchantmentEffect( int baseDuration, int durationPerLevel) implements EnchantmentEntityEffect {

    private static final int SLOW_AMPLIFIER = 2;

    public static final MapCodec<IceAspectEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("base_duration").forGetter(IceAspectEnchantmentEffect::baseDuration),
                    Codec.INT.fieldOf("duration_per_level").forGetter(IceAspectEnchantmentEffect::durationPerLevel)
            ).apply(instance, IceAspectEnchantmentEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos) {
        // Apply potion effect to the target
        if (entity instanceof LivingEntity target) {
            // Calculate duration and amplifier based on enchantment level
            int duration = baseDuration + (durationPerLevel * enchantmentLevel);

            // Extinguish flames
            target.extinguishFire();
            
            // Apply the effect
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, SLOW_AMPLIFIER));
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}

