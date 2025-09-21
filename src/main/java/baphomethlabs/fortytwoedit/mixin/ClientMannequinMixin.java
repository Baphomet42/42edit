package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.gui.screen.Capes;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin extends Mannequin {

	public ClientMannequinMixin() {
		super(null);
        throw new AssertionError();
	}

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void injectSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if(this.getProfile() != null) {
            ResolvableProfile resolvableProfile = this.getProfile();
            if(resolvableProfile.skinPatch() != null) {
                PlayerSkin.Patch overrides = resolvableProfile.skinPatch();
                if(overrides.body().isPresent() || overrides.cape().isPresent() || overrides.elytra().isPresent() || overrides.model().isPresent())
                    return;
            }
            String name = BlackMagick.getDynamicProfileName(resolvableProfile);
            if(name != null && !name.isEmpty()) {
                PlayerSkin skin = Capes.injectSkinLogic(name, cir.getReturnValue());
                if(skin != null) {
                    cir.setReturnValue(skin);
                }
            }
        }
    }

}