package baphomethlabs.fortytwoedit.gui.screen;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;

public class SecretScreen extends GenericScreen {

    protected EditBox txtUpsideDown;
    protected static final String UPSIDE_DOWN_REF = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789 .,?!':[](){}-=";
    protected static final String UPSIDE_DOWN_CHARS = "ⱯɐᗺqƆɔᗡpƎǝℲɟ⅁ᵷHɥIᴉՐɾꞰʞꞀꞁWɯNuOoԀdꝹbᴚɹSs⟘ʇ∩nɅʌMʍXx⅄ʎZz0⥝ᘔƐ߈ϛ9ㄥ86 ˙'¿¡,:][)(}{-=";
    protected static final Tooltip ITEM_WARN_TT = Tooltip.create(Component.nullToEmpty("vanilla - no change to item warnings\n\nhide - never show warnings\n\nsmart - hide warnings for items that cannot run operator commands"));
    private static final int CONFIG_BUTTON_WIDTH = ROW_WIDTH-NARROW_OFFSET;

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
            WIDGET_UTIL.newButton("Upside Down", btn -> flipTextBox()).setTooltip("Convert text to upside down text").build(),
            this.txtUpsideDown
        );
        paneScroll().addRow("Config");
        {
            MutableComponent btnTxt = Component.empty().append("Chat Icons: ");
            if (!OptionsUtil.ModOptions.CHAT_ICONS.getSetting())
                btnTxt.append(Component.empty().append("false").withStyle(ChatFormatting.GRAY));
            else
                btnTxt.append(Component.empty().append("true").withStyle(ChatFormatting.GREEN));
            paneScroll().addRow(
                WIDGET_UTIL.newButton(btnTxt, btn -> {
                    OptionsUtil.ModOptions.CHAT_ICONS.toggleSetting();
                    reloadScreen();
                }).setSize(CONFIG_BUTTON_WIDTH).setTooltip("Show player heads next to chat messages.\n\nDefault: false").build()
            );
        }
        {
            MutableComponent btnTxt = Component.empty().append("Locator Bar Skin: ");
            if (FortytwoEdit.mixinLocatorBarModeDefault())
                btnTxt.append(Component.empty().append(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting()).withStyle(ChatFormatting.GRAY));
            else if (FortytwoEdit.mixinLocatorBarModeUnknown())
                btnTxt.append(Component.empty().append(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting()).withStyle(ChatFormatting.YELLOW));
            else
                btnTxt.append(Component.empty().append(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting()).withStyle(ChatFormatting.GREEN));
            paneScroll().addRow(
                WIDGET_UTIL.newButton(btnTxt, btn -> {
                    FortytwoEdit.mixinLocatorBarCycle();
                    reloadScreen();
                }).setSize(CONFIG_BUTTON_WIDTH).setTooltip("Override locator bar icon to show player skin.\n\nDefault: never").build()
            );
        }
        {
            MutableComponent btnTxt = Component.empty().append("Profile Tooltip Info: ");
            if (!OptionsUtil.ModOptions.DYNAMIC_PROFILE_TOOLTIP_INFO.getSetting())
                btnTxt.append(Component.empty().append("false").withStyle(ChatFormatting.GRAY));
            else
                btnTxt.append(Component.empty().append("true").withStyle(ChatFormatting.GREEN));
            paneScroll().addRow(
                WIDGET_UTIL.newButton(btnTxt, btn -> {
                    OptionsUtil.ModOptions.DYNAMIC_PROFILE_TOOLTIP_INFO.toggleSetting();
                    reloadScreen();
                }).setSize(CONFIG_BUTTON_WIDTH).setTooltip("Override dynamic profile item tooltip to show extra info.\n\nDefault: false").build()
            );
        }
        {
            MutableComponent btnTxt = Component.empty().append("F3 Screen Rearrange: ");
            if (!OptionsUtil.ModOptions.DEBUG_SCREEN_REARRANGE.getSetting())
                btnTxt.append(Component.empty().append("false").withStyle(ChatFormatting.GRAY));
            else
                btnTxt.append(Component.empty().append("true").withStyle(ChatFormatting.GREEN));
            paneScroll().addRow(
                WIDGET_UTIL.newButton(btnTxt, btn -> {
                    OptionsUtil.ModOptions.DEBUG_SCREEN_REARRANGE.toggleSetting();
                    reloadScreen();
                }).setSize(CONFIG_BUTTON_WIDTH).setTooltip("Move certain elements in the F3 screen for easier readability.\n\nDefault: false").build()
            );
        }
    }

    protected void flipTextBox() {
        if (txtUpsideDown != null) {
            String txt = txtUpsideDown.getValue();
            boolean isJson = false;
            if (txt.startsWith("{") && txt.endsWith("}")) {
                CompoundTag nbt = BlackMagick.validCompoundFromString(txt);
                if (!nbt.isEmpty()) {
                    isJson = true;
                    StringBuilder json = new StringBuilder();
                    List<String> sortKeys = BlackMagick.sortSet(nbt.keySet());
                    boolean firstEntry = true;
                    json.append("{");
                    for (String k : sortKeys) {
                        Tag el = nbt.get(k);
                        if (el.getId() == Tag.TAG_STRING && !k.contains("\"")) {
                            String flip = flipString(((StringTag)el).asString().get()).replace("\"","\\\"");
                            if (!firstEntry)
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
                    if (isJson)
                        txtUpsideDown.setValue(json.toString());
                }
            }
            if (!isJson) {
                txtUpsideDown.setValue(flipString(txt));
            }
        }
        unsel();
    }

    protected static String flipString(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i=text.length()-1; i>=0; i--)
            sb.append(flipChar(text.charAt(i)));
        return sb.toString();
    }

    protected static char flipChar(char character) {
        int c = UPSIDE_DOWN_REF.indexOf(character);
        if (c>=0)
            return UPSIDE_DOWN_CHARS.charAt(c);
        FortytwoEdit.logWarn("Failed to flip char: "+character);
        return character;
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtUpsideDown.canConsumeInput();
    }

}
