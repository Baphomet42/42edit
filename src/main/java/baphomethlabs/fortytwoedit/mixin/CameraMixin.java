package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;

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

    @Inject(method = "setup", at = @At(value = "RETURN"), cancellable = true)
    private void injectSetup(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo c) {
        if(FortytwoEdit.isFreeLooking) {
            this.setRotation(FortytwoEdit.cameraRotation[0], FortytwoEdit.cameraRotation[1]);
            this.setPosition(
                Mth.lerp((double)tickDelta, focusedEntity.xo, focusedEntity.getX()),
                Mth.lerp((double)tickDelta, focusedEntity.yo, focusedEntity.getY()) + Mth.lerp(tickDelta, this.eyeHeightOld, this.eyeHeight),
                Mth.lerp((double)tickDelta, focusedEntity.zo, focusedEntity.getZ())
            );

            float s = 1f;
            if(focusedEntity instanceof LivingEntity)
                s = ((LivingEntity)focusedEntity).getScale();
            this.move(-this.getMaxZoom(4.0f * s), 0.0f, 0.0f);
        }
    }

}
