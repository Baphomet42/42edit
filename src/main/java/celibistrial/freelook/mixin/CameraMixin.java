package celibistrial.freelook.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import celibistrial.freelook.CameraOverriddenEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;

@Mixin(Camera.class)
public abstract class CameraMixin {
    boolean firsttime = true;
    @Shadow
    public abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0, shift = At.Shift.AFTER))
    public void lockRotation(BlockView focusedBlock, Entity cameraEntity, boolean isThirdPerson, boolean isFrontFacing, float f, CallbackInfo ci) {
        if (FortytwoEdit.isFreeLooking && cameraEntity instanceof ClientPlayerEntity) {
            CameraOverriddenEntity cameraOverriddenEntity = (CameraOverriddenEntity) cameraEntity;

            if(firsttime && MinecraftClient.getInstance().player != null) {
                final MinecraftClient client = MinecraftClient.getInstance();
                cameraOverriddenEntity.setCameraPitch(client.player.getPitch());
                cameraOverriddenEntity.setCameraYaw(client.player.getYaw());
                firsttime = false;
            }
            this.setRotation(cameraOverriddenEntity.getCameraYaw(), cameraOverriddenEntity.getCameraPitch());

        }
        if(!FortytwoEdit.isFreeLooking && cameraEntity instanceof ClientPlayerEntity) {
            firsttime = true;
        }
    }

}