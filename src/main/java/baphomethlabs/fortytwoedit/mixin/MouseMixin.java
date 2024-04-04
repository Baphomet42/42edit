package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Redirect(method="updateMouse", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void handleFreeLook(ClientPlayerEntity entity, double cursorDeltaX, double cursorDeltaY) {
        if(FortytwoEdit.isFreeLooking) {
            float deltaY = (float)cursorDeltaY * 0.15f;
            float deltaX = (float)cursorDeltaX * 0.15f;
            FortytwoEdit.cameraRotation[0] += deltaX;
            FortytwoEdit.cameraRotation[1] = MathHelper.clamp(FortytwoEdit.cameraRotation[1]+deltaY, -90.0f, 90.0f);
        }
        else
            entity.changeLookDirection(cursorDeltaX,cursorDeltaY);
    }

}
