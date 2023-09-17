package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

@Mixin(ScrollableWidget.class)
public abstract class ScrollableWidgetMixin extends ClickableWidget {

    public ScrollableWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @ModifyVariable(method="mouseDragged", at=@At("STORE"), ordinal = 1)
    private int injected(int i) {
        if(i==this.height)
            return 0;
        return i;
    }

}
