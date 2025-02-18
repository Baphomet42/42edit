package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.InactivityFpsLimiter;
import net.minecraft.entity.Entity;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Inject(method = "tick", at = @At("RETURN"))
	private void appendClientTick(CallbackInfo ci) {
		FortytwoEdit.clientTick((MinecraftClient)(Object)this);
	}

    @Inject(method = "hasOutline(Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    private void setGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(FortytwoEdit.xrayEntity) {
            cir.setReturnValue(true);
        }
    }

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/InactivityFpsLimiter;update()I"))
	private int injectFramerate(InactivityFpsLimiter inactivityFpsLimiter) {
        if(FortytwoEdit.autoClicker && FortytwoEdit.afkScreenLock && ((MinecraftClient)(Object)this).player != null) {
            return 10;
        }
        return inactivityFpsLimiter.update();
	}

}
