package baphomethlabs.fortytwoedit.gui.screen;

import java.util.List;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public class SecretScreen extends GenericScreen {

    protected EditBox txtUpsideDown;
    protected static final String UPSIDE_DOWN_REF = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789 .,?!':[](){}-=";
    protected static final String UPSIDE_DOWN_CHARS = "ⱯɐᗺqƆɔᗡpƎǝℲɟ⅁ᵷHɥIᴉՐɾꞰʞꞀꞁWɯNuOoԀdꝹbᴚɹSs⟘ʇ∩nɅʌMʍXx⅄ʎZz0⥝ᘔƐ߈ϛ9ㄥ86 ˙'¿¡,:][)(}{-=";
    protected static final Tooltip ITEM_WARN_TT = Tooltip.create(Component.nullToEmpty("vanilla - no change to item warnings\n\nhide - never show warnings\n\nsmart - hide warnings for items that cannot run operator commands"));

    public SecretScreen() {
        super("Super Secret Settings");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = SecretScreen::new;
        this.addBackButton();

        setupScrollPane();
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Debug Tools...", btn -> changeScreen(new DebugScreen())).build()
        );
        this.txtUpsideDown = WIDGET_UTIL.newEditBox(100).build();
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Upside Down", btn -> flipTextBox()).build(),
            this.txtUpsideDown
        );
    }

    protected void flipTextBox() {
        if(txtUpsideDown != null) {
            String txt = txtUpsideDown.getValue();
            boolean isJson = false;
            if(txt.startsWith("{") && txt.endsWith("}")) {
                CompoundTag nbt = BlackMagick.validCompoundFromString(txt);
                if(!nbt.isEmpty()) {
                    isJson = true;
                    StringBuilder json = new StringBuilder();
                    List<String> sortKeys = BlackMagick.sortSet(nbt.keySet());
                    boolean firstEntry = true;
                    json.append("{");
                    for(String k : sortKeys) {
                        Tag el = nbt.get(k);
                        if(el.getId() == Tag.TAG_STRING && !k.contains("\"")) {
                            String flip = flipString(((StringTag)el).asString().get()).replace("\"","\\\"");
                            if(!firstEntry)
                                json.append(",");
                            else
                                firstEntry = false;
                            json.append("\n    \"").append(k).append("\": \"").append(flip).append("\"");
                        }
                        else {
                            isJson = false;
                            break;
                        }
                    }
                    json.append("\n}");
                    if(isJson)
                        txtUpsideDown.setValue(json.toString());
                }
            }
            if(!isJson) {
                txtUpsideDown.setValue(flipString(txt));
            }
        }
        unsel();
    }

    protected static String flipString(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for(int i=text.length()-1; i>=0; i--)
            sb.append(flipChar(text.charAt(i)));
        return sb.toString();
    }

    protected static char flipChar(char character) {
        int c = UPSIDE_DOWN_REF.indexOf(character);
        if(c>=0)
            return UPSIDE_DOWN_CHARS.charAt(c);
        FortytwoEdit.logWarn("Failed to flip char: "+character);
        return character;
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtUpsideDown.canConsumeInput();
    }

}
