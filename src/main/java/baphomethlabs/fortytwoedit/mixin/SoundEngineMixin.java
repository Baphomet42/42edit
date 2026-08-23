package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    protected abstract float calculateVolume(float volume, SoundSource soundSource);
    
    @Redirect(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
    private float redirectCalculateVolume(SoundEngine soundEngine, float volume, SoundSource soundSource) {
        if (FortytwoEdit.shouldMuteVolume()) {
            return 0f;
        }
        return this.calculateVolume(volume, soundSource);
    }
}
