package net.tagtart.rechanted.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class CustomClientSoundInstanceHandler {
    public static HashMap<BlockPos, LoopingAmbientSound> playingAmbientSounds = new HashMap<>();

    public static void createAnyPlayAmbientSound(SoundEvent pSoundEvent, BlockPos pPos, float volume) {
        if (playingAmbientSounds.containsKey(pPos))
            return;

        LoopingAmbientSound ambientSound = new LoopingAmbientSound(pSoundEvent, SoundSource.AMBIENT, pPos.getX() + 0.5f, pPos.getY() + 0.5f, pPos.getZ() + 0.5f);
        ambientSound.setVolume(volume);
        Minecraft.getInstance().getSoundManager().play(ambientSound);

        playingAmbientSounds.put(pPos, ambientSound);
    }

    public static void tryStopAmbientSound(BlockPos pPos) {
        if (playingAmbientSounds.containsKey(pPos)) {
            playingAmbientSounds.get(pPos).stopPlaying();
            playingAmbientSounds.remove(pPos);
        }
    }
}
