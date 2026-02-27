package net.tagtart.rechanted.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;

import java.util.Random;

public class SatiatedEffect extends MobEffect {

    public static int DEFAULT_LENGTH_TICKS = 36000;

    private final Random rand = new Random();

    // This is pretty much just 1:1 the nourishment effect from farmer's delight. I take no credit in this!!!
    // https://github.com/vectorwing/FarmersDelight/blob/1.20/src/main/java/vectorwing/farmersdelight/common/effect/NourishmentEffect.java
    protected SatiatedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {

        // Small change to burp randomly each tick cuz it's funny.
        // Happens every 30 sec. on average.
        if (rand.nextInt(0, 600) == 69) {
            if (livingEntity.level() instanceof ServerLevel level) {
                level.playSound(null, livingEntity.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, rand.nextFloat(0.75f, 1.3f));
            }
        }

        // Part copied from farmer's delight:
        if (livingEntity instanceof Player player) {
            FoodData foodData = player.getFoodData();
            boolean isPlayerHealingWithHunger =
                    (player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION))
                            && player.isHurt()
                            && foodData.getFoodLevel() >= 18;
            if (!isPlayerHealingWithHunger) {
                float exhaustion = foodData.getExhaustionLevel();
                float reduction = Math.min(exhaustion, 4.0F);
                if (exhaustion > 0.0F) {
                    player.causeFoodExhaustion(-reduction);
                }
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
