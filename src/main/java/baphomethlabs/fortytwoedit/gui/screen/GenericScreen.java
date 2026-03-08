package baphomethlabs.fortytwoedit.gui.screen;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilderScreen.RowWidget;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton;
import baphomethlabs.fortytwoedit.gui.widget.WidgetUtil;

public abstract class GenericScreen extends Screen {

    protected static final Identifier TEXTURE_GENERIC = Identifier.fromNamespaceAndPath("42edit","gui/generic");
    protected static final Identifier TEXTURE_MENU_BAR = Identifier.fromNamespaceAndPath("42edit","gui/menu_bar");
    protected int backgroundWidth = 12*20;
    protected int backgroundHeight = 9*22;
    protected int x;// to_do rename to leftPos and topPos (see AbstractContainerScreen)
    protected int y;
    protected static final int LABEL_COLOR = 0xFFA0A0A0;
    protected static final int LABEL_COLOR_DIM = 0xFF404040;
    protected static final int ERROR_COLOR = 0xFFFF5555;
    protected static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final int WID_HEIGHT = 20; // standard widget height
    protected static final int ROW_HEIGHT = 22; // standard spacing amounts between rows of widgets
    protected static final int ROW_WIDTH = 208;
    protected static final int TOP_OFFSET = (ROW_HEIGHT-WID_HEIGHT)/2;
    protected static final int WID_SPACE = 5; // standard horizontal spacing between widgets
    protected static final int GUI_SPACE = 5; // standard starting position for widget in top corner of gui (for both x and y)
    protected static final int WID_LEFT = 10; // standard spacing before first widget in row
    protected static final int NARROW_OFFSET = 10;
    protected static final int WID_LEFT_NARROW = WID_LEFT + NARROW_OFFSET;
    protected static final int SCROLL_ROW_LEFT_OFFSET = 3;
    protected static final int MULTI_LINE_TEXT_WIDGET_Y_OFFSET = 6;
    protected static final Duration TOOLTIP_DELAY = Duration.ofMillis(500L);
    protected static final Duration TOOLTIP_DELAY_SHORT = Duration.ofMillis(100L);
    public static final int MAX_TEXT_LENGTH = 131072;
    public static final String UNICODE_SECTION_SIGN = "\u00a7";
    public static final String UNICODE_UP_ARROW = "\u2227";
    public static final String UNICODE_DOWN_ARROW = "\u2228";
    public static final String UNICODE_CHECK = "\u2611";
    public static final String UNICODE_X = "\u2612";
    public static final String UNICODE_REFRESH = "🗘";
    public static final Component ERROR_CREATIVE = Component.empty().append("Creative required").withStyle(ChatFormatting.RED);
    public static final Tooltip TT_CREATIVE = Tooltip.create(ERROR_CREATIVE);

    private static String prevTooltipRaw = null;
    private static String prevTooltipNbt = null;
    private static long prevTooltipTime = 0L;
    private static long prevTooltipCopyTime = 0L;
    private static final int TOOLTIP_COPY_COOLDOWN = 1500;
    private static int prevTooltipScroll = -1;
    private static List<FormattedCharSequence> prevTooltipCache = null;

    private boolean unsel = false;
    private boolean hasTitle = false;
    protected final WidgetUtil WIDGET_UTIL;
    protected ScrollList SCROLL_PANE = null;
    protected TextSuggestor suggs = null;

    protected void setupScrollPane() {
        setupScrollPane(true, false);
    }

    protected void setupScrollPane(boolean narrow, boolean slotHeight) {
        SCROLL_PANE = new ScrollList(narrow, slotHeight);
        this.addRenderableWidget(SCROLL_PANE);
    }

    protected ScrollList paneScroll() {
        return SCROLL_PANE;
    }

