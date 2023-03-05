package io.github.cottonmc.cotton.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A multiline label widget.
 *
 * @since 1.8.0
 */
public class WText extends WWidget {
	protected Text text;
	protected int color;
	protected int darkmodeColor;
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
	protected VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
	@Environment(EnvType.CLIENT)
	private List<OrderedText> wrappedLines;
	private boolean wrappingScheduled = false;

	public WText(Text text) {
		this(text, WLabel.DEFAULT_TEXT_COLOR);
	}

	public WText(Text text, int color) {
		this.text = Objects.requireNonNull(text, "text must not be null");
		this.color = color;
		this.darkmodeColor = (color == WLabel.DEFAULT_TEXT_COLOR) ? WLabel.DEFAULT_DARKMODE_TEXT_COLOR : color;
	}

	@Override
	public void setSize(int x, int y) {
		super.setSize(x, y);
		wrappingScheduled = true;
	}

	@Override
	public boolean canResize() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	private void wrapLines() {
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		wrappedLines = font.wrapLines(text, width);
	}

	/**
	 * Gets the text style at the specific widget-space coordinates.
	 *
	 * @param x the X coordinate in widget space
	 * @param y the Y coordinate in widget space
	 * @return the text style at the position, or null if not found
	 */
	@Environment(EnvType.CLIENT)
	@Nullable
	public Style getTextStyleAt(int x, int y) {
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		int lineIndex = y / font.fontHeight;

		if (lineIndex >= 0 && lineIndex < wrappedLines.size()) {
			OrderedText line = wrappedLines.get(lineIndex);
			return font.getTextHandler().getStyleAt(line, x);
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		if (wrappedLines == null || wrappingScheduled) {
			wrapLines();
			wrappingScheduled = false;
		}

		TextRenderer font = MinecraftClient.getInstance().textRenderer;

		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - font.fontHeight * wrappedLines.size() / 2;
			case BOTTOM -> height - font.fontHeight * wrappedLines.size();
			case TOP -> 0;
		};

		for (int i = 0; i < wrappedLines.size(); i++) {
			OrderedText line = wrappedLines.get(i);
			int c = LibGui.isDarkMode() ? darkmodeColor : color;

			ScreenDrawing.drawString(matrices, line, horizontalAlignment, x, y + yOffset + i * font.fontHeight, width, c);
		}

		Style hoveredTextStyle = getTextStyleAt(mouseX, mouseY);
		ScreenDrawing.drawTextHover(matrices, hoveredTextStyle, x + mouseX, y + mouseY);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public InputResult onClick(int x, int y, int button) {
		if (button != 0) return InputResult.IGNORED; // only left clicks

		Style hoveredTextStyle = getTextStyleAt(x, y);
		if (hoveredTextStyle != null) {
			boolean processed = MinecraftClient.getInstance().currentScreen.handleTextClick(hoveredTextStyle);
			return InputResult.of(processed);
		}

		return InputResult.IGNORED;
	}

	/**
	 * Gets the text of this label.
	 *
	 * @return the text
	 */
	public Text getText() {
		return text;
	}

	/**
	 * Sets the text of this label.
	 *
	 * @param text the new text
	 * @return this label
	 */
	public WText setText(Text text) {
		Objects.requireNonNull(text, "text is null");
		this.text = text;
		wrappingScheduled = true;

		return this;
	}

	/**
	 * Gets the light mode color of this label.
	 *
	 * @return the color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * Sets the light mode color of this label.
	 *
	 * @param color the new color
	 * @return this text widget
	 */
	public WText setColor(int color) {
		this.color = color;
		return this;
	}

	/**
	 * Gets the dark mode color of this label.
	 *
	 * @return the color
	 * @since 2.0.0
	 */
	public int getDarkmodeColor() {
		return darkmodeColor;
	}

	/**
	 * Sets the dark mode color of this label.
	 *
	 * @param darkmodeColor the new color
	 * @return this text widget
	 */
	public WText setDarkmodeColor(int darkmodeColor) {
		this.darkmodeColor = darkmodeColor;
		return this;
	}

	/**
	 * Sets the light and dark mode colors of this label.
	 *
	 * @param color         the new light color
	 * @param darkmodeColor the new dark color
	 * @return this text widget
	 */
	public WText setColor(int color, int darkmodeColor) {
		setColor(color);
		setDarkmodeColor(darkmodeColor);
		return this;
	}

	/**
	 * Disables separate dark mode coloring by copying the dark color to be the light color.
	 *
	 * @return this text widget
	 */
	public WText disableDarkmode() {
		this.darkmodeColor = this.color;
		return this;
	}

	/**
	 * Gets the horizontal alignment of this text widget.
	 *
	 * @return the alignment
	 * @since 1.9.0
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * Sets the horizontal alignment of this text widget.
	 *
	 * @param horizontalAlignment the new alignment
	 * @return this widget
	 * @since 1.9.0
	 */
	public WText setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		return this;
	}

	/**
	 * Gets the vertical alignment of this text widget.
	 *
	 * @return the alignment
	 * @since 2.0.0
	 */
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * Sets the vertical alignment of this text widget.
	 *
	 * @param verticalAlignment the new alignment
	 * @return this widget
	 * @since 2.0.0
	 */
	public WText setVerticalAlignment(VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void addNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, text);
	}
}
