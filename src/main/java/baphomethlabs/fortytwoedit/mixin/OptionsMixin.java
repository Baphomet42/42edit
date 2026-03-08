package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(Options.class)
public abstract class OptionsMixin {

	@Mutable
	@Final
	@Shadow
	public KeyMapping[] keyMappings;

	@Redirect(method = "processOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
	private KeyMapping[] removeModBindings(Options gameOptions) {
		List<KeyMapping> allKeysList = Lists.newArrayList(keyMappings);

        for (KeyMapping k : FortytwoEdit.KEYBINDS)
            allKeysList.remove(k);

		return allKeysList.toArray(new KeyMapping[0]);
	}

    @Inject(method = "save", at = @At("RETURN"))
    private void saveModKeybinds(CallbackInfo ci) {
        FortytwoEdit.saveKeybindOptions();
    }

}
