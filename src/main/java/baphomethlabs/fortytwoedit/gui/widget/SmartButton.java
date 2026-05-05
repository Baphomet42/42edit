package baphomethlabs.fortytwoedit.gui.widget;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SmartButton extends Button.Plain {

	protected final GenericScreen screen;
	protected final SmartButton.OnPressInput onPressInput;
	private ItemStack renderLeftItem = null;
	private boolean creativeOnly = false;

    public SmartButton(GenericScreen screen, Component message, OnPressInput onPressInput) {
		super(0, 0, GenericScreen.WID_WIDTH_HALF, GenericScreen.WID_HEIGHT, message, button -> {}, Button.DEFAULT_NARRATION);
		this.screen = screen;
        this.onPressInput = onPressInput;
    }

	public SmartButton(GenericScreen screen, Component message, Button.OnPress onPress) {
		super(0, 0, GenericScreen.WID_WIDTH_HALF, GenericScreen.WID_HEIGHT, message, button -> {}, Button.DEFAULT_NARRATION);
		this.screen = screen;
        this.onPressInput = (button, inputWithModifiers) -> {onPress.onPress(button);};
	}

	public SmartButton setRenderItem(Item item) {
		return setRenderItem(new ItemStack(item));
	}

	public SmartButton setRenderItem(ItemStack item) {
		this.renderLeftItem = item;
		return this;
	}

	public SmartButton creativeOnly() {
		this.creativeOnly = true;
		return this;
	}

	@Override
	public void onPress(InputWithModifiers inputWithModifiers) {
		if (this.creativeOnly && !BlackMagick.isCreative())
			FortytwoEdit.showToastCreativeError();
		else
			this.onPressInput.onPress(this, inputWithModifiers);
		screen.unsel();
	}

	@Environment(EnvType.CLIENT)
	public interface OnPressInput {
		void onPress(Button button, InputWithModifiers inputWithModifiers);
	}

	@Override
	protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		this.extractDefaultSprite(graphics);

		int margin = 2;
		int left = this.getX() + margin;
		int right = this.getX() + this.getWidth() - margin;
		int top = this.getY();
		int bottom = this.getY() + this.getHeight();
		if (this.renderLeftItem != null) {
			left += 17;
			graphics.fakeItem(this.renderLeftItem,this.getX() + 2, this.getY() + 2);
		}
		graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(this.getMessage(), left, right, top, bottom);
	}

}
