package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Camera;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpyglassItem;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    private float getMaxZoom(float desiredCameraDistance) {
        return 0f;
    }

    @Shadow
    protected abstract void move(float x, float y, float z);

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Redirect(method = "tickFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getFieldOfViewModifier(ZF)F"))
    private float injectTickFov(AbstractClientPlayer player, boolean firstPerson, float effectScale) {
        if (FortytwoEdit.zoomed) {
            return SpyglassItem.ZOOM_FOV_MODIFIER;
        }
        return player.getFieldOfViewModifier(firstPerson, effectScale);
    }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"))
    private float redirectAlignWithEntityGetViewXRot(Entity entity, float partialTicks) {
        if (FortytwoEdit.isFreeLooking)
            return FortytwoEdit.cameraRotation[1];
        return entity.getViewXRot(partialTicks);
    }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"))
    private float redirectAlignWithEntityGetViewYRot(Entity entity, float partialTicks) {
        if (FortytwoEdit.isFreeLooking)
            return FortytwoEdit.cameraRotation[0];
        return entity.getViewYRot(partialTicks);
    }

}
