package net.tagtart.rechanted.event;

import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.tagtart.rechanted.Rechanted;

@EventBusSubscriber(modid = Rechanted.MOD_ID)
public class ParticleEmitter {
    private static int ticksRemaining = 0;
    private static int particlesPerTick = 0;
    private static Player player;
    private static ServerLevel serverLevel;
    private static SimpleParticleType[] particleTypes;

    public static void emitParticlesOverTime(Player p, ServerLevel level, int totalParticles, int totalTicks, SimpleParticleType[] particles ) {
        ticksRemaining = totalTicks;
        particlesPerTick = (int) Math.ceil((double) totalParticles / totalTicks);
        player = p;
        serverLevel = level;
        particleTypes = particles;

    }

    // NOTE: If particles don't work later, first thing to try is making this a ServerTickEvent.Pre.
    // ServerTickEvent is an abstract class now and I picked ServerTickEvent.Post arbitrarily for now.
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ticksRemaining > 0 && player != null && serverLevel != null && particleTypes.length > 0) {
            ticksRemaining--;

            for (int i = 0; i < particlesPerTick; i++) {
                spawnParticlesOnPlayer(player, serverLevel, particleTypes);
            }

            if (ticksRemaining == 0) {
                player = null;
                serverLevel = null;
                particleTypes = null;
            }
        }
    }

    private static void spawnParticlesOnPlayer(Player player, ServerLevel level, SimpleParticleType[] particles) {
        double speedX = (Math.random() - 0.5) * 2.0;
        double speedY = Math.random() * 1.25;
        double speedZ = (Math.random() - 0.5) * 2.0;
        for (ServerPlayer otherPlayer : level.players()) {
            for (SimpleParticleType particle : particles) {
                level.sendParticles(
                        otherPlayer,
                        particle,  // Particle type
                        true,
                        player.getX(),
                        player.getY() + 1 ,               // Y position (just above the player)
                        player.getZ(),
                        1,                             // Number of particles per spawn (1 per iteration)
                        speedX,                          // Random speed in X
                        speedY,                          // Random speed in Y
                        speedZ,                          // Random speed in Z
                        0.1                              // Speed multiplier
                );
            }
        }

    }
}
