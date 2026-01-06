package net.tagtart.rechantment.sound;

import ca.weblite.objc.Client;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoopingAmbientSound extends AbstractTickableSoundInstance {
    private boolean shouldStop = false;

    public LoopingAmbientSound(SoundEvent soundEvent, SoundSource category, double x, double y, double z) {
        super(soundEvent, category, RandomSource.create());
        this.looping = true;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = 1.0F;
        this.pitch = 1.0F;
    }

    @Override
    public void tick() {
        if (shouldStop) {
            this.stop();
        }
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void stopPlaying() {
        this.shouldStop = true;
    }
}