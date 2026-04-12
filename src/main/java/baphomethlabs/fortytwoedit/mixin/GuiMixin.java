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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

@Mixin(Gui.class)
public abstract class GuiMixin {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static long lastRefreshTime = 0;
    private static Component cacheCoordHud = null;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void injectExtractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo c) {
        if (FortytwoEdit.autoMove || FortytwoEdit.autoClicker || FortytwoEdit.randoMode || FortytwoEdit.autoFish || FortytwoEdit.showCoordHud) {
            final Minecraft client = Minecraft.getInstance();
            if (!client.options.hideGui && client.player != null) {
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
                if (FortytwoEdit.showCoordHud && !client.debugEntries.isOverlayVisible()) {
                    // see net.minecraft.client.gui.components.debug.DebugEntryPosition
                    long currentTime = System.currentTimeMillis();
                    if (cacheCoordHud == null || currentTime - lastRefreshTime > 50) {
                        lastRefreshTime = currentTime;
			            BlockPos feetPos = client.player.blockPosition();
                        String coordFacing = "S";
                        switch (client.player.getDirection()) {
                            case NORTH: coordFacing = "N"; break;
                            case EAST: coordFacing = "E"; break;
                            case WEST: coordFacing = "W"; break;
                            default: break;
                        }
                        cacheCoordHud = Component.empty().append(coordFacing+" "+feetPos.getX()+" "+feetPos.getY()+" "+feetPos.getZ());
                    }
                    context.text(client.font, cacheCoordHud, 2, 2, TEXT_COLOR, true);
                }
            }
        }
    }

}
