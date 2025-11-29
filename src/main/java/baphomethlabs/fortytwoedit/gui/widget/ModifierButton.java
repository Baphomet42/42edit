package baphomethlabs.fortytwoedit.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ModifierButton extends Button.Plain {

	protected final ModifierButton.OnPressInput onPressInput;

    public ModifierButton(Component message, OnPressInput onPressInput, int x, int y, int width, int height) {
		super(x, y, width, height, message, button -> {}, Button.DEFAULT_NARRATION);
        this.onPressInput = onPressInput;
    }

	@Override
	public void onPress(InputWithModifiers inputWithModifiers) {
		this.onPressInput.onPress(this, inputWithModifiers);
	}

	@Environment(EnvType.CLIENT)
	public interface OnPressInput {
		void onPress(Button button, InputWithModifiers inputWithModifiers);
	}

}
