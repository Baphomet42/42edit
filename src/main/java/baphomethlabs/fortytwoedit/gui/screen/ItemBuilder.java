package baphomethlabs.fortytwoedit.gui.screen;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.ComponentHelper;
import baphomethlabs.fortytwoedit.ComponentHelper.PathFlag;
import baphomethlabs.fortytwoedit.ComponentHelper.PathInfo;
import baphomethlabs.fortytwoedit.ComponentHelper.PathType;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton;

public class ItemBuilder extends GenericScreen {

    protected static int tab = 0;
    protected static final int TAB_OFFSET = 5;
    protected static final int TAB_SIZE = 24;
    protected static final int TAB_SPACING = 2;
    private static final int LEFT_TABS = 3;
    protected static final Tab[] tabs = new Tab[]{new Tab(0,"Components",new ItemStack(Items.GOLDEN_SWORD)),
        new Tab(1,"Presets",FortytwoEdit.HEAD42), new Tab(2,"Custom Data",new ItemStack(Items.COMMAND_BLOCK)),
        new Tab(3,"Inventory",new ItemStack(Items.ENDER_CHEST)), new Tab(4,"Saved Items",new ItemStack(Items.JIGSAW)),
        new Tab()};
    protected static final int CACHE_TAB_MAIN = 0;
    protected static final int CACHE_TAB_PRESETS = 1;
    protected static final int CACHE_TAB_NBT = 2;
    protected static final int CACHE_TAB_INV = 3;
    protected static final int CACHE_TAB_SAVED = 4;
    protected static final int CACHE_TAB_BLANK = 5;
    private boolean firstInit = true;
    protected static final int ROW_LEFT = 15;
    protected static final int ROW_RIGHT = 219;
    private static boolean runSuggsTest = true;
    protected ItemStack selItem = ItemStack.EMPTY;
    protected ItemStack selItemOff = ItemStack.EMPTY;
    protected static List<List<String>> cacheStates = Lists.newArrayList();
    protected ItemSlotButton itemBtn = null;
    protected Button swapBtn;
    protected Button swapCopyBtn;
    protected Button throwCopyBtn;
    protected Button hotbarLeftBtn;
    protected Button hotbarRightBtn;
    private EditBox txtFormat;
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
    private static boolean viewBlackMarket = false;
    private static final Tooltip TOOLTIP_BLACK_MARKET =
        Tooltip.create(BlackMagick.textComponentFromString("[{text:\"Black Market Items\"},{text:\"\n\nGet custom items produced by \",color:\"gray\"},"
        + "{text:\"BaphomethLabs\",color:\"gold\",italic:true},{text:\"\n(Mostly Harmless)\",color:\"gray\"}]").text());
    private static final Tooltip TOOLTIP_LOCAL_ITEMS =
        Tooltip.create(BlackMagick.textComponentFromString("[{text:\"Local Items\"},"
        + "{text:\"\n\nSave items for later without using up your saved hotbars\",color:\"gray\"}]").text());
    private static final ItemStack[] SAVED_TAB_MODE_ITEMS = new ItemStack[]{BlackMagick.itemFromNbtStatic(BlackMagick.validCompoundFromString(
        "{id:player_head,components:{profile:{properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQ"
        +"iIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pb"
        +"mVjcmFmdC5uZXQvdGV4dHVyZS9iZDlmMThjOWQ4NWY5MmY3MmY4NjRkNjdjMTM2N2U5YTQ1ZGMxMGYzNzE1NDljNDZhNGQ0ZGQ5ZTRmMTN"
        +"mZjQiDQogICAgfQ0KICB9DQp9\"}]}}}")),
        BlackMagick.itemFromNbtStatic(BlackMagick.validCompoundFromString("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlN"
        +"LSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MjY0ODZmNDI0ODljZWYwMmM5ZTk4ZGQ4Y"
        +"mU1YTNmMzhlODc5MTQ3NTQzMjZlNzdjODM3YzFiMmJjYmE2NSINCiAgICB9DQogIH0NCn0=\"}]}}}"))};
    protected static final ResourceLocation DELETE_ITEM_OVERLAY = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
    protected static final int DELETE_ITEM_OVERLAY_SIZE = 16;
    private ArmorStand renderArmorStand;
    private ArmorStand renderArmorPose;
    protected final int playerX = 240+10;
    protected final int playerY = -10;
    protected final int bookX = -150 - 2;
    protected final int bookY = 7;
    private static final int RENDER_SIZE = 35;
    private boolean prevArmorStand = false;
    private List<List<Set<PoseSlider>>> poseSliders = Lists.newArrayList();
    private List<Set<Button>> poseSliderBtns = Lists.newArrayList();
    private static CompoundTag poseCompound = new CompoundTag();
    private static final String[] poseTypes = new String[]{"Head","Body","RightArm","LeftArm","RightLeg","LeftLeg"};
    public static final String BANNER_PRESET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
    public static final String[] BANNER_CHAR_LIST = new String[BANNER_PRESET_CHARS.replaceAll("\\s","").length()+1];
    private TextSuggestor suggs;
    private Set<EditBox> currentTxt = Sets.newHashSet();
    private static int[][] colorSets = {{66,6,102},{0,0,0}};
    private static float[] colorHsv = {0f,0f,0f};
    private List<Set<EditBox>> colorHexTxts = Lists.newArrayList();
    private List<Set<EditBox>> colorDecTxts = Lists.newArrayList();
    private List<List<Set<RgbSlider>>> colorRgbSliders = Lists.newArrayList();
    private List<Set<RgbSlider>> colorHsvSliders = Lists.newArrayList();
    private List<List<Set<PosWidget>>> colorItemWids = Lists.newArrayList();
    private boolean editorLocked = false;
    private boolean hsvLock = false;
    private boolean editorOutputLocked = false;
    private Set<AbstractWidget> editorLockedWidget = Sets.newHashSet();
    private static final ItemStack[] rgbItems = new ItemStack[]{new ItemStack(Items.LEATHER_CHESTPLATE),new ItemStack(Items.POTION),new ItemStack(Items.FILLED_MAP)};
    private Component textComponentPreview = Component.nullToEmpty("");
    private boolean textComponentPreviewBook = false;
    private int textComponentEffectMode = -1;
    private String textComponentEffectPath = null;
    private String textComponentEffectBase = null;
    private static int[] textComponentEffects = new int[8];//bold,italic,underlined,strikethrough,obfuscated,radgrad,colmode,elmode
    private static String textComponentShadowColor = "";
    private static String textComponentFont = "";
    private static double[] tabScroll = new double[tabs.length];
    private boolean pauseSaveScroll = false;
    protected Tag blankTabEl = null;
    protected boolean blankTabUnsaved = false;
    private String textComponentBaseText = "";
    private boolean textComponentBaseValid = false;
    private boolean textComponentEffectValid = false;
    private String textComponentEffectFull = "";
    private static String textComponentLastColor = "white";
    private boolean bannerShield = false;
    private static ArmorStand bannerChangePreview = null;
    protected boolean showBannerPreview = false;
    protected boolean showPosePreview = false;
    private ItemStack[] cacheInv = new ItemStack[41];
    private int cacheInvSlot = -1;
    public static final Tooltip FORMAT_CODES_TT = Tooltip.create(BlackMagick.textComponentFromString("[{text:\"Formatting\n"+
        "0-black§r 1-§1dark_blue§r 2-§2dark_green§r 3-§3dark_aqua§r 4-§4dark_red§r 5-§5dark_purple§r "+
        "6-§6gold§r 7-§7gray§r 8-§8dark_gray§r 9-§9blue§r a-§agreen§r b-§baqua§r "+
        "c-§cred§r d-§dlight_purple§r e-§eyellow§r f-§fwhite§r #420666-\"},{text:\"0xRRGGBB\",color:\"#420666\"},"+
        "\"\nr-§rreset§r k-obfuscated§r l-§lbold§r m-§mstrikethrough§r n-§nunderlined§r o-§oitalic§r\","+
        "{text:\"\n\nFonts\ndefault- ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\nuniform- \"},{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\n\",font:\"uniform\"},"+
        "\"alt- \",{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\n\",font:\"alt\"},\"illageralt- \","+
        "{text:\"ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789\",font:\"illageralt\"}]").text());

    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.ITEM_BUILDER;

        if(firstInit) {
            if(tabs[tab].hideTabs())
                tab = CACHE_TAB_MAIN;

            updateArmorStand(null);

            for(int i=0; i<colorSets.length; i++) {
                colorHexTxts.add(Sets.newHashSet());
                colorDecTxts.add(Sets.newHashSet());
                colorRgbSliders.add(Lists.newArrayList());
                for(int j=0; j<3; j++)
                    colorRgbSliders.get(colorRgbSliders.size()-1).add(Sets.newHashSet());
                colorItemWids.add(Lists.newArrayList());
                for(int j=0; j<3; j++)
                    colorItemWids.get(colorItemWids.size()-1).add(Sets.newHashSet());
            }
            for(int i=0; i<3; i++)
                colorHsvSliders.add(Sets.newHashSet());
            for(int i=0; i<6; i++) {
                poseSliderBtns.add(Sets.newHashSet());
                poseSliders.add(Lists.newArrayList());
                for(int j=0; j<3; j++)
                    poseSliders.get(poseSliders.size()-1).add(Sets.newHashSet());
            }

            ComponentHelper.clearDynamicListCaches();
            firstInit = false;
        }
        pauseSaveScroll = false;

        if(runSuggsTest) {
            runSuggsTest = false;
            ComponentHelper.runAllListMethods();
        }

