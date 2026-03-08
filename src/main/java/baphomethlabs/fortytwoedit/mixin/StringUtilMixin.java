package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StringUtil.class)
public class StringUtilMixin {

    @ModifyVariable(method = "isAllowedChatCharacter", at = @At("HEAD"), argsOnly = true)
    private static int modifyIsAllowedChatCharacter(int chr) {
        if (chr == '\u00a7')
            return 'S';
        return chr;
    }

}
