package baphomethlabs.fortytwoedit.gui.widget;

import java.time.Duration;
import java.util.function.Consumer;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WidgetUtil {

    protected final Minecraft MINECRAFT;
    protected final GenericScreen SCREEN;

    public WidgetUtil(Minecraft minecraft, GenericScreen screen) {
        this.MINECRAFT = minecraft;
        this.SCREEN = screen;
    }

    public static abstract class AbstractWidgetBuilder<T extends AbstractWidget, SELF extends AbstractWidgetBuilder<T, SELF>> {

        protected T w;
        protected final Minecraft minecraft;
        protected final GenericScreen screen;
        protected static final Component LABEL_OPTION_ON = Component.nullToEmpty(" [On]");
        protected static final Component LABEL_OPTION_OFF = Component.nullToEmpty(" [Off]");

        protected abstract SELF self();

        private AbstractWidgetBuilder(Minecraft minecraft, GenericScreen screen, T w) {
            this.minecraft = minecraft;
            this.screen = screen;
            this.w = w;
            w.setSize(GenericScreen.WID_HEIGHT, GenericScreen.WID_HEIGHT);
        }

        public SELF setPosition(int x, int y) { // to_do remove
            w.setPosition(x, y);
            return self();
        }

        public SELF setSize(int width) {
            return setSize(width, w.getHeight());
        }

        public SELF setSize(int width, int height) {
            w.setSize(Math.max(width, GenericScreen.WID_MIN_WIDTH), height);
            return self();
        }

        public SELF fullWidth() {
            return setSize(GenericScreen.WID_WIDTH_FULL);
        }

        public SELF setTooltip(String tooltip) {
            return setTooltip(Component.nullToEmpty(tooltip));
        }

        public SELF setTooltip(Component tooltip) {
            return setTooltip(Tooltip.create(tooltip));
        }

        public SELF setTooltip(Tooltip tooltip) {
            w.setTooltip(tooltip);
            setTooltipDelayStandard();
            return self();
        }

        public SELF setTooltipDelay(Duration delay) {
            w.setTooltipDelay(delay);
            return self();
        }

        public SELF setTooltipDelayStandard() {
            return setTooltipDelay(GenericScreen.TOOLTIP_DELAY);
        }

        public SELF setTooltipDelayShort() {
            return setTooltipDelay(GenericScreen.TOOLTIP_DELAY_SHORT);
        }

        public SELF setTooltipDelayNone() {
            return setTooltipDelay(Duration.ZERO);
        }

        public SELF runWithSelf(Consumer<T> method) {
            method.accept(w);
            return self();
        }

        public T build() {
            return w;
        }

    }

    public static class ButtonBuilder extends AbstractWidgetBuilder<SmartButton, ButtonBuilder> {

        @Override
        protected ButtonBuilder self() {
            return this;
        }

        private ButtonBuilder(Minecraft minecraft, GenericScreen screen, Component label, int width, Button.OnPress onPress) {
            super(minecraft, screen, new SmartButton(screen, label, onPress));
            setSize(width);
        }

        private ButtonBuilder(Minecraft minecraft, GenericScreen screen, Component label, int width, SmartButton.OnPressInput onPressInput) {
            super(minecraft, screen, new SmartButton(screen, label, onPressInput));
            setSize(width);
        }

        public ButtonBuilder setActive(boolean active) {
            w.active = active;
            return this;
        }

        public ButtonBuilder setRenderItem(Item item) {
            return setRenderItem(new ItemStack(item));
        }

        public ButtonBuilder setRenderItem(ItemStack item) {
            w.setRenderItem(item);
            return this;
        }

        public ButtonBuilder setBoolName(boolean condition) {
            this.w.setMessage(Component.empty().append(this.w.getMessage()).append(condition ? LABEL_OPTION_ON : LABEL_OPTION_OFF));
            return this;
        }

        public ButtonBuilder creativeOnly(String tooltip) {
            if (tooltip == null)
                return creativeOnly();

            return creativeOnly(Component.nullToEmpty(tooltip));
        }

        public ButtonBuilder creativeOnly(Component tooltip) {
            if (tooltip == null)
                return creativeOnly();

            this.w.creativeOnly();
            if (!BlackMagick.isCreative(minecraft)) {
                this.w.active = false;
                return setTooltip(Component.empty().append(GenericScreen.ERROR_CREATIVE).append("\n\n").append(tooltip));
            }
            return setTooltip(tooltip);
        }

        public ButtonBuilder creativeOnly() {
            this.w.creativeOnly();
            if (!BlackMagick.isCreative(minecraft)) {
                this.w.active = false;
                return setTooltip(GenericScreen.TT_CREATIVE);
            }
            return this;
        }

    }

    public ButtonBuilder newButton(String label, Button.OnPress onPress) {
        return newButton(Component.nullToEmpty(label), onPress);
    }

    public ButtonBuilder newButton(Component label, Button.OnPress onPress) {
        return new ButtonBuilder(MINECRAFT, SCREEN, label, GenericScreen.WID_WIDTH_HALF, onPress);
    }

    public ButtonBuilder newButton(String label, SmartButton.OnPressInput onPressInput) {
        return newButton(Component.nullToEmpty(label), onPressInput);
    }

    public ButtonBuilder newButton(Component label, SmartButton.OnPressInput onPressInput) {
        return new ButtonBuilder(MINECRAFT, SCREEN, label, GenericScreen.WID_WIDTH_HALF, onPressInput);
    }

    public static class EditBoxBuilder extends AbstractWidgetBuilder<SmartEditBox, EditBoxBuilder> {

        @Override
        protected EditBoxBuilder self() {
            return this;
        }

        private EditBoxBuilder(Minecraft minecraft, GenericScreen screen, int width) {
            super(minecraft, screen, new SmartEditBox(screen.getFont(), 0, 0, GenericScreen.WID_HEIGHT, GenericScreen.WID_HEIGHT, Component.empty()));
            w.setMaxLength(GenericScreen.MAX_TEXT_LENGTH);
            setSize(width);
        }

        public EditBoxBuilder setMaxLength(int maxLength) {
            w.setMaxLength(maxLength);
            return this;
        }

        public EditBoxBuilder setValue(String value) {
            if (value != null)
                this.w.setValue(value);
            return this;
        }

        public EditBoxBuilder setResponder(final Consumer<String> responder) {
            this.w.setResponder(responder);
            return this;
        }

        public EditBoxBuilder setSmartTooltip(String tooltip) {
            return setSmartTooltip(Component.nullToEmpty(tooltip));
        }

        public EditBoxBuilder setSmartTooltip(Component tooltip) {
            return setSmartTooltip(Tooltip.create(tooltip));
        }

        public EditBoxBuilder setSmartTooltip(Tooltip tooltip) {
            w.setSmartTooltip(tooltip);
            setTooltip((Tooltip)null);
            return self();
        }

        public EditBoxBuilder moveCursorToStart(boolean hasShiftDown) {
            w.moveCursorToStart(hasShiftDown);
            return self();
        }

        public EditBoxBuilder setEditable(boolean isEditable) {
            w.setEditable(isEditable);
            return self();
        }

    }

    public EditBoxBuilder newEditBox() {
        return new EditBoxBuilder(MINECRAFT, SCREEN, GenericScreen.WID_WIDTH_HALF);
    }

}
