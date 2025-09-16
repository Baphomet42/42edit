package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FileTools;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class DebugScreen extends GenericScreen {

    public DebugScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.DEBUG_SCREEN;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new SecretScreen())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("View Log..."), button -> changeScreen(new LogScreen())).bounds(x+20,y+ROW_HEIGHT*2+1,80,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Reset Misc"), button -> this.btnRefreshRuntime()).bounds(x+20,y+ROW_HEIGHT*3+1,80,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Clear various caches, refresh files, etc.")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("View Hist..."), button -> changeScreen(new ItemHistoryScreen())).bounds(x+20,y+ROW_HEIGHT*4+1,80,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("View item editor history")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Copy Hist"), button -> this.btnOutputHist()).bounds(x+20+80+5,y+ROW_HEIGHT*4+1,80,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Copy item history info and send to log")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Open Dir"), button -> btnOpenDir()).bounds(x+20,y+ROW_HEIGHT*5+1,80,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Open 42edit file directory")));
    }

    protected void btnRefreshRuntime() {
        FortytwoEdit.showToast("Reset Misc","Variables reset");
        FortytwoEdit.debugTryRefreshVarious();
        unsel();
    }

    protected void btnOutputHist() {
        String hist = BlackMagick.nbtToSnbt(FortytwoEdit.getItemHist());
        FortytwoEdit.setClipboard(hist);
        FortytwoEdit.logInfo("Item History: "+hist);
        FortytwoEdit.showToast("Output Hist", "Item history has been copied and sent to the output log");
        unsel();
    }

    protected void btnOpenDir() {
        FileTools.openModDir();
        unsel();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Debug Tools"), this.width / 2, y+11, TEXT_COLOR);
    }

}
