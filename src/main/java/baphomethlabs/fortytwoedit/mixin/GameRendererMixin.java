package baphomethlabs.fortytwoedit.mixin;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)F", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Float> cir) {
        if(FortytwoEdit.zoomed) {
            cir.setReturnValue(17.5f);
        }
    }
    
}
