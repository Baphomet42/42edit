package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipComponent;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;

@Mixin(TooltipComponent.class)
public abstract interface TooltipComponentMixin {
    
    @Inject(method="of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at=@At("HEAD"), cancellable = true)
    private static void ofContainer(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        
        if (data instanceof ContainerTooltipData) {
            cir.setReturnValue(new ContainerTooltipComponent((ContainerTooltipData)data));
        }

    }

}
