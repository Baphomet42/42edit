package baphomethlabs.fortytwoedit.gui.widget;

import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class WidgetUtil {

    private final GenericScreen SCREEN;

    public WidgetUtil(GenericScreen screen) {
        this.SCREEN = screen;
    }

    /**
     * Get widget size based on text (between 40 and 100 pixels).
     * Size is a multiple of 20.
     * 
     * @param text
     * @return width
     */
    public static int sizeFromName(GenericScreen screen, String text) {
        return sizeFromName(screen, Component.nullToEmpty(text));
    }

    /**
     * Get widget size based on text (between 40 and 100 pixels).
     * Size is a multiple of 20.
     * 
     * @param text
     * @return width
     */
    public static int sizeFromName(GenericScreen screen, Component text) {
        return sizeFromName(screen, text, 100);
    }

    /**
     * Get widget size based on text (between 40 and `maxWidth` pixels).
     * Size is a multiple of 20.
     * 
     * @param text
     * @return width
     */
    public static int sizeFromName(GenericScreen screen, String text, int maxWidth) {
        return sizeFromName(screen, Component.nullToEmpty(text), maxWidth);
    }

    /**
     * Get widget size based on text (between 40 and `maxWidth` pixels).
     * Size is a multiple of 20.
     * 
     * @param text
     * @return width
     */
    public static int sizeFromName(GenericScreen screen, Component text, int maxWidth) {
        int size = 40;
        int min = screen.getFont().width(text)+4;
        while(min>size && size<maxWidth) {
            size += 20;
        }
        return size;
    }

    public static abstract class AbstractWidgetBuilder<T extends AbstractWidget, SELF extends AbstractWidgetBuilder<T, SELF>> {

        protected T w;

        protected abstract SELF self();

        private AbstractWidgetBuilder(T w) {
            this.w = w;
            w.setSize(GenericScreen.WID_HEIGHT, GenericScreen.WID_HEIGHT);
        }

        public SELF setSize(int width) {
            w.setSize(width, GenericScreen.WID_HEIGHT);
            return self();
        }

        public SELF setSize(int width, int height) {
            w.setSize(width, height);
            return self();
        }

        public SELF setTooltip(String tooltip) {
            return setTooltip(Component.nullToEmpty(tooltip));
        }

        public SELF setTooltip(Component tooltip) {
            return setTooltip(Tooltip.create(tooltip));
        }

        public SELF setTooltip(Tooltip tooltip) {
            w.setTooltip(tooltip);
            return self();
        }

        public T build() {
            return w;
        }

    }

    public static class ButtonBuilder extends AbstractWidgetBuilder<Button, ButtonBuilder> {

        @Override
        protected ButtonBuilder self() {
            return this;
        }

        private ButtonBuilder(GenericScreen screen, Component label, Button.OnPress onPress) {
            super(Button.builder(label, onPress).build());
            setSize(sizeFromName(screen, label));
        }

        public ButtonBuilder setActive(boolean active) {
            w.active = active;
            return this;
        }

    }

    public ButtonBuilder newButton(String label, Button.OnPress onPress) {
        return new ButtonBuilder(SCREEN, Component.nullToEmpty(label), onPress);
    }

    public ButtonBuilder newButton(Component label, Button.OnPress onPress) {
        return new ButtonBuilder(SCREEN, label, onPress);
    }

    public static class EditBoxBuilder extends AbstractWidgetBuilder<EditBox, EditBoxBuilder> {

        @Override
        protected EditBoxBuilder self() {
            return this;
        }

        private EditBoxBuilder(GenericScreen screen, int width) {
            super(new EditBox(screen.getFont(), 0, 0, GenericScreen.WID_HEIGHT, GenericScreen.WID_HEIGHT, Component.empty()));
            w.setMaxLength(GenericScreen.MAX_TEXT_LENGTH);
            setSize(width);
        }

        public EditBoxBuilder setMaxLength(int maxLength) {
            w.setMaxLength(maxLength);
            return this;
        }

    }

    public EditBoxBuilder newEditBox(int width) {
        return new EditBoxBuilder(SCREEN, width);
    }

}
