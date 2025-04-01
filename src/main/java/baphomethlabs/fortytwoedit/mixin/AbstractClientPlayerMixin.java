package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.resources.ResourceLocation;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Shadow
    protected abstract PlayerInfo getPlayerInfo();

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void injectSkin(CallbackInfoReturnable<PlayerSkin> cir) {

        PlayerInfo playerEntry = this.getPlayerInfo();
        if(playerEntry != null) {

            PlayerSkin skin = playerEntry.getSkin();
            ResourceLocation texture = skin.texture();
            ResourceLocation cape = skin.capeTexture();
            Model model = skin.model();
            String name = playerEntry.getProfile().getName();
            boolean changed = false;

            //cape
            if(FortytwoEdit.opticapesWorking && FortytwoEdit.opticapesOn) {
                if(FortytwoEdit.capeCached(name)) {
                    cape = ResourceLocation.fromNamespaceAndPath("42edit","cache/cape/"+name.toLowerCase());
                    changed = true;
                }
                else if(!FortytwoEdit.nameCached(name) && FortytwoEdit.capeTimeCheck()) {
                    FortytwoEdit.tryLoadCape(name);
                }
            }
            if(FortytwoEdit.showClientCape && name.equals(FortytwoEdit.USERNAME)) {
                cape = FortytwoEdit.getClientCape();
                changed = true;
            }

            //skin
            if(FortytwoEdit.showClientSkin && !FortytwoEdit.customSkinName.equals("") && name.equals(FortytwoEdit.USERNAME)) {
                texture = FortytwoEdit.CUSTOM_SKIN_ID;
                changed = true;
            }


            //model
            if(FortytwoEdit.showClientSkin && name.equals(FortytwoEdit.USERNAME)) {
                if(FortytwoEdit.clientSkinSlim)
                    model = Model.SLIM;
                else
                    model = Model.WIDE;

                changed = true;
            }

            if(changed) {
                cir.setReturnValue(new PlayerSkin(texture, null, cape, cape, model, false));
            }

        }
    }

}