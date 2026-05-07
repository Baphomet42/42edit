package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {

    @ModifyArgs(method = "tryRebuildTabContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;tryRebuildTabContents(Lnet/minecraft/world/flag/FeatureFlagSet;ZLnet/minecraft/core/HolderLookup$Provider;)Z"))
    private void injectTryRebuildTabContents(Args args) {
        args.set(0, FortytwoEdit.FEATURES);
        args.set(1, true);
    }

    @ModifyArgs(method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;tryRefreshInvalidatedTabs(Lnet/minecraft/world/flag/FeatureFlagSet;ZLnet/minecraft/core/HolderLookup$Provider;)V"))
    private void injectContainerTick(Args args) {
        args.set(0, FortytwoEdit.FEATURES);
        args.set(1, true);
    }

}
