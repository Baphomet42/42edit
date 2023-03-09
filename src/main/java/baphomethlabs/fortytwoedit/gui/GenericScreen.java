package baphomethlabs.fortytwoedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public abstract class GenericScreen extends Screen {

    private static final Identifier TEXTURE = new Identifier("42edit", "textures/generic_gui.png");
    protected int backgroundWidth = 12*20;
    protected int backgroundHeight = 9*22;
    protected int x;
    protected int y;
    
    public GenericScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
    }
    
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        GenericScreen.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
