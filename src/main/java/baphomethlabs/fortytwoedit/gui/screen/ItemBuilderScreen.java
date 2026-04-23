package baphomethlabs.fortytwoedit.gui.screen;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.storage.TagValueOutput;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.BlackMagick.ParsedText;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.ItemStackPreset;
import baphomethlabs.fortytwoedit.PathHelper;
import baphomethlabs.fortytwoedit.SuggestionHelper;
import baphomethlabs.fortytwoedit.PathHelper.NbtType;
import baphomethlabs.fortytwoedit.PathHelper.PathFlag;
import baphomethlabs.fortytwoedit.PathHelper.PathInfo;
import baphomethlabs.fortytwoedit.PathHelper.PathNode;
import baphomethlabs.fortytwoedit.PathHelper.PathType;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;

public class ItemBuilderScreen extends GenericScreen {

    protected static int tab = 0;
    protected static final int TAB_OFFSET = 5;
    protected static final int TAB_SIZE = 24;
    protected static final int TAB_SPACING = 2;
    private static final int LEFT_TABS = 3;
    protected static final Tab[] tabs = new Tab[]{new Tab(0,"Components",ItemStackPreset.set(Items.GOLDEN_SWORD)),
        new Tab(1,"Presets",FortytwoEdit.HEAD42), new Tab(2,"Custom Data",ItemStackPreset.set(Items.COMMAND_BLOCK)),
        new Tab(3,"Inventory",ItemStackPreset.set(Items.ENDER_CHEST)), new Tab(4,"Saved Items",ItemStackPreset.set(Items.JIGSAW)),
        new Tab()};
    protected static final int CACHE_TAB_MAIN = 0;
    protected static final int CACHE_TAB_PRESETS = 1;
    protected static final int CACHE_TAB_NBT = 2;
    protected static final int CACHE_TAB_INV = 3;
    protected static final int CACHE_TAB_SAVED = 4;
    protected static final int CACHE_TAB_BLANK = 5;
    private boolean firstInit = true;
    private static boolean firstInitStatic = true;
    protected static final int ROW_TOP = 36; // used for noScrollWidgets to align to the top of the first row
    protected static final int ROW_LEFT_LOCKED = 10;
    protected static final int ROW_RIGHT_LOCKED = ROW_LEFT_LOCKED+ROW_WIDTH;
    protected static final int ROW_LEFT_SCROLL = ROW_LEFT_LOCKED+SCROLL_ROW_LEFT_OFFSET;
    protected static final int ROW_RIGHT_SCROLL = ROW_RIGHT_LOCKED+SCROLL_ROW_LEFT_OFFSET;
    private static boolean runSuggsTest = true;
    protected ItemStack selItem = ItemStack.EMPTY;
    protected ItemStack selItemOff = ItemStack.EMPTY;
    protected ItemSlotButton itemBtn = null;
    protected Button swapBtn;
    protected Button swapCopyBtn;
    protected Button throwCopyBtn;
    protected Button hotbarLeftBtn;
    protected Button hotbarRightBtn;
    private SmartEditBox txtFormat;
    private TabWidget tabWidget;
    private final List<List<PosWidget>> TAB_WIDGETS_LOCKED = Lists.newArrayList();
    private final List<List<RowWidget>> TAB_WIDGETS_SCROLL = Lists.newArrayList();
    private final Set<AbstractWidget> UNSAVED_TEXT_WIDGETS = Sets.newHashSet();
    private final Set<AbstractWidget> ALL_TEXT_WIDGETS = Sets.newHashSet();
    private final Set<AbstractWidget> ALL_SLIDER_WIDGETS = Sets.newHashSet();
    private final Map<WidgetCacheType,AbstractWidget> WIDGET_CACHE = Maps.newHashMap();
    public static boolean savedModeSet = false;
    private Map<Integer,String> savedItems = Maps.newHashMap();
    public static boolean savedItemsError = false;
    private String inpError = null;
    private String inpErrorTrim = null;
    private MultiLineTextWidget lblInpError;
    private static boolean viewBlackMarket = false;
    private static final Tooltip TOOLTIP_BLACK_MARKET =
        Tooltip.create(grayWhiteText("Black Market Items","\n\nGet custom items produced by ")
        .append(Component.empty().append("BaphomethLabs").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
        .append(grayWhiteText("","\n(Mostly Harmless)")));
    private static final Tooltip TOOLTIP_LOCAL_ITEMS =
        Tooltip.create(grayWhiteText("Local Items","\n\nSave items for later without using up your saved hotbars"));
    private static final ItemStackPreset[] SAVED_TAB_MODE_ITEMS = new ItemStackPreset[]{ItemStackPreset.set(
        "{id:player_head,components:{profile:{properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQ"
        +"iIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pb"
        +"mVjcmFmdC5uZXQvdGV4dHVyZS9iZDlmMThjOWQ4NWY5MmY3MmY4NjRkNjdjMTM2N2U5YTQ1ZGMxMGYzNzE1NDljNDZhNGQ0ZGQ5ZTRmMTN"
        +"mZjQiDQogICAgfQ0KICB9DQp9\"}]}}}"),
        ItemStackPreset.set("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlN"
        +"LSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MjY0ODZmNDI0ODljZWYwMmM5ZTk4ZGQ4Y"
        +"mU1YTNmMzhlODc5MTQ3NTQzMjZlNzdjODM3YzFiMmJjYmE2NSINCiAgICB9DQogIH0NCn0=\"}]}}}")};
    protected static final Identifier DELETE_ITEM_OVERLAY = Identifier.withDefaultNamespace("container/beacon/cancel");
    protected static final int DELETE_ITEM_OVERLAY_SIZE = 16;
    private ArmorStand renderArmorStand;
    private ArmorStand renderArmorPose;
    protected final int playerX = backgroundWidth+10;
    protected final int playerY = -10;
    protected final int bookX = backgroundWidth - 20 - 13;
    protected final int bookY = 7;
    private static final int ENTITY_RENDER_SIZE = 35;
    private boolean prevArmorStand = false;
    private List<List<Set<PoseSlider>>> poseSliders = Lists.newArrayList();
    private List<Set<Button>> poseSliderBtns = Lists.newArrayList();
    private static CompoundTag poseCompound = new CompoundTag();
    private static final String[] poseTypes = new String[]{"Head","Body","RightArm","LeftArm","RightLeg","LeftLeg"};
    public static final String BANNER_PRESET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
    public static final String[] BANNER_CHAR_LIST = new String[BANNER_PRESET_CHARS.replaceAll("\\s","").length()+1];
    private boolean suggsPause = false;
    private Set<SmartEditBox> currentTxt = Sets.newHashSet();
    private static int[][] colorSets = {{66,6,102},{0,0,0}};
    private static float[] colorHsl = {0f,0f,0f};
    private List<Set<SmartEditBox>> colorHexTxts = Lists.newArrayList();
    private List<Set<SmartEditBox>> colorDecTxts = Lists.newArrayList();
    private List<List<Set<RgbSlider>>> colorRgbSliders = Lists.newArrayList();
    private List<Set<RgbSlider>> colorHslSliders = Lists.newArrayList();
    private List<List<Set<PosWidget>>> colorItemWids = Lists.newArrayList();
    private boolean editorLocked = false;
    private boolean hslLock = false;
    private boolean editorOutputLocked = false;
    private Set<AbstractWidget> editorLockedWidget = Sets.newHashSet();
    private static final ItemStackPreset[] RGB_ITEMS = //to_do old code
        new ItemStackPreset[]{ItemStackPreset.set(Items.LEATHER_CHESTPLATE),ItemStackPreset.set(Items.POTION),ItemStackPreset.set(Items.FILLED_MAP)};
    private Component textComponentPreview = null;
    private boolean textComponentPreviewBook = false;
    private static double[] tabScroll = new double[tabs.length];
    private boolean pauseSaveScroll = false;
    protected NbtEdit nbtEdit = null;
    protected Map<String,Double> nbtEditScroll = Maps.newHashMap();
    protected boolean nbtEditScrollNow = false;
    protected static NbtEditStyle nbtEditStyle = NbtEditStyle.TEMPLATE;
    protected static final ItemStackPreset[] NBT_EDIT_STYLE_ITEMS =
        new ItemStackPreset[]{ItemStackPreset.set(Items.KNOWLEDGE_BOOK),ItemStackPreset.set(Items.COMMAND_BLOCK),ItemStackPreset.set(Items.NAME_TAG)};
    private boolean bannerShield = false;
    private static ArmorStand bannerChangePreview = null;
    protected boolean showBannerPreview = false;
    protected boolean showPosePreview = false;
    private ItemStack[] cacheInv = new ItemStack[41]; // 0-26 for inventory, 27-35 for hotbar, 36-40 for armor, 41 for offhand
    private int cacheInvSlot = -1;
    public static Tooltip FORMAT_CODES_TT = null;

    public ItemBuilderScreen() {
        super("");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = ItemBuilderScreen::new;

        if (firstInit) {
            if (firstInitStatic) {
                firstInitStatic = false;
                FORMAT_CODES_TT = Tooltip.create(BlackMagick.textComponentFromSnbt("[\"Formatting\n"+
                    "0-black§r 1-§1dark_blue§r 2-§2dark_green§r 3-§3dark_aqua§r 4-§4dark_red§r 5-§5dark_purple§r "+
                    "6-§6gold§r 7-§7gray§r 8-§8dark_gray§r 9-§9blue§r a-§agreen§r b-§baqua§r "+
                    "c-§cred§r d-§dlight_purple§r e-§eyellow§r f-§fwhite§r #420666-\",{text:\"0xRRGGBB\",color:\"#420666\"},"+
                    "\"\nr-§rreset§r k-obfuscated§r l-§lbold§r m-§mstrikethrough§r n-§nunderlined§r o-§oitalic§r\","+
                    "\"\n\nFonts\ndefault- ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\nuniform- \",{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\",font:\"uniform\"},"+
                    "\"\nalt- \",{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",font:\"alt\"},\"\nillageralt- \","+
                    "{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\",font:\"illageralt\"}]").text());

                int foundComponentPaths = 0;
                for (String c : SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList()) {
                    // this will log warnings if PathHelper doesnt include a vanilla component
                    PathInfo pi = PathHelper.getItemPath(null, PathNode.of("components"),PathNode.of(c));
                    if (!pi.isEmpty())
                        foundComponentPaths++;
                    else
                        FortytwoEdit.logWarn("No PathInfo found for component \""+c+"\"");
                }
                FortytwoEdit.logInfo("Found PathInfo for "+foundComponentPaths+"/"+SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList().size()+" components");
            }

            if (tabs[tab].hideTabs())
                tab = CACHE_TAB_MAIN;

            updateArmorStand(null);

            for (int i=0; i<colorSets.length; i++) {
                colorHexTxts.add(Sets.newHashSet());
                colorDecTxts.add(Sets.newHashSet());
                colorRgbSliders.add(Lists.newArrayList());
                for (int j=0; j<3; j++)
                    colorRgbSliders.get(colorRgbSliders.size()-1).add(Sets.newHashSet());
                colorItemWids.add(Lists.newArrayList());
                for (int j=0; j<3; j++)
                    colorItemWids.get(colorItemWids.size()-1).add(Sets.newHashSet());
            }
            for (int i=0; i<3; i++)
                colorHslSliders.add(Sets.newHashSet());
            for (int i=0; i<6; i++) {
                poseSliderBtns.add(Sets.newHashSet());
                poseSliders.add(Lists.newArrayList());
                for (int j=0; j<3; j++)
                    poseSliders.get(poseSliders.size()-1).add(Sets.newHashSet());
            }

            SuggestionHelper.clearDynamicListCaches();
            firstInit = false;
        }
        pauseSaveScroll = false;

        if (runSuggsTest) {
            runSuggsTest = false;
            SuggestionHelper.runAllListMethods();
        }

        if (!tabs[tab].hideTabs()) {

            //tabs
            for (int posNum = 0; posNum<tabs.length; posNum++)
                for (int i = 0; i<tabs.length; i++)
                    if (tabs[i].pos()==posNum) {
                        int tabNum = i;
                        int tabX;
                        int tabY;
                        if (posNum<LEFT_TABS) {
                            tabX = x-TAB_SIZE;
                            tabY = y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(posNum);
                        }
                        else {
                            tabX = x+backgroundWidth;
                            tabY = y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(posNum-LEFT_TABS);
                        }
                        ItemSlotButton w = new ItemSlotButton(this, tabX, tabY, TAB_SIZE, tabs[i].display().get(), btn -> this.btnTab(tabNum));
                        w.setTooltip(Tooltip.create(Component.nullToEmpty(tabs[i].lbl())));
                        w.showSlot(false);
                        if (tab==i)
                            w.active = false;
                        this.addRenderableWidget(w);
                    }

            //main
            this.addBackButton();
            txtFormat = new SmartEditBox(this.font,x+50,y+5+1,15,18,Component.nullToEmpty(""));
            txtFormat.setEditable(false);
            txtFormat.setValue(UNICODE_SECTION_SIGN);
            txtFormat.setTooltip(FORMAT_CODES_TT);
            swapCopyBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("c*"),
                button -> this.btnSwapOff(true)).bounds(width/2 - 50,y+5,20,WID_HEIGHT).build());
            swapBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("c"),
                button -> this.btnSwapOff(false)).bounds(width/2 - 30,y+5,15,WID_HEIGHT).build());
            hotbarLeftBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("<"),
                button -> this.btnChangeSlot(true)).bounds(width/2 - 15,y+5,15,WID_HEIGHT).build());
            hotbarRightBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty(">"),
                button -> this.btnChangeSlot(false)).bounds(width/2,y+5,15,WID_HEIGHT).build());
            Button throwBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("Q"),
                button -> this.btnThrow(false)).bounds(width/2 + 15,y+5,15,WID_HEIGHT).build());
            throwCopyBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("Q*"),
                button -> this.btnThrow(true)).bounds(width/2 + 30,y+5,20,WID_HEIGHT).build());

            if (!BlackMagick.isCreative(minecraft)) {
                swapCopyBtn.active = false;
                swapCopyBtn.setTooltip(TT_CREATIVE);
                throwCopyBtn.active = false;
                throwCopyBtn.setTooltip(TT_CREATIVE);
            }
            if (BlackMagick.isSpectator(minecraft)) {
                swapCopyBtn.active = false;
                swapCopyBtn.setTooltip(null);
                swapBtn.active = false;
                swapBtn.setTooltip(null);
                hotbarLeftBtn.active = false;
                hotbarLeftBtn.setTooltip(null);
                hotbarRightBtn.active = false;
                hotbarRightBtn.setTooltip(null);
                throwBtn.active = false;
                throwBtn.setTooltip(null);
                throwCopyBtn.active = false;
                throwCopyBtn.setTooltip(null);
            }
            if (swapCopyBtn.active)
                swapCopyBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Copy item to offhand")));
            if (swapBtn.active)
                swapBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Swap item with offhand")));
            if (hotbarLeftBtn.active)
                hotbarLeftBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Scroll hotbar left")));
            if (hotbarRightBtn.active)
                hotbarRightBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Scroll hotbar right")));
            if (throwBtn.active)
                throwBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Throw item")));
            if (throwCopyBtn.active)
                throwCopyBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Throw a copy of item")));

            compareItems();

            itemBtn = this.addRenderableWidget(new ItemSlotButton(this, x+backgroundWidth-20-5, y+5, 20, selItem, button -> this.btnCopySelItemNbt()));
            if (selItem==null || selItem.isEmpty()) {
                itemBtn.active = false;
                itemBtn.setTooltip(null);
            }
            else {
                itemBtn.active = true;
                itemBtn.setTooltip(makeItemTooltip(selItem));
            }
        }

        //tabs
        if (TAB_WIDGETS_SCROLL.isEmpty())
            createStaticTabs();
        this.tabWidget = null;
        for (PosWidget p : TAB_WIDGETS_LOCKED.get(tab)) {
            p.repositionInScreen(this);
            this.addRenderableWidget(p.w());
        }
        if (!TAB_WIDGETS_SCROLL.get(tab).isEmpty()) {
            this.tabWidget = new TabWidget(tab);
            this.tabWidget.setScrollAmount(tabScroll[tab]);
            if (nbtEditScrollNow) {
                nbtEditScrollNow = false;
                if (nbtEdit != null && nbtEditScroll.containsKey(nbtEdit.fullPath())) {
                    tabWidget.setScrollAmount(nbtEditScroll.get(nbtEdit.fullPath()));
                }
            }
            this.addRenderableWidget(this.tabWidget);
        }

        //banner prev
        if (bannerChangePreview == null) {
            bannerChangePreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
            bannerChangePreview.yBodyRot = 210.0f;
            bannerChangePreview.setXRot(25.0f);
            bannerChangePreview.yHeadRot = bannerChangePreview.getYRot();
            bannerChangePreview.yHeadRotO = bannerChangePreview.getYRot();
            bannerChangePreview.load(BlackMagick.valueInputFromCompound(BlackMagick.validCompoundFromString("{Invisible:true,Pose:{RightArm:[-90f,-90f,0f]}}")));
        }

        //banner
        int i=1;
        BANNER_CHAR_LIST[0] = "*";
        for (char c : BANNER_PRESET_CHARS.replaceAll("\\s","").toCharArray()) {
            BANNER_CHAR_LIST[i] = ""+c;
            i++;
        }

        // this should always be the last thing in init()
        updateItem();
    }

    protected void btnSwapOff(boolean copy) {
        if (!BlackMagick.isSpectator(minecraft)) {
            if (!copy) {
                // from MinecraftClient (search `this.options.swapHandsKey.wasPressed()`)
                minecraft.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
            else if (BlackMagick.isCreative(minecraft)) {
                if (!minecraft.player.getMainHandItem().isEmpty()) {
                    BlackMagick.setItemOff(minecraft.player.getMainHandItem());
                }
                else if (!minecraft.player.getOffhandItem().isEmpty()) {
                    BlackMagick.setItemMain(minecraft.player.getOffhandItem());
                }
            }
        }
        unsel();
    }

    protected void btnChangeSlot(boolean left) {
        if (!BlackMagick.isSpectator(minecraft)) {
            int slot = minecraft.player.getInventory().getSelectedSlot();
            if (left)
                slot--;
            else
                slot++;
            if (slot<0)
                slot = 8;
            else if (slot>8)
                slot = 0;
            minecraft.player.getInventory().setSelectedSlot(slot);
        }
        unsel();
    }

    protected void btnThrow(boolean copy) {
        if (!BlackMagick.isSpectator(minecraft)) {
            if (!copy) {
                if (minecraft.player.drop(true))
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                minecraft.player.inventoryMenu.broadcastChanges();
            }
            else if (BlackMagick.isCreative(minecraft)) {
                ItemStack item = minecraft.player.getMainHandItem().copy();
                if (minecraft.player.drop(true))
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                minecraft.player.inventoryMenu.broadcastChanges();
                BlackMagick.setItemMain(item);
            }
        }
        unsel();
    }

    protected void btnTab(int i) {
        if (!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        if (tab != i && (i == CACHE_TAB_NBT))
            setErrorMsg(null);
        tab = i;
        reloadScreen();
        resetSuggs();
        unsel();
    }

    protected void btnCopySelItemNbt() {
        btnCopyItemNbt(selItem);
    }

    protected void btnCopyItemNbt(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            String itemData = BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(stack));
            FortytwoEdit.setClipboard(itemData);
            FortytwoEdit.showToast("Item Builder","Item NBT copied to clipboard");
        }
        unsel();
    }

    private void updateItem() {
        minecraft.player.inventoryMenu.broadcastChanges();
        boolean changed = false;
        if (!ItemStack.matches(selItem,minecraft.player.getMainHandItem()) && !TAB_WIDGETS_SCROLL.isEmpty())
            changed = true;
        boolean changedOff = false;
        if (!ItemStack.matches(selItemOff,minecraft.player.getOffhandItem()) && !TAB_WIDGETS_SCROLL.isEmpty())
            changedOff = true;

        if (changed) {
            selItem = minecraft.player.getMainHandItem().copy();
            FortytwoEdit.addItemHist(selItem);

            if (itemBtn != null) {
                itemBtn.setItem(selItem);
                if (selItem==null || selItem.isEmpty()) {
                    itemBtn.active = false;
                    itemBtn.setTooltip(null);
                }
                else {
                    itemBtn.active = true;
                    itemBtn.setTooltip(makeItemTooltip(selItem));
                }
            }

            if (tab==CACHE_TAB_MAIN)
                setErrorMsg(null);

            if (selItem.is(Items.ARMOR_STAND)) {
                updateArmorStand(selItem.copy());
                prevArmorStand = true;
            }
            else if (prevArmorStand) {
                updateArmorStand(null);
                prevArmorStand = false;
            }

            if (widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                MultiLineEditBox giveBox = (MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX);
                boolean wasUnsaved = testUnsaved(giveBox);
                (giveBox).setValue(
                    (giveBox).getValue());
                if (!wasUnsaved) {
                    markSaved(giveBox);
                }
            }

            if (!TAB_WIDGETS_SCROLL.isEmpty()) {
                createTab(CACHE_TAB_MAIN);
            }
        }
        if (changedOff) {
            selItemOff = minecraft.player.getOffhandItem().copy();
            FortytwoEdit.addItemHist(selItemOff);
        }
        if (changed || changedOff)
            compareItems();
    }

    private void compareItems() {

        swapBtn.setTooltip(swapBtn.active ? Tooltip.create(Component.nullToEmpty("Swap item with offhand")) : null);
        if (!minecraft.player.getMainHandItem().isEmpty() && !minecraft.player.getOffhandItem().isEmpty()) {
            if (ItemStack.isSameItemSameComponents(minecraft.player.getMainHandItem(),minecraft.player.getOffhandItem())) {
                swapBtn.setMessage(Component.empty().append("c").withStyle(ChatFormatting.GREEN));
            }
            else {
                swapBtn.setMessage(Component.empty().append("c").withStyle(ChatFormatting.RED));
                swapBtn.setTooltip(Tooltip.create(Component.empty().append("Swap item with offhand\n\n").append(BlackMagick.getElementDifferences(BlackMagick.itemToNbtStorage(minecraft.player.getOffhandItem()),
                    BlackMagick.itemToNbtStorage(minecraft.player.getMainHandItem())))));
            }
        }
        else {
            swapBtn.setMessage(Component.nullToEmpty("c"));
        }

    }

    protected void markUnsaved(AbstractWidget widget) {
        this.UNSAVED_TEXT_WIDGETS.add(widget);
    }

    protected void markSaved(AbstractWidget widget) {
        this.UNSAVED_TEXT_WIDGETS.remove(widget);
    }

    protected boolean testUnsaved(AbstractWidget widget) {
        return this.UNSAVED_TEXT_WIDGETS.contains(widget);
    }

    protected boolean activeTxt() {
        for (AbstractWidget w : ALL_TEXT_WIDGETS)
            if (w.isFocused())
                return true;
        return false;
    }

    protected boolean activeSlider() {
        for (AbstractWidget w : ALL_SLIDER_WIDGETS)
            if (w.isFocused())
                return true;
        return false;
    }

    public static Tooltip makeItemTooltip(ItemStack stack) {
        if (stack==null || stack.isEmpty())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        return Tooltip.create(Component.empty().append(stack.getStyledHoverName()).append("\n")
            .append(BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(stack))));
    }

    public static Tooltip makeItemTooltip(CompoundTag nbt, ItemStack stack) {
        if (nbt==null || !nbt.getString("id").isPresent())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        return Tooltip.create(Component.empty().append(stack == null ?
            BlackMagick.textComponentFromSnbt("{text:\"Failed to read item\",color:\"red\"}").text()
            : stack.getStyledHoverName()
            ).append("\n").append(BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(stack))));
    }

    public static Tooltip makeItemTooltip(String nbtString) {
        if (nbtString==null || nbtString.isEmpty())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        return Tooltip.create(Component.empty().append(BlackMagick.textComponentFromSnbt("{text:\"Failed to read item\",color:\"red\"}").text()
            ).append("\n"+nbtString));
    }

    private void refreshSaved() {
        savedItems = FortytwoEdit.getSavedItems();
        updateSavedTab();
    }
    private void updateSavedTab() {
        if (TAB_WIDGETS_SCROLL.get(CACHE_TAB_SAVED).size() >= FortytwoEdit.SAVED_ROWS)
            for (int i=0; i<FortytwoEdit.SAVED_ROWS; i++) {
                RowWidget row = TAB_WIDGETS_SCROLL.get(CACHE_TAB_SAVED).get(i);
                if (row instanceof RowWidgetSavedItemsRow row2)
                    row2.updateSavedDisplay();
            }
    }

    private void updateSavedModeButtons() {
        if (widgetCacheTest(WidgetCacheType.BTN_SAVED_SOURCE, WidgetCacheType.BTN_SAVED_MODE)) {
            ItemSlotButton btnSource = (ItemSlotButton)widgetCacheGet(WidgetCacheType.BTN_SAVED_SOURCE);
            Button btnMode = (Button)widgetCacheGet(WidgetCacheType.BTN_SAVED_MODE);
            if (viewBlackMarket) {
                btnSource.setTooltip(TOOLTIP_BLACK_MARKET);
                btnSource.setItem(SAVED_TAB_MODE_ITEMS[1].get());
                btnMode.setTooltip(Tooltip.create(Component.nullToEmpty("Refresh from Web")));
                btnMode.setMessage(Component.nullToEmpty(UNICODE_REFRESH));
            }
            else {
                btnSource.setTooltip(TOOLTIP_LOCAL_ITEMS);
                btnSource.setItem(SAVED_TAB_MODE_ITEMS[0].get());
                if (savedModeSet) {
                    btnMode.setTooltip(Tooltip.create(Component.nullToEmpty("C - Save to slot")));
                    btnMode.setMessage(Component.nullToEmpty("C"));
                }
                else {
                    btnMode.setTooltip(Tooltip.create(Component.nullToEmpty("V - Get item")));
                    btnMode.setMessage(Component.nullToEmpty("V"));
                }
            }
        }
    }

    protected AbstractWidget widgetCacheAdd(WidgetCacheType type, AbstractWidget widget) {
        if (type != null && type != WidgetCacheType.NONE && widget != null) {
            WIDGET_CACHE.put(type,widget);
        }
        return widget;
    }

    protected AbstractWidget widgetCacheGet(WidgetCacheType type) {
        return WIDGET_CACHE.get(type);
    }

    protected boolean widgetCacheTest(WidgetCacheType... types) {
        for (WidgetCacheType type : types) {
            if (WIDGET_CACHE.containsKey(type)) {
                if (WIDGET_CACHE.get(type) == null)
                    WIDGET_CACHE.remove(type);
                else
                    continue;
            }
            return false;
        }
        return true;
    }

    private void resetSuggs() {
        currentTxt.clear();
        suggs = null;
        if (tab != CACHE_TAB_BLANK && tab != CACHE_TAB_NBT)
            setErrorMsg(null);
    }

    private void updateArmorStand(ItemStack stand) {
        renderArmorStand = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
        renderArmorStand.yBodyRot = 210.0f;
        renderArmorStand.setXRot(25.0f);
        renderArmorStand.yHeadRot = renderArmorStand.getYRot();
        renderArmorStand.yHeadRotO = renderArmorStand.getYRot();

        if (stand != null && !stand.isEmpty() && BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data",Tag.TAG_COMPOUND) != null) {
            CompoundTag entity = (CompoundTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data");
            entity.putString("id","armor_stand");
            entity.put("Pos",BlackMagick.nbtFromSnbt("[0d,0d,0d]"));
            entity.put("Motion",BlackMagick.nbtFromSnbt("[0d,0d,0d]"));
            entity.put("Rotation",BlackMagick.nbtFromSnbt("[0f,0f]"));
            renderArmorStand.load(BlackMagick.valueInputFromCompound(entity.copy()));
        }

        updatePose();
    }

    protected void updatePose() {
        if (!editorLocked) {
            editorLocked = true;

            renderArmorPose = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
            renderArmorPose.yBodyRot = 210f;
            renderArmorPose.setXRot(25f);
            renderArmorPose.yHeadRot = renderArmorPose.getYRot();
            renderArmorPose.yHeadRotO = renderArmorPose.getYRot();
            CompoundTag nbt = new CompoundTag();
            if (renderArmorStand != null) {
                TagValueOutput val = BlackMagick.valueOutputNew();
                renderArmorStand.saveWithoutId(val);
                nbt = BlackMagick.valueOutputToCompound(val);
            }

            for (int i=0; i<poseSliders.size(); i++) {
                ListTag poseList = null;
                if (poseCompound.getList(poseTypes[i]).isPresent()) {
                    ListTag l = poseCompound.getListOrEmpty(poseTypes[i]);
                    if (l.size()==3)
                        poseList = l.copy();
                }
                for (int j=0; j<3; j++) {
                    float val = 0f;
                    if (poseList != null)
                        val = poseList.getFloatOr(j,0);
                    for (PoseSlider p : poseSliders.get(i).get(j))
                        p.setVal(val);
                }
                for (Button w : poseSliderBtns.get(i)) {
                    if (poseCompound.contains(poseTypes[i])) {
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("Remove pose")));
                        w.active = true;
                    }
                    else {
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("No pose")));
                        w.active = false;
                    }
                }
            }

            nbt.put("Pose",poseCompound.copy());
            renderArmorPose.load(BlackMagick.valueInputFromCompound(nbt.copy()));

            if (!editorOutputLocked) {
                if (widgetCacheTest(WidgetCacheType.TXT_POSE)) {
                    SmartEditBox txt = (SmartEditBox)widgetCacheGet(WidgetCacheType.TXT_POSE);
                    if (poseCompound.isEmpty())
                        txt.setValue("");
                    else
                        txt.setValue(BlackMagick.nbtToSnbt(poseCompound));
                    resetSuggs();
                }
            }

            editorLocked = false;
            editorLockedWidget.clear();
        }
    }

    private void setPoseVal(String partKey, int axis, float val) {
        if (!editorLocked && !editorOutputLocked) {
            if (!poseCompound.contains(partKey)) {
                ListTag newPart = new ListTag();
                for (int i=0; i<3; i++)
                    newPart.add(FloatTag.valueOf(0f));
                poseCompound.put(partKey,newPart);
            }

            ListTag partList = poseCompound.getListOrEmpty(partKey);
            if (partList.size()==3) {
                partList.set(axis,FloatTag.valueOf(val));
                poseCompound.put(partKey,partList);
            }

            updatePose();
        }
    }

    public static MutableComponent grayWhiteText(String... parts) {
        return alternatingText(ChatFormatting.WHITE, ChatFormatting.GRAY, parts);
    }

    public static MutableComponent grayWhiteText(String label, Component text) {
        return Component.empty().append(Component.empty().append(label).withStyle(ChatFormatting.WHITE))
            .append(Component.empty().append(text).withStyle(ChatFormatting.GRAY));
    }

    public static MutableComponent alternatingText(ChatFormatting even, ChatFormatting odd, String... parts) {
        MutableComponent text = Component.empty();
        for (int i=0; i<parts.length; i++) {
            if (i%2==0) {
                if (even == null)
                    text = text.append(parts[i]);
                else
                    text = text.append(Component.empty().append(parts[i]).withStyle(even));
            }
            else {
                if (odd == null)
                    text = text.append(parts[i]);
                else
                    text = text.append(Component.empty().append(parts[i]).withStyle(odd));
            }
        }
        return text;
    }

    public static MutableComponent errorText(String text) {
        return Component.empty().append(text).withStyle(ChatFormatting.RED);
    }

    public static MutableComponent warnText(String text) {
        return Component.empty().append(text).withStyle(ChatFormatting.YELLOW);
    }

    private void updateColorSets() {//to_do old code
        if (!editorLocked) {
            editorLocked = true;

            RGB_ITEMS[0] = ItemStackPreset.set("{id:leather_chestplate,components:{dyed_color:"+getRgbDec(0)+"}}");
            RGB_ITEMS[1] = ItemStackPreset.set("{id:potion,components:{potion_contents:{custom_color:"+getRgbDec(0)+"}}}");
            RGB_ITEMS[2] = ItemStackPreset.set("{id:filled_map,components:{map_color:"+getRgbDec(0)+"}}");

            for (int rgbNum=0; rgbNum<colorSets.length; rgbNum++) {
                for (SmartEditBox w : colorHexTxts.get(rgbNum)) {
                    if (!editorLockedWidget.contains(w)) {
                        w.setValue(""+getRgbHex(rgbNum));
                    }
                    w.setTextColor(getRgbDec(rgbNum));
                    w.setTooltip(Tooltip.create(Component.nullToEmpty(getRgbHex(rgbNum))));
                }
                for (SmartEditBox w : colorDecTxts.get(rgbNum)) {
                    if (!editorLockedWidget.contains(w)) {
                        w.setValue(""+getRgbDec(rgbNum));
                    }
                    w.setTextColor(getRgbDec(rgbNum));
                    w.setTooltip(Tooltip.create(Component.nullToEmpty(""+getRgbDec(rgbNum))));
                }
                for (int num=0; num<3; num++) {
                    for (RgbSlider w : colorRgbSliders.get(rgbNum).get(num)) {
                        w.setVal(colorSets[rgbNum][num]);
                    }
                    // for (PosWidget w : colorItemWids.get(rgbNum).get(num)) {
                    //     w.s = rgbItems[num];
                    // }
                }
            }

            if (!hslLock) {
                rgbToHsl(colorSets[0][0],colorSets[0][1],colorSets[0][2]);
                for (int num=0; num<3; num++) {
                    for (RgbSlider w : colorHslSliders.get(num)) {
                        w.setVal(colorHsl[num]);
                    }
                }
            }

            // if (textComponentEffectMode>=0)
            //     updateTextComponentEffect();

            if (!editorOutputLocked) {
                if (widgetCacheTest(WidgetCacheType.TXT_DECIMAL_COLOR)) {
                    SmartEditBox txt = (SmartEditBox)widgetCacheGet(WidgetCacheType.TXT_DECIMAL_COLOR);
                    txt.setValue(""+getRgbDec(0));
                    resetSuggs();
                }
            }

            editorLocked = false;
            editorLockedWidget.clear();
        }
    }

    private int getRgbDec(int rgbNum) {
        return colorSets[rgbNum][0]*256*256+colorSets[rgbNum][1]*256+colorSets[rgbNum][2];
    }

    private String getRgbHex(int rgbNum) {
        String hex = "#";
        for (int i=0; i<3; i++) {
            String current = Integer.toHexString(colorSets[rgbNum][i]).toUpperCase();
            if (current.length()==1)
                current = "0" + current;
            hex += current;
        }
        return hex;
    }

    private void trySetColorHex(int rgbNum, String inp, AbstractWidget w) {
        if (!editorLocked && rgbNum>=0 && rgbNum<colorSets.length && inp!=null && inp.length()>1 && inp.length()<=7 && inp.startsWith("#")) {
            boolean valid = true;
            String hex = inp.substring(1);
            int[] rgb = {0,0,0};
            while (hex.length()<6)
                hex = "0"+hex;
            for (int i=0; i<3; i++) {
                try {
                    int c = Integer.parseInt(hex.substring(2*i,2*i+2),16);
                    if (c<0 || c>255)
                        valid = false;
                    rgb[i] = c;
                }
                catch (Exception ex) {
                    valid = false;
                }
            }
            if (valid) {
                for (int i=0; i<3; i++)
                    colorSets[rgbNum][i]=rgb[i];
                if (w!=null)
                    editorLockedWidget.add(w);
                updateColorSets();
            }
        }
    }

    private void trySetColorDec(int rgbNum, String inp, AbstractWidget w) {
        trySetColorHex(rgbNum, BlackMagick.colorHexFromDec(inp), w);
    }

    // private void swapColorSets(int left, int right) {
    //     int[] temp = colorSets[left];
    //     colorSets[left] = colorSets[right];
    //     colorSets[right] = temp;
    //     updateColorSets();
    // }

    private void setHsl(int num, float val) {
        if (!editorLocked && !editorOutputLocked) {
            colorHsl[num] = val;
            hslToRgb(0,colorHsl[0],colorHsl[1],colorHsl[2]);
            hslLock = true;
            updateColorSets();
            hslLock = false;
        }
    }

    private void hslToRgb(int rgbNum, float h, float s, float v) {
        float sat = s/100f;
        float val = v/100f;
        float c = val*sat;
        float x = c*(1 - Math.abs((h/60f)%2 - 1));
        float m = val - c;

        float r0 = 0f;
        float g0 = 0f;
        float b0 = 0f;

        if (h==360)
            h=0;

        if (h<60) {
            r0 = c;
            g0 = x;
            b0 = 0f;
        }
        else if (h<120) {
            r0 = x;
            g0 = c;
            b0 = 0f;
        }
        else if (h<180) {
            r0 = 0f;
            g0 = c;
            b0 = x;
        }
        else if (h<240) {
            r0 = 0f;
            g0 = x;
            b0 = c;
        }
        else if (h<300) {
            r0 = x;
            g0 = 0f;
            b0 = c;
        }
        else {
            r0 = c;
            g0 = 0f;
            b0 = x;
        }

        colorSets[rgbNum][0] = Math.round((r0+m)*255);
        colorSets[rgbNum][1] = Math.round((g0+m)*255);
        colorSets[rgbNum][2] = Math.round((b0+m)*255);
    }

    private void rgbToHsl(int r, int g, int b) {
        float red = r/255f;
        float green = g/255f;
        float blue = b/255f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float h = 0;

        if (delta == 0f) {
            h = 0;
        }
        else if (max == red) {
            h = 60*(((green-blue)/delta)%6);
        }
        else if (max == green) {
            h = 60*(((blue-red)/delta)+2);
        }
        else if (max == blue) {
            h = 60*(((red-green)/delta)+4);
        }

        if (h<0)
            h+=360;

        float s = (max==0) ? 0 : (delta/max)*100;
        float v = max*100;

        colorHsl = new float[]{h,s,v};
    }

    private void drawItem(GuiGraphicsExtractor context, ItemStack item, int x, int y) {//to_do remove
        context.item(item,x,y);
        context.itemDecorations(this.font,item,x,y);
    }

    // private void updateTextComponentPreview(String path, String textComponentBase) { to_do old code
    //     updateTextComponentPreview(path,textComponentBase,null);
    // }
    // private void updateTextComponentPreview(String path, String textComponentBase, String textComponentEffect) {
    //     textComponentBaseText = textComponentBase;
    //     textComponentBaseValid = false;
    //     textComponentEffectValid = false;
    //     textComponentPreviewBook = false;
    //     textComponentPreview = BlackMagick.textComponentFromSnbt(textComponentBase).text();
    //     if (BlackMagick.textComponentFromSnbt(textComponentBase).isValid()) {
    //         textComponentBaseValid = true;
    //         if (textComponentEffect != null && BlackMagick.textComponentFromSnbt(appendTextComponentEffect(textComponentBase,textComponentEffect)).isValid()) {
    //             textComponentPreview = BlackMagick.textComponentFromSnbt(appendTextComponentEffect(textComponentBase,textComponentEffect)).text();
    //             if (textComponentEffect.length()>0 && !textComponentEffect.equals("{text:\"\"}"))
    //                 textComponentEffectValid = true;
    //             textComponentEffectFull = appendTextComponentEffect(textComponentBase,textComponentEffect);
    //         }
    //         if (path != null) {
    //             if (path.endsWith("custom_name"))
    //                 textComponentPreview = textComponentPreview.copy().withStyle(ChatFormatting.ITALIC);
    //             else if (path.contains("lore[")) {
    //                 textComponentPreview = textComponentPreview.copy().withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_PURPLE);
    //             }
    //             else if (path.contains("written_book_content.pages["))
    //                 textComponentPreviewBook = true;
    //         }
    //     }
    //     if (textComponentEffectMode>=0 && widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_ADD_BTN)) {
    //         Button btnAdd = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_ADD_BTN);
    //         if (textComponentEffectValid && textComponentEffect != null && textComponentEffect.length()>0) {
    //             btnAdd.active = true;
    //             btnAdd.setTooltip(Tooltip.create(grayWhiteText("Set text to:\n",textComponentEffectFull)));
    //         }
    //         else {
    //             btnAdd.active = false;
    //             btnAdd.setTooltip(Tooltip.create(errorText("Invalid Text Component")));
    //         }
    //     }
    // }

    // private String appendTextComponentEffect(String textComponentBase, String textComponentEffect) {
    //     if (textComponentBase.length()>1 && textComponentBase.startsWith("\"") && textComponentBase.endsWith("\""))
    //         textComponentBase = "{text:"+textComponentBase+"}";
    //     else if (textComponentBase.length()>1 && textComponentBase.startsWith("'") && textComponentBase.endsWith("'"))
    //         textComponentBase = "{text:\""+textComponentBase.substring(0,textComponentBase.length()-1)+"\"}";
    //     else if (!(textComponentBase.startsWith("{") && textComponentBase.endsWith("}"))
    //     && !(textComponentBase.startsWith("[") && textComponentBase.endsWith("]")) && !textComponentBase.contains("\"") && !textComponentBase.contains("'"))
    //         textComponentBase = "{text:\""+textComponentBase+"\"}";

    //     if (textComponentEffectMode == 0 || textComponentEffectMode == 1) {
    //         if (textComponentBase.isEmpty() || textComponentBase.equals("{}") || textComponentBase.equals("[]")
    //                 || textComponentBase.equals("[{}]") || textComponentBase.equals("{text:\"\"}") || textComponentBase.equals("[{text:\"\"}]"))
    //             return textComponentEffect;
    //         else if (textComponentBase.length()>=4 && textComponentBase.charAt(0)=='[' && textComponentBase.charAt(textComponentBase.length()-1)==']'
    //         && textComponentBase.charAt(1)=='{' && textComponentBase.charAt(textComponentBase.length()-2)=='}')
    //             return textComponentBase.substring(0,textComponentBase.length()-1) +","+ textComponentEffect +"]";
    //         else if (textComponentBase.length()>=2 && textComponentBase.charAt(0)=='{' && textComponentBase.charAt(textComponentBase.length()-1)=='}')
    //             return "["+textComponentBase+","+textComponentEffect+"]";
    //         return null;
    //     }
    //     else if (textComponentEffectMode == 2) {
    //         if (textComponentBase.length()>4 && textComponentBase.charAt(0)=='[' && textComponentBase.charAt(textComponentBase.length()-1)==']'
    //         && textComponentBase.charAt(1)=='{' && textComponentBase.charAt(textComponentBase.length()-2)=='}')
    //             return textComponentBase.substring(0,textComponentBase.length()-2) + textComponentEffect +"}]";
    //         else if (textComponentBase.length()>2 && textComponentBase.charAt(0)=='{' && textComponentBase.charAt(textComponentBase.length()-1)=='}')
    //             return textComponentBase.substring(0,textComponentBase.length()-1) + textComponentEffect +"}";
    //         return null;
    //     }
    //     else
    //         return null;
    // }

    // private void updateTextComponentEffectBtns() {
    //     if ((textComponentEffectMode == 0 || textComponentEffectMode == 1)
    //     && widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD, WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC,
    //     WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED, WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,
    //     WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED)) {
    //         Button[] effectBtns = new Button[]{
    //             (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD),
    //             (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC),
    //             (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED),
    //             (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH),
    //             (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED)
    //         };
    //         int num = 0;
    //         String col = "";
    //         if (textComponentEffects[num]==1)
    //             col = "\u00a7a";
    //         else if (textComponentEffects[num]==2)
    //             col = "\u00a7c";
    //         effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7ll"));
    //         num++;
    //         col = "";
    //         if (textComponentEffects[num]==1)
    //             col = "\u00a7a";
    //         else if (textComponentEffects[num]==2)
    //             col = "\u00a7c";
    //         effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7oo"));
    //         num++;
    //         col = "";
    //         if (textComponentEffects[num]==1)
    //             col = "\u00a7a";
    //         else if (textComponentEffects[num]==2)
    //             col = "\u00a7c";
    //         effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7nn"));
    //         num++;
    //         col = "";
    //         if (textComponentEffects[num]==1)
    //             col = "\u00a7a";
    //         else if (textComponentEffects[num]==2)
    //             col = "\u00a7c";
    //         effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7mm"));
    //         num++;
    //         col = "";
    //         if (textComponentEffects[num]==1)
    //             col = "\u00a7a";
    //         else if (textComponentEffects[num]==2)
    //             col = "\u00a7c";
    //         effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7kk"));
    //         switch (textComponentEffectMode) {
    //             case 0: {
    //                 if (widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_RADIAL)) {
    //                     Button w = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_RADIAL);
    //                     if (textComponentEffects[5]==0)
    //                         w.setMessage(Component.nullToEmpty("[Radial]"));
    //                     else
    //                         w.setMessage(Component.nullToEmpty("[Linear]"));
    //                 }
    //                 break;
    //             }
    //             case 1: {
    //                 if (widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN, WidgetCacheType.TEXT_COMPONENT_COLOR_TXT,
    //                 WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE)) {

    //                     Button btnColor = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN);
    //                     SmartEditBox txtColor = (SmartEditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_COLOR_TXT);

    //                     txtColor.setTextColor(TEXT_COLOR);
    //                     txtColor.setEditable(true);
    //                     txtColor.setTooltip(null);

    //                     if (textComponentEffects[6]==2) {
    //                         btnColor.setMessage(Component.nullToEmpty("Color [RGB]"));
    //                         txtColor.setValue(getRgbHex(0));
    //                         txtColor.setTextColor(getRgbDec(0));
    //                         txtColor.setTooltip(Tooltip.create(Component.nullToEmpty(getRgbHex(0))));
    //                     }
    //                     else if (textComponentEffects[6]==1) {
    //                         btnColor.setMessage(Component.nullToEmpty("Color [Vanilla]"));
    //                         txtColor.setValue(textComponentLastColor);
    //                     }
    //                     else if (textComponentEffects[6]==0) {
    //                         btnColor.setMessage(Component.nullToEmpty("Color [None]"));
    //                         txtColor.setValue("<None>");
    //                         txtColor.setEditable(false);
    //                     }

    //                     Button btnTextMode = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE);

    //                     btnTextMode.setMessage(Component.nullToEmpty("[Text]"));
    //                     if (textComponentEffects[7]==1)
    //                         btnTextMode.setMessage(Component.nullToEmpty("[Keybind]"));
    //                     else if (textComponentEffects[7]==2)
    //                         btnTextMode.setMessage(Component.nullToEmpty("[Translate]"));
    //                 }
    //                 break;
    //             }
    //             default: break;
    //         }
    //     }
    // }

    // private void updateTextComponentEffect() {
    //     if (textComponentEffectPath == null || textComponentEffectBase == null)
    //         return;
    //     switch (textComponentEffectMode) {
    //         case 0: {
    //             if (widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)) {
    //                 String value = ((SmartEditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)).getValue();
    //                 String val = "";
    //                 if (value.length()==1 || (colorSets[0][0]==colorSets[1][0] && colorSets[0][1]==colorSets[1][1] && colorSets[0][2]==colorSets[1][2])) {
    //                     val+="{text:\"";
    //                     for (int i=0; i<value.length(); i++) {
    //                         String thisChar = ""+value.charAt(i);
    //                         if (thisChar.equals("\\") || thisChar.equals("\""))
    //                             thisChar = "\\"+thisChar;
    //                         val+=thisChar;
    //                     }
    //                     val+="\",color:\""+getRgbHex(0)+"\"";
    //                     if (textComponentShadowColor.length()>0)
    //                         val+=",shadow_color:"+textComponentShadowColor;
    //                     if (textComponentFont.length()>0)
    //                         val+=",font:\""+textComponentFont+"\"";
    //                     if (textComponentEffects[0]==1)
    //                         val+=",bold:true";
    //                     else if (textComponentEffects[0]==2)
    //                         val+=",bold:false";
    //                     if (textComponentEffects[1]==1)
    //                         val+=",italic:true";
    //                     else if (textComponentEffects[1]==2)
    //                         val+=",italic:false";
    //                     if (textComponentEffects[2]==1)
    //                         val+=",underlined:true";
    //                     else if (textComponentEffects[2]==2)
    //                         val+=",underlined:false";
    //                     if (textComponentEffects[3]==1)
    //                         val+=",strikethrough:true";
    //                     else if (textComponentEffects[3]==2)
    //                         val+=",strikethrough:false";
    //                     if (textComponentEffects[4]==1)
    //                         val+=",obfuscated:true";
    //                     else if (textComponentEffects[4]==2)
    //                         val+=",obfuscated:false";
    //                     val+="}";
    //                 }
    //                 else if (value.length() > 1) {
    //                     val+="{text:\"";
    //                     for (int i=0; i<1; i++) {
    //                         String thisChar = ""+value.charAt(i);
    //                         if (thisChar.equals("\\") || thisChar.equals("\""))
    //                             thisChar = "\\"+thisChar;
    //                         val+=thisChar;
    //                     }
    //                     val+="\",color:\"#";
    //                     for (int c=0; c<3; c++) {
    //                         int col = 0;
    //                         if (textComponentEffects[5]==1)
    //                             col = colorSets[0][c];
    //                         else {
    //                             col = colorSets[1][c];
    //                         }
    //                         String current = Integer.toHexString(col).toUpperCase();
    //                         if (current.length()==1)
    //                             current = "0" + current;
    //                         val += current;
    //                     }
    //                     val+="\"";
    //                     if (textComponentShadowColor.length()>0)
    //                         val+=",shadow_color:"+textComponentShadowColor;
    //                     if (textComponentFont.length()>0)
    //                         val+=",font:\""+textComponentFont+"\"";
    //                     if (textComponentEffects[0]==1)
    //                         val+=",bold:true";
    //                     else if (textComponentEffects[0]==2)
    //                         val+=",bold:false";
    //                     if (textComponentEffects[1]==1)
    //                         val+=",italic:true";
    //                     else if (textComponentEffects[1]==2)
    //                         val+=",italic:false";
    //                     if (textComponentEffects[2]==1)
    //                         val+=",underlined:true";
    //                     else if (textComponentEffects[2]==2)
    //                         val+=",underlined:false";
    //                     if (textComponentEffects[3]==1)
    //                         val+=",strikethrough:true";
    //                     else if (textComponentEffects[3]==2)
    //                         val+=",strikethrough:false";
    //                     if (textComponentEffects[4]==1)
    //                         val+=",obfuscated:true";
    //                     else if (textComponentEffects[4]==2)
    //                         val+=",obfuscated:false";
    //                     val+=",\"extra\":[";
    //                     boolean firstPart = true;
    //                     for (int i=1; i<value.length(); i++) {
    //                         if (!firstPart)
    //                             val+=",";
    //                         String thisChar = ""+value.charAt(i);
    //                         if (thisChar.equals("\\") || thisChar.equals("\""))
    //                             thisChar = "\\"+thisChar;
    //                         val+="{text:\""+thisChar+"\",color:\"#";
    //                         for (int c=0; c<3; c++) {
    //                             int col = 0;
    //                             if (textComponentEffects[5]==1)
    //                                 col = colorSets[0][c] + (int)((colorSets[1][c]-colorSets[0][c])*i/((double)(value.length()-1)));
    //                             else {
    //                                 if (value.length()%2==0) {
    //                                     if (i<value.length()/2)
    //                                         col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*i/((double)(value.length()/2-1)));
    //                                     else
    //                                         col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*(value.length()-i-1)/((double)(value.length()/2-1)));
    //                                 }
    //                                 else {
    //                                     if (i<=value.length()/2)
    //                                         col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*i/((double)(value.length()/2)));
    //                                     else
    //                                         col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*(value.length()-i-1)/((double)(value.length()/2)));
    //                                 }
    //                             }

    //                             String current = Integer.toHexString(col).toUpperCase();
    //                             if (current.length()==1)
    //                                 current = "0" + current;
    //                             val += current;
    //                         }
    //                         val+="\"}";
    //                         firstPart = false;
    //                     }
    //                     val+="]}";
    //                 }
    //                 if (value == null || value.equals(""))
    //                     val = "{text:\"\"}";
    //                 updateTextComponentPreview(textComponentEffectPath,textComponentEffectBase,val);
    //             }
    //             break;
    //         }
    //         case 1: {
    //             if (widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)) {
    //                 String value = ((SmartEditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)).getValue();
    //                 String val = "{";
    //                 if (textComponentEffects[7] == 0) {
    //                     val+="text:\"";
    //                     for (int i=0; i<value.length(); i++) {
    //                         String thisChar = ""+value.charAt(i);
    //                         if (thisChar.equals("\\") || thisChar.equals("\""))
    //                             thisChar = "\\"+thisChar;
    //                         val+=thisChar;
    //                     }
    //                     val+="\"";
    //                 }
    //                 else if (textComponentEffects[7] == 1) {
    //                     val+="\"keybind\":\""+value+"\"";
    //                 }
    //                 else if (textComponentEffects[7] == 2) {
    //                     if (widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH, WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK)) {
    //                         SmartEditBox txtWith = (SmartEditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH);
    //                         SmartEditBox txtFallback = (SmartEditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK);
    //                         val+="\"translate\":\""+value+"\"";
    //                         if (txtWith.getValue().length()>0)
    //                             val+=",\"with\":"+txtWith.getValue();
    //                         if (txtFallback.getValue().length()>0)
    //                             val+=",\"fallback\":\""+txtFallback.getValue()+"\"";
    //                     }
    //                 }
    //                 if (textComponentEffects[6]==2)
    //                     val+=",color:\""+getRgbHex(0)+"\"";
    //                 else if (textComponentEffects[6]==1)
    //                     val+=",color:\""+textComponentLastColor+"\"";
    //                 if (textComponentShadowColor.length()>0)
    //                     val+=",shadow_color:"+textComponentShadowColor;
    //                 if (textComponentFont.length()>0)
    //                     val+=",font:\""+textComponentFont+"\"";
    //                 if (textComponentEffects[0]==1)
    //                     val+=",bold:true";
    //                 else if (textComponentEffects[0]==2)
    //                     val+=",bold:false";
    //                 if (textComponentEffects[1]==1)
    //                     val+=",italic:true";
    //                 else if (textComponentEffects[1]==2)
    //                     val+=",italic:false";
    //                 if (textComponentEffects[2]==1)
    //                     val+=",underlined:true";
    //                 else if (textComponentEffects[2]==2)
    //                     val+=",underlined:false";
    //                 if (textComponentEffects[3]==1)
    //                     val+=",strikethrough:true";
    //                 else if (textComponentEffects[3]==2)
    //                     val+=",strikethrough:false";
    //                 if (textComponentEffects[4]==1)
    //                     val+=",obfuscated:true";
    //                 else if (textComponentEffects[4]==2)
    //                     val+=",obfuscated:false";
    //                 val+="}";
    //                 if (value == null || value.equals(""))
    //                     val = "{text:\"\"}";
    //                 updateTextComponentPreview(textComponentEffectPath,textComponentEffectBase,val);
    //             }
    //             break;
    //         }
    //     }
    // }

    private void updateInvTab() {
        boolean changed = false;
        for (int i=0; i<cacheInv.length; i++) {
            ItemStack current = null;

            if (i<27)
                current = minecraft.player.getInventory().getItem(i+9).copy();
            else if (i<36)
                current = minecraft.player.getInventory().getItem(i-27).copy();
            else if (i<41)
                current = minecraft.player.getInventory().getItem(i).copy();

            if (cacheInv[i] == null || !ItemStack.matches(cacheInv[i],current)) {
                changed = true;
                cacheInv[i] = current;
            }
        }
        if (cacheInvSlot != minecraft.player.getInventory().getSelectedSlot()) {
            changed = true;
            cacheInvSlot = minecraft.player.getInventory().getSelectedSlot();
        }

        if (changed) {
            createTab(CACHE_TAB_INV);
            btnTab(CACHE_TAB_INV);
        }
    }

    private String trimStringSize(String inp) {
        int maxSize = this.width-10;
        if (this.font.width(inp)>maxSize && inp.length()>1) {
            String trail = "...";
            maxSize -= font.width(trail);
            if (width>10)
                inp = this.font.plainSubstrByWidth(inp,maxSize);
            return inp+trail;
        }
        return inp;
    }

    private void setErrorMsg(String errorMsg) {
        if (errorMsg == null) {
            inpError = null;
            inpErrorTrim = null;
            lblInpError = null;
        }
        else {
            inpError = errorMsg;
            inpErrorTrim = trimStringSize(errorMsg);
            lblInpError = (new MultiLineTextWidget(width/2, y+this.backgroundHeight+3,
                Component.empty().append(inpErrorTrim).withStyle(ChatFormatting.RED),
                this.font));
            lblInpError.setTooltip(Tooltip.create(Component.empty().append(inpError).withStyle(ChatFormatting.RED)));
            lblInpError.setX((width - lblInpError.getWidth()) / 2);
        }
    }

    private static Component getComplexButtonText(Tag el) {
        return getComplexButtonText(el, null);
    }

    private static Component getComplexButtonText(Tag el, PathFlag flag) {
        if (el==null)
            return Component.empty();
        Component flagText = getSpecialElementPreview(el, flag, false);
        if (flagText != null)
            return flagText;
        return BlackMagick.nbtToColorfulText(el);
    }

    private static MutableComponent getElementTooltipInfo(Tag el, PathFlag flag) {
        MutableComponent text = Component.empty();
        Component flagText = getSpecialElementPreview(el, flag, true);
        if (flagText != null)
            text = text.append("\n\n").append(flagText);
        return text.append(grayWhiteText("\n\nNBT: ", BlackMagick.nbtToColorfulText(el)));
    }

    private static Component getSpecialElementPreview(Tag el, PathFlag flag, boolean showLabel) {
        if (el != null && flag != null) {
            String elSnbt = BlackMagick.nbtToSnbt(el);
            switch (flag) {
                case NONE: break;

                case ATTRIBUTE_MODIFIER:
                {
                    if (el != null && el.getId()==Tag.TAG_COMPOUND) {
                        ItemStack stack = BlackMagick.itemFromString("{id:'minecraft:stone',components:{'minecraft:attribute_modifiers':["+BlackMagick.nbtToSnbt(el)+"]}}");
                        if (!stack.isEmpty()) {
                            List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                            if (textList.size()>3) {
                                Component btnTxt = Component.nullToEmpty(BlackMagick.textComponentToStringLiteral(textList.get(2))).copy()
                                    .append(Component.nullToEmpty(" ")).append(textList.get(3));
                                return showLabel ? Component.empty().append(grayWhiteText("Modifier:\n",btnTxt)) : btnTxt;
                            }
                            if (BlackMagick.elementsEqual(el, BlackMagick.getNbtPath(BlackMagick.itemToNbt(stack),"components.minecraft:attribute_modifiers[0]"))) {
                                return showLabel ? Component.empty().append(grayWhiteText("Modifier: ","Hidden")) : Component.empty().append("Hidden");
                            }
                        }
                    }
                    return errorText("Invalid attribute modifier");
                }

                case BANNER_PATTERN:
                {
                    if (el != null && el.getId()==Tag.TAG_COMPOUND) {
                        ItemStack stack = BlackMagick.itemFromString("{id:'minecraft:white_banner',components:{'minecraft:banner_patterns':["+BlackMagick.nbtToSnbt(el)+"]}}");
                        if (!stack.isEmpty()) {
                            List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                            if (textList.size()>1) {
                                Component btnTxt = Component.nullToEmpty(BlackMagick.textComponentToStringLiteral(textList.get(1)));
                                return showLabel ? Component.empty().append(grayWhiteText("Pattern:\n",btnTxt)) : btnTxt;
                            }
                        }
                    }
                    return errorText("Invalid banner pattern");
                }

                case COLOR_RGB_INT:
                {
                    if (el.getId()==Tag.TAG_INT) {
                        int colorInt = ((IntTag)el).intValue();
                        Component btnTxt = Component.empty().append(BlackMagick.hexFromInt(colorInt)).withColor(colorInt);
                        return showLabel ? Component.empty().append(grayWhiteText("Color: ",btnTxt)) : btnTxt;
                    }
                    return errorText("Invalid color");
                }
                case COLOR_RGB_INT_OR_LIST:
                {
                    boolean valid = el.getId()==Tag.TAG_INT;
                    if (!valid) {
                        if (el.getId()==Tag.TAG_LIST) {
                            ListTag elList = (ListTag)el;
                            if (elList.size()==3) {
                                valid = true;
                                for (int i=0; i<elList.size(); i++)
                                    if (!BlackMagick.nbtIsNumber(elList.get(i)))
                                        valid = false;
                            }
                        }
                        else if (
                            (el.getId()==Tag.TAG_BYTE_ARRAY && ((ByteArrayTag)el).size()==3)
                            || (el.getId()==Tag.TAG_INT_ARRAY && ((IntArrayTag)el).size()==3)
                            || (el.getId()==Tag.TAG_LONG_ARRAY && ((LongArrayTag)el).size()==3)
                        )
                            valid = true;
                    }
                    if (!valid)
                        return errorText("Invalid color");

                    Tag color = BlackMagick.getNbtPath(BlackMagick.itemToNbt(
                        BlackMagick.itemFromString("{id:\"stone\",components:{\"minecraft:dyed_color\":"+elSnbt+"}}")),
                        "components.minecraft:dyed_color");
                    if (color!=null && color.getId()==Tag.TAG_INT) {
                        int colorInt = ((IntTag)color).intValue();
                        Component btnTxt = Component.empty().append(BlackMagick.hexFromInt(colorInt)).withColor(colorInt);
                        return showLabel ? Component.empty().append(grayWhiteText("Color: ",btnTxt)) : btnTxt;
                    }

                    return errorText("Invalid color");
                }
                case COLOR_ARGB_INT_OR_LIST:
                {
                    boolean valid = el.getId()==Tag.TAG_INT;
                    if (!valid) {
                        if (el.getId()==Tag.TAG_LIST) {
                            ListTag elList = (ListTag)el;
                            if (elList.size()==4) {
                                valid = true;
                                for (int i=0; i<elList.size(); i++)
                                    if (!BlackMagick.nbtIsNumber(elList.get(i)))
                                        valid = false;
                            }
                        }
                        else if (
                            (el.getId()==Tag.TAG_BYTE_ARRAY && ((ByteArrayTag)el).size()==4)
                            || (el.getId()==Tag.TAG_INT_ARRAY && ((IntArrayTag)el).size()==4)
                            || (el.getId()==Tag.TAG_LONG_ARRAY && ((LongArrayTag)el).size()==4)
                        )
                            valid = true;
                    }
                    if (!valid)
                        return errorText("Invalid color");

                    Tag color = BlackMagick.getNbtPath(BlackMagick.itemToNbt(
                        BlackMagick.itemFromString("{id:\"stone\",components:{\"minecraft:custom_name\":{text:\"\",shadow_color:"+elSnbt+"}}}")),
                        "components.minecraft:custom_name.shadow_color");
                    if (color!=null && color.getId()==Tag.TAG_INT) {
                        int colorInt = ((IntTag)color).intValue();
                        Component btnTxt = Component.empty().append(BlackMagick.hexFromInt(colorInt)).withColor(colorInt);
                        return showLabel ? Component.empty().append(grayWhiteText("Color: ",btnTxt)) : btnTxt;
                    }

                    return errorText("Invalid color");
                }

                case ITEM_STACK:
                {
                    ItemStack tryParseStack = BlackMagick.itemFromNbtTag(el);
                    if (!tryParseStack.isEmpty()) {
                        Component btnTxt = tryParseStack.getStyledHoverName();
                        return showLabel ? Component.empty().append(grayWhiteText("Item stack:\n",btnTxt)) : btnTxt;
                    }
                    return errorText("Invalid item");
                }

                case CONTAINER_SLOT:
                {
                    ItemStack tryParseStack = BlackMagick.itemFromNbtTag(BlackMagick.getNbtPath(BlackMagick.validCompound(el),"item"));
                    Tag slotTag = BlackMagick.getNbtPath(BlackMagick.validCompound(el),"slot");
                    if (slotTag != null && slotTag.getId() == Tag.TAG_INT) {
                        int slot = ((IntTag)slotTag).intValue();
                        if (!tryParseStack.isEmpty()) {
                            Component btnTxt = Component.empty().append(grayWhiteText("","Slot "+slot+": ")).append(tryParseStack.getStyledHoverName());
                            return showLabel ? Component.empty().append(grayWhiteText("Container slot:\n",btnTxt)) : btnTxt;
                        }
                    }
                    return errorText("Invalid item");
                }

                case RARITY:
                {
                    if (el.getId()==Tag.TAG_STRING) {
                        String rarity = BlackMagick.nbtToSnbtOrString(el);
                        MutableComponent btnTxt = Component.empty();
                        switch (rarity) {
                            case "common": btnTxt = btnTxt.append(rarity).withStyle(ChatFormatting.WHITE); break;
                            case "uncommon": btnTxt = btnTxt.append(rarity).withStyle(ChatFormatting.YELLOW); break;
                            case "rare": btnTxt = btnTxt.append(rarity).withStyle(ChatFormatting.AQUA); break;
                            case "epic": btnTxt = btnTxt.append(rarity).withStyle(ChatFormatting.LIGHT_PURPLE); break;
                            default: return errorText("Invalid rarity");
                        }
                        return showLabel ? Component.empty().append(grayWhiteText("Rarity: ",btnTxt)) : btnTxt;
                    }
                    return errorText("Invalid rarity");
                }

                case TEXT_COMPONENT_FIELD_COLOR:
                {
                    Tag color = BlackMagick.getNbtPath(BlackMagick.itemToNbt(
                        BlackMagick.itemFromString("{id:\"stone\",components:{\"minecraft:custom_name\":{text:\"\",color:"+elSnbt+"}}}")),
                        "components.minecraft:custom_name.color");
                    if (color!=null && color.getId()==Tag.TAG_STRING) {
                        String colorString = BlackMagick.nbtToSnbtOrString(color);
                        ParsedText text = BlackMagick.textComponentFromSnbt("{text:'"+colorString+"',color:"+elSnbt+"}");
                        if (text.isValid())
                            return showLabel ? Component.empty().append(grayWhiteText("Color: ",text.text())) : text.text();
                    }
                    return errorText("Invalid color");
                }

                case TEXT_COMPONENT:
                case TEXT_COMPONENT_ITALIC:
                case TEXT_COMPONENT_LORE:
                case TEXT_COMPONENT_BOOK:
                case TEXT_COMPONENT_SIGN:
                {
                    ParsedText text = BlackMagick.textComponentFromSnbt(elSnbt);
                    if (text.isValid()) {
                        Component btnTxt = text.text();
                        if (flag==PathFlag.TEXT_COMPONENT_ITALIC)
                            btnTxt = Component.empty().withStyle(ChatFormatting.ITALIC).append(btnTxt);
                        else if (flag==PathFlag.TEXT_COMPONENT_LORE)
                            btnTxt = Component.empty().withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_PURPLE).append(btnTxt);
                        return showLabel ? Component.empty().append(grayWhiteText("Text component:\n")).append(btnTxt) : btnTxt;
                    }
                    return errorText("Invalid text component");
                }

            }
        }
        return null;
    }

    private static String[] getStartVals(Tag el, boolean isString, PathFlag flag) {
        List<String> tempList = Lists.newArrayList();
        tempList.add(isString ? BlackMagick.nbtToSnbtOrString(el) : BlackMagick.nbtToSnbt(el));
        switch (flag) {
            case COLOR_RGB_INT:
            case COLOR_RGB_INT_OR_LIST:
            case COLOR_ARGB_INT_OR_LIST:
                if (el != null && el.getId() == Tag.TAG_INT) {
                    tempList.add(BlackMagick.hexFromInt(((IntTag)el).intValue()));
                }
                break;
            default: break;
        }
        return tempList.toArray(new String[0]);
    }

    private ItemStack bundleOrItemFromList(String inp) {
        if (inp.startsWith("[") && inp.endsWith("]") && BlackMagick.nbtFromSnbt(inp,Tag.TAG_LIST) != null
        && !((ListTag)BlackMagick.nbtFromSnbt(inp,Tag.TAG_LIST)).isEmpty()) {
            ListTag list = (ListTag)BlackMagick.nbtFromSnbt(inp,Tag.TAG_LIST);
            ItemStack bundle = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.validCompoundFromString("{id:\"bundle\"}"),
                "components.minecraft:bundle_contents",list));
            if (bundle != null && !bundle.isEmpty()) {
                ListTag validatedList = (ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(bundle),"components.minecraft:bundle_contents",Tag.TAG_LIST);
                if (validatedList != null && !validatedList.isEmpty()) {
                    if (validatedList.size() == 1 && validatedList.get(0).getId() == Tag.TAG_COMPOUND) {
                        ItemStack innerItem = BlackMagick.itemFromNbt((CompoundTag)validatedList.get(0));
                        if (innerItem != null && !innerItem.isEmpty())
                            return innerItem;
                    }
                    return bundle;
                }
            }
        }
        return null;
    }

    private Component getKeyButtonTooltip(PathNode node, PathInfo pi) {
        MutableComponent btnTt = Component.empty().append(grayWhiteText(node.isKey() ? "Key: " : "Index: ", node.isKey() ? node.key() : (""+node.index())));
        appendPathInfo(btnTt, pi);

        return btnTt;
    }

    private Component getPathBoxTooltip() {
        MutableComponent pathTt = nbtEdit.fullPath().isEmpty() ? grayWhiteText("Path: ","{}")
            : grayWhiteText("Path:\n",nbtEdit.fullPath());
        appendPathInfo(pathTt, nbtEdit.pi());

        pathTt = pathTt.append(grayWhiteText("\n\nCurrent element:"));

        Component flagText = getSpecialElementPreview(nbtEdit.getEditElement(), nbtEdit.pi().getFlag(), true);
        if (flagText != null)
            pathTt = pathTt.append("\n\n").append(flagText);

        pathTt = pathTt.append(grayWhiteText("\n\nNBT:\n",nbtEdit.getPathChanges()));

        return pathTt;
    }

    private void appendPathInfo(MutableComponent currentText, PathInfo pi) {
        if (pi != null) {
            if (!pi.getNbtTypes().isEmpty()) {
                if (pi.getNbtTypes().size()==1)
                    currentText = currentText.append(grayWhiteText("\n\nValid Type: ",pi.getNbtTypes().get(0).label()));
                else {
                    String types = "";
                    for (NbtType t : pi.getNbtTypes())
                        types += "\n"+t.label();
                    currentText = currentText.append(grayWhiteText("\n\nValid Types: ",types));
                }
            }
            else {
                currentText = currentText.append(grayWhiteText("\n\nValid Type: ","Unknown"));
            }
            if (pi.getInfo() != null)
                currentText = currentText.append(grayWhiteText("\n\nInfo:\n",pi.getInfo()));
            if (pi.getUnsetInfo() != null)
                currentText = currentText.append(grayWhiteText("\n\nWhen unset:\n",pi.getUnsetInfo()));
        }
    }

    private void suggsOnChanged(SmartEditBox w, String[] suggestions) {
        suggsOnChanged(w, suggestions, new String[0]);
    }

    private void suggsOnChanged(SmartEditBox w, String[] suggestions, String... startVals) {
        if (w == null || suggsPause)
            return;

        boolean shouldSetSuggs = false;
        if (!currentTxt.contains(w)) {
            resetSuggs();
            currentTxt.add(w);
            suggs = new TextSuggestor(minecraft, w, font);
            shouldSetSuggs = true;
        }
        else {
            if (suggs != null)
                suggs.refresh();
            else {
                resetSuggs();
                suggs = new TextSuggestor(minecraft, w, font);
                shouldSetSuggs = true;
            }
        }
        if (shouldSetSuggs) {
            if (suggs == null)
                return;
            List<String> joinSuggs = null;
            if (suggestions != null && suggestions.length>0) {
                joinSuggs = List.of(suggestions);
            }
            String[] suggsArr = BlackMagick.joinCommandSuggs(joinSuggs, startVals).toArray(new String[0]);
            if (suggsArr != null && suggsArr.length>0)
                suggs.setSuggestions(suggsArr);
        }
    }

    protected RowWidget addTabWidgetScroll(int tabNum, RowWidget row) {
        TAB_WIDGETS_SCROLL.get(tabNum).add(row);
        return row;
    }

    protected PosWidget addTabWidgetLocked(int tabNum, PosWidget posWidget) {
        if (posWidget.w() instanceof SmartEditBox || posWidget.w() instanceof MultiLineEditBox)
            ALL_TEXT_WIDGETS.add(posWidget.w());
        else if (posWidget.w() instanceof AbstractSliderButton)
            ALL_SLIDER_WIDGETS.add(posWidget.w());

        TAB_WIDGETS_LOCKED.get(tabNum).add(posWidget);
        return posWidget;
    }

    protected int getTabWidgetScrollIndex(int tabNum) {
        return TAB_WIDGETS_SCROLL.get(tabNum).size();
    }

    /**
     * Resets all tabs. Only use on first init when widgets.size().isEmpty().
     * Creates widgets for static pages.
     */
    private void createStaticTabs() {
        TAB_WIDGETS_SCROLL.clear();
        TAB_WIDGETS_LOCKED.clear();
        UNSAVED_TEXT_WIDGETS.clear();
        ALL_TEXT_WIDGETS.clear();
        ALL_SLIDER_WIDGETS.clear();
        WIDGET_CACHE.clear();
        for (int i=0; i<tabs.length; i++) {
            TAB_WIDGETS_SCROLL.add(Lists.newArrayList());
            TAB_WIDGETS_LOCKED.add(Lists.newArrayList());
        }

        {
            int tabNum = CACHE_TAB_PRESETS;
            {
                addTabWidgetScroll(tabNum, new RowWidget("\u00a76\u00a7oBaphomethLabs\u00a7r"));
            }
            {//public RowWidget(Text[] names, String[] tooltips, String[][] suggestions, boolean survival, PressAction... onPressActions) {
                addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("Item Lore"),Component.nullToEmpty("Bottle Lore"),Component.nullToEmpty("Watermark")},new int[]{65,64,65},
                new String[]{"Add \u00a76\u00a7oBaphomethLabs\u00a7r watermark to lore","Add \u00a76\u00a7oBottled by BaphomethLabs\u00a7r watermark to lore",
                "Add watermark in custom_data"},null,false,btn -> {
                    if (!selItem.isEmpty()) {
                        boolean removed = false;
                        Tag loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",Tag.TAG_LIST);
                        if (loreEl != null) {
                            ListTag lore = (ListTag)loreEl;
                            if (lore.size()>=2 && BlackMagick.elementsEqual(lore.get(lore.size()-2),StringTag.valueOf(""))) {
                                if (BlackMagick.elementsEqual(lore.get(lore.size()-1),FortytwoEdit.LORE_BAPHOMETHLABS)) {
                                    removed = true;
                                    lore.removeLast();
                                    lore.removeLast();
                                    ItemStack newStack = BlackMagick.itemFromNbt(
                                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                                    if (!newStack.isEmpty())
                                        BlackMagick.setItemMain(newStack);
                                }
                                else if (BlackMagick.elementsEqual(lore.get(lore.size()-1),FortytwoEdit.LORE_BAPHOMETHLABS_BOTTLE)) {
                                    lore.removeLast();
                                    lore.removeLast();
                                    ItemStack newStack = BlackMagick.itemFromNbt(
                                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                                    if (!newStack.isEmpty())
                                        BlackMagick.setItemMain(newStack);
                                }
                            }
                        }
                        if (!removed) {
                            ListTag lore;
                            if (loreEl != null)
                                lore = (ListTag)loreEl;
                            else
                                lore = new ListTag();
                            lore.add(StringTag.valueOf(""));
                            lore.add(FortytwoEdit.LORE_BAPHOMETHLABS);
                            ItemStack newStack = BlackMagick.itemFromNbtTag(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                            if (!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel();
                }, btn -> {
                    if (!selItem.isEmpty()) {
                        boolean removed = false;
                        Tag loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",Tag.TAG_LIST);
                        if (loreEl != null) {
                            ListTag lore = (ListTag)loreEl;
                            if (lore.size()>=2 && BlackMagick.elementsEqual(lore.get(lore.size()-2),StringTag.valueOf(""))) {
                                if (BlackMagick.elementsEqual(lore.get(lore.size()-1),FortytwoEdit.LORE_BAPHOMETHLABS)) {
                                    lore.removeLast();
                                    lore.removeLast();
                                    ItemStack newStack = BlackMagick.itemFromNbt(
                                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                                    if (!newStack.isEmpty())
                                        BlackMagick.setItemMain(newStack);
                                }
                                else if (BlackMagick.elementsEqual(lore.get(lore.size()-1),FortytwoEdit.LORE_BAPHOMETHLABS_BOTTLE)) {
                                    removed = true;
                                    lore.removeLast();
                                    lore.removeLast();
                                    ItemStack newStack = BlackMagick.itemFromNbt(
                                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                                    if (!newStack.isEmpty())
                                        BlackMagick.setItemMain(newStack);
                                }
                            }
                        }
                        if (!removed) {
                            ListTag lore;
                            if (loreEl != null)
                                lore = (ListTag)loreEl;
                            else
                                lore = new ListTag();
                            lore.add(StringTag.valueOf(""));
                            lore.add(FortytwoEdit.LORE_BAPHOMETHLABS_BOTTLE);
                            ItemStack newStack = BlackMagick.itemFromNbtTag(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore));
                            if (!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel();
                }, btn -> {
                    if (!selItem.isEmpty()) {
                        if (BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"")!=null) {
                            ItemStack newStack = BlackMagick.itemFromNbtTag(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"",null));
                            if (BlackMagick.nbtToSnbt(BlackMagick.getNbtPath(BlackMagick.itemToNbt(newStack),"components.minecraft:custom_data")).equals("{}")) {
                                newStack = BlackMagick.itemFromNbtTag(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data",null));
                            }
                            if (!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                        else {
                            ItemStack newStack = BlackMagick.itemFromNbtTag(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"",IntTag.valueOf(42)));
                            if (!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel();
                }));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget("Player Heads"));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget("Owner","Create dynamic player head from profile name or id",btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j).btn()[0];
                    if (!inp.equals("")) {
                        Tag profile = StringTag.valueOf(inp);
                        Tag inpEl = BlackMagick.nbtFromSnbt(inp);
                        if (inpEl != null && inpEl.getId() == Tag.TAG_INT_ARRAY) {
                            IntArrayTag profileId = (IntArrayTag)inpEl;
                            if (profileId.size() == 4) {
                                CompoundTag profileCompound = new CompoundTag();
                                profileCompound.put("id",profileId);
                                profile = profileCompound;
                            }
                        }

                        if (minecraft.player.getMainHandItem().isEmpty()) {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                BlackMagick.validCompoundFromString("{id:player_head}"),"components.minecraft:profile",profile)));
                        }
                        else {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:profile",profile)));
                        }
                    }
                    else {
                        if (minecraft.player.getMainHandItem().isEmpty())
                            BlackMagick.setItemMain(new ItemStack(Items.PLAYER_HEAD));
                    }
                },FortytwoEdit.PROFILE_SUGGS,false));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget("Skin","Create static player head from base64 properties",btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j).btn()[0];
                    if (inp.equals("")) {
                        if (minecraft.player.getMainHandItem().isEmpty())
                            BlackMagick.setItemMain(new ItemStack(Items.PLAYER_HEAD));
                    }
                    else {
                        String base64 = inp;
                        if (inp.contains("name:\"textures\"") && inp.contains(",value:\"")) {
                            String value = inp;
                            value = value.substring(value.indexOf(",value:\"")+8);
                            if (value.contains("\""))
                                base64 = value.substring(0,value.indexOf("\""));
                        }

                        String testBase64 = base64;
                        while (testBase64.endsWith("="))
                            testBase64 = testBase64.substring(0,testBase64.length()-1);
                        testBase64 = testBase64.replaceAll("[a-zA-Z0-9+/]","");
                        if (testBase64.length()==0) {
                            CompoundTag temp;
                            if (selItem.isEmpty())
                                temp = BlackMagick.validCompoundFromString("{id:player_head}");
                            else
                                temp = BlackMagick.itemToNbt(selItem);
                            temp = BlackMagick.setNbtPath(
                                BlackMagick.setNbtPath(temp,"components.minecraft:profile.properties",BlackMagick.nbtFromSnbt("[{name:\"textures\",value:\"\"}]")),
                                "components.minecraft:profile.properties[0].value",
                                StringTag.valueOf(base64)
                                );

                            ItemStack newItem = BlackMagick.itemFromNbt(temp);
                            if (!newItem.isEmpty())
                                BlackMagick.setItemMain(newItem);
                        }
                    }
                },null,false));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget("Sounds"));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget("Play","Listen to the sound (client-side only)",btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j).btn()[0];
                    if (!inp.trim().equals("")) {
                        String sound = inp.trim();
                        Identifier soundId = BlackMagick.identifierOrNull(sound);
                        if (soundId != null)
                            BlackMagick.playClientSound(soundId, 1, 1);
                    }
                }, SuggestionHelper.REGISTRY_SOUND_EVENT.getArray(),true));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("Head Sound")},new int[]{80},
                        new String[]{"Create a preset head that plays the sound when on a note block"},null,false,btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j-1).btn()[0];
                    if (!inp.trim().equals("")) {
                        String sound = inp.trim();
                        Identifier soundId = BlackMagick.identifierOrNull(sound);
                        if (soundId != null) {
                            String soundDisplay = soundId.getNamespace().equals("minecraft") ? soundId.getPath() : BlackMagick.identifierToString(soundId);
                            ItemStack item = BlackMagick.itemFromString(
                                "{id:player_head,components:{\"minecraft:profile\":{name:\"Note_Block\",properties:[{name:\"textures\",value:"+
                                "\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDov"+
                                "L3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2VlYjc3ZDRkMjU3MjRhOWNhZjJjN2NkZjJkODgzOTliMTQxN2M2YjlmZjUyMTM2NTliNjUzYmU0Mz"+
                                "c2ZTMiDQogICAgfQ0KICB9DQp9\"}]},\"minecraft:note_block_sound\":\""+BlackMagick.identifierToString(soundId)+"\","+
                                "\"minecraft:custom_name\":{italic:false,text:\""+soundDisplay+"\"}}}");
                            if (!item.isEmpty())
                                BlackMagick.setItemMain(item);
                        }
                    }
                }));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget("Banners"));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("Symbol")},new int[]{40,55,49,55},
                new String[]{"Create banner(s) with preset designs\n\nChar Color | Chars | Base Color"+"\n\n"+BANNER_PRESET_CHARS},new String[][]
                {SuggestionHelper.LIST_DYE_COLOR.getArray(),BANNER_CHAR_LIST,SuggestionHelper.LIST_DYE_COLOR.getArray()},
                false,btn -> {
                    String[] inps = TAB_WIDGETS_SCROLL.get(i).get(j).btn();
                    if (BlackMagick.isCreative(minecraft)) {

                        ItemStack bannerStack = ItemStack.EMPTY;

                        String chars = inps[1];
                        if (chars.equals("*"))
                            chars = BANNER_PRESET_CHARS;
                        else {
                            String newchars = "";
                            for (int ii=0; ii<chars.length(); ii++)
                                if (BANNER_PRESET_CHARS.contains(""+chars.charAt(ii)))
                                    newchars += chars.charAt(ii);
                            chars = newchars;
                        }

                        if (chars.length()==1)
                            bannerStack = BlackMagick.itemFromNbt(BlackMagick.createBanner(chars.charAt(0),inps[2].toLowerCase(),inps[0].toLowerCase()));
                        else if (chars.length()>1) {
                            ListTag items = new ListTag();
                            while (chars.length()>0) {
                                CompoundTag bannerItem = BlackMagick.createBanner(chars.charAt(0),inps[2].toLowerCase(),inps[0].toLowerCase());
                                if (bannerItem!=null)
                                    items.add(bannerItem);
                                if (chars.length()==1)
                                    chars = "";
                                else
                                    chars = chars.substring(1);
                            }
                            if (!items.isEmpty())
                                bannerStack = BlackMagick.itemFromString("{id:bundle,components:{bundle_contents:"
                                    +BlackMagick.nbtToSnbt(items)+"}}");
                        }

                        if (!bannerStack.isEmpty()) {
                            BlackMagick.setItemMain(bannerStack);
                        }
                    }
                }));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
        }

        {
            int tabNum = CACHE_TAB_NBT;
            final MultiLineEditBox giveBox;
            {
                giveBox = MultiLineEditBox.builder().setX(0).setY(0).build(minecraft.font, ROW_WIDTH, ROW_HEIGHT*6, Component.nullToEmpty(""));
                addTabWidgetLocked(tabNum, new PosWidget(giveBox,ROW_LEFT_LOCKED,ROW_TOP));
                widgetCacheAdd(WidgetCacheType.GIVE_BOX_BOX,giveBox);
                giveBox.setValueListener(value -> {
                    if (widgetCacheTest(WidgetCacheType.GIVE_BOX_CLONE, WidgetCacheType.GIVE_BOX_GIVE)) {
                        setErrorMsg(null);
                        Button btnClone = (Button)widgetCacheGet(WidgetCacheType.GIVE_BOX_CLONE);
                        Button btnGive = (Button)widgetCacheGet(WidgetCacheType.GIVE_BOX_GIVE);
                        btnGive.active = false;
                        btnGive.setTooltip(Tooltip.create(errorText("Invalid item")));
                        if (value != null && !value.trim().equals("")) {
                            String inp = ""+value;
                            ItemStack item = ItemStack.EMPTY;

                            // keep consistent
                            inp = inp.trim();
                            if (inp.contains("/") && inp.indexOf("/")==0)
                                inp = inp.substring(1);
                            if (inp.contains("give ") && inp.indexOf("give ")==0) {
                                inp = inp.substring(5);
                                if (inp.contains(" "))
                                    inp = inp.substring(inp.indexOf(" ")+1); // remove selector or player name
                            }
                            else if (inp.startsWith("summon item ~ ~ ~ {Item:") && inp.endsWith("}")) {
                                inp = inp.substring("summon item ~ ~ ~ {Item:".length(),inp.length()-1);
                            }

                            if (inp.startsWith("{") && inp.endsWith("}")) {
                                if (BlackMagick.nbtFromSnbt(inp,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromString(inp);
                                }

                                setErrorMsg(BlackMagick.getItemCompoundErrors(inp,inpError));
                                if (item.isEmpty() && inpError == null)
                                    setErrorMsg("Invalid item");

                                if (!item.isEmpty() && inpError == null) {
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        grayWhiteText("Set current item to:\n",BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }
                            else if (((inp.startsWith("\"{") && inp.endsWith("}\"")) || (inp.startsWith("'{") && inp.endsWith("}'")))
                            && BlackMagick.nbtFromSnbt(inp,Tag.TAG_STRING) != null) {
                                String inpString = BlackMagick.nbtToSnbtOrString(BlackMagick.nbtFromSnbt(inp,Tag.TAG_STRING));
                                if (BlackMagick.nbtFromSnbt(inpString,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromString(inpString);
                                }

                                setErrorMsg(BlackMagick.getItemCompoundErrors(inpString,inpError));
                                if (item.isEmpty() && inpError == null)
                                    setErrorMsg("Invalid item");

                                if (!item.isEmpty() && inpError == null) {
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        grayWhiteText("Set current item to:\n",BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }
                            else if (bundleOrItemFromList(inp) != null) {
                                item = bundleOrItemFromList(inp);
                                btnGive.active = true;
                                btnGive.setTooltip(Tooltip.create(
                                    grayWhiteText("Set current item to:\n",BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(item)))));
                            }
                            else {
                                int count = 1;
                                if (inp.contains(" ")) {
                                    int last = inp.lastIndexOf(" ");
                                    try {
                                        count = Integer.parseInt(inp.substring(last+1));
                                        inp = inp.substring(0,last);
                                    } catch (NumberFormatException ex) {}
                                }

                                try {
                                    item = ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createItemStack(1);
                                }
                                catch (Exception ex) {
                                    if (ex instanceof CommandSyntaxException ex2) {
                                        setErrorMsg(ex2.getMessage());
                                        if (inpError.contains(" at position ")) {
                                            setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                        }
                                    }
                                }

                                if (!item.isEmpty()) {
                                    item.setCount(count);
                                    if (item.getCount() > item.getMaxStackSize()) {
                                        item.setCount(item.getMaxStackSize());
                                    }
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        grayWhiteText("Set current item to:\n",BlackMagick.nbtToColorfulText(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }

                            if (ItemStack.matches(item,selItem)) {
                                this.markSaved(giveBox);
                                btnGive.active = false;
                                btnGive.setTooltip(Tooltip.create(Component.nullToEmpty("Item unchanged")));
                            }
                            else {
                                this.markUnsaved(giveBox);
                            }

                        }
                        else {
                            this.markSaved(giveBox);
                        }

                        if (!value.equals(BlackMagick.itemToGive(minecraft.player.getMainHandItem()))) {
                            btnClone.active = true;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("Copy current item")));
                        }
                        else {
                            btnClone.active = false;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("Already cloned")));
                        }

                        if (selItem.isEmpty()) {
                            btnClone.active = false;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("No item to clone")));
                        }
                    }
                });
            }
            {
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.GIVE_BOX_CLONE,Button.builder(Component.nullToEmpty("Clone"), btn -> {
                    if (!minecraft.player.getMainHandItem().isEmpty() && widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                        ((MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX)).setValue(BlackMagick.itemToGive(minecraft.player.getMainHandItem()));
                    }
                    this.unsel();
                }).size(60,WID_HEIGHT).build()),ROW_LEFT_LOCKED,ROW_TOP+ROW_HEIGHT*6));
            }
            {
                Button w = Button.builder(Component.nullToEmpty("Give"), btn -> {
                    if (widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                        MultiLineEditBox editBox = (MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX);
                        String inp = editBox.getValue();
                        this.markSaved(editBox);

                        if (BlackMagick.isCreative(minecraft)) {
                            ItemStack item = ItemStack.EMPTY;

                            // keep consistent
                            inp = inp.trim();
                            if (inp.contains("/") && inp.indexOf("/")==0)
                                inp = inp.substring(1);
                            if (inp.contains("give ") && inp.indexOf("give ")==0) {
                                inp = inp.substring(5);
                                if (inp.contains(" "))
                                    inp = inp.substring(inp.indexOf(" ")+1); // remove selector or player name
                            }
                            else if (inp.startsWith("summon item ~ ~ ~ {Item:") && inp.endsWith("}")) {
                                inp = inp.substring("summon item ~ ~ ~ {Item:".length(),inp.length()-1);
                            }

                            if (inp.startsWith("{") && inp.endsWith("}")) {
                                if (BlackMagick.nbtFromSnbt(inp,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromString(inp);
                                }
                            }
                            else if (((inp.startsWith("\"{") && inp.endsWith("}\"")) || (inp.startsWith("'{") && inp.endsWith("}'")))
                            && BlackMagick.nbtFromSnbt(inp,Tag.TAG_STRING) != null) {
                                String inpString = BlackMagick.nbtToSnbtOrString(BlackMagick.nbtFromSnbt(inp,Tag.TAG_STRING));
                                if (BlackMagick.nbtFromSnbt(inpString,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromString(inpString);
                                }
                            }
                            else if (bundleOrItemFromList(inp) != null) {
                                item = bundleOrItemFromList(inp);
                            }
                            else {
                                int count = 1;
                                if (inp.contains(" ")) {
                                    int last = inp.lastIndexOf(" ");
                                    try {
                                        count = Integer.parseInt(inp.substring(last+1));
                                        inp = inp.substring(0,last);
                                    } catch (NumberFormatException ex) {}
                                }

                                try {
                                    item = ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createItemStack(1);
                                } catch (Exception ex) {}

                                if (!item.isEmpty()) {
                                    item.setCount(count);
                                    if (item.getCount() > item.getMaxStackSize()) {
                                        item.setCount(item.getMaxStackSize());
                                    }
                                }
                            }

                            BlackMagick.setItemMain(item);
                        }
                    }
                    this.unsel();
                }).size(60,WID_HEIGHT).build();
                if (!BlackMagick.isCreative(minecraft))
                    w.active = false;
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.GIVE_BOX_GIVE,w),ROW_LEFT_LOCKED+5+60,ROW_TOP+ROW_HEIGHT*6));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
            giveBox.setValue("");
        }

        {
            int tabNum = CACHE_TAB_SAVED;
            {
                // button is setup in updateSavedModeButtons()
                ItemSlotButton w = new ItemSlotButton(this, ItemSlotButton.SLOT_HEIGHT, null, btn -> {
                    viewBlackMarket = !viewBlackMarket;
                    updateSavedModeButtons();
                    updateSavedTab();
                    ItemBuilderScreen.this.unsel();
                });
                w.showSlot(false);
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.BTN_SAVED_SOURCE,w),ROW_LEFT_LOCKED,ROW_TOP));
            }
            {
                // button is setup in updateSavedModeButtons()
                Button w = Button.builder(Component.nullToEmpty(""), btn -> {
                    if (viewBlackMarket) {
                        CompoundTag result = FortytwoEdit.refreshWebItems();

                        if (result.contains("site_match_catch"))
                            FortytwoEdit.showToast("Black Market", "Items up to date");
                        else if (result.contains("site_updated_catch"))
                            FortytwoEdit.showToast("Black Market", "Items updated");
                        else
                            FortytwoEdit.showToast("Black Market", "Failed to connect to website");

                        refreshSaved();
                        reloadScreen();
                    }
                    else {
                        savedModeSet = !savedModeSet;
                        updateSavedModeButtons();
                        updateSavedTab();
                    }
                    ItemBuilderScreen.this.unsel();
                }).size(20,WID_HEIGHT).build();
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.BTN_SAVED_MODE,w),ROW_LEFT_LOCKED,ROW_TOP+ROW_HEIGHT));
            }
            for (int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
                addTabWidgetScroll(tabNum, new RowWidgetSavedItemsRow(i));
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
            refreshSaved();
            updateSavedModeButtons();
        }

        resetSuggs();
    }

    /**
     * Clears tabNum tab then recreates its widgets.
     * If tabNum is current tab, reloads the window.
     * Use for tabs that change depending on cached variables.
     * 
     * @param tabNum
     */
    public void createTab(int tabNum) {
        suggsPause = true;
        if (!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        for (RowWidget r : TAB_WIDGETS_SCROLL.get(tabNum)) {
            for (SmartEditBox t : r.txts) {
                this.UNSAVED_TEXT_WIDGETS.remove(t);
                this.ALL_TEXT_WIDGETS.remove(t);
            }
            for (PosWidget p : r.wids) {
                if (p.w() != null) {
                    this.UNSAVED_TEXT_WIDGETS.remove(p.w());
                    this.ALL_TEXT_WIDGETS.remove(p.w());
                    this.ALL_SLIDER_WIDGETS.remove(p.w());
                }
            }
        }
        for (PosWidget r : TAB_WIDGETS_LOCKED.get(tabNum)) {
            this.UNSAVED_TEXT_WIDGETS.remove(r.w());
            this.ALL_TEXT_WIDGETS.remove(r.w());
        }
        TAB_WIDGETS_SCROLL.get(tabNum).clear();
        TAB_WIDGETS_LOCKED.get(tabNum).clear();

        if (tabNum == CACHE_TAB_MAIN) {   //createBlock components
            ItemStack editItemInstance = selItem.copy();
            CompoundTag editItemStack = BlackMagick.itemToNbt(editItemInstance);
            {
                addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,"id",false));
            }
            if (!selItem.isEmpty()) {
                {
                    addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,"count",false));
                }
                Set<String> allSetComponentKeys = Sets.newHashSet();
                Set<String> modifiedComponentKeys = Sets.newHashSet();
                Set<String> defaultComponentKeys = Sets.newHashSet();
                Set<String> removedComponentKeys = Sets.newHashSet();
                for (String k : BlackMagick.validCompound(BlackMagick.getNbtPath(editItemStack,"components")).keySet()) {
                    if (k.startsWith("!"))
                        removedComponentKeys.add(k);
                    else {
                        allSetComponentKeys.add(k);
                        if (BlackMagick.isComponentDefault(selItem, k))
                            defaultComponentKeys.add(k);
                        else
                            modifiedComponentKeys.add(k);
                    }
                }
                if (!modifiedComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget(PathInfo.COMPOUND_KEY_MODIFIED_COMPONENTS_LABEL));
                    for (String c : BlackMagick.sortSet(modifiedComponentKeys))
                        addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,c,true));
                }
                if (!removedComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget(PathInfo.COMPOUND_KEY_REMOVED_COMPONENTS_LABEL));
                    for (String c : BlackMagick.sortSet(removedComponentKeys))
                        addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,c,true));
                }
                if (!defaultComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget(PathInfo.COMPOUND_KEY_DEFAULT_COMPONENTS_LABEL));
                    for (String c : BlackMagick.sortSet(defaultComponentKeys))
                        addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,c,true));
                }
                boolean firstUnset = true;
                for (String c : SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList()) {
                    if (!allSetComponentKeys.contains(c)) {
                        if (firstUnset) {
                            addTabWidgetScroll(tabNum, new RowWidget(PathInfo.COMPOUND_KEY_UNSET_COMPONENTS_LABEL));
                            firstUnset = false;
                        }
                        addTabWidgetScroll(tabNum, new RowWidgetComponent(editItemStack,c,true));
                    }
                }
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
        }
        else if (tabNum == CACHE_TAB_INV) {   //createBlock inventory
            {
                addTabWidgetScroll(tabNum, new RowWidget("Inventory"));
            }
            {
                for (int i=0; i<5; i++)
                    addTabWidgetScroll(tabNum, new RowWidgetInvRow(i));
            }
            for (int i=0; i<2; i++) {
                ItemStack current = i==0 ? minecraft.player.getMainHandItem() : minecraft.player.getOffhandItem();
                if (current != null && !current.isEmpty()) {
                    int[] size = SuggestionHelper.getContainerSize(current.getItem());

                    if (current.is(Items.BUNDLE)) {
                        {
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Bundle" : "Offhand Bundle"));
                        }
                        ListTag itemsList = new ListTag();
                        if (BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents[0]",Tag.TAG_COMPOUND) != null) {
                            itemsList = (ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents");
                        }

                        ItemStack[] stacks = new ItemStack[itemsList.size()+1];
                        for (int index=0; index<itemsList.size(); index++) {
                            stacks[index+1] = BlackMagick.itemFromNbtTag(itemsList.get(index));
                        }

                        int index = 0;
                        for (int r=0; r<=(stacks.length-1)/9; r++) {
                            ItemStack[] stackRow = new ItemStack[stacks.length-index > 9 ? 9 : stacks.length-index];
                            for (int c=0; c<stackRow.length; c++)
                                stackRow[c] = stacks[index+c];
                            index+=9;
                            addTabWidgetScroll(tabNum, new RowWidgetInvRow(stackRow));
                        }
                    }
                    else if (current.is(Items.ARMOR_STAND)
                    || BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment",Tag.TAG_COMPOUND)!=null) {
                        if (current.is(Items.ARMOR_STAND))
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Armor Stand" : "Offhand Armor Stand"));
                        else
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Entity" : "Offhand Entity"));

                        ItemStack[] stacks = new ItemStack[8];
                        stacks[0] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.feet"));
                        stacks[1] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.legs"));
                        stacks[2] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.chest"));
                        stacks[3] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.head"));
                        stacks[4] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.body"));
                        stacks[5] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.saddle"));
                        stacks[6] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.mainhand"));
                        stacks[7] = BlackMagick.itemFromNbtTag(
                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.equipment.offhand"));

                        addTabWidgetScroll(tabNum, new RowWidgetInvRow(stacks,RowWidgetInvRow.ARMOR_STAND_SPRITES));
                    }
                    else if (size[0]>0 && size[1]>0) {
                        {
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Container" : "Offhand Container"));
                        }
                        ListTag itemsList = null;
                        if (BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container[0]",Tag.TAG_COMPOUND) != null)
                            itemsList = (ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container");
                        for (int r=0; r<size[0]; r++) {
                            ItemStack[] stacks = new ItemStack[size[1]];
                            if (itemsList != null)
                                for (int c=0; c<size[1]; c++) {
                                    for (int index=0; index<itemsList.size(); index++) {
                                        if (itemsList.get(index).getId() == Tag.TAG_COMPOUND
                                        && ((CompoundTag)itemsList.get(index)).getInt("slot").isPresent()
                                        && ((CompoundTag)itemsList.get(index)).getInt("slot").get()==(r*size[1]+c)) {
                                            stacks[c] = BlackMagick.itemFromNbt(
                                                BlackMagick.validCompound(BlackMagick.getNbtPath((CompoundTag)itemsList.get(index),"item",Tag.TAG_COMPOUND)));
                                        }
                                    }
                                }
                            addTabWidgetScroll(tabNum, new RowWidgetInvRow(stacks));
                        }
                    }
                }
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget("Saved Hotbars"));
            }
            for (int h=0; h<HotbarManager.NUM_HOTBAR_GROUPS; h++) {
                List<ItemStack> row = minecraft.getHotbarManager().get(h).load(BlackMagick.getRegistryAccess());
                ItemStack[] stacks = new ItemStack[9];
                for (int c=0; c<stacks.length; c++) {
                    if (row.size() > c)
                        stacks[c] = row.get(c);
                    else
                        stacks[c] = ItemStack.EMPTY;
                }
                addTabWidgetScroll(tabNum, new RowWidgetInvRow(stacks));
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
        }

        if (tab == tabNum) {
            btnTab(tab);
        }
        resetSuggs();
        suggsPause = false;
    }

    public int createBlankTabSetup() {
        int tabNum = CACHE_TAB_BLANK;
        textComponentPreview = null;
        textComponentPreviewBook = false;
        showBannerPreview = false;
        showPosePreview = false;
        tabScroll[tabNum] = 0d;
        setErrorMsg(null);

        if (!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        for (RowWidget r : TAB_WIDGETS_SCROLL.get(tabNum)) {
            for (SmartEditBox t : r.txts) {
                this.UNSAVED_TEXT_WIDGETS.remove(t);
                this.ALL_TEXT_WIDGETS.remove(t);
            }
            for (PosWidget p : r.wids) {
                if (p.w() != null) {
                    this.UNSAVED_TEXT_WIDGETS.remove(p.w());
                    this.ALL_TEXT_WIDGETS.remove(p.w());
                    this.ALL_SLIDER_WIDGETS.remove(p.w());
                }
            }
        }
        for (PosWidget r : TAB_WIDGETS_LOCKED.get(tabNum)) {
            this.UNSAVED_TEXT_WIDGETS.remove(r.w());
            this.ALL_TEXT_WIDGETS.remove(r.w());
        }
        TAB_WIDGETS_SCROLL.get(tabNum).clear();
        TAB_WIDGETS_LOCKED.get(tabNum).clear();

        return tabNum;
    }

    private enum NbtEditStyle {
        SNBT,
        NBT,
        TEMPLATE
    }

    private record NbtEdit(CompoundTag original, CompoundTag current, boolean unsaved, PathNode[] pathNodes, String fullPath, PathInfo pi, TextComponentPathNode textComponentNode) {

        public static NbtEdit newEdit(CompoundTag startValue) {
            CompoundTag newNbt = startValue==null ? new CompoundTag() : startValue.copy();
            return new NbtEdit(startValue, newNbt, false, new PathNode[0], "", PathHelper.getItemPath(newNbt, new PathNode[0]), null);
        }

        public NbtEdit withPath(PathNode[] path) {
            PathNode[] newPath = path == null ? new PathNode[0] : path;

            TextComponentPathNode textComponentNode = null;
            List<PathNode> textComponentPathNodes = Lists.newArrayList();
            for (PathNode n : newPath) {
                textComponentPathNodes.add(n);
                PathFlag f = PathHelper.getItemPath(this.current, textComponentPathNodes.toArray(new PathNode[0])).getFlag();
                if (f.isTextComponent()) {
                    textComponentNode = new TextComponentPathNode(PathNode.resolvePath(textComponentPathNodes.toArray(new PathNode[0])), f);
                    break;
                }
            }

            return new NbtEdit(this.original, this.current, this.unsaved, newPath, PathNode.resolvePath(newPath), PathHelper.getItemPath(this.current, path), textComponentNode);
        }

        public NbtEdit addPath(PathNode node) {
            List<PathNode> newPath = Lists.newArrayList();
            newPath.addAll(List.of(pathNodes));
            newPath.add(node);
            return withPath(newPath.toArray(new PathNode[0]));
        }

        public NbtEdit backPath() {
            if (pathNodes.length == 0) {
                return this;
            }
            PathNode[] newPath = new PathNode[pathNodes.length-1];
            for (int i=0; i<newPath.length; i++)
                newPath[i] = pathNodes[i];
            return withPath(newPath);
        }

        public NbtEdit update(CompoundTag newValue) {
            return new NbtEdit(this.original, newValue,
                !BlackMagick.elementsEqual(
                    BlackMagick.itemToNbtStorage(BlackMagick.itemFromNbt(this.original)),
                    BlackMagick.itemToNbtStorage(BlackMagick.itemFromNbt(newValue))
                ),
                this.pathNodes, this.fullPath, this.pi, this.textComponentNode);
        }

        public ItemStack getItem() {
            return BlackMagick.itemFromNbt(this.current);
        }

        public Tag getEditElement() {
            return BlackMagick.getNbtPath(current, fullPath);
        }

        public Component getRevertItemChanges() {
            return BlackMagick.getElementDifferencesOrColorfulText(
                this.current,
                BlackMagick.itemToNbt(BlackMagick.itemFromNbt(this.original))
            );
        }

        public Component getNewItemDiff() {
            return BlackMagick.getElementDifferencesOrColorfulText(
                this.current,
                BlackMagick.itemToNbt(BlackMagick.itemFromNbt(this.current))
            );
        }

        public boolean newItemIsDiff() {
            return !BlackMagick.elementsEqual(this.current, BlackMagick.itemToNbt(BlackMagick.itemFromNbt(this.current)));
        }

        public Component getPathChanges() {
            return BlackMagick.getElementDifferencesOrColorfulText(
                BlackMagick.getNbtPath(this.original,fullPath),
                BlackMagick.getNbtPath(this.current,fullPath)
            );
        }

        public String getPrevPath() {
            if (pathNodes.length <= 1)
                return "";

            PathNode[] prevPathNodes = new PathNode[pathNodes.length-1];
            for (int i=0; i<prevPathNodes.length; i++)
                prevPathNodes[i] = pathNodes[i];
            return PathNode.resolvePath(prevPathNodes);
        }

    }

    private record TextComponentPathNode(String path, PathFlag flag) {}

    /**
     * Used to update current nbtEdit page
     * 
     * @param path path to edit compound
     * @param element value to set at path
     */
    protected void nbtEditUpdate(String path, Tag element) {
        nbtEditUpdate(BlackMagick.setNbtPath(nbtEdit.current(),path,element));
    }

    /**
     * Used to update current nbtEdit page
     * 
     * @param newValue edited compound
     */
    protected void nbtEditUpdate(CompoundTag newValue) {
        if (nbtEdit != null) {
            nbtEdit = nbtEdit.update(newValue);
        }
        nbtEditRefreshScreen();
    }

    protected boolean nbtEditUnsaved() {
        return nbtEdit != null && nbtEdit.unsaved();
    }

    protected void nbtEditRefreshScreen() {
        createBlankTabNbtEdit(nbtEdit);
    }

    /**
     * Create a new nbtEdit instance with a starting value and path
     * 
     * @param startValue
     * @param path
     */
    public void createBlankTabNbtEdit(CompoundTag startValue, PathNode... path) {
        createBlankTabNbtEdit(NbtEdit.newEdit(startValue).withPath(path));
    }

    /**
     * Call any time nbtEdit or nbtEdit page needs to be updated
     * 
     * @param newNbtEdit
     */
    public void createBlankTabNbtEdit(NbtEdit newNbtEdit) {
        suggsPause = true;
        int tabNum = createBlankTabSetup();

        if (nbtEdit != null && tab == tabNum && tabWidget != null) {
            nbtEditScroll.put(nbtEdit.fullPath(),tabWidget.scrollAmount());
        }
        nbtEditScrollNow = true;
        nbtEdit = newNbtEdit;

        if (nbtEdit.textComponentNode() != null) {
            ParsedText text = BlackMagick.textComponentFromNbt(BlackMagick.getNbtPath(nbtEdit.current(),nbtEdit.textComponentNode().path()));
            textComponentPreview = text.text();
            if (text.isValid())
                switch (nbtEdit.textComponentNode().flag()) {
                    case TEXT_COMPONENT_ITALIC: {
                        textComponentPreview = Component.empty().withStyle(ChatFormatting.ITALIC).append(textComponentPreview);
                        break;
                    }
                    case TEXT_COMPONENT_LORE: {
                        textComponentPreview = Component.empty().withStyle(ChatFormatting.ITALIC,ChatFormatting.DARK_PURPLE).append(textComponentPreview);
                        break;
                    }
                    case TEXT_COMPONENT_BOOK: {
                        textComponentPreviewBook = true;
                        break;
                    }
                    default: break;
                }
        }

        ItemSlotButton btnStyleTemplate = new ItemSlotButton(this, TAB_SIZE, NBT_EDIT_STYLE_ITEMS[0].get(), btn -> {
            nbtEditStyle = NbtEditStyle.TEMPLATE;
            nbtEditRefreshScreen();
        }).showSlot(false);
        btnStyleTemplate.setTooltip(Tooltip.create(grayWhiteText("Style: ","Template")));
        addTabWidgetLocked(tabNum, new PosWidget(btnStyleTemplate,0-TAB_SIZE,30+TAB_OFFSET));

        ItemSlotButton btnStyleNbt = new ItemSlotButton(this, TAB_SIZE, NBT_EDIT_STYLE_ITEMS[1].get(), btn -> {
            nbtEditStyle = NbtEditStyle.NBT;
            nbtEditRefreshScreen();
        }).showSlot(false);
        btnStyleNbt.setTooltip(Tooltip.create(grayWhiteText("Style: ","NBT")));
        addTabWidgetLocked(tabNum, new PosWidget(btnStyleNbt,0-TAB_SIZE,30+TAB_OFFSET+1*(TAB_SPACING+TAB_SIZE)));

        ItemSlotButton btnStyleSnbt = new ItemSlotButton(this, TAB_SIZE, NBT_EDIT_STYLE_ITEMS[2].get(), btn -> {
            nbtEditStyle = NbtEditStyle.SNBT;
            nbtEditRefreshScreen();
        }).showSlot(false);
        btnStyleSnbt.setTooltip(Tooltip.create(grayWhiteText("Style: ","SNBT")));
        addTabWidgetLocked(tabNum, new PosWidget(btnStyleSnbt,0-TAB_SIZE,30+TAB_OFFSET+2*(TAB_SPACING+TAB_SIZE)));

        switch (nbtEditStyle) {
            case TEMPLATE : btnStyleTemplate.active = false; break;
            case NBT : btnStyleNbt.active = false; break;
            case SNBT : btnStyleSnbt.active = false; break;
        }

        {
            Button w = Button.builder(Component.nullToEmpty("<"),btn -> {
                createBlankTabNbtEdit(nbtEdit.backPath());
            }).size(15,WID_HEIGHT).build();
            String prevPath = nbtEdit.getPrevPath();
            w.setTooltip(Tooltip.create(prevPath.isEmpty() ? grayWhiteText("View path: ","{}")
                : grayWhiteText("View path:\n",prevPath)));
            if (nbtEdit.pathNodes().length==0) {
                w.active = false;
                w.setTooltip(null);
            }
            addTabWidgetLocked(tabNum, new PosWidget(w,5,5));
        }
        {
            SmartEditBox pathTxt = new SmartEditBox(this.font,0,0,(backgroundWidth-5-40)-(5+40+5+15+15)-5,WID_HEIGHT,Component.nullToEmpty(""));
            pathTxt.setSuggsResponder(value -> {
                setErrorMsg(null);
                if (widgetCacheTest(WidgetCacheType.NBT_EDIT_PATH_BTN)) {
                    Button pathBtn = (Button)widgetCacheGet(WidgetCacheType.NBT_EDIT_PATH_BTN);
                    pathTxt.setTextColor(LABEL_COLOR);
                    pathBtn.setTooltip(null);
                    pathBtn.active = false;
                    if (!(value.equals(nbtEdit.fullPath()) || (value.equals("{}") && nbtEdit.fullPath().isEmpty()))) {
                        PathNode[] parsePath = PathNode.parsePath(value);

                        if (parsePath != null && !PathNode.resolvePath(parsePath).equals(nbtEdit.fullPath())) {
                            pathTxt.setTextColor(TEXT_COLOR);
                            String newPath = PathNode.resolvePath(parsePath);
                            pathBtn.setTooltip(Tooltip.create(newPath.isEmpty() ? grayWhiteText("Path: ","{}")
                                : grayWhiteText("Path:\n",newPath)));
                            pathBtn.active = true;
                        }
                        else {
                            pathTxt.setTextColor(ERROR_COLOR);
                            pathBtn.setTooltip(Tooltip.create(errorText("Invalid path")));
                            setErrorMsg("Invalid path: "+value);
                        }
                    }
                }
            });
            pathTxt.setMaxLength(MAX_TEXT_LENGTH);
            pathTxt.setTooltipDelay(TOOLTIP_DELAY);
            addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.NBT_EDIT_PATH_TXT,pathTxt),5+15,5));

            Button goPathBtn = Button.builder(Component.nullToEmpty(">"),btn -> {
                String value = pathTxt.getValue();
                if (!(value.equals(nbtEdit.fullPath()) || (value.equals("{}") && nbtEdit.fullPath().isEmpty()))) {
                    PathNode[] parsePath = PathNode.parsePath(value);

                    if (parsePath != null && !PathNode.resolvePath(parsePath).equals(nbtEdit.fullPath())) {
                        createBlankTabNbtEdit(nbtEdit.withPath(parsePath));
                    }
                }
                unsel();
            }).size(15,WID_HEIGHT).build();
            addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.NBT_EDIT_PATH_BTN,goPathBtn),backgroundWidth-5-40-5-40-5-15,5));

            pathTxt.setValue(nbtEdit.fullPath());
        }
        {
            Button w = Button.builder(Component.nullToEmpty("Cancel"),btn -> {
                nbtEdit = null;
                nbtEditScroll.clear();
                this.btnTab(CACHE_TAB_MAIN);
            }).size(40,WID_HEIGHT).build();
            w.setTooltipDelay(TOOLTIP_DELAY);
            addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.NBT_EDIT_CANCEL_BTN,w),backgroundWidth-5-40-5-40,5));
        }
        {
            Button w = Button.builder(Component.nullToEmpty("Save"), btn -> {
                if (!nbtEdit.getItem().isEmpty() && BlackMagick.isCreative(minecraft)) {
                    BlackMagick.setItemMain(nbtEdit.getItem());
                    nbtEdit = null;
                    nbtEditScroll.clear();
                    this.btnTab(CACHE_TAB_MAIN);
                }
                unsel();
            }).size(40,WID_HEIGHT).build();
            w.setTooltipDelay(TOOLTIP_DELAY);
            addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.NBT_EDIT_SAVE_BTN,w),backgroundWidth-5-40,5));
        }

        Tag editElement = nbtEdit.getEditElement();

        if (
            nbtEdit.pi().isEmpty()
            || (editElement != null && editElement.getId() == Tag.TAG_COMPOUND && !nbtEdit.pi().hasPathType(PathType.COMPOUND))
            || (editElement != null && editElement.getId() == Tag.TAG_LIST && !nbtEdit.pi().hasPathType(PathType.LIST))
        ) {
            btnStyleTemplate.setError(ItemSlotButton.ItemError.WARN);
            btnStyleTemplate.setTooltip(Tooltip.create(grayWhiteText("Style: ","Template")
                .append(warnText("\nNo template found for current path and element"))));
        }

        switch (nbtEditStyle) {
            case TEMPLATE: {
                if (editElement != null && nbtEdit.pi().hasPathType(PathType.COMPOUND) && editElement.getId() == Tag.TAG_COMPOUND) {
                    addTabWidgetScroll(tabNum, new RowWidget(Component.empty().append(editElement == null ? "Set Element"
                        : ("Edit " + PathHelper.nbtTypeFromByte(editElement.getId()).label())).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.UNDERLINE)));

                    CompoundTag thisCompound = (editElement != null && editElement.getId() == Tag.TAG_COMPOUND) ? (CompoundTag)editElement : new CompoundTag();

                    Set<String> allKeys = Sets.newHashSet();
                    allKeys.addAll(nbtEdit.pi().getCompoundKeys(nbtEdit.current(), nbtEdit.pathNodes()));
                    allKeys.addAll(thisCompound.keySet());

                    List<String> keyLabelOrder = Lists.newArrayList();
                    Map<String,Set<String>> keyLabelSetsPresent = Maps.newHashMap();
                    Map<String,Set<String>> keyLabelSetsMissing = Maps.newHashMap();

                    for (String k : allKeys) {
                        String lbl = nbtEdit.pi().getCompoundKeyLabel(k, nbtEdit.current(), nbtEdit.pathNodes());
                        if (!keyLabelOrder.contains(lbl)) {
                            keyLabelOrder.add(lbl);
                        }
                        if (thisCompound.contains(k)) {
                            if (!keyLabelSetsPresent.containsKey(lbl))
                                keyLabelSetsPresent.put(lbl,Sets.newHashSet());
                            keyLabelSetsPresent.get(lbl).add(k);
                        }
                        else {
                            if (!keyLabelSetsMissing.containsKey(lbl))
                                keyLabelSetsMissing.put(lbl,Sets.newHashSet());
                            keyLabelSetsMissing.get(lbl).add(k);
                        }
                    }

                    Collections.sort(keyLabelOrder, String.CASE_INSENSITIVE_ORDER);

                    int labelsLength = PathInfo.COMPOUND_KEY_SPECIAL_LABELS.length;
                    for (int i=0; i<labelsLength; i++) {
                        String lbl = PathInfo.COMPOUND_KEY_SPECIAL_LABELS[labelsLength-1-i];
                        if (keyLabelOrder.contains(lbl)) {
                            keyLabelOrder.remove(lbl);
                            keyLabelOrder.add(0,lbl);
                        }
                    }

                    if (!keyLabelSetsPresent.isEmpty())
                        for (String lbl : keyLabelOrder) {
                            if (keyLabelSetsPresent.containsKey(lbl)) {
                                addTabWidgetScroll(tabNum, new RowWidget(lbl));
                                for (String k : BlackMagick.sortSet(keyLabelSetsPresent.get(lbl)))
                                    addTabWidgetScroll(tabNum, RowWidgetElement.compoundTemplateElement(this, k, nbtEdit.current(), nbtEdit.pathNodes()));
                            }
                        }
                    if (!keyLabelSetsMissing.isEmpty()) {
                        if (!keyLabelSetsPresent.isEmpty())
                            addTabWidgetScroll(tabNum, new RowWidget(Component.empty().append("Unset Keys").withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.UNDERLINE)));
                        for (String lbl : keyLabelOrder) {
                            if (keyLabelSetsMissing.containsKey(lbl)) {
                                addTabWidgetScroll(tabNum, new RowWidget(lbl));
                                for (String k : BlackMagick.sortSet(keyLabelSetsMissing.get(lbl)))
                                    addTabWidgetScroll(tabNum, RowWidgetElement.compoundTemplateElement(this, k, nbtEdit.current(), nbtEdit.pathNodes()));
                            }
                        }
                    }

                    if (nbtEdit.pi().showCompoundNewKeyRow()) {
                        addTabWidgetScroll(tabNum, new RowWidget("Set Key"));
                        addTabWidgetScroll(tabNum, RowWidgetElement.customCompoundKey(this, thisCompound, true, nbtEdit.current(), nbtEdit.pathNodes()));
                    }
                    else if (keyLabelOrder.isEmpty()) {
                        addTabWidgetScroll(tabNum, new RowWidget("No key suggestions found."));
                    }

                    addTabWidgetScroll(tabNum, new RowWidget());
                    break;
                }
                else if (editElement != null && nbtEdit.pi().hasPathType(PathType.LIST) && editElement.getId() == Tag.TAG_LIST) {
                    addTabWidgetScroll(tabNum, new RowWidget(Component.empty().append(editElement == null ? "Set Element"
                        : ("Edit " + PathHelper.nbtTypeFromByte(editElement.getId()).label())).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.UNDERLINE)));

                    ListTag thisList = (editElement != null && editElement.getId() == Tag.TAG_LIST) ? (ListTag)editElement : new ListTag();
                    int listSize = thisList.size();

                    for (int i=0; i<listSize; i++)
                        addTabWidgetScroll(tabNum, RowWidgetElement.listTemplateElement(this, thisList, i, nbtEdit.current(), nbtEdit.pathNodes()));

                    if (nbtEdit.pi().getListSize() == -1 || thisList.size() < nbtEdit.pi().getListSize()) {
                        addTabWidgetScroll(tabNum, new RowWidget("Append Element"));
                        addTabWidgetScroll(tabNum, RowWidgetElement.customListElement(this, thisList, true, nbtEdit.current(), nbtEdit.pathNodes()));
                    }

                    addTabWidgetScroll(tabNum, new RowWidget());
                    break;
                }
            }
            case NBT: {
                boolean found = false;
                addTabWidgetScroll(tabNum, new RowWidget(Component.empty().append(editElement == null ? "Set Element"
                    : ("Edit " + PathHelper.nbtTypeFromByte(editElement.getId()).label())).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.UNDERLINE)));
                if (editElement != null) {
                    switch (editElement.getId()) {
                        case Tag.TAG_COMPOUND: {
                            found = true;
                            CompoundTag thisCompound = (CompoundTag)editElement;

                            for (String k : BlackMagick.sortSet(thisCompound.keySet()))
                                addTabWidgetScroll(tabNum, new RowWidgetElement(k, nbtEdit.current(), nbtEdit.pathNodes()));

                            addTabWidgetScroll(tabNum, new RowWidget("Set Key"));
                            addTabWidgetScroll(tabNum, RowWidgetElement.customCompoundKey(this, thisCompound, false, nbtEdit.current(), nbtEdit.pathNodes()));

                            break;
                        }
                        case Tag.TAG_LIST: {
                            found = true;
                            ListTag thisList = (ListTag)editElement;
                            int listSize = thisList.size();

                            for (int i=0; i<listSize; i++)
                                addTabWidgetScroll(tabNum, new RowWidgetElement(thisList, i, nbtEdit.current(), nbtEdit.pathNodes()));

                            addTabWidgetScroll(tabNum, new RowWidget("Append Element"));
                            addTabWidgetScroll(tabNum, RowWidgetElement.customListElement(this, thisList, false, nbtEdit.current(), nbtEdit.pathNodes()));

                            break;
                        }
                        default: break;
                    }
                }
                if (!found) {
                    if (nbtEditStyle == NbtEditStyle.TEMPLATE)
                        addTabWidgetScroll(tabNum, RowWidgetElement.fallbackTemplateElement(this));
                    else
                        addTabWidgetScroll(tabNum, RowWidgetElement.fallbackElement(this));
                }
                addTabWidgetScroll(tabNum, new RowWidget());
                break;
            }
            case SNBT: {
                final String currentVal = BlackMagick.nbtToSnbt(editElement);

                MultiLineEditBox elementTxt = MultiLineEditBox.builder().setX(0).setY(0).build(minecraft.font, ROW_WIDTH, ROW_HEIGHT*6, Component.nullToEmpty(""));
                final PathFlag pathFlag = nbtEdit.pi().getFlag();
                elementTxt.setValueListener(value -> {
                    if (widgetCacheTest(WidgetCacheType.NBT_EDIT_SNBT_ADD_BTN)) {
                        Button addBtn = (Button)widgetCacheGet(WidgetCacheType.NBT_EDIT_SNBT_ADD_BTN);
                        setErrorMsg(null);
                        addBtn.active = false;
                        addBtn.setTooltip(null);

                        if (!value.isEmpty()) {
                            Tag el = BlackMagick.nbtFromSnbt(value);
                            if (el == null) {
                                setErrorMsg("Invalid element");
                                addBtn.setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else if (BlackMagick.elementsEqual(editElement,el)) {
                                addBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                            }
                            else {
                                addBtn.active = true;
                                addBtn.setTooltip(Tooltip.create(grayWhiteText("Set value:").append(getElementTooltipInfo(el,pathFlag))));
                            }
                        }
                    }
                });
                addTabWidgetLocked(tabNum, new PosWidget(elementTxt,ROW_LEFT_LOCKED,ROW_TOP));
                {
                    Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                        Tag el = BlackMagick.nbtFromSnbt(elementTxt.getValue());
                        if (el!=null)
                            nbtEditUpdate(nbtEdit.fullPath(),el);
                        unsel();
                    }).size(20,WID_HEIGHT).build();
                    addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.NBT_EDIT_SNBT_ADD_BTN,w), ROW_RIGHT_LOCKED-20-(editElement==null ? 0 : 20), ROW_TOP+ROW_HEIGHT*6));
                }
                if (editElement!=null) {
                    Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                        if (nbtEdit.fullPath().isEmpty())
                            nbtEditUpdate(new CompoundTag());
                        else
                            nbtEditUpdate(nbtEdit.fullPath(), null);
                        createBlankTabNbtEdit(nbtEdit.backPath());
                    }).size(20,WID_HEIGHT).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                    addTabWidgetLocked(tabNum, new PosWidget(w, ROW_RIGHT_LOCKED-20, ROW_TOP+ROW_HEIGHT*6));
                }
                elementTxt.setValue(currentVal);
                break;
            }
        }

        // else if (elType == PathType.TEXT) { to_do old custom pages
            // textComponentEffectMode = -1;
            // textComponentEffectPath = null;
            // textComponentEffectBase = null;
            // String startVal = "";

            // if (el2 != null)
            //     startVal = BlackMagick.nbtToSnbt(el2);
            // else
            //     startVal = "{text:\"\"}";

            // if (args.getString("textComponentOverride").isPresent())
            //     startVal = args.getString("textComponentOverride").get();

            // {
            //     MultiLineEditBox w = new MultiLineEditBox(minecraft.font, x+15-3, y+ROW_TOP, 240-36, ROW_HEIGHT*6,
            //         Component.nullToEmpty(""), Component.nullToEmpty(""));
            //     w.setValueListener(value -> {
            //         setErrorMsg(null);
            //         updateTextComponentPreview(fullPath,value);
            //         if (textComponentBaseValid) {
            //             nbtEditUpdate(path,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
            //                 BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),fullPath,
            //                 BlackMagick.nbtFromSnbt(value)),path),saveBtn,
            //                 path2==null ? null : fullPath);
            //         }
            //         else {
            //             setErrorMsg("Invalid Text Component");
            //             nbtEditUpdate(path,blankTabEl,saveBtn,path2==null ? null : fullPath);
            //         }
            //     });
            //     addTabWidgetLocked(tabNum, new PosWidget(w,15-3,ROW_TOP));
            //     this.ALL_TEXT_WIDGETS.add(w);
            //     w.setValue(startVal);
            // }
            // {
            //     addTabWidgetLocked(tabNum, new PosWidget(Button.builder(Component.nullToEmpty("Add Text"), button -> {
            //         textComponentEffectMode = 1;
            //         String baseTextComponent;
            //         if (textComponentBaseValid)
            //             baseTextComponent = textComponentBaseText;
            //         else
            //             baseTextComponent = "{text:\"\"}";

            //         CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
            //         newArgs.putString("baseTextComponent",baseTextComponent);
            //         if (args.getList("path2").isPresent())
            //             newArgs.put("path2",args.get("path2"));
            //         if (blankTabEl != null)
            //             newArgs.put("overrideEl",blankTabEl);

            //         CompoundTag cancelNbt = new CompoundTag();
            //         if (cancelElCopy != null)
            //             cancelNbt.put("el",cancelElCopy);
            //         newArgs.put("cancelEl",cancelNbt);

            //         createBlankTab(BlankTabMode.TEXT_COMPONENT,newArgs);
            //         unsel();
            //     }).size(60,WID_HEIGHT).build(),15-3,ROW_TOP+ROW_HEIGHT*6));
            // }
            // {
            //     addTabWidgetLocked(tabNum, new PosWidget(Button.builder(Component.nullToEmpty("Add Effect"), button -> {
            //         textComponentEffectMode = 0;
            //         String baseTextComponent;
            //         if (textComponentBaseValid)
            //             baseTextComponent = textComponentBaseText;
            //         else
            //             baseTextComponent = "{text:\"\"}";

            //         CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
            //         newArgs.putString("baseTextComponent",baseTextComponent);
            //         if (args.getList("path2").isPresent())
            //             newArgs.put("path2",args.get("path2"));
            //         if (blankTabEl != null)
            //             newArgs.put("overrideEl",blankTabEl);

            //         CompoundTag cancelNbt = new CompoundTag();
            //         if (cancelElCopy != null)
            //             cancelNbt.put("el",cancelElCopy);
            //         newArgs.put("cancelEl",cancelNbt);

            //         createBlankTab(BlankTabMode.TEXT_COMPONENT,newArgs);
            //         unsel();
            //     }).size(60,WID_HEIGHT).build(),15-3+60+5,ROW_TOP+ROW_HEIGHT*6));
            // }
        // }
        // else if (elType == PathType.DECIMAL_COLOR) {
        //     editorOutputLocked = true;
        //     addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,WidgetCacheType.TXT_DECIMAL_COLOR));
        //     addTabWidgetScroll(tabNum, new RowWidgetEditor(WidgetCacheType.TXT_DECIMAL_COLOR));
        //     addTabWidgetScroll(tabNum, new RowWidget("Color Editor"));

        //     int rgbNum = 0;
        //     colorHexTxts.get(rgbNum).clear();
        //     colorDecTxts.get(rgbNum).clear();

        //     for (int i=0; i<3; i++) {
        //         colorRgbSliders.get(rgbNum).get(i).clear();
        //         colorItemWids.get(rgbNum).get(i).clear();
        //         {
        //             RgbSlider w = new RgbSlider(rgbNum,i,false,true);
        //             PosWidget w2 = new PosWidget(rgbItems[i],180,0);
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),w2}));
        //             colorRgbSliders.get(rgbNum).get(i).add(w);
        //             colorItemWids.get(rgbNum).get(i).add(w2);
        //         }
        //     }

        //     {
        //         SmartEditBox w = new SmartEditBox(font, x+15, 0, 80, WID_HEIGHT, Component.nullToEmpty(""));
        //         w.setMaxLength(MAX_TEXT_LENGTH);
        //         w.setSuggsResponder(value -> {
        //             trySetColorHex(0,value,w);
        //         });
        //         SmartEditBox w2 = new SmartEditBox(font, x+15+100+5, 0, 80, WID_HEIGHT, Component.nullToEmpty(""));
        //         w2.setMaxLength(MAX_TEXT_LENGTH);
        //         w2.setSuggsResponder(value -> {
        //             trySetColorDec(0,value,w2);
        //         });
        //         addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
        //         colorHexTxts.get(rgbNum).add(w);
        //         colorDecTxts.get(rgbNum).add(w2);
        //     }

        //     for (int i=0; i<3; i++) {
        //         colorHslSliders.get(i).clear();
        //         {
        //             RgbSlider w = new RgbSlider(rgbNum,i,false,false);
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
        //             colorHslSliders.get(i).add(w);
        //         }
        //     }

        //     updateColorSets();
        //     editorOutputLocked = false;
        // }
        // else if (elType == PathType.BANNER) {
        //     if (el2==null || el2.getId()!=Tag.TAG_COMPOUND)
        //         el2 = new CompoundTag();

        //     CompoundTag bannerNbt = (CompoundTag)el2;
        //     String bannerCol = null;
        //     String bannerPat = null;
        //     bannerShield = false;
        //     if (editItem.is(Items.SHIELD)) {
        //         bannerShield = true;
        //         showBannerPreview = true;
        //         bannerChangePreview.load(BlackMagick.validCompoundFromString("{equipment:{mainhand:"
        //             +BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(editItem))+"},Invisible:true,Pose:{RightArm:[-90f,-90f,0f]}}"));
        //     }
        //     else if (editItem.is(ItemTags.BANNERS)) {
        //         showBannerPreview = true;
        //         bannerChangePreview.load(BlackMagick.validCompoundFromString("{equipment:{head:"
        //             +BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(editItem))+"},Invisible:true,Pose:{RightArm:[-90f,-90f,0f]}}"));
        //     }

        //     if (bannerNbt.getString("color").isPresent())
        //         bannerCol = bannerNbt.getString("color").get();
        //     if (bannerNbt.getString("pattern").isPresent())
        //         bannerPat = bannerNbt.getString("pattern").get();

        //     List<String> bannerVals = Lists.newArrayList();
        //     for (String c : ComponentHelper.LIST_DYE_COLOR.getList())
        //         bannerVals.add(c);
        //     for (String b : ComponentHelper.DATA_BANNER_PATTERN.getList())
        //         bannerVals.add(b);

        //     int row=0;
        //     while (!bannerVals.isEmpty()) {
        //         String[] currentVals = new String[Math.min(8,bannerVals.size())];
        //         for (int i=0; i<currentVals.length; i++)
        //             currentVals[i] = bannerVals.remove(0);
        //         addTabWidgetScroll(tabNum,
        //             new RowWidgetBannerRow(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,row<2,currentVals,row<2 ? bannerCol : bannerPat,cancelEl));
        //         row++;
        //     }

        // }
        // else if (elType == PathType.POSE) {
        //     editorOutputLocked = true;

        //     addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,WidgetCacheType.TXT_POSE));
        //     addTabWidgetScroll(tabNum, new RowWidgetEditor(WidgetCacheType.TXT_POSE));
        //     addTabWidgetScroll(tabNum, new RowWidget("Pose Editor"));

        //     String[] poseParts = new String[]{"Head","Body","Right Arm","Left Arm","Right Leg","Left Leg"};
        //     for (int partNum=0; partNum<poseParts.length; partNum++) {
        //         {
        //             poseSliderBtns.get(partNum).clear();
        //             String partKey = poseParts[partNum].replace(" ","");
        //             Button w2 = Button.builder(Component.nullToEmpty(partKey), btn -> {
        //                 poseCompound.remove(partKey);
        //                 updatePose();
        //                 unsel();
        //             }).size(60,WID_HEIGHT).build();
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w2,15,0)}));
        //             poseSliderBtns.get(partNum).add(w2);

        //             for (int partAxis=0; partAxis<3; partAxis++) {
        //                 poseSliders.get(partNum).get(partAxis).clear();
        //                 PoseSlider w = new PoseSlider(partKey,partAxis);
        //                 addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
        //                 poseSliders.get(partNum).get(partAxis).add(w);
        //             }
        //         }
        //     }

        //     updateArmorStand(editItem);
        //     showPosePreview = true;

        //     editorOutputLocked = false;
        // }

        nbtEditRefreshWidgets();
        btnTab(tabNum);
        resetSuggs();
        suggsPause = false;
    }

    private void nbtEditRefreshWidgets() {
        if (widgetCacheTest(WidgetCacheType.NBT_EDIT_SAVE_BTN, WidgetCacheType.NBT_EDIT_CANCEL_BTN, WidgetCacheType.NBT_EDIT_PATH_TXT)) {
            Button saveBtn = (Button)widgetCacheGet(WidgetCacheType.NBT_EDIT_SAVE_BTN);
            Button cancelBtn = (Button)widgetCacheGet(WidgetCacheType.NBT_EDIT_CANCEL_BTN);
            SmartEditBox pathTxt = (SmartEditBox)widgetCacheGet(WidgetCacheType.NBT_EDIT_PATH_TXT);

            pathTxt.setTooltip(Tooltip.create(getPathBoxTooltip()));

            if (nbtEdit.unsaved())
                cancelBtn.setTooltip(Tooltip.create(grayWhiteText("Revert to:\n",nbtEdit.getRevertItemChanges())));
            else
                cancelBtn.setTooltip(Tooltip.create(grayWhiteText("Keep unchanged:\n",nbtEdit.getRevertItemChanges())));

            if (inpError == null)
                setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToSnbt(nbtEdit.current()),inpError));

            saveBtn.active = true;
            saveBtn.setMessage(Component.nullToEmpty("Save"));
            if (!nbtEdit.unsaved()) {
                saveBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Item unchanged")));
                saveBtn.active = false;
            }
            else if (nbtEdit.current() != null && !nbtEdit.getItem().isEmpty()) {
                if (nbtEdit.newItemIsDiff())
                    saveBtn.setMessage(Component.nullToEmpty("Save").copy().withStyle(ChatFormatting.YELLOW));

                Component tempText = grayWhiteText("Set item:\n",nbtEdit.getNewItemDiff());
                if (BlackMagick.isCreative(minecraft)) {
                    saveBtn.setTooltip(Tooltip.create(tempText));
                }
                else {
                    saveBtn.setTooltip(Tooltip.create(Component.empty().append(ERROR_CREATIVE).append("\n\n").append(tempText)));
                    saveBtn.active = false;
                }
            }
            else {
                saveBtn.active = false;
                saveBtn.setMessage(Component.nullToEmpty("Save").copy().withStyle(ChatFormatting.RED));
                saveBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Error loading item")));
            }
        }
    }

    public void createBlankTabTextComponent() {
        int tabNum = createBlankTabSetup();
        // to_do old custom page
        // if (args.getString("path").isPresent() && args.getString("baseTextComponent").isPresent() && textComponentEffectMode>=0 && textComponentEffectMode<=2) {
        //     String path = args.getString("path").get();
        //     String baseTextComponent = args.getString("baseTextComponent").get();

        //     textComponentEffectPath = fullPath;
        //     textComponentEffectBase = baseTextComponent;
        //     valid = true;
        //     {
        //         Button w = Button.builder(Component.nullToEmpty("Cancel"),btn -> {
        //             // textComponentEffectMode = -1;
        //             // textComponentEffectPath = null;
        //             // textComponentEffectBase = null;
        //             // CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
        //             // if (args.getList("path2").isPresent())
        //             //     newArgs.put("path2",args.get("path2"));
        //             // newArgs.put("textComponentOverride",args.get("baseTextComponent"));
        //             // if (blankTabEl != null)
        //             //     newArgs.put("overrideEl",blankTabEl);

        //             // CompoundTag cancelNbt = new CompoundTag();
        //             // if (cancelElCopy != null)
        //             //     cancelNbt.put("el",cancelElCopy);
        //             // newArgs.put("cancelEl",cancelNbt);

        //             // createBlankTab(BlankTabMode.NBT_EDIT,newArgs);
        //         }).size(40,WID_HEIGHT).build();
        //         w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep text as:\n"+baseTextComponent)));
        //         addTabWidgetLocked(tabNum, new PosWidget(w,5,5));
        //     }
        //     final Button saveBtn;
        //     {
        //         saveBtn = Button.builder(Component.nullToEmpty("Add"), btn -> {
        //             // if (textComponentEffectValid) {
        //             //     textComponentEffectMode = -1;
        //             //     textComponentEffectPath = null;
        //             //     textComponentEffectBase = null;
        //             //     CompoundTag newArgs = BlackMagick.validCompoundFromString(
        //             //         "{path:\""+path+"\"}");
        //             //     newArgs.put("textComponentOverride",BlackMagick.nbtFromSnbt(textComponentEffectFull));
        //             //     if (args.getList("path2").isPresent())
        //             //         newArgs.put("path2",args.get("path2"));
        //             //     if (blankTabEl != null)
        //             //         newArgs.put("overrideEl",blankTabEl);

        //             //     CompoundTag cancelNbt = new CompoundTag();
        //             //     if (cancelElCopy != null)
        //             //         cancelNbt.put("el",cancelElCopy);
        //             //     newArgs.put("cancelEl",cancelNbt);

        //             //     createBlankTab(BlankTabMode.NBT_EDIT,newArgs);
        //             // }
        //             unsel();
        //         }).size(40,WID_HEIGHT).build();
        //         addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_ADD_BTN,saveBtn),240-5-40,5));
        //     }

        //     {
        //         SmartEditBox w = new SmartEditBox(this.font,x+5+40+5,y+5,(240-5-40)-(5+40+5)-5,WID_HEIGHT,Component.nullToEmpty(""));
        //         w.setEditable(false);
        //         w.setMaxLength(MAX_TEXT_LENGTH);
        //         w.setValue(cleanPath(fullPath));
        //         w.setTooltip(Tooltip.create(Component.nullToEmpty("Current path:\n"+fullPath)));
        //         addTabWidgetLocked(tabNum, new PosWidget(w,5+40+5,5));
        //     }

        //     updateTextComponentPreview(fullPath,baseTextComponent);

        //     if (textComponentEffectMode == 0) {
        //         addTabWidgetScroll(tabNum, new RowWidget("Gradient"));
        //     }
        //     else if (textComponentEffectMode == 1) {
        //         addTabWidgetScroll(tabNum, new RowWidget("Text Element"));
        //     }
        //     if (textComponentEffectMode == 0 || textComponentEffectMode == 1) {
        //         SmartEditBox w = new SmartEditBox(this.font,x+15-3,y+ROW_TOP,240-36,WID_HEIGHT,Component.nullToEmpty(""));
        //         w.setMaxLength(MAX_TEXT_LENGTH);
        //         w.setSuggsResponder(value -> {
        //             updateTextComponentEffect();
        //             if (textComponentEffectMode == 1) {
        //                 if (textComponentEffects[7]==1 || textComponentEffects[7]==2) {
        //                     String[] suggestions = textComponentEffects[7]==1 ? ComponentHelper.LIST_KEYBIND.getArray() :
        //                         ComponentHelper.LIST_TRANSLATION_KEY.getArray();
        //                     suggsOnChanged(w,suggestions,null);
        //                 }
        //                 else
        //                     resetSuggs();
        //             }
        //         });
        //         addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
        //             new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY,w),15,0)}));
        //         this.ALL_TEXT_WIDGETS.add(w);
        //     }
        //     if (textComponentEffectMode == 0 || textComponentEffectMode == 1) {
        //         RowWidget row = addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("\u00a7ll"),Component.nullToEmpty("\u00a7oo"),Component.nullToEmpty("\u00a7nn"),
        //             Component.nullToEmpty("\u00a7mm"),Component.nullToEmpty("\u00a7kk")},new int[]{20,20,20,20,20},
        //             new String[]{"none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r",
        //             "none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r"},
        //             null,true,btn -> {
        //                 unsel();
        //                 textComponentEffects[0]++;
        //                 if (textComponentEffects[0]>2)
        //                     textComponentEffects[0]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             },btn -> {
        //                 unsel();
        //                 textComponentEffects[1]++;
        //                 if (textComponentEffects[1]>2)
        //                     textComponentEffects[1]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             },btn -> {
        //                 unsel();
        //                 textComponentEffects[2]++;
        //                 if (textComponentEffects[2]>2)
        //                     textComponentEffects[2]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             },btn -> {
        //                 unsel();
        //                 textComponentEffects[3]++;
        //                 if (textComponentEffects[3]>2)
        //                     textComponentEffects[3]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             },btn -> {
        //                 unsel();
        //                 textComponentEffects[4]++;
        //                 if (textComponentEffects[4]>2)
        //                     textComponentEffects[4]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             }
        //         ));
        //         widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD,row.btns[0]);
        //         widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC,row.btns[1]);
        //         widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED,row.btns[2]);
        //         widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,row.btns[3]);
        //         widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED,row.btns[4]);
        //     }
        //     if (textComponentEffectMode == 0) {
        //         Button w = Button.builder(Component.nullToEmpty("[Radial]"), btn -> {
        //             unsel();
        //             textComponentEffects[5]++;
        //             if (textComponentEffects[5]>1)
        //                 textComponentEffects[5]=0;
        //             updateTextComponentEffect();
        //             updateTextComponentEffectBtns();
        //         }).size(60,WID_HEIGHT).build();
        //         w.setTooltip(Tooltip.create(Component.nullToEmpty("Radial | Linear")));

        //         Button w2 = Button.builder(Component.nullToEmpty("Swap"), btn -> {
        //             unsel();
        //             swapColorSets(0,1);
        //         }).size(40,WID_HEIGHT).build();
        //         w2.setTooltip(Tooltip.create(Component.nullToEmpty("Swap colors")));

        //         addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
        //             new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_RADIAL,w),15,0),
        //             new PosWidget(w2,15+60+5,0)}));
        //     }
        //     else if (textComponentEffectMode == 1) {
        //         Button w = Button.builder(Component.nullToEmpty("Color [Vanilla]"), btn -> {
        //             unsel();
        //             textComponentEffects[6]++;
        //             if (textComponentEffects[6]>2)
        //                 textComponentEffects[6]=0;
        //             updateTextComponentEffect();
        //             updateTextComponentEffectBtns();
        //         }).size(80,WID_HEIGHT).build();
        //         w.setTooltip(Tooltip.create(Component.nullToEmpty("Vanilla | RGB | None")));
        //         SmartEditBox w2 = new SmartEditBox(this.font,0,0,240-36-80-5,WID_HEIGHT,Component.nullToEmpty(""));
        //         w2.setMaxLength(MAX_TEXT_LENGTH);
        //         w2.setSuggsResponder(value -> {
        //             if (textComponentEffects[6]==1) {
        //                 boolean validColor = false;
        //                 for (String f : ComponentHelper.LIST_FORMATTING_COLOR.getList())
        //                     if (f.equals(value))
        //                         validColor = true;

        //                 if (validColor)
        //                     textComponentLastColor = value;
        //                 suggsOnChanged(w2,ComponentHelper.LIST_FORMATTING_COLOR.getArray(),null);
        //                 updateTextComponentEffect();
        //             }
        //             else
        //                 resetSuggs();

        //             if (textComponentEffects[6]==2)
        //                 trySetColorHex(0,value,w2);
        //         });
        //         addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
        //             new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN,w),15,0),
        //             new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_COLOR_TXT,w2),15+80+5,0)}));
        //         colorHexTxts.get(0).clear();
        //         colorHexTxts.get(0).add(w2);
        //     }
        //     if (textComponentEffectMode == 0) {
        //         for (int num=0; num<3; num++) {
        //             RgbSlider w = new RgbSlider(0,num,true,true);
        //             RgbSlider w2 = new RgbSlider(1,num,true,true);
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
        //             colorRgbSliders.get(0).get(num).clear();
        //             colorRgbSliders.get(1).get(num).clear();
        //             colorRgbSliders.get(0).get(num).add(w);
        //             colorRgbSliders.get(1).get(num).add(w2);
        //         }
        //         {
        //             SmartEditBox w = new SmartEditBox(font, x+15, 0, 60, WID_HEIGHT, Component.nullToEmpty(""));
        //             w.setMaxLength(MAX_TEXT_LENGTH);
        //             w.setSuggsResponder(value -> {
        //                 trySetColorHex(0,value,w);
        //             });
        //             SmartEditBox w2 = new SmartEditBox(font, x+15+100+5, 0, 60, WID_HEIGHT, Component.nullToEmpty(""));
        //             w2.setMaxLength(MAX_TEXT_LENGTH);
        //             w2.setSuggsResponder(value -> {
        //                 trySetColorHex(1,value,w2);
        //             });
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
        //             colorHexTxts.get(0).clear();
        //             colorHexTxts.get(1).clear();
        //             colorHexTxts.get(0).add(w);
        //             colorHexTxts.get(1).add(w2);
        //         }
        //         addTabWidgetScroll(tabNum, new RowWidget("Shadow:",9));
        //         addTabWidgetScroll(tabNum, new RowWidget("Font:",10));
        //     }
        //     else if (textComponentEffectMode == 1) {
        //         for (int num=0; num<3; num++) {
        //             RgbSlider w = new RgbSlider(0,num,false,true);
        //             addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
        //             colorRgbSliders.get(0).get(num).clear();
        //             colorRgbSliders.get(0).get(num).add(w);
        //         }
        //         addTabWidgetScroll(tabNum, new RowWidget("Shadow:",9));
        //         addTabWidgetScroll(tabNum, new RowWidget("Font:",10));
        //         {
        //             RowWidget row = addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("[Text]")},
        //             new int[]{80},new String[]{"Text | Keybind | Translate"},null,true,btn -> {
        //                 unsel();
        //                 textComponentEffects[7]++;
        //                 if (textComponentEffects[7]>2)
        //                     textComponentEffects[7]=0;
        //                 updateTextComponentEffect();
        //                 updateTextComponentEffectBtns();
        //             }));
        //             widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE,row.btns[0]);
        //         }
        //         {
        //             addTabWidgetScroll(tabNum, new RowWidget("Translations Only"));
        //         }
        //         {
        //             RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("Params:",3));
        //             widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH,row.txts[0]);
        //         }
        //         {
        //             RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("Fallback:",4));
        //             widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK,row.txts[0]);
        //         }
        //     }
        //     updateTextComponentEffectBtns();
        //     updateColorSets();
        // }

        btnTab(tabNum);
        resetSuggs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleEntry}
     */
    private abstract class TabWidgetEntry extends ContainerObjectSelectionList.Entry<TabWidgetEntry> {
        public TabWidgetEntry() {}

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
            if (super.mouseClicked(mouseButtonEvent, doubleTap))
                return true;
            unsel();
            return false;
        }
    }
    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleList}
     */
    private class TabWidget extends ContainerObjectSelectionList<TabWidgetEntry> {
        public TabWidget(final int tab) {
            super(ItemBuilderScreen.this.minecraft,
                ItemBuilderScreen.this.width+GenericScreen.ScrollList.AREA_WIDTH_OFFSET,
                ItemBuilderScreen.this.backgroundHeight+GenericScreen.ScrollList.AREA_HEIGHT_OFFSET,
                ItemBuilderScreen.this.y+GenericScreen.ScrollList.AREA_Y_OFFSET,
                (tab == CACHE_TAB_INV || tab == CACHE_TAB_SAVED) ? ItemSlotButton.SLOT_HEIGHT : ROW_HEIGHT);

            for (RowWidget row : TAB_WIDGETS_SCROLL.get(tab))
                this.addEntry((TabWidgetEntry)row);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor context) {}

        @Override
        protected void extractListBackground(GuiGraphicsExtractor context) {}

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
            if (super.mouseClicked(mouseButtonEvent, doubleTap))
                return true;
            unsel();
            return false;
        }

    }

    public class RowWidget extends TabWidgetEntry {

        protected final List<AbstractWidget> children = Lists.newArrayList();
        protected Button[] btns = new Button[0];
        protected int[] btnX = new int[0];
        protected int[] btnY = null;
        protected SmartEditBox[] txts = new SmartEditBox[0];
        protected int[] txtX = new int[0];
        protected Component lbl;
        protected boolean lblCentered = false;
        protected int lblColor = LABEL_COLOR;
        protected ItemStack displayItem = null;//to_do remove
        protected int displayItemXoff = 0;
        private List<PosWidget> wids = Lists.newArrayList();

        protected void initChildren() {//to_do remove after reworking btns and txts
            if (this.children.isEmpty()) {
                for (int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for (int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilderScreen.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
                }
            }
        }

        protected AbstractWidget addPosWidget(AbstractWidget w, int x, int y) {
            return addPosWidget(new PosWidget(w,x,y));
        }

        protected AbstractWidget addPosWidget(PosWidget posWidget) {
            if (posWidget==null || posWidget.w() == null)
                return null;
            this.wids.add(posWidget);
            this.children.add(posWidget.w());
            if (posWidget.w() instanceof SmartEditBox || posWidget.w() instanceof EditBox || posWidget.w() instanceof MultiLineEditBox)
                ALL_TEXT_WIDGETS.add(posWidget.w());
            else if (posWidget.w() instanceof AbstractSliderButton)
                ALL_SLIDER_WIDGETS.add(posWidget.w());
            return posWidget.w();
        }

        protected boolean testPosWidget(int index) {//to_do remove
            return (this.wids.size()>index && index>=0);
        }

        protected AbstractWidget getPosWidget(int index) {//to_do remove
            if (testPosWidget(index))
                return this.wids.get(index).w();
            return null;
        }

        protected int getPosWidgetSize() {//to_do remove
            return this.wids.size();
        }

        /**
         * Blank row
         */
        public RowWidget() {}

        /**
         * btn(size) txt
         */
        public RowWidget(String name, String tooltip, OnPress onPress, String[] suggestions, boolean survival) {
            int size = 40; //to_do

            this.btns = new Button[]{Button.builder(Component.nullToEmpty(name), onPress).size(size,WID_HEIGHT).build()};
            this.btnX = new int[]{ROW_LEFT_SCROLL};
            if (tooltip != null)
                this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty(tooltip)));
            if (!BlackMagick.isCreative(minecraft) && !survival)
                this.btns[0].active = false;
            this.txts = new SmartEditBox[]{new SmartEditBox(minecraft.font,0,0, ROW_WIDTH-WID_SPACE-size, WID_HEIGHT, Component.nullToEmpty(""))};
            this.txtX = new int[]{ROW_LEFT_SCROLL+5+size};
            this.txts[0].setSuggsResponder(value -> {
                if (value != null && !value.equals("")) {
                    this.txts[0].setTextColor(TEXT_COLOR);
                    //ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setTextColor(LABEL_COLOR);
                    //ItemBuilder.this.markSaved(this.txts[0]);
                }
                suggsOnChanged(this.txts[0],suggestions);
            });
            this.txts[0].setMaxLength(MAX_TEXT_LENGTH);
            initChildren();
        }

        /**
         * Centered lbl
         */
        public RowWidget(String label) {
            this(Component.nullToEmpty(label));
        }

        public RowWidget(Component label) {
            lbl = label;
            lblCentered = true;
        }

        /**
         * btn...(sizes) txt...(sizes)
         */
        public RowWidget(Component[] names, int[] sizes, String[] tooltips, String[][] suggestions, boolean survival, OnPress... onPressActions) {
            if (names.length <= sizes.length && names.length == tooltips.length && names.length == onPressActions.length) {
                this.btns = new Button[names.length];
                this.btnX = new int[names.length];
                this.txts = new SmartEditBox[sizes.length-this.btns.length];
                this.txtX = new int[this.txts.length];

                int currentX = ROW_LEFT_SCROLL;
                for (int i=0; i<this.btns.length; i++) {
                    this.btnX[i] = currentX;
                    this.btns[i] = Button.builder(names[i], onPressActions[i]).size(sizes[i],WID_HEIGHT).build();
                    currentX += 5 + sizes[i];
                    if (tooltips[i] != null)
                        this.btns[i].setTooltip(Tooltip.create(Component.nullToEmpty(tooltips[i])));

                    if (!BlackMagick.isCreative(minecraft) && !survival)
                        this.btns[i].active = false;
                }
                for (int i=0; i<this.txts.length; i++) {
                    this.txtX[i] = currentX;
                    this.txts[i] = new SmartEditBox(minecraft.font,0,0,
                        sizes[this.btns.length+i], WID_HEIGHT, Component.nullToEmpty(""));
                    currentX += sizes[this.btns.length+i];

                    final int ii = i;
                    this.txts[i].setSuggsResponder(value -> {
                        if (value != null && !value.equals("")) {
                            this.txts[ii].setTextColor(TEXT_COLOR);
                            //ItemBuilder.this.markUnsaved(this.txts[ii]);
                        }
                        else {
                            this.txts[ii].setTextColor(LABEL_COLOR);
                            //ItemBuilder.this.markSaved(this.txts[ii]);
                        }

                        String[] suggsArr = null;
                        if (suggestions != null && suggestions.length > ii && suggestions[ii] != null)
                            suggsArr = suggestions[ii];
                        suggsOnChanged(this.txts[ii],suggsArr);
                    });
                    this.txts[i].setMaxLength(MAX_TEXT_LENGTH);
                }
            }
            initChildren();
        }

        /**
         * PosWidgets
         */
        public static RowWidget of(ItemBuilderScreen context, PosWidget[] p) {
            RowWidget row = context.new RowWidget();
            for (PosWidget w : p)
                row.addPosWidget(w);
            return row;
        }

        public String[] btn() {
            String[] texts = new String[this.txts.length];
            for (int i=0; i<texts.length; i++) {
                this.txts[i].setTextColor(LABEL_COLOR);
                ItemBuilderScreen.this.markSaved(this.txts[i]);
                texts[i] = this.txts[i].getValue();
            }
            ItemBuilderScreen.this.unsel();
            return texts;
        }

        protected String cleanKeyBtn(String key) {
            String components = "components.";
            String minecraft = "minecraft:";
            String removedMinecraft = "!minecraft:";
            if (key.startsWith(components) && key.length()>components.length())
                key = key.substring(components.length());
            if (key.startsWith(minecraft) && key.length()>minecraft.length())
                key = key.substring(minecraft.length());
            else if (key.startsWith(removedMinecraft) && key.length()>removedMinecraft.length())
                key = "!"+key.substring(removedMinecraft.length());
            return key;
        }

        protected String indexKeyBtn(int index) {
            return ""+index+":";
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for (int i=0; i<btns.length; i++) {
                this.btns[i].setX(this.getContentX()+this.btnX[i]);
                if (this.btnY == null)
                    this.btns[i].setY(this.getContentY());
                else
                    this.btns[i].setY(this.getContentY()+this.btnY[i]);
                this.btns[i].extractRenderState(context, mouseX, mouseY, tickDelta);
            }
            for (int i=0; i<txts.length; i++) {
                this.txts[i].setX(this.getContentX()+this.txtX[i]);
                this.txts[i].setY(this.getContentY());
                this.txts[i].extractRenderState(context, mouseX, mouseY, tickDelta);
            }
            for (PosWidget posWidget : this.wids) {
                posWidget.repositionInRow(this);
                posWidget.w().extractRenderState(context, mouseX, mouseY, tickDelta);
            }
            if (lbl != null) {
                if (lblCentered)
                    context.centeredText(ItemBuilderScreen.this.font, this.lbl, ItemBuilderScreen.this.width/2, this.getContentY()+6, lblColor);
                else
                    context.text(ItemBuilderScreen.this.font, this.lbl, ItemBuilderScreen.this.x+ROW_LEFT_SCROLL+3, this.getContentY()+6, lblColor);
            }
            if (displayItem != null) {
                drawItem(context,displayItem,this.getContentX()+ROW_LEFT_SCROLL+2+displayItemXoff,this.getContentY()+2);//to_do remove (used to draw item for component widget)
            }
        }

    }

    class RowWidgetComponent extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.create(Component.nullToEmpty("Set component"));
        private static final ItemStack DEFAULT_COMPONENT_ICON = FortytwoEdit.ITEM_QUESTION.get();
        private static final int ROW_LEFT_ICON = 10;

        /**
         * For use in components screen only.
         * Row contains txt/btn, btns to edit/delete/add, or btns to set trinary/binary values.
         * 
         * @param path nbt path in format: components.minecraft:foo.bar.list[0]
         */
        public RowWidgetComponent(CompoundTag itemStack, String key, boolean isComponent) {
            PathNode[] pathNodes;
            if (isComponent)
                pathNodes = new PathNode[]{PathNode.of("components"),PathNode.of(key)};
            else
                pathNodes = new PathNode[]{PathNode.of(key)};
            final String path = PathNode.resolvePath(pathNodes);

            PathInfo pi = PathHelper.getItemPath(itemStack, pathNodes);
            Component btnTt = getKeyButtonTooltip(PathNode.of(key), pi);
            final PathFlag pathFlag = pi.getFlag();

            Tag startEl = BlackMagick.getNbtPath(itemStack,path);

            String keyBtnTxt = cleanKeyBtn(path);
            int size = 70;
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {})
                .size(size,WID_HEIGHT).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(btnTt));

            displayItem = pi.getIcon() == null ? DEFAULT_COMPONENT_ICON : pi.getIcon();
            displayItemXoff = -9;

            if ((startEl != null && pi.hasPathType(PathType.UNIT) && startEl.getId()==Tag.TAG_COMPOUND && ((CompoundTag)startEl).isEmpty())
            || (startEl == null && pi.getDefaultPathType()==PathType.UNIT)) {

                final int startVal = startEl == null ? 0 : 1;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    String comp = path.replaceFirst("components\\.","");
                    if (!comp.startsWith("!"))
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            itemStack,path,null),"components.!"+comp,new CompoundTag())));
                    else
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,null)));
                    unsel();
                }).size(btnSize,WID_HEIGHT).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,new CompoundTag())));
                    unsel();
                }).size(btnSize,WID_HEIGHT).build()};
                this.btnX = new int[]{(ROW_LEFT_SCROLL+ROW_LEFT_ICON),ROW_RIGHT_SCROLL-(2*btnSize+btnSpacing),ROW_RIGHT_SCROLL-btnSize};

                this.btns[startVal+1].active = false;

                for (int i=0; i<btns.length; i++) {
                    if (i>0 && this.btns[i].active) {
                        if (!BlackMagick.isCreative(minecraft))
                            this.btns[i].setTooltip(Tooltip.create(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                }
            }
            else if ((startEl != null && (startEl.getId()==Tag.TAG_COMPOUND || startEl.getId()==Tag.TAG_LIST))
            || (startEl == null && pi.getDefaultPathType().isComplex())) {

                this.btns = new Button[]{
                keyBtn,
                Button.builder(getComplexButtonText(startEl, pi.getFlag()), btn -> {
                    createBlankTabNbtEdit(itemStack,pathNodes);
                }).size(ROW_WIDTH-ROW_LEFT_ICON-20-size-WID_SPACE,WID_HEIGHT).build(),
                Button.builder(Component.nullToEmpty(startEl==null ? "+" : "X"), btn -> {
                    if (startEl==null) {
                        createBlankTabNbtEdit(itemStack,pathNodes);
                    }
                    else if (isComponent) {
                        if (!key.startsWith("!"))
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                                itemStack,path,null),"components.!"+key,new CompoundTag())));
                        else
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,null)));
                    }
                    unsel();
                }).size(20,WID_HEIGHT).build()};
                this.btnX = new int[]{(ROW_LEFT_SCROLL+ROW_LEFT_ICON),(ROW_LEFT_SCROLL+ROW_LEFT_ICON)+size+5,ROW_RIGHT_SCROLL-20};
                if (startEl!=null) {
                    this.btns[1].setTooltip(Tooltip.create(grayWhiteText("Edit component:").append(getElementTooltipInfo(startEl,pathFlag))));
                    this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                }
                else
                    this.btns[1].active = false;

                this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty(startEl==null ? "Create component" : "Delete")));
                if (!BlackMagick.isCreative(minecraft) && startEl!=null) {
                    this.btns[2].active = false;
                    this.btns[2].setTooltip(Tooltip.create(ERROR_CREATIVE));
                }
            }
            else if ((startEl != null && pi.hasPathType(PathType.BOOLEAN) && startEl.getId()==Tag.TAG_BYTE && (((ByteTag)startEl).asByte().get()==0 || ((ByteTag)startEl).asByte().get()==1))
            || (startEl == null && pi.getDefaultPathType()==PathType.BOOLEAN)) {

                final int startVal;
                if (startEl != null) {
                    String tempVal = BlackMagick.nbtToSnbt(startEl);
                    if (tempVal.equals("0b") || tempVal.equals("0") || tempVal.equals("false"))
                        startVal = 1;
                    else
                        startVal = 2;
                }
                else
                    startVal = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("Unset"), btn -> {
                    String comp = path.replaceFirst("components\\.","");
                    if (!comp.startsWith("!"))
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            itemStack,path,null),"components.!"+comp,new CompoundTag())));
                    else
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,null)));
                    unsel();
                }).size(btnSize,WID_HEIGHT).build(),
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,ByteTag.ZERO)));
                    unsel();
                }).size(btnSize,WID_HEIGHT).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,ByteTag.ONE)));
                    unsel();
                }).size(btnSize,WID_HEIGHT).build()};
                this.btnX = new int[]{(ROW_LEFT_SCROLL+ROW_LEFT_ICON),ROW_RIGHT_SCROLL-(3*btnSize+2*btnSpacing),ROW_RIGHT_SCROLL-(2*btnSize+btnSpacing),ROW_RIGHT_SCROLL-btnSize};

                this.btns[startVal+1].active = false;

                for (int i=0; i<btns.length; i++) {
                    if (i>0 && this.btns[i].active) {
                        if (!BlackMagick.isCreative(minecraft))
                            this.btns[i].setTooltip(Tooltip.create(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                }
            }
            else { // inline component
                boolean isString = false; // to_do isString
                // boolean isString = ((startEl != null && pi.hasPathType(PathType.STRING) && startEl.getId()==Tag.TAG_STRING)
                //     || (startEl == null && pi.getDefaultPathType()==PathType.STRING));
                final String[] startVals = getStartVals(startEl, isString, pi.getFlag());
                String[] baseSuggestions = isString ? pi.getSuggs().getArray() : pi.getSuggs().getArraySnbt();

                int deleteBtnSize = 20;
                if (startEl == null)
                    deleteBtnSize = 0;

                addPosWidget(keyBtn, ROW_LEFT_SCROLL+ROW_LEFT_ICON, 0);
                {
                    SmartEditBox w = new SmartEditBox(minecraft.font,0,0,
                        ROW_WIDTH-ROW_LEFT_ICON-size-5-20-deleteBtnSize, WID_HEIGHT, Component.nullToEmpty(""));
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setSuggsResponder(value -> {
                        if (testPosWidget(2)) {
                            setErrorMsg(null);
                            ((Button)getPosWidget(2)).active = false;
                            ((Button)getPosWidget(2)).setTooltip(null);
                            ((Button)getPosWidget(2)).setTooltip(startEl == null ? null : Tooltip.create(Component.nullToEmpty("Component already set")));

                            String keyType = "component";

                            boolean unsaved = !value.equals(startVals[startVals.length-1]);
                            boolean itemStoneShortcut = false;
                            if (path.equals("id") && value.equals("")) {
                                itemStoneShortcut = true;
                                value = isString ? "minecraft:stone" : "\"minecraft:stone\"";
                            }
                            Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromSnbt(value);

                            if (unsaved || itemStoneShortcut) {

                                if (path.startsWith("components.")) {
                                    if (el != null)
                                        setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToSnbt(BlackMagick.setNbtPath(itemStack,path,el)),inpError));
                                    else {
                                        try {
                                            ItemArgument.item(BlackMagick.getCommandRegistries()).parse(
                                                new StringReader("stone["+path.replaceFirst("components\\.","")+"="+value+"]"));
                                        }
                                        catch (Exception ex) {
                                            if (ex instanceof CommandSyntaxException ex2) {
                                                setErrorMsg(ex2.getMessage());
                                                if (inpError.contains(" at position ")) {
                                                    setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                                }
                                            }
                                        }
                                    }
                                }
                                else if (path.equals("id")) {
                                    keyType = "id";
                                    String idValue = null;
                                    if (el != null && el.getId()==Tag.TAG_STRING) {
                                        idValue = BlackMagick.nbtToSnbtOrString(el);
                                    }

                                    if (idValue == null) {
                                        setErrorMsg("Invalid string");
                                    }
                                    else {
                                        try {
                                            ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(idValue));
                                        }
                                        catch (Exception ex) {
                                            if (ex instanceof CommandSyntaxException ex2) {
                                                setErrorMsg(ex2.getMessage());
                                                if (inpError.contains(" at position ")) {
                                                    setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                                }
                                            }
                                        }
                                        try {
                                            Identifier.parse(idValue);
                                        }
                                        catch (Exception ex) {
                                            setErrorMsg(ex.getMessage());
                                        }
                                    }
                                }
                                else if (path.equals("count")) {
                                    keyType = "count";
                                    try {
                                        IntegerArgumentType.integer(1,selItem.getMaxStackSize()).parse(new StringReader(value));//to_do get from itemStack param
                                    }
                                    catch (Exception ex) {
                                        if (ex instanceof CommandSyntaxException ex2) {
                                            setErrorMsg(ex2.getMessage());
                                            if (inpError.contains(" at position ")) {
                                                setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                            }
                                        }
                                    }
                                    try {
                                        Integer.parseInt(value);
                                    }
                                    catch (Exception ex) {
                                        setErrorMsg("Expected integer");
                                    }
                                }

                                if (inpError != null) {
                                    w.setTextColor(ERROR_COLOR);
                                    ((Button)getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid component")));
                                }
                                else {
                                    Component tempLbl = grayWhiteText("Set "+keyType+" to:").append(getElementTooltipInfo(el,pathFlag));
                                    if (BlackMagick.isCreative(minecraft)) {
                                        ((Button)getPosWidget(2)).setTooltip(Tooltip.create(tempLbl));
                                        ((Button)getPosWidget(2)).active = true;
                                    }
                                    else
                                        ((Button)getPosWidget(2)).setTooltip(Tooltip.create(
                                            Component.empty().append(ERROR_CREATIVE).append("\n\n").append(tempLbl)));

                                    if (
                                        (startEl != null && BlackMagick.elementsEqual(startEl,
                                            BlackMagick.getNbtPath(BlackMagick.itemToNbt(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,el))),path)
                                        ))
                                        || (
                                            startEl == null && path.equals("id") && el != null
                                            && (BlackMagick.nbtToSnbt(el).equals("\"air\"") || BlackMagick.nbtToSnbt(el).equals("\"minecraft:air\""))
                                        )
                                    ) {
                                        unsaved = false;
                                        ((Button)getPosWidget(2)).setTooltip(Tooltip.create(Component.nullToEmpty("Component already set")));
                                        ((Button)getPosWidget(2)).active = false;
                                    }
                                }
                            }

                            if (unsaved) {
                                if (inpError==null)
                                    w.setTextColor(TEXT_COLOR);
                                ItemBuilderScreen.this.markUnsaved(w);
                            }
                            else {
                                w.setTextColor(LABEL_COLOR);
                                ItemBuilderScreen.this.markSaved(w);
                            }

                            suggsOnChanged(w,baseSuggestions,startVals);
                        }
                    });
                    if (startEl != null) {
                        w.setSmartTooltip(Tooltip.create(grayWhiteText("Edit component:").append(getElementTooltipInfo(startEl,pathFlag))));
                        w.setTooltipDelay(TOOLTIP_DELAY);
                    }
                    addPosWidget(w, ROW_LEFT_SCROLL+ROW_LEFT_ICON+5+size, 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                        if (testPosWidget(1)) {
                            String inp = ((SmartEditBox)getPosWidget(1)).getValue();
                            if (path.equals("id")) {
                                if (inp.equals("")) {
                                    BlackMagick.setItemMain(new ItemStack(Items.STONE));
                                }
                                else {
                                    Tag el = isString ? StringTag.valueOf(inp) : BlackMagick.nbtFromSnbt(inp);
                                    if (el != null && (BlackMagick.nbtToSnbt(el).equals("\"air\"") || BlackMagick.nbtToSnbt(el).equals("\"minecraft:air\""))) {
                                        BlackMagick.setItemMain(ItemStack.EMPTY);
                                    }
                                    else {
                                        if (el != null) {
                                            ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbtExclusive(selItem),path,el));//to_do get from itemStack param
                                            if (!newItem.isEmpty())
                                                BlackMagick.setItemMain(newItem);
                                        }
                                    }
                                }
                            }
                            else {
                                Tag el = isString ? StringTag.valueOf(inp) : BlackMagick.nbtFromSnbt(inp);
                                if (el != null) {
                                    ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,el));
                                    if (!newItem.isEmpty())
                                        BlackMagick.setItemMain(newItem);
                                }
                            }
                            btnTab(CACHE_TAB_MAIN); //careful removing, may be required for some components
                        }
                        unsel();
                    }).size(20,WID_HEIGHT).build();
                    addPosWidget(w, ROW_RIGHT_SCROLL-20-deleteBtnSize, 0);
                }
                if (startEl != null) {
                    Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                        if (startEl != null) {
                            if (isComponent && !key.startsWith("!"))
                                BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                                    itemStack,path,null),"components.!"+key,new CompoundTag())));
                            else
                                BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(itemStack,path,null)));
                        }
                        unsel();
                    }).size(20,WID_HEIGHT).build();
                    w.active = true;
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                    if (!BlackMagick.isCreative(minecraft)) {
                        w.active = false;
                        w.setTooltip(Tooltip.create(ERROR_CREATIVE));
                    }
                    addPosWidget(w, ROW_RIGHT_SCROLL-deleteBtnSize, 0);
                }
                ((SmartEditBox)getPosWidget(1)).setValue(startVals[startVals.length-1]);
            }

            initChildren();
        }

    }

    class RowWidgetElement extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.create(Component.nullToEmpty("Set element"));
        private String cacheNode = null;
        private PathInfo cachePathInfo = null;
        private boolean cacheIsString = false;
        private Tag cacheCurrentElement = null;
        private String[] cacheSuggs = null;

        private RowWidgetElement() {}

        public RowWidgetElement(String key, CompoundTag contextRoot, PathNode[] contextPath) {
            NbtEdit thisNbtEdit = nbtEdit.addPath(PathNode.of(key));
            String fullPath = thisNbtEdit.fullPath();
            PathInfo pi = nbtEdit.pi().getCompoundKeyInfo(key, contextRoot, contextPath);
            final PathFlag pathFlag = pi.getFlag();

            Tag currentEl = thisNbtEdit.getEditElement();
            final String currentVal = BlackMagick.nbtToSnbt(currentEl);

            String keyBtnTxt = key;
            int size = 70;
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {}).size(size,WID_HEIGHT).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(getKeyButtonTooltip(PathNode.of(key), pi)));

            if (currentEl == null) {
                throw new IllegalArgumentException("Tried to create RowWidgetElement for nonexistent key");
            }
            else if (currentEl.getId()==Tag.TAG_COMPOUND || currentEl.getId()==Tag.TAG_LIST) {
                this.btns = new Button[]{
                    keyBtn,
                    Button.builder(getComplexButtonText(currentEl), btn -> {
                        createBlankTabNbtEdit(nbtEdit.addPath(PathNode.of(key)));
                    }).size(ROW_WIDTH-20-size-WID_SPACE,WID_HEIGHT).build(),
                    Button.builder(Component.nullToEmpty("X"), btn -> {
                        nbtEditUpdate(fullPath, null);
                    }).size(20,WID_HEIGHT).build()
                };
                this.btnX = new int[]{ROW_LEFT_SCROLL,ROW_LEFT_SCROLL+size+5,ROW_RIGHT_SCROLL-20};
                this.btns[1].setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
            }
            else {
                int deleteBtnSize = 20;
                addPosWidget(keyBtn, ROW_LEFT_SCROLL, 0);
                String[] baseSuggestions = pi.getSuggs().getArraySnbt();
                {
                    SmartEditBox w = new SmartEditBox(minecraft.font,0,0,
                        ROW_WIDTH-size-WID_SPACE-20-deleteBtnSize, WID_HEIGHT, Component.nullToEmpty(""));
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setSuggsResponder(value -> {
                        if (testPosWidget(2)) {
                            setErrorMsg(null);
                            ((Button)getPosWidget(2)).active = false;
                            ((Button)getPosWidget(2)).setTooltip(null);
                            ((SmartEditBox)getPosWidget(1)).setTextColor(TEXT_COLOR);

                            String element = value;
                            Tag el = BlackMagick.nbtFromSnbt(element);
                            if (el == null) {
                                ((SmartEditBox)getPosWidget(1)).setTextColor(ERROR_COLOR);
                                setErrorMsg("Invalid element");
                                ((Button)getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else if (BlackMagick.elementsEqual(currentEl,el)) {
                                ((SmartEditBox)getPosWidget(1)).setTextColor(LABEL_COLOR);
                                ((Button)getPosWidget(2)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                            }
                            else {
                                ((Button)getPosWidget(2)).active = true;
                                ((Button)getPosWidget(2)).setTooltip(Tooltip.create(grayWhiteText(
                                    "Set key ",BlackMagick.validCompoundKey(key)," to:").append(getElementTooltipInfo(el,pathFlag))));

                            }

                            suggsOnChanged(w, baseSuggestions, currentVal);
                        }
                    });
                    w.setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                    w.setTooltipDelay(TOOLTIP_DELAY);
                    addPosWidget(w, ROW_LEFT_SCROLL+WID_SPACE+size, 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                        if (testPosWidget(1)) {
                            Tag el = BlackMagick.nbtFromSnbt(((SmartEditBox)getPosWidget(1)).getValue());
                            if (el != null) {
                                nbtEditUpdate(fullPath,el);
                            }
                        }
                        unsel();
                    }).size(20,WID_HEIGHT).build();
                    addPosWidget(w, ROW_RIGHT_SCROLL-20-deleteBtnSize, 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                        nbtEditUpdate(fullPath, null);
                    }).size(20,WID_HEIGHT).build();
                    w.active = true;
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                    addPosWidget(w, ROW_RIGHT_SCROLL-deleteBtnSize, 0);
                }
                ((SmartEditBox)getPosWidget(1)).setValue(currentVal);
            }

            initChildren();
        }

        public static RowWidgetElement compoundTemplateElement(ItemBuilderScreen context, String key, CompoundTag contextRoot, PathNode[] contextPath) {
            RowWidgetElement row = context.new RowWidgetElement();

            NbtEdit thisNbtEdit = context.nbtEdit.addPath(PathNode.of(key));
            String fullPath = thisNbtEdit.fullPath();
            PathInfo pi = context.nbtEdit.pi().getCompoundKeyInfo(key, contextRoot, contextPath);
            final PathFlag pathFlag = pi.getFlag();

            Tag currentEl = thisNbtEdit.getEditElement();

            String keyBtnTxt = row.cleanKeyBtn(key);
            int size = 70;
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {}).size(size,WID_HEIGHT).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(context.getKeyButtonTooltip(PathNode.of(key), pi)));

            if ((currentEl != null && pi.hasPathType(PathType.UNIT) && currentEl.getId()==Tag.TAG_COMPOUND && ((CompoundTag)currentEl).isEmpty())
            || (currentEl == null && pi.getDefaultPathType()==PathType.UNIT)) {

                final int startVal = currentEl == null ? 0 : 1;
                final int btnSize = 35;
                final int btnSpacing = 2;

                row.addPosWidget(new PosWidget(keyBtn, ROW_LEFT_SCROLL, 0));
                {
                    Button w = Button.builder(Component.nullToEmpty("False"), btn -> {
                        context.nbtEditUpdate(fullPath, null);
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    if (startVal == 0)
                        w.active = false;
                    else
                        w.setTooltip(TT_SET);
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-(2*btnSize+btnSpacing), 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("True"), btn -> {
                        context.nbtEditUpdate(fullPath, new CompoundTag());
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    if (startVal == 1)
                        w.active = false;
                    else
                        w.setTooltip(TT_SET);
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-btnSize, 0);
                }
            }
            else if ((currentEl != null && (currentEl.getId()==Tag.TAG_COMPOUND || currentEl.getId()==Tag.TAG_LIST))
            || (currentEl == null && pi.getDefaultPathType().isComplex())) {

                row.addPosWidget(new PosWidget(keyBtn, ROW_LEFT_SCROLL, 0));
                {
                    Button w = Button.builder(getComplexButtonText(currentEl, pi.getFlag()), btn -> {
                        context.createBlankTabNbtEdit(context.nbtEdit.addPath(PathNode.of(key)));
                    }).size(ROW_WIDTH-20-size-WID_SPACE,WID_HEIGHT).build();
                    if (currentEl!=null) {
                        w.setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                        w.setTooltipDelay(TOOLTIP_DELAY);
                    }
                    else
                        w.active = false;
                    row.addPosWidget(w, ROW_LEFT_SCROLL+size+5, 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty(currentEl==null ? "+" : "X"), btn -> {
                        if (currentEl==null) {
                            context.createBlankTabNbtEdit(context.nbtEdit.addPath(PathNode.of(key)));
                        }
                        else {
                            context.nbtEditUpdate(fullPath, null);
                        }
                        context.unsel();
                    }).size(20,WID_HEIGHT).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty(currentEl==null ? "Create element" : "Delete")));
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-20, 0);
                }
            }
            else if ((currentEl != null && pi.hasPathType(PathType.BOOLEAN) && currentEl.getId()==Tag.TAG_BYTE && (((ByteTag)currentEl).asByte().get()==0 || ((ByteTag)currentEl).asByte().get()==1))
            || (currentEl == null && pi.getDefaultPathType()==PathType.BOOLEAN)) {

                final int startVal;
                if (currentEl != null) {
                    String tempVal = BlackMagick.nbtToSnbt(currentEl);
                    if (tempVal.equals("0b") || tempVal.equals("0") || tempVal.equals("false"))
                        startVal = 1;
                    else
                        startVal = 2;
                }
                else
                    startVal = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;

                row.addPosWidget(new PosWidget(keyBtn, ROW_LEFT_SCROLL, 0));
                {
                    Button w = Button.builder(Component.nullToEmpty("Unset"), btn -> {
                        context.nbtEditUpdate(fullPath, null);
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    if (startVal == 0)
                        w.active = false;
                    else
                        w.setTooltip(TT_SET);
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-(3*btnSize+2*btnSpacing), 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("False"), btn -> {
                        context.nbtEditUpdate(fullPath, ByteTag.ZERO);
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    if (startVal == 1)
                        w.active = false;
                    else
                        w.setTooltip(TT_SET);
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-(2*btnSize+btnSpacing), 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("True"), btn -> {
                        context.nbtEditUpdate(fullPath, ByteTag.ONE);
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    if (startVal == 2)
                        w.active = false;
                    else
                        w.setTooltip(TT_SET);
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-btnSize, 0);
                }
            }
            else { // inline element

                boolean isString = false; // to_do isString
                // boolean isString = ((currentEl != null && pi.hasPathType(PathType.STRING) && currentEl.getId()==Tag.TAG_STRING)
                //     || (currentEl == null && pi.getDefaultPathType()==PathType.STRING));
                final String[] startVals = getStartVals(currentEl, isString, pi.getFlag());
                String[] baseSuggestions = isString ? pi.getSuggs().getArray() : pi.getSuggs().getArraySnbt();

                int deleteBtnSize = 20;
                if (currentEl == null)
                    deleteBtnSize = 0;

                row.addPosWidget(keyBtn, ROW_LEFT_SCROLL, 0);
                {
                    SmartEditBox w = new SmartEditBox(context.minecraft.font,0,0,
                        ROW_WIDTH-size-5-20-deleteBtnSize, WID_HEIGHT, Component.nullToEmpty(""));
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setSuggsResponder(value -> {
                        if (row.testPosWidget(2)) {
                            context.setErrorMsg(null);
                            ((Button)row.getPosWidget(2)).active = false;
                            ((Button)row.getPosWidget(2)).setTooltip(null);
                            ((SmartEditBox)row.getPosWidget(1)).setTextColor(TEXT_COLOR);

                            Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromSnbt(value);

                            if (currentEl == null && value.isEmpty() && !isString) {
                                ((SmartEditBox)row.getPosWidget(1)).setTextColor(LABEL_COLOR);
                            }
                            else if (el == null) {
                                ((SmartEditBox)row.getPosWidget(1)).setTextColor(ERROR_COLOR);
                                context.setErrorMsg("Invalid element");
                                ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else if (BlackMagick.elementsEqual(currentEl,el)) {
                                ((SmartEditBox)row.getPosWidget(1)).setTextColor(LABEL_COLOR);
                                ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                            }
                            else {
                                ((Button)row.getPosWidget(2)).active = true;
                                ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(grayWhiteText(
                                    "Set key ",BlackMagick.validCompoundKey(key)," to:").append(getElementTooltipInfo(el,pathFlag))));
                            }

                            context.suggsOnChanged(w, baseSuggestions, startVals);
                        }
                    });
                    if (currentEl != null) {
                        w.setSmartTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                        w.setTooltipDelay(TOOLTIP_DELAY);
                    }
                    row.addPosWidget(w, ROW_LEFT_SCROLL+5+size, 0);
                }
                {
                    Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                        if (row.testPosWidget(1)) {
                            String inp = ((SmartEditBox)row.getPosWidget(1)).getValue();
                            Tag el = isString ? StringTag.valueOf(inp) : BlackMagick.nbtFromSnbt(inp);
                            if (el != null) {
                                context.nbtEditUpdate(fullPath,el);
                            }
                        }
                        context.unsel();
                    }).size(20,WID_HEIGHT).build();
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-20-deleteBtnSize, 0);
                }
                if (currentEl != null) {
                    Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                        context.nbtEditUpdate(fullPath, null);
                        context.unsel();
                    }).size(20,WID_HEIGHT).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                    row.addPosWidget(w, ROW_RIGHT_SCROLL-deleteBtnSize, 0);
                }
                ((SmartEditBox)row.getPosWidget(1)).setValue(startVals[startVals.length-1]);
            }

            return row;
        }

        public static RowWidgetElement customCompoundKey(ItemBuilderScreen context, CompoundTag baseCompound, boolean isTemplate, CompoundTag contextRoot, PathNode[] contextPath) {
            RowWidgetElement row = context.new RowWidgetElement();

            int size = 70;

            {
                Set<String> newKeySet = Sets.newHashSet();
                newKeySet.addAll(context.nbtEdit.pi().getCompoundKeys(contextRoot, contextPath));
                if (baseCompound != null)
                    newKeySet.removeAll(baseCompound.keySet());
                String[] keysSuggs = newKeySet.toArray(new String[0]);

                SmartEditBox w = new SmartEditBox(context.minecraft.font,0,0, size, WID_HEIGHT, Component.nullToEmpty(""));
                w.setMaxLength(MAX_TEXT_LENGTH);
                w.setSuggsResponder(value -> {
                    if (row.testPosWidget(2)) {
                        context.setErrorMsg(null);
                        ((Button)row.getPosWidget(2)).active = false;
                        ((Button)row.getPosWidget(2)).setTooltip(null);

                        String key = value;
                        if (row.cacheNode == null || !row.cacheNode.equals(key)) {
                            row.cacheNode = key;
                            row.cachePathInfo = context.nbtEdit.pi().getCompoundKeyInfo(key, contextRoot, contextPath);
                            row.cacheIsString = isTemplate && row.cachePathInfo.getDefaultPathType()==PathType.STRING;
                            row.cacheCurrentElement = baseCompound != null ? baseCompound.get(key) : null;
                            row.cacheSuggs = row.cacheIsString ? row.cachePathInfo.getSuggs().getArray() : row.cachePathInfo.getSuggs().getArraySnbt();
                        }
                        String element = ((SmartEditBox)row.getPosWidget(1)).getValue();
                        if (!key.isEmpty() || !element.isEmpty()) {
                            if (key.isEmpty()) {
                                context.setErrorMsg("Invalid key");
                                ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid key")));
                            }
                            else {
                                Tag el = row.cacheIsString ? StringTag.valueOf(element) : BlackMagick.nbtFromSnbt(element);
                                if (el == null) {
                                    context.setErrorMsg("Invalid element");
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid element")));
                                }
                                else if (BlackMagick.elementsEqual(el,row.cacheCurrentElement)) {
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                                }
                                else {
                                    ((Button)row.getPosWidget(2)).active = true;
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(grayWhiteText(
                                        row.cacheCurrentElement != null ? "Modify key " : "Set key ",BlackMagick.validCompoundKey(key)," to:").append(getElementTooltipInfo(el,row.cachePathInfo.getFlag()))));
                                }
                            }
                        }

                        context.suggsOnChanged(w, keysSuggs);
                    }
                });
                row.addPosWidget(w, ROW_LEFT_SCROLL, 0);
            }
            {
                SmartEditBox w = new SmartEditBox(context.minecraft.font,0,0,
                    ROW_WIDTH-size-WID_SPACE-20, WID_HEIGHT, Component.nullToEmpty(""));
                w.setMaxLength(MAX_TEXT_LENGTH);
                w.setSuggsResponder(value -> {
                    if (row.testPosWidget(2)) {
                        context.setErrorMsg(null);
                        ((Button)row.getPosWidget(2)).active = false;
                        ((Button)row.getPosWidget(2)).setTooltip(null);
                        ((SmartEditBox)row.getPosWidget(1)).setTextColor(LABEL_COLOR);

                        String key = ((SmartEditBox)row.getPosWidget(0)).getValue();
                        String element = value;
                        if (!key.isEmpty() || !element.isEmpty()) {
                            Tag el = row.cacheIsString ? StringTag.valueOf(element) : BlackMagick.nbtFromSnbt(element);
                            if (el == null) {
                                ((SmartEditBox)row.getPosWidget(1)).setTextColor(ERROR_COLOR);
                                context.setErrorMsg("Invalid element");
                                ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else {
                                ((SmartEditBox)row.getPosWidget(1)).setTextColor(TEXT_COLOR);
                                if (key.isEmpty()) {
                                    context.setErrorMsg("Invalid key");
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(errorText("Invalid key")));
                                }
                                else if (BlackMagick.elementsEqual(el,row.cacheCurrentElement)) {
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                                }
                                else {
                                    ((Button)row.getPosWidget(2)).active = true;
                                    ((Button)row.getPosWidget(2)).setTooltip(Tooltip.create(grayWhiteText(
                                        row.cacheCurrentElement != null ? "Modify key " : "Set key ",BlackMagick.validCompoundKey(key)," to:").append(getElementTooltipInfo(el,row.cachePathInfo.getFlag()))));
                                }
                            }
                        }

                        context.suggsOnChanged(w, row.cacheSuggs);
                    }
                });
                row.addPosWidget(w, ROW_LEFT_SCROLL+WID_SPACE+size, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                    if (row.testPosWidget(1)) {
                        String key = ((SmartEditBox)row.getPosWidget(0)).getValue();
                        String element = ((SmartEditBox)row.getPosWidget(1)).getValue();
                        Tag el = row.cacheIsString ? StringTag.valueOf(element) : BlackMagick.nbtFromSnbt(element);

                        if (el!=null && !key.isEmpty()) {
                            NbtEdit thisNbtEdit = context.nbtEdit.addPath(PathNode.of(key));
                            String fullPath = thisNbtEdit.fullPath();
                            context.nbtEditUpdate(fullPath,el);
                        }
                    }
                    context.unsel();
                }).size(20,WID_HEIGHT).build();
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20, 0);
            }
            ((SmartEditBox)row.getPosWidget(1)).setValue("");

            return row;
        }

        public static RowWidgetElement customListElement(ItemBuilderScreen context, ListTag baseList, boolean isTemplate, CompoundTag contextRoot, PathNode[] contextPath) {
            RowWidgetElement row = context.new RowWidgetElement();

            PathInfo pi = context.nbtEdit.pi().getListIndexInfo(baseList.size(), contextRoot, contextPath);
            boolean isString = false; // to_do isString
            // boolean isString = isTemplate && pi.getDefaultPathType()==PathType.STRING;
            String[] baseSuggestions = isString ? pi.getSuggs().getArray() : pi.getSuggs().getArraySnbt();
            final PathFlag pathFlag = pi.getFlag();

            {
                SmartEditBox w = new SmartEditBox(context.minecraft.font,0,0,
                    ROW_WIDTH-20, WID_HEIGHT, Component.nullToEmpty(""));
                w.setMaxLength(MAX_TEXT_LENGTH);
                w.setSuggsResponder(value -> {
                    if (row.testPosWidget(1)) {
                        context.setErrorMsg(null);
                        ((Button)row.getPosWidget(1)).active = false;
                        ((Button)row.getPosWidget(1)).setTooltip(null);
                        ((SmartEditBox)row.getPosWidget(0)).setTextColor(LABEL_COLOR);

                        if (!value.isEmpty() || isString) {
                            Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromSnbt(value);
                            if (el == null) {
                                ((SmartEditBox)row.getPosWidget(0)).setTextColor(ERROR_COLOR);
                                context.setErrorMsg("Invalid element");
                                ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else {
                                ((SmartEditBox)row.getPosWidget(0)).setTextColor(TEXT_COLOR);
                                ((Button)row.getPosWidget(1)).active = true;
                                ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(grayWhiteText(
                                    "Append value:").append(getElementTooltipInfo(el,pathFlag))));
                            }
                        }

                        context.suggsOnChanged(w, baseSuggestions);
                    }
                });
                row.addPosWidget(w, ROW_LEFT_SCROLL, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                    if (row.testPosWidget(0)) {
                        String inp = ((SmartEditBox)row.getPosWidget(0)).getValue();
                        Tag el = isString ? StringTag.valueOf(inp) : BlackMagick.nbtFromSnbt(inp);
                        if (el!=null) {
                            context.nbtEditUpdate(BlackMagick.appendListElement(context.nbtEdit.current(),context.nbtEdit.fullPath(),el));
                        }
                    }
                    context.unsel();
                }).size(20,WID_HEIGHT).build();
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20, 0);
            }
            ((SmartEditBox)row.getPosWidget(0)).setValue("");

            return row;
        }

        public static RowWidgetElement listTemplateElement(ItemBuilderScreen context, ListTag baseList, int index, CompoundTag contextRoot, PathNode[] contextPath) {
            if (index<0 || index>=baseList.size())
                throw new IllegalArgumentException("Tried to create RowWidgetElement with index or listSize out of bounds");

            RowWidgetElement row = context.new RowWidgetElement();

            NbtEdit thisNbtEdit = context.nbtEdit.addPath(PathNode.of(index));
            String fullPath = thisNbtEdit.fullPath();
            PathInfo pi = context.nbtEdit.pi().getListIndexInfo(index, contextRoot, contextPath);
            final PathFlag pathFlag = pi.getFlag();

            Tag currentEl = thisNbtEdit.getEditElement();

            int keyBtnSize = 30;
            int listBtnSize = 15;
            int delBtnSize = 20;
            String keyBtnTxt = row.indexKeyBtn(index);
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {})
                .size(keyBtnSize,WID_HEIGHT).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(context.getKeyButtonTooltip(PathNode.of(index), pi)));

            int listElWidth = ROW_WIDTH-keyBtnSize-5-listBtnSize-listBtnSize-delBtnSize;

            row.addPosWidget(keyBtn, ROW_LEFT_SCROLL, 0);
            {
                if (currentEl == null) {
                    throw new IllegalArgumentException("Tried to create RowWidgetElement for nonexistent index");
                }
                else if (pi.hasPathType(PathType.UNIT) && currentEl.getId()==Tag.TAG_COMPOUND && ((CompoundTag)currentEl).isEmpty()) {
                    final int btnSize = 35;

                    Button w = Button.builder(Component.nullToEmpty("True"), btn -> {
                        context.nbtEditUpdate(fullPath, new CompoundTag());
                        context.unsel();
                    }).size(btnSize,WID_HEIGHT).build();
                    w.active = false;
                    row.addPosWidget(w, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE+listElWidth-btnSize, 0);
                }
                else if (currentEl.getId()==Tag.TAG_COMPOUND || currentEl.getId()==Tag.TAG_LIST) {
                    Button w = Button.builder(getComplexButtonText(currentEl, pi.getFlag()), btn -> {
                        context.createBlankTabNbtEdit(context.nbtEdit.addPath(PathNode.of(index)));
                    }).size(listElWidth,WID_HEIGHT).build();
                    w.setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                    w.setTooltipDelay(TOOLTIP_DELAY);
                    row.addPosWidget(w, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE, 0);
                }
                else if (pi.hasPathType(PathType.BOOLEAN) && currentEl.getId()==Tag.TAG_BYTE && (((ByteTag)currentEl).asByte().get()==0 || ((ByteTag)currentEl).asByte().get()==1)) {
                    final int startVal;
                    String tempVal = BlackMagick.nbtToSnbt(currentEl);
                    if (tempVal.equals("0b") || tempVal.equals("0") || tempVal.equals("false"))
                        startVal = 1;
                    else
                        startVal = 2;
                    final int btnSize = 35;
                    final int btnSpacing = 2;
                    {
                        Button w = Button.builder(Component.nullToEmpty("False"), btn -> {
                            context.nbtEditUpdate(fullPath, ByteTag.ZERO);
                            context.unsel();
                        }).size(btnSize,WID_HEIGHT).build();
                        if (startVal == 1)
                            w.active = false;
                        else
                            w.setTooltip(TT_SET);
                        row.addPosWidget(w, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE+listElWidth-btnSize-btnSize-btnSpacing, 0);
                    }
                    {
                        Button w = Button.builder(Component.nullToEmpty("True"), btn -> {
                            context.nbtEditUpdate(fullPath, ByteTag.ONE);
                            context.unsel();
                        }).size(btnSize,WID_HEIGHT).build();
                        if (startVal == 2)
                            w.active = false;
                        else
                            w.setTooltip(TT_SET);
                        row.addPosWidget(w, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE+listElWidth-btnSize, 0);
                    }
                }
                else { // inline element

                    boolean isString = false; // to_do isString
                    // boolean isString = ((currentEl != null && pi.hasPathType(PathType.STRING) && currentEl.getId()==Tag.TAG_STRING)
                    //     || (currentEl == null && pi.getDefaultPathType()==PathType.STRING));
                    final String[] startVals = getStartVals(currentEl, isString, pi.getFlag());
                    String[] baseSuggestions = isString ? pi.getSuggs().getArray() : pi.getSuggs().getArraySnbt();

                    int addBtnSize = 20;
                    final int btnIndex = row.getPosWidgetSize()+1;

                    SmartEditBox txtElement = new SmartEditBox(context.minecraft.font,0,0,listElWidth-addBtnSize,WID_HEIGHT,Component.nullToEmpty(""));
                    txtElement.setMaxLength(MAX_TEXT_LENGTH);
                    txtElement.setSuggsResponder(value -> {
                        if (row.testPosWidget(btnIndex)) {
                            context.setErrorMsg(null);
                            ((Button)row.getPosWidget(btnIndex)).active = false;
                            ((Button)row.getPosWidget(btnIndex)).setTooltip(null);
                            txtElement.setTextColor(TEXT_COLOR);

                            Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromSnbt(value);

                            if (el == null) {
                                txtElement.setTextColor(ERROR_COLOR);
                                context.setErrorMsg("Invalid element");
                                ((Button)row.getPosWidget(btnIndex)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else if (BlackMagick.elementsEqual(currentEl,el)) {
                                txtElement.setTextColor(LABEL_COLOR);
                                ((Button)row.getPosWidget(btnIndex)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                            }
                            else {
                                ((Button)row.getPosWidget(btnIndex)).active = true;
                                ((Button)row.getPosWidget(btnIndex)).setTooltip(Tooltip.create(grayWhiteText(
                                    "Set index ",""+index," to:").append(getElementTooltipInfo(el,pathFlag))));
                            }

                            context.suggsOnChanged(txtElement, baseSuggestions, startVals);
                        }
                    });
                    txtElement.setSmartTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                    txtElement.setTooltipDelay(TOOLTIP_DELAY);
                    row.addPosWidget(txtElement, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE, 0);

                    Button btnSet = Button.builder(Component.nullToEmpty("+"), btn -> {
                        String inp = txtElement.getValue();
                        Tag el = isString ? StringTag.valueOf(inp) : BlackMagick.nbtFromSnbt(inp);
                        if (el != null) {
                            context.nbtEditUpdate(fullPath,el);
                        }
                        context.unsel();
                    }).size(20,WID_HEIGHT).build();
                    row.addPosWidget(btnSet, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize-delBtnSize-addBtnSize, 0);

                    txtElement.setValue(startVals[startVals.length-1]);
                }
            }
            {
                Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                    context.nbtEditUpdate(fullPath, null);
                }).size(delBtnSize,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                row.addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize-delBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty("*"), btn -> {
                    context.nbtEditUpdate(BlackMagick.cloneListElement(context.nbtEdit.current(),context.nbtEdit.fullPath(),index));
                }).size(listBtnSize,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Clone")));
                row.addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty(UNICODE_UP_ARROW), btn -> {
                    context.nbtEditUpdate(BlackMagick.moveListElement(context.nbtEdit.current(),context.nbtEdit.fullPath(),index,true));
                }).size(listBtnSize,10).build();
                if (index>0)
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Move Up")));
                else
                    w.active = false;
                row.addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty(UNICODE_DOWN_ARROW), btn -> {
                    context.nbtEditUpdate(BlackMagick.moveListElement(context.nbtEdit.current(),context.nbtEdit.fullPath(),index,false));
                }).size(listBtnSize,10).build();
                if (index<baseList.size()-1)
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Move Down")));
                else
                    w.active = false;
                row.addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize, 10);
            }

            return row;
        }

        public static RowWidgetElement fallbackElement(ItemBuilderScreen context) {
            RowWidgetElement row = context.new RowWidgetElement();

            Tag currentEl = context.nbtEdit.getEditElement();
            final String currentVal = BlackMagick.nbtToSnbt(currentEl);

            PathInfo pi = context.nbtEdit.pi();
            String[] baseSuggestions = pi.getSuggs().getArraySnbt();
            final PathFlag pathFlag = pi.getFlag();

            SmartEditBox elementTxt = new SmartEditBox(context.minecraft.font,0,0,
                ROW_WIDTH-20-(currentEl==null ? 0 : 20), WID_HEIGHT, Component.nullToEmpty(""));
            elementTxt.setMaxLength(MAX_TEXT_LENGTH);
            elementTxt.setSuggsResponder(value -> {
                if (row.testPosWidget(1)) {
                    context.setErrorMsg(null);
                    ((Button)row.getPosWidget(1)).active = false;
                    ((Button)row.getPosWidget(1)).setTooltip(null);
                    ((SmartEditBox)row.getPosWidget(0)).setTextColor(TEXT_COLOR);

                    if (!value.isEmpty()) {
                        Tag el = BlackMagick.nbtFromSnbt(value);
                        if (el == null) {
                            ((SmartEditBox)row.getPosWidget(0)).setTextColor(ERROR_COLOR);
                            context.setErrorMsg("Invalid element");
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(errorText("Invalid element")));
                        }
                        else if (BlackMagick.elementsEqual(currentEl,el)) {
                            ((SmartEditBox)row.getPosWidget(0)).setTextColor(LABEL_COLOR);
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                        }
                        else {
                            ((Button)row.getPosWidget(1)).active = true;
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(grayWhiteText("Set value:").append(getElementTooltipInfo(el,pathFlag))));
                        }
                    }

                    context.suggsOnChanged(elementTxt, baseSuggestions, currentVal);
                }
            });
            row.addPosWidget(elementTxt, ROW_LEFT_SCROLL, 0);
            {
                Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                    if (row.testPosWidget(0)) {
                        Tag el = BlackMagick.nbtFromSnbt(((SmartEditBox)row.getPosWidget(0)).getValue());

                        if (el!=null) {
                            context.nbtEditUpdate(context.nbtEdit.fullPath(),el);
                        }
                    }
                    context.unsel();
                }).size(20,WID_HEIGHT).build();
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20-(currentEl==null ? 0 : 20), 0);
            }
            if (currentEl!=null) {
                Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                    context.nbtEditUpdate(context.nbtEdit.fullPath(), null);
                    context.createBlankTabNbtEdit(context.nbtEdit.backPath());
                }).size(20,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20, 0);
            }
            elementTxt.setValue(currentVal);

            return row;
        }

        public static RowWidgetElement fallbackTemplateElement(ItemBuilderScreen context) {
            RowWidgetElement row = context.new RowWidgetElement();

            Tag currentEl = context.nbtEdit.getEditElement();
            PathInfo pi = context.nbtEdit.pi();

            boolean isString = false; // to_do isString
            // boolean isString = ((currentEl != null && pi.hasPathType(PathType.STRING) && currentEl.getId()==Tag.TAG_STRING)
            //     || (currentEl == null && pi.getDefaultPathType()==PathType.STRING));
            final String[] startVals = getStartVals(currentEl, isString, pi.getFlag());
            String[] baseSuggestions = isString ? pi.getSuggs().getArray() : pi.getSuggs().getArraySnbt();

            final PathFlag pathFlag = pi.getFlag();

            SmartEditBox elementTxt = new SmartEditBox(context.minecraft.font,0,0,
                ROW_WIDTH-20-(currentEl==null ? 0 : 20), WID_HEIGHT, Component.nullToEmpty(""));
            elementTxt.setMaxLength(MAX_TEXT_LENGTH);
            elementTxt.setSuggsResponder(value -> {
                if (row.testPosWidget(1)) {
                    context.setErrorMsg(null);
                    ((Button)row.getPosWidget(1)).active = false;
                    ((Button)row.getPosWidget(1)).setTooltip(null);
                    ((SmartEditBox)row.getPosWidget(0)).setTextColor(TEXT_COLOR);

                    if (!value.isEmpty()) {
                        Tag el = BlackMagick.nbtFromSnbt(value);
                        if (el == null) {
                            ((SmartEditBox)row.getPosWidget(0)).setTextColor(ERROR_COLOR);
                            context.setErrorMsg("Invalid element");
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(errorText("Invalid element")));
                        }
                        else if (BlackMagick.elementsEqual(currentEl,el)) {
                            ((SmartEditBox)row.getPosWidget(0)).setTextColor(LABEL_COLOR);
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                        }
                        else {
                            ((Button)row.getPosWidget(1)).active = true;
                            ((Button)row.getPosWidget(1)).setTooltip(Tooltip.create(grayWhiteText("Set value:").append(getElementTooltipInfo(el,pathFlag))));
                        }
                    }

                    context.suggsOnChanged(elementTxt, baseSuggestions, startVals);
                }
            });
            row.addPosWidget(elementTxt, ROW_LEFT_SCROLL, 0);
            {
                Button w = Button.builder(Component.nullToEmpty("+"), btn -> {
                    if (row.testPosWidget(0)) {
                        Tag el = BlackMagick.nbtFromSnbt(((SmartEditBox)row.getPosWidget(0)).getValue());

                        if (el!=null) {
                            context.nbtEditUpdate(context.nbtEdit.fullPath(),el);
                        }
                    }
                    context.unsel();
                }).size(20,WID_HEIGHT).build();
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20-(currentEl==null ? 0 : 20), 0);
            }
            if (currentEl!=null) {
                Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                    context.nbtEditUpdate(context.nbtEdit.fullPath(), null);
                    context.createBlankTabNbtEdit(context.nbtEdit.backPath());
                }).size(20,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                row.addPosWidget(w, ROW_RIGHT_SCROLL-20, 0);
            }
            elementTxt.setValue(startVals[startVals.length-1]);

            return row;
        }

        public RowWidgetElement(ListTag baseList, int index, CompoundTag contextRoot, PathNode[] contextPath) {
            if (index<0 || index>=baseList.size())
                throw new IllegalArgumentException("Tried to create RowWidgetElement with index or listSize out of bounds");

            NbtEdit thisNbtEdit = nbtEdit.addPath(PathNode.of(index));
            String fullPath = thisNbtEdit.fullPath();
            PathInfo pi = nbtEdit.pi().getListIndexInfo(index, contextRoot, contextPath);
            final PathFlag pathFlag = pi.getFlag();

            Tag currentEl = thisNbtEdit.getEditElement();
            final String currentVal = BlackMagick.nbtToSnbt(currentEl);

            int keyBtnSize = 30;
            int listBtnSize = 15;
            int delBtnSize = 20;
            String keyBtnTxt = indexKeyBtn(index);
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {})
                .size(keyBtnSize,WID_HEIGHT).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(getKeyButtonTooltip(PathNode.of(index), pi)));

            int listElWidth = ROW_WIDTH-keyBtnSize-5-listBtnSize-listBtnSize-delBtnSize;

            addPosWidget(keyBtn, ROW_LEFT_SCROLL, 0);
            {
                if (currentEl == null) {
                    throw new IllegalArgumentException("Tried to create RowWidgetElement for nonexistent index");
                }
                else if (currentEl.getId()==Tag.TAG_COMPOUND || currentEl.getId()==Tag.TAG_LIST) {
                    Button w = Button.builder(getComplexButtonText(currentEl), btn -> {
                        createBlankTabNbtEdit(nbtEdit.addPath(PathNode.of(index)));
                    }).size(listElWidth,WID_HEIGHT).build();
                    w.setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                    w.setTooltipDelay(TOOLTIP_DELAY);
                    addPosWidget(w, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE, 0);
                }
                else {
                    String[] baseSuggestions = pi.getSuggs().getArraySnbt();

                    int addBtnSize = 20;
                    final int btnIndex = getPosWidgetSize()+1;
                    SmartEditBox txtElement = new SmartEditBox(minecraft.font,0,0,listElWidth-addBtnSize,WID_HEIGHT,Component.nullToEmpty(""));
                    txtElement.setMaxLength(MAX_TEXT_LENGTH);
                    txtElement.setSuggsResponder(value -> {
                        if (testPosWidget(btnIndex)) {
                            setErrorMsg(null);
                            ((Button)getPosWidget(btnIndex)).active = false;
                            ((Button)getPosWidget(btnIndex)).setTooltip(null);
                            txtElement.setTextColor(TEXT_COLOR);

                            String element = value;
                            Tag el = BlackMagick.nbtFromSnbt(element);
                            if (el == null) {
                                txtElement.setTextColor(ERROR_COLOR);
                                setErrorMsg("Invalid element");
                                ((Button)getPosWidget(btnIndex)).setTooltip(Tooltip.create(errorText("Invalid element")));
                            }
                            else if (BlackMagick.elementsEqual(currentEl,el)) {
                                txtElement.setTextColor(LABEL_COLOR);
                                ((Button)getPosWidget(btnIndex)).setTooltip(Tooltip.create(Component.nullToEmpty("Element already set")));
                            }
                            else {
                                ((Button)getPosWidget(btnIndex)).active = true;
                                ((Button)getPosWidget(btnIndex)).setTooltip(Tooltip.create(grayWhiteText(
                                    "Set index ",""+index," to:").append(getElementTooltipInfo(el,pathFlag))));
                            }

                            suggsOnChanged(txtElement, baseSuggestions, currentVal);
                        }
                    });
                    txtElement.setTooltip(Tooltip.create(grayWhiteText("Edit element:").append(getElementTooltipInfo(currentEl,pathFlag))));
                    txtElement.setTooltipDelay(TOOLTIP_DELAY);
                    addPosWidget(txtElement, ROW_LEFT_SCROLL+keyBtnSize+WID_SPACE, 0);

                    Button btnSet = Button.builder(Component.nullToEmpty("+"), btn -> {
                        Tag el = BlackMagick.nbtFromSnbt(txtElement.getValue());
                        if (el != null) {
                            nbtEditUpdate(fullPath,el);
                        }
                        unsel();
                    }).size(20,WID_HEIGHT).build();
                    addPosWidget(btnSet, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize-delBtnSize-addBtnSize, 0);

                    txtElement.setValue(currentVal);
                }
            }
            {
                Button w = Button.builder(Component.nullToEmpty("X"), btn -> {
                    nbtEditUpdate(fullPath, null);
                }).size(delBtnSize,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));
                addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize-delBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty("*"), btn -> {
                    nbtEditUpdate(BlackMagick.cloneListElement(nbtEdit.current(),nbtEdit.fullPath(),index));
                }).size(listBtnSize,WID_HEIGHT).build();
                w.setTooltip(Tooltip.create(Component.nullToEmpty("Clone")));
                addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize-listBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty(UNICODE_UP_ARROW), btn -> {
                    nbtEditUpdate(BlackMagick.moveListElement(nbtEdit.current(),nbtEdit.fullPath(),index,true));
                }).size(listBtnSize,10).build();
                if (index>0)
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Move Up")));
                else
                    w.active = false;
                addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize, 0);
            }
            {
                Button w = Button.builder(Component.nullToEmpty(UNICODE_DOWN_ARROW), btn -> {
                    nbtEditUpdate(BlackMagick.moveListElement(nbtEdit.current(),nbtEdit.fullPath(),index,false));
                }).size(listBtnSize,10).build();
                if (index<baseList.size()-1)
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Move Down")));
                else
                    w.active = false;
                addPosWidget(w, ROW_RIGHT_SCROLL-listBtnSize, 10);
            }
        }

        /**
         * Used for elements with specialized editors.
         */
        // public RowWidgetElement(String blankElPath, ListTag path2, Button saveBtn, WidgetCacheType cacheType) { to_do custom page row
        //     String currentPath2;
        //     if (path2==null)
        //         currentPath2 = "";
        //     else
        //         currentPath2 = path2.get(0).asString().get();
        //     String fullPath = blankElPath+currentPath2;

        //     //PathInfo pi = ComponentHelper.getPathInfo(fullPath);
        //     boolean isString = false; // to_do isString
        //     // boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==Tag.TAG_STRING;

        //     Tag tempEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
        //     final String startVal = (isString && tempEl != null && tempEl.getId() == Tag.TAG_STRING) ? tempEl.asString().get() : BlackMagick.nbtToSnbt(tempEl);
        //     // tempEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
        //     final String currentVal = (isString && tempEl != null && tempEl.getId() == Tag.TAG_STRING) ? tempEl.asString().get() : BlackMagick.nbtToSnbt(tempEl);

        //     String[] baseSuggestions = pi.suggs() == null ? null : pi.suggs().getArray();

        //     this.txts = new SmartEditBox[]{new SmartEditBox(minecraft.font,0,0, ROW_WIDTH, WID_HEIGHT, Component.nullToEmpty(""))};
        //     this.txtX = new int[]{ROW_LEFT_SCROLL};
        //     this.txts[0].setMaxLength(MAX_TEXT_LENGTH);

        //     this.txts[0].setSuggsResponder(value -> {
        //         setErrorMsg(null);
        //         // Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromSnbt(value);

        //         // if (el != null || value.isEmpty())
        //         //     nbtEditUpdate(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
        //         //         BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
        //         //         path2==null ? null : fullPath);
        //         // else
        //         //     setErrorMsg("Invalid element");
        //         //
        //         // if ((value != null && !value.equals(startVal))) {
        //         //     this.txts[0].setTextColor(TEXT_COLOR);
        //         //     if (inpError == null)
        //         //         setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToSnbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl)),inpError));
        //         // }
        //         // else {
        //         //     this.txts[0].setTextColor(LABEL_COLOR);
        //         // }

        //         if (inpError != null)
        //             this.txts[0].setTextColor(ERROR_COLOR);

        //         suggsOnChanged(this.txts[0],baseSuggestions,startVal);
        //     });

        //     this.txts[0].setValue(currentVal);
        //     widgetCacheAdd(cacheType, this.txts[0]);
        //     initChildren();
        // }

    }

    class RowWidgetSavedItemsRow extends RowWidget {

        protected int savedRow;

        /**
         * Saved row (9 btns)
         */
        public RowWidgetSavedItemsRow(int row) {
            this.savedRow = row;
            this.btns = new Button[9];
            this.btnX = new int[9];
            int currentX = 10+30;
            for (int i=0; i<9; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                final ItemStack thisItemStart =
                    (viewBlackMarket ?
                        ((FortytwoEdit.webItems.size()>index) ?
                            BlackMagick.itemFromString(FortytwoEdit.webItems.get(index))
                            : ItemStack.EMPTY)
                        : ((savedItems.containsKey(index)) ?
                            BlackMagick.itemFromString(savedItems.get(index))
                            : ItemStack.EMPTY)
                    );
                this.btns[i] = new ItemSlotButton(ItemBuilderScreen.this, ItemSlotButton.SLOT_HEIGHT, thisItemStart, btn -> {
                    if (!viewBlackMarket && savedModeSet) {
                        String itemString = "";
                        ItemStack savedItem = minecraft.player.getMainHandItem().copy();
                        if (!savedItem.isEmpty()) {
                            itemString = BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(savedItem));
                        }

                        if (FortytwoEdit.testSavedItems(savedItems)) {
                            boolean inMap = savedItems.containsKey(index);
                            if ((!inMap && itemString.isEmpty()) || (inMap && savedItems.get(index).equals(itemString))) {
                                FortytwoEdit.showToast("No Change","Item already saved");
                            }
                            else {
                                if (itemString.isEmpty())
                                    savedItems.remove(index);
                                else
                                    savedItems.put(index,itemString);

                                if (!FortytwoEdit.setSavedItems(savedItems)) {
                                    FortytwoEdit.logError("Failed to save item. The saved items list could not be loaded properly after saving."
                                        + "\nTried to save: " + (itemString.isEmpty() ? "air" : itemString));
                                    FortytwoEdit.showToast("Error Saving Item","Item NBT could not be saved.");
                                }
                                refreshSaved();
                            }
                        }
                        else {
                            FortytwoEdit.showToast("Error Saving Item","Reopen the screen and try again.");
                            FortytwoEdit.logWarn("Failed to save item. The saved items file has changed since the screen has been open. File not changed."
                                +"\nTried to save: "+(itemString.isEmpty() ? "air" : itemString));
                        }
                    }
                    else if (BlackMagick.isCreative(minecraft)) {
                        ItemStack thisItem =
                            (viewBlackMarket ?
                                ((FortytwoEdit.webItems.size()>index) ?
                                    BlackMagick.itemFromString(FortytwoEdit.webItems.get(index))
                                    : ItemStack.EMPTY)
                                : ((savedItems.containsKey(index)) ?
                                    BlackMagick.itemFromString(savedItems.get(index))
                                    : ItemStack.EMPTY)
                            );
                        if (!thisItem.isEmpty())
                            BlackMagick.setItemMain(thisItem);
                    }
                    ItemBuilderScreen.this.unsel();
                });
                currentX += 20;
                this.btns[i].active = false;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);
            }
            initChildren();
        }

        public void updateSavedDisplay() {
            for (int i=0; i<9; i++) {
                ItemSlotButton w = (ItemSlotButton)this.btns[i];
                w.active = savedModeSet && !viewBlackMarket;
                w.setError(null);
                w.removeOverlay();
                w.showSlot(false);
                if ((!viewBlackMarket && savedItems.containsKey(savedRow*9+i)) || (viewBlackMarket && FortytwoEdit.webItems.size()>(savedRow*9+i))) {
                    SavedItem current = SavedItem.build(viewBlackMarket ? FortytwoEdit.webItems.get(savedRow*9+i) : savedItems.get(savedRow*9+i));
                    if (current.stack()==null) {
                        w.setTooltip(makeItemTooltip(current.storedString()));
                        w.setItem(FortytwoEdit.ITEM_ERROR.get());
                        w.setError(ItemSlotButton.ItemError.ERROR);
                    }
                    else {
                        w.setItem(current.stack());
                        w.showSlot(true);
                        if (current.nbtError()) {
                            w.setError(ItemSlotButton.ItemError.WARN);
                            Tag currentEl = BlackMagick.nbtFromSnbt(current.storedString());
                            if (currentEl != null && currentEl.getId()==Tag.TAG_COMPOUND) {
                                w.setTooltip(Tooltip.create(Component.empty().append(
                                    BlackMagick.textComponentFromSnbt("{text:\"Failed to load all item data\",color:\"red\"}").text()).append(
                                    Component.nullToEmpty("\n")).append(
                                    BlackMagick.getElementDifferences((CompoundTag)currentEl, BlackMagick.itemToNbtStorage(current.stack())))));
                            }
                            else {
                                w.setTooltip(makeItemTooltip(current.storedString()));
                            }
                        }
                        else
                            w.setTooltip(makeItemTooltip(current.stack()));

                        if (BlackMagick.isCreative(minecraft))
                            w.active = true;
                    }
                    if (savedModeSet && !viewBlackMarket)
                        w.setOverlay(DELETE_ITEM_OVERLAY,DELETE_ITEM_OVERLAY_SIZE);
                }
                else {
                    w.setTooltip(null);
                    w.setItem(null);
                    if (savedModeSet && !viewBlackMarket)
                        w.showSlot(true);
                }
            }
        }

    }

    class RowWidgetBannerRow extends RowWidget {

        /**
         * banner row (8 btns)
         */
        public RowWidgetBannerRow(String blankElPath, ListTag path2, Button saveBtn, boolean isDye, String[] vals, String currentVal, Tag cancelEl) {//to_do custom page row
            ItemStack[] stacks = new ItemStack[vals.length];
            boolean[] stackWarns = new boolean[vals.length];

            ItemStack[] patternItems = isDye ? null : new ItemStack[vals.length];

            // String currentPath2;
            // if (path2==null)
            //     currentPath2 = "";
            // else
            //     currentPath2 = path2.get(0).asString().get();
            // String pagePath = blankElPath+currentPath2;
            // String fullPath = pagePath+"."+(isDye ? "color" : "pattern");

            if (!isDye) {
                for (int i=0; i<vals.length; i++)
                    patternItems[i] = BlackMagick.itemFromString("{id:white_banner,components:{\"minecraft:banner_patterns\":[{color:red,pattern:\""+vals[i]+"\"}]}}");

                if (!bannerShield)
                    for (int i=0; i<vals.length; i++)
                        stacks[i] = patternItems[i];
                else
                    for (int i=0; i<vals.length; i++)
                        stacks[i] =
                            BlackMagick.itemFromString(
                                "{id:shield,components:{\"minecraft:base_color\":white,\"minecraft:banner_patterns\":[{color:red,pattern:\""+vals[i]+"\"}]}}");
            }
            else {
                for (int i=0; i<vals.length; i++)
                    stacks[i] = BlackMagick.itemFromString("{id:"+vals[i]+"_dye}");
            }
            this.btns = new Button[vals.length];
            this.btnX = new int[vals.length];
            int currentX = 10+30;
            for (int i=0; i<btns.length; i++) {
                this.btnX[i] = currentX;
                // final int col = i;

                Tooltip tt = null;
                boolean disabled = false;
                if (!isDye) {
                    disabled = true;
                    if (!patternItems[i].isEmpty()) {
                        List<Component> textList = patternItems[i].getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                        if (textList.size()>1) {
                            disabled = false;
                            tt = Tooltip.create(Component.nullToEmpty(BlackMagick.textComponentToStringLiteral(textList.get(1)).replace("Red ","")));
                        }
                    }
                    if (disabled) {
                        tt = Tooltip.create(errorText("Pattern disabled: "+vals[i]));
                        stacks[i] = FortytwoEdit.ITEM_ERROR.get();
                        stackWarns[i] = true;
                    }
                }
                else
                    tt = Tooltip.create(stacks[i].getHoverName());

                ItemSlotButton w = new ItemSlotButton(ItemBuilderScreen.this, ItemSlotButton.SLOT_HEIGHT, stacks[i], btn -> {
                    // nbtEditUpdate(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                    //     BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,StringTag.valueOf(vals[col])),blankElPath),saveBtn,
                    //     path2==null ? null : pagePath);
                    // CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    // if (path2 != null) {
                    //     newArgs.put("path2",path2);
                    // }
                    // if (blankTabEl != null)
                    //     newArgs.put("overrideEl",blankTabEl);

                    // CompoundTag cancelNbt = new CompoundTag();
                    // if (cancelEl != null)
                    //     cancelNbt.put("el",cancelEl);
                    // newArgs.put("cancelEl",cancelNbt);

                    // createBlankTab(BlankTabMode.NBT_EDIT,newArgs);
                });
                w.showSlot(false);
                if (stackWarns[i]) {
                    w.setError(ItemSlotButton.ItemError.ERROR);
                }
                this.btns[i] = w;
                currentX += 20;

                if (currentVal != null && (vals[i].equals(currentVal) || currentVal.equals("minecraft:"+vals[i])))
                    this.btns[i].active = false;

                this.btns[i].setTooltip(tt);
            }
            initChildren();
        }

    }

    class RowWidgetInvRow extends RowWidget {

        private static final Identifier[] PLAYER_ARMOR_SPRITES = new Identifier[]{
            ItemSlotButton.SPRITE_FEET,
            ItemSlotButton.SPRITE_LEGS,
            ItemSlotButton.SPRITE_CHEST,
            ItemSlotButton.SPRITE_HEAD,
            ItemSlotButton.SPRITE_OFFHAND
        };
        private static final Identifier[] ARMOR_STAND_SPRITES = new Identifier[]{
            ItemSlotButton.SPRITE_FEET,
            ItemSlotButton.SPRITE_LEGS,
            ItemSlotButton.SPRITE_CHEST,
            ItemSlotButton.SPRITE_HEAD,
            ItemSlotButton.SPRITE_BODY,
            ItemSlotButton.SPRITE_SADDLE,
            ItemSlotButton.SPRITE_MAINHAND,
            ItemSlotButton.SPRITE_OFFHAND
        };
        private static final Identifier SEL_SLOT = Identifier.parse("hud/hotbar_selection");
        private boolean renderHotbarSel = false;

        /**
         * Used for player inventory rows. Always make 5 rows (3 for inv, 1 for hotbar, 1 for armor/offhand).
         * 
         * @param row
         */
        public RowWidgetInvRow(int row) {
            if (row >= 0 && row < 4) {
                this.btns = new Button[9];
                this.btnX = new int[btns.length];
            }
            else if (row == 4) {
                renderHotbarSel = true; // rendered on row 4 so sprite isnt covered by row 4
                this.btns = new Button[5];
                this.btnX = new int[btns.length];
            }

            int currentX = 10+30;
            if (row==4)
                currentX += 20;
            for (int i=0; i<this.btns.length; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                final ItemStack thisItem = cacheInv[index];
                ItemSlotButton w = new ItemSlotButton(ItemBuilderScreen.this, ItemSlotButton.SLOT_HEIGHT, thisItem, btn -> btnCopyItemNbt(thisItem));
                if (row == 4) {
                    w.addEmptySlotSprite(PLAYER_ARMOR_SPRITES[i]);
                }
                this.btns[i] = w;
                this.btns[i].active = false;
                if (thisItem != null && !thisItem.isEmpty()) {
                    this.btns[i].active = true;
                    this.btns[i].setTooltip(makeItemTooltip(thisItem));
                }
                currentX += 20;
                if (row==4 && i==3)
                    currentX += 40;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);
            }
            initChildren();
        }

        /**
         * For any row of inventory items in inv tab.
         * Row must contain 1 to 9 items.
         * 
         * @param stacks
         */
        public RowWidgetInvRow(ItemStack[] stacks) {
            this(stacks, null);
        }


        /**
         * For any row of inventory items in inv tab.
         * Row must contain 1 to 9 items.
         * Slot sprites correspond to the stacks and should be same length array.
         * 
         * @param stacks
         * @param slotSprites
         */
        public RowWidgetInvRow(ItemStack[] stacks, Identifier[] slotSprites) {
            this.btns = new Button[stacks.length];
            this.btnX = new int[btns.length];

            int currentX = 10+30;
            for (int i=0; i<this.btns.length; i++) {
                this.btnX[i] = currentX;
                final ItemStack thisItem = stacks[i];
                ItemSlotButton w = new ItemSlotButton(ItemBuilderScreen.this, ItemSlotButton.SLOT_HEIGHT, thisItem, btn -> btnCopyItemNbt(thisItem));
                if (slotSprites != null && slotSprites.length == stacks.length)
                    w.addEmptySlotSprite(slotSprites[i]);
                this.btns[i] = w;
                this.btns[i].active = false;
                if (thisItem != null && !thisItem.isEmpty()) {
                    this.btns[i].active = true;
                    this.btns[i].setTooltip(makeItemTooltip(thisItem));
                }
                currentX += 20;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);
            }
            initChildren();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.extractContent(context, mouseX, mouseY, hovered, tickDelta);
            if (this.renderHotbarSel)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, SEL_SLOT,
                    this.getContentX()+(minecraft.player.getInventory().getSelectedSlot()*20)+40-2, this.getContentY()-2-20, 24, 23);
        }

    }

    class RowWidgetEditor extends RowWidget {

        private WidgetCacheType cacheType = WidgetCacheType.NONE;

        /**
         * For screens with an editor and separate txt for the element.
         * Contains get/set buttons and lbl to warn unset.
         * 
         * @param cacheType
         */
        public RowWidgetEditor(WidgetCacheType cacheType) {
            this.cacheType = cacheType;
            if (cacheType == null) {
                FortytwoEdit.logError("Tried to create RowWidgetEditor with null WidgetCacheType");
            }

            switch (cacheType) {
                case TXT_POSE: {
                    this.btns = new Button[3];
                    this.btnX = new int[]{ROW_LEFT_SCROLL,ROW_LEFT_SCROLL+20+WID_SPACE,ROW_LEFT_SCROLL+20+WID_SPACE+20+WID_SPACE};

                    this.btns[2] = Button.builder(Component.nullToEmpty(UNICODE_X), btn -> {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            poseCompound = new CompoundTag();
                            txt.setValue("");
                            resetSuggs();
                            updatePose();
                        }
                        unsel();
                    }).size(20,WID_HEIGHT).build();

                    this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty("Clear pose")));
                    break;
                }
                case TXT_DECIMAL_COLOR: {
                    this.btns = new Button[2];
                    this.btnX = new int[]{ROW_LEFT_SCROLL,ROW_LEFT_SCROLL+20+WID_SPACE};
                    break;
                }
                default: {
                    FortytwoEdit.logWarn("Tried to create RowWidgetEditor for invalid WidgetCacheType: "+cacheType);
                    break;
                }
            }

            this.btns[0] = Button.builder(Component.nullToEmpty(UNICODE_DOWN_ARROW), btn -> {
                switch (cacheType) {
                    case TXT_DECIMAL_COLOR: {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            if (txt.getValue().length()>0) {
                                trySetColorDec(0,txt.getValue(),null);
                            }
                            else {
                                trySetColorDec(0,"0",null);
                            }
                        }
                        break;
                    }
                    case TXT_POSE: {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            if (txt.getValue().length()>0) {
                                Tag el = BlackMagick.nbtFromSnbt(txt.getValue());
                                if (el!=null && el.getId()==Tag.TAG_COMPOUND) {
                                    poseCompound = new CompoundTag();
                                    CompoundTag copyFrom = (CompoundTag)el;
                                    for (String k : poseTypes) {
                                        if (copyFrom.getList(k).isPresent()) {
                                            ListTag l = copyFrom.getListOrEmpty(k);
                                            if (l.size()==3)
                                                poseCompound.put(k,l.copy());
                                        }
                                    }
                                }
                            }
                            else {
                                poseCompound = new CompoundTag();
                            }
                            updatePose();
                        }
                        break;
                    }
                    default: break;
                }
                unsel();
            }).size(20,WID_HEIGHT).build();

            this.btns[1] = Button.builder(Component.nullToEmpty(UNICODE_UP_ARROW), btn -> {
                switch (cacheType) {
                    case TXT_DECIMAL_COLOR: {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            txt.setValue(""+getRgbDec(0));
                            resetSuggs();
                        }
                        break;
                    }
                    case TXT_POSE: {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            if (poseCompound.isEmpty())
                                txt.setValue("");
                            else
                                txt.setValue(BlackMagick.nbtToSnbt(poseCompound));
                            resetSuggs();
                            updatePose();
                        }
                        break;
                    }
                    default: break;
                }
                unsel();
            }).size(20,WID_HEIGHT).build();

            this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty("Copy to editor")));
            this.btns[1].setTooltip(Tooltip.create(Component.nullToEmpty("Set from editor")));

            initChildren();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.extractContent(context, mouseX, mouseY, hovered, tickDelta);

            boolean editorEqual = false;
            switch (this.cacheType) {
                case TXT_DECIMAL_COLOR: {
                    if (widgetCacheTest(cacheType)) {
                        SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                        if (txt.getValue().equals(""+getRgbDec(0)))
                            editorEqual = true;
                    }
                    break;
                }
                case TXT_POSE: {
                    if (widgetCacheTest(cacheType)) {
                        SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                        if (txt.getValue().equals(""+BlackMagick.nbtToSnbt(poseCompound)))
                            editorEqual = true;
                        else if (txt.getValue().isEmpty() && poseCompound.isEmpty())
                            editorEqual = true;
                    }
                    break;
                }
                default: break;
            }

            if (!editorEqual) {
                context.text(ItemBuilderScreen.this.font, Component.nullToEmpty("Unlinked from editor"),
                    this.getContentX()+this.btnX[this.btnX.length-1]+20+5, this.getContentY()+6, ERROR_COLOR);
                this.btns[0].active = true;
                this.btns[1].active = true;
            }
            else {
                context.text(ItemBuilderScreen.this.font, Component.nullToEmpty("Linked to editor"),
                    this.getContentX()+this.btnX[this.btnX.length-1]+20+5, this.getContentY()+6, LABEL_COLOR_DIM);
                this.btns[0].active = false;
                this.btns[1].active = false;
            }
            if (this.btnX.length>=3) {
                boolean cleared = false;
                switch (this.cacheType) {
                    case TXT_POSE: {
                        if (widgetCacheTest(cacheType)) {
                            SmartEditBox txt = (SmartEditBox)widgetCacheGet(cacheType);
                            if ((txt.getValue().equals("{}") || txt.getValue().isEmpty()) && (poseCompound.isEmpty()))
                                cleared = true;
                        }
                        break;
                    }
                    default: break;
                }

                if (cleared) {
                    this.btns[2].active = false;
                }
                else {
                    this.btns[2].active = true;
                }
            }

        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    class PoseSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        public float val;
        private final String part;
        private final int num;

        public PoseSlider(String part, int num) {
            super(0, 0, 180, WID_HEIGHT, Component.nullToEmpty(""), 0.0);
            this.min = -180f;
            this.max = 180f;
            this.part = part;
            this.num = num;
            this.value = (0f - min) / (max - min);
            this.applyValue();
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            val = (float)Math.round(this.value*(max-min)+min);
            setPoseVal(part,num,val);
        }

        @Override
        protected void updateMessage() {
            if ((float)((int)val) == val)
                this.setMessage(Component.nullToEmpty(""+(int)val));
            else
                this.setMessage(Component.nullToEmpty(""+val));
        }

        public void setVal(float newVal) {
            while (newVal > 180)
                newVal -= 360;
            while (newVal < -180)
                newVal += 360;
            this.value = (double)((newVal - min)/(max-min));
            val = newVal;
            updateMessage();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    class RgbSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        public int setNum;
        public int num;
        private final boolean rgb;

        public RgbSlider(int setNum, int num, boolean halfWidth, boolean rgb) {
            super(0, 0, halfWidth ? 120-15-5 : 180-40, WID_HEIGHT, Component.nullToEmpty(""), 0.0);
            this.min = 0f;
            this.rgb = rgb;
            this.setNum = setNum;
            this.num = num;
            if (rgb) {
                this.max = 255f;
                this.value = (colorSets[this.setNum][this.num] - min) / (max - min);
            }
            else {
                if (num==0)
                    this.max = 360f;
                else
                    this.max = 100f;
                this.value = (colorHsl[num] - min) / (max - min);
            }
            this.applyValue();
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if (rgb) {
                colorSets[setNum][num] = (int)Math.round(this.value*(max-min)+min);
                updateColorSets();
            }
            else {
                float valMult = Math.round((this.value*(max-min)+min)*1000);
                setHsl(num,valMult/1000f);
            }
        }

        @Override
        protected void updateMessage() {
            if (rgb) {
                String color = UNICODE_SECTION_SIGN;
                if (num == 0 || num == 3)
                    color += "4";
                else if (num == 1 || num == 4)
                    color += "2";
                else
                    color += "1";
                this.setMessage(Component.nullToEmpty(color+colorSets[setNum][num]));
            }
            else {
                String color = UNICODE_SECTION_SIGN+"7";
                if (num == 0)
                    color += "H";
                else if (num == 1)
                    color += "S";
                else
                    color += "L";
                color += UNICODE_SECTION_SIGN+"r ";

                float valMult = Math.round((colorHsl[num])*1000);
                float val = valMult/1000f;

                this.setMessage(Component.nullToEmpty(color+val));
            }
        }

        public void setVal(int newVal) {
            if (newVal > max)
                newVal = (int)max;
            else if (newVal < min)
                newVal = (int)min;

            this.value = (double)((newVal - min)/(max-min));
            updateMessage();
        }

        public void setVal(float newVal) {
            if (newVal > max)
                newVal = (int)max;
            else if (newVal < min)
                newVal = (int)min;

            this.value = (double)((newVal - min)/(max-min));
            updateMessage();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private record Tab(int pos, String lbl, ItemStackPreset display, boolean hideTabs) {

        public Tab(int pos, String lbl, ItemStackPreset display) {
            this(pos,lbl,display,false);
        }

        public Tab() {
            this(-1,"",ItemStackPreset.set(ItemStack.EMPTY),true);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private enum WidgetCacheType {//to_do remove unused
        NONE,                   // do not cache (used for fallback page)

        TXT_DECIMAL_COLOR,      // SmartEditBox for PathType.DECIMAL_COLOR
        TXT_POSE,               // SmartEditBox for PathType.POSE

        BTN_SAVED_SOURCE,       // ItemSlotButtonWidget for Local Items | Black Market Items
        BTN_SAVED_MODE,         // Button below saved source btn

        GIVE_BOX_BOX,           // MultiLineEditBox for custom data tab
        GIVE_BOX_CLONE,         // Button to clone to give box
        GIVE_BOX_GIVE,          // Button to give from give box

        NBT_EDIT_SAVE_BTN,      // Button to save NbtEdit
        NBT_EDIT_CANCEL_BTN,    // Button to cancel NbtEdit
        NBT_EDIT_PATH_TXT,      // SmartEditBox to show path
        NBT_EDIT_PATH_BTN,      // Button to go to custom path
        NBT_EDIT_SNBT_ADD_BTN,  // Button to add nbt defined in SNBT MultiLineEditBox

        TEXT_COMPONENT_ADD_BTN,                     // Button to add current editor to main text component
        TEXT_COMPONENT_COLOR_BTN,                   // Button for color mode
        TEXT_COMPONENT_COLOR_TXT,                   // SmartEditBox for color mode
        TEXT_COMPONENT_RADIAL,                      // Button for Radial | Linear
        TEXT_COMPONENT_EFFECT_TEXT_MODE,            // Button for text/translation/keybind selection
        TEXT_COMPONENT_EFFECT_TEXT_ENTRY,           // SmartEditBox for text/translation/keybind entry
        TEXT_COMPONENT_TRANSLATION_WITH,            // SmartEditBox for `with`
        TEXT_COMPONENT_TRANSLATION_FALLBACK,        // SmartEditBox for `fallback`
        TEXT_COMPONENT_EFFECT_BTN_BOLD,             // Button for bold
        TEXT_COMPONENT_EFFECT_BTN_ITALIC,           // Button for italic
        TEXT_COMPONENT_EFFECT_BTN_UNDERLINED,       // Button for underlined
        TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,    // Button for strikethrough
        TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED,       // Button for obfuscated
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public record SavedItem(String storedString, ItemStack stack, boolean nbtError) {
        public static SavedItem build(String itemString) {
            ItemStack stack = BlackMagick.itemFromString(itemString);
            if (!stack.isEmpty()) {
                String newItemString = BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(stack));
                if (newItemString.equals(itemString)) {
                    return new SavedItem(itemString,stack,false);
                }
                else {
                    return new SavedItem(itemString,stack,true);
                }
            }
            return new SavedItem(itemString,null,true);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        if (!tabs[tab].hideTabs()) {
            if (tab == CACHE_TAB_SAVED && !viewBlackMarket && savedItemsError)
                context.centeredText(this.font,
                    Component.nullToEmpty("Failed to read saved items"), this.width / 2, y+this.backgroundHeight+3, ERROR_COLOR);

            if (prevArmorStand)
                InventoryScreen.extractEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, ENTITY_RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)renderArmorStand);
            else
                InventoryScreen.extractEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, ENTITY_RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)this.minecraft.player);

            txtFormat.setX(x+50);
            txtFormat.extractRenderState(context, mouseX, mouseY, delta);
            if (!this.UNSAVED_TEXT_WIDGETS.isEmpty())
                context.centeredText(this.font, Component.nullToEmpty("Unsaved"), this.width / 2, y-11, TEXT_COLOR);
        }
        else {
            if (textComponentPreview != null) {
                txtFormat.setX(x-15);
                txtFormat.extractRenderState(context, mouseX, mouseY, delta);

                if (textComponentPreviewBook) {
                    // Modified from {@link net.minecraft.client.gui.screens.inventory.BookViewScreen#visitText}
                    FormattedText formattedText = ComponentUtils.mergeStyles(textComponentPreview, Style.EMPTY.withoutShadow().withColor(-16777216));
                    List<FormattedCharSequence> cachedPageComponents = this.font.split(formattedText, 114);

                    int i = x + bookX;
                    int j = y + bookY;

                    int k = Math.min(128 / 9, cachedPageComponents.size());

                    for (int l = 0; l < k; l++) {
                        FormattedCharSequence formattedCharSequence = (FormattedCharSequence)cachedPageComponents.get(l);
                        context.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR).accept(i + 36, j + 30 + l * 9, formattedCharSequence);
                    }
                }
                else
                    context.centeredText(this.font, textComponentPreview, this.width / 2, y-14, TEXT_COLOR);

                if (tab != CACHE_TAB_BLANK) {
                    textComponentPreview = null;
                }
            }

            if (showBannerPreview && bannerChangePreview != null) {
                if (!bannerShield)
                    InventoryScreen.extractEntityInInventoryFollowsMouse(context,x+backgroundWidth,y,
                        x+backgroundWidth+100,y+400,2*ENTITY_RENDER_SIZE,0f,x+backgroundWidth+50,y+200,(LivingEntity)bannerChangePreview);
                else
                    InventoryScreen.extractEntityInInventoryFollowsMouse(context,x+backgroundWidth,y,
                        x+backgroundWidth+100,y+200,2*ENTITY_RENDER_SIZE,0f,x+backgroundWidth+50,y+100,(LivingEntity)bannerChangePreview);
            }

            if (showPosePreview) {
                InventoryScreen.extractEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, ENTITY_RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)renderArmorPose);
            }
        }
        if (lblInpError != null)
            lblInpError.extractRenderState(context, mouseX, mouseY, delta);

        if (suggs != null)
            suggs.extractRenderState(context, mouseX, mouseY);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return TEXTURE_MENU_BAR;
    }

    @Override
    protected void extractBehindBackgroundTexture(GuiGraphicsExtractor context) {
        if (textComponentPreviewBook && tabs[tab].hideTabs())
            context.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, x + bookX, y + bookY, 0.0F, 0.0F, 192, 192, 256, 256);
    }

    @Override
    public void resize(int width, int height) {
        if (!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        resetSuggs();
        super.resize(width, height);
        setErrorMsg(inpError);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return this.UNSAVED_TEXT_WIDGETS.isEmpty() && !activeTxt() && !tabs[tab].hideTabs();
    }

    @Override
    public void onCloseAction() {
        if (!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
        }
        super.onCloseAction();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (suggs != null && suggs.keyPressed(keyEvent)) {
            return true;
        }
        if (keyEvent.key() == GLFW.GLFW_KEY_LEFT || keyEvent.key() == GLFW.GLFW_KEY_RIGHT || keyEvent.key() == GLFW.GLFW_KEY_UP || keyEvent.key() == GLFW.GLFW_KEY_DOWN) {
            if (!activeTxt() && !activeSlider()) {
                if ((keyEvent.key() == GLFW.GLFW_KEY_LEFT || keyEvent.key() == GLFW.GLFW_KEY_RIGHT) && this.UNSAVED_TEXT_WIDGETS.isEmpty() && !tabs[tab].hideTabs() && hotbarLeftBtn.active) {
                    btnChangeSlot(keyEvent.key() == GLFW.GLFW_KEY_LEFT);
                }
                return true;
            }
        }
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (suggs != null && suggs.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleTap) {
        if (suggs != null && suggs.mouseClicked(mouseButtonEvent, doubleTap)) {
            return true;
        }
        resetSuggs();
        return super.mouseClicked(mouseButtonEvent, doubleTap);
    }

    @Override
    public void tick() {
        if (tab != CACHE_TAB_BLANK)
            updateItem();

        if (tab == CACHE_TAB_INV)
            updateInvTab();
        else if (tab == CACHE_TAB_MAIN && TAB_WIDGETS_SCROLL.get(tab).isEmpty())
            createTab(tab);

        super.tick();
    }

}
