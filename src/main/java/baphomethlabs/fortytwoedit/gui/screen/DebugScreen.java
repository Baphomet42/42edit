package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FileTools;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DebugScreen extends GenericScreen {
    
    public DebugScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.DEBUG_SCREEN;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> changeScreen(new MagickGui() //to_do return to SecretScreen()
            )).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("View Log..."), button -> changeScreen(new LogScreen())).dimensions(x+20,y+22*2+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Reset Misc"), button -> this.btnRefreshRuntime()).dimensions(x+20,y+22*3+1,80,20).build())
            .setTooltip(Tooltip.of(Text.of("Clear various caches, refresh files, etc.")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Output Hist"), button -> this.btnOutputHist()).dimensions(x+20,y+22*4+1,80,20).build())
            .setTooltip(Tooltip.of(Text.of("Send item history info to log")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Open Dir"), button -> btnOpenDir()).dimensions(x+20,y+22*5+1,80,20).build())
            .setTooltip(Tooltip.of(Text.of("Open 42edit file directory")));
    }

    protected void btnRefreshRuntime() {
        FortytwoEdit.showToast("Reset Misc","Variables reset");
        FortytwoEdit.debugTryRefreshVarious();
        unsel();
    }

    protected void btnOutputHist() {
        FortytwoEdit.showToast("Output Hist", "Item history has been sent to the output log");
        FortytwoEdit.logInfo("Item History: "+FortytwoEdit.getItemHist().asString());
        unsel();
    }

    protected void btnOpenDir() {
        FileTools.openModDir();
        unsel();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Debug Tools"), this.width / 2, y+11, TEXT_COLOR);
    }

}
