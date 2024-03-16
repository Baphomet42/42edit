package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.util.StringHelper;

@Mixin(StringHelper.class)
public class StringHelperMixin {
    @ModifyVariable(method="isValidChar(C)Z", at=@At("HEAD"), ordinal=0)
    private static char injected(char chr) {
        if(chr == '\u00a7')
            return 'S';
        return chr;
    }

}