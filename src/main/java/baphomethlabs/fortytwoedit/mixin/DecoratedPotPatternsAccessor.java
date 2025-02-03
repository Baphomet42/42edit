package baphomethlabs.fortytwoedit.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.block.DecoratedPotPattern;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

@Mixin(DecoratedPotPatterns.class)
public interface DecoratedPotPatternsAccessor {

	@Accessor("SHERD_TO_PATTERN")
	public static Map<Item,RegistryKey<DecoratedPotPattern>> getSherdToPattern() {
        throw new AssertionError();
    }

}
