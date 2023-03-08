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
            else if(!FortytwoEdit.nameCached(name) && FortytwoEdit.capeTimeCheck()) {
                FortytwoEdit.tryLoadCape(name);
            }
        }
        if(FortytwoEdit.showClientCape && this.getPlayerListEntry().getProfile().getName().equals(FortytwoEdit.clientUsername)) {
            cir.setReturnValue(new Identifier("42edit:textures/capes/"+FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]+".png"));
        }
    }
    
    @Inject(method="Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTexture()Lnet/minecraft/util/Identifier;",
        at=@At("RETURN"), cancellable = true)
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        if(FortytwoEdit.showClientSkin && !FortytwoEdit.customSkinName.equals("") && this.getPlayerListEntry().getProfile().getName().equals(FortytwoEdit.clientUsername)) {
            cir.setReturnValue(FortytwoEdit.customSkinID);
        }
    }
    
    @Inject(method="Lnet/minecraft/client/network/AbstractClientPlayerEntity;getModel()Ljava/lang/String;",
        at=@At("RETURN"), cancellable = true)
    public void getModel(CallbackInfoReturnable<String> cir) {
        if(FortytwoEdit.showClientSkin && this.getPlayerListEntry().getProfile().getName().equals(FortytwoEdit.clientUsername)) {
            if(FortytwoEdit.clientSkinSlim)
                cir.setReturnValue("slim");
            else
                cir.setReturnValue("default");

        }
    }

}