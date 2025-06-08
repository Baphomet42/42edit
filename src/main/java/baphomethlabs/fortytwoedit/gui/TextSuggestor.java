package baphomethlabs.fortytwoedit.gui;

import com.google.common.collect.Lists;
import java.util.List;
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
import net.minecraft.network.chat.MutableComponent;
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
    private static final int MIN_WIDTH = 20;

    public TextSuggestor(Minecraft client, EditBox editBox, Font font) {
        this.client = client;
        this.input = editBox;
        this.font = font;
        this.lineStartOffset = 1;
        this.suggestionLineLimit = 5;
        this.fillColor = 0;
    }

    public void setSuggestions(String[] list) {
        suggsArr = list;
        refresh();
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
        if(suggsArr.length > 0) {
            this.suggestionsWindow = new TextSuggestor.SuggestionsList(this.sortSuggestions(suggsArr));
        }
    }

    public void hide() {
        this.suggestionsWindow = null;
    }

    private List<SuggestionEntry> sortSuggestions(String[] suggestions) {
        String searchTerm = this.input.getValue().toLowerCase();
        List<SuggestionEntry> list = Lists.newArrayList();
        List<SuggestionEntry> list2 = Lists.newArrayList();
        List<SuggestionEntry> list3 = Lists.newArrayList();
        list.add(SuggestionEntry.of(this.font, this.input.getValue(), ChatFormatting.GRAY, ChatFormatting.ITALIC));
        for(String suggestion : suggestions) {
            if(suggestion.length() > 0 && searchTerm.length() > 0) {
                if(testSuggsMatch(suggestion.toLowerCase(), searchTerm, true)) {
                    list.add(SuggestionEntry.of(this.font, suggestion, searchTerm, ChatFormatting.BOLD));
                }
                else if(testSuggsMatch(suggestion.toLowerCase(), searchTerm, false)) {
                    list2.add(SuggestionEntry.of(this.font, suggestion, searchTerm, ChatFormatting.BOLD));
                }
            }
            list3.add(searchTerm.length() > 0 ? SuggestionEntry.of(this.font, suggestion, ChatFormatting.GRAY) : SuggestionEntry.of(this.font, suggestion));
        }
        list.addAll(list2);
        if(list.size() > 1) // the `1` accounts for the this.input.getValue() entry above
            list.add(SuggestionEntry.of(this.font, this.input.getValue(), Component.empty()));
        list.addAll(list3);
        return list;
    }

    protected static final String SEARCH_PREFIXES = "!#";
    protected static final String SEARCH_WRAPPERS = "\"'";

    protected static boolean testSuggsMatch(String suggestion, String searchTerm, boolean starting) {
        if((starting && suggestion.startsWith(searchTerm))
        || (!starting && suggestion.contains(searchTerm)))
            return true;

        String searchWrapper = "";
        String suggsWrapper = "";
        if(suggestion.length() > 1 && SEARCH_WRAPPERS.contains(suggestion.substring(0,1))) {
            suggsWrapper = suggestion.substring(0,1);
            suggestion = suggestion.substring(1);
        }
        if(searchTerm.length() > 1 && SEARCH_WRAPPERS.contains(searchTerm.substring(0,1))) {
            searchWrapper = searchTerm.substring(0,1);
            searchTerm = searchTerm.substring(1);

            if(!suggsWrapper.equals(searchWrapper))
                return false;
        }

        if((starting && suggestion.startsWith(searchTerm))
        || (!starting && suggestion.contains(searchTerm)))
            return true;

        String searchPrefix = "";
        String suggsPrefix = "";
        if(suggestion.length() > 1 && SEARCH_PREFIXES.contains(suggestion.substring(0,1))) {
            suggsPrefix = suggestion.substring(0,1);
            suggestion = suggestion.substring(1);
        }
        if(searchTerm.length() > 1 && SEARCH_PREFIXES.contains(searchTerm.substring(0,1))) {
            searchPrefix = searchTerm.substring(0,1);
            searchTerm = searchTerm.substring(1);

            if(!suggsPrefix.equals(searchPrefix))
                return false;
        }

        if(!searchTerm.contains(":") && suggestion.contains(":") && (suggestion.length() > suggestion.indexOf(":") + 1)) {
            if((starting && suggestion.substring(suggestion.indexOf(":") + 1).startsWith(searchTerm))
            || (!starting && suggestion.substring(suggestion.indexOf(":") + 1).contains(searchTerm)))
                return true;
        }

        return (starting && suggestion.startsWith(searchTerm))
            || (!starting && suggestion.contains(searchTerm));
    }

	public void refresh() {
        if(!keepSuggestions) {
            this.suggestionsWindow = null;
            this.showSuggestions();
        }
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

    protected record SuggestionEntry(String raw, Component formatted) {

        public static SuggestionEntry of(Font font, String raw, Component formatted) {
            while(font.width(formatted) < MIN_WIDTH) {
                formatted = formatted.copy().append(" ");
            }
            return new SuggestionEntry(raw, formatted);
        }

        public static SuggestionEntry of(Font font, String raw) {
            return SuggestionEntry.of(font, raw, Component.nullToEmpty(raw));
        }

        public static SuggestionEntry of(Font font, String raw, ChatFormatting... formatting) {
            return SuggestionEntry.of(font, raw, Component.nullToEmpty(raw).copy().withStyle(formatting));
        }

        public static SuggestionEntry of(Font font, String raw, String searchTerm, ChatFormatting... matching) {
            MutableComponent formatted = Component.empty();
            String parseString = raw;

            for(int i=0; i<SEARCH_WRAPPERS.length(); i++) {
                if(parseString.startsWith(SEARCH_WRAPPERS.substring(i,i+1))) {
                    if(searchTerm.startsWith(SEARCH_WRAPPERS.substring(i,i+1))) {
                        formatted.append(Component.nullToEmpty(SEARCH_WRAPPERS.substring(i,i+1)).copy().withStyle(matching));
                        searchTerm = searchTerm.substring(1);
                    }
                    else {
                        formatted.append(Component.nullToEmpty(SEARCH_WRAPPERS.substring(i,i+1)));
                    }
                    parseString = parseString.substring(1);
                    break;
                }
            }
            for(int i=0; i<SEARCH_PREFIXES.length(); i++) {
                if(parseString.startsWith(SEARCH_PREFIXES.substring(i,i+1))) {
                    if(searchTerm.startsWith(SEARCH_PREFIXES.substring(i,i+1))) {
                        formatted.append(Component.nullToEmpty(SEARCH_PREFIXES.substring(i,i+1)).copy().withStyle(matching));
                        searchTerm = searchTerm.substring(1);
                    }
                    else {
                        formatted.append(Component.nullToEmpty(SEARCH_PREFIXES.substring(i,i+1)));
                    }
                    parseString = parseString.substring(1);
                    break;
                }
            }

            while(parseString.length() > 0 && searchTerm.length() > 0) {
                int index = parseString.toLowerCase().indexOf(searchTerm.toLowerCase());
                if(index == -1) {
                    break;
                }
                else if(index == 0) {
                    formatted.append(Component.nullToEmpty(parseString.substring(0,searchTerm.length())).copy().withStyle(matching));
                    parseString = parseString.substring(searchTerm.length());
                }
                else {
                    formatted.append(Component.nullToEmpty(parseString.substring(0,index)));
                    parseString = parseString.substring(index);
                }

            }

            if(parseString.length() > 0)
                formatted.append(Component.nullToEmpty(parseString));

            return SuggestionEntry.of(font, raw, formatted);
        }

    }

    public class SuggestionsList {
        private final Rect2i rect;
        private final List<SuggestionEntry> suggestionList;
        private int offset;
        private int current;
        private Vec2 lastMouse = Vec2.ZERO;
        boolean tabCycles;
        private final int LINE_HEIGHT = 10;
        private final int MIN_WIDTH = 5;
        private final int WIDTH_PADDING = 3;
        private final int SUGGS_PADDING = 2;

        SuggestionsList(final List<SuggestionEntry> list) {
            this.rect = new Rect2i(0, 0, MIN_WIDTH, Math.min(list.size(), TextSuggestor.this.suggestionLineLimit) * LINE_HEIGHT);
            this.suggestionList = list;
            this.select(0);
        }

        public void render(GuiGraphics guiGraphics, int i, int j) {
            int k = Math.min(this.suggestionList.size(), TextSuggestor.this.suggestionLineLimit);

            List<ClientTooltipComponent> tooltipList = Lists.newArrayList();
            int maxWidth = MIN_WIDTH;
            for(int n = 0; n < k; n++) {
                SuggestionEntry suggestion = this.suggestionList.get(n + this.offset);
                Component text = suggestion.formatted();
                if(n + this.offset == this.current)
                    text = text.copy().withStyle(ChatFormatting.YELLOW);
                tooltipList.add(ClientTooltipComponent.create(text.getVisualOrderText()));
                maxWidth = Math.max(maxWidth, TextSuggestor.this.font.width(text));
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
            SuggestionEntry suggestion = this.suggestionList.get(this.current);
            TextSuggestor.this.keepSuggestions = true;
            TextSuggestor.this.input.setValue(suggestion.raw());
            TextSuggestor.this.input.setCursorPosition(suggestion.raw().length());
            TextSuggestor.this.input.setHighlightPos(suggestion.raw().length());
            this.select(this.current);
            TextSuggestor.this.keepSuggestions = false;
            this.tabCycles = true;
        }
    }
}
