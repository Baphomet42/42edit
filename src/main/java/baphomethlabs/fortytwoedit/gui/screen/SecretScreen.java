package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SecretScreen extends GenericScreen {
    
    public SecretScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.SECRET_SCREEN;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> changeScreen(new MagickGui())).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Debug Tools..."), button -> changeScreen(new DebugScreen())).dimensions(x+20,y+22*3+1,80,20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Super Secret Settings"), this.width / 2, y+11, TEXT_COLOR);
    }

}
