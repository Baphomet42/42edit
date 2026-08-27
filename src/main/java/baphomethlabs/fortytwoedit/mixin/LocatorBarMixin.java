package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin {

    private static PlayerInfo playerInfo = null;
    private static boolean isUpsideDown = false;
    private static int size = 0;

    @Inject(method = "lambda$extractRenderState$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"), cancellable = true)
    public void injectExtractRenderState(Entity entity, Level level, PartialTickSupplier partialTickSupplier, GuiGraphicsExtractor guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {

        playerInfo = null;
        if (FortytwoEdit.mixinLocatorBar
        && (trackedWaypoint.icon().style == WaypointStyleAssets.DEFAULT || FortytwoEdit.mixinLocatorBarAlways)
        && trackedWaypoint.id().left().isPresent()) {
            final Minecraft minecraft = Minecraft.getInstance();
            playerInfo = minecraft.player.connection.getPlayerInfo(trackedWaypoint.id().left().get());
            if (playerInfo != null) {
                Player player = minecraft.level.getPlayerByUUID(playerInfo.getProfile().id());
                isUpsideDown = player != null && AvatarRenderer.isPlayerUpsideDown(player);

                WaypointStyle waypointStyle = minecraft.gui.hud.getWaypointStyles().get(trackedWaypoint.icon().style);
                float f = Mth.sqrt((float)trackedWaypoint.distanceSquared(entity));
                size = 0;
                if (f < waypointStyle.nearDistance())
                    size = 1;
                else if (f >= waypointStyle.farDistance())
                    size = -1;
            }
        }

    }

    @Redirect(method = "lambda$extractRenderState$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void redirectBlitSprite(GuiGraphicsExtractor guiGraphics, final RenderPipeline renderPipeline, final Identifier location, final int x, final int y, final int width, final int height, final int color) {
        if (playerInfo == null)
            guiGraphics.blitSprite(renderPipeline, location, x, y, width, height, color);
        else {
            guiGraphics.fill(x - size, y - size, x + width + size, y + height + size, color);
            PlayerFaceExtractor.extractRenderState(guiGraphics, playerInfo.getSkin().body().texturePath(),
                x + 1 - size, y + 1 - size, width - 2 + size + size, playerInfo.showHat(), isUpsideDown, -1);
        }
    }

}
