package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.SharedConstants;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
 
    @ModifyVariable(method="isValidChar(C)Z", at=@At("HEAD"), ordinal=0)
    private static char injected(char chr) {
        if(chr == '\u00a7')
            return 'S';
        return chr;
    }

}