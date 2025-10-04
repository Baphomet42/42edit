package baphomethlabs.fortytwoedit.mixin;

import java.util.stream.Stream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.components.debug.DebugEntryLookingAtFluid;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(DebugEntryLookingAtFluid.class)
public abstract class DebugEntryLookingAtFluidMixin {

	@Redirect(method = "display", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getTags()Ljava/util/stream/Stream;"))
	private Stream<TagKey<Fluid>> redirectDisplayTags(FluidState fluidState) {
        if(FortytwoEdit.debugMixinHideBlockTags) {
            return Stream.empty();
        }
        return fluidState.getTags();
	}

}
