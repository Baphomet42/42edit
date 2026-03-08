package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;

@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {

    @Shadow
    @Final
	protected List<Identifier> currentlyEnabled;

    private static final Identifier[] MOVE_LAST = {
        Identifier.withDefaultNamespace("looking_at_entity"),
        Identifier.withDefaultNamespace("looking_at_fluid"),
        Identifier.withDefaultNamespace("looking_at_block")
    };
    
    @Inject(method = "rebuildCurrentList", at = @At("RETURN"), cancellable = true)
	public void injectRebuildCurrentList(CallbackInfo ci) {
        if (FortytwoEdit.debugMixinRearrange) {
            for (Identifier rl : MOVE_LAST) {
                if (currentlyEnabled.contains(rl)) {
                    currentlyEnabled.remove(rl);
                    currentlyEnabled.add(rl);
                }
            }
        }
	}

}
