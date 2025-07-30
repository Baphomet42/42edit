package baphomethlabs.fortytwoedit.gui.screen;

import java.time.Duration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public abstract class GenericScreen extends Screen {

    protected static final ResourceLocation TEXTURE_GENERIC = ResourceLocation.fromNamespaceAndPath("42edit","gui/generic");
    protected static final ResourceLocation TEXTURE_MENU_BAR = ResourceLocation.fromNamespaceAndPath("42edit","gui/menu_bar");
    protected int backgroundWidth = 12*20;
    protected int backgroundHeight = 9*22;
    protected int x;// to_do rename to leftPos and topPos (see AbstractContainerScreen)
    protected int y;
    protected static final int LABEL_COLOR = 0xFFA0A0A0;
    protected static final int LABEL_COLOR_DIM = 0xFF404040;
    protected static final int ERROR_COLOR = 0xFFFF5555;
    protected static final int TEXT_COLOR = 0xFFFFFFFF;
    protected static final int WID_HEIGHT = 20; // standard widget height
    protected static final int ROW_HEIGHT = 22; // standard spacing amounts between rows of widgets
    protected static final int TOP_OFFSET = (ROW_HEIGHT-WID_HEIGHT)/2;
    protected static final int WID_SPACE = 5; // standard horizontal spacing between widgets
    protected static final int GUI_SPACE = 5; // standard starting position for widget in top corner of gui (for both x and y)
    protected static final int WID_LEFT = 20; // standard spacing before first widget in row
    protected static final Duration TOOLTIP_DELAY = Duration.ofMillis(500L);
    protected static final Duration TOOLTIP_DELAY_SHORT = Duration.ofMillis(100L);
    protected static final int MAX_TEXT_LENGTH = 131072;
    public static final String UNICODE_SECTION_SIGN = "\u00a7";
    public static final String UNICODE_UP_ARROW = "\u2227";
    public static final String UNICODE_DOWN_ARROW = "\u2228";
    public static final String UNICODE_CHECK = "\u2611";
    public static final String UNICODE_X = "\u2612";
    public static final String UNICODE_REFRESH = "🗘";
    public static final Component ERROR_CREATIVE = Component.empty().append("Creative required").withStyle(ChatFormatting.RED);
    public static final Tooltip TT_CREATIVE = Tooltip.create(ERROR_CREATIVE);
    private boolean unsel = false;

    public GenericScreen() {
        super(GameNarrator.NO_TITLE);
    }

    public boolean shouldCloseOnKeybind() {
        return true;
    }

    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE_GENERIC;
    }

    protected void renderBehindBackgroundTexture(GuiGraphics context) {}

    protected void changeScreen(Screen newScreen) {
        this.onCloseAction();
        minecraft.setScreen(newScreen);
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.NONE;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(context);
        this.renderBehindBackgroundTexture(context);

        ResourceLocation backgroundTexture = getBackgroundTexture();
        if(backgroundTexture != null)
		    context.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean doubleTap) {
        if(super.mouseClicked(mouseX, mouseY, button, doubleTap))
            return true;
        unsel();
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(FortytwoEdit.keyMagickGui.matches(keyCode,scanCode) || minecraft.options.keyInventory.matches(keyCode,scanCode)) {
            if(shouldCloseOnKeybind()) {
                this.onClose();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void unsel() {
        unsel = true;
    }

    protected void reloadScreen() {
        this.rebuildWidgets();
    }

    protected void onCloseAction() {}

    @Override
    public void onClose() {
        onCloseAction();
        super.onClose();
    }

    @Override
    public void tick() {
        if(unsel) {
            clearFocus();
            unsel = false;
        }

        super.tick();
    }

}
