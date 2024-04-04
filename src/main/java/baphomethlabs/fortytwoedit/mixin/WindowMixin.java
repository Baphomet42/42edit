package baphomethlabs.fortytwoedit.mixin;

import java.io.IOException;
import java.io.InputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;

@Mixin(Window.class)
public abstract class WindowMixin {

    @Redirect(method="setIcon", at=@At(value="INVOKE", target="Lnet/minecraft/client/texture/NativeImage;read(Ljava/io/InputStream;)Lnet/minecraft/client/texture/NativeImage;"))
    private NativeImage returnIcon(InputStream stream) {

        try{
            if(getClass().getClassLoader().getResourceAsStream("assets/42edit/icon.png") != null)
                return NativeImage.read(getClass().getClassLoader().getResourceAsStream("assets/42edit/game_icon/mycelium.png"));
        }catch(IOException e) {}
        return new NativeImage(16,16,true);
    }

}