    public static List<FormattedCharSequence> setCurrentTooltip(Component text) {
        prevTooltipTime = System.currentTimeMillis();
        String newTooltipNbt = BlackMagick.textComponentToSnbt(text);
        if (prevTooltipNbt == null || !prevTooltipNbt.equals(newTooltipNbt)) {
            prevTooltipNbt = newTooltipNbt;
            prevTooltipRaw = BlackMagick.textComponentToStringLiteral(text);
            prevTooltipScroll = 0;
            prevTooltipCache = null;
        }
        return prevTooltipCache;
    }
    public static void setCurrentTooltipScroll(int i) {
        prevTooltipScroll = i;
    }
    public static int getCurrentTooltipScroll() {
        return prevTooltipScroll;
    }

    public GenericScreen() {
        super(GameNarrator.NO_TITLE);
        WIDGET_UTIL = createWidgetUtil();
    }

    public GenericScreen(String title) {
        super(title == null ? GameNarrator.NO_TITLE : Component.nullToEmpty(title));
        if (title != null)
            hasTitle = true;
        WIDGET_UTIL = createWidgetUtil();
    }

    public GenericScreen(Component title) {
        super(title == null ? GameNarrator.NO_TITLE : title);
        if (title != null)
            hasTitle = true;
        WIDGET_UTIL = createWidgetUtil();
    }

    private WidgetUtil createWidgetUtil() {
        return new WidgetUtil(this);
    }

    public boolean shouldCloseOnKeybind() {
        return true;
    }

    protected Identifier getBackgroundTexture() {
        return TEXTURE_GENERIC;
    }

    protected void renderBehindBackgroundTexture(GuiGraphics context) {}

    protected void changeScreen(Screen newScreen) {
        this.onCloseAction();
        minecraft.setScreen(newScreen);
    }

