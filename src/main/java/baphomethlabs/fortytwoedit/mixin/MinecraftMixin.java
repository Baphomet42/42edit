package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void appendConstructor(CallbackInfo ci) {
        FortytwoEdit.onMinecraftInit();
    }

	@Inject(method = "tick", at = @At("RETURN"))
	private void appendClientTick(CallbackInfo ci) {
		FortytwoEdit.clientTick((Minecraft)(Object)this);
	}

    @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
    private void setGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(FortytwoEdit.xrayEntity) {
            cir.setReturnValue(true);
        }
    }

	@Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;getFramerateLimit()I"))
	private int redirectFramerate(FramerateLimitTracker inactivityFpsLimiter) {
        if(FortytwoEdit.autoClicker && FortytwoEdit.afkScreenLock && ((Minecraft)(Object)this).player != null) {
            return 10;
        }
        return inactivityFpsLimiter.getFramerateLimit();
	}

}
