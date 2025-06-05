package baphomethlabs.fortytwoedit.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

/**
 * Modified from {@link net.minecraft.client.gui.components.CommandSuggestions}
 */
public class TextSuggestor {
    final Minecraft client;
    final EditBox input;
    final Font font;
    final int lineStartOffset;
    final int suggestionLineLimit;
    final int fillColor;
    @Nullable
    private TextSuggestor.SuggestionsList suggestionsWindow;
    boolean keepSuggestions;
    String[] suggsArr = {};
    Suggestions suggestionsObject;

    public TextSuggestor(Minecraft client, EditBox editBox, Font font) {
        this.client = client;
        this.input = editBox;
        this.font = font;
        this.lineStartOffset = 1;
        this.suggestionLineLimit = 5;
        this.fillColor = 0;
        suggestionsObject = new Suggestions(StringRange.at(0),Lists.newArrayList());
    }

    public void setSuggestions(String[] list) {
        suggsArr = list;
        updateCommandInfo();
    }

    public boolean keyPressed(int i, int j, int k) {
        if(this.suggestionsWindow != null && this.suggestionsWindow.keyPressed(i, j, k)) {
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double d) {
        return this.suggestionsWindow != null && this.suggestionsWindow.mouseScrolled(Mth.clamp(d, -1.0, 1.0));
    }

    public boolean mouseClicked(double d, double e, int i) {
        return this.suggestionsWindow != null && this.suggestionsWindow.mouseClicked((int)d, (int)e, i);
    }

    public void showSuggestions() {
        if(!suggestionsObject.isEmpty()) {
            this.suggestionsWindow = new TextSuggestor.SuggestionsList(this.sortSuggestions(suggestionsObject));
        }
    }

    public void hide() {
        this.suggestionsWindow = null;
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        String string = this.input.getValue().substring(0, this.input.getCursorPosition());
        String string2 = string.toLowerCase(Locale.ROOT);
        List<Suggestion> list = Lists.newArrayList();
        List<Suggestion> list2 = Lists.newArrayList();
        list.add(new Suggestion(StringRange.at(0),this.input.getValue()));
        for(Suggestion suggestion : suggestions.getList()) {
            if(suggestion.getText().startsWith(string2) || suggestion.getText().startsWith("minecraft:" + string2)
            || suggestion.getText().startsWith("!minecraft:" + string2) || suggestion.getText().startsWith("!" + string2)
            || suggestion.getText().startsWith("\"" + string2) || suggestion.getText().startsWith("'" + string2)) {
                list.add(suggestion);
                continue;
            }
            list2.add(suggestion);
        }
        list.addAll(list2);
        return list;
    }

	public void refresh() {
		updateCommandInfo();
	}

    public void updateCommandInfo() {
        String string = this.input.getValue();
        if(!this.keepSuggestions) {
            this.suggestionsWindow = null;
        }
        StringReader stringReader = new StringReader(string);
        int i = this.input.getCursorPosition();
        int j = stringReader.getCursor();
        if(i >= j && (this.suggestionsWindow == null || !this.keepSuggestions)) {
            List<Suggestion> tempList = Lists.newArrayList();
            for(String s : suggsArr) {
                tempList.add(new Suggestion(StringRange.at(0),s));
            }
            suggestionsObject = new Suggestions(StringRange.at(0), tempList);
            this.updateUsageInfo();
        }
    }

    private void updateUsageInfo() {
        this.suggestionsWindow = null;
        this.showSuggestions();
    }

    @Nullable
    static String calculateSuggestionSuffix(String string, String string2) {
        return string2.startsWith(string) ? string2.substring(string.length()) : null;
    }

    public void render(GuiGraphics guiGraphics, int i, int j) {
        this.renderSuggestions(guiGraphics, i, j);
    }

    public boolean renderSuggestions(GuiGraphics guiGraphics, int i, int j) {
        if(this.suggestionsWindow != null) {
            this.suggestionsWindow.render(guiGraphics, i, j);
            return true;
        }
        return false;
    }

    public class SuggestionsList {
        private final Rect2i rect;
        private final List<Suggestion> suggestionList;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        boolean tabCycles;
        private final int LINE_HEIGHT = 10;
        private final int MIN_WIDTH = 5;
        private final int WIDTH_PADDING = 3;
        private final int SUGGS_PADDING = 2;

        SuggestionsList(final List<Suggestion> list) {
            this.rect = new Rect2i(0, 0, MIN_WIDTH, Math.min(list.size(), TextSuggestor.this.suggestionLineLimit) * LINE_HEIGHT);
            this.suggestionList = list;
            this.select(0);
        }

        public void render(GuiGraphics guiGraphics, int i, int j) {
            int k = Math.min(this.suggestionList.size(), TextSuggestor.this.suggestionLineLimit);

            List<ClientTooltipComponent> tooltipList = Lists.newArrayList();
            int maxWidth = MIN_WIDTH;
            for(int n = 0; n < k; n++) {
                Suggestion suggestion = (Suggestion)this.suggestionList.get(n + this.offset);
                Component t = Component.nullToEmpty(suggestion.getText());
                if(n + this.offset == this.current)
                    t = t.copy().withStyle(ChatFormatting.YELLOW);
                tooltipList.add(ClientTooltipComponent.create(t.getVisualOrderText()));
                maxWidth = Math.max(maxWidth, TextSuggestor.this.font.width(suggestion.getText()));
            }
            rect.setWidth(maxWidth+WIDTH_PADDING*2);

            BelowOrAboveWidgetTooltipPositioner ttPositioner = new BelowOrAboveWidgetTooltipPositioner(TextSuggestor.this.input.getRectangle());
            Vector2ic vec = getTooltipPosition(TextSuggestor.this.font, guiGraphics, i, j, tooltipList, ttPositioner);
            rect.setX(vec.x()-WIDTH_PADDING);
            rect.setY(vec.y());

            if(this.lastMouse.x != i || this.lastMouse.y != j) {
                this.lastMouse = new Vec2(i, j);
                
                if(this.rect.contains(i, j)) {
                    int lineNum = (j - this.rect.getY()) / LINE_HEIGHT;
                    int suggsNum = lineNum + this.offset;
                    if(suggsNum >= 0 && suggsNum < this.suggestionList.size() && lineNum >= 0 && lineNum < TextSuggestor.this.suggestionLineLimit) {
                        this.select(suggsNum);
                    }
                }
            }

            guiGraphics.renderTooltip(TextSuggestor.this.font, tooltipList, i, j, ttPositioner, null);
        }

        /**
         * Modified from {@link net.minecraft.client.gui.GuiGraphics#renderTooltip}
         */
        private static Vector2ic getTooltipPosition(Font font, GuiGraphics guiGraphics, int i, int j, List<ClientTooltipComponent> list, BelowOrAboveWidgetTooltipPositioner clientTooltipPositioner) {
            int k = 0;
            int l = list.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clientTooltipComponent : list) {
                int m = clientTooltipComponent.getWidth(font);
                if (m > k) {
                    k = m;
                }

                l += clientTooltipComponent.getHeight(font);
            }

            return clientTooltipPositioner.positionTooltip(guiGraphics.guiWidth(), guiGraphics.guiHeight(), i, j, k, l);
        }

        public boolean mouseClicked(int i, int j, int k) {
            if(!this.rect.contains(i, j)) {
                return false;
            }

            int lineNum = (j - this.rect.getY()) / LINE_HEIGHT;
            int suggsNum = lineNum + this.offset;
            if(suggsNum >= 0 && suggsNum < this.suggestionList.size() && lineNum >= 0 && lineNum < TextSuggestor.this.suggestionLineLimit) {
                this.select(suggsNum);
                this.useSuggestion();
            }

            return true;
        }

        public boolean mouseScrolled(double d) {
            int j = (int)TextSuggestor.this.client.mouseHandler.getScaledYPos(TextSuggestor.this.client.getWindow());
            if(this.rect.getY() <= j && j <= this.rect.getY()+this.rect.getHeight()) {
                this.offset = Mth.clamp((int)(this.offset - d), 0, Math.max(this.suggestionList.size() - TextSuggestor.this.suggestionLineLimit, 0));
                return true;
            }
            return false;
        }

        public boolean keyPressed(int i, int j, int k) {
            if(i == 265) {//arrow up
                this.cycle(-1);
                this.tabCycles = false;
                return true;
            }
            if(i == 264) {//arrow down
                this.cycle(1);
                this.tabCycles = false;
                return true;
            }
            if(i == 258) {//tab
                if(this.tabCycles) {
                    this.cycle(Screen.hasShiftDown() ? -1 : 1);
                }
                this.useSuggestion();
                return true;
            }
            if(i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) {//enter
                this.useSuggestion();
                TextSuggestor.this.hide();
                return true;
            }
            if(i == 256) {//escape
                TextSuggestor.this.hide();
                return true;
            }
            return false;
        }

        public void cycle(int i) {
            this.select(this.current + i);
            int j = this.offset;
            int k = this.offset + TextSuggestor.this.suggestionLineLimit - 1;
            if(this.current < j + SUGGS_PADDING) {
                this.offset = Mth.clamp(
                    this.current - SUGGS_PADDING,
                    0, Math.max(this.suggestionList.size() - TextSuggestor.this.suggestionLineLimit, 0)
                );
            } else if(this.current > k - SUGGS_PADDING) {
                this.offset = Mth.clamp(
                    this.current + SUGGS_PADDING + TextSuggestor.this.lineStartOffset - TextSuggestor.this.suggestionLineLimit,
                    0, Math.max(this.suggestionList.size() - TextSuggestor.this.suggestionLineLimit, 0)
                );
            }
        }

        public void select(int i) {
            this.current = i;
            if(this.current < 0) {
                this.current = 0;
            }
            if(this.current >= this.suggestionList.size()) {
                this.current = this.suggestionList.size() - 1;
            }
        }

        public void useSuggestion() {
            Suggestion suggestion = (Suggestion)this.suggestionList.get(this.current);
            TextSuggestor.this.keepSuggestions = true;
            TextSuggestor.this.input.setValue(suggestion.getText());
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            TextSuggestor.this.input.setCursorPosition(i);
            TextSuggestor.this.input.setHighlightPos(i);
            this.select(this.current);
            TextSuggestor.this.keepSuggestions = false;
            this.tabCycles = true;
        }
    }
}
