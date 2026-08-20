package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;

@Mixin(Window.class)
public abstract class WindowMixin {

    @ModifyArg(method = "createIconSurface(Lnet/minecraft/server/packs/resources/IoSupplier;Ljava/util/List;)Lorg/lwjgl/sdl/SDL_Surface;", at = @At(value = "INVOKE", target = "createIconSurface(Lcom/mojang/blaze3d/platform/NativeImage;)Lorg/lwjgl/sdl/SDL_Surface;"))
    private static NativeImage modifyCreateIconSurfaceNativeImage(NativeImage image) {
        try {
            return NativeImage.read(WindowMixin.class.getClassLoader().getResourceAsStream("assets/42edit/textures/icon/mycelium.png"));
        }
        catch (Exception ex) {
            FortytwoEdit.logError("Failed to set game icon");
        }
        return image;
    }

}
