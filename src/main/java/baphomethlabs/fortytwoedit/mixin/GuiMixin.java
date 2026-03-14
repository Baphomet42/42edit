package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(Gui.class)
public abstract class GuiMixin {

    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void injectExtractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo c) {
        if (FortytwoEdit.autoMove || FortytwoEdit.autoClicker || FortytwoEdit.randoMode || FortytwoEdit.autoFish) {
            final Minecraft client = Minecraft.getInstance();
            if (!client.options.hideGui) {
                int x = client.getWindow().getGuiScaledWidth()-80;
                int y = client.getWindow().getGuiScaledHeight()-15;
                if (FortytwoEdit.autoMove)
                    context.text(client.font, "[Auto Move]", x, y - 20, TEXT_COLOR, true);
                if (FortytwoEdit.autoClicker)
                    context.text(client.font, "[Auto Click]", x, y - 10, TEXT_COLOR, true);
                else if (FortytwoEdit.autoFish && client.options.showSubtitles().get())
                    context.text(client.font, "[Auto Fish]", x, y - 10, TEXT_COLOR, true);
                else if (FortytwoEdit.autoFish && !client.options.showSubtitles().get())
                    context.text(client.font, "\u00a7cAuto Fish requires Subtitles", x-64, y - 10, TEXT_COLOR, true);
                if (FortytwoEdit.randoMode)
                    context.text(client.font, "[Rando Mode]", x, y, TEXT_COLOR, true);
            }
        }
    }

}
