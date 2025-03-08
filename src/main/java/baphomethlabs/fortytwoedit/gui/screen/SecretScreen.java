package baphomethlabs.fortytwoedit.gui.screen;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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

    public SecretScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.SECRET_SCREEN;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new MagickGui())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Debug Tools..."), button -> changeScreen(new DebugScreen())).bounds(x+20,y+ROW_HEIGHT*2+1,80,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Upside Down"), button -> flipTextBox()).bounds(x+20,y+ROW_HEIGHT*3+1,80,WID_HEIGHT).build());
        this.txtUpsideDown = new EditBox(this.font,x+20+80+WID_SPACE,y+ROW_HEIGHT*3+1,100,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtUpsideDown.setMaxLength(MAX_TEXT_LENGTH);
        this.addRenderableWidget(this.txtUpsideDown);
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

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Super Secret Settings"), this.width / 2, y+11, TEXT_COLOR);
    }

}
