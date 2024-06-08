package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    private static final int TEXT_COLOR = 0xFFFFFF;

    @Inject(method="render", at=@At("TAIL"))
    private void renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo c) {
        if(FortytwoEdit.autoMove || FortytwoEdit.autoClicker || FortytwoEdit.randoMode || FortytwoEdit.autoFish) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int x = client.getWindow().getScaledWidth()-80;
            int y = client.getWindow().getScaledHeight()-15;
            if (FortytwoEdit.autoMove)
                context.drawText(client.textRenderer, "[Auto Move]", x, y - 20, TEXT_COLOR, true);
            if (FortytwoEdit.autoClicker)
                context.drawText(client.textRenderer, "[Auto Click]", x, y - 10, TEXT_COLOR, true);
            else if (FortytwoEdit.autoFish && client.options.getShowSubtitles().getValue())
                context.drawText(client.textRenderer, "[Auto Fish]", x, y - 10, TEXT_COLOR, true);
            else if (FortytwoEdit.autoFish && !client.options.getShowSubtitles().getValue())
                context.drawText(client.textRenderer, "\u00a7cAuto Fish requires Subtitles", x-64, y - 10, TEXT_COLOR, true);
            if (FortytwoEdit.randoMode)
                context.drawText(client.textRenderer, "[Rando Mode]", x, y, TEXT_COLOR, true);
        }
    }

}
