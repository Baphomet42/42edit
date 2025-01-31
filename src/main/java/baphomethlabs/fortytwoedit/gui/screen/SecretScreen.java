package baphomethlabs.fortytwoedit.gui.screen;

import java.util.List;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class SecretScreen extends GenericScreen {

    protected TextFieldWidget txtUpsideDown;
    protected static final String UPSIDE_DOWN_REF = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789 .,?!':[](){}-=";
    protected static final String UPSIDE_DOWN_CHARS = "ⱯɐᗺqƆɔᗡpƎǝℲɟ⅁ᵷHɥIᴉՐɾꞰʞꞀꞁWɯNuOoԀdꝹbᴚɹSs⟘ʇ∩nɅʌMʍXx⅄ʎZz0⥝ᘔƐ߈ϛ9ㄥ86 ˙'¿¡,:][)(}{-=";
    protected static final Tooltip ITEM_WARN_TT = Tooltip.of(Text.of("vanilla - no change to item warnings\n\nhide - never show warnings\n\nsmart - hide warnings for items that cannot run operator commands"));

    public SecretScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.SECRET_SCREEN;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> changeScreen(new MagickGui())).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Debug Tools..."), button -> changeScreen(new DebugScreen())).dimensions(x+20,y+22*2+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Upside Down"), button -> flipTextBox()).dimensions(x+20,y+22*3+1,80,20).build());
        this.txtUpsideDown = new TextFieldWidget(this.textRenderer,x+105+1,y+22*3+1,100-2,20,Text.of(""));
        this.txtUpsideDown.setMaxLength(MAX_TEXT_LENGTH);
        this.addDrawableChild(this.txtUpsideDown);
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnCycleItemWarn(false)).dimensions(x+20+80+5,y+22*4+1,15,20).build());
        TextFieldWidget txtCustom = new TextFieldWidget(this.textRenderer,x+20+1+80+5+15,y+22*4+1,90-2,20,Text.of(""));
        txtCustom.setMaxLength(256);
        txtCustom.setText(FortytwoEdit.getItemWarningMode());
        txtCustom.setCursorToStart(false);
        txtCustom.setTooltip(ITEM_WARN_TT);
        txtCustom.setEditable(false);
        this.addDrawableChild(txtCustom);
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnCycleItemWarn(true)).dimensions(x+20+80+5+15+90,y+22*4+1,15,20).build());
    }

    protected void flipTextBox() {
        if(txtUpsideDown != null) {
            String txt = txtUpsideDown.getText();
            boolean isJson = false;
            if(txt.startsWith("{") && txt.endsWith("}")) {
                NbtCompound nbt = BlackMagick.validCompound(BlackMagick.nbtFromString(txt));
                if(!nbt.isEmpty()) {
                    isJson = true;
                    StringBuilder json = new StringBuilder();
                    List<String> sortKeys = BlackMagick.sortSet(nbt.getKeys());
                    boolean firstEntry = true;
                    json.append("{");
                    for(String k : sortKeys) {
                        NbtElement el = nbt.get(k);
                        if(el.getType() == NbtElement.STRING_TYPE && !k.contains("\"")) {
                            String flip = flipString(((NbtString)el).asString()).replace("\"","\\\"");
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
                        txtUpsideDown.setText(json.toString());
                }
            }
            if(!isJson) {
                txtUpsideDown.setText(flipString(txt));
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

    protected void btnCycleItemWarn(boolean right) {
        FortytwoEdit.cycleItemWarningMode(right);
        reloadScreen();
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtUpsideDown.isActive();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Super Secret Settings"), this.width / 2, y+11, TEXT_COLOR);
        context.drawTextWithShadow(this.textRenderer, Text.of("Item Op Warning:"), x+20+3,y+7+22*4, LABEL_COLOR);
    }

}
