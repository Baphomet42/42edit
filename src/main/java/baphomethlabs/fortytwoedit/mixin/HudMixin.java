package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.include.com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

@Mixin(Hud.class)
public abstract class HudMixin {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static long lastRefreshTime = 0;
    private static Component cacheCoordHud = null;

    /**
     * see {@link net.minecraft.client.gui.components.DebugScreenOverlay#extractRenderState}
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void injectExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo c) {
        if (FortytwoEdit.autoMove || FortytwoEdit.autoClicker || FortytwoEdit.isRandoModeActive() || OptionsUtil.ModOptions.COORD_HUD.getSetting()) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.gui.hud.isHidden() && minecraft.player != null) {

                List<Component> rightText = Lists.newArrayList();
                if (FortytwoEdit.autoMove)
                    rightText.add(Component.nullToEmpty("[Auto Move]"));
                if (FortytwoEdit.autoClicker)
                    rightText.add(Component.nullToEmpty("[Auto Click]"));
                if (FortytwoEdit.isRandoModeActive())
                    rightText.add(Component.nullToEmpty("[Rando Mode]"));

                boolean showCoordHud = OptionsUtil.ModOptions.COORD_HUD.getSetting() && !minecraft.debugEntries.isOverlayVisible() && !minecraft.showOnlyReducedInfo();

                if (showCoordHud || !rightText.isEmpty()) {
                    graphics.nextStratum();

                    Window window = minecraft.getWindow();
                    int standardGuiScale = window.getGuiScale();
                    int newScale = minecraft.options.debugGuiScale().get();
                    if (newScale == -1) {
                        newScale = standardGuiScale;
                    } else if (newScale == 0) {
                        int maxGuiScale = minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
                        newScale = maxGuiScale / 2;
                    } else {
                        newScale = window.calculateScale(newScale, minecraft.isEnforceUnicode());
                    }

                    graphics.pose().pushMatrix();
                    int scaledScreenHeight;
                    int scaledScreenWidth;
                    if (newScale < standardGuiScale && newScale > 0) {
                        graphics.pose().scale((float)newScale / standardGuiScale, (float)newScale / standardGuiScale);
                        scaledScreenWidth = window.getWidth() / newScale;
                        scaledScreenHeight = window.getHeight() / newScale;
                    } else {
                        scaledScreenWidth = graphics.guiWidth();
                        scaledScreenHeight = graphics.guiHeight();
                    }

                    if (showCoordHud) {
                        // see net.minecraft.client.gui.components.debug.DebugEntryPosition
                        long currentTime = System.currentTimeMillis();
                        if (cacheCoordHud == null || currentTime - lastRefreshTime > 50) {
                            lastRefreshTime = currentTime;
                            BlockPos feetPos = minecraft.player.blockPosition();
                            String coordFacing = "S";
                            switch (minecraft.player.getDirection()) {
                                case NORTH: coordFacing = "N"; break;
                                case EAST: coordFacing = "E"; break;
                                case WEST: coordFacing = "W"; break;
                                default: break;
                            }
                            cacheCoordHud = Component.empty().append(coordFacing + " " + feetPos.getX() + " " + feetPos.getY() + " " + feetPos.getZ());
                        }
                        graphics.text(minecraft.font, cacheCoordHud, 2, 2, TEXT_COLOR, true);
                    }

                    if (!rightText.isEmpty()) {
                        int height = 9;
                        int right = scaledScreenWidth - 30;
                        int bottom = scaledScreenHeight - 30;
                        for (int i = 0; i < rightText.size(); i++) {
                            Component line = rightText.get(i);
                            int width = minecraft.font.width(line);
                            int left = right - width;
                            int top = bottom - (height * i);
                            graphics.text(minecraft.font, line, left, top, TEXT_COLOR, true);
                        }
                    }

                    graphics.nextStratum();
                    graphics.pose().popMatrix();
                }
            }
        }
    }

}
