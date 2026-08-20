package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

    @Invoker("setSpectatedEntityPostEffect")
    public void invokeSetSpectatedEntityPostEffect(Identifier id);

    @Accessor("spectatedEntityEffectActive")
    public void setSpectatedEntityEffectActive(boolean enabled);

}
