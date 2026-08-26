package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    protected abstract float calculateVolume(float volume, SoundSource soundSource);
    
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void injectPlay(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (FortytwoEdit.autoFish && FortytwoEdit.autoClicker) {
            try {
                if (BlackMagick.identifierToString(instance.getIdentifier()).equals("minecraft:entity.fishing_bobber.splash")) // to_do change logic to detect actual owned bobber
                    FortytwoEdit.queueAutoFish();
            } catch (Exception ex) {}
        }
    }
    
    @Redirect(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
    private float redirectCalculateVolume(SoundEngine soundEngine, float volume, SoundSource soundSource) {
        if (FortytwoEdit.shouldMuteVolume()) {
            return 0f;
        }
        return this.calculateVolume(volume, soundSource);
    }
}
