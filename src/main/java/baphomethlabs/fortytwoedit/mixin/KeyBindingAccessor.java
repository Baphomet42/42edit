package baphomethlabs.fortytwoedit.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

	@Accessor("KEYS_BY_ID")
	public static Map<String, KeyBinding> getKeysList() {
		throw new AssertionError();
	}

	@Accessor("boundKey")
	InputUtil.Key getBoundKey();

}
