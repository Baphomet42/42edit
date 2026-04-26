package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FileTools;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public class DebugScreen extends GenericScreen {

    public DebugScreen() {
        super("Debug Tools");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = DebugScreen::new;
        this.addBackButton(SecretScreen::new);

        setupScrollPane();
        paneScroll().addRow(
            WIDGET_UTIL.newButton("View Log...", btn -> changeScreen(new LogScreen())).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Item History...", btn -> changeScreen(new ItemHistoryScreen())).build(),
            WIDGET_UTIL.newButton("Copy", btn -> btnOutputHist())
                .setTooltip("Copy item history info and send to log").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Open Dir", btn -> btnOpenDir())
                .setTooltip("Open 42edit file directory").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Reset Misc", btn -> btnRefreshRuntime())
                .setTooltip("Clear various caches, refresh files, etc.").build()
        );
        finalizeScrollPane();
    }

    protected void btnRefreshRuntime() {
        FortytwoEdit.showToast("Reset Misc","Variables reset");
        FortytwoEdit.debugTryRefreshVarious();
    }

    protected void btnOutputHist() {
        String hist = BlackMagick.nbtToSnbt(FortytwoEdit.getItemHist());
        FortytwoEdit.setClipboard(hist);
        FortytwoEdit.logInfo("Item History: "+hist);
        FortytwoEdit.showToast("Output Hist", "Item history has been copied and sent to the output log");
    }

    protected void btnOpenDir() {
        FileTools.openModDir();
    }

}
