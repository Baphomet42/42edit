package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Shadow protected abstract PlayerListEntry getPlayerListEntry();
    
    @Inject(method="Lnet/minecraft/client/network/AbstractClientPlayerEntity;getCapeTexture()Lnet/minecraft/util/Identifier;",
        at=@At("RETURN"), cancellable = true)
    public void getCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        if(FortytwoEdit.opticapes) {
            String name = this.getPlayerListEntry().getProfile().getName();
            if(FortytwoEdit.capeCached(name)) {
                cir.setReturnValue(new Identifier("42edit:cache/capes/"+name.toLowerCase()));
            }
            else if(FortytwoEdit.capeTimeCheck() && !FortytwoEdit.nameCached(name)) {
                FortytwoEdit.tryLoadCape(name);
            }
        }
        if(FortytwoEdit.showClientCape && this.getPlayerListEntry().getProfile().getName().equals(FortytwoEdit.clientUsername)) {
            cir.setReturnValue(new Identifier("42edit:textures/capes/"+FortytwoEdit.clientCape+".png"));
        }
    }

}