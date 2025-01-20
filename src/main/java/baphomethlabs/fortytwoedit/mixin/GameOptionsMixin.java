package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import java.util.Map;

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
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

	@Mutable
	@Final
	@Shadow
	public KeyBinding[] allKeys;

	@Inject(method = "load", at = @At("HEAD"))
	private void appendModBindings(CallbackInfo ci) {
		List<KeyBinding> allKeysList = Lists.newArrayList(allKeys);
        Map<String, Integer> categoryOrderMap = KeyBindingAccessor.getCategoryOrderMap();
        List<String> newCategories = Lists.newArrayList();

        for(KeyBinding k : FortytwoEdit.KEYBINDS) {
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

		allKeys = allKeysList.toArray(new KeyBinding[0]);
	}

	@Redirect(method = "accept", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;allKeys:[Lnet/minecraft/client/option/KeyBinding;"))
	private KeyBinding[] removeModBindings(GameOptions gameOptions) {
		List<KeyBinding> allKeysList = Lists.newArrayList(allKeys);

        for(KeyBinding k : FortytwoEdit.KEYBINDS)
            allKeysList.remove(k);
        
		return allKeysList.toArray(new KeyBinding[0]);
	}

    @Inject(method = "write", at = @At("RETURN"))
    private void saveModKeybinds(CallbackInfo ci) {
        FortytwoEdit.saveKeybindOptions();
    }

}
