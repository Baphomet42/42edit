package baphomethlabs.fortytwoedit.mixin;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DecoratedPotPatterns.class)
public interface DecoratedPotPatternsAccessor {

	@Accessor("ITEM_TO_POT_TEXTURE")
	public static Map<Item,ResourceKey<DecoratedPotPattern>> getItemToPotTexture() {
        throw new AssertionError();
    }

}
