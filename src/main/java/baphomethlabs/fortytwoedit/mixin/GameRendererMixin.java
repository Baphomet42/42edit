package baphomethlabs.fortytwoedit.mixin;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void injectGetFov(CallbackInfoReturnable<Float> cir) {
        if(FortytwoEdit.zoomed) {
            cir.setReturnValue(17.5f);
        }
    }

}
