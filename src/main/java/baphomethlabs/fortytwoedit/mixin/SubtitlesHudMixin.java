package baphomethlabs.fortytwoedit.mixin;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;

@Mixin(SubtitlesHud.class)
public abstract class SubtitlesHudMixin {

    @Inject(method = "onSoundPlayed", at = @At("HEAD"), cancellable = true)
    private void testSound(SoundInstance sound, WeightedSoundSet soundSet, float range, CallbackInfo c) {
        if(FortytwoEdit.autoFish && sound.getId().getPath().equals("entity.fishing_bobber.splash")) {
            FortytwoEdit.autoFishClick = true;
        }
    }
    
}
