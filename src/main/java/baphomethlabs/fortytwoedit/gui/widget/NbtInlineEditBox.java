package baphomethlabs.fortytwoedit.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class NbtInlineEditBox extends EditBox {

    private Tooltip smartTooltip = null;

    public NbtInlineEditBox(Font font, int x, int y, int width, int height, Component component) {
        super(font, x, y, width, height, component);
    }

    public void setSmartTooltip(Tooltip tooltip) {
        smartTooltip = tooltip;
    }

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderWidget(guiGraphics, i, j, f);

        if(smartTooltip != null) {
            if(isFocused())
                this.setTooltip(null);
            else
                this.setTooltip(smartTooltip);
        }
    }
    
}
