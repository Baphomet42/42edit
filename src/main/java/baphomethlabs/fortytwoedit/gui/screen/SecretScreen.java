package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SecretScreen extends GenericScreen {
    
    public SecretScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.SECRET_SCREEN;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Output Hist"), button -> this.btnOutputHist()).dimensions(x+20,y+22*3+1,80,20).build())
            .setTooltip(Tooltip.of(Text.of("Send item history info to log")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("View Log..."), button -> 
            client.setScreen(new LogScreen())).dimensions(x+20,y+22*4+1,80,20).build());
    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnOutputHist() {
        FortytwoEdit.LOGGER.info("Item History: "+FortytwoEdit.getItemHist().asString());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Super Secret Settings"), this.width / 2, y+11, 0xFFFFFF);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context, delta, mouseX, mouseY, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            this.client.setScreen(null);
            return true;
        }
        if(super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

}
