package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void handleFreeLook(LocalPlayer entity, double cursorDeltaX, double cursorDeltaY) {
        if(FortytwoEdit.isFreeLooking) {
            float deltaY = (float)cursorDeltaY * 0.15f;
            float deltaX = (float)cursorDeltaX * 0.15f;
            FortytwoEdit.cameraRotation[0] += deltaX;
            FortytwoEdit.cameraRotation[1] = Mth.clamp(FortytwoEdit.cameraRotation[1]+deltaY, -90.0f, 90.0f);
        }
        else if(!(FortytwoEdit.autoClicker && FortytwoEdit.afkScreenLock))
            entity.turn(cursorDeltaX,cursorDeltaY);
    }

}
