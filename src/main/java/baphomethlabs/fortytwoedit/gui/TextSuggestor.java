package baphomethlabs.fortytwoedit.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

/**
 * modified from {@link net.minecraft.client.gui.screen.ChatInputSuggestor}
 */
public class TextSuggestor {
    final MinecraftClient client;
    final TextFieldWidget textField;
    final TextRenderer textRenderer;
    final int inWindowIndexOffset;
    final int maxSuggestionSize;
    final int color;
    private SuggestionWindow window;
    private boolean windowActive;
    boolean completingSuggestions;
    String[] suggestionList = {};
    Suggestions suggestions;

    public TextSuggestor(MinecraftClient client, TextFieldWidget textField, TextRenderer textRenderer) {
        this.client = client;
        this.textField = textField;
        this.textRenderer = textRenderer;
        this.inWindowIndexOffset = 0;
        this.maxSuggestionSize = 5;
        this.color = 0;
        suggestions = new Suggestions(StringRange.at(0),new ArrayList<>());
        windowActive = true;
    }

    public void setSuggestions(String[] list) {
        suggestionList = list;
        refresh();
    }

    public void setWindowActive(boolean windowActive) {
        this.windowActive = windowActive;
        if (!windowActive) {
            this.window = null;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.window != null && this.window.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double amount) {
        return this.window != null && this.window.mouseScrolled(MathHelper.clamp(amount, -1.0, 1.0));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.window != null && this.window.mouseClicked((int)mouseX, (int)mouseY, button);
    }

    public void show() {
        if (!suggestions.isEmpty()) {
            int i = 0;
            int w = 0;
            w = Math.max(w, this.textRenderer.getWidth(this.textField.getText()));
            for (Suggestion suggestion : suggestions.getList()) {
                w = Math.max(w, this.textRenderer.getWidth(suggestion.getText()));
            }
            int j = MathHelper.clamp(this.textField.getCharacterX(suggestions.getRange().getStart()), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth() - i);
            this.window = new SuggestionWindow(j, this.textField, w, this.sortSuggestions(suggestions));
        }
    }

    public void clearWindow() {
        this.window = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        String string = this.textField.getText().substring(0, this.textField.getCursor());
        String string2 = string.substring(0).toLowerCase(Locale.ROOT);
        ArrayList<Suggestion> list = Lists.newArrayList();
        ArrayList<Suggestion> list2 = Lists.newArrayList();
        list.add(new Suggestion(StringRange.at(0),this.textField.getText()));
        for (Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getText().startsWith(string2) || suggestion.getText().startsWith("minecraft:" + string2)) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        return list;
    }

    public void refresh() {
        String string = this.textField.getText();
        if (!this.completingSuggestions) {
            //this.textField.setSuggestion(null);
            this.window = null;
        }
        StringReader stringReader = new StringReader(string);
        int i = this.textField.getCursor();
        int j = stringReader.getCursor();
        if (!(i < j || this.window != null && this.completingSuggestions)) {
            ArrayList<Suggestion> tempList = new ArrayList<>();
            for(String s : suggestionList) {
                tempList.add(new Suggestion(StringRange.at(0),s));
            }
            suggestions = new Suggestions(StringRange.at(0), tempList);
            this.showCommandSuggestions();
        }
    }

    private void showCommandSuggestions() {
        this.window = null;
        if (this.windowActive) {
            this.show();
        }
    }

    static String getSuggestionSuffix(String original, String suggestion) {
        if (suggestion.startsWith(original)) {
            return suggestion.substring(original.length());
        }
        return null;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        this.tryRenderWindow(context, mouseX, mouseY);
    }

    public boolean tryRenderWindow(DrawContext context, int mouseX, int mouseY) {
        if (this.window != null) {
            this.window.render(context, mouseX, mouseY);
            return true;
        }
        return false;
    }

    public class SuggestionWindow {
        private final Rect2i area;
        private final Rect2i areaMax;
        private final TextFieldWidget txt;
        private final List<Suggestion> suggestions;
        private int inWindowIndex;
        private int selection;
        private Vec2f mouse = Vec2f.ZERO;
        private boolean completed;

        SuggestionWindow(int x, TextFieldWidget txt, int width, List<Suggestion> suggestions) {
            int i = x - 1 + 2;
            this.txt = txt;
            int j = txt.getY() + 25;
            this.area = new Rect2i(i, j, 1, Math.min(suggestions.size(), TextSuggestor.this.maxSuggestionSize) * 10 + 2);
            this.areaMax = new Rect2i(i, j, width, Math.min(suggestions.size(), TextSuggestor.this.maxSuggestionSize) * 10 + 2);
            this.suggestions = suggestions;
            this.select(0);
        }

        public void render(DrawContext context, int mouseX, int mouseY) {
            area.setY(txt.getY() + 25);
            areaMax.setY(txt.getY() + 25);
            Message message;
            int i = Math.min(this.suggestions.size(), TextSuggestor.this.maxSuggestionSize);
            //boolean bl = this.inWindowIndex > 0;
            //boolean bl2 = this.suggestions.size() > this.inWindowIndex + i;
            //boolean bl3 = bl || bl2;
            boolean bl4 = this.mouse.x != (float)mouseX || this.mouse.y != (float)mouseY;
            if (bl4) {
                this.mouse = new Vec2f(mouseX, mouseY);
            }
            // if (bl3) {
            //     int k;
            //     //context.fill(this.area.getX(), this.area.getY() - 1, this.area.getX() + this.area.getWidth(), this.area.getY(), 1, TextSuggestor.this.color);
            //     //context.fill(this.area.getX(), this.area.getY() + this.area.getHeight(), this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight() + 1, 1, TextSuggestor.this.color);
            //     if (bl) {
            //         for (k = 0; k < this.area.getWidth(); ++k) {
            //             if (k % 2 != 0) continue;
            //             //context.fill(this.area.getX() + k, this.area.getY() - 1, this.area.getX() + k + 1, this.area.getY(), 1, -1);
            //         }
            //     }
            //     if (bl2) {
            //         for (k = 0; k < this.area.getWidth(); ++k) {
            //             if (k % 2 != 0) continue;
            //             //context.fill(this.area.getX() + k, this.area.getY() + this.area.getHeight(), this.area.getX() + k + 1, this.area.getY() + this.area.getHeight() + 1, 1, -1);
            //         }
            //     }
            // }
            boolean bl52 = false;
            List<Text> textList = new ArrayList<>();
            int width = 0;
            for (int l = 0; l < i; ++l) {
                Suggestion suggestion = this.suggestions.get(l + this.inWindowIndex);
                //context.fill(this.area.getX(), this.area.getY() + 12 * l, this.area.getX() + this.area.getWidth(), this.area.getY() + 12 * l + 12, 1, TextSuggestor.this.color);
                if (mouseX > this.area.getX() && mouseX < this.area.getX() + this.area.getWidth() && mouseY > this.area.getY() + 10 * l && mouseY < this.area.getY() + 10 * l + 10) {
                    if (bl4) {
                        this.select(l + this.inWindowIndex);
                    }
                    bl52 = true;
                }
                //context.drawTextWithShadow(TextSuggestor.this.textRenderer, suggestion.getText(), this.area.getX() + 1, this.area.getY() + 2 + 12 * l, l + this.inWindowIndex == this.selection ? -256 : -5592406);
                Text t = Text.of(suggestion.getText());
                if(l + this.inWindowIndex == this.selection)
                    t = t.copy().formatted(Formatting.YELLOW);
                textList.add(t);
                width = Math.max(width, textRenderer.getWidth(suggestion.getText()));
            }
            area.setWidth(width+6);
            context.drawTooltip(TextSuggestor.this.textRenderer, textList, this.area.getX() + 1 - 10, this.area.getY() + 12);
            if (bl52 && (message = this.suggestions.get(this.selection).getTooltip()) != null) {
                context.drawTooltip(TextSuggestor.this.textRenderer, Texts.toText(message), mouseX, mouseY);
            }
        }

        public boolean mouseClicked(int x, int y, int button) {
            if (!this.area.contains(x, y)) {
                return false;
            }
            int i = (y - this.area.getY()) / 10 + this.inWindowIndex;
            if (i >= 0 && i < this.suggestions.size()) {
                this.select(i);
                this.complete();
            }
            return true;
        }

        public boolean mouseScrolled(double amount) {
            int i = (int)(TextSuggestor.this.client.mouse.getX() * (double)TextSuggestor.this.client.getWindow().getScaledWidth() / (double)TextSuggestor.this.client.getWindow().getWidth());
            if (this.areaMax.contains(i, (int)(TextSuggestor.this.client.mouse.getY() * (double)TextSuggestor.this.client.getWindow().getScaledHeight() / (double)TextSuggestor.this.client.getWindow().getHeight()))) {
                this.inWindowIndex = MathHelper.clamp((int)((double)this.inWindowIndex - amount), 0, Math.max(this.suggestions.size() - TextSuggestor.this.maxSuggestionSize, 0));
                return true;
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 265) {//arrow up
                this.scroll(-1);
                this.completed = false;
                return true;
            }
            if (keyCode == 264) {//arrow down
                this.scroll(1);
                this.completed = false;
                return true;
            }
            if (keyCode == 258) {//tab
                if (this.completed) {
                    this.scroll(Screen.hasShiftDown() ? -1 : 1);
                }
                this.complete();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {//enter
                this.complete();
                TextSuggestor.this.clearWindow();
                return true;
            }
            if (keyCode == 256) {//escape
                TextSuggestor.this.clearWindow();
                return true;
            }
            return false;
        }

        public void scroll(int offset) {
            this.select(this.selection + offset);
            int i = this.inWindowIndex;
            int j = this.inWindowIndex + TextSuggestor.this.maxSuggestionSize - 1;
            if (this.selection < i) {
                this.inWindowIndex = MathHelper.clamp(this.selection, 0, Math.max(this.suggestions.size() - TextSuggestor.this.maxSuggestionSize, 0));
            } else if (this.selection > j) {
                this.inWindowIndex = MathHelper.clamp(this.selection + TextSuggestor.this.inWindowIndexOffset - TextSuggestor.this.maxSuggestionSize, 0, Math.max(this.suggestions.size() - TextSuggestor.this.maxSuggestionSize, 0));
            }
        }

        public void select(int index) {
            this.selection = index;
            if (this.selection < 0) {
                this.selection += this.suggestions.size();
            }
            if (this.selection >= this.suggestions.size()) {
                this.selection -= this.suggestions.size();
            }
            //Suggestion suggestion = this.suggestions.get(this.selection);
            //TextSuggestor.this.textField.setSuggestion(TextSuggestor.getSuggestionSuffix(TextSuggestor.this.textField.getText(), suggestion.getText()));
        }

        public void complete() {
            Suggestion suggestion = this.suggestions.get(this.selection);
            TextSuggestor.this.completingSuggestions = true;
            TextSuggestor.this.textField.setText(suggestion.getText());
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            TextSuggestor.this.textField.setSelectionStart(i);
            TextSuggestor.this.textField.setSelectionEnd(i);
            this.select(this.selection);
            //TextSuggestor.this.textField.setSuggestion(null);
            TextSuggestor.this.completingSuggestions = false;
            this.completed = true;
        }
    }
}

