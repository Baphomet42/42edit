package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.gui.screen.Capes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache.RenderInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin extends Mannequin {

	public ClientMannequinMixin() {
		super(null);
        throw new AssertionError();
	}

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void injectSkin(CallbackInfoReturnable<PlayerSkin> cir) {

        String name = null;
        if(this.getProfile().right().isPresent()) {
            ResolvableProfile resolvableProfile = this.getProfile().right().get();
            if(resolvableProfile instanceof ResolvableProfile.Dynamic dynamicProfile) {
                if(dynamicProfile.name().isPresent()) {
                    name = dynamicProfile.name().get();
                }
                else {
                    final Minecraft minecraft = Minecraft.getInstance();
                    RenderInfo renderInfo = minecraft.playerSkinRenderCache().getOrDefault(resolvableProfile);
                    if(renderInfo != null) {
                        name = renderInfo.gameProfile().name();
                    }
                }
            }
        }
        if(name != null && !name.isEmpty()) {
            PlayerSkin skin = Capes.injectSkinLogic(name, cir.getReturnValue());
            if(skin != null) {
                cir.setReturnValue(skin);
            }
        }
    }

}