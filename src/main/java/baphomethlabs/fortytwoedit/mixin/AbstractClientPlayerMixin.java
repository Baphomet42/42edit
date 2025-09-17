package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.gui.screen.Capes;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Shadow
    protected abstract PlayerInfo getPlayerInfo();

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void injectSkin(CallbackInfoReturnable<PlayerSkin> cir) {

        PlayerInfo playerEntry = this.getPlayerInfo();
        if(playerEntry != null) {
            PlayerSkin skin = Capes.injectSkinLogic(playerEntry.getProfile().name(), cir.getReturnValue());
            if(skin != null) {
                cir.setReturnValue(skin);
            }
        }
    }

}