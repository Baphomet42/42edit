package baphomethlabs.fortytwoedit.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SmartEditBox extends EditBox {

    private Tooltip smartTooltip = null;
	private Consumer<String> clickResponder = null;

    public SmartEditBox(Font font, int x, int y, int width, int height, Component component) {
        super(font, x, y, width, height, component);
    }

    public void setSmartTooltip(Tooltip tooltip) {
        this.smartTooltip = tooltip;
    }

    public void setSuggsResponder(final Consumer<String> responder) {
		this.clickResponder = responder;
        this.setResponder(responder);
	}

    private void handleClickResponder() {
		if (this.clickResponder != null) {
			this.clickResponder.accept(this.getValue());
		}
    }

	@Override
	public void onClick(final MouseButtonEvent event, final boolean doubleClick) {
        super.onClick(event, doubleClick);

        this.handleClickResponder();
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        super.extractWidgetRenderState(guiGraphics, i, j, f);

        if (this.smartTooltip != null) {
            if (isFocused())
                this.setTooltip(null);
            else
                this.setTooltip(this.smartTooltip);
        }
    }
    
}
