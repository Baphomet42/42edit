package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

    @Invoker("setPostEffect")
    public void invokeSetPostEffect(Identifier id);

	@Accessor("effectActive")
	public void setEffectActive(boolean enabled);

}
