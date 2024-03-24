package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Identifier;

public abstract class GenericScreen extends Screen {

    private static final Identifier TEXTURE = new Identifier("42edit", "textures/generic_gui.png");
    private static final Identifier TEXTURE1 = new Identifier("42edit", "textures/nbt_gui.png");
    protected int backgroundWidth = 12*20;
    protected int backgroundHeight = 9*22;
    protected int x;
    protected int y;
    protected static final int LABEL_COLOR = 0xA0A0A0;
    
    public GenericScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        FortytwoEdit.quickScreen = 0;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
    }
    
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, int gui) {
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        if(gui==0)
            context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        else if(gui==1)
            context.drawTexture(TEXTURE1, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
