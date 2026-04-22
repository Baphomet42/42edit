package baphomethlabs.fortytwoedit.gui.screen;

import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;

public class OptionsScreen extends GenericScreen {

    public OptionsScreen() {
        super("42edit Options");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = OptionsScreen::new;
        this.addBackButton(SecretScreen::new);

        setupScrollPane();
        
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("controls.keybinds"), btn -> changeScreen(new KeyBindsScreen(this, this.minecraft.options))).build()
        );

        for (String key : BlackMagick.sortSet(OptionsUtil.MOD_OPTIONS.keySet())) {
            paneScroll().addRow(OptionsUtil.MOD_OPTIONS.get(key).getButton(this, WID_WIDTH_FULL));
        }

        paneScroll().addRow();
    }

}
