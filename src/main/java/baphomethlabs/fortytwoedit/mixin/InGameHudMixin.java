package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method="render(Lnet/minecraft/client/gui/DrawContext;F)V", at=@At("TAIL"))
    private void renderHud(DrawContext context, float tickDelta, CallbackInfo c) {
        if(FortytwoEdit.autoMove || FortytwoEdit.autoClicker || FortytwoEdit.randoMode) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int x = client.getWindow().getScaledWidth()-80;
            int y = client.getWindow().getScaledHeight()-15;
            if (FortytwoEdit.autoMove)
                context.drawText(client.textRenderer, "[Auto Move]", x, y - 20, 0xffffff, true);
            if (FortytwoEdit.autoClicker)
                context.drawText(client.textRenderer, "[Auto Click]", x, y - 10, 0xffffff, true);
            if (FortytwoEdit.randoMode)
                context.drawText(client.textRenderer, "[Rando Mode]", x, y, 0xffffff, true);
        }
    }

}
