package baphomethlabs.fortytwoedit.mixin;

import java.util.stream.Stream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.components.debug.DebugEntryLookingAtBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(DebugEntryLookingAtBlock.class)
public abstract class DebugEntryLookingAtBlockMixin {
    
	@Redirect(method = "display", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getTags()Ljava/util/stream/Stream;"))
	private Stream<TagKey<Block>> redirectDisplayTags(BlockState blockState) {
        if(FortytwoEdit.debugMixinHideBlockTags) {
            return Stream.empty();
        }
        return blockState.getTags();
	}

}
