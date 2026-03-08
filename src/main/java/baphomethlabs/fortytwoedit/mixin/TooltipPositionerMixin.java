package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MenuTooltipPositioner.class, BelowOrAboveWidgetTooltipPositioner.class, DefaultTooltipPositioner.class})
public abstract class TooltipPositionerMixin {

    @Inject(method = "positionTooltip(IIIIII)Lorg/joml/Vector2ic;", at = @At(value = "RETURN"), cancellable = true)
    private void repositionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height, CallbackInfoReturnable<Vector2ic> cir) {
        Vector2i pos = (Vector2i)cir.getReturnValue();
        if (pos.x<9 || pos.y<9) {
            if (pos.x<9)
                pos.x=9;
            if (pos.y<9)
                pos.y=9;
            cir.setReturnValue(pos);
        }
    }

}