        if(!tabs[tab].hideTabs()) {

            //tabs
            for(int posNum = 0; posNum<tabs.length; posNum++)
                for(int i = 0; i<tabs.length; i++)
                    if(tabs[i].pos()==posNum) {
                        int tabNum = i;
                        int tabX;
                        int tabY;
                        if(posNum<LEFT_TABS) {
                            tabX = x-TAB_SIZE;
                            tabY = y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(posNum);
                        }
                        else {
                            tabX = x+240;
                            tabY = y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(posNum-LEFT_TABS);
                        }
                        ItemSlotButton w = new ItemSlotButton(tabX, tabY, TAB_SIZE, tabs[i].display(), btn -> this.btnTab(tabNum));
                        w.setTooltip(Tooltip.create(Component.nullToEmpty(tabs[i].lbl())));
                        w.showSlot(false);
                        if(tab==i)
                            w.active = false;
                        this.addRenderableWidget(w);
                    }

            //main
            this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new MagickGui())).bounds(x+5,y+5,40,20).build());
            txtFormat = new EditBox(this.font,x+50,y+5+1,15,18,Component.nullToEmpty(""));
            txtFormat.setEditable(false);
            txtFormat.setValue(UNICODE_SECTION_SIGN);
            txtFormat.setTooltip(FORMAT_CODES_TT);
            swapCopyBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("c*"),
                button -> this.btnSwapOff(true)).bounds(width/2 - 50,y+5,20,20).build());
            swapBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("c"),
                button -> this.btnSwapOff(false)).bounds(width/2 - 30,y+5,15,20).build());
            hotbarLeftBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("<"),
                button -> this.btnChangeSlot(true)).bounds(width/2 - 15,y+5,15,20).build());
            hotbarRightBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty(">"),
                button -> this.btnChangeSlot(false)).bounds(width/2,y+5,15,20).build());
            Button throwBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("Q"),
                button -> this.btnThrow(false)).bounds(width/2 + 15,y+5,15,20).build());
            throwCopyBtn = this.addRenderableWidget(Button.builder(Component.nullToEmpty("Q*"),
                button -> this.btnThrow(true)).bounds(width/2 + 30,y+5,20,20).build());

            if(!minecraft.player.getAbilities().instabuild) {
                swapCopyBtn.active = false;
                throwCopyBtn.active = false;
            }
            if(minecraft.player.isSpectator()) {
                swapCopyBtn.active = false;
                swapBtn.active = false;
                hotbarLeftBtn.active = false;
                hotbarRightBtn.active = false;
                throwBtn.active = false;
                throwCopyBtn.active = false;
            }
            if(swapCopyBtn.active)
                swapCopyBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Copy item to offhand")));
            if(swapBtn.active)
                swapBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Swap item with offhand")));
            if(hotbarLeftBtn.active)
                hotbarLeftBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Scroll hotbar left")));
            if(hotbarRightBtn.active)
                hotbarRightBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Scroll hotbar right")));
            if(throwBtn.active)
                throwBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Throw item")));
            if(throwCopyBtn.active)
                throwCopyBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Throw a copy of item")));

            itemBtn = this.addRenderableWidget(new ItemSlotButton(x+240-20-5, y+5, 20, selItem, button -> this.btnCopySelItemNbt()));
            if(selItem==null || selItem.isEmpty()) {
                itemBtn.active = false;
                itemBtn.setTooltip(null);
            }
            else {
                itemBtn.active = true;
                itemBtn.setTooltip(makeItemTooltip(selItem));
            }
        }

        //tabs
        if(TAB_WIDGETS_SCROLL.isEmpty())
            createStaticTabs();
        this.tabWidget = null;
        for(PosWidget p : TAB_WIDGETS_LOCKED.get(tab)) {
            p.w.setX(x+p.x);
            p.w.setY(y+p.y);
            this.addRenderableWidget(p.w);
        }
        if(!TAB_WIDGETS_SCROLL.get(tab).isEmpty()) {
            this.tabWidget = new TabWidget(tab);
            this.tabWidget.setScrollAmount(tabScroll[tab]);
            this.addRenderableWidget(this.tabWidget);
        }

        //banner prev
        if(bannerChangePreview == null) {
            bannerChangePreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
            bannerChangePreview.yBodyRot = 210.0f;
            bannerChangePreview.setXRot(25.0f);
            bannerChangePreview.yHeadRot = bannerChangePreview.getYRot();
            bannerChangePreview.yHeadRotO = bannerChangePreview.getYRot();
            bannerChangePreview.load(BlackMagick.validCompoundFromString("{Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
        }

        //banner
        int i=1;
        BANNER_CHAR_LIST[0] = "*";
        for(char c : BANNER_PRESET_CHARS.replaceAll("\\s","").toCharArray()) {
            BANNER_CHAR_LIST[i] = ""+c;
            i++;
        }

        // this should always be the last thing in init()
        updateItem();
    }

    protected void btnSwapOff(boolean copy) {
        if(!minecraft.player.isSpectator()) {
            if(!copy) {
                // from MinecraftClient (search `this.options.swapHandsKey.wasPressed()`)
                minecraft.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
            else if(minecraft.player.getAbilities().instabuild) {
                if(!minecraft.player.getMainHandItem().isEmpty()) {
                    BlackMagick.setItemOff(minecraft.player.getMainHandItem());
                }
                else if(!minecraft.player.getOffhandItem().isEmpty()) {
                    BlackMagick.setItemMain(minecraft.player.getOffhandItem());
                }
            }
        }
        unsel();
    }

    protected void btnChangeSlot(boolean left) {
        if(!minecraft.player.isSpectator()) {
            int slot = minecraft.player.getInventory().getSelectedSlot();
            if(left)
                slot--;
            else
                slot++;
            if(slot<0)
                slot = 8;
            else if(slot>8)
                slot = 0;
            minecraft.player.getInventory().setSelectedSlot(slot);
        }
        unsel();
    }

    protected void btnThrow(boolean copy) {
        if(!minecraft.player.isSpectator()) {
            if(!copy) {
                if(minecraft.player.drop(true))
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                minecraft.player.inventoryMenu.broadcastChanges();
            }
            else if(minecraft.player.getAbilities().instabuild) {
                ItemStack item = minecraft.player.getMainHandItem().copy();
                if(minecraft.player.drop(true))
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
                minecraft.player.inventoryMenu.broadcastChanges();
                BlackMagick.setItemMain(item);
            }
        }
        unsel();
    }

    protected void btnTab(int i) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        if(tab != i && (i == CACHE_TAB_NBT))
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
        if(stack != null && !stack.isEmpty()) {
            String itemData = BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(stack));
            FortytwoEdit.setClipboard(itemData);
            FortytwoEdit.showToast("Item Builder","Item NBT copied to clipboard");
        }
        unsel();
    }

    private void updateItem() {
        minecraft.player.inventoryMenu.broadcastChanges();
        boolean changed = false;
        if(!ItemStack.matches(selItem,minecraft.player.getMainHandItem()) && !TAB_WIDGETS_SCROLL.isEmpty())
            changed = true;
        boolean changedOff = false;
        if(!ItemStack.matches(selItemOff,minecraft.player.getOffhandItem()) && !TAB_WIDGETS_SCROLL.isEmpty())
            changedOff = true;

        if(changed) {
            selItem = minecraft.player.getMainHandItem().copy();
            FortytwoEdit.addItemHist(selItem);

            if(itemBtn != null) {
                itemBtn.setItem(selItem);
                if(selItem==null || selItem.isEmpty()) {
                    itemBtn.active = false;
                    itemBtn.setTooltip(null);
                }
                else {
                    itemBtn.active = true;
                    itemBtn.setTooltip(makeItemTooltip(selItem));
                }
            }

            if(tab==CACHE_TAB_MAIN)
                setErrorMsg(null);

            if(!cacheStates.isEmpty())
                cacheStates.clear();
            if(selItem!=null && !selItem.isEmpty())
                cacheStates = ComponentHelper.getBlockStates(selItem.getItem());

            if(selItem.is(Items.ARMOR_STAND)) {
                updateArmorStand(selItem.copy());
                prevArmorStand = true;
            }
            else if(prevArmorStand) {
                updateArmorStand(null);
                prevArmorStand = false;
            }

            if(widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                MultiLineEditBox giveBox = (MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX);
                boolean wasUnsaved = testUnsaved(giveBox);
                (giveBox).setValue(
                    (giveBox).getValue());
                if(!wasUnsaved) {
                    markSaved(giveBox);
                }
            }

            if(!TAB_WIDGETS_SCROLL.isEmpty()) {
                createTab(CACHE_TAB_MAIN);
            }
        }
        if(changedOff) {
            selItemOff = minecraft.player.getOffhandItem().copy();
            FortytwoEdit.addItemHist(selItemOff);
        }
        if(changed || changedOff)
            compareItems();
    }

    private void compareItems() {

        swapBtn.setTooltip(null);
        if(!minecraft.player.getMainHandItem().isEmpty() && !minecraft.player.getOffhandItem().isEmpty()) {
            if(ItemStack.isSameItemSameComponents(minecraft.player.getMainHandItem(),minecraft.player.getOffhandItem())) {
                swapBtn.setMessage(Component.empty().append("c").withStyle(ChatFormatting.GREEN));
            }
            else {
                swapBtn.setMessage(Component.empty().append("c").withStyle(ChatFormatting.RED));
                swapBtn.setTooltip(Tooltip.create(BlackMagick.getElementDifferences(BlackMagick.itemToNbtStorage(minecraft.player.getOffhandItem()),
                    BlackMagick.itemToNbtStorage(minecraft.player.getMainHandItem()))));
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
        for(AbstractWidget w : ALL_TEXT_WIDGETS)
            if(w.isFocused())
                return true;
        return false;
    }

    protected boolean activeSlider() {
        for(AbstractWidget w : ALL_SLIDER_WIDGETS)
            if(w.isFocused())
                return true;
        return false;
    }

    protected record EditingPath(int tab, String path) {}

    protected Tooltip makeItemTooltip(ItemStack stack) {
        if(stack==null || stack.isEmpty())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        String itemData = "";
        itemData += BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(stack));
        itemData = makeItemTooltipShorten(itemData);

        MutableComponent mutableText = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
        if(stack.has(DataComponents.CUSTOM_NAME)) {
            mutableText.withStyle(ChatFormatting.ITALIC);
        }

        return Tooltip.create(Component.empty().append(mutableText).append(Component.nullToEmpty("\n"+itemData)));
    }

    protected Tooltip makeItemTooltip(CompoundTag nbt, ItemStack stack) {
        if(nbt==null || !nbt.getString("id").isPresent())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        String itemData = "";
        itemData += BlackMagick.nbtToString(nbt);
        itemData = makeItemTooltipShorten(itemData);

        MutableComponent mutableText = null;
        if(stack != null) {
            mutableText = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
            if(stack.has(DataComponents.CUSTOM_NAME)) {
                mutableText.withStyle(ChatFormatting.ITALIC);
            }
        }

        return Tooltip.create(Component.empty().append(stack == null ? BlackMagick.textComponentFromString("{text:\"Failed to read item\",color:\"red\"}").text() :
            mutableText).append(Component.nullToEmpty("\n"+itemData)));
    }

    protected Tooltip makeItemTooltip(String nbtString) {
        if(nbtString==null || nbtString.isEmpty())
            return Tooltip.create(Component.nullToEmpty("Failed to read item"));
        String itemData = nbtString;
        itemData = makeItemTooltipShorten(itemData);

        return Tooltip.create(Component.empty().append(BlackMagick.textComponentFromString("{text:\"Failed to read item\",color:\"red\"}").text()).append(Component.nullToEmpty("\n"+itemData)));
    }

    private String makeItemTooltipShorten(String itemData) {
        //remove profile component properties
        String props = "properties:[";
        while(itemData.contains(props)) {
            int propertiesIndex = itemData.indexOf(props);
            String firstHalf = itemData.substring(0,propertiesIndex)+"properties:...";
            String secondHalf = itemData.substring(propertiesIndex+props.length());
            int bracketCount = 1;
            int nextOpenBracket;
            int nextCloseBracket;
            while(bracketCount!=0 && secondHalf.length()>0) {
                nextOpenBracket = secondHalf.indexOf('[');
                nextCloseBracket = secondHalf.indexOf(']');
                if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                    bracketCount++;
                    secondHalf = secondHalf.substring(nextOpenBracket+1);
                }
                else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1) {
                    bracketCount--;
                    secondHalf = secondHalf.substring(nextCloseBracket+1);
                }
                else
                    secondHalf = "";
            }
            itemData = firstHalf + secondHalf;
        }
        itemData = itemData.replace("properties:...","properties:[...]");
        return itemData;
    }

    private void refreshSaved() {
        savedItems = FortytwoEdit.getSavedItems();
        updateSavedTab();
    }
    private void updateSavedTab() {
        if(TAB_WIDGETS_SCROLL.get(CACHE_TAB_SAVED).size() >= FortytwoEdit.SAVED_ROWS)
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++) {
                RowWidget row = TAB_WIDGETS_SCROLL.get(CACHE_TAB_SAVED).get(i);
                if(row instanceof RowWidgetSavedItemsRow)
                    ((RowWidgetSavedItemsRow)row).updateSavedDisplay();
            }
    }

    private void updateSavedModeButtons() {
        if(widgetCacheTest(WidgetCacheType.BTN_SAVED_SOURCE, WidgetCacheType.BTN_SAVED_MODE)) {
            ItemSlotButton btnSource = (ItemSlotButton)widgetCacheGet(WidgetCacheType.BTN_SAVED_SOURCE);
            Button btnMode = (Button)widgetCacheGet(WidgetCacheType.BTN_SAVED_MODE);
            if(viewBlackMarket) {
                btnSource.setTooltip(TOOLTIP_BLACK_MARKET);
                btnSource.setItem(SAVED_TAB_MODE_ITEMS[1]);
                btnMode.setTooltip(Tooltip.create(Component.nullToEmpty("Refresh from Web")));
                btnMode.setMessage(Component.nullToEmpty(UNICODE_REFRESH));
            }
            else {
                btnSource.setTooltip(TOOLTIP_LOCAL_ITEMS);
                btnSource.setItem(SAVED_TAB_MODE_ITEMS[0]);
                if(savedModeSet) {
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
        if(type != null && type != WidgetCacheType.NONE && widget != null) {
            WIDGET_CACHE.put(type,widget);
        }
        return widget;
    }

    protected AbstractWidget widgetCacheGet(WidgetCacheType type) {
        return WIDGET_CACHE.get(type);
    }

    protected boolean widgetCacheTest(WidgetCacheType... types) {
        for(WidgetCacheType type : types) {
            if(WIDGET_CACHE.containsKey(type)) {
                if(WIDGET_CACHE.get(type) == null)
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
        if(tab != CACHE_TAB_BLANK && tab != CACHE_TAB_NBT)
            setErrorMsg(null);
    }

    private void updateArmorStand(ItemStack stand) {
        renderArmorStand = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
        renderArmorStand.yBodyRot = 210.0f;
        renderArmorStand.setXRot(25.0f);
        renderArmorStand.yHeadRot = renderArmorStand.getYRot();
        renderArmorStand.yHeadRotO = renderArmorStand.getYRot();

        if(stand != null && !stand.isEmpty() && BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data",Tag.TAG_COMPOUND) != null) {
            CompoundTag entity = (CompoundTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data");
            entity.putString("id","armor_stand");
            entity.put("Pos",BlackMagick.nbtFromString("[0d,0d,0d]"));
            entity.put("Motion",BlackMagick.nbtFromString("[0d,0d,0d]"));
            entity.put("Rotation",BlackMagick.nbtFromString("[0f,0f]"));
            renderArmorStand.load(entity.copy());
        }

        updatePose();
    }

    protected void updatePose() {
        if(!editorLocked) {
            editorLocked = true;

            renderArmorPose = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
            renderArmorPose.yBodyRot = 210f;
            renderArmorPose.setXRot(25f);
            renderArmorPose.yHeadRot = renderArmorPose.getYRot();
            renderArmorPose.yHeadRotO = renderArmorPose.getYRot();
            CompoundTag nbt = new CompoundTag();
            if(renderArmorStand != null)
                nbt = renderArmorStand.saveWithoutId(new CompoundTag());

            for(int i=0; i<poseSliders.size(); i++) {
                ListTag poseList = null;
                if(poseCompound.getList(poseTypes[i]).isPresent()) {
                    ListTag l = poseCompound.getListOrEmpty(poseTypes[i]);
                    if(l.size()==3)
                        poseList = l.copy();
                }
                for(int j=0; j<3; j++) {
                    float val = 0f;
                    if(poseList != null)
                        val = poseList.getFloatOr(j,0);
                    for(PoseSlider p : poseSliders.get(i).get(j))
                        p.setVal(val);
                }
                for(Button w : poseSliderBtns.get(i)) {
                    if(poseCompound.contains(poseTypes[i])) {
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
            renderArmorPose.load(nbt.copy());

            if(!editorOutputLocked) {
                if(widgetCacheTest(WidgetCacheType.TXT_POSE)) {
                    EditBox txt = (EditBox)widgetCacheGet(WidgetCacheType.TXT_POSE);
                    if(poseCompound.isEmpty())
                        txt.setValue("");
                    else
                        txt.setValue(BlackMagick.nbtToString(poseCompound));
                    resetSuggs();
                }
            }

            editorLocked = false;
            editorLockedWidget.clear();
        }
    }

    private void setPoseVal(String partKey, int axis, float val) {
        if(!editorLocked && !editorOutputLocked) {
            if(!poseCompound.contains(partKey)) {
                ListTag newPart = new ListTag();
                for(int i=0; i<3; i++)
                    newPart.add(FloatTag.valueOf(0f));
                poseCompound.put(partKey,newPart);
            }

            ListTag partList = poseCompound.getListOrEmpty(partKey);
            if(partList.size()==3) {
                partList.set(axis,FloatTag.valueOf(val));
                poseCompound.put(partKey,partList);
            }

            updatePose();
        }
    }

    public static String[] getStatesArr() {
        if(!cacheStates.isEmpty()) {
            String[] temp = new String[cacheStates.size()];
            for(int i=0; i<temp.length; i++)
                temp[i]=cacheStates.get(i).get(0);
            return temp;
        }
        return null;
    }

    public static String[] getStateVals(String key) {
        if(!cacheStates.isEmpty()) {
            for(int i=0; i<cacheStates.size(); i++) {
                if(cacheStates.get(i).get(0).equals(key) && cacheStates.get(i).size()>1) {
                    String[] temp = new String[cacheStates.get(i).size()-1];
                    for(int j=0; j<temp.length; j++)
                        temp[j]=cacheStates.get(i).get(j+1);
                    return temp;
                }
            }
        }
        return null;
    }

    private void updateColorSets() {
        if(!editorLocked) {
            editorLocked = true;

            rgbItems[0] = BlackMagick.itemFromString("{id:leather_chestplate,components:{dyed_color:"+getRgbDec(0)+"}}");
            rgbItems[1] = BlackMagick.itemFromString("{id:potion,components:{potion_contents:{custom_color:"+getRgbDec(0)+"}}}");
            rgbItems[2] = BlackMagick.itemFromString("{id:filled_map,components:{map_color:"+getRgbDec(0)+"}}");

            for(int rgbNum=0; rgbNum<colorSets.length; rgbNum++) {
                for(EditBox w : colorHexTxts.get(rgbNum)) {
                    if(textComponentEffectMode != 1 || textComponentEffects[6]==2) {
                        if(!editorLockedWidget.contains(w)) {
                            w.setValue(""+getRgbHex(rgbNum));
                        }
                        w.setTextColor(getRgbDec(rgbNum));
                        w.setTooltip(Tooltip.create(Component.nullToEmpty(getRgbHex(rgbNum))));
                    }
                }
                for(EditBox w : colorDecTxts.get(rgbNum)) {
                    if(!editorLockedWidget.contains(w)) {
                        w.setValue(""+getRgbDec(rgbNum));
                    }
                    w.setTextColor(getRgbDec(rgbNum));
                    w.setTooltip(Tooltip.create(Component.nullToEmpty(""+getRgbDec(rgbNum))));
                }
                for(int num=0; num<3; num++) {
                    for(RgbSlider w : colorRgbSliders.get(rgbNum).get(num)) {
                        w.setVal(colorSets[rgbNum][num]);
                    }
                    for(PosWidget w : colorItemWids.get(rgbNum).get(num)) {
                        w.s = rgbItems[num];
                    }
                }
            }

            if(!hsvLock) {
                rgbToHsv(colorSets[0][0],colorSets[0][1],colorSets[0][2]);
                for(int num=0; num<3; num++) {
                    for(RgbSlider w : colorHsvSliders.get(num)) {
                        w.setVal(colorHsv[num]);
                    }
                }
            }

            if(textComponentEffectMode>=0)
                updateTextComponentEffect();

            if(!editorOutputLocked) {
                if(widgetCacheTest(WidgetCacheType.TXT_DECIMAL_COLOR)) {
                    EditBox txt = (EditBox)widgetCacheGet(WidgetCacheType.TXT_DECIMAL_COLOR);
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
        for(int i=0; i<3; i++) {
            String current = Integer.toHexString(colorSets[rgbNum][i]).toUpperCase();
            if(current.length()==1)
                current = "0" + current;
            hex += current;
        }
        return hex;
    }

    private void trySetColorHex(int rgbNum, String inp, AbstractWidget w) {
        if(!editorLocked && rgbNum>=0 && rgbNum<colorSets.length && inp!=null && inp.length()>1 && inp.length()<=7 && inp.startsWith("#")) {
            boolean valid = true;
            String hex = inp.substring(1);
            int[] rgb = {0,0,0};
            while(hex.length()<6)
                hex = "0"+hex;
            for(int i=0; i<3; i++) {
                try {
                    int c = Integer.parseInt(hex.substring(2*i,2*i+2),16);
                    if(c<0 || c>255)
                        valid = false;
                    rgb[i] = c;
                } catch(Exception ex) {
                    valid = false;
                }
            }
            if(valid) {
                for(int i=0; i<3; i++)
                    colorSets[rgbNum][i]=rgb[i];
                if(w!=null)
                    editorLockedWidget.add(w);
                updateColorSets();
            }
        }
    }

    private void trySetColorDec(int rgbNum, String inp, AbstractWidget w) {
        trySetColorHex(rgbNum, BlackMagick.colorHexFromDec(inp), w);
    }

    private void swapColorSets(int left, int right) {
        int[] temp = colorSets[left];
        colorSets[left] = colorSets[right];
        colorSets[right] = temp;
        updateColorSets();
    }

    private void setHsv(int num, float val) {
        if(!editorLocked && !editorOutputLocked) {
            colorHsv[num] = val;
            hsvToRgb(0,colorHsv[0],colorHsv[1],colorHsv[2]);
            hsvLock = true;
            updateColorSets();
            hsvLock = false;
        }
    }

    private void hsvToRgb(int rgbNum, float h, float s, float v) {
        float sat = s/100f;
        float val = v/100f;
        float c = val*sat;
        float x = c*(1 - Math.abs((h/60f)%2 - 1));
        float m = val - c;

        float r0 = 0f;
        float g0 = 0f;
        float b0 = 0f;

        if(h==360)
            h=0;

        if(h<60) {
            r0 = c;
            g0 = x;
            b0 = 0f;
        }
        else if(h<120) {
            r0 = x;
            g0 = c;
            b0 = 0f;
        }
        else if(h<180) {
            r0 = 0f;
            g0 = c;
            b0 = x;
        }
        else if(h<240) {
            r0 = 0f;
            g0 = x;
            b0 = c;
        }
        else if(h<300) {
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

    private void rgbToHsv(int r, int g, int b) {
        float red = r/255f;
        float green = g/255f;
        float blue = b/255f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float h = 0;

        if(delta == 0f) {
            h = 0;
        }
        else if(max == red) {
            h = 60*(((green-blue)/delta)%6);
        }
        else if(max == green) {
            h = 60*(((blue-red)/delta)+2);
        }
        else if(max == blue) {
            h = 60*(((red-green)/delta)+4);
        }

        if(h<0)
            h+=360;

        float s = (max==0) ? 0 : (delta/max)*100;
        float v = max*100;

        colorHsv = new float[]{h,s,v};
    }

    private void drawItem(GuiGraphics context, ItemStack item, int x, int y) {//to_do remove
        context.renderItem(item,x,y);
        context.renderItemDecorations(this.font,item,x,y);
    }

    private void updateTextComponentPreview(String path, String textComponentBase) {
        updateTextComponentPreview(path,textComponentBase,null);
    }
    private void updateTextComponentPreview(String path, String textComponentBase, String textComponentEffect) {
        textComponentBaseText = textComponentBase;
        textComponentBaseValid = false;
        textComponentEffectValid = false;
        textComponentPreviewBook = false;
        textComponentPreview = BlackMagick.textComponentFromString(textComponentBase).text();
        if(BlackMagick.textComponentFromString(textComponentBase).isValid()) {
            textComponentBaseValid = true;
            if(textComponentEffect != null && BlackMagick.textComponentFromString(appendTextComponentEffect(textComponentBase,textComponentEffect)).isValid()) {
                textComponentPreview = BlackMagick.textComponentFromString(appendTextComponentEffect(textComponentBase,textComponentEffect)).text();
                if(textComponentEffect.length()>0 && !textComponentEffect.equals("{text:\"\"}"))
                    textComponentEffectValid = true;
                textComponentEffectFull = appendTextComponentEffect(textComponentBase,textComponentEffect);
            }
            if(path != null) {
                if(path.endsWith("custom_name"))
                    textComponentPreview = BlackMagick.textComponentFromString("{text:\"\",italic:true}").text().copy().append(textComponentPreview.copy());
                else if(path.contains("lore[")) {
                    textComponentPreview = BlackMagick.textComponentFromString("{text:\"\",color:\"dark_purple\",italic:true}").text().copy().append(textComponentPreview.copy());
                }
                else if(path.contains("written_book_content.pages["))
                    textComponentPreviewBook = true;
            }
        }
        if(textComponentEffectMode>=0 && widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_ADD_BTN)) {
            Button btnAdd = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_ADD_BTN);
            if(textComponentEffectValid && textComponentEffect != null && textComponentEffect.length()>0) {
                btnAdd.active = true;
                btnAdd.setTooltip(Tooltip.create(Component.nullToEmpty("Set text to:\n"+textComponentEffectFull)));
            }
            else {
                btnAdd.active = false;
                btnAdd.setTooltip(Tooltip.create(Component.nullToEmpty("Invalid Text Component")));
            }
        }
    }

    private String appendTextComponentEffect(String textComponentBase, String textComponentEffect) {
        if(textComponentBase.length()>1 && textComponentBase.startsWith("\"") && textComponentBase.endsWith("\""))
            textComponentBase = "{text:"+textComponentBase+"}";
        else if(textComponentBase.length()>1 && textComponentBase.startsWith("'") && textComponentBase.endsWith("'"))
            textComponentBase = "{text:\""+textComponentBase.substring(0,textComponentBase.length()-1)+"\"}";
        else if(!(textComponentBase.startsWith("{") && textComponentBase.endsWith("}"))
        && !(textComponentBase.startsWith("[") && textComponentBase.endsWith("]")) && !textComponentBase.contains("\"") && !textComponentBase.contains("'"))
            textComponentBase = "{text:\""+textComponentBase+"\"}";

        if(textComponentEffectMode == 0 || textComponentEffectMode == 1) {
            if(textComponentBase.isEmpty() || textComponentBase.equals("{}") || textComponentBase.equals("[]")
                    || textComponentBase.equals("[{}]") || textComponentBase.equals("{text:\"\"}") || textComponentBase.equals("[{text:\"\"}]"))
                return textComponentEffect;
            else if(textComponentBase.length()>=4 && textComponentBase.charAt(0)=='[' && textComponentBase.charAt(textComponentBase.length()-1)==']'
            && textComponentBase.charAt(1)=='{' && textComponentBase.charAt(textComponentBase.length()-2)=='}')
                return textComponentBase.substring(0,textComponentBase.length()-1) +","+ textComponentEffect +"]";
            else if(textComponentBase.length()>=2 && textComponentBase.charAt(0)=='{' && textComponentBase.charAt(textComponentBase.length()-1)=='}')
                return "["+textComponentBase+","+textComponentEffect+"]";
            return null;
        }
        else if(textComponentEffectMode == 2) {
            if(textComponentBase.length()>4 && textComponentBase.charAt(0)=='[' && textComponentBase.charAt(textComponentBase.length()-1)==']'
            && textComponentBase.charAt(1)=='{' && textComponentBase.charAt(textComponentBase.length()-2)=='}')
                return textComponentBase.substring(0,textComponentBase.length()-2) + textComponentEffect +"}]";
            else if(textComponentBase.length()>2 && textComponentBase.charAt(0)=='{' && textComponentBase.charAt(textComponentBase.length()-1)=='}')
                return textComponentBase.substring(0,textComponentBase.length()-1) + textComponentEffect +"}";
            return null;
        }
        else
            return null;
    }

    private void updateTextComponentEffectBtns() {
        if((textComponentEffectMode == 0 || textComponentEffectMode == 1)
        && widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD, WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC,
        WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED, WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,
        WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED)) {
            Button[] effectBtns = new Button[]{
                (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD),
                (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC),
                (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED),
                (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH),
                (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED)
            };
            int num = 0;
            String col = "";
            if(textComponentEffects[num]==1)
                col = "\u00a7a";
            else if(textComponentEffects[num]==2)
                col = "\u00a7c";
            effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7ll"));
            num++;
            col = "";
            if(textComponentEffects[num]==1)
                col = "\u00a7a";
            else if(textComponentEffects[num]==2)
                col = "\u00a7c";
            effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7oo"));
            num++;
            col = "";
            if(textComponentEffects[num]==1)
                col = "\u00a7a";
            else if(textComponentEffects[num]==2)
                col = "\u00a7c";
            effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7nn"));
            num++;
            col = "";
            if(textComponentEffects[num]==1)
                col = "\u00a7a";
            else if(textComponentEffects[num]==2)
                col = "\u00a7c";
            effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7mm"));
            num++;
            col = "";
            if(textComponentEffects[num]==1)
                col = "\u00a7a";
            else if(textComponentEffects[num]==2)
                col = "\u00a7c";
            effectBtns[num].setMessage(Component.nullToEmpty(col+"\u00a7kk"));
            switch(textComponentEffectMode) {
                case 0: {
                    if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_RADIAL)) {
                        Button w = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_RADIAL);
                        if(textComponentEffects[5]==0)
                            w.setMessage(Component.nullToEmpty("[Radial]"));
                        else
                            w.setMessage(Component.nullToEmpty("[Linear]"));
                    }
                    break;
                }
                case 1: {
                    if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN, WidgetCacheType.TEXT_COMPONENT_COLOR_TXT,
                    WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE)) {

                        Button btnColor = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN);
                        EditBox txtColor = (EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_COLOR_TXT);

                        txtColor.setTextColor(TEXT_COLOR);
                        txtColor.setEditable(true);
                        txtColor.setTooltip(null);

                        if(textComponentEffects[6]==2) {
                            btnColor.setMessage(Component.nullToEmpty("Color [RGB]"));
                            txtColor.setValue(getRgbHex(0));
                            txtColor.setTextColor(getRgbDec(0));
                            txtColor.setTooltip(Tooltip.create(Component.nullToEmpty(getRgbHex(0))));
                        }
                        else if(textComponentEffects[6]==1) {
                            btnColor.setMessage(Component.nullToEmpty("Color [Vanilla]"));
                            txtColor.setValue(textComponentLastColor);
                        }
                        else if(textComponentEffects[6]==0) {
                            btnColor.setMessage(Component.nullToEmpty("Color [None]"));
                            txtColor.setValue("<None>");
                            txtColor.setEditable(false);
                        }

                        Button btnTextMode = (Button)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE);
        
                        btnTextMode.setMessage(Component.nullToEmpty("[Text]"));
                        if(textComponentEffects[7]==1)
                            btnTextMode.setMessage(Component.nullToEmpty("[Keybind]"));
                        else if(textComponentEffects[7]==2)
                            btnTextMode.setMessage(Component.nullToEmpty("[Translate]"));
                    }
                    break;
                }
                default: break;
            }
        }
    }

    private void updateTextComponentEffect() {
        if(textComponentEffectPath == null || textComponentEffectBase == null)
            return;
        switch(textComponentEffectMode) {
            case 0: {
                if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)) {
                    String value = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)).getValue();
                    String val = "";
                    if(value.length()==1 || (colorSets[0][0]==colorSets[1][0] && colorSets[0][1]==colorSets[1][1] && colorSets[0][2]==colorSets[1][2])) {
                        val+="{text:\"";
                        for(int i=0; i<value.length(); i++) { // to_do handle escapes
                            String thisChar = ""+value.charAt(i);
                            if(thisChar.equals("\\") || thisChar.equals("\""))
                                thisChar = "\\"+thisChar;
                            val+=thisChar;
                        }
                        val+="\",color:\""+getRgbHex(0)+"\"";
                        if(textComponentShadowColor.length()>0)
                            val+=",shadow_color:"+textComponentShadowColor;
                        if(textComponentFont.length()>0)
                            val+=",font:\""+textComponentFont+"\"";
                        if(textComponentEffects[0]==1)
                            val+=",bold:true";
                        else if(textComponentEffects[0]==2)
                            val+=",bold:false";
                        if(textComponentEffects[1]==1)
                            val+=",italic:true";
                        else if(textComponentEffects[1]==2)
                            val+=",italic:false";
                        if(textComponentEffects[2]==1)
                            val+=",underlined:true";
                        else if(textComponentEffects[2]==2)
                            val+=",underlined:false";
                        if(textComponentEffects[3]==1)
                            val+=",strikethrough:true";
                        else if(textComponentEffects[3]==2)
                            val+=",strikethrough:false";
                        if(textComponentEffects[4]==1)
                            val+=",obfuscated:true";
                        else if(textComponentEffects[4]==2)
                            val+=",obfuscated:false";
                        val+="}";
                    }
                    else if(value.length() > 1) {
                        val+="{text:\"";
                        for(int i=0; i<1; i++) { // to_do handle escapes
                            String thisChar = ""+value.charAt(i);
                            if(thisChar.equals("\\") || thisChar.equals("\""))
                                thisChar = "\\"+thisChar;
                            val+=thisChar;
                        }
                        val+="\",color:\"#";
                        for(int c=0; c<3; c++) {
                            int col = 0;
                            if(textComponentEffects[5]==1)
                                col = colorSets[0][c];
                            else {
                                col = colorSets[1][c];
                            }
                            String current = Integer.toHexString(col).toUpperCase();
                            if(current.length()==1)
                                current = "0" + current;
                            val += current;
                        }
                        val+="\"";
                        if(textComponentShadowColor.length()>0)
                            val+=",shadow_color:"+textComponentShadowColor;
                        if(textComponentFont.length()>0)
                            val+=",font:\""+textComponentFont+"\"";
                        if(textComponentEffects[0]==1)
                            val+=",bold:true";
                        else if(textComponentEffects[0]==2)
                            val+=",bold:false";
                        if(textComponentEffects[1]==1)
                            val+=",italic:true";
                        else if(textComponentEffects[1]==2)
                            val+=",italic:false";
                        if(textComponentEffects[2]==1)
                            val+=",underlined:true";
                        else if(textComponentEffects[2]==2)
                            val+=",underlined:false";
                        if(textComponentEffects[3]==1)
                            val+=",strikethrough:true";
                        else if(textComponentEffects[3]==2)
                            val+=",strikethrough:false";
                        if(textComponentEffects[4]==1)
                            val+=",obfuscated:true";
                        else if(textComponentEffects[4]==2)
                            val+=",obfuscated:false";
                        val+=",\"extra\":[";
                        boolean firstPart = true;
                        for(int i=1; i<value.length(); i++) { // to_do handle escapes
                            if(!firstPart)
                                val+=",";
                            String thisChar = ""+value.charAt(i);
                            if(thisChar.equals("\\") || thisChar.equals("\""))
                                thisChar = "\\"+thisChar;
                            val+="{text:\""+thisChar+"\",color:\"#";
                            for(int c=0; c<3; c++) {
                                int col = 0;
                                if(textComponentEffects[5]==1)
                                    col = colorSets[0][c] + (int)((colorSets[1][c]-colorSets[0][c])*i/((double)(value.length()-1)));
                                else {
                                    if(value.length()%2==0) {
                                        if(i<value.length()/2)
                                            col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*i/((double)(value.length()/2-1)));
                                        else
                                            col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*(value.length()-i-1)/((double)(value.length()/2-1)));
                                    }
                                    else {
                                        if(i<=value.length()/2)
                                            col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*i/((double)(value.length()/2)));
                                        else
                                            col = colorSets[1][c] + (int)((colorSets[0][c]-colorSets[1][c])*(value.length()-i-1)/((double)(value.length()/2)));
                                    }
                                }
        
                                String current = Integer.toHexString(col).toUpperCase();
                                if(current.length()==1)
                                    current = "0" + current;
                                val += current;
                            }
                            val+="\"}";
                            firstPart = false;
                        }
                        val+="]}";
                    }
                    if(value == null || value.equals(""))
                        val = "{text:\"\"}";
                    updateTextComponentPreview(textComponentEffectPath,textComponentEffectBase,val);
                }
                break;
            }
            case 1: {
                if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)) {
                    String value = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY)).getValue();
                    String val = "{";
                    if(textComponentEffects[7] == 0) { // to_do handle escapes
                        val+="text:\"";
                        for(int i=0; i<value.length(); i++) {
                            String thisChar = ""+value.charAt(i);
                            if(thisChar.equals("\\") || thisChar.equals("\""))
                                thisChar = "\\"+thisChar;
                            val+=thisChar;
                        }
                        val+="\"";
                    }
                    else if(textComponentEffects[7] == 1) {
                        val+="\"keybind\":\""+value+"\"";
                    }
                    else if(textComponentEffects[7] == 2) {
                        if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH, WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK)) {
                            EditBox txtWith = (EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH);
                            EditBox txtFallback = (EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK);
                            val+="\"translate\":\""+value+"\"";
                            if(txtWith.getValue().length()>0)
                                val+=",\"with\":"+txtWith.getValue();
                            if(txtFallback.getValue().length()>0)
                                val+=",\"fallback\":\""+txtFallback.getValue()+"\"";
                        }
                    }
                    if(textComponentEffects[6]==2)
                        val+=",color:\""+getRgbHex(0)+"\"";
                    else if(textComponentEffects[6]==1)
                        val+=",color:\""+textComponentLastColor+"\"";
                    if(textComponentShadowColor.length()>0)
                        val+=",shadow_color:"+textComponentShadowColor;
                    if(textComponentFont.length()>0)
                        val+=",font:\""+textComponentFont+"\"";
                    if(textComponentEffects[0]==1)
                        val+=",bold:true";
                    else if(textComponentEffects[0]==2)
                        val+=",bold:false";
                    if(textComponentEffects[1]==1)
                        val+=",italic:true";
                    else if(textComponentEffects[1]==2)
                        val+=",italic:false";
                    if(textComponentEffects[2]==1)
                        val+=",underlined:true";
                    else if(textComponentEffects[2]==2)
                        val+=",underlined:false";
                    if(textComponentEffects[3]==1)
                        val+=",strikethrough:true";
                    else if(textComponentEffects[3]==2)
                        val+=",strikethrough:false";
                    if(textComponentEffects[4]==1)
                        val+=",obfuscated:true";
                    else if(textComponentEffects[4]==2)
                        val+=",obfuscated:false";
                    val+="}";
                    if(value == null || value.equals(""))
                        val = "{text:\"\"}";
                    updateTextComponentPreview(textComponentEffectPath,textComponentEffectBase,val);
                }
                break;
            }
            case 2: {
                if(widgetCacheTest(WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_ACTION, WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_VALUE,
                WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_ACTION, WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_CONTENTS)) {
                    String val = "";
                    String click = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_ACTION)).getValue();
                    String value = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_VALUE)).getValue();
                    String hover = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_ACTION)).getValue();
                    String contents = ((EditBox)widgetCacheGet(WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_CONTENTS)).getValue();
                    if(click != null && click.length()>0 && value != null && value.length()>0) {
                        val += ",\"clickEvent\":{\"action\":\""+click+"\",\"value\":\"";
                        for(int i=0; i<value.length(); i++) { // to_do handle escapes
                            String thisChar = ""+value.charAt(i);
                            if(thisChar.equals("\\") || thisChar.equals("\""))
                                thisChar = "\\"+thisChar;
                            val+=thisChar;
                        }
                        val += "\"}";
                    }
                    if(hover != null && hover.length()>0 && contents != null && contents.length()>0) { // to_do handle escapes
                        val += ",\"hoverEvent\":{\"action\":\""+hover+"\",\"contents\":"+contents+"}";
                    }
                    updateTextComponentPreview(textComponentEffectPath,textComponentEffectBase,val);
                }
                break;
            }
        }
    }

    /**
     * Modified from {@link net.minecraft.client.gui.screens.inventory.BookViewScreen#getClickedComponentStyleAt}
     */
    private Style getBookTextStyleAt(List<FormattedCharSequence> page, int bookRenderX, int bookRenderY, double x, double y) {
        if(page.isEmpty()) {
            return null;
        }
        int i = Mth.floor(x - (double)bookRenderX - 36.0);
        int j = Mth.floor(y - 2.0 - 30.0 - (double)bookRenderY);
        if(i < 0 || j < 0) {
            return null;
        }
        int k = Math.min(128 / this.font.lineHeight, page.size());
        if(i <= 114 && j < this.minecraft.font.lineHeight * k + k) {
            int l = j / this.minecraft.font.lineHeight;
            if(l >= 0 && l < page.size()) {
                FormattedCharSequence orderedText = page.get(l);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(orderedText, i);
            }
        }
        return null;
    }

    private void updateInvTab() {
        boolean changed = false;
        for(int i=0; i<cacheInv.length; i++) {
            ItemStack current = null;
            if(i<27)
                current = minecraft.player.getInventory().getItem(i+9).copy();
            else if(i<36)
                current = minecraft.player.getInventory().getItem(i-27).copy();
            else if(i<40)
                current = minecraft.player.getInventory().getItem(i).copy();
            else if(i<41)
                current = minecraft.player.getOffhandItem().copy();
            if(cacheInv[i] == null || !ItemStack.matches(cacheInv[i],current)) {
                changed = true;
                cacheInv[i] = current;
            }
        }
        if(cacheInvSlot != minecraft.player.getInventory().getSelectedSlot()) {
            changed = true;
            cacheInvSlot = minecraft.player.getInventory().getSelectedSlot();
        }

        if(changed) {
            createTab(CACHE_TAB_INV);
            btnTab(CACHE_TAB_INV);
        }
    }

    /**
     * Make path look better for labels. Do not use for actual path searches.
     * 
     * @param path like components.minecraft:foo.bar.list[2]
     * @return path without starting components. node and with all minecraft: namespaces removed
     */
    private String cleanPath(String path) {
        if(path.startsWith("components."))
            path = path.replaceFirst("components\\.","");
        return path.replace("minecraft:","");
    }

    private String trimStringSize(String inp) {
        int maxSize = this.width-10;
        if(ItemBuilder.this.font.width(inp)>maxSize && inp.length()>1) {
            String trail = "...";
            maxSize -= font.width(trail);
            if(width>10)
                inp = ItemBuilder.this.font.plainSubstrByWidth(inp,maxSize);
            return inp+trail;
        }
        return inp;
    }

    private void setErrorMsg(String errorMsg) {
        if(errorMsg == null) {
            inpError = null;
            inpErrorTrim = null;
        }
        else {
            inpError = errorMsg;
            inpErrorTrim = trimStringSize(errorMsg);
        }
    }

    private Component getButtonText(String path, Tag el) {
        if(el==null)
            return Component.empty();
        String elString = el==null ? "null" : BlackMagick.nbtToString(el);
        String elStringContent = elString;
        if(el != null && el.getId() == Tag.TAG_STRING)
            elStringContent = el.asString().get(); // keep asString
        PathInfo pi = ComponentHelper.getPathInfo(path);
        if(pi.type()==PathType.TEXT) {
            Component btnTxt = BlackMagick.textComponentFromString((el==null) ? "" : BlackMagick.nbtToString(el)).text();
            if(el != null && el.getId()==Tag.TAG_STRING && BlackMagick.textComponentFromString(elString).isValid()) {
                if(path.endsWith("custom_name"))
                    btnTxt = BlackMagick.textComponentFromString("{text:\"\",italic:true}").text().copy().append(btnTxt.copy());
                else if(path.contains("lore["))
                    btnTxt = BlackMagick.textComponentFromString("{text:\"\",color:\"dark_purple\",italic:true}").text().copy().append(btnTxt.copy());
            }
            return btnTxt;
        }
        else if(pi.type()==PathType.DECIMAL_COLOR) {
            if(el!=null && el.getId()!=Tag.TAG_STRING && el.getId()!=Tag.TAG_LIST && el.getId()!=Tag.TAG_COMPOUND) {
                if(BlackMagick.colorHexFromDec(elStringContent) != null)
                    return BlackMagick.textComponentFromString("{text:\""+elStringContent+"\",color:\""+BlackMagick.colorHexFromDec(elStringContent)+"\"}").text();
                return BlackMagick.textComponentFromString("{text:\"Invalid color: "+elStringContent+"\",color:\"red\"}").text();
            }
            return Component.nullToEmpty("Invalid color").copy().withStyle(ChatFormatting.RED);
        }
        else if(pi.flag()==PathFlag.ATTRIBUTE) {
            if(el != null && el.getId()==Tag.TAG_COMPOUND) {
                ItemStack stack = BlackMagick.itemFromString("{id:stone,components:{attribute_modifiers:{modifiers:["+BlackMagick.nbtToString(el)+"]}}}");
                if(!stack.isEmpty()) {
                    List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                    if(textList.size()>3)
                        return Component.nullToEmpty(textList.get(2).getString()).copy().append(Component.nullToEmpty(" ")).append(textList.get(3));
                }
            }
            return Component.nullToEmpty("Invalid attribute").copy().withStyle(ChatFormatting.RED);
        }
        else if(pi.type()==PathType.BANNER) {
            if(el != null && el.getId()==Tag.TAG_COMPOUND) {
                ItemStack stack = BlackMagick.itemFromString("{id:white_banner,components:{banner_patterns:["+BlackMagick.nbtToString(el)+"]}}");
                if(!stack.isEmpty()) {
                    List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                    if(textList.size()>1)
                        return Component.nullToEmpty(textList.get(1).getString());
                }
            }
            return Component.nullToEmpty("Invalid pattern").copy().withStyle(ChatFormatting.RED);
        }
        else if(pi.flag()==PathFlag.EFFECT) {
            if(el != null && el.getId()==Tag.TAG_COMPOUND) {
                ItemStack stack = BlackMagick.itemFromString("{id:potion,components:{potion_contents:{custom_effects:["+BlackMagick.nbtToString(el)+"]}}}");
                if(!stack.isEmpty()) {
                    List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                    if(textList.size()>1 && !textList.get(1).getString().equals("No Effects"))
                        return textList.get(1);
                }
            }
            return Component.nullToEmpty("Invalid effect").copy().withStyle(ChatFormatting.RED);
        }
        else if(pi.flag()==PathFlag.PROBABILITY_EFFECT) {
            if(el != null && el.getId()==Tag.TAG_COMPOUND) {
                CompoundTag nbt = (CompoundTag)el;
                if(nbt.getCompound("effect").isPresent()) {
                    ItemStack stack = BlackMagick.itemFromString("{id:potion,components:{potion_contents:{custom_effects:["+BlackMagick.nbtToString(nbt.getCompoundOrEmpty("effect"))+"]}}}");
                    if(!stack.isEmpty()) {
                        List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                        if(textList.size()>1 && !textList.get(1).getString().equals("No Effects"))
                            return textList.get(1);
                    }
                }
            }
            return Component.nullToEmpty("Invalid effect").copy().withStyle(ChatFormatting.RED);
        }
        else if(pi.flag()==PathFlag.FIREWORK) {
            if(el != null && el.getId()==Tag.TAG_COMPOUND) {
                ItemStack stack = BlackMagick.itemFromString("{id:firework_star,components:{firework_explosion:"+BlackMagick.nbtToString(el)+"}}");
                if(!stack.isEmpty()) {
                    List<Component> textList = stack.getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                    if(textList.size()>1) {
                        MutableComponent btnLbl = Component.empty();
                        for(int i=1; i<textList.size(); i++) {
                            if(i>1)
                                btnLbl.append(Component.nullToEmpty(", "));
                            btnLbl.append(textList.get(i));
                        }
                        return Component.nullToEmpty(btnLbl.getString());
                    }
                }
            }
            return Component.nullToEmpty("Invalid explosion").copy().withStyle(ChatFormatting.RED);
        }
        return Component.nullToEmpty(elString);
    }

    private Component getButtonTooltip(PathInfo pi, String key) {
        MutableComponent btnTt = Component.empty().append(Component.nullToEmpty("Key: "+key));
        if(ComponentHelper.pathTypeToNbtType(pi.type()) != -1)
            btnTt = btnTt.append(Component.nullToEmpty("\nNBT Type: "+ComponentHelper.formatNbtType(ComponentHelper.pathTypeToNbtType(pi.type()))));
        if(pi.description() != null)
            btnTt = btnTt.append(Component.nullToEmpty("\n\n")).append(pi.description());
        return btnTt;
    }

    private void suggsOnChanged(EditBox w, String[] suggestions, String startVal) {
        if(w == null)
            return;

        boolean shouldSetSuggs = false;
        if(!currentTxt.contains(w)) {
            resetSuggs();
            currentTxt.add(w);
            suggs = new TextSuggestor(minecraft, w, font);
            shouldSetSuggs = true;
        }
        else {
            if(suggs != null)
                suggs.refresh();
            else {
                resetSuggs();
                suggs = new TextSuggestor(minecraft, w, font);
                shouldSetSuggs = true;
            }
        }
        if(shouldSetSuggs) {
            if(suggs == null)
                return;
            List<String> startVals = null;
            if(startVal != null)
                startVals = List.of(startVal);
            List<List<String>> joinSuggs = null;
            if(suggestions != null) {
                joinSuggs = List.of(List.of(suggestions));
            }
            String[] suggsArr = BlackMagick.joinCommandSuggs(joinSuggs, startVals).toArray(new String[0]);
            if(suggsArr != null && suggsArr.length>0)
                suggs.setSuggestions(suggsArr);
        }
    }

    public void setEditingElement(String path, Tag newEl, Button saveBtn) {
        setEditingElement(path,newEl,saveBtn,null);
    }

    public void setEditingElement(String path, Tag newEl, Button saveBtn, String pagePath) {
        blankTabEl = newEl;
        blankTabUnsaved = true;

        Tag displayEl = null;
        if(pagePath == null)
            saveBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Invalid component:\n" + (blankTabEl==null ? "null" : BlackMagick.nbtToString(blankTabEl)))));
        else {
            displayEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),pagePath);
            saveBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Invalid element:\n" + (displayEl==null ? "null" : BlackMagick.nbtToString(displayEl)))));
        }
        saveBtn.active = pagePath!=null;

        if(inpError == null)
            setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToString(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl)),inpError));

        if(blankTabEl != null && inpError == null) {
            ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl));
            if(ItemStack.matches(selItem,newItem)) {
                blankTabUnsaved = false;
                if(pagePath == null)
                    saveBtn.setTooltip(Tooltip.create(Component.nullToEmpty("Item unchanged")));
                else {
                    Component tempText = Component.nullToEmpty("Element value:\n").copy().append(
                        BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath), displayEl));
                    if(displayEl == null && BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath) == null)
                        tempText = Component.nullToEmpty("Element value:\nnull");
                    saveBtn.setTooltip(Tooltip.create(tempText));
                }
            }
            else if(newItem != null) {
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(newItem),path) != null) {
                    if(pagePath == null) {
                        Tag modEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(BlackMagick.itemFromNbt(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl))),path);
                        if(modEl != null) {
                            Component tempText = Component.nullToEmpty("Set component:\n").copy().append(
                                BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path), modEl));
                            if(minecraft.player.getAbilities().instabuild) {
                                saveBtn.setTooltip(Tooltip.create(tempText));
                                saveBtn.active = true;
                            }
                            else {
                                saveBtn.setTooltip(Tooltip.create(ERROR_CREATIVE.copy().append("\n").append(tempText)));
                            }
                        }
                    }
                    else {
                        Component tempText = Component.nullToEmpty("Element value:\n").copy().append(
                            BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath), displayEl));
                        if(displayEl == null && BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath) == null)
                            tempText = Component.nullToEmpty("Element value:\nnull");
                        saveBtn.setTooltip(Tooltip.create(tempText));
                    }
                }
            }
        }
    }

    protected RowWidget addTabWidgetScroll(int tabNum, RowWidget row) {
        TAB_WIDGETS_SCROLL.get(tabNum).add(row);
        return row;
    }

    protected PosWidget addTabWidgetLocked(int tabNum, PosWidget widget) {
        TAB_WIDGETS_LOCKED.get(tabNum).add(widget);
        return widget;
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
        for(int i=0; i<tabs.length; i++) {
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
                    if(!selItem.isEmpty()) {
                        boolean removed = false;
                        Tag loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",Tag.TAG_LIST);
                        if(loreEl != null) {
                            ListTag lore = (ListTag)loreEl;
                            if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'{color:\"gold\",text:\"BaphomethLabs\"}'")) {
                                removed = true;
                                lore.remove(lore.size()-1);
                                if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                            if(!removed && !lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals(
                                    "'{color:\"gold\",text:\"Bottled by BaphomethLabs\"}'")) {
                                lore.remove(lore.size()-1);
                                if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                        }
                        if(!removed) {
                            ListTag lore;
                            if(loreEl != null)
                                lore = (ListTag)loreEl;
                            else
                                lore = new ListTag();
                            lore.add(StringTag.valueOf("\"\""));
                            lore.add(StringTag.valueOf("{color:\"gold\",text:\"BaphomethLabs\"}"));
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel();
                }, btn -> {
                    if(!selItem.isEmpty()) {
                        boolean removed = false;
                        Tag loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",Tag.TAG_LIST);
                        if(loreEl != null) {
                            ListTag lore = (ListTag)loreEl;
                            if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals(
                                    "'{color:\"gold\",text:\"Bottled by BaphomethLabs\"}'")) {
                                removed = true;
                                lore.remove(lore.size()-1);
                                if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                            if(!removed && !lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals(
                                    "'{color:\"gold\",text:\"BaphomethLabs\"}'")) {
                                lore.remove(lore.size()-1);
                                if(!lore.isEmpty() && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                        }
                        if(!removed) {
                            ListTag lore;
                            if(loreEl != null)
                                lore = (ListTag)loreEl;
                            else
                                lore = new ListTag();
                            lore.add(StringTag.valueOf("\"\""));
                            lore.add(StringTag.valueOf("{color:\"gold\",text:\"Bottled by BaphomethLabs\"}"));
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel();
                }, btn -> {
                    if(!selItem.isEmpty()) {
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"")!=null) {
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"",null)));
                            if(BlackMagick.nbtToString(BlackMagick.getNbtPath(BlackMagick.itemToNbt(newStack),"components.minecraft:custom_data")).equals("{}")) {
                                newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data",null)));
                            }
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                        else {
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"",IntTag.valueOf(42))));
                            if(!newStack.isEmpty())
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
                addTabWidgetScroll(tabNum, new RowWidget("Owner","Create player head from player name",btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j).btn()[0];
                    if(!inp.equals("")) {
                        if(minecraft.player.getMainHandItem().isEmpty()) {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                BlackMagick.validCompoundFromString("{id:player_head}"),"components.minecraft:profile",StringTag.valueOf(inp))));
                        }
                        else {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:profile",StringTag.valueOf(inp))));
                        }
                    }
                    else {
                        if(minecraft.player.getMainHandItem().isEmpty())
                            BlackMagick.setItemMain(new ItemStack(Items.PLAYER_HEAD));
                        else {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:profile",null)));
                        }
                    }
                },null,false));
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget("Skin","Create player head from give command (with the name removed)",btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j).btn()[0];
                    if(inp.equals("")) {
                        if(!minecraft.player.getMainHandItem().isEmpty())
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),
                                "components.minecraft:profile",null)));
                    }
                    else if(inp.contains("name:\"textures\"") && inp.contains(",value:\"")) {
                        String value = inp;
                        value = value.substring(value.indexOf(",value:\"")+8);
                        if(value.contains("\"")) {
                            value = value.substring(0,value.indexOf("\""));
                            CompoundTag temp;
                            if(selItem.isEmpty())
                                temp = BlackMagick.validCompoundFromString("{id:player_head}");
                            else
                                temp = BlackMagick.itemToNbt(selItem);
                            Tag parseValue = BlackMagick.nbtFromString("[{name:\"textures\",value:\""+value+"\"}]");
                            if(parseValue != null && parseValue.getId()==Tag.TAG_LIST) {
                                temp = BlackMagick.setNbtPath(temp,"components.minecraft:profile.properties",parseValue);
                                temp = BlackMagick.setNbtPath(temp,"components.minecraft:profile.name",null);
                                temp = BlackMagick.setNbtPath(temp,"components.minecraft:profile.id",null);
                                ItemStack newItem = BlackMagick.itemFromNbt(temp);
                                if(!newItem.isEmpty())
                                    BlackMagick.setItemMain(newItem);
                            }
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
                    if(!inp.trim().equals("")) {
                        String sound = inp.trim();
                        ResourceLocation soundId = BlackMagick.identifierOrNull(sound);
                        if(soundId != null)
                            minecraft.player.playNotifySound(SoundEvent.createVariableRangeEvent(soundId), SoundSource.MASTER, 1, 1);
                    }
                }, ComponentHelper.REGISTRY_SOUND_EVENT.getArray(),true));// to_do add registry sounds + dynamic assets sounds
            }
            {
                final int i = tabNum; final int j = getTabWidgetScrollIndex(tabNum);
                addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("Head Sound")},new int[]{80},
                        new String[]{"Create a preset head that plays the sound when on a note block"},null,false,btn -> {
                    String inp = TAB_WIDGETS_SCROLL.get(i).get(j-1).btn()[0];
                    if(!inp.trim().equals("")) {
                        String sound = inp.trim();
                        ResourceLocation soundId = BlackMagick.identifierOrNull(sound);
                        if(soundId != null) {
                            String soundDisplay = soundId.getNamespace().equals("minecraft") ? soundId.getPath() : soundId.toString();
                            ItemStack item = BlackMagick.itemFromString(
                                "{id:player_head,components:{\"minecraft:profile\":{properties:[{name:\"textures\",value:"+
                                "\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDov"+
                                "L3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2VlYjc3ZDRkMjU3MjRhOWNhZjJjN2NkZjJkODgzOTliMTQxN2M2YjlmZjUyMTM2NTliNjUzYmU0Mz"+
                                "c2ZTMiDQogICAgfQ0KICB9DQp9\"}]},\"minecraft:note_block_sound\":\""+soundId.toString()+"\","+
                                "\"minecraft:custom_name\":'{italic:false,text:\""+soundDisplay+"\"}'}}"); //to_do use nbt methods instead of string appending
                            if(!item.isEmpty())
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
                {ComponentHelper.LIST_DYE_COLOR.getArray(),BANNER_CHAR_LIST,ComponentHelper.LIST_DYE_COLOR.getArray()},
                false,btn -> {
                    String[] inps = TAB_WIDGETS_SCROLL.get(i).get(j).btn();
                    if(minecraft.player.getAbilities().instabuild) {

                        ItemStack bannerStack = ItemStack.EMPTY;

                        String chars = inps[1];
                        if(chars.equals("*"))
                            chars = BANNER_PRESET_CHARS;
                        else {
                            String newchars = "";
                            for(int ii=0; ii<chars.length(); ii++)
                                if(BANNER_PRESET_CHARS.contains(""+chars.charAt(ii)))
                                    newchars += chars.charAt(ii);
                            chars = newchars;
                        }

                        if(chars.length()==1)
                            bannerStack = BlackMagick.itemFromNbt(BlackMagick.createBanner(chars.charAt(0),inps[2].toLowerCase(),inps[0].toLowerCase()));
                        else if(chars.length()>1) {
                            ListTag items = new ListTag();
                            while(chars.length()>0) {
                                CompoundTag bannerItem = BlackMagick.createBanner(chars.charAt(0),inps[2].toLowerCase(),inps[0].toLowerCase());
                                if(bannerItem!=null)
                                    items.add(bannerItem);
                                if(chars.length()==1)
                                    chars = "";
                                else
                                    chars = chars.substring(1);
                            }
                            if(!items.isEmpty())
                                bannerStack = BlackMagick.itemFromString("{id:bundle,components:{bundle_contents:"
                                    +BlackMagick.nbtToString(items)+"}}");
                        }

                        if(!bannerStack.isEmpty()) {
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
                giveBox = new MultiLineEditBox(((ItemBuilder)ItemBuilder.this).minecraft.font, x+15-3, y+35, 240-36, 22*6, Component.nullToEmpty(""), Component.nullToEmpty(""));
                addTabWidgetLocked(tabNum, new PosWidget(giveBox,15-3,35));
                this.ALL_TEXT_WIDGETS.add(giveBox);
                widgetCacheAdd(WidgetCacheType.GIVE_BOX_BOX,giveBox);
                giveBox.setValueListener(value -> {
                    if(widgetCacheTest(WidgetCacheType.GIVE_BOX_CLONE, WidgetCacheType.GIVE_BOX_GIVE)) {
                        setErrorMsg(null);
                        Button btnClone = (Button)widgetCacheGet(WidgetCacheType.GIVE_BOX_CLONE);
                        Button btnGive = (Button)widgetCacheGet(WidgetCacheType.GIVE_BOX_GIVE);
                        btnGive.active = false;
                        btnGive.setTooltip(Tooltip.create(Component.nullToEmpty("Invalid item")));
                        if(value != null && !value.trim().equals("")) {
                            String inp = ""+value;
                            ItemStack item = ItemStack.EMPTY;

                            // keep consistent
                            inp = inp.trim();
                            if(inp.contains("/") && inp.indexOf("/")==0)
                                inp = inp.substring(1);
                            if(inp.contains("give ") && inp.indexOf("give ")==0) {
                                inp = inp.substring(5);
                                if(inp.contains(" "))
                                    inp = inp.substring(inp.indexOf(" ")+1); // remove selector or player name
                            }
                            else if(inp.startsWith("summon item ~ ~ ~ {Item:") && inp.endsWith("}")) {
                                inp = inp.substring("summon item ~ ~ ~ {Item:".length(),inp.length()-1);
                            }

                            if(inp.startsWith("{") && inp.endsWith("}")) {
                                if(BlackMagick.nbtFromString(inp,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromNbt((CompoundTag)BlackMagick.nbtFromString(inp,Tag.TAG_COMPOUND));
                                }

                                setErrorMsg(BlackMagick.getItemCompoundErrors(inp,inpError));
                                if(item.isEmpty() && inpError == null)
                                    setErrorMsg("Invalid item");

                                if(!item.isEmpty() && inpError == null) {
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        Component.nullToEmpty("Set current item to:\n"+BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }
                            else if(((inp.startsWith("\"{") && inp.endsWith("}\"")) || (inp.startsWith("'{") && inp.endsWith("}'")))
                            && BlackMagick.nbtFromString(inp,Tag.TAG_STRING) != null) {
                                String inpString = ((StringTag)BlackMagick.nbtFromString(inp,Tag.TAG_STRING)).asString().get(); // keep asString
                                if(BlackMagick.nbtFromString(inpString,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromNbt((CompoundTag)BlackMagick.nbtFromString(inpString,Tag.TAG_COMPOUND));
                                }

                                setErrorMsg(BlackMagick.getItemCompoundErrors(inpString,inpError));
                                if(item.isEmpty() && inpError == null)
                                    setErrorMsg("Invalid item");

                                if(!item.isEmpty() && inpError == null) {
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        Component.nullToEmpty("Set current item to:\n"+BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }
                            else {
                                int count = 1;
                                if(inp.contains(" ")) {
                                    int last = inp.lastIndexOf(" ");
                                    try {
                                        count = Integer.parseInt(inp.substring(last+1));
                                        inp = inp.substring(0,last);
                                    } catch(NumberFormatException ex) {}
                                }

                                try {
                                    item = ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createItemStack(1,false);
                                } catch(Exception ex) {
                                    if(ex instanceof CommandSyntaxException) {
                                        setErrorMsg(((CommandSyntaxException)ex).getMessage());
                                        if(inpError.contains(" at position ")) {
                                            setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                        }
                                    }
                                }

                                if(!item.isEmpty()) {
                                    item.setCount(count);
                                    btnGive.active = true;
                                    btnGive.setTooltip(Tooltip.create(
                                        Component.nullToEmpty("Set current item to:\n"+BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(item)))));
                                }
                            }

                            if(ItemStack.matches(item,selItem)) {
                                ItemBuilder.this.markSaved(giveBox);
                                btnGive.active = false;
                                btnGive.setTooltip(Tooltip.create(Component.nullToEmpty("Item unchanged")));
                            }
                            else {
                                ItemBuilder.this.markUnsaved(giveBox);
                            }

                        }
                        else {
                            ItemBuilder.this.markSaved(giveBox);
                        }

                        if(!value.equals(BlackMagick.itemToGive(minecraft.player.getMainHandItem()))) {
                            btnClone.active = true;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("Copy current item")));
                        }
                        else {
                            btnClone.active = false;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("Already cloned")));
                        }

                        if(selItem.isEmpty()) {
                            btnClone.active = false;
                            btnClone.setTooltip(Tooltip.create(Component.nullToEmpty("No item to clone")));
                        }
                    }
                });
            }
            {
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.GIVE_BOX_CLONE,Button.builder(Component.nullToEmpty("Clone"), btn -> {
                    if(!minecraft.player.getMainHandItem().isEmpty() && widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                        ((MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX)).setValue(BlackMagick.itemToGive(minecraft.player.getMainHandItem()));
                    }
                    ItemBuilder.this.unsel();
                }).bounds(x+15-3,y+35+22*6+1,60,20).build()),15-3,35+22*6+1));
            }
            {
                Button w = Button.builder(Component.nullToEmpty("Give"), btn -> {
                    if(widgetCacheTest(WidgetCacheType.GIVE_BOX_BOX)) {
                        MultiLineEditBox editBox = (MultiLineEditBox)widgetCacheGet(WidgetCacheType.GIVE_BOX_BOX);
                        String inp = editBox.getValue();
                        ItemBuilder.this.markSaved(editBox);

                        if(minecraft.player.getAbilities().instabuild) {
                            ItemStack item = ItemStack.EMPTY;

                            // keep consistent
                            inp = inp.trim();
                            if(inp.contains("/") && inp.indexOf("/")==0)
                                inp = inp.substring(1);
                            if(inp.contains("give ") && inp.indexOf("give ")==0) {
                                inp = inp.substring(5);
                                if(inp.contains(" "))
                                    inp = inp.substring(inp.indexOf(" ")+1); // remove selector or player name
                            }
                            else if(inp.startsWith("summon item ~ ~ ~ {Item:") && inp.endsWith("}")) {
                                inp = inp.substring("summon item ~ ~ ~ {Item:".length(),inp.length()-1);
                            }

                            if(inp.startsWith("{") && inp.endsWith("}")) {
                                if(BlackMagick.nbtFromString(inp,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromNbt((CompoundTag)BlackMagick.nbtFromString(inp,Tag.TAG_COMPOUND));
                                }
                            }
                            else if(((inp.startsWith("\"{") && inp.endsWith("}\"")) || (inp.startsWith("'{") && inp.endsWith("}'")))
                            && BlackMagick.nbtFromString(inp,Tag.TAG_STRING) != null) {
                                String inpString = ((StringTag)BlackMagick.nbtFromString(inp,Tag.TAG_STRING)).asString().get(); // keep asString
                                if(BlackMagick.nbtFromString(inpString,Tag.TAG_COMPOUND) != null) {
                                    item = BlackMagick.itemFromNbt((CompoundTag)BlackMagick.nbtFromString(inpString,Tag.TAG_COMPOUND));
                                }
                            }
                            else {
                                int count = 1;
                                if(inp.contains(" ")) {
                                    int last = inp.lastIndexOf(" ");
                                    try {
                                        count = Integer.parseInt(inp.substring(last+1));
                                        inp = inp.substring(0,last);
                                    } catch(NumberFormatException ex) {}
                                }

                                try {
                                    item = ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createItemStack(1,false);
                                } catch(Exception ex) {}

                                if(!item.isEmpty())
                                    item.setCount(count);
                            }

                            BlackMagick.setItemMain(item);
                        }
                    }
                    ItemBuilder.this.unsel();
                }).bounds(x+15-3+5+60,y+35+22*6+1,60,20).build();
                if(!minecraft.player.getAbilities().instabuild)
                    w.active = false;
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.GIVE_BOX_GIVE,w),15-3+5+60,35+22*6+1));
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
                ItemSlotButton w = new ItemSlotButton(x+15-3, y+35+1+22, 20, btn -> {
                    viewBlackMarket = !viewBlackMarket;
                    updateSavedModeButtons();
                    updateSavedTab();
                    ItemBuilder.this.unsel();
                });
                w.showSlot(false);
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.BTN_SAVED_SOURCE,w),15-3,35+1));
            }
            {
                // button is setup in updateSavedModeButtons()
                Button w = Button.builder(Component.nullToEmpty(""), btn -> {
                    if(viewBlackMarket) {
                        FortytwoEdit.readOptions();
                        CompoundTag result = FortytwoEdit.refreshWebItems(true);

                        if(result.contains("site_match_catch"))
                            FortytwoEdit.showToast("Black Market", "Items up to date");
                        else if(result.contains("site_updated_catch"))
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
                    ItemBuilder.this.unsel();
                }).bounds(x+15-3, y+35+1+22,20,20).build();
                addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.BTN_SAVED_MODE,w),15-3,35+1+22));
            }
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
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
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        for(RowWidget r : TAB_WIDGETS_SCROLL.get(tabNum)) {
            for(EditBox t : r.txts) {
                this.UNSAVED_TEXT_WIDGETS.remove(t);
                this.ALL_TEXT_WIDGETS.remove(t);
            }
            for(PosWidget p : r.wids) {
                if(p.w != null) {
                    this.UNSAVED_TEXT_WIDGETS.remove(p.w);
                    this.ALL_TEXT_WIDGETS.remove(p.w);
                    this.ALL_SLIDER_WIDGETS.remove(p.w);
                }
            }
        }
        for(PosWidget r : TAB_WIDGETS_LOCKED.get(tabNum)) {
            this.UNSAVED_TEXT_WIDGETS.remove(r.w);
            this.ALL_TEXT_WIDGETS.remove(r.w);
        }
        TAB_WIDGETS_SCROLL.get(tabNum).clear();
        TAB_WIDGETS_LOCKED.get(tabNum).clear();

        if(tabNum == CACHE_TAB_MAIN) {   //createBlock components
            {
                addTabWidgetScroll(tabNum, new RowWidgetComponent("id"));
            }
            if(!selItem.isEmpty()) {
                {
                    addTabWidgetScroll(tabNum, new RowWidgetComponent("count"));
                }
                Set<String> allSetComponentKeys = Sets.newHashSet();
                Set<String> modifiedComponentKeys = Sets.newHashSet();
                Set<String> defaultComponentKeys = Sets.newHashSet();
                Set<String> removedComponentKeys = Sets.newHashSet();
                Set<String> storedComponentKeys = BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.itemToNbtStorage(selItem),"components")).keySet();
                for(String k : BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components")).keySet()) {
                    if(k.startsWith("!"))
                        removedComponentKeys.add(k);
                    else {
                        allSetComponentKeys.add(k);
                        if(storedComponentKeys.contains(k))
                            modifiedComponentKeys.add(k);
                        else
                            defaultComponentKeys.add(k);
                    }
                }
                if(!modifiedComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget("Modified Components"));
                    for(String c : modifiedComponentKeys)
                        addTabWidgetScroll(tabNum, new RowWidgetComponent("components."+c));
                }
                if(!defaultComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget("Default Components"));
                    for(String c : defaultComponentKeys)
                        addTabWidgetScroll(tabNum, new RowWidgetComponent("components."+c));
                }
                if(!removedComponentKeys.isEmpty()) {
                    addTabWidgetScroll(tabNum, new RowWidget("Removed Components"));
                    for(String c : removedComponentKeys)
                        addTabWidgetScroll(tabNum, new RowWidgetComponent("components."+c));
                }
                boolean firstUnset = true;
                for(String c : ComponentHelper.LIST_DATA_COMPONENT_TYPE.getList()) {
                    if(!allSetComponentKeys.contains(c)) {
                        if(firstUnset) {
                            addTabWidgetScroll(tabNum, new RowWidget("Unset Components"));
                            firstUnset = false;
                        }
                        addTabWidgetScroll(tabNum, new RowWidgetComponent("components."+c));
                    }
                }
            }
            {
                addTabWidgetScroll(tabNum, new RowWidget());
            }
        }
        else if(tabNum == CACHE_TAB_INV) {   //createBlock inventory
            {
                addTabWidgetScroll(tabNum, new RowWidget("Inventory"));
            }
            {
                for(int i=0; i<5; i++)
                    addTabWidgetScroll(tabNum, new RowWidgetInvRow(i));
            }
            for(int i=0; i<2; i++) {
                ItemStack current = i==0 ? minecraft.player.getMainHandItem() : minecraft.player.getOffhandItem();
                if(current != null && !current.isEmpty()) {
                    int[] size = ComponentHelper.getContainerSize(current.getItem());

                    if(current.is(Items.BUNDLE)) {
                        {
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Bundle" : "Offhand Bundle"));
                        }
                        ListTag itemsList = new ListTag();
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents[0]",Tag.TAG_COMPOUND) != null) {
                            itemsList = (ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents");
                        }

                        ItemStack[] stacks = new ItemStack[itemsList.size()+1];
                        for(int index=0; index<itemsList.size(); index++) { //to_do verify cast below
                            stacks[index+1] = BlackMagick.itemFromNbt((CompoundTag)itemsList.get(index));
                        }

                        int index = 0;
                        for(int r=0; r<=(stacks.length-1)/9; r++) {
                            ItemStack[] stackRow = new ItemStack[stacks.length-index > 9 ? 9 : stacks.length-index];
                            for(int c=0; c<stackRow.length; c++)
                                stackRow[c] = stacks[index+c];
                            index+=9;
                            addTabWidgetScroll(tabNum, new RowWidgetInvRow(stackRow));
                        }
                    }
                    else if(current.is(Items.ARMOR_STAND)) {
                        {
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Armor Stand" : "Offhand Armor Stand"));
                        }
                        ItemStack[] stacks = new ItemStack[6];

                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.ArmorItems[3]",Tag.TAG_COMPOUND) != null) {
                            ListTag itemsList = ((ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.ArmorItems"));
                            for(int a=0; a<4; a++) {//to_do verify cast below
                                stacks[a] = BlackMagick.itemFromNbt((CompoundTag)itemsList.get(a));
                            }
                        }

                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.HandItems[1]",Tag.TAG_COMPOUND) != null) {
                            ListTag itemsList = ((ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.HandItems"));
                            for(int h=0; h<2; h++) {//to_do verify cast below
                                stacks[4+h] = BlackMagick.itemFromNbt((CompoundTag)itemsList.get(h));
                            }
                        }

                        addTabWidgetScroll(tabNum, new RowWidgetInvRow(stacks,RowWidgetInvRow.ARMOR_STAND_SPRITES));
                    }
                    else if(size[0]>0 && size[1]>0) {
                        {
                            addTabWidgetScroll(tabNum, new RowWidget(i==0 ? "Selected Container" : "Offhand Container"));
                        }
                        ListTag itemsList = null;
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container[0]",Tag.TAG_COMPOUND) != null)
                            itemsList = (ListTag)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container");
                        for(int r=0; r<size[0]; r++) {
                            ItemStack[] stacks = new ItemStack[size[1]];
                            if(itemsList != null)
                                for(int c=0; c<size[1]; c++) {
                                    for(int index=0; index<itemsList.size(); index++) {
                                        if(itemsList.get(index).getId() == Tag.TAG_COMPOUND
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
            for(int h=0; h<HotbarManager.NUM_HOTBAR_GROUPS; h++) {
                List<ItemStack> row = minecraft.getHotbarManager().get(h).load(minecraft.level.registryAccess());
                ItemStack[] stacks = new ItemStack[9];
                for(int c=0; c<stacks.length; c++) {
                    if(row.size() > c)
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

        if(tab == tabNum) {
            btnTab(tab);
        }
        resetSuggs();
    }

    /**
     * Clears blank tab then recreates its widgets.
     * Sets the current page to this tab.
     * Use for hidden tabs.
     * 
     * @param mode type of blank page to make
     * @param args args depending on mode (sometimes can be null)
     */
    public void createBlankTab(int mode, CompoundTag args) {
        int tabNum = CACHE_TAB_BLANK;
        textComponentPreview = null;
        showBannerPreview = false;
        showPosePreview = false;
        tabScroll[tabNum] = 0d;
        setErrorMsg(null);

        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        for(RowWidget r : TAB_WIDGETS_SCROLL.get(tabNum)) {
            for(EditBox t : r.txts) {
                this.UNSAVED_TEXT_WIDGETS.remove(t);
                this.ALL_TEXT_WIDGETS.remove(t);
            }
            for(PosWidget p : r.wids) {
                if(p.w != null) {
                    this.UNSAVED_TEXT_WIDGETS.remove(p.w);
                    this.ALL_TEXT_WIDGETS.remove(p.w);
                    this.ALL_SLIDER_WIDGETS.remove(p.w);
                }
            }
        }
        for(PosWidget r : TAB_WIDGETS_LOCKED.get(tabNum)) {
            this.UNSAVED_TEXT_WIDGETS.remove(r.w);
            this.ALL_TEXT_WIDGETS.remove(r.w);
        }
        TAB_WIDGETS_SCROLL.get(tabNum).clear();
        TAB_WIDGETS_LOCKED.get(tabNum).clear();

        boolean valid = false;

        if(mode==0) { // display current component
            if(args.getString("path").isPresent()) {
                String path = args.getString("path").get();
                String[] path2;
                if(args.getList("path2").isPresent() && !args.getList("path2").get().isEmpty()
                && args.getList("path2").get().get(0).getId()==Tag.TAG_STRING) {
                    ListTag pathList = args.getList("path2").get();
                    path2 = new String[pathList.size()]; // to_do verify entire list contains only strings
                    for(int i=0; i<path2.length; i++)
                        path2[i] = pathList.get(i).asString().get(); // keep asString
                }
                else
                    path2 = null;

                boolean showCancelEl = false;
                Tag cancelEl = null;
                if(args.getCompound("cancelEl").isPresent()) {
                    CompoundTag cancelNbt = args.getCompoundOrEmpty("cancelEl");
                    showCancelEl = true;
                    if(cancelNbt.contains("el")) {
                        cancelEl = cancelNbt.get("el");
                    }
                }

                String fullPath;
                if(path2 != null)
                    fullPath = path+path2[0];
                else
                    fullPath = path;

                valid = true;

                final Button saveBtn;
                {
                    saveBtn = Button.builder(Component.nullToEmpty(path2 != null ? "Done" : "Save"), btn -> {
                        if(path2 == null) {
                            if(blankTabEl != null && minecraft.player.getAbilities().instabuild) {
                                ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl));
                                if(newItem != null) {
                                    if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(newItem),path) != null) {
                                        BlackMagick.setItemMain(newItem);
                                        this.btnTab(CACHE_TAB_MAIN);
                                    }
                                }
                            }
                        }
                        else {
                            if(path2.length==1) {
                                CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                                if(blankTabEl != null)
                                    newArgs.put("overrideEl",blankTabEl);
                                createBlankTab(0,newArgs);
                            }
                            else {
                                CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                                if(blankTabEl != null)
                                    newArgs.put("overrideEl",blankTabEl);
                                ListTag pathList = new ListTag();
                                for(int i=1; i<path2.length; i++)
                                    pathList.add(StringTag.valueOf(path2[i]));
                                newArgs.put("path2",pathList);
                                createBlankTab(0,newArgs);
                            }
                        }
                        unsel();
                    }).bounds(x+240-5-40,y+5,40,20).build();
                    if(path2 == null && !minecraft.player.getAbilities().instabuild) {
                        saveBtn.active = false;
                        saveBtn.setTooltip(Tooltip.create(ERROR_CREATIVE));
                    }
                    addTabWidgetLocked(tabNum, new PosWidget(saveBtn,240-5-40,5));
                }

                Tag el = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                if(args.contains("overrideEl"))
                    el = args.get("overrideEl");
                if(el != null)
                    setEditingElement(path,el.copy(),saveBtn);
                else
                    setEditingElement(path,null,saveBtn);

                Tag el2 = null;
                PathType elType = ComponentHelper.getPathInfo(fullPath).type();
                if(path2 == null && el != null)
                    el2 = el.copy();
                else if(path2 != null) {
                    el2 = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el),fullPath);
                    setEditingElement(path,blankTabEl,saveBtn,fullPath);
                }

                if(el2 != null && !showCancelEl)
                    cancelEl = el2.copy();
                if(elType == PathType.BANNER || elType == PathType.TEXT || elType == PathType.DECIMAL_COLOR || elType == PathType.POSE) {
                    // when adding a new type here, make sure all buttons on the page handle cancelEl accordingly
                    showCancelEl = true;
                }
                final Tag cancelElCopy = cancelEl == null ? null : cancelEl.copy();

                if(path2 == null) {
                    Tag selItemComp = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                    Button w = Button.builder(Component.nullToEmpty("Cancel"),btn -> this.btnTab(CACHE_TAB_MAIN)).bounds(x+5,y+5,40,20).build();
                    if(selItemComp == null)
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep component unset")));
                    else
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep component as:\n"+BlackMagick.nbtToString(selItemComp))));
                    addTabWidgetLocked(tabNum, new PosWidget(w,5,5));
                }
                else if(showCancelEl) {
                    Button w = Button.builder(Component.nullToEmpty("Cancel"),btn -> {
                        setEditingElement(path,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),fullPath,cancelElCopy),path),saveBtn,
                            path2==null ? null : fullPath);
                        if(path2.length==1) {
                            CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(0,newArgs);
                        }
                        else {
                            CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            ListTag pathList = new ListTag();
                            for(int i=1; i<path2.length; i++)
                                pathList.add(StringTag.valueOf(path2[i]));
                            newArgs.put("path2",pathList);
                            createBlankTab(0,newArgs);
                        }
                    }).bounds(x+5,y+5,40,20).build();
                    if(cancelElCopy == null)
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep element unset")));
                    else
                        w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep element as:\n"+BlackMagick.nbtToString(cancelElCopy))));
                    addTabWidgetLocked(tabNum, new PosWidget(w,5,5));
                }

                {
                    EditBox w = new EditBox(this.font,x+5+40+5,y+5,(240-5-40)-(5+40+5)-5,20,Component.nullToEmpty(""));
                    w.setEditable(false);
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setValue(cleanPath(fullPath));
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Current path:\n"+fullPath)));
                    addTabWidgetLocked(tabNum, new PosWidget(w,5+40+5,5));
                }

                ItemStack editItem = ItemStack.EMPTY;
                if(fullPath.contains("components.")) {
                    String itemPath = fullPath.substring(0,fullPath.lastIndexOf("components."));
                    if(itemPath.length()>0)
                        editItem = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,blankTabEl),itemPath)));
                    else
                        editItem = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,blankTabEl)));
                }

                if(elType == PathType.COMPOUND) {
                    if(el2==null || el2.getId()!=Tag.TAG_COMPOUND)
                        el2 = new CompoundTag();

                    final Map<String,Set<String>> keyGroups = Maps.newHashMap();
                    final Set<String> allKeys = Sets.newHashSet();

                    final String UNKNOWN_GROUP = "Unknown";
                    keyGroups.put(UNKNOWN_GROUP,Sets.newHashSet());
                    final String REQUIRED_GROUP = "Required";
                    keyGroups.put(REQUIRED_GROUP,Sets.newHashSet());
                    final String OPTIONAL_GROUP = "Optional";
                    keyGroups.put(OPTIONAL_GROUP,Sets.newHashSet());

                    {
                        Set<String> reqKeys = ComponentHelper.getPathInfo(fullPath).keys().getRequired();
                        keyGroups.get(REQUIRED_GROUP).addAll(reqKeys);
                        allKeys.addAll(reqKeys);
                    }
                    {
                        Set<String> sortKeys = Sets.newHashSet();
                        sortKeys.addAll(ComponentHelper.getPathInfo(fullPath).keys().getOptional());
                        sortKeys.addAll(((CompoundTag)el2).keySet());
                        for(String k : sortKeys) {
                            if(!allKeys.contains(k)) {
                                PathInfo pi = ComponentHelper.getPathInfo(fullPath+"."+k);
                                String thisGroup;
                                if(pi.keyGroup()!=null)
                                    thisGroup = pi.keyGroup();
                                else if(pi.type()==PathType.UNKNOWN)
                                    thisGroup = UNKNOWN_GROUP;
                                else
                                    thisGroup = OPTIONAL_GROUP;

                                if(!keyGroups.containsKey(thisGroup))
                                    keyGroups.put(thisGroup,Sets.newHashSet());

                                keyGroups.get(thisGroup).add(k);
                                allKeys.add(k);
                            }
                        }
                    }

                    List<String> keyGroupLbls = BlackMagick.sortSet(keyGroups.keySet());
                    if(keyGroupLbls.contains(OPTIONAL_GROUP)) {
                        keyGroupLbls.remove(OPTIONAL_GROUP);
                        keyGroupLbls.add(0,OPTIONAL_GROUP);
                    }
                    if(keyGroupLbls.contains(REQUIRED_GROUP)) {
                        keyGroupLbls.remove(REQUIRED_GROUP);
                        keyGroupLbls.add(0,REQUIRED_GROUP);
                    }
                    if(keyGroupLbls.contains(UNKNOWN_GROUP)) {
                        keyGroupLbls.remove(UNKNOWN_GROUP);
                        keyGroupLbls.add(0,UNKNOWN_GROUP);
                    }

                    for(String keySetLbl : keyGroupLbls) {
                        if(!keyGroups.get(keySetLbl).isEmpty()) {
                            addTabWidgetScroll(tabNum, new RowWidget(keySetLbl));
                            for(String k : BlackMagick.sortSet(keyGroups.get(keySetLbl))) // to_do verify cast below
                                addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,k));
                        }
                    }

                    {
                        addTabWidgetScroll(tabNum, new RowWidget());
                    }
                }
                else if(elType == PathType.LIST) {
                    ListTag currentList = null;
                    if(el2 != null && el2.getId()==Tag.TAG_LIST)
                        currentList = ((ListTag)el2).copy();

                    {
                        Button keyBtn = Button.builder(Component.nullToEmpty("List ("+(currentList==null ? "null" : ("Size "+currentList.size()))+")"), btn -> {})
                            .bounds((width/2)-40,5,80,20).build();
                        keyBtn.active = false;
                        String key = fullPath;
                        if(key.contains(".") && key.length()>key.lastIndexOf(".")+1)
                            key = key.substring(key.lastIndexOf(".")+1);
                        if(key.contains("]") && key.length()>key.lastIndexOf("]")+1)
                            key = key.substring(key.lastIndexOf("]")+1);
                        if(key.startsWith("minecraft:"))
                            key = key.replaceFirst("minecraft:","");
                        keyBtn.setTooltip(Tooltip.create(getButtonTooltip(ComponentHelper.getPathInfo(fullPath),key)));
                        addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(keyBtn,(width/2)-40-x,0)}));
                    }

                    if(currentList==null)
                        currentList = new ListTag();

                    for(int i=0; i<=currentList.size(); i++) { // to_do verify cast below
                        addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,i,currentList.size()-1));
                    }

                    {
                        addTabWidgetScroll(tabNum, new RowWidget());
                    }
                }
                else if(elType == PathType.TEXT) {
                    textComponentEffectMode = -1;
                    textComponentEffectPath = null;
                    textComponentEffectBase = null;
                    String startVal = "";

                    if(el2 != null)
                        startVal = BlackMagick.nbtToString(el2); // keep asString
                    else
                        startVal = "{text:\"\"}";

                    if(args.getString("textComponentOverride").isPresent())
                        startVal = args.getString("textComponentOverride").get();

                    {
                        MultiLineEditBox w = new MultiLineEditBox(((ItemBuilder)ItemBuilder.this).minecraft.font, x+15-3, y+35, 240-36, 22*6,
                            Component.nullToEmpty(""), Component.nullToEmpty(""));
                        w.setValueListener(value -> {
                            setErrorMsg(null);
                            updateTextComponentPreview(fullPath,value);
                            if(textComponentBaseValid) {
                                setEditingElement(path,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                                    BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),fullPath,
                                    BlackMagick.nbtFromString(value)),path),saveBtn,
                                    path2==null ? null : fullPath);
                            }
                            else {
                                setErrorMsg("Invalid Text Component");
                                setEditingElement(path,blankTabEl,saveBtn,path2==null ? null : fullPath);
                            }
                        });
                        addTabWidgetLocked(tabNum, new PosWidget(w,15-3,35));
                        this.ALL_TEXT_WIDGETS.add(w);
                        w.setValue(startVal);
                    }
                    {
                        addTabWidgetLocked(tabNum, new PosWidget(Button.builder(Component.nullToEmpty("Add Text"), button -> {
                            textComponentEffectMode = 1;
                            String baseTextComponent;
                            if(textComponentBaseValid)
                                baseTextComponent = textComponentBaseText;
                            else
                                baseTextComponent = "{text:\"\"}";

                            CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                            newArgs.putString("baseTextComponent",baseTextComponent);
                            if(args.getList("path2").isPresent())
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);

                            CompoundTag cancelNbt = new CompoundTag();
                            if(cancelElCopy != null)
                                cancelNbt.put("el",cancelElCopy);
                            newArgs.put("cancelEl",cancelNbt);

                            createBlankTab(2,newArgs);
                            unsel();
                        }).bounds(x+15-3,y+35+22*6+1,60,20).build(),15-3,35+22*6+1));
                    }
                    {
                        addTabWidgetLocked(tabNum, new PosWidget(Button.builder(Component.nullToEmpty("Add Effect"), button -> {
                            textComponentEffectMode = 0;
                            String baseTextComponent;
                            if(textComponentBaseValid)
                                baseTextComponent = textComponentBaseText;
                            else
                                baseTextComponent = "{text:\"\"}";

                            CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                            newArgs.putString("baseTextComponent",baseTextComponent);
                            if(args.getList("path2").isPresent())
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);

                            CompoundTag cancelNbt = new CompoundTag();
                            if(cancelElCopy != null)
                                cancelNbt.put("el",cancelElCopy);
                            newArgs.put("cancelEl",cancelNbt);

                            createBlankTab(2,newArgs);
                            unsel();
                        }).bounds(x+15-3+60+5,y+35+22*6+1,60,20).build(),15-3+60+5,35+22*6+1));
                    }
                    if(path.contains("written_book_content")) {
                        addTabWidgetLocked(tabNum, new PosWidget(Button.builder(Component.nullToEmpty("Set Event"), button -> {
                            textComponentEffectMode = 2;
                            String baseTextComponent;
                            if(textComponentBaseValid)
                                baseTextComponent = textComponentBaseText;
                            else
                                baseTextComponent = "{text:\"\"}";

                            CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                            newArgs.putString("baseTextComponent",baseTextComponent);
                            if(args.getList("path2").isPresent())
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(2,newArgs);

                            unsel();
                        }).bounds(x+15-3+60+5+60+5,y+35+22*6+1,60,20).build(),15-3+60+5+60+5,35+22*6+1));
                    }
                }
                else if(elType == PathType.DECIMAL_COLOR) {
                    editorOutputLocked = true; // to_do verify cast below
                    addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,WidgetCacheType.TXT_DECIMAL_COLOR));
                    addTabWidgetScroll(tabNum, new RowWidgetEditor(WidgetCacheType.TXT_DECIMAL_COLOR));
                    addTabWidgetScroll(tabNum, new RowWidget("Color Editor"));

                    int rgbNum = 0;
                    colorHexTxts.get(rgbNum).clear();
                    colorDecTxts.get(rgbNum).clear();

                    for(int i=0; i<3; i++) {
                        colorRgbSliders.get(rgbNum).get(i).clear();
                        colorItemWids.get(rgbNum).get(i).clear();
                        {
                            RgbSlider w = new RgbSlider(rgbNum,i,false,true);
                            PosWidget w2 = new PosWidget(rgbItems[i],180,0);
                            addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),w2}));
                            colorRgbSliders.get(rgbNum).get(i).add(w);
                            colorItemWids.get(rgbNum).get(i).add(w2);
                        }
                    }

                    {
                        EditBox w = new EditBox(font, x+15, 0, 80, 20, Component.nullToEmpty(""));
                        w.setMaxLength(MAX_TEXT_LENGTH);
                        w.setResponder(value -> {
                            trySetColorHex(0,value,w);
                        });
                        EditBox w2 = new EditBox(font, x+15+100+5, 0, 80, 20, Component.nullToEmpty(""));
                        w2.setMaxLength(MAX_TEXT_LENGTH);
                        w2.setResponder(value -> {
                            trySetColorDec(0,value,w2);
                        });
                        addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                        colorHexTxts.get(rgbNum).add(w);
                        colorDecTxts.get(rgbNum).add(w2);
                    }

                    for(int i=0; i<3; i++) {
                        colorHsvSliders.get(i).clear();
                        {
                            RgbSlider w = new RgbSlider(rgbNum,i,false,false);
                            addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                            colorHsvSliders.get(i).add(w);
                        }
                    }

                    updateColorSets();
                    editorOutputLocked = false;
                }
                else if(elType == PathType.BANNER) {
                    if(el2==null || el2.getId()!=Tag.TAG_COMPOUND)
                        el2 = new CompoundTag();

                    CompoundTag bannerNbt = (CompoundTag)el2;
                    String bannerCol = null;
                    String bannerPat = null;
                    bannerShield = false;
                    if(editItem.is(Items.SHIELD)) {
                        bannerShield = true;
                        showBannerPreview = true;
                        bannerChangePreview.load(BlackMagick.validCompoundFromString("{ArmorItems:[{},{},{},{}],HandItems:["
                            +BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(editItem))+",{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
                    }
                    else if(editItem.is(ItemTags.BANNERS)) {
                        showBannerPreview = true;
                        bannerChangePreview.load(BlackMagick.validCompoundFromString("{ArmorItems:[{},{},{},"
                            +BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(editItem))+"],HandItems:[{},{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
                    }

                    if(bannerNbt.getString("color").isPresent())
                        bannerCol = bannerNbt.getString("color").get();
                    if(bannerNbt.getString("pattern").isPresent())
                        bannerPat = bannerNbt.getString("pattern").get();

                    List<String> bannerVals = Lists.newArrayList();
                    for(String c : ComponentHelper.LIST_DYE_COLOR.getList())
                        bannerVals.add(c);
                    for(String b : ComponentHelper.DATA_BANNER_PATTERN.getList())
                        bannerVals.add(b);

                    int row=0;
                    while(!bannerVals.isEmpty()) {
                        String[] currentVals = new String[Math.min(8,bannerVals.size())];
                        for(int i=0; i<currentVals.length; i++)
                            currentVals[i] = bannerVals.remove(0);
                        addTabWidgetScroll(tabNum,  // to_do verify cast below
                            new RowWidgetBannerRow(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,row<2,currentVals,row<2 ? bannerCol : bannerPat,cancelEl));
                        row++;
                    }

                }
                else if(elType == PathType.POSE) {
                    editorOutputLocked = true; // to_do verify cast below

                    addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn,WidgetCacheType.TXT_POSE));
                    addTabWidgetScroll(tabNum, new RowWidgetEditor(WidgetCacheType.TXT_POSE));
                    addTabWidgetScroll(tabNum, new RowWidget("Pose Editor"));

                    String[] poseParts = new String[]{"Head","Body","Right Arm","Left Arm","Right Leg","Left Leg"};
                    for(int partNum=0; partNum<poseParts.length; partNum++) {
                        {
                            poseSliderBtns.get(partNum).clear();
                            String partKey = poseParts[partNum].replace(" ","");
                            Button w2 = Button.builder(Component.nullToEmpty(partKey), btn -> {
                                poseCompound.remove(partKey);
                                updatePose();
                                unsel();
                            }).bounds(0,0,60,20).build();
                            addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w2,15,0)}));
                            poseSliderBtns.get(partNum).add(w2);

                            for(int partAxis=0; partAxis<3; partAxis++) {
                                poseSliders.get(partNum).get(partAxis).clear();
                                PoseSlider w = new PoseSlider(partKey,partAxis);
                                addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                                poseSliders.get(partNum).get(partAxis).add(w);
                            }
                        }
                    }

                    updateArmorStand(editItem);
                    showPosePreview = true;

                    editorOutputLocked = false;
                }
                else {
                    FortytwoEdit.logWarn("Fallback page created for path: "+fullPath); // to_do verify cast below
                    addTabWidgetScroll(tabNum, new RowWidgetElement(path,path2==null ? null : (ListTag)args.get("path2"),saveBtn));
                }
            }
        }
        else if(mode==2) { // text component effects
            if(args.getString("path").isPresent() && args.getString("baseTextComponent").isPresent() && textComponentEffectMode>=0 && textComponentEffectMode<=2) {
                String path = args.getString("path").get();
                String baseTextComponent = args.getString("baseTextComponent").get();

                String[] path2;
                if(args.getList("path2").isPresent() && !args.getList("path2").get().isEmpty()
                && args.getList("path2").get().get(0).getId()==Tag.TAG_STRING) {
                    ListTag pathList = (ListTag)args.get("path2");
                    path2 = new String[pathList.size()];
                    for(int i=0; i<path2.length; i++) // to_do verify entire list contains only strings
                        path2[i] = pathList.get(i).asString().get(); // keep asString
                }
                else
                    path2 = null;

                Tag cancelEl = null;
                if(args.getCompound("cancelEl").isPresent()) {
                    CompoundTag cancelNbt = args.getCompoundOrEmpty("cancelEl");
                    if(cancelNbt.contains("el")) {
                        cancelEl = cancelNbt.get("el");
                    }
                }
                final Tag cancelElCopy = cancelEl == null ? null : cancelEl.copy();

                String fullPath;
                if(path2 != null)
                    fullPath = path+path2[0];
                else
                    fullPath = path;

                textComponentEffectPath = fullPath;
                textComponentEffectBase = baseTextComponent;
                valid = true;
                {
                    Button w = Button.builder(Component.nullToEmpty("Cancel"),btn -> {
                        textComponentEffectMode = -1;
                        textComponentEffectPath = null;
                        textComponentEffectBase = null;
                        CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+path+"\"}");
                        if(args.getList("path2").isPresent())
                            newArgs.put("path2",args.get("path2"));
                        newArgs.put("textComponentOverride",args.get("baseTextComponent"));
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);

                        CompoundTag cancelNbt = new CompoundTag();
                        if(cancelElCopy != null)
                            cancelNbt.put("el",cancelElCopy);
                        newArgs.put("cancelEl",cancelNbt);

                        createBlankTab(0,newArgs);
                    }).bounds(x+5,y+5,40,20).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Keep text as:\n"+baseTextComponent)));
                    addTabWidgetLocked(tabNum, new PosWidget(w,5,5));
                }
                final Button saveBtn;
                {
                    saveBtn = Button.builder(Component.nullToEmpty("Add"), btn -> {
                        if(textComponentEffectValid) {
                            textComponentEffectMode = -1;
                            textComponentEffectPath = null;
                            textComponentEffectBase = null;
                            CompoundTag newArgs = BlackMagick.validCompoundFromString(
                                "{path:\""+path+"\"}");
                            newArgs.put("textComponentOverride",BlackMagick.nbtFromString(textComponentEffectFull));
                            if(args.getList("path2").isPresent())
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);

                            CompoundTag cancelNbt = new CompoundTag();
                            if(cancelElCopy != null)
                                cancelNbt.put("el",cancelElCopy);
                            newArgs.put("cancelEl",cancelNbt);

                            createBlankTab(0,newArgs);
                        }
                        unsel();
                    }).bounds(x+240-5-40,y+5,40,20).build();
                    addTabWidgetLocked(tabNum, new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_ADD_BTN,saveBtn),240-5-40,5));
                }

                {
                    EditBox w = new EditBox(this.font,x+5+40+5,y+5,(240-5-40)-(5+40+5)-5,20,Component.nullToEmpty(""));
                    w.setEditable(false);
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setValue(cleanPath(fullPath));
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Current path:\n"+fullPath)));
                    addTabWidgetLocked(tabNum, new PosWidget(w,5+40+5,5));
                }

                updateTextComponentPreview(fullPath,baseTextComponent);

                if(textComponentEffectMode == 0) {
                    addTabWidgetScroll(tabNum, new RowWidget("Gradient"));
                }
                else if(textComponentEffectMode == 1) {
                    addTabWidgetScroll(tabNum, new RowWidget("Text Element"));
                }
                if(textComponentEffectMode == 0 || textComponentEffectMode == 1) {
                    EditBox w = new EditBox(this.font,x+15-3,y+35,240-36,20,Component.nullToEmpty(""));
                    w.setMaxLength(MAX_TEXT_LENGTH);
                    w.setResponder(value -> {
                        updateTextComponentEffect();
                        if(textComponentEffectMode == 1) {
                            if(textComponentEffects[7]==1 || textComponentEffects[7]==2) {
                                String[] suggestions = textComponentEffects[7]==1 ? ComponentHelper.LIST_KEYBIND.getArray() :
                                    ComponentHelper.LIST_TRANSLATION_KEY.getArray();
                                suggsOnChanged(w,suggestions,null);
                            }
                            else
                                resetSuggs();
                        }
                    });
                    addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
                        new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_ENTRY,w),15,0)}));
                    this.ALL_TEXT_WIDGETS.add(w);
                }
                if(textComponentEffectMode == 0 || textComponentEffectMode == 1) {
                    RowWidget row = addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("\u00a7ll"),Component.nullToEmpty("\u00a7oo"),Component.nullToEmpty("\u00a7nn"),
                        Component.nullToEmpty("\u00a7mm"),Component.nullToEmpty("\u00a7kk")},new int[]{20,20,20,20,20},
                        new String[]{"none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r",
                        "none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r"},
                        null,true,btn -> {
                            unsel();
                            textComponentEffects[0]++;
                            if(textComponentEffects[0]>2)
                                textComponentEffects[0]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        },btn -> {
                            unsel();
                            textComponentEffects[1]++;
                            if(textComponentEffects[1]>2)
                                textComponentEffects[1]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        },btn -> {
                            unsel();
                            textComponentEffects[2]++;
                            if(textComponentEffects[2]>2)
                                textComponentEffects[2]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        },btn -> {
                            unsel();
                            textComponentEffects[3]++;
                            if(textComponentEffects[3]>2)
                                textComponentEffects[3]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        },btn -> {
                            unsel();
                            textComponentEffects[4]++;
                            if(textComponentEffects[4]>2)
                                textComponentEffects[4]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        }
                    ));
                    widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_BOLD,row.btns[0]);
                    widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_ITALIC,row.btns[1]);
                    widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_UNDERLINED,row.btns[2]);
                    widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,row.btns[3]);
                    widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED,row.btns[4]);
                }
                if(textComponentEffectMode == 0) {
                    Button w = Button.builder(Component.nullToEmpty("[Radial]"), btn -> {
                        unsel();
                        textComponentEffects[5]++;
                        if(textComponentEffects[5]>1)
                            textComponentEffects[5]=0;
                        updateTextComponentEffect();
                        updateTextComponentEffectBtns();
                    }).bounds(0,0,60,20).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Radial | Linear")));

                    Button w2 = Button.builder(Component.nullToEmpty("Swap"), btn -> {
                        unsel();
                        swapColorSets(0,1);
                    }).bounds(0,0,40,20).build();
                    w2.setTooltip(Tooltip.create(Component.nullToEmpty("Swap colors")));

                    addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
                        new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_RADIAL,w),15,0),
                        new PosWidget(w2,15+60+5,0)}));
                }
                else if(textComponentEffectMode == 1) {
                    Button w = Button.builder(Component.nullToEmpty("Color [Vanilla]"), btn -> {
                        unsel();
                        textComponentEffects[6]++;
                        if(textComponentEffects[6]>2)
                            textComponentEffects[6]=0;
                        updateTextComponentEffect();
                        updateTextComponentEffectBtns();
                    }).bounds(0,0,80,20).build();
                    w.setTooltip(Tooltip.create(Component.nullToEmpty("Vanilla | RGB | None")));
                    EditBox w2 = new EditBox(this.font,0,0,240-36-80-5,20,Component.nullToEmpty(""));
                    w2.setMaxLength(MAX_TEXT_LENGTH);
                    w2.setResponder(value -> {
                        if(textComponentEffects[6]==1) {
                            boolean validColor = false;
                            for(String f : ComponentHelper.LIST_FORMATTING_COLOR.getList())
                                if(f.equals(value))
                                    validColor = true;

                            if(validColor)
                                textComponentLastColor = value;
                            suggsOnChanged(w2,ComponentHelper.LIST_FORMATTING_COLOR.getArray(),null);
                            updateTextComponentEffect();
                        }
                        else
                            resetSuggs();

                        if(textComponentEffects[6]==2)
                            trySetColorHex(0,value,w2);
                    });
                    addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{
                        new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_COLOR_BTN,w),15,0),
                        new PosWidget(widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_COLOR_TXT,w2),15+80+5,0)}));
                    colorHexTxts.get(0).clear();
                    colorHexTxts.get(0).add(w2);
                }
                if(textComponentEffectMode == 0) {
                    for(int num=0; num<3; num++) {
                        RgbSlider w = new RgbSlider(0,num,true,true);
                        RgbSlider w2 = new RgbSlider(1,num,true,true);
                        addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                        colorRgbSliders.get(0).get(num).clear();
                        colorRgbSliders.get(1).get(num).clear();
                        colorRgbSliders.get(0).get(num).add(w);
                        colorRgbSliders.get(1).get(num).add(w2);
                    }
                    {
                        EditBox w = new EditBox(font, x+15, 0, 60, 20, Component.nullToEmpty(""));
                        w.setMaxLength(MAX_TEXT_LENGTH);
                        w.setResponder(value -> {
                            trySetColorHex(0,value,w);
                        });
                        EditBox w2 = new EditBox(font, x+15+100+5, 0, 60, 20, Component.nullToEmpty(""));
                        w2.setMaxLength(MAX_TEXT_LENGTH);
                        w2.setResponder(value -> {
                            trySetColorHex(1,value,w2);
                        });
                        addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                        colorHexTxts.get(0).clear();
                        colorHexTxts.get(1).clear();
                        colorHexTxts.get(0).add(w);
                        colorHexTxts.get(1).add(w2);
                    }
                    addTabWidgetScroll(tabNum, new RowWidget("Shadow:",9));
                    addTabWidgetScroll(tabNum, new RowWidget("Font:",10));
                }
                else if(textComponentEffectMode == 1) {
                    for(int num=0; num<3; num++) {
                        RgbSlider w = new RgbSlider(0,num,false,true);
                        addTabWidgetScroll(tabNum, new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                        colorRgbSliders.get(0).get(num).clear();
                        colorRgbSliders.get(0).get(num).add(w);
                    }
                    addTabWidgetScroll(tabNum, new RowWidget("Shadow:",9));
                    addTabWidgetScroll(tabNum, new RowWidget("Font:",10));
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget(new Component[]{Component.nullToEmpty("[Text]")},
                        new int[]{80},new String[]{"Text | Keybind | Translate"},null,true,btn -> {
                            unsel();
                            textComponentEffects[7]++;
                            if(textComponentEffects[7]>2)
                                textComponentEffects[7]=0;
                            updateTextComponentEffect();
                            updateTextComponentEffectBtns();
                        }));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_EFFECT_TEXT_MODE,row.btns[0]);
                    }
                    {
                        addTabWidgetScroll(tabNum, new RowWidget("Translations Only"));
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("Params:",3));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_WITH,row.txts[0]);
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("Fallback:",4));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_TRANSLATION_FALLBACK,row.txts[0]);
                    }
                }
                else if(textComponentEffectMode == 2) {
                    {
                        addTabWidgetScroll(tabNum, new RowWidget("clickEvent"));
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("action:",5));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_ACTION,row.txts[0]);
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("value:",6));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_CLICK_EVENT_VALUE,row.txts[0]);
                    }
                    {
                        addTabWidgetScroll(tabNum, new RowWidget("hoverEvent"));
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("action:",7));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_ACTION,row.txts[0]);
                    }
                    {
                        RowWidget row = addTabWidgetScroll(tabNum, new RowWidget("contents:",8));
                        widgetCacheAdd(WidgetCacheType.TEXT_COMPONENT_HOVER_EVENT_CONTENTS,row.txts[0]);
                    }
                }
                {
                    addTabWidgetScroll(tabNum, new RowWidget());
                }
                updateTextComponentEffectBtns();
                updateColorSets();
            }
        }

        if(!valid)
            btnTab(CACHE_TAB_MAIN);
        else
            btnTab(tabNum);
        resetSuggs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleEntry}
     */
    private static abstract class TabWidgetEntry extends ContainerObjectSelectionList.Entry<TabWidgetEntry> {
        public TabWidgetEntry() {}
    }
    /**
     * Modified from {@link net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen.RuleList}
     */
    private class TabWidget extends ContainerObjectSelectionList<TabWidgetEntry> {
        public TabWidget(final int tab) {
            super(ItemBuilder.this.minecraft, ItemBuilder.this.width-30, ItemBuilder.this.backgroundHeight-32-5, ItemBuilder.this.y+32,
                (tab == CACHE_TAB_INV || tab == CACHE_TAB_SAVED) ? 20 : 22);

            for(RowWidget row : TAB_WIDGETS_SCROLL.get(tab))
                this.addEntry((TabWidgetEntry)row);
        }

        @Override
        protected void renderListSeparators(GuiGraphics context) {}

        @Override
        protected void renderListBackground(GuiGraphics context) {}
    }

    protected class RowWidget extends TabWidgetEntry {

        protected final List<AbstractWidget> children;
        protected Button[] btns;
        protected int[] btnX;
        protected int[] btnY = null;
        protected EditBox[] txts;
        protected int[] txtX;
        protected String lbl;
        protected boolean lblCentered = false;
        protected int lblColor = LABEL_COLOR;
        protected ItemStack displayItem = null;//to_do remove
        protected int displayItemXoff = 0;
        protected PosWidget[] wids;

        /**
         * Blank row
         */
        public RowWidget() {
            super();
            this.children = Lists.newArrayList();
            setup();
        }

        /**
         * btn(size) txt
         */
        public RowWidget(String name, String tooltip, OnPress onPress, String[] suggestions, boolean survival) {
            super();
            this.children = Lists.newArrayList();
            setup();

            int size = sizeFromName(name);

            this.btns = new Button[]{Button.builder(Component.nullToEmpty(name), onPress).bounds(ItemBuilder.this.x+15,5,size,20).build()};
            this.btnX = new int[]{15};
            if(tooltip != null)
                this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty(tooltip)));
            if(!minecraft.player.getAbilities().instabuild && !survival)
                this.btns[0].active = false;
            this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Component.nullToEmpty(""))};
            this.txtX = new int[]{15+5+size};
            this.txts[0].setResponder(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setTextColor(TEXT_COLOR);
                    //ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setTextColor(LABEL_COLOR);
                    //ItemBuilder.this.markSaved(this.txts[0]);
                }
                suggsOnChanged(this.txts[0],suggestions,null);
            });
            this.txts[0].setMaxLength(MAX_TEXT_LENGTH);
            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
            }
        }

        /**
         * Centered lbl
         */
        public RowWidget(String label) {
            super();
            this.children = Lists.newArrayList();
            setup();

            lbl = label;
            lblCentered = true;
        }

        /**
         * Used for text component effects tab
         * lbl(size) txt [custom suggs]
         */
        public RowWidget(String name, int suggsNum) { // to_do remove and replace with real solution
            super();
            this.children = Lists.newArrayList();
            setup();

            int size = sizeFromName(name);

            this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Component.nullToEmpty(""))};
            this.txtX = new int[]{15+5+size};

            if(suggsNum==9)
                this.txts[0].setValue(textComponentShadowColor);
            else if(suggsNum==10)
                this.txts[0].setValue(textComponentFont);

            this.txts[0].setResponder(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setTextColor(TEXT_COLOR);
                    //ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setTextColor(LABEL_COLOR);
                    //ItemBuilder.this.markSaved(this.txts[0]);
                }
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(minecraft, this.txts[0], font);
                    switch(suggsNum) {
                        case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{text:\"\"}]"}); break;
                        case 4: resetSuggs(); break;
                        case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                        case 6: resetSuggs(); break;
                        case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                        case 8: suggs.setSuggestions(new String[]
                                {"{text:\"\"}","{\"id\":\"stone\"}","{\"id\":\"bundle\",\"components\":\"{bundle_content:[{id:\\\"stone\\\"}]}\"}"}); break;
                        case 9: suggs.setSuggestions(new String[]{"0","[0.0,0.0,0.0,0.0]"}); break;
                        case 10: suggs.setSuggestions(ComponentHelper.ASSETS_FONT.getArray()); break;
                        default: resetSuggs(); break;
                    }
                }
                else {
                    if(suggs != null)
                        suggs.refresh();
                    else {
                        resetSuggs();
                        suggs = new TextSuggestor(minecraft, this.txts[0], font);
                        switch(suggsNum) {
                            case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{text:\"\"}]"}); break;
                            case 4: resetSuggs(); break;
                            case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                            case 6: resetSuggs(); break;
                            case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                            case 8: suggs.setSuggestions(new String[]
                                {"{text:\"\"}","{\"id\":\"stone\"}","{\"id\":\"bundle\",\"components\":\"{bundle_content:[{id:\\\"stone\\\"}]}\"}"}); break;
                            case 9: suggs.setSuggestions(new String[]{"0","[0.0,0.0,0.0,0.0]"}); break;
                            case 10: suggs.setSuggestions(ComponentHelper.ASSETS_FONT.getArray()); break;
                            default: resetSuggs(); break;
                        }
                    }
                }

                if(suggsNum==9)
                    textComponentShadowColor = value;
                else if(suggsNum==10)
                    textComponentFont = value;

                if(suggsNum >= 3 && suggsNum <= 10) {
                    updateTextComponentEffect();
                }
            });
            this.txts[0].setMaxLength(MAX_TEXT_LENGTH);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
            }
            lbl = name;
        }

        /**
         * btn...(sizes) txt...(sizes)
         */
        public RowWidget(Component[] names, int[] sizes, String[] tooltips, String[][] suggestions, boolean survival, OnPress... onPressActions) {
            super();
            this.children = Lists.newArrayList();
            setup();

            if(names.length <= sizes.length && names.length == tooltips.length && names.length == onPressActions.length) {
                this.btns = new Button[names.length];
                this.btnX = new int[names.length];
                this.txts = new EditBox[sizes.length-this.btns.length];
                this.txtX = new int[this.txts.length];

                int currentX = 15;
                for(int i=0; i<this.btns.length; i++) {
                    this.btnX[i] = currentX;
                    this.btns[i] = Button.builder(names[i], onPressActions[i]).bounds(currentX,5,sizes[i],20).build();
                    currentX += 5 + sizes[i];
                    if(tooltips[i] != null)
                        this.btns[i].setTooltip(Tooltip.create(Component.nullToEmpty(tooltips[i])));

                    if(!minecraft.player.getAbilities().instabuild && !survival)
                        this.btns[i].active = false;
                    this.children.add(this.btns[i]);
                }
                for(int i=0; i<this.txts.length; i++) {
                    this.txtX[i] = currentX;
                    this.txts[i] = new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font, currentX, 5,
                        sizes[this.btns.length+i], 20, Component.nullToEmpty(""));
                    currentX += sizes[this.btns.length+i];

                    final int ii = i;
                    this.txts[i].setResponder(value -> {
                        if(value != null && !value.equals("")) {
                            this.txts[ii].setTextColor(TEXT_COLOR);
                            //ItemBuilder.this.markUnsaved(this.txts[ii]);
                        }
                        else {
                            this.txts[ii].setTextColor(LABEL_COLOR);
                            //ItemBuilder.this.markSaved(this.txts[ii]);
                        }

                        String[] suggsArr = null;
                        if(suggestions != null && suggestions.length > ii && suggestions[ii] != null)
                            suggsArr = suggestions[ii];
                        suggsOnChanged(this.txts[ii],suggsArr,null);
                    });
                    this.txts[i].setMaxLength(MAX_TEXT_LENGTH);

                    this.children.add(this.txts[i]);
                    ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
                }
            }
        }

        /**
         * PosWidgets
         */
        public RowWidget(PosWidget[] p) {
            super();
            this.children = Lists.newArrayList();
            setup();

            wids = p;

            for(int i=0; i<p.length; i++) {
                if(p[i].w != null) {
                    this.children.add(p[i].w);
                    if(p[i].w instanceof EditBox || p[i].w instanceof MultiLineEditBox) {
                        ALL_TEXT_WIDGETS.add(p[i].w);
                    }
                    else if(p[i].w instanceof AbstractSliderButton) {
                        ALL_SLIDER_WIDGETS.add(p[i].w);
                    }
                }
            }
        }

        /**
         * Get button size based on text (between 40 and 100 pixels).
         * Size is a multiple of 20.
         * 
         * @param text
         * @return width of button
         */
        protected int sizeFromName(String text) {
            int size = 40;
            int min = ItemBuilder.this.font.width(text)+4;
            while(min>size && size<100) {
                size += 20;
            }
            return size;
        }

        protected void setup() {
            btns = new Button[0];
            btnX = new int[0];
            txts = new EditBox[0];
            txtX = new int[0];
            wids = new PosWidget[0];
        }

        public String[] btn() {
            String[] texts = new String[this.txts.length];
            for(int i=0; i<texts.length; i++) {
                this.txts[i].setTextColor(LABEL_COLOR);
                ItemBuilder.this.markSaved(this.txts[i]);
                texts[i] = this.txts[i].getValue();
            }
            ItemBuilder.this.unsel();
            return texts;
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
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for(int i=0; i<btns.length; i++) {
                this.btns[i].setX(x+this.btnX[i]);
                if(this.btnY == null)
                    this.btns[i].setY(y);
                else
                    this.btns[i].setY(y+this.btnY[i]);
                this.btns[i].render(context, mouseX, mouseY, tickDelta);
            }
            for(int i=0; i<txts.length; i++) {
                this.txts[i].setX(x+this.txtX[i]);
                this.txts[i].setY(y);
                this.txts[i].render(context, mouseX, mouseY, tickDelta);
            }
            for(int i=0; i<wids.length; i++) {
                if(this.wids[i].w != null) {
                    this.wids[i].w.setX(x+this.wids[i].x);
                    this.wids[i].w.setY(y+this.wids[i].y);
                    this.wids[i].w.render(context, mouseX, mouseY, tickDelta);
                }
                if(this.wids[i].s != null) {
                    drawItem(context,this.wids[i].s,x+15+2+this.wids[i].x,y+2+this.wids[i].y);//to_do draw item for dyed tab item display
                }
            }
            if(lbl != null) {
                if(lblCentered)
                    context.drawCenteredString(ItemBuilder.this.font, Component.nullToEmpty(this.lbl), ItemBuilder.this.width/2, y+6, lblColor);
                else
                    context.drawString(ItemBuilder.this.font, Component.nullToEmpty(this.lbl), ItemBuilder.this.x+15+3, y+6, lblColor);
            }
            if(displayItem != null) {
                drawItem(context,displayItem,x+15+2+displayItemXoff,y+2);//to_do draw item for component widget
            }

        }

    }

    class RowWidgetComponent extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.create(Component.nullToEmpty("Set component"));
        private static final ItemStack DEFAULT_COMPONENT_ICON = FortytwoEdit.ITEM_QUESTION;
        private static final int ROW_LEFT_ICON = 10;

        /**
         * For use in components screen only.
         * Row contains txt/btn, btns to edit/delete/add, or btns to set trinary/binary values.
         * 
         * @param path nbt path in format: components.minecraft:foo.bar.list[0]
         */
        public RowWidgetComponent(String path) {
            super();

            PathInfo pi = ComponentHelper.getPathInfo(path);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==Tag.TAG_STRING;
            Component btnTt = getButtonTooltip(pi,path);

            String keyBtnTxt = cleanPath(path);
            if(keyBtnTxt.equals("enchantment_glint_override"))
                keyBtnTxt = "ench glint override";
            int size = sizeFromName(keyBtnTxt);
            if((size>80) && (pi.type() == PathType.TRINARY || pi.type() == PathType.TOOLTIP_UNIT))
                size = 80;
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {})
                .bounds(ItemBuilder.this.x+(ROW_LEFT+ROW_LEFT_ICON),5,size,20).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(btnTt));

            displayItem = pi.icon() == null ? DEFAULT_COMPONENT_ICON : pi.icon();
            displayItemXoff = -9;

            if(ComponentHelper.isComplex(pi.type())) {

                Tag startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                final String startVal = (isString && startEl != null && startEl.getId() == Tag.TAG_STRING) ? startEl.asString().get() : BlackMagick.nbtToString(startEl); // keep asString

                this.btns = new Button[]{
                keyBtn,
                Button.builder(getButtonText(path,startEl), btn -> {
                    createBlankTab(0,BlackMagick.validCompoundFromString("{path:\""+path+"\"}"));
                    unsel();
                }).bounds(ItemBuilder.this.x+(ROW_LEFT+ROW_LEFT_ICON)+size+5,5,ROW_RIGHT-(ROW_LEFT+ROW_LEFT_ICON)-20-size-5,20).build(),
                Button.builder(Component.nullToEmpty(startVal.equals("") ? "+" : "X"), btn -> {
                    if(startVal.equals("")) {
                        createBlankTab(0,BlackMagick.validCompoundFromString("{path:\""+path+"\"}"));
                    }
                    else if(path.startsWith("components.")) {
                        String comp = path.replaceFirst("components\\.","");
                        if(!comp.startsWith("!"))
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new CompoundTag())));
                        else
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null)));
                    }
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-20,5,20,20).build()};
                this.btnX = new int[]{(ROW_LEFT+ROW_LEFT_ICON),(ROW_LEFT+ROW_LEFT_ICON)+size+5,ROW_RIGHT-20};
                if(!startVal.equals("")) {
                    this.btns[1].setTooltip(Tooltip.create(Component.nullToEmpty("Edit component:\n"+startVal)));
                    this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                }
                else
                    this.btns[1].active = false;

                this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty(startVal.equals("") ? "Create component" : "Delete component")));
                if(!minecraft.player.getAbilities().instabuild && !startVal.equals("")) {
                    this.btns[2].active = false;
                    this.btns[2].setTooltip(Tooltip.create(ERROR_CREATIVE));
                }

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
            }
            else if(pi.type() == PathType.UNIT) {
                final int startVal;
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path) != null) {
                    startVal = 1;
                }
                else
                    startVal = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    String comp = path.replaceFirst("components\\.","");
                    if(!comp.startsWith("!"))
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new CompoundTag())));
                    else
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null)));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,new CompoundTag())));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{(ROW_LEFT+ROW_LEFT_ICON),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                this.btns[startVal+1].active = false;

                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!minecraft.player.getAbilities().instabuild)
                            this.btns[i].setTooltip(Tooltip.create(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type() == PathType.TRINARY) {
                final int startVal;
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path) != null) {
                    String tempVal = BlackMagick.nbtToString(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path));
                    if(tempVal.equals("1b") || tempVal.equals("1"))
                        startVal = 2;
                    else
                        startVal = 1;
                }
                else
                    startVal = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("Unset"), btn -> {
                    String comp = path.replaceFirst("components\\.","");
                    if(!comp.startsWith("!"))
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new CompoundTag())));
                    else
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null)));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,ByteTag.ZERO)));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,ByteTag.ONE)));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{(ROW_LEFT+ROW_LEFT_ICON),ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                this.btns[startVal+1].active = false;

                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!minecraft.player.getAbilities().instabuild)
                            this.btns[i].setTooltip(Tooltip.create(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type() == PathType.TOOLTIP_UNIT) {
                final int startVal;
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path) != null) {
                    String tempVal = BlackMagick.nbtToString(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path));
                    if(tempVal.equals("{show_in_tooltip:0b}") || tempVal.equals("{show_in_tooltip:0}"))
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
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    String comp = path.replaceFirst("components\\.","");
                    if(!comp.startsWith("!"))
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new CompoundTag())));
                    else
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null)));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("Hide"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,
                        BlackMagick.validCompoundFromString("{show_in_tooltip:0b}"))));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("Show"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,new CompoundTag())));
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{(ROW_LEFT+ROW_LEFT_ICON),ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                this.btns[startVal+1].active = false;

                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!minecraft.player.getAbilities().instabuild)
                            this.btns[i].setTooltip(Tooltip.create(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else { // inline component
                String[] baseSuggestions = pi.suggs() == null ? null : pi.suggs().getArray();

                Tag tempEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                final String startVal = (isString && tempEl != null && tempEl.getId() == Tag.TAG_STRING) ? tempEl.asString().get() : BlackMagick.nbtToString(tempEl); // keep asString

                this.btns = new Button[]{Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {
                    String inp = this.txts[0].getValue();
                    if(path.equals("id")) {
                        if(inp.equals("")) {
                            BlackMagick.setItemMain(new ItemStack(Items.STONE));
                        }
                        else if(inp.equals("air") || inp.equals("minecraft:air")) {
                            BlackMagick.setItemMain(ItemStack.EMPTY);
                        }
                        else {
                            Tag el = (isString && inp.length()>0) ? StringTag.valueOf(inp) : BlackMagick.nbtFromString(inp);
                            if(el != null) {
                                ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbtExclusive(selItem),path,el));
                                if(!newItem.isEmpty())
                                    BlackMagick.setItemMain(newItem);
                            }
                        }
                    }
                    else {
                        Tag el = (isString && inp.length()>0) ? StringTag.valueOf(inp) : BlackMagick.nbtFromString(inp);
                        if(inp.equals("") || el != null) {
                            ItemStack newItem;
                            if(inp.equals("")) {
                                String comp = path.replaceFirst("components\\.","");
                                if(!comp.startsWith("!"))
                                    newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                                        BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new CompoundTag()));
                                else
                                    newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null));
                            }
                            else
                                newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el));
                            if(!newItem.isEmpty())
                                BlackMagick.setItemMain(newItem);
                        }
                    }
                    btnTab(CACHE_TAB_MAIN); //careful removing, may be required for some components
                    unsel();
                }).bounds(ItemBuilder.this.x+(ROW_LEFT+ROW_LEFT_ICON),5,size,20).build()};
                this.btnX = new int[]{(ROW_LEFT+ROW_LEFT_ICON)};

                this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                    ItemBuilder.this.x+(ROW_LEFT+ROW_LEFT_ICON)+5+size, 5, ROW_RIGHT-(ROW_LEFT+ROW_LEFT_ICON)-size-5, 20, Component.nullToEmpty(""))};
                this.txtX = new int[]{(ROW_LEFT+ROW_LEFT_ICON)+5+size};
                this.txts[0].setMaxLength(MAX_TEXT_LENGTH);

                this.txts[0].setResponder(value -> {
                    setErrorMsg(null);
                    MutableComponent newBtnTt = btnTt.copy();
                    this.btns[0].active = false;

                    boolean noUnsaved = false;
                    boolean removeItem = false;
                    String keyType = "component";
                    if(path.equals("count") && value.equals(""))
                        value = "1";
                    else if(path.equals("id")) {
                        if(value.equals("")) {
                            removeItem = true;
                            value = "stone";
                            if(startVal.equals(""))
                                noUnsaved = true;
                        }
                        else if(value.equals("air") || value.equals("minecraft:air"))
                            value = "";
                    }

                    if((value != null && !value.equals(startVal)) || removeItem) {
                        this.txts[0].setTextColor(TEXT_COLOR);
                        if(!noUnsaved)
                            ItemBuilder.this.markUnsaved(this.txts[0]);
                        else
                            ItemBuilder.this.markSaved(this.txts[0]);

                        if(path.startsWith("components.") && value.length()>0) {
                            Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromString(value);
                            if(el != null)
                                setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToString(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el)),inpError));
                            else {
                                try {
                                    ItemArgument.item(BlackMagick.getCommandRegistries()).parse(
                                        new StringReader("stone["+path.replaceFirst("components\\.","")+"="+value+"]"));
                                } catch(Exception ex) {
                                    if(ex instanceof CommandSyntaxException) {
                                        setErrorMsg(((CommandSyntaxException)ex).getMessage());
                                        if(inpError.contains(" at position ")) {
                                            setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                        }
                                    }
                                }
                            }
                        }
                        else if(path.equals("id")) {
                            keyType = "id";
                            if(value.isEmpty())
                                value = "stone";
                            try {
                                ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(value));
                            } catch(Exception ex) {
                                if(ex instanceof CommandSyntaxException) {
                                    setErrorMsg(((CommandSyntaxException)ex).getMessage());
                                    if(inpError.contains(" at position ")) {
                                        setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                    }
                                }
                            }
                        }
                        else if(path.equals("count")) {
                            keyType = "count";
                            if(value.length()>0) {
                                try {
                                    IntegerArgumentType.integer(1,selItem.getMaxStackSize()).parse(new StringReader(value));
                                } catch(Exception ex) {
                                    if(ex instanceof CommandSyntaxException) {
                                        setErrorMsg(((CommandSyntaxException)ex).getMessage());
                                        if(inpError.contains(" at position ")) {
                                            setErrorMsg(inpError.substring(0,inpError.indexOf(" at position ")));
                                        }
                                    }
                                }
                                try {
                                    Integer.parseInt(value);
                                } catch(Exception ex) {
                                    setErrorMsg("Expected integer");
                                }
                            }
                        }

                        if(inpError != null) {
                            this.txts[0].setTextColor(ERROR_COLOR);
                        }
                        else {
                            if(minecraft.player.getAbilities().instabuild) {
                                if(value.isEmpty())
                                    newBtnTt = Component.empty().append(Component.nullToEmpty("Remove "+keyType+"\n\n")).append(btnTt);
                                else
                                    newBtnTt = Component.empty().append(Component.nullToEmpty("Set "+keyType+"\n\n")).append(btnTt);
                                this.btns[0].active = true;
                            }
                            else
                                newBtnTt = Component.empty().append(ERROR_CREATIVE).append("\n\n").append(btnTt);
                        }
                    }
                    else {
                        this.txts[0].setTextColor(LABEL_COLOR);
                        ItemBuilder.this.markSaved(this.txts[0]);
                    }

                    suggsOnChanged(this.txts[0],baseSuggestions,startVal);
                    this.btns[0].setTooltip(Tooltip.create(newBtnTt));
                });

                this.btns[0].setTooltip(Tooltip.create(btnTt));
                this.txts[0].setValue(startVal);

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
                }
            }

        }

    }

    class RowWidgetElement extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.create(Component.nullToEmpty("Set element"));

        /**
         * Row to edit any element in base compound.
         * Contains button to set and txt to input (or others depending on path and key).
         * Element is removed on set when txt is empty.
         */
        public RowWidgetElement(String blankElPath, ListTag path2, Button saveBtn, String key) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString().get(); // keep asString
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"."+key;

            PathInfo pi = ComponentHelper.getPathInfo(fullPath);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==Tag.TAG_STRING;

            Tag startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
            final String startVal = (isString && startEl != null && startEl.getId() == Tag.TAG_STRING) ? startEl.asString().get() : BlackMagick.nbtToString(startEl); // keep asString
            Tag currentEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
            final String currentVal = (isString && currentEl != null && currentEl.getId() == Tag.TAG_STRING) ? currentEl.asString().get() : BlackMagick.nbtToString(currentEl); // keep asString

            String keyBtnTxt = key.replace("minecraft:","");
            int size = sizeFromName(keyBtnTxt);
            if((size>80) && (pi.type() == PathType.TRINARY || pi.type() == PathType.TOOLTIP_UNIT))
                size = 80;
            Button keyBtn = Button.builder(Component.nullToEmpty(keyBtnTxt), btn -> {})
                .bounds(ItemBuilder.this.x+ROW_LEFT,5,size,20).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.create(getButtonTooltip(pi,key)));

            if(ComponentHelper.isComplex(pi.type())) {
                this.btns = new Button[]{
                keyBtn,
                Button.builder(getButtonText(fullPath,currentEl), btn -> {
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    ListTag newPath2 = new ListTag();
                    newPath2.add(StringTag.valueOf(currentPath2+"."+key));
                    if(path2 != null) {
                        for(int i=0; i<path2.size(); i++)
                            newPath2.add(path2.get(i));
                    }
                    newArgs.put("path2",newPath2);
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_LEFT+size+5,5,ROW_RIGHT-ROW_LEFT-20-size-5,20).build(),
                Button.builder(Component.nullToEmpty(currentVal.equals("") ? "+" : "X"), btn -> {
                    if(currentVal.equals("")) {
                        CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                        ListTag newPath2 = new ListTag();
                        newPath2.add(StringTag.valueOf(currentPath2+"."+key));
                        if(path2 != null) {
                            for(int i=0; i<path2.size(); i++)
                                newPath2.add(path2.get(i));
                        }
                        newArgs.put("path2",newPath2);
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                    }
                    else {
                        setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                            path2==null ? null : pagePath);
                        CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                        if(path2 != null) {
                            newArgs.put("path2",path2);
                        }
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                    }
                    unsel();
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-20,5,20,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_LEFT+size+5,ROW_RIGHT-20};
                if(!currentVal.equals("")) {
                    this.btns[1].setTooltip(Tooltip.create(Component.nullToEmpty("Edit element:\n"+currentVal)));
                    this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                }
                else
                    this.btns[1].active = false;
                this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty(currentVal.equals("") ? "Create element" : "Delete element")));

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
            }
            else if(pi.type()==PathType.TRINARY) {
                int selBtn;
                if(currentVal != null && currentVal.length()>0) {
                    if(currentVal.equals("1b") || currentVal.equals("1"))
                        selBtn = 2;
                    else
                        selBtn = 1;
                }
                else
                    selBtn = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("Unset"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,ByteTag.ZERO),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,ByteTag.ONE),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active = false;
                    else if(i>0)
                        this.btns[i].setTooltip(TT_SET);
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type()==PathType.TOOLTIP_UNIT) {
                int selBtn;
                if(currentVal != null && currentVal.length()>0) {
                    if(currentVal.equals("{show_in_tooltip:0b}") || currentVal.equals("{show_in_tooltip:0}"))
                        selBtn = 1;
                    else
                        selBtn = 2;
                }
                else
                    selBtn = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("Hide"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,
                        BlackMagick.validCompoundFromString("{show_in_tooltip:0b}")),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("Show"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,new CompoundTag()),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active = false;
                    else if(i>0)
                        this.btns[i].setTooltip(TT_SET);
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type()==PathType.UNIT) {
                int selBtn;
                if(currentVal != null && currentVal.length()>0) {
                    selBtn = 1;
                }
                else
                    selBtn = 0;

                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new Button[]{
                keyBtn,
                Button.builder(Component.nullToEmpty("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                Button.builder(Component.nullToEmpty("True"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,new CompoundTag()),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};

                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active = false;
                    else if(i>0)
                        this.btns[i].setTooltip(TT_SET);
                    this.children.add(this.btns[i]);
                }
            }
            else {
                String[] baseSuggestions = pi.suggs() == null ? null : pi.suggs().getArray();

                this.btns = new Button[]{keyBtn};
                this.btnX = new int[]{ROW_LEFT};

                this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                    ItemBuilder.this.x+ROW_LEFT+5+size, 5, ROW_RIGHT-ROW_LEFT-size-5, 20, Component.nullToEmpty(""))};
                this.txtX = new int[]{ROW_LEFT+5+size};
                this.txts[0].setMaxLength(MAX_TEXT_LENGTH);

                this.txts[0].setResponder(value -> {
                    setErrorMsg(null);

                    Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromString(value);
                    if(value.isEmpty())
                        el = null;

                    if((value != null && !value.equals(startVal))) {
                        this.txts[0].setTextColor(TEXT_COLOR);

                        if(value.length()>0 && el==null) {
                            setErrorMsg("Invalid element");
                        }

                        if(inpError != null) {
                            this.txts[0].setTextColor(ERROR_COLOR);
                        }
                    }
                    else {
                        this.txts[0].setTextColor(LABEL_COLOR);
                    }

                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                        path2==null ? null : pagePath);

                    suggsOnChanged(this.txts[0],baseSuggestions,startVal);

                });

                this.txts[0].setValue(currentVal);

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
                }
            }

        }

        /**
         * Row to edit any element in a list.
         * Contains button to delete, clone, move, and edit the element in the list.
         * Add one of these for each index of the list, and also for index = NbtList.size()
         */
        public RowWidgetElement(String blankElPath, ListTag path2, Button saveBtn, int index, int maxIndex) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString().get(); // keep asString
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"["+index+"]";

            if(index>=0 && index<=maxIndex) {

                PathInfo pi = ComponentHelper.getPathInfo(fullPath);
                boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==Tag.TAG_STRING;

                Tag startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
                final String startVal = (isString && startEl != null && startEl.getId() == Tag.TAG_STRING) ? startEl.asString().get() : BlackMagick.nbtToString(startEl); // keep asString
                Tag currentEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
                final String currentVal = (isString && currentEl != null && currentEl.getId() == Tag.TAG_STRING) ? currentEl.asString().get() : BlackMagick.nbtToString(currentEl); // keep asString

                Component btnTxt = null;
                int listElWidth = ROW_RIGHT-ROW_LEFT-15-15-15;

                if(ComponentHelper.isComplex(pi.type())) {
                    btnTxt = getButtonText(fullPath,currentEl);
                }
                else {
                    String[] baseSuggestions = pi.suggs() == null ? null : pi.suggs().getArray();
                    final String[] suggestions;
                    if(baseSuggestions == null) {
                        if(startVal.length()>0)
                            suggestions = new String[]{startVal};
                        else
                            suggestions = null;
                    }
                    else {
                        if(startVal.length()>0) {
                            suggestions = new String[baseSuggestions.length+1];
                            suggestions[0]=startVal;
                            for(int i=1; i<suggestions.length; i++)
                                suggestions[i] = baseSuggestions[i-1];
                        }
                        else
                            suggestions = baseSuggestions;
                    }

                    this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                        ItemBuilder.this.x+ROW_LEFT, 5, listElWidth, 20, Component.nullToEmpty(""))};
                    this.txtX = new int[]{ROW_LEFT};
                    this.txts[0].setMaxLength(MAX_TEXT_LENGTH);

                    this.txts[0].setResponder(value -> {
                        setErrorMsg(null);

                        Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromString(value);

                        if((value != null && !value.equals(startVal)))
                            this.txts[0].setTextColor(TEXT_COLOR);
                        else
                            this.txts[0].setTextColor(LABEL_COLOR);

                        if(el==null)
                            setErrorMsg("Invalid element");
                        if(inpError != null) {
                            this.txts[0].setTextColor(ERROR_COLOR);
                        }

                        if(el != null)
                            setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                                BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                                path2==null ? null : pagePath);

                        suggsOnChanged(this.txts[0],baseSuggestions,startVal);
                    });

                    this.txts[0].setValue(currentVal);
                }

                int btnOffset = 0;
                if(btnTxt == null) {
                    this.btns = new Button[4];
                    this.btnX = new int[4];
                    this.btnY = new int[]{0,0,0,10};
                }
                else {
                    this.btns = new Button[5];
                    this.btnX = new int[5];
                    this.btnY = new int[]{0,0,0,0,10};
                    btnOffset = 1;

                    this.btns[0] = Button.builder(btnTxt, btn -> {
                        CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                        ListTag newPath2 = new ListTag();
                        newPath2.add(StringTag.valueOf(currentPath2+"["+index+"]"));
                        if(path2 != null) {
                            for(int i=0; i<path2.size(); i++)
                                newPath2.add(path2.get(i));
                        }
                        newArgs.put("path2",newPath2);
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                        unsel();
                    }).bounds(ItemBuilder.this.x+ROW_LEFT,5,listElWidth,20).build();
                    this.btnX[0] = ROW_LEFT;
                    this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty("Edit element:\n"+currentVal)));
                    this.btns[0].setTooltipDelay(TOOLTIP_DELAY);
                }

                this.btnX[btnOffset+0] = ROW_LEFT+listElWidth;
                this.btnX[btnOffset+1] = ROW_LEFT+listElWidth+15;
                this.btnX[btnOffset+2] = ROW_LEFT+listElWidth+15+15;
                this.btnX[btnOffset+3] = ROW_LEFT+listElWidth+15+15;

                //del btn
                int currentBtn = btnOffset;
                this.btns[currentBtn] = Button.builder(Component.nullToEmpty("X"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(this.btnX[currentBtn],5+this.btnY[currentBtn],15,20).build();
                if(index<0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.create(Component.nullToEmpty("Delete")));

                //clone btn
                currentBtn++;
                this.btns[currentBtn] = Button.builder(Component.nullToEmpty("*"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.cloneListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(this.btnX[currentBtn],5+this.btnY[currentBtn],15,20).build();
                if(index<0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.create(Component.nullToEmpty("Clone")));

                //up btn
                currentBtn++;
                this.btns[currentBtn] = Button.builder(Component.nullToEmpty(UNICODE_UP_ARROW), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.moveListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index,true),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(this.btnX[currentBtn],5+this.btnY[currentBtn],15,10).build();
                if(index<=0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.create(Component.nullToEmpty("Move Up")));

                //down btn
                currentBtn++;
                this.btns[currentBtn] = Button.builder(Component.nullToEmpty(UNICODE_DOWN_ARROW), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.moveListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index,false),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(this.btnX[currentBtn],5+this.btnY[currentBtn],15,10).build();
                if(index>=maxIndex || index<0)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.create(Component.nullToEmpty("Move Down")));

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
                }
            }
            else { // add new element to list
                Component btnTxt = Component.nullToEmpty("Add Element");
                PathInfo pi = ComponentHelper.getPathInfo(pagePath);
                PathInfo pie = ComponentHelper.getPathInfo(pagePath+"[0]");

                this.btns = new Button[]{
                Button.builder(btnTxt, btn -> {
                    Tag el = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath);
                    ListTag list;
                    if(el != null && el.getId()==Tag.TAG_LIST) {
                        list = (ListTag)el;
                    }
                    else
                        list = new ListTag();

                    if(!list.isEmpty()) {
                        Tag newEl = BlackMagick.getDefaultNbt(list.get(0).getId());
                        if(newEl != null)
                            list.add(newEl);
                    }
                    else {
                        if(pi.type()==PathType.LIST && pi.listType()>=0) {
                            Tag newEl = BlackMagick.getDefaultNbt(pi.listType());
                            if(newEl != null)
                                list.add(newEl);
                        }
                        else {
                            FortytwoEdit.logWarn("Failed to add element to unknown list at path: "+pagePath);
                        }
                    }

                    if(!list.isEmpty()) {
                        setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,list),blankElPath),saveBtn,
                            path2==null ? null : pagePath);
                    }

                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).bounds(ItemBuilder.this.x+ROW_LEFT,5,80,20).build()};
                this.btnX = new int[]{ROW_LEFT};
                this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty("Add a default element to the list")));

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
            }

        }

        /**
         * Used for editPath fallback (only one EditBox).
         */
        public RowWidgetElement(String blankElPath, ListTag path2, Button saveBtn) {
            this(blankElPath, path2, saveBtn, WidgetCacheType.NONE);
        }

        /**
         * Used for elements with specialized editors.
         */
        public RowWidgetElement(String blankElPath, ListTag path2, Button saveBtn, WidgetCacheType cacheType) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString().get(); // keep asString
            String fullPath = blankElPath+currentPath2;

            PathInfo pi = ComponentHelper.getPathInfo(fullPath);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==Tag.TAG_STRING;

            Tag tempEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
            final String startVal = (isString && tempEl != null && tempEl.getId() == Tag.TAG_STRING) ? tempEl.asString().get() : BlackMagick.nbtToString(tempEl); // keep asString
            tempEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
            final String currentVal = (isString && tempEl != null && tempEl.getId() == Tag.TAG_STRING) ? tempEl.asString().get() : BlackMagick.nbtToString(tempEl); // keep asString

            String[] baseSuggestions = pi.suggs() == null ? null : pi.suggs().getArray();

            this.txts = new EditBox[]{new EditBox(((ItemBuilder)ItemBuilder.this).minecraft.font,
                ItemBuilder.this.x+ROW_LEFT, 5, ROW_RIGHT-ROW_LEFT, 20, Component.nullToEmpty(""))};
            this.txtX = new int[]{ROW_LEFT};
            this.txts[0].setMaxLength(MAX_TEXT_LENGTH);

            this.txts[0].setResponder(value -> {
                setErrorMsg(null);
                Tag el = isString ? StringTag.valueOf(value) : BlackMagick.nbtFromString(value);

                if(el != null || value.isEmpty())
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                        path2==null ? null : fullPath);
                else
                    setErrorMsg("Invalid element");

                if((value != null && !value.equals(startVal))) {
                    this.txts[0].setTextColor(TEXT_COLOR);
                    if(inpError == null)
                        setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.nbtToString(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl)),inpError));
                }
                else {
                    this.txts[0].setTextColor(LABEL_COLOR);
                }

                if(inpError != null)
                    this.txts[0].setTextColor(ERROR_COLOR);

                suggsOnChanged(this.txts[0],baseSuggestions,startVal);
            });

            this.txts[0].setValue(currentVal);
            widgetCacheAdd(cacheType, this.txts[0]);

            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.ALL_TEXT_WIDGETS.add(this.txts[i]);
            }
        }

    }

    class RowWidgetSavedItemsRow extends RowWidget {
        
        protected int savedRow;

        /**
         * Saved row (9 btns)
         */
        public RowWidgetSavedItemsRow(int row) {
            super();

            this.savedRow = row;
            this.btns = new Button[9];
            this.btnX = new int[9];
            int currentX = 10+30;
            for(int i=0; i<9; i++) {
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
                this.btns[i] = new ItemSlotButton(currentX, 5, 20, thisItemStart, btn -> {
                    if(!viewBlackMarket && savedModeSet) {
                        String itemString = "";
                        ItemStack savedItem = minecraft.player.getMainHandItem().copy();
                        if(!savedItem.isEmpty()) {
                            itemString = BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(savedItem));
                        }

                        if(FortytwoEdit.testSavedItems(savedItems)) {
                            boolean inMap = savedItems.containsKey(index);
                            if((!inMap && itemString.isEmpty()) || (inMap && savedItems.get(index).equals(itemString))) {
                                FortytwoEdit.showToast("No Change","Item already saved");
                            }
                            else {
                                if(itemString.isEmpty())
                                    savedItems.remove(index);
                                else
                                    savedItems.put(index,itemString);

                                if(!FortytwoEdit.setSavedItems(savedItems)) {
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
                    else if(minecraft.player.getAbilities().instabuild) {
                        ItemStack thisItem =
                            (viewBlackMarket ?
                                ((FortytwoEdit.webItems.size()>index) ?
                                    BlackMagick.itemFromString(FortytwoEdit.webItems.get(index))
                                    : ItemStack.EMPTY)
                                : ((savedItems.containsKey(index)) ?
                                    BlackMagick.itemFromString(savedItems.get(index))
                                    : ItemStack.EMPTY)
                            );
                        if(!thisItem.isEmpty())
                            BlackMagick.setItemMain(thisItem);
                    }
                    ItemBuilder.this.unsel();
                });
                currentX += 20;
                this.btns[i].active = false;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);

                this.children.add(this.btns[i]);
            }
        }

        public void updateSavedDisplay() {
            for(int i=0; i<9; i++) {
                ItemSlotButton w = (ItemSlotButton)this.btns[i];
                w.active = savedModeSet && !viewBlackMarket;
                w.setError(null);
                w.removeOverlay();
                w.showSlot(false);
                if((!viewBlackMarket && savedItems.containsKey(savedRow*9+i)) || (viewBlackMarket && FortytwoEdit.webItems.size()>(savedRow*9+i))) {
                    SavedItem current = SavedItem.build(viewBlackMarket ? FortytwoEdit.webItems.get(savedRow*9+i) : savedItems.get(savedRow*9+i));
                    if(current.stack()==null) {
                        w.setTooltip(makeItemTooltip(current.storedString()));
                        w.setItem(FortytwoEdit.ITEM_ERROR);
                        w.setError(ItemSlotButton.ItemError.ERROR);
                    }
                    else {
                        w.setItem(current.stack());
                        w.showSlot(true);
                        if(current.nbtError()) {
                            w.setError(ItemSlotButton.ItemError.WARN);
                            Tag currentEl = BlackMagick.nbtFromString(current.storedString());
                            if(currentEl != null && currentEl.getId()==Tag.TAG_COMPOUND) {
                                w.setTooltip(Tooltip.create(Component.empty().append(
                                    BlackMagick.textComponentFromString("{text:\"Failed to load all item data\",color:\"red\"}").text()).append(
                                    Component.nullToEmpty("\n")).append(
                                    BlackMagick.getElementDifferences((CompoundTag)currentEl, BlackMagick.itemToNbtStorage(current.stack())))));
                            }
                            else {
                                w.setTooltip(makeItemTooltip(current.storedString()));
                            }
                        }
                        else
                            w.setTooltip(makeItemTooltip(current.stack()));

                        if(minecraft.player.getAbilities().instabuild)
                            w.active = true;
                    }
                    if(savedModeSet && !viewBlackMarket)
                        w.setOverlay(DELETE_ITEM_OVERLAY,DELETE_ITEM_OVERLAY_SIZE);
                }
                else {
                    w.setTooltip(null);
                    w.setItem(null);
                    if(savedModeSet && !viewBlackMarket)
                        w.showSlot(true);
                }
            }
        }

    }

    class RowWidgetBannerRow extends RowWidget {

        /**
         * banner row (8 btns)
         */
        public RowWidgetBannerRow(String blankElPath, ListTag path2, Button saveBtn, boolean isDye, String[] vals, String currentVal, Tag cancelEl) {
            super();

            ItemStack[] stacks = new ItemStack[vals.length];
            boolean[] stackWarns = new boolean[vals.length];

            ItemStack[] patternItems = isDye ? null : new ItemStack[vals.length];

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString().get(); // keep asString
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"."+(isDye ? "color" : "pattern");

            if(!isDye) {
                for(int i=0; i<vals.length; i++)
                    patternItems[i] = BlackMagick.itemFromNbt((CompoundTag)BlackMagick
                        .nbtFromString("{id:white_banner,components:{banner_patterns:[{color:red,pattern:\""+vals[i]+"\"}]}}"));

                if(!bannerShield)
                    for(int i=0; i<vals.length; i++)
                        stacks[i] = patternItems[i];
                else
                    for(int i=0; i<vals.length; i++)
                        stacks[i] = BlackMagick.itemFromNbt((CompoundTag)BlackMagick
                            .nbtFromString("{id:shield,components:{base_color:white,banner_patterns:[{color:red,pattern:\""+vals[i]+"\"}]}}"));
            }
            else {
                for(int i=0; i<vals.length; i++)
                    stacks[i] = BlackMagick.itemFromNbt((CompoundTag)BlackMagick.nbtFromString("{id:"+vals[i]+"_dye}"));
            }
            this.btns = new Button[vals.length];
            this.btnX = new int[vals.length];
            int currentX = 10+30;
            for(int i=0; i<btns.length; i++) {
                this.btnX[i] = currentX;
                final int col = i;

                Tooltip tt = null;
                boolean disabled = false;
                if(!isDye) {
                    disabled = true;
                    if(!patternItems[i].isEmpty()) {
                        List<Component> textList = patternItems[i].getTooltipLines(TooltipContext.EMPTY,null,TooltipFlag.NORMAL);
                        if(textList.size()>1) {
                            disabled = false;
                            tt = Tooltip.create(Component.nullToEmpty(textList.get(1).getString().replace("Red ","")));
                        }
                    }
                    if(disabled) {
                        tt = Tooltip.create(Component.nullToEmpty("Pattern disabled: "+vals[i]).copy().withStyle(ChatFormatting.RED));
                        stacks[i] = FortytwoEdit.ITEM_ERROR;
                        stackWarns[i] = true;
                    }
                }
                else
                    tt = Tooltip.create(stacks[i].getHoverName());

                ItemSlotButton w = new ItemSlotButton(currentX, 5, 20, stacks[i], btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,StringTag.valueOf(vals[col])),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    CompoundTag newArgs = BlackMagick.validCompoundFromString("{path:\""+blankElPath+"\"}");
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);

                    CompoundTag cancelNbt = new CompoundTag();
                    if(cancelEl != null)
                        cancelNbt.put("el",cancelEl);
                    newArgs.put("cancelEl",cancelNbt);

                    createBlankTab(0,newArgs);
                });
                w.showSlot(false);
                if(stackWarns[i]) {
                    w.setError(ItemSlotButton.ItemError.ERROR);
                }
                this.btns[i] = w;
                currentX += 20;

                if(currentVal != null && (vals[i].equals(currentVal) || currentVal.equals("minecraft:"+vals[i])))
                    this.btns[i].active = false;

                this.btns[i].setTooltip(tt);
                this.children.add(this.btns[i]);
            }
        }

    }

    class RowWidgetInvRow extends RowWidget {

        private static final ResourceLocation[] PLAYER_ARMOR_SPRITES = new ResourceLocation[]{
            ItemSlotButton.SPRITE_FEET,
            ItemSlotButton.SPRITE_LEGS,
            ItemSlotButton.SPRITE_CHEST,
            ItemSlotButton.SPRITE_HEAD,
            ItemSlotButton.SPRITE_OFFHAND
        };
        private static final ResourceLocation[] ARMOR_STAND_SPRITES = new ResourceLocation[]{
            ItemSlotButton.SPRITE_FEET,
            ItemSlotButton.SPRITE_LEGS,
            ItemSlotButton.SPRITE_CHEST,
            ItemSlotButton.SPRITE_HEAD,
            ItemSlotButton.SPRITE_MAINHAND,
            ItemSlotButton.SPRITE_OFFHAND
        };
        private static final ResourceLocation SEL_SLOT = ResourceLocation.parse("hud/hotbar_selection");
        private boolean renderHotbarSel = false;

        /**
         * Used for player inventory rows. Always make 5 rows (3 for inv, 1 for hotbar, 1 for armor/offhand).
         * 
         * @param row
         */
        public RowWidgetInvRow(int row) {
            super();

            if(row >= 0 && row < 4) {
                this.btns = new Button[9];
                this.btnX = new int[btns.length];
            }
            else if(row == 4) {
                renderHotbarSel = true; // rendered on row 4 so sprite isnt covered by row 4
                this.btns = new Button[5];
                this.btnX = new int[btns.length];
            }

            int currentX = 10+30;
            if(row==4)
                currentX += 20;
            for(int i=0; i<this.btns.length; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                final ItemStack thisItem = cacheInv[index];
                ItemSlotButton w = new ItemSlotButton(currentX, 5, 20, thisItem, btn -> btnCopyItemNbt(thisItem));
                if(row == 4) {
                    w.addEmptySlotSprite(PLAYER_ARMOR_SPRITES[i]);
                }
                this.btns[i] = w;
                this.btns[i].active = false;
                if(thisItem != null && !thisItem.isEmpty()) {
                    this.btns[i].active = true;
                    this.btns[i].setTooltip(makeItemTooltip(thisItem));
                }
                currentX += 20;
                if(row==4 && i==3)
                    currentX += 40;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);

                this.children.add(this.btns[i]);
            }
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
         * Slot sprites correspond to the stacks and should be numbered 1 to 5 (boots to offhand).
         * 
         * @param stacks
         * @param slotSprites
         */
        public RowWidgetInvRow(ItemStack[] stacks, ResourceLocation[] slotSprites) {
            super();

            this.btns = new Button[stacks.length];
            this.btnX = new int[btns.length];

            int currentX = 10+30;
            for(int i=0; i<this.btns.length; i++) {
                this.btnX[i] = currentX;
                final ItemStack thisItem = stacks[i];
                ItemSlotButton w = new ItemSlotButton(currentX, 5, 20, thisItem, btn -> btnCopyItemNbt(thisItem));
                if(slotSprites != null && slotSprites.length == stacks.length)
                    w.addEmptySlotSprite(slotSprites[i]);
                this.btns[i] = w;
                this.btns[i].active = false;
                if(thisItem != null && !thisItem.isEmpty()) {
                    this.btns[i].active = true;
                    this.btns[i].setTooltip(makeItemTooltip(thisItem));
                }
                currentX += 20;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);

                this.children.add(this.btns[i]);
            }
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            if(this.renderHotbarSel)
                context.blitSprite(RenderType::guiTexturedOverlay, SEL_SLOT,
                    x+(minecraft.player.getInventory().getSelectedSlot()*20)+40-2, y-2-20, 24, 23);
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
            super();

            this.cacheType = cacheType;
            if(cacheType == null) {
                FortytwoEdit.logError("Tried to create RowWidgetEditor with null WidgetCacheType");
            }

            switch(cacheType) {
                case TXT_POSE: {
                    this.btns = new Button[3];
                    this.btnX = new int[]{ROW_LEFT,ROW_LEFT+20+5,ROW_LEFT+20+5+20+5};
    
                    this.btns[2] = Button.builder(Component.nullToEmpty(UNICODE_X), btn -> {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            poseCompound = new CompoundTag();
                            txt.setValue("");
                            resetSuggs();
                            updatePose();
                        }
                        unsel();
                    }).bounds(ItemBuilder.this.x+ROW_LEFT+20+5+20+5,5,20,20).build();
    
                    this.btns[2].setTooltip(Tooltip.create(Component.nullToEmpty("Clear pose")));
                    break;
                }
                case TXT_DECIMAL_COLOR: {
                    this.btns = new Button[2];
                    this.btnX = new int[]{ROW_LEFT,ROW_LEFT+20+5};
                    break;
                }
                default: {
                    FortytwoEdit.logWarn("Tried to create RowWidgetEditor for invalid WidgetCacheType: "+cacheType);
                    break;
                }
            }

            this.btns[0] = Button.builder(Component.nullToEmpty(UNICODE_DOWN_ARROW), btn -> {
                switch(cacheType) {
                    case TXT_DECIMAL_COLOR: {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            if(txt.getValue().length()>0) {
                                trySetColorDec(0,txt.getValue(),null);
                            }
                            else {
                                trySetColorDec(0,"0",null);
                            }
                        }
                        break;
                    }
                    case TXT_POSE: {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            if(txt.getValue().length()>0) {
                                Tag el = BlackMagick.nbtFromString(txt.getValue());
                                if(el!=null && el.getId()==Tag.TAG_COMPOUND) {
                                    poseCompound = new CompoundTag();
                                    CompoundTag copyFrom = (CompoundTag)el;
                                    for(String k : poseTypes) {
                                        if(copyFrom.getList(k).isPresent()) {
                                            ListTag l = copyFrom.getListOrEmpty(k);
                                            if(l.size()==3)
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
            }).bounds(ItemBuilder.this.x+ROW_LEFT,5,20,20).build();

            this.btns[1] = Button.builder(Component.nullToEmpty(UNICODE_UP_ARROW), btn -> {
                switch(cacheType) {
                    case TXT_DECIMAL_COLOR: {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            txt.setValue(""+getRgbDec(0));
                            resetSuggs();
                        }
                        break;
                    }
                    case TXT_POSE: {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            if(poseCompound.isEmpty())
                                txt.setValue("");
                            else
                                txt.setValue(BlackMagick.nbtToString(poseCompound));
                            resetSuggs();
                            updatePose();
                        }
                        break;
                    }
                    default: break;
                }
                unsel();
            }).bounds(ItemBuilder.this.x+ROW_LEFT+20+5,5,20,20).build();

            this.btns[0].setTooltip(Tooltip.create(Component.nullToEmpty("Copy to editor")));
            this.btns[1].setTooltip(Tooltip.create(Component.nullToEmpty("Set from editor")));

            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

            boolean editorEqual = false;
            switch(this.cacheType) {
                case TXT_DECIMAL_COLOR: {
                    if(widgetCacheTest(cacheType)) {
                        EditBox txt = (EditBox)widgetCacheGet(cacheType);
                        if(txt.getValue().equals(""+getRgbDec(0)))
                            editorEqual = true;
                    }
                    break;
                }
                case TXT_POSE: {
                    if(widgetCacheTest(cacheType)) {
                        EditBox txt = (EditBox)widgetCacheGet(cacheType);
                        if(txt.getValue().equals(""+BlackMagick.nbtToString(poseCompound)))
                            editorEqual = true;
                        else if(txt.getValue().isEmpty() && poseCompound.isEmpty())
                            editorEqual = true;
                    }
                    break;
                }
                default: break;
            }

            if(!editorEqual) {
                context.drawString(ItemBuilder.this.font, Component.nullToEmpty("Unlinked from editor"), x+this.btnX[this.btnX.length-1]+20+5, y+6, ERROR_COLOR);
                this.btns[0].active = true;
                this.btns[1].active = true;
            }
            else {
                context.drawString(ItemBuilder.this.font, Component.nullToEmpty("Linked to editor"), x+this.btnX[this.btnX.length-1]+20+5, y+6, LABEL_COLOR_DIM);
                this.btns[0].active = false;
                this.btns[1].active = false;
            }
            if(this.btnX.length>=3) {
                boolean cleared = false;
                switch(this.cacheType) {
                    case TXT_POSE: {
                        if(widgetCacheTest(cacheType)) {
                            EditBox txt = (EditBox)widgetCacheGet(cacheType);
                            if((txt.getValue().equals("{}") || txt.getValue().isEmpty()) && (poseCompound.isEmpty()))
                                cleared = true;
                        }
                        break;
                    }
                    default: break;
                }

                if(cleared) {
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
            super(0, 0, 180, 20, Component.nullToEmpty(""), 0.0);
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
            if((float)((int)val) == val)
                this.setMessage(Component.nullToEmpty(""+(int)val));
            else
                this.setMessage(Component.nullToEmpty(""+val));
        }

        public void setVal(float newVal) {
            while(newVal > 180)
                newVal -= 360;
            while(newVal < -180)
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
            super(0, 0, halfWidth ? 120-15-5 : 180-40, 20, Component.nullToEmpty(""), 0.0);
            this.min = 0f;
            this.rgb = rgb;
            this.setNum = setNum;
            this.num = num;
            if(rgb) {
                this.max = 255f;
                this.value = (colorSets[this.setNum][this.num] - min) / (max - min);
            }
            else {
                if(num==0)
                    this.max = 360f;
                else
                    this.max = 100f;
                this.value = (colorHsv[num] - min) / (max - min);
            }
            this.applyValue();
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if(rgb) {
                colorSets[setNum][num] = (int)Math.round(this.value*(max-min)+min);
                updateColorSets();
            }
            else {
                float valMult = Math.round((this.value*(max-min)+min)*1000);
                setHsv(num,valMult/1000f);
            }
        }

        @Override
        protected void updateMessage() {
            if(rgb) {
                String color = UNICODE_SECTION_SIGN;
                if(num == 0 || num == 3)
                    color += "4";
                else if(num == 1 || num == 4)
                    color += "2";
                else
                    color += "1";
                this.setMessage(Component.nullToEmpty(color+colorSets[setNum][num]));
            }
            else {
                String color = UNICODE_SECTION_SIGN+"7";
                if(num == 0)
                    color += "H";
                else if(num == 1)
                    color += "S";
                else
                    color += "V";
                color += UNICODE_SECTION_SIGN+"r ";

                float valMult = Math.round((colorHsv[num])*1000);
                float val = valMult/1000f;

                this.setMessage(Component.nullToEmpty(color+val));
            }
        }

        public void setVal(int newVal) {
            if(newVal > max)
                newVal = (int)max;
            else if(newVal < min)
                newVal = (int)min;

            this.value = (double)((newVal - min)/(max-min));
            updateMessage();
        }

        public void setVal(float newVal) {
            if(newVal > max)
                newVal = (int)max;
            else if(newVal < min)
                newVal = (int)min;

            this.value = (double)((newVal - min)/(max-min));
            updateMessage();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private record Tab(int pos, String lbl, ItemStack display, boolean hideTabs) {

        public Tab(int pos, String lbl, ItemStack display) {
            this(pos,lbl,display,false);
        }

        public Tab() {
            this(-1,"",ItemStack.EMPTY,true);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PosWidget {
        public AbstractWidget w = null;
        public ItemStack s = null;//to_do remove
        public int x;
        public int y;

        public PosWidget(AbstractWidget w, int x, int y) {
            this.w = w;
            this.x = x;
            this.y = y;
        }

        public PosWidget(ItemStack s, int x, int y) {//to_do remove
            this.s = s;
            this.x = x;
            this.y = y;
        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private enum WidgetCacheType {
        NONE,                   // do not cache (used for fallback page)

        TXT_DECIMAL_COLOR,      // EditBox for PathType.DECIMAL_COLOR
        TXT_POSE,               // EditBox for PathType.POSE

        BTN_SAVED_SOURCE,       // ItemSlotButtonWidget for Local Items | Black Market Items
        BTN_SAVED_MODE,         // Button below saved source btn

        GIVE_BOX_BOX,           // MultiLineEditBox for custom data tab
        GIVE_BOX_CLONE,         // Button to clone to give box
        GIVE_BOX_GIVE,          // Button to give from give box

        TEXT_COMPONENT_ADD_BTN,                     // Button to add current editor to main text component
        TEXT_COMPONENT_COLOR_BTN,                   // Button for color mode
        TEXT_COMPONENT_COLOR_TXT,                   // EditBox for color mode
        TEXT_COMPONENT_RADIAL,                      // Button for Radial | Linear
        TEXT_COMPONENT_EFFECT_TEXT_MODE,            // Button for text/translation/keybind selection
        TEXT_COMPONENT_EFFECT_TEXT_ENTRY,           // EditBox for text/translation/keybind entry
        TEXT_COMPONENT_TRANSLATION_WITH,            // EditBox for `with`
        TEXT_COMPONENT_TRANSLATION_FALLBACK,        // EditBox for `fallback`
        TEXT_COMPONENT_EFFECT_BTN_BOLD,             // Button for bold
        TEXT_COMPONENT_EFFECT_BTN_ITALIC,           // Button for italic
        TEXT_COMPONENT_EFFECT_BTN_UNDERLINED,       // Button for underlined
        TEXT_COMPONENT_EFFECT_BTN_STRIKETHROUGH,    // Button for strikethrough
        TEXT_COMPONENT_EFFECT_BTN_OBFUSCATED,       // Button for obfuscated
        TEXT_COMPONENT_CLICK_EVENT_ACTION,          // EditBox for clickEvent action
        TEXT_COMPONENT_CLICK_EVENT_VALUE,           // EditBox for clickEvent value
        TEXT_COMPONENT_HOVER_EVENT_ACTION,          // EditBox for hoverEvent action
        TEXT_COMPONENT_HOVER_EVENT_CONTENTS,        // EditBox for hoverEvent contents
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public record SavedItem(String storedString, ItemStack stack, boolean nbtError) {
        public static SavedItem build(String itemString) {
            ItemStack stack = BlackMagick.itemFromString(itemString);
            if(!stack.isEmpty()) {
                String newItemString = BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(stack));
                if(newItemString.equals(itemString)) {
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if(!tabs[tab].hideTabs()) {
            if(tab == CACHE_TAB_SAVED && !viewBlackMarket && savedItemsError)
                context.drawCenteredString(this.font,
                    Component.nullToEmpty("Failed to read saved items"), this.width / 2, y+this.backgroundHeight+3, ERROR_COLOR);

            if(prevArmorStand)
                InventoryScreen.renderEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)renderArmorStand);
            else
                InventoryScreen.renderEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)this.minecraft.player);

            txtFormat.setX(x+50);
            txtFormat.render(context, mouseX, mouseY, delta);
            if(!this.UNSAVED_TEXT_WIDGETS.isEmpty())
                context.drawCenteredString(this.font, Component.nullToEmpty("Unsaved"), this.width / 2, y-11, TEXT_COLOR);
        }
        else {
            if(textComponentPreview != null) {
                txtFormat.setX(x-15);
                txtFormat.render(context, mouseX, mouseY, delta);

                if(textComponentPreviewBook) {
                    // Modified from {@link net.minecraft.client.gui.screens.inventory.BookViewScreen#render}
                    FormattedText stringVisitable = textComponentPreview;
                    List<FormattedCharSequence> page = this.font.split(stringVisitable, 114);
                    int l = Math.min(128 / this.font.lineHeight, page.size());
                    for(int m = 0; m < l; ++m) {
                        FormattedCharSequence orderedText = page.get(m);
                        context.drawString(this.font, orderedText, x + bookX + 36, y + bookY + 32 + m * this.font.lineHeight, 0, false);
                    }
                    Style style = this.getBookTextStyleAt(page, x + bookX, y + bookY, mouseX, mouseY);
                    if(style != null) {
                        context.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
                    }
                }
                else
                    context.drawCenteredString(this.font, textComponentPreview, this.width / 2, y-14, TEXT_COLOR);

                if(tab != CACHE_TAB_BLANK) {
                    textComponentPreview = null;
                }
            }
            else {
                textComponentPreviewBook = false;
                if(blankTabUnsaved && tab == CACHE_TAB_BLANK)
                    context.drawCenteredString(this.font, Component.nullToEmpty("Unsaved"), this.width / 2, y-11, TEXT_COLOR);
            }

            if(showBannerPreview && bannerChangePreview != null) {
                if(!bannerShield)
                    InventoryScreen.renderEntityInInventoryFollowsMouse(context,x+240,y,x+240+100,y+400,2*RENDER_SIZE,0f,x+240+50,y+200,(LivingEntity)bannerChangePreview);
                else
                    InventoryScreen.renderEntityInInventoryFollowsMouse(context,x+240,y,x+240+100,y+200,2*RENDER_SIZE,0f,x+240+50,y+100,(LivingEntity)bannerChangePreview);
            }

            if(showPosePreview) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f,
                    mouseX, mouseY, (LivingEntity)renderArmorPose);
            }
        }
        if(inpErrorTrim != null)
            context.drawCenteredString(this.font, Component.nullToEmpty(inpErrorTrim), this.width / 2, y+this.backgroundHeight+3, ERROR_COLOR);

        if(suggs != null)
            suggs.render(context, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE_MENU_BAR;
    }

    @Override
    protected void renderBehindBackgroundTexture(GuiGraphics context) {
        if(textComponentPreviewBook)
            context.blit(RenderType::guiTextured, BookViewScreen.BOOK_LOCATION, x + bookX, y + bookY, 0.0F, 0.0F, 192, 192, 256, 256);
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
            pauseSaveScroll = true;
        }
        resetSuggs();
        super.resize(client, width, height);
        setErrorMsg(inpError);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return this.UNSAVED_TEXT_WIDGETS.isEmpty() && !activeTxt() && !tabs[tab].hideTabs();
    }

    @Override
    public void onCloseAction() {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.scrollAmount();
        }
        super.onCloseAction();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(suggs != null && suggs.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            if(!activeTxt() && !activeSlider()) {
                if((keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) && this.UNSAVED_TEXT_WIDGETS.isEmpty() && !tabs[tab].hideTabs() && hotbarLeftBtn.active) {
                    btnChangeSlot(keyCode == GLFW.GLFW_KEY_LEFT);
                }
                return true;
            }
        }
        if(super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(suggs != null && suggs.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(suggs != null && suggs.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        resetSuggs();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        if(tab != CACHE_TAB_BLANK)
            updateItem();

        if(tab == CACHE_TAB_INV)
            updateInvTab();
        else if(tab == CACHE_TAB_MAIN && TAB_WIDGETS_SCROLL.get(tab).isEmpty())
            createTab(tab);

        super.tick();
    }

}
