package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @ModifyArgs(method="<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/resource/featuretoggle/FeatureSet;Z)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroups;updateDisplayContext(Lnet/minecraft/resource/featuretoggle/FeatureSet;ZLnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Z"))
    private void injectedInit(Args args) {
        args.set(0,FortytwoEdit.FEATURES);
        args.set(1,true);
    }

    @ModifyArgs(method="handledScreenTick()V", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;updateDisplayParameters(Lnet/minecraft/resource/featuretoggle/FeatureSet;ZLnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V"))
    private void injectedTick(Args args) {
        args.set(0,FortytwoEdit.FEATURES);
        args.set(1,true);
    }

}
