package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.option.HotbarStorage;

@Mixin(HotbarStorage.class)
public interface HotbarStorageAccessor {

	@Accessor("loaded")
	public void setLoaded(boolean loaded);

}
