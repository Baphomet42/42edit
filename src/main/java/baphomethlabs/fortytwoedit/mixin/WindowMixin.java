package baphomethlabs.fortytwoedit.mixin;

import java.io.IOException;
import java.io.InputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;

@Mixin(Window.class)
public abstract class WindowMixin {

    @Redirect(method = "setIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;read(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;"))
    private NativeImage redirectIcon(InputStream stream) {
        try {
            return NativeImage.read(getClass().getClassLoader().getResourceAsStream("assets/42edit/textures/icon/mycelium.png"));
        } catch(IOException ex) {
            FortytwoEdit.logError("Failed to set game icon");
        }
        return new NativeImage(16,16,true);
    }

}
