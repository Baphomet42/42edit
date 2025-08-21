package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipComponent;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Mixin(ClientTooltipComponent.class)
public abstract interface ClientTooltipComponentMixin {

    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void createContainer(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> cir) {

        if(data instanceof ContainerTooltipData data2) {
            cir.setReturnValue(new ContainerTooltipComponent(data2));
        }

    }

}
