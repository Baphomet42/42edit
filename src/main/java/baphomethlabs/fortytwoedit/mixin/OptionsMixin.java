package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import java.util.Map;
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

	@Inject(method = "load", at = @At("HEAD"))
	private void appendModBindings(CallbackInfo ci) {
		List<KeyMapping> allKeysList = Lists.newArrayList(keyMappings);
        Map<String, Integer> categoryOrderMap = KeyMappingAccessor.getCategorySortOrder();
        List<String> newCategories = Lists.newArrayList();

        for(KeyMapping k : FortytwoEdit.KEYBINDS) {
            if(!allKeysList.contains(k))
                allKeysList.add(k);
            if(!categoryOrderMap.containsKey(k.getCategory()))
                newCategories.add(k.getCategory());
        }
        int maxCategory = 0;
        for(String c : categoryOrderMap.keySet())
           maxCategory = Math.max(maxCategory,categoryOrderMap.get(c));
        for(String c : newCategories) {
            maxCategory++;
            categoryOrderMap.put(c,maxCategory);
        }

		keyMappings = allKeysList.toArray(new KeyMapping[0]);
	}

	@Redirect(method = "processOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
	private KeyMapping[] removeModBindings(Options gameOptions) {
		List<KeyMapping> allKeysList = Lists.newArrayList(keyMappings);

        for(KeyMapping k : FortytwoEdit.KEYBINDS)
            allKeysList.remove(k);

		return allKeysList.toArray(new KeyMapping[0]);
	}

    @Inject(method = "save", at = @At("RETURN"))
    private void saveModKeybinds(CallbackInfo ci) {
        FortytwoEdit.saveKeybindOptions();
    }

}