    protected void addBackButton() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"),
            btn -> changeScreen(FortytwoEdit.DEFAULT_SCREEN.get())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
    }

    protected void addBackButton(Supplier<GenericScreen> backScreen) {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"),
            btn -> changeScreen(backScreen.get())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.backgroundWidth) / 2;
        y = (this.height - this.backgroundHeight) / 2;
        FortytwoEdit.quickScreen = FortytwoEdit.DEFAULT_SCREEN;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(context);
        this.renderBehindBackgroundTexture(context);

        Identifier backgroundTexture = getBackgroundTexture();
        if (backgroundTexture != null)
		    context.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
        if (super.mouseClicked(mouseButtonEvent, doubleTap))
            return true;
        unsel();
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (suggs == null && System.currentTimeMillis()-prevTooltipTime < 100) {
            if (keyEvent.hasControlDown() && keyEvent.key() == GLFW.GLFW_KEY_C) {
                if (prevTooltipNbt != null) {
                    if (keyEvent.hasAltDown()) {
                        if (System.currentTimeMillis() - prevTooltipCopyTime > TOOLTIP_COPY_COOLDOWN || !prevTooltipNbt.equals(FortytwoEdit.getClipboard())) {
                            FortytwoEdit.setClipboard(prevTooltipNbt);
                            FortytwoEdit.showToast("Clipboard", "Tooltip component copied");
                            prevTooltipCopyTime = System.currentTimeMillis();
                        }
                    }
                    else {
                        if (System.currentTimeMillis() - prevTooltipCopyTime > TOOLTIP_COPY_COOLDOWN || !prevTooltipRaw.equals(FortytwoEdit.getClipboard())) {
                            FortytwoEdit.setClipboard(prevTooltipRaw);
                            FortytwoEdit.showToast("Clipboard", "Tooltip text copied");
                            prevTooltipCopyTime = System.currentTimeMillis();
                        }
                    }
                }
                return true;
            }
            if (keyEvent.hasControlDown() && keyEvent.key() == GLFW.GLFW_KEY_PAGE_UP) {
                if (keyEvent.hasAltDown()) {
                    if (prevTooltipScroll != 0) {
                        prevTooltipScroll = 0;
                        prevTooltipCache = null;
                    }
                }
                else {
                    if (prevTooltipScroll > 0) {
                        prevTooltipScroll--;
                        prevTooltipCache = null;
                    }
                }
                return true;
            }
            if (keyEvent.hasControlDown() && keyEvent.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
                if (keyEvent.hasAltDown()) {
                    if (prevTooltipScroll != Integer.MAX_VALUE) {
                        prevTooltipScroll = Integer.MAX_VALUE;
                        prevTooltipCache = null;
                    }
                }
                else {
                    if (prevTooltipScroll < Integer.MAX_VALUE) {
                        prevTooltipScroll++;
                        prevTooltipCache = null;
                    }
                }
                return true;
            }
        }
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        if (FortytwoEdit.keyMagickGui.matches(keyEvent) || minecraft.options.keyInventory.matches(keyEvent)) {
            if (shouldCloseOnKeybind()) {
                this.onClose();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void unsel() {
        unsel = true;
    }

    protected void reloadScreen() {
        double scroll = 0;
        if (SCROLL_PANE != null)
            scroll = SCROLL_PANE.scrollAmount();
        this.rebuildWidgets();
        if (SCROLL_PANE != null)
            SCROLL_PANE.setScrollAmount(scroll);
    }

    protected void onCloseAction() {}

    @Override
    public void onClose() {
        onCloseAction();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (hasTitle)
            context.drawCenteredString(this.font, this.getTitle(), this.width / 2, y+11, TEXT_COLOR);
    }

    @Override
    public void tick() {
        if (unsel) {
            clearFocus();
            unsel = false;
        }

        super.tick();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleList}
     */
    protected class ScrollList extends ContainerObjectSelectionList<ScrollRow> {

        public static final int AREA_WIDTH_OFFSET = -30;
        public static final int AREA_HEIGHT_OFFSET = -32-5;
        public static final int AREA_Y_OFFSET = 32;
        public static final int AREA_Y_OFFSET_NARROW = 9;

        public final boolean NARROW;

        public ScrollList(boolean narrow, boolean slotHeight) {
            super(GenericScreen.this.minecraft,
                GenericScreen.this.width+AREA_WIDTH_OFFSET,
                GenericScreen.this.backgroundHeight+AREA_HEIGHT_OFFSET-(narrow ? AREA_Y_OFFSET_NARROW : 0),
                GenericScreen.this.y+AREA_Y_OFFSET+(narrow ? AREA_Y_OFFSET_NARROW : 0),
                slotHeight ? ItemSlotButton.SLOT_HEIGHT : ROW_HEIGHT);
            NARROW = narrow;
        }

        public ScrollRow addRow() {
            this.addEntry(new ScrollRow(NARROW));
            return getRow();
        }

        public ScrollRow addRow(ScrollRow row) {
            this.addEntry(row);
            return getRow();
        }

        public ScrollRow addRow(PosWidget... posWidgets) {
            ScrollRow row = new ScrollRow(NARROW);
            for (PosWidget pw : posWidgets) {
                row.add(pw);
            }
            return addRow(row);
        }

        public ScrollRow addRow(AbstractWidget... widgets) {
            ScrollRow row = new ScrollRow(NARROW);
            for (AbstractWidget w : widgets) {
                row.add(w);
            }
            return addRow(row);
        }

        public ScrollRow addRow(String title) {
            return addRow(title, true);
        }

        public ScrollRow addRow(String title, boolean centered) {
            return addRow(Component.nullToEmpty(title), centered);
        }

        public ScrollRow addRow(Component title) {
            return addRow(title, true);
        }

        public ScrollRow addRow(Component title, boolean centered) {
            ScrollRow row = new ScrollRow(NARROW);
            row.add(new MultiLineTextWidget(Component.empty().withColor(LABEL_COLOR).append(title), GenericScreen.this.font), true, MULTI_LINE_TEXT_WIDGET_Y_OFFSET);
            if (centered)
                row.center();
            return addRow(row);
        }

        public ScrollRow getRow() {
            if (this.children().isEmpty())
                this.addRow();
            return this.children().getLast();
        }

        public void centerAll() {
            for (ScrollRow row : this.children()) {
                row.center();
            }
        }

        @Override
        protected void renderListSeparators(GuiGraphics context) {}

        @Override
        protected void renderListBackground(GuiGraphics context) {}

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
            if (super.mouseClicked(mouseButtonEvent, doubleTap))
                return true;
            unsel();
            return false;
        }

    }
    
    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleEntry}
     */
    protected class ScrollRow extends ContainerObjectSelectionList.Entry<ScrollRow> {

        protected final List<PosWidget> children = Lists.newArrayList();
        protected final List<AbstractWidget> childrenCache = Lists.newArrayList();
        protected final int LEFT_START;

        public ScrollRow(boolean narrow) {
            this.LEFT_START = (narrow ? WID_LEFT_NARROW : WID_LEFT) + SCROLL_ROW_LEFT_OFFSET;
        }

        private void set(int i, PosWidget posWidget) {
            childrenCache.clear();
            if (i==-1)
                children.add(posWidget);
            else
                children.set(i, posWidget);
        }

        public void add(PosWidget posWidget) {
            set(-1, posWidget);
        }

        public void add(AbstractWidget w, int x, int y) {
            add(new PosWidget(w, x, y));
        }

        public void add(AbstractWidget w, boolean padLeft, int y) {
            add(w, children.isEmpty() ? LEFT_START : (getRight() + (padLeft ? WID_SPACE : 0)), y);
        }

        public void add(AbstractWidget w, boolean padLeft) {
            add(w, padLeft, 0);
        }

        public void add(AbstractWidget w) {
            add(w, true);
        }

        public void center() {
            int left = getLeft();
            int right = getRight();
            int offset = ((backgroundWidth - (right - left)) / 2) + SCROLL_ROW_LEFT_OFFSET - left;
            
            for (int i=0; i<children.size(); i++) {
                set(i, PosWidget.create(children.get(i).w(), children.get(i).x() + offset, children.get(i).y()));
            }
        }

        private int getLeft() {
            int temp = 0;
            if (!this.children.isEmpty())
                temp = this.children.get(0).x();
            for (PosWidget pw : this.children)
                if (pw.x() < temp)
                    temp = pw.x();
            return temp;
        }

        private int getRight() {
            int temp = 0;
            if (!this.children.isEmpty())
                temp = this.children.get(0).x() + this.children.get(0).w().getWidth();
            for (PosWidget pw : this.children) {
                int thisRight = pw.x() + pw.w().getWidth();
                if (thisRight > temp)
                    temp = thisRight;
            }
            return temp;
        }

        private List<AbstractWidget> getChildrenCache() {
            if (childrenCache.isEmpty()) {
                for (PosWidget pw : this.children)
                    childrenCache.add(pw.w());
            }
            return this.childrenCache;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return getChildrenCache();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return getChildrenCache();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
            if (super.mouseClicked(mouseButtonEvent, doubleTap))
                return true;
            unsel();
            return false;
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for (PosWidget posWidget : this.children) {
                posWidget.repositionInRow(this);
                posWidget.w().render(context, mouseX, mouseY, tickDelta);
            }
        }

    }

    protected static record PosWidget(AbstractWidget w, int x, int y) {

        public static PosWidget create(AbstractWidget w, int x, int y) {
            return new PosWidget(w, x, y);
        }

        public static PosWidget create(AbstractWidget w, int x) {
            return create(w, x, 0);
        }

        public static PosWidget create(AbstractWidget w) {
            return create(w, 0, 0);
        }

        public void repositionInScreen(GenericScreen screen) {
            w.setPosition(screen.x + x, screen.y + y);
        }

        public void repositionInRow(ScrollRow row) {
            w.setPosition(row.getContentX() + x, row.getContentY() + y);
        }

        public void repositionInRow(RowWidget row) {
            w.setPosition(row.getContentX() + x, row.getContentY() + y);
        }

    }

}
