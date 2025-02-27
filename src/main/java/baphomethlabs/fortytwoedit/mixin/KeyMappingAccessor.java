package baphomethlabs.fortytwoedit.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

	@Accessor("ALL")
	public static Map<String, KeyMapping> getKeysList() {
		throw new AssertionError();
	}

	@Accessor("CATEGORY_SORT_ORDER")
	public static Map<String, Integer> getCategorySortOrder() {
		throw new AssertionError();
	}

	@Accessor("key")
	InputConstants.Key getBoundKey();

}
