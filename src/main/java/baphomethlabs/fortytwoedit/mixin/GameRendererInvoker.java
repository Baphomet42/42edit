package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

    @Invoker("setPostProcessor")
    public void invokeSetPostProcessor(Identifier id);

	@Accessor("postProcessorEnabled")
	public void setPostProcessorEnabled(boolean enabled);
    
}
