package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.HotbarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HotbarManager.class)
public interface HotbarManagerAccessor {

    @Accessor("loaded")
    public void setLoaded(boolean loaded);

}
