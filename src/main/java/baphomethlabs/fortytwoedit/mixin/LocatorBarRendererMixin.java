package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
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

@Mixin(LocatorBarRenderer.class)
public abstract class LocatorBarRendererMixin {

    private static PlayerInfo playerInfo = null;
    private static boolean isUpsideDown = false;
    private static int size = 0;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"), cancellable = true)
    public void injectRender(Entity entity, Level level, PartialTickSupplier partialTickSupplier, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {

        playerInfo = null;
        if (FortytwoEdit.mixinLocatorBar
        && (trackedWaypoint.icon().style == WaypointStyleAssets.DEFAULT || FortytwoEdit.mixinLocatorBarAlways)
        && trackedWaypoint.id().left().isPresent()) {
            final Minecraft minecraft = Minecraft.getInstance();
            playerInfo = minecraft.player.connection.getPlayerInfo(trackedWaypoint.id().left().get());
            Player player = minecraft.level.getPlayerByUUID(playerInfo.getProfile().id());
            isUpsideDown = player != null && AvatarRenderer.isPlayerUpsideDown(player);

            WaypointStyle waypointStyle = minecraft.getWaypointStyles().get(trackedWaypoint.icon().style);
            float f = Mth.sqrt((float)trackedWaypoint.distanceSquared(entity));
            size = 0;
            if (f < waypointStyle.nearDistance())
                size = 1;
            else if (f >= waypointStyle.farDistance())
                size = -1;
        }

    }

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
	private void redirectBlitSprite(GuiGraphics guiGraphics, RenderPipeline renderPipeline, Identifier identifier, int i, int j, int k, int l, int m) {
        if (playerInfo == null)
            guiGraphics.blitSprite(renderPipeline, identifier, i, j, k, l, m);
        else {
            if (FortytwoEdit.mixinLocatorBarColor)
                guiGraphics.fill(i-size, j-size, i+k+size, j+k+size, m);
            PlayerFaceRenderer.draw(guiGraphics, playerInfo.getSkin().body().texturePath(), i+1-size, j+1-size, k-2+size+size, playerInfo.showHat(), isUpsideDown, -1);
        }
	}
    
}
