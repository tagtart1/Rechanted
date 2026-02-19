package net.tagtart.rechantment.event.enchantment;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.tagtart.rechantment.Rechantment;
import net.tagtart.rechantment.effect.ModEffects;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Rechantment.MOD_ID)
public class BlitzActiveOrbHandler {
    private static final int ORBIT_CYCLE_TICKS = 3 * 20; // One full circle every 3 seconds.
    private static final float ORBIT_RADIUS = 0.85F;
    private static final float ORB_SCALE = 0.9F;
    private static final float TORSO_HEIGHT_FACTOR = 0.55F;
    private static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffectInstance blitzEffect = player.getEffect(ModEffects.BLITZ_EFFECT);
        if (blitzEffect == null) {
            return;
        }

        // Drive the orbit from effect duration so the path is deterministic and stays
        // continuous while active.
        int cycleTick = Math.floorMod(ORBIT_CYCLE_TICKS - (blitzEffect.getDuration() % ORBIT_CYCLE_TICKS),
                ORBIT_CYCLE_TICKS);
        float progress = cycleTick / (float) (ORBIT_CYCLE_TICKS - 1);
        float baseAngle = progress * ((float) Math.PI * 2.0F);
        // Blitz I/II/III => 1/2/3 orbs; offsets are evenly spaced around the circle
        // (2PI / N).
        int orbCount = Math.max(1, blitzEffect.getAmplifier() + 1);
        float angleStep = ((float) Math.PI * 2.0F) / orbCount;

        // Recompute center each tick so the orbit follows player movement naturally.
        double centerX = player.getX();
        double centerZ = player.getZ();
        double y = player.getY() + (player.getBbHeight() * TORSO_HEIGHT_FACTOR);
        DustParticleOptions particle = new DustParticleOptions(WHITE, ORB_SCALE);
        for (int i = 0; i < orbCount; i++) {
            float angle = baseAngle + (angleStep * i);
            double x = centerX + Math.cos(angle) * ORBIT_RADIUS;
            double z = centerZ + Math.sin(angle) * ORBIT_RADIUS;
            serverLevel.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }
}
