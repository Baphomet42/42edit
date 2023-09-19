package baphomethlabs.fortytwoedit.mixin;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;

@Mixin({WidgetTooltipPositioner.class, FocusedTooltipPositioner.class, HoveredTooltipPositioner.class})
public abstract class TooltipPositionerMixin {

    @Inject(method="getPosition", at=@At(value = "RETURN"), cancellable = true)
    private void repositionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height, CallbackInfoReturnable<Vector2ic> cir) {
        Vector2i pos = (Vector2i)cir.getReturnValue();
        if(pos.x<9 || pos.y<9) {
            if(pos.x<9)
                pos.x=9;
            if(pos.y<9)
                pos.y=9;
            cir.setReturnValue(pos);
        }
    }
    
}
