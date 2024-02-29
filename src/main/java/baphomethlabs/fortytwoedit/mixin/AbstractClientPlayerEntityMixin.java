package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
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
    
    @Inject(method="getSkinTextures()Lnet/minecraft/client/util/SkinTextures;", at=@At("RETURN"), cancellable = true)
    public void getSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {

        PlayerListEntry playerEntry = this.getPlayerListEntry();
        if(playerEntry != null) {
            
            SkinTextures skin = playerEntry.getSkinTextures();
            Identifier texture = skin.texture();
            Identifier cape = skin.capeTexture();
            Model model = skin.model();
            String name = playerEntry.getProfile().getName();
            boolean changed = false;

            //cape
            if(FortytwoEdit.opticapesWorking && FortytwoEdit.opticapesOn) {
                if(FortytwoEdit.capeCached(name)) {
                    cape = new Identifier("42edit:cache/capes/"+name.toLowerCase());
                    changed = true;
                }
                else if(!FortytwoEdit.nameCached(name) && FortytwoEdit.capeTimeCheck()) {
                    FortytwoEdit.tryLoadCape(name);
                }
            }
            if(FortytwoEdit.showClientCape && name.equals(FortytwoEdit.clientUsername)) {
                cape = new Identifier("42edit:textures/capes/"+FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]+".png");
                if(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape].equals("none"))
                    cape = null;
                changed = true;
            }

            //skin
            if(FortytwoEdit.showClientSkin && !FortytwoEdit.customSkinName.equals("") && name.equals(FortytwoEdit.clientUsername)) {
                texture = FortytwoEdit.customSkinID;
                changed = true;
            }


            //model
            if(FortytwoEdit.showClientSkin && name.equals(FortytwoEdit.clientUsername)) {
                if(FortytwoEdit.clientSkinSlim)
                    model = Model.SLIM;
                else
                    model = Model.WIDE;

                changed = true;
            }

            if(changed) {
                cir.setReturnValue(new SkinTextures(texture, null, cape, cape, model, false));
            }

        }
    }

}