package baphomethlabs.fortytwoedit.gui.screen;

import java.time.Duration;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Identifier;

public abstract class GenericScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.of("42edit","textures/generic_gui.png");
    private static final Identifier TEXTURE1 = Identifier.of("42edit","textures/nbt_gui.png");
    protected int backgroundWidth = 12*20;
    protected int backgroundHeight = 9*22;
    protected int x;
    protected int y;
    protected static final int LABEL_COLOR = 0xA0A0A0;
    protected static final int LABEL_COLOR_DIM = 0x404040;
    protected static final int ERROR_COLOR = 0xFF5555;
    protected static final int TEXT_COLOR = 0xFFFFFF;
    protected static final int WID_HEIGHT = 20;
    protected static final int ROW_HEIGHT = 22;
    protected static final Duration TOOLTIP_DELAY = Duration.ofMillis(500L);
    protected static final Duration TOOLTIP_DELAY_SHORT = Duration.ofMillis(100L);
    protected static final int MAX_TEXT_LENGTH = 131072;
    public static final String UNICODE_SECTION_SIGN = "\u00a7";
    public static final String UNICODE_UP_ARROW = "\u2227";
    public static final String UNICODE_DOWN_ARROW = "\u2228";
    public static final String UNICODE_CHECK = "\u2611";
    public static final String UNICODE_X = "\u2612";
    public static final String UNICODE_REFRESH = "\u27F3";
    private boolean unsel = false;
    
    public GenericScreen() {
        super(NarratorManager.EMPTY);
    }

    public void unfocus() {
        GuiNavigationPath guiNavigationPath = this.getFocusedPath();
        if (guiNavigationPath != null) {
            guiNavigationPath.setFocused(false);
        }
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.NONE;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }
    
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, int gui) {
        if(gui==0)
		    context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        else if(gui==1)
            context.drawTexture(RenderLayer::getGuiTextured, TEXTURE1, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean superReturn = super.mouseClicked(mouseX, mouseY, button);
        if(superReturn)
            return true;
        unsel();
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    protected void unsel() {
        unsel = true;
    }

    protected void reloadScreen() {
        this.clearAndInit();
    }

    @Override
    public void tick() {
        if(unsel) {
            unfocus();
            unsel = false;
        }

        super.tick();
    }

}
