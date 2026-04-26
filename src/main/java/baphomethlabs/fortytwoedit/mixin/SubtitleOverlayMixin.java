package baphomethlabs.fortytwoedit.mixin;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {

    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void testSound(SoundInstance sound, WeighedSoundEvents soundSet, float range, CallbackInfo c) {
        if (FortytwoEdit.autoClicker && FortytwoEdit.autoFish
            && BlackMagick.identifierToString(sound.getIdentifier()).equals("minecraft:entity.fishing_bobber.splash")) {
                FortytwoEdit.queueAutoFish();
        }
    }

}
