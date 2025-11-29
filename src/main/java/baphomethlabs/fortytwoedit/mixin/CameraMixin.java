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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

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
    private void injectSetup(Level level, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo c) {
        if(FortytwoEdit.isFreeLooking) {
            this.setRotation(FortytwoEdit.cameraRotation[0], FortytwoEdit.cameraRotation[1]);
            this.setPosition(
                Mth.lerp((double)tickDelta, focusedEntity.xo, focusedEntity.getX()),
                Mth.lerp((double)tickDelta, focusedEntity.yo, focusedEntity.getY()) + Mth.lerp(tickDelta, this.eyeHeightOld, this.eyeHeight),
                Mth.lerp((double)tickDelta, focusedEntity.zo, focusedEntity.getZ())
            );

            float g = 4.0F;
			float h = 1.0F;
			if (focusedEntity instanceof LivingEntity livingEntity) {
				h = livingEntity.getScale();
				g = (float)livingEntity.getAttributeValue(Attributes.CAMERA_DISTANCE);
			}

			float i = h;
			float j = g;
			if (focusedEntity.isPassenger() && focusedEntity.getVehicle() instanceof LivingEntity livingEntity2) {
				i = livingEntity2.getScale();
				j = (float)livingEntity2.getAttributeValue(Attributes.CAMERA_DISTANCE);
			}

			this.move(-this.getMaxZoom(Math.max(h * g, i * j)), 0.0F, 0.0F);
        }
    }

}
