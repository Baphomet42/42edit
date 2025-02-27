package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin extends AbstractWidget {

    public AbstractScrollAreaMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @ModifyVariable(method = "mouseDragged", at = @At("STORE"), ordinal = 1)
    private int injectedInt(int i) {
        if(i==this.height)
            return 0;
        return i;
    }

}
