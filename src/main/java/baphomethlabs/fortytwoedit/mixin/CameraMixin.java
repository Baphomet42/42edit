package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    private double clipToSpace(double desiredCameraDistance) {
        return 0d;
    }

    @Shadow
    protected abstract void moveBy(double x, double y, double z);
    
    @Shadow
    private float cameraY;

    @Shadow
    private float lastCameraY;

    @Inject(method="update", at=@At(value="RETURN"), cancellable=true)
    private void setView(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo c) {
        if (FortytwoEdit.isFreeLooking) {
            this.setRotation(FortytwoEdit.cameraRotation[0], FortytwoEdit.cameraRotation[1]);
            this.setPos(MathHelper.lerp((double)tickDelta, focusedEntity.prevX, focusedEntity.getX()), MathHelper.lerp((double)tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY), MathHelper.lerp((double)tickDelta, focusedEntity.prevZ, focusedEntity.getZ()));

            float s = 1f;
            if (focusedEntity instanceof LivingEntity)
                s = ((LivingEntity)focusedEntity).getScale();
            this.moveBy(-this.clipToSpace(4.0f * s), 0.0, 0.0);
        }
    }

}
