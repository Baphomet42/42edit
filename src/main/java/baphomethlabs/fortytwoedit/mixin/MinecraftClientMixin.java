package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    
    @Inject(method="hasOutline(Lnet/minecraft/entity/Entity;)Z", at=@At("RETURN"), cancellable = true)
    private void setGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(FortytwoEdit.xrayEntity) {
            cir.setReturnValue(true);
        }
    }
    
}
