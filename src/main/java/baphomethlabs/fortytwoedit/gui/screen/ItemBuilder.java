package baphomethlabs.fortytwoedit.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
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
import baphomethlabs.fortytwoedit.mixin.HotbarStorageAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.item.TooltipType;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ItemBuilder extends GenericScreen {

    protected boolean unsel = false;//TODO remove unused fields
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
    protected ItemStack selItem = ItemStack.EMPTY;
    protected ItemStack selItemOff = ItemStack.EMPTY;
    protected static List<List<String>> cacheStates = new ArrayList<>();
    protected ButtonWidget itemBtn = null;
    protected ButtonWidget swapBtn;
    protected ButtonWidget swapCopyBtn;
    protected ButtonWidget throwCopyBtn;
    protected ButtonWidget hotbarLeftBtn;
    protected ButtonWidget hotbarRightBtn;
    private TextFieldWidget txtFormat;
    private TabWidget tabWidget;
    private final List<List<PosWidget>> noScrollWidgets = new ArrayList<>();
    private final List<List<RowWidget>> widgets = new ArrayList<>();
    private final Set<ClickableWidget> unsavedTxtWidgets = Sets.newHashSet();
    private final Set<ClickableWidget> allTxtWidgets = Sets.newHashSet();
    public static boolean savedModeSet = false;
    private NbtList savedItems = null;
    private boolean savedError = false;
    private String inpError = null;
    private String inpErrorTrim = null;
    private static boolean showUnusedComponents = false;
    private static boolean viewBlackMarket = false;
    private static NbtList webItems = null;
    private static final ItemStack[] savedModeItems = new ItemStack[]{BlackMagick.itemFromNbtStatic((NbtCompound)BlackMagick.nbtFromString(
        "{id:player_head,components:{profile:{properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQ"
        +"iIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZDlmMThjOWQ4NWY5MmY3"
        +"MmY4NjRkNjdjMTM2N2U5YTQ1ZGMxMGYzNzE1NDljNDZhNGQ0ZGQ5ZTRmMTNmZjQiDQogICAgfQ0KICB9DQp9\"}]}}}")),
        BlackMagick.itemFromNbtStatic((NbtCompound)BlackMagick.nbtFromString("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3"
        +"RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MjY0ODZmNDI0ODljZWYwMmM5ZTk4ZGQ4YmU1YTNmMzhlODc5MTQ3NTQzMjZlNzdjODM3YzFiMmJjYmE2NSINCiAgICB9DQogIH0NCn0=\"}]}}}")),
        new ItemStack(Items.BARRIER)};
    private ArmorStandEntity renderArmorStand;
    private ArmorStandEntity renderArmorPose;
    protected final int playerX = 240+10;
    protected final int playerY = -10;
    private static final int RENDER_SIZE = 35;
    private static final Text ERROR_CREATIVE = Text.of("Creative required to edit");
    private ArrayList<RowWidget> sliders = new ArrayList<>();
    private ArrayList<RowWidget> sliderBtns = new ArrayList<>();
    private ButtonWidget setPoseButton;
    private boolean editArmorStand = false;
    private float[][] armorPose;
    public static final String BANNER_PRESET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
    public static final String[] BANNER_CHAR_LIST = new String[BANNER_PRESET_CHARS.replaceAll("\\s","").length()+1];
    private TextSuggestor suggs;
    private Set<TextFieldWidget> currentTxt = Sets.newHashSet();
    private static int[] rgb = new int[]{66,6,102,0,0,0};
    private ArrayList<RowWidget> rgbSliders = new ArrayList<>();
    private boolean rgbChanged[] = {true,true};//used to update txtwids {general rgb dec, general rgb hex}
    private static final ItemStack[] rgbItems = new ItemStack[]{new ItemStack(Items.LEATHER_CHESTPLATE),new ItemStack(Items.POTION),new ItemStack(Items.FILLED_MAP)};
    private Map<String,int[]> cacheI = Maps.newHashMap();
    private Text jsonPreview = Text.of("");
    private boolean jsonPreviewBook = false;
    private int jsonEffectMode = -1;
    private String jsonEffectPath = null;
    private String jsonEffectBase = null;
    private static int[] jsonEffects = new int[8];//bold,italic,underlined,strikethrough,obfuscated,radgrad,colmode,elmode
    private static double[] tabScroll = new double[tabs.length];
    private boolean pauseSaveScroll = false;
    protected NbtElement blankTabEl = null;
    protected boolean blankTabUnsaved = false;
    private String jsonBaseText = "";
    private boolean jsonBaseValid = false;
    private boolean jsonEffectValid = false;
    private String jsonEffectFull = "";
    private static String jsonLastColor = "reset";
    private boolean bannerShield = false;
    private static ArmorStandEntity bannerChangePreview = null;
    protected boolean showBannerPreview = false;
    private ItemStack[] cacheInv = new ItemStack[41];
    private int cacheInvSlot = -1;
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = 1;

        if(firstInit) {
            if(tabs[tab].hideTabs)
                tab = CACHE_TAB_MAIN;
            firstInit = false;
        }
        pauseSaveScroll = false;
        
        if(!tabs[tab].hideTabs) {

            //tabs
            for(int posNum = 0; posNum<tabs.length; posNum++)
                for(int i = 0; i<tabs.length; i++)
                    if(tabs[i].pos==posNum) {
                        int tabNum = i;
                        ButtonWidget w;
                        if(tabs[i].pos<LEFT_TABS)
                            w = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x-TAB_SIZE,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*tabs[i].pos,TAB_SIZE,TAB_SIZE).build();
                        else
                            w = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x+240,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(tabs[i].pos-LEFT_TABS),TAB_SIZE,TAB_SIZE).build();
                        w.setTooltip(Tooltip.of(Text.of(tabs[i].lbl)));
                        if(tab==i)
                            w.active = false;
                        this.addDrawableChild(w);
                    }

            //main
            this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
            txtFormat = new TextFieldWidget(this.textRenderer,x+50,y+5+1,15,18,Text.of(""));
            txtFormat.setEditable(false);
            txtFormat.setText("\u00a7");
            txtFormat.setTooltip(Tooltip.of(FortytwoEdit.formatTooltip));
            swapCopyBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("c*"), button -> this.btnSwapOff(true)).dimensions(width/2 - 50,y+5,20,20).build());
            swapBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("c"), button -> this.btnSwapOff(false)).dimensions(width/2 - 30,y+5,15,20).build());
            hotbarLeftBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnChangeSlot(true)).dimensions(width/2 - 15,y+5,15,20).build());
            hotbarRightBtn = this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnChangeSlot(false)).dimensions(width/2,y+5,15,20).build());
            ButtonWidget throwBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("Q"), button -> this.btnThrow(false)).dimensions(width/2 + 15,y+5,15,20).build());
            throwCopyBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("Q*"), button -> this.btnThrow(true)).dimensions(width/2 + 30,y+5,20,20).build());
            
            if(!client.player.getAbilities().creativeMode) {
                swapCopyBtn.active = false;
                throwCopyBtn.active = false;
                swapBtn.active = false;
            }
            if(client.player.isSpectator()) {
                swapCopyBtn.active = false;
                swapBtn.active = false;
                hotbarLeftBtn.active = false;
                hotbarRightBtn.active = false;
                throwBtn.active = false;
                throwCopyBtn.active = false;
            }
            if(swapCopyBtn.active)
                swapCopyBtn.setTooltip(Tooltip.of(Text.of("Copy item to offhand")));
            if(swapBtn.active)
                swapBtn.setTooltip(Tooltip.of(Text.of("Swap item with offhand")));
            if(hotbarLeftBtn.active)
                hotbarLeftBtn.setTooltip(Tooltip.of(Text.of("Scroll hotbar left")));
            if(hotbarRightBtn.active)
                hotbarRightBtn.setTooltip(Tooltip.of(Text.of("Scroll hotbar right")));
            if(throwBtn.active)
                throwBtn.setTooltip(Tooltip.of(Text.of("Throw item")));
            if(throwCopyBtn.active)
                throwCopyBtn.setTooltip(Tooltip.of(Text.of("Throw a copy of item")));

            itemBtn = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> this.btnCopyNbt()).dimensions(x+240-20-5,y+5,20,20).build());
            if(selItem==null || selItem.isEmpty()) {
                itemBtn.active = false;
                itemBtn.setTooltip(null);
            }
            else {
                itemBtn.active = true;
                itemBtn.setTooltip(makeItemTooltip(selItem));
            }
        }
        updateItem();

        //tabs
        if(widgets.size() == 0)
            createStaticTabs();
        this.tabWidget = null;
        for(int i=0; i<noScrollWidgets.get(tab).size(); i++) {
            PosWidget p = noScrollWidgets.get(tab).get(i);
            p.w.setX(x+p.x);
            p.w.setY(y+p.y);
            this.addDrawableChild(p.w);
        }
        if(widgets.get(tab).size()!=0) {
            this.tabWidget = new TabWidget(tab);
            this.tabWidget.setScrollAmount(tabScroll[tab]);
            this.addDrawableChild(this.tabWidget);
        }

        //armor stand
        renderArmorStand = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
        renderArmorStand.bodyYaw = 210.0f;
        renderArmorStand.setPitch(25.0f);
        renderArmorStand.headYaw = renderArmorStand.getYaw();
        renderArmorStand.prevHeadYaw = renderArmorStand.getYaw();
        //pose
        renderArmorPose = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
        renderArmorPose.bodyYaw = 210.0f;
        renderArmorPose.setPitch(25.0f);
        renderArmorPose.headYaw = renderArmorPose.getYaw();
        renderArmorPose.prevHeadYaw = renderArmorPose.getYaw();
        //banner prev
        if(bannerChangePreview == null) {
            bannerChangePreview = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
            bannerChangePreview.bodyYaw = 210.0f;
            bannerChangePreview.setPitch(25.0f);
            bannerChangePreview.headYaw = bannerChangePreview.getYaw();
            bannerChangePreview.prevHeadYaw = bannerChangePreview.getYaw();
            bannerChangePreview.readNbt((NbtCompound)BlackMagick.nbtFromString("{Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
        }

        //banner
        int i=1;
        BANNER_CHAR_LIST[0] = "*";
        for(char c : BANNER_PRESET_CHARS.replaceAll("\\s","").toCharArray()) {
            BANNER_CHAR_LIST[i] = ""+c;
            i++;
        }

    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnSwapOff(boolean copy) {
        if(!client.player.isSpectator()) {
            if(!copy) {
                if(client.player.getAbilities().creativeMode) {
                    ItemStack mainhand = client.player.getMainHandStack().copy();
                    ItemStack offhand = client.player.getOffHandStack().copy();
                    BlackMagick.setItemMain(offhand);
                    BlackMagick.setItemOff(mainhand);
                }
            }
            else {
                if(client.player.getAbilities().creativeMode) {
                    if(!client.player.getMainHandStack().isEmpty()) {
                        BlackMagick.setItemOff(client.player.getMainHandStack());
                    }
                    else if(!client.player.getOffHandStack().isEmpty()) {
                        BlackMagick.setItemMain(client.player.getOffHandStack());
                    }
                }
            }
        }
        unsel = true;
    }

    protected void btnChangeSlot(boolean left) {
        if(!client.player.isSpectator()) {
            int slot = client.player.getInventory().selectedSlot;
            if(left)
                slot--;
            else
                slot++;
            if(slot<0)
                slot = 8;
            else if(slot>8)
                slot = 0;
            client.player.getInventory().selectedSlot = slot;
        }
        unsel = true;
    }

    protected void btnThrow(boolean copy) {
        if(!client.player.isSpectator()) {
            if(!copy) {
                client.player.dropSelectedItem(true);
                client.player.playerScreenHandler.sendContentUpdates();
            }
            else if(client.player.getAbilities().creativeMode) {
                ItemStack item = client.player.getMainHandStack().copy();
                client.player.dropSelectedItem(true);
                client.player.playerScreenHandler.sendContentUpdates();
                BlackMagick.setItemMain(item);
            }
        }
        unsel = true;
    }

    protected void btnTab(int i) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        if(tab != i && (i == CACHE_TAB_NBT))
            setErrorMsg(null);
        tab = i;
        this.resize(this.client,this.width,this.height);
        resetSuggs();
        unsel = true;
    }

    protected void btnCopyNbt() {
        if(client.player.getMainHandStack() != null && !client.player.getMainHandStack().isEmpty()) {
            String itemData = BlackMagick.itemToNbtStorage(client.player.getMainHandStack()).asString();
            client.player.sendMessage(Text.of(itemData),false);
            client.keyboard.setClipboard(itemData);
        }
        unsel = true;
    }

    private void updateItem() {
        client.player.playerScreenHandler.sendContentUpdates();
        boolean changed = false;
        if(!ItemStack.areEqual(selItem,client.player.getMainHandStack()) && !widgets.isEmpty())
            changed = true;
        boolean changedOff = false;
        if(!ItemStack.areEqual(selItemOff,client.player.getOffHandStack()) && !widgets.isEmpty())
            changedOff = true;

        if(changed) {
            selItem = client.player.getMainHandStack().copy();
            FortytwoEdit.addItemHist(selItem);

            if(itemBtn != null) {
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

            if(cacheStates.size()>0)
                cacheStates.clear();
            if(selItem!=null && !selItem.isEmpty())
                cacheStates = BlackMagick.getBlockStates(selItem.getItem());

            if(selItem.isOf(Items.ARMOR_STAND)) {
                updateArmorStand(selItem.copy());
                editArmorStand = true;
            }
            else if(editArmorStand) {
                resetArmorStand();
                updatePose();
            }

            if(cacheI.containsKey("giveBox")) {
                int[] cacheGiveBox = cacheI.get("giveBox");
                boolean wasUnsaved = testUnsaved((EditBoxWidget)noScrollWidgets.get(cacheGiveBox[0]).get(cacheGiveBox[1]).w);
                ((EditBoxWidget)noScrollWidgets.get(cacheGiveBox[0]).get(cacheGiveBox[1]).w).setText(
                    ((EditBoxWidget)noScrollWidgets.get(cacheGiveBox[0]).get(cacheGiveBox[1]).w).getText());
                if(!wasUnsaved) {
                    markSaved((EditBoxWidget)noScrollWidgets.get(cacheGiveBox[0]).get(cacheGiveBox[1]).w);
                }
            }

            if(!widgets.isEmpty())
                createTab(CACHE_TAB_MAIN);
        }
        if(changedOff) {
            selItemOff = client.player.getOffHandStack().copy();
            FortytwoEdit.addItemHist(selItemOff);
        }
        if(changed || changedOff)
            compareItems();
    }

    private void compareItems() {
        
        swapBtn.setTooltip(null);
        if(!client.player.getMainHandStack().isEmpty() && !client.player.getOffHandStack().isEmpty()) {
            if(ItemStack.areItemsAndComponentsEqual(client.player.getMainHandStack(),client.player.getOffHandStack())) {
                swapBtn.setMessage(Text.of("\u00a7ac"));
            }
            else {
                swapBtn.setMessage(Text.of("\u00a7cc"));
                swapBtn.setTooltip(Tooltip.of(BlackMagick.getElementDifferences(BlackMagick.itemToNbtStorage(client.player.getOffHandStack()),
                    BlackMagick.itemToNbtStorage(client.player.getMainHandStack()))));
            }
        }
        else {
            swapBtn.setMessage(Text.of("c"));
        }

    }

    protected void markUnsaved(ClickableWidget widget) {
        this.unsavedTxtWidgets.add(widget);
    }

    protected void markSaved(ClickableWidget widget) {
        this.unsavedTxtWidgets.remove(widget);
    }

    protected boolean testUnsaved(ClickableWidget widget) {
        return this.unsavedTxtWidgets.contains(widget);
    }

    protected boolean activeTxt() {
        for(ClickableWidget w : allTxtWidgets)
            if(w.isFocused())
                return true;
        return false;
    }

    protected record EditingPath(int tab, String path) {}

    protected Tooltip makeItemTooltip(ItemStack stack) {
        if(stack==null || stack.isEmpty())
            return Tooltip.of(Text.of("Failed to read item"));
        String itemData = "";
        itemData += BlackMagick.itemToNbtStorage(stack).asString();
        itemData = makeItemTooltipShorten(itemData);

        MutableText mutableText = Text.empty().append(stack.getName()).formatted(stack.getRarity().getFormatting());
        if(stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            mutableText.formatted(Formatting.ITALIC);
        }

        return Tooltip.of(Text.empty().append(mutableText).append(Text.of("\n"+itemData)));
    }

    protected Tooltip makeItemTooltip(NbtCompound nbt, ItemStack stack) {
        if(nbt==null || !nbt.contains("id",NbtElement.STRING_TYPE))
            return Tooltip.of(Text.of("Failed to read item"));
        String itemData = "";
        itemData += nbt.asString();
        itemData = makeItemTooltipShorten(itemData);

        MutableText mutableText = null;
        if(stack != null) {
            mutableText = Text.empty().append(stack.getName()).formatted(stack.getRarity().getFormatting());
            if(stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                mutableText.formatted(Formatting.ITALIC);
            }
        }

        return Tooltip.of(Text.empty().append(stack == null ? BlackMagick.jsonFromString("{\"text\":\"Failed to read item\",\"color\":\"red\"}").text() :
            mutableText).append(Text.of("\n"+itemData)));
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
                else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
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
        if(savedItems == null) {
            savedError = true;
            NbtList nbt = new NbtList();
            while(nbt.size()<9*FortytwoEdit.SAVED_ROWS) {
                NbtCompound air = new NbtCompound();
                nbt.add(air);
            }
            savedItems = nbt;
        }
        updateSavedTab();
    }
    private void getWebItems() {
        webItems = new NbtList();

        if(FortytwoEdit.webItems != null)
            webItems = FortytwoEdit.webItems.copy();

        while(webItems.size()<9*FortytwoEdit.SAVED_ROWS) {
            NbtCompound air = new NbtCompound();
            webItems.add(air);
        }
        updateSavedTab();
    }
    private void updateSavedTab() {
        if(widgets.get(CACHE_TAB_SAVED).size() >= FortytwoEdit.SAVED_ROWS)
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
                widgets.get(CACHE_TAB_SAVED).get(i).updateSavedDisplay();
    }

    private void setSavedModeTooltip() {
        if(viewBlackMarket) {
            noScrollWidgets.get(CACHE_TAB_SAVED).get(0).w.setTooltip(Tooltip.of(Text.of("Black Market Items")));
            noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setTooltip(Tooltip.of(Text.of("Refresh from Web")));
            noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setMessage(Text.of("\u27F3"));
        }
        else {
            noScrollWidgets.get(CACHE_TAB_SAVED).get(0).w.setTooltip(Tooltip.of(Text.of("Local Items")));
            if(savedModeSet) {
                noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setTooltip(Tooltip.of(Text.of("C - Save to slot")));
                noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setMessage(Text.of("C"));
            }
            else {
                noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setTooltip(Tooltip.of(Text.of("V - Get item")));
                noScrollWidgets.get(CACHE_TAB_SAVED).get(1).w.setMessage(Text.of("V"));
            }
        }
    }

    private void resetSuggs() {
        currentTxt.clear();
        suggs = null;
        if(tab != CACHE_TAB_BLANK && tab != CACHE_TAB_NBT)
            setErrorMsg(null);
    }

    private NbtCompound updateArmorStand(ItemStack stand) {
        resetArmorStand();
        if(stand != null && !stand.isEmpty() && BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data",NbtElement.COMPOUND_TYPE) != null) {
            NbtCompound entity = (NbtCompound)BlackMagick.getNbtPath(BlackMagick.itemToNbt(stand),"components.minecraft:entity_data");
            entity.putString("id","armor_stand");
            entity.put("Pos",BlackMagick.nbtFromString("[0d,0d,0d]"));
            entity.put("Motion",BlackMagick.nbtFromString("[0d,0d,0d]"));
            entity.put("Rotation",BlackMagick.nbtFromString("[0f,0f]"));
            renderArmorStand.readNbt(entity.copy());
            updatePose();
            if(entity.contains("Pose",NbtElement.COMPOUND_TYPE))
                return (NbtCompound)entity.get("Pose").copy();
        }
        return null;
    }

    private void resetArmorStand() {
        renderArmorStand = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
        renderArmorStand.bodyYaw = 210.0f;
        renderArmorStand.setPitch(25.0f);
        renderArmorStand.headYaw = renderArmorStand.getYaw();
        renderArmorStand.prevHeadYaw = renderArmorStand.getYaw();
        editArmorStand = false;
    }

    protected NbtCompound updatePose() {
        renderArmorPose = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
        renderArmorPose.bodyYaw = 210f;
        renderArmorPose.setPitch(25f);
        renderArmorPose.headYaw = renderArmorPose.getYaw();
        renderArmorPose.prevHeadYaw = renderArmorPose.getYaw();
        NbtCompound nbt = new NbtCompound();
        if(renderArmorStand != null)
            nbt = renderArmorStand.writeNbt(new NbtCompound());
        NbtCompound pose = new NbtCompound();
        if(nbt.contains("Pose",NbtElement.COMPOUND_TYPE))
            pose = (NbtCompound)nbt.get("Pose");

        if(armorPose == null)
            armorPose = new float[6][];

        if(armorPose[0] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[0][i]));
            pose.put("Head",l);
        }
        else if(pose.contains("Head"))
            pose.remove("Head");

        if(armorPose[1] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[1][i]));
            pose.put("RightArm",l);
        }
        else if(pose.contains("RightArm"))
            pose.remove("RightArm");

        if(armorPose[2] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[2][i]));
            pose.put("LeftArm",l);
        }
        else if(pose.contains("LeftArm"))
            pose.remove("LeftArm");

        if(armorPose[3] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[3][i]));
            pose.put("RightLeg",l);
        }
        else if(pose.contains("RightLeg"))
            pose.remove("RightLeg");

        if(armorPose[4] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[4][i]));
            pose.put("LeftLeg",l);
        }
        else if(pose.contains("LeftLeg"))
            pose.remove("LeftLeg");

        if(armorPose[5] != null) {
            NbtList l = new NbtList();
            for(int i=0; i<3; i++)
                l.add(NbtFloat.of(armorPose[5][i]));
            pose.put("Body",l);
        }
        else if(pose.contains("Body"))
            pose.remove("Body");

        nbt.put("Pose",pose.copy());
        renderArmorPose.readNbt(nbt.copy());

        if(setPoseButton != null)
            setPoseButton.setTooltip(Tooltip.of(Text.of("Set Pose\n\nPose:"+pose.asString())));

        return pose.copy();
    }

    // private void btnResetPose() {//TODO old pose logic start
    //     for(int i=0; i<sliders.size(); i++)
    //         sliders.get(i).setSlider(0f);

    //     for(int i=0; i<sliderBtns.size(); i++) {
    //         sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("No pose")));
    //         sliderBtns.get(i).btns[0].active = false;
    //     }

    //     armorPose = null;
    //     updatePose();
    //     unsavedPose = false;
    //     unsel = true;
    // }

    // private void btnGetPose() {
    //     btnResetPose();
    //     if(client.player.getMainHandStack().getItem().toString().equals("armor_stand") && sliders.size()==6*3) {
    //         NbtCompound pose = updateArmorStand(client.player.getMainHandStack().copy());
    //         if(pose != null) {
    //             armorPose = new float[6][];

    //             String part = "Head";
    //             int partNum = 0;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }
                
    //             part = "RightArm";
    //             partNum = 1;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }
                
    //             part = "LeftArm";
    //             partNum = 2;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }
                
    //             part = "RightLeg";
    //             partNum = 3;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }
                
    //             part = "LeftLeg";
    //             partNum = 4;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }
                
    //             part = "Body";
    //             partNum = 5;
    //             if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
    //                     && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
    //                 NbtList list = (NbtList)pose.get(part);
    //                 armorPose[partNum] = new float[3];
    //                 for(int i=0; i<3; i++)
    //                     sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
    //             }

    //         }
    //     }
    //     updatePose();
    //     unsavedPose = false;
    //     unsel = true;

    //     for(int i=0; i<armorPose.length; i++) {
    //         if(armorPose[i] == null) {
    //             sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("No pose")));
    //             sliderBtns.get(i).btns[0].active = false;
    //         }
    //         else {
    //             sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("Remove Pose")));
    //             if(client.player.getAbilities().creativeMode)
    //                 sliderBtns.get(i).btns[0].active = true;
    //         }
    //     }
    // }

    // private void btnSetPose() {
    //     // ItemStack item = BlackMagick.setId("armor_stand");
    //     // if(armorPose != null) {
    //     //     boolean empty = true;
    //     //     for(int i=0; i<armorPose.length; i++)
    //     //         if(armorPose[i] != null)
    //     //             empty = false;

    //     //     if(!empty) {
    //     //         BlackMagick.setNbt(item,"EntityTag/Pose",updatePose());
    //     //         unsavedPose = false;
    //     //         unsel = true;
    //     //         return;
    //     //     }
    //     // }
    //     // BlackMagick.removeNbt(item,"EntityTag/Pose");
    //     // unsavedPose = false;
    //     unsel = true;
    // }

    public static String[] getStatesArr() {
        if(cacheStates.size()>0) {
            String[] temp = new String[cacheStates.size()];
            for(int i=0; i<temp.length; i++)
                temp[i]=cacheStates.get(i).get(0);
            return temp;
        }
        return null;
    }

    public static String[] getStateVals(String key) {
        if(cacheStates.size()>0) {
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

    private void updateRgbSliders() {
        for(int i=0; i<rgbSliders.size(); i++)
            rgbSliders.get(i).setSlider(rgb[i]);
        for(int i=0; i<rgbChanged.length; i++)
            rgbChanged[i] = true;

        if(cacheI.containsKey("jsonEffectBtns") && widgets.get(cacheI.get("jsonEffectBtns")[0]).size()>cacheI.get("jsonEffectBtns")[1]+2+3) {
            int[] jsonEffectBtnsI = cacheI.get("jsonEffectBtns");
            if(jsonEffectMode == 0) {
                for(int rgbNum=0; rgbNum<2; rgbNum++) {
                    for(int i=0; i<3; i++)
                        ((RgbSlider)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+2+i).wids[rgbNum].w).setVal(rgb[i+3*rgbNum]);

                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+2+3).wids[rgbNum].w).setText(""+getRgbHex(rgbNum));
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+2+3).wids[rgbNum].w).setEditableColor(getRgbDec(rgbNum));
                }
                updateJsonEffect();
            }
            else if(jsonEffectMode == 1) {
                for(int i=0; i<3; i++)
                    ((RgbSlider)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+2+i).wids[0].w).setVal(rgb[i]);

                if(jsonEffects[6]==0) {
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setText(""+getRgbHex(0));
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setEditableColor(getRgbDec(0));
                    updateJsonEffect();
                }
            }
        }
        
    }

    private void updateRgbItems() {
        rgbItems[0] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString("{id:leather_chestplate,components:{dyed_color:"+getRgbDec()+"}}"));
        rgbItems[1] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString("{id:potion,components:{potion_contents:{custom_color:"+getRgbDec()+"}}}"));
        rgbItems[2] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString("{id:filled_map,components:{map_color:"+getRgbDec()+"}}"));
    }

    private int getRgbDec() {
        return getRgbDec(0);
    }
    private int getRgbDec(int rgbNum) {
        return rgb[0+3*rgbNum]*256*256+rgb[1+3*rgbNum]*256+rgb[2+3*rgbNum];
    }

    private String getRgbHex() {
        return getRgbHex(0);
    }
    private String getRgbHex(int rgbNum) {
        String hex = "#";
        for(int i=0; i<3; i++) {
            String current = Integer.toHexString(rgb[i+3*rgbNum]).toUpperCase();
            if(current.length()==1)
                current = "0" + current;
            hex += current;
        }
        return hex;
    }

    private void drawItem(DrawContext context, ItemStack item, int x, int y) {
        context.drawItem(item,x,y);
        context.drawItemInSlot(this.textRenderer,item,x,y);
    }

    private void updateJsonPreview(String path, String jsonBase) {
        updateJsonPreview(path,jsonBase,null);
    }
    private void updateJsonPreview(String path, String jsonBase, String jsonEffect) {
        jsonBaseText = jsonBase;
        jsonBaseValid = false;
        jsonEffectValid = false;
        jsonPreviewBook = false;
        jsonPreview = BlackMagick.jsonFromString(jsonBase).text();
        if(BlackMagick.jsonFromString(jsonBase).isValid()) {
            jsonBaseValid = true;
            if(jsonEffect != null && BlackMagick.jsonFromString(appendJsonEffect(jsonBase,jsonEffect)).isValid()) {
                jsonPreview = BlackMagick.jsonFromString(appendJsonEffect(jsonBase,jsonEffect)).text();
                if(jsonEffect.length()>0 && !jsonEffect.equals("{\"text\":\"\"}"))
                    jsonEffectValid = true;
                jsonEffectFull = appendJsonEffect(jsonBase,jsonEffect);
            }
            if(path != null) {
                if(path.endsWith("custom_name"))
                    jsonPreview = BlackMagick.jsonFromString("{\"text\":\"\",\"italic\":true}").text().copy().append(jsonPreview.copy());
                else if(path.contains("lore["))
                    jsonPreview = BlackMagick.jsonFromString("{\"text\":\"\",\"color\":\"dark_purple\",\"italic\":true}").text().copy().append(jsonPreview.copy());
                else if(path.contains("written_book_content.pages["))
                    jsonPreviewBook = true;
            }
        }
        if(jsonEffectMode>=0 && cacheI.containsKey("jsonAdd") && cacheI.get("jsonAdd")[0] == tab && noScrollWidgets.size() > cacheI.get("jsonAdd")[0]
        && noScrollWidgets.get(cacheI.get("jsonAdd")[0]).size() > cacheI.get("jsonAdd")[1]) {
            int[] jsonAddBtn = cacheI.get("jsonAdd");
            if(jsonEffectValid && jsonEffect != null && jsonEffect.length()>0) {
                ((ButtonWidget)noScrollWidgets.get(jsonAddBtn[0]).get(jsonAddBtn[1]).w).active = true;
                ((ButtonWidget)noScrollWidgets.get(jsonAddBtn[0]).get(jsonAddBtn[1]).w).setTooltip(Tooltip.of(Text.of("Set text to:\n"+jsonEffectFull)));
            }
            else {
                ((ButtonWidget)noScrollWidgets.get(jsonAddBtn[0]).get(jsonAddBtn[1]).w).active = false;
                ((ButtonWidget)noScrollWidgets.get(jsonAddBtn[0]).get(jsonAddBtn[1]).w).setTooltip(Tooltip.of(Text.of("Invalid JSON")));
            }
        }
    }

    private String appendJsonEffect(String jsonBase, String jsonEffect) {
        if(jsonBase.length()>1 && jsonBase.startsWith("\"") && jsonBase.endsWith("\""))
            jsonBase = "{\"text\":"+jsonBase+"}";
        else if(jsonBase.length()>1 && jsonBase.startsWith("'") && jsonBase.endsWith("'"))
            jsonBase = "{\"text\":\""+jsonBase.substring(0,jsonBase.length()-1)+"\"}";
        else if(!(jsonBase.startsWith("{") && jsonBase.endsWith("}"))
        && !(jsonBase.startsWith("[") && jsonBase.endsWith("]")) && !jsonBase.contains("\"") && !jsonBase.contains("'"))
            jsonBase = "{\"text\":\""+jsonBase+"\"}";

        if(jsonEffectMode == 0 || jsonEffectMode == 1) {
            if(jsonBase.length()==0 || jsonBase.equals("{}") || jsonBase.equals("[]") || jsonBase.equals("[{}]") || jsonBase.equals("{\"text\":\"\"}") || jsonBase.equals("[{\"text\":\"\"}]"))
                return jsonEffect;
            else if(jsonBase.length()>=4 && jsonBase.charAt(0)=='[' && jsonBase.charAt(jsonBase.length()-1)==']'
            && jsonBase.charAt(1)=='{' && jsonBase.charAt(jsonBase.length()-2)=='}')
                return jsonBase.substring(0,jsonBase.length()-1) +","+ jsonEffect +"]";
            else if(jsonBase.length()>=2 && jsonBase.charAt(0)=='{' && jsonBase.charAt(jsonBase.length()-1)=='}')
                return "["+jsonBase+","+jsonEffect+"]";
            return null;
        }
        else if(jsonEffectMode == 2) {
            if(jsonBase.length()>4 && jsonBase.charAt(0)=='[' && jsonBase.charAt(jsonBase.length()-1)==']'
            && jsonBase.charAt(1)=='{' && jsonBase.charAt(jsonBase.length()-2)=='}')
                return jsonBase.substring(0,jsonBase.length()-2) + jsonEffect +"}]";
            else if(jsonBase.length()>2 && jsonBase.charAt(0)=='{' && jsonBase.charAt(jsonBase.length()-1)=='}')
                return jsonBase.substring(0,jsonBase.length()-1) + jsonEffect +"}";
            return null;
        }
        else
            return null;
    }

    private void updateJsonEffectBtns() {
        if(cacheI.containsKey("jsonEffectBtns") && (jsonEffectMode == 0 || jsonEffectMode == 1)) {
            int[] jsonEffectBtnsI = cacheI.get("jsonEffectBtns");
            int num = 0;
            String col = "";
            if(jsonEffects[num]==1)
                col = "\u00a7a";
            else if(jsonEffects[num]==2)
                col = "\u00a7c";
            widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]).btns[num].setMessage(Text.of(col+"\u00a7ll"));
            num++;
            col = "";
            if(jsonEffects[num]==1)
                col = "\u00a7a";
            else if(jsonEffects[num]==2)
                col = "\u00a7c";
            widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]).btns[num].setMessage(Text.of(col+"\u00a7oo"));
            num++;
            col = "";
            if(jsonEffects[num]==1)
                col = "\u00a7a";
            else if(jsonEffects[num]==2)
                col = "\u00a7c";
            widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]).btns[num].setMessage(Text.of(col+"\u00a7nn"));
            num++;
            col = "";
            if(jsonEffects[num]==1)
                col = "\u00a7a";
            else if(jsonEffects[num]==2)
                col = "\u00a7c";
            widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]).btns[num].setMessage(Text.of(col+"\u00a7mm"));
            num++;
            col = "";
            if(jsonEffects[num]==1)
                col = "\u00a7a";
            else if(jsonEffects[num]==2)
                col = "\u00a7c";
            widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]).btns[num].setMessage(Text.of(col+"\u00a7kk"));
            if(jsonEffectMode == 0) {
                widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).btns[0].setMessage(Text.of("[Radial]"));
                if(jsonEffects[5]==1)
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).btns[0].setMessage(Text.of("[Linear]"));
            }
            else if(jsonEffectMode == 1) {
                ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setEditableColor(0xFFFFFF);
                ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setEditable(true);

                if(jsonEffects[6]==0) {
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[0].w.setMessage(Text.of("Color [RGB]"));
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setText(getRgbHex());
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setEditableColor(getRgbDec());
                }
                else if(jsonEffects[6]==1) {
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[0].w.setMessage(Text.of("Color [Vanilla]"));
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setText(jsonLastColor);
                }
                else if(jsonEffects[6]==2) {
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[0].w.setMessage(Text.of("Color [None]"));
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setText("<None>");
                    ((TextFieldWidget)widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+1).wids[1].w).setEditable(false);
                }

                widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+5).btns[0].setMessage(Text.of("[Text]"));
                if(jsonEffects[7]==1)
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+5).btns[0].setMessage(Text.of("[Keybind]"));
                else if(jsonEffects[7]==2)
                    widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+5).btns[0].setMessage(Text.of("[Translate]"));
            }
        }
    }

    private void updateJsonEffect() {
        if(jsonEffectPath == null || jsonEffectBase == null)
            return;
        if(jsonEffectMode == 0) {
            if(!cacheI.containsKey("jsonEffectTxt") || tab != cacheI.get("jsonEffectTxt")[0])
                return;
            String value = ((TextFieldWidget)widgets.get(cacheI.get("jsonEffectTxt")[0]).get(cacheI.get("jsonEffectTxt")[1]).wids[0].w).getText();
            String val = "";
            if(value.length()==1 || (rgb[0]==rgb[3] && rgb[1]==rgb[4] && rgb[2]==rgb[5])) {
                val+="{\"text\":\"";
                for(int i=0; i<value.length(); i++) {
                    String thisChar = ""+value.charAt(i);
                    if(thisChar.equals("\\") || thisChar.equals("\""))
                        thisChar = "\\"+thisChar;
                    val+=thisChar;
                }
                val+="\",\"color\":\""+getRgbHex()+"\"";
                if(jsonEffects[0]==1)
                    val+=",\"bold\":true";
                else if(jsonEffects[0]==2)
                    val+=",\"bold\":false";
                if(jsonEffects[1]==1)
                    val+=",\"italic\":true";
                else if(jsonEffects[1]==2)
                    val+=",\"italic\":false";
                if(jsonEffects[2]==1)
                    val+=",\"underlined\":true";
                else if(jsonEffects[2]==2)
                    val+=",\"underlined\":false";
                if(jsonEffects[3]==1)
                    val+=",\"strikethrough\":true";
                else if(jsonEffects[3]==2)
                    val+=",\"strikethrough\":false";
                if(jsonEffects[4]==1)
                    val+=",\"obfuscated\":true";
                else if(jsonEffects[4]==2)
                    val+=",\"obfuscated\":false";
                val+="}";
            }
            else if(value.length() > 1) {
                val+="{\"text\":\"";
                for(int i=0; i<1; i++) {
                    String thisChar = ""+value.charAt(i);
                    if(thisChar.equals("\\") || thisChar.equals("\""))
                        thisChar = "\\"+thisChar;
                    val+=thisChar;
                }
                val+="\",\"color\":\"#";
                for(int c=0; c<3; c++) {
                    int col = 0;
                    if(jsonEffects[5]==1)
                        col = rgb[c];
                    else {
                        col = rgb[c+3];
                    }
                    String current = Integer.toHexString(col).toUpperCase();
                    if(current.length()==1)
                        current = "0" + current;
                    val += current;
                }
                val+="\"";
                if(jsonEffects[0]==1)
                    val+=",\"bold\":true";
                else if(jsonEffects[0]==2)
                    val+=",\"bold\":false";
                if(jsonEffects[1]==1)
                    val+=",\"italic\":true";
                else if(jsonEffects[1]==2)
                    val+=",\"italic\":false";
                if(jsonEffects[2]==1)
                    val+=",\"underlined\":true";
                else if(jsonEffects[2]==2)
                    val+=",\"underlined\":false";
                if(jsonEffects[3]==1)
                    val+=",\"strikethrough\":true";
                else if(jsonEffects[3]==2)
                    val+=",\"strikethrough\":false";
                if(jsonEffects[4]==1)
                    val+=",\"obfuscated\":true";
                else if(jsonEffects[4]==2)
                    val+=",\"obfuscated\":false";
                val+=",\"extra\":[";
                boolean firstPart = true;
                for(int i=1; i<value.length(); i++) {
                    if(!firstPart)
                        val+=",";
                    String thisChar = ""+value.charAt(i);
                    if(thisChar.equals("\\") || thisChar.equals("\""))
                        thisChar = "\\"+thisChar;
                    val+="{\"text\":\""+thisChar+"\",\"color\":\"#";
                    for(int c=0; c<3; c++) {
                        int col = 0;
                        if(jsonEffects[5]==1)
                            col = rgb[c] + (int)((rgb[c+3]-rgb[c])*i/((double)(value.length()-1)));
                        else {
                            if(value.length()%2==0) {
                                if(i<value.length()/2)
                                    col = rgb[c+3] + (int)((rgb[c]-rgb[c+3])*i/((double)(value.length()/2-1)));
                                else
                                    col = rgb[c+3] + (int)((rgb[c]-rgb[c+3])*(value.length()-i-1)/((double)(value.length()/2-1)));
                            }
                            else {
                                if(i<=value.length()/2)
                                    col = rgb[c+3] + (int)((rgb[c]-rgb[c+3])*i/((double)(value.length()/2)));
                                else
                                    col = rgb[c+3] + (int)((rgb[c]-rgb[c+3])*(value.length()-i-1)/((double)(value.length()/2)));
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
                val = "{\"text\":\"\"}";
            updateJsonPreview(jsonEffectPath,jsonEffectBase,val);
        }
        else if(jsonEffectMode == 1) {
            if(!cacheI.containsKey("jsonEffectTxt") || tab != cacheI.get("jsonEffectTxt")[0])
                return;
            String value = ((TextFieldWidget)widgets.get(cacheI.get("jsonEffectTxt")[0]).get(cacheI.get("jsonEffectTxt")[1]).wids[0].w).getText();
            String val = "{";
            if(jsonEffects[7] == 0) {
                val+="\"text\":\"";
                for(int i=0; i<value.length(); i++) {
                    String thisChar = ""+value.charAt(i);
                    if(thisChar.equals("\\") || thisChar.equals("\""))
                        thisChar = "\\"+thisChar;
                    val+=thisChar;
                }
                val+="\"";
            }
            else if(jsonEffects[7] == 1) {
                val+="\"keybind\":\""+value+"\"";
            }
            else if(jsonEffects[7] == 2) {
                int[] jsonEffectBtnsI = cacheI.get("jsonEffectBtns");
                val+="\"translate\":\""+value+"\"";
                if(widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+7).txts[0].getText() != null
                        && widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+7).txts[0].getText().length()>0)
                    val+=",\"with\":"+widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+7).txts[0].getText();
                if(widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+8).txts[0].getText() != null
                        && widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+8).txts[0].getText().length()>0)
                    val+=",\"fallback\":\""+widgets.get(jsonEffectBtnsI[0]).get(jsonEffectBtnsI[1]+8).txts[0].getText()+"\"";
            }
            if(jsonEffects[6]==0)
                val+=",\"color\":\""+getRgbHex()+"\"";
            else if(jsonEffects[6]==1)
                val+=",\"color\":\""+jsonLastColor+"\"";
            if(jsonEffects[0]==1)
                val+=",\"bold\":true";
            else if(jsonEffects[0]==2)
                val+=",\"bold\":false";
            if(jsonEffects[1]==1)
                val+=",\"italic\":true";
            else if(jsonEffects[1]==2)
                val+=",\"italic\":false";
            if(jsonEffects[2]==1)
                val+=",\"underlined\":true";
            else if(jsonEffects[2]==2)
                val+=",\"underlined\":false";
            if(jsonEffects[3]==1)
                val+=",\"strikethrough\":true";
            else if(jsonEffects[3]==2)
                val+=",\"strikethrough\":false";
            if(jsonEffects[4]==1)
                val+=",\"obfuscated\":true";
            else if(jsonEffects[4]==2)
                val+=",\"obfuscated\":false";
            val+="}";
            if(value == null || value.equals(""))
                val = "{\"text\":\"\"}";
            updateJsonPreview(jsonEffectPath,jsonEffectBase,val);
        }
        else if(jsonEffectMode == 2) {
            if(!cacheI.containsKey("clickEventLbl") || tab != cacheI.get("clickEventLbl")[0])
                return;
            int[] clickEventLblI = cacheI.get("clickEventLbl");
            String val = "";
            String click = ((TextFieldWidget)widgets.get(clickEventLblI[0]).get(clickEventLblI[1]+1).txts[0]).getText();
            String value = ((TextFieldWidget)widgets.get(clickEventLblI[0]).get(clickEventLblI[1]+2).txts[0]).getText();
            String hover = ((TextFieldWidget)widgets.get(clickEventLblI[0]).get(clickEventLblI[1]+4).txts[0]).getText();
            String contents = ((TextFieldWidget)widgets.get(clickEventLblI[0]).get(clickEventLblI[1]+5).txts[0]).getText();
            if(click != null && click.length()>0 && value != null && value.length()>0) {
                val += ",\"clickEvent\":{\"action\":\""+click+"\",\"value\":\"";
                for(int i=0; i<value.length(); i++) {
                    String thisChar = ""+value.charAt(i);
                    if(thisChar.equals("\\") || thisChar.equals("\""))
                        thisChar = "\\"+thisChar;
                    val+=thisChar;
                }
                val += "\"}";
            }
            if(hover != null && hover.length()>0 && contents != null && contents.length()>0) {
                val += ",\"hoverEvent\":{\"action\":\""+hover+"\",\"contents\":"+contents+"}";
            }
            updateJsonPreview(jsonEffectPath,jsonEffectBase,val);
        }

    }
    
    private Style getBookTextStyleAt(List<OrderedText> page, int bookRenderX, int bookRenderY, double x, double y) {
        if (page.isEmpty()) {
            return null;
        }
        int i = MathHelper.floor(x - (double)bookRenderX - 36.0);
        int j = MathHelper.floor(y - 2.0 - 30.0 - (double)bookRenderY);
        if (i < 0 || j < 0) {
            return null;
        }
        int k = Math.min(128 / this.textRenderer.fontHeight, page.size());
        if (i <= 114 && j < this.client.textRenderer.fontHeight * k + k) {
            int l = j / this.client.textRenderer.fontHeight;
            if (l >= 0 && l < page.size()) {
                OrderedText orderedText = page.get(l);
                return this.client.textRenderer.getTextHandler().getStyleAt(orderedText, i);
            }
            return null;
        }
        return null;
    }

    private void updateInvTab() {
        boolean changed = false;
        for(int i=0; i<cacheInv.length; i++) {
            ItemStack current = null;
            if(i<27)
                current = client.player.getInventory().main.get(i+9).copy();
            else if(i<36)
                current = client.player.getInventory().main.get(i-27).copy();
            else if(i<40)
                current = client.player.getInventory().armor.get(i-36).copy();
            else if(i<41)
                current = client.player.getInventory().offHand.get(i-40).copy();
            if(cacheInv[i] == null || !ItemStack.areEqual(cacheInv[i],current)) {
                changed = true;
                cacheInv[i] = current;
            }
        }
        if(cacheInvSlot != client.player.getInventory().selectedSlot) {
            changed = true;
            cacheInvSlot = client.player.getInventory().selectedSlot;
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
            path = path.replaceFirst("components.","");
        return path.replace("minecraft:","");
    }

    private String trimStringSize(String inp) {
        int maxSize = this.width-10;
        if(ItemBuilder.this.textRenderer.getWidth(inp)>maxSize && inp.length()>1) {
            String trail = "...";
            maxSize -= textRenderer.getWidth(trail);
            if(width>10)
                inp = ItemBuilder.this.textRenderer.trimToWidth(inp,maxSize);
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

    private Text getButtonText(String path, NbtElement el) {
        if(el==null)
            return Text.empty();
        String elString = el==null ? "null" : BlackMagick.nbtToString(el);
        String elStringContent = elString;
        if(el != null && el.getType() == NbtElement.STRING_TYPE)
            elStringContent = el.asString();
        PathInfo pi = ComponentHelper.getPathInfo(path);
        if(pi.type()==PathType.TEXT) {
            Text btnTxt = BlackMagick.jsonFromString((el==null || el.getType()!=NbtElement.STRING_TYPE) ? "" : elStringContent).text();
            if(el != null && el.getType()==NbtElement.STRING_TYPE && BlackMagick.jsonFromString(elStringContent).isValid()) {
                if(path.endsWith("custom_name"))
                    btnTxt = BlackMagick.jsonFromString("{\"text\":\"\",\"italic\":true}").text().copy().append(btnTxt.copy());
                else if(path.contains("lore["))
                    btnTxt = BlackMagick.jsonFromString("{\"text\":\"\",\"color\":\"dark_purple\",\"italic\":true}").text().copy().append(btnTxt.copy());
            }
            return btnTxt;
        }
        else if(pi.type()==PathType.DECIMAL_COLOR) {
            if(el!=null && el.getType()!=NbtElement.STRING_TYPE && el.getType()!=NbtElement.LIST_TYPE && el.getType()!=NbtElement.COMPOUND_TYPE) {
                if(BlackMagick.colorHexFromDec(elStringContent) != null)
                    return BlackMagick.jsonFromString("{\"text\":\""+elStringContent+"\",\"color\":\""+BlackMagick.colorHexFromDec(elStringContent)+"\"}").text();
                return BlackMagick.jsonFromString("{\"text\":\"Invalid color: "+elStringContent+"\",\"color\":\"red\"}").text();
            }
            return Text.of("Invalid color").copy().formatted(Formatting.RED);
        }
        else if(pi.flag()==PathFlag.ATTRIBUTE) {
            if(el != null && el.getType()==NbtElement.COMPOUND_TYPE) {
                ItemStack stack = BlackMagick.itemFromString("{id:stone,components:{attribute_modifiers:{modifiers:["+el.asString()+"]}}}");
                if(!stack.isEmpty()) {
                    List<Text> textList = stack.getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                    if(textList.size()>21)
                        return Text.of("Any slot: ").copy().append(textList.get(3));
                    if(textList.size()>12)
                        return Text.of("When equipped: ").copy().append(textList.get(3));
                    if(textList.size()>6)
                        return Text.of("When held: ").copy().append(textList.get(3));
                    else if(textList.size()>3)
                        return Text.of(textList.get(2).getString()).copy().append(Text.of(" ")).append(textList.get(3));
                }
            }
            return Text.of("Invalid attribute").copy().formatted(Formatting.RED);
        }
        else if(pi.type()==PathType.BANNER) {
            if(el != null && el.getType()==NbtElement.COMPOUND_TYPE) {
                ItemStack stack = BlackMagick.itemFromString("{id:white_banner,components:{banner_patterns:["+el.asString()+"]}}");
                if(!stack.isEmpty()) {
                    List<Text> textList = stack.getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                    if(textList.size()>1)
                        return Text.of(textList.get(1).getString());
                }
            }
            return Text.of("Invalid pattern").copy().formatted(Formatting.RED);
        }
        else if(pi.flag()==PathFlag.EFFECT) {
            if(el != null && el.getType()==NbtElement.COMPOUND_TYPE) {
                ItemStack stack = BlackMagick.itemFromString("{id:potion,components:{potion_contents:{custom_effects:["+el.asString()+"]}}}");
                if(!stack.isEmpty()) {
                    List<Text> textList = stack.getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                    if(textList.size()>1 && !textList.get(1).getString().equals("No Effects"))
                        return textList.get(1);
                }
            }
            return Text.of("Invalid effect").copy().formatted(Formatting.RED);
        }
        else if(pi.flag()==PathFlag.PROBABILITY_EFFECT) {
            if(el != null && el.getType()==NbtElement.COMPOUND_TYPE) {
                NbtCompound nbt = (NbtCompound)el;
                if(nbt.contains("effect",NbtElement.COMPOUND_TYPE)) {
                    ItemStack stack = BlackMagick.itemFromString("{id:potion,components:{potion_contents:{custom_effects:["+nbt.getCompound("effect").asString()+"]}}}");
                    if(!stack.isEmpty()) {
                        List<Text> textList = stack.getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                        if(textList.size()>1 && !textList.get(1).getString().equals("No Effects"))
                            return textList.get(1);
                    }
                }
            }
            return Text.of("Invalid effect").copy().formatted(Formatting.RED);
        }
        else if(pi.flag()==PathFlag.FIREWORK) {
            if(el != null && el.getType()==NbtElement.COMPOUND_TYPE) {
                ItemStack stack = BlackMagick.itemFromString("{id:firework_star,components:{firework_explosion:"+el.asString()+"}}");
                if(!stack.isEmpty()) {
                    List<Text> textList = stack.getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                    if(textList.size()>1) {
                        MutableText btnLbl = Text.empty();
                        for(int i=1; i<textList.size(); i++) {
                            if(i>1)
                                btnLbl.append(Text.of(", "));
                            btnLbl.append(textList.get(i));
                        }
                        return Text.of(btnLbl.getString());
                    }
                }
            }
            return Text.of("Invalid explosion").copy().formatted(Formatting.RED);
        }
        return Text.of(elString);
    }

    private Text getButtonTooltip(PathInfo pi, String key) {
        MutableText btnTt = Text.empty().append(Text.of("Key: "+key));
        if(ComponentHelper.pathTypeToNbtType(pi.type()) != -1)
            btnTt = btnTt.append(Text.of("\nNBT Type: "+ComponentHelper.formatNbtType(ComponentHelper.pathTypeToNbtType(pi.type()))));
        if(pi.description() != null)
            btnTt = btnTt.append(Text.of("\n\n")).append(pi.description());
        return btnTt;
    }

    private void suggsOnChanged(TextFieldWidget w, String[] suggestions, String startVal) {
        if(w == null)
            return;

        boolean shouldSetSuggs = false;
        if(!currentTxt.contains(w)) {
            resetSuggs();
            currentTxt.add(w);
            suggs = new TextSuggestor(client, w, textRenderer);
            shouldSetSuggs = true;
        }
        else{
            if(suggs != null)
                suggs.refresh();
            else {
                resetSuggs();
                suggs = new TextSuggestor(client, w, textRenderer);
                shouldSetSuggs = true;
            }
        }
        if(shouldSetSuggs) {
            if(suggs == null)
                return;
            String[] startVals = null;
            if(startVal != null)
                startVals = new String[]{startVal};
            String[][] joinSuggs = null;
            if(suggestions != null)
                joinSuggs = new String[][]{suggestions};
            String[] suggsArr = FortytwoEdit.joinCommandSuggs(joinSuggs, null, startVals);
            if(suggsArr != null && suggsArr.length>0)
                suggs.setSuggestions(suggsArr);
        }
    }

    public void setEditingElement(String path, NbtElement newEl, ButtonWidget saveBtn) {
        setEditingElement(path,newEl,saveBtn,null);
    }

    public void setEditingElement(String path, NbtElement newEl, ButtonWidget saveBtn, String pagePath) {
        blankTabEl = newEl;
        blankTabUnsaved = true;
        
        NbtElement displayEl = null;
        if(pagePath == null)
            saveBtn.setTooltip(Tooltip.of(Text.of("Invalid component:\n" + (blankTabEl==null ? "null" : BlackMagick.nbtToString(blankTabEl)))));
        else {
            displayEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),pagePath);
            saveBtn.setTooltip(Tooltip.of(Text.of("Invalid element:\n" + (displayEl==null ? "null" : BlackMagick.nbtToString(displayEl)))));
        }
        saveBtn.active = pagePath!=null;

        if(inpError == null)
            setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl).asString(),inpError));

        if(blankTabEl != null && inpError == null) {
            ItemStack newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl));
            if(ItemStack.areEqual(selItem,newItem)) {
                blankTabUnsaved = false;
                if(pagePath == null)
                    saveBtn.setTooltip(Tooltip.of(Text.of("Item unchanged")));
                else {
                    Text tempText = Text.of("Element value:\n").copy().append(BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath), displayEl));
                    if(displayEl == null && BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath) == null)
                        tempText = Text.of("Element value:\nnull");
                    saveBtn.setTooltip(Tooltip.of(tempText));
                }
            }
            else if(newItem != null) {
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(newItem),path) != null) {
                    if(pagePath == null) {
                        NbtElement modEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(BlackMagick.itemFromNbt(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl))),path);
                        if(modEl != null) {
                            Text tempText = Text.of("Set component:\n").copy().append(BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path), modEl));
                            if(client.player.getAbilities().creativeMode) {
                                saveBtn.setTooltip(Tooltip.of(tempText));
                                saveBtn.active = true;
                            }
                            else {
                                saveBtn.setTooltip(Tooltip.of(ERROR_CREATIVE.copy().append("\n").append(tempText)));
                            }
                        }
                    }
                    else {
                        Text tempText = Text.of("Element value:\n").copy().append(BlackMagick.getElementDifferences(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath), displayEl));
                        if(displayEl == null && BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),pagePath) == null)
                            tempText = Text.of("Element value:\nnull");
                        saveBtn.setTooltip(Tooltip.of(tempText));
                    }
                }
            }
        }
    }

    /**
     * Resets all tabs. Only use on first init when widgets.size()==0.
     * Creates widgets for static pages.
     */
    private void createStaticTabs() {
        widgets.clear();
        noScrollWidgets.clear();
        unsavedTxtWidgets.clear();
        allTxtWidgets.clear();
        for(int i=0; i<tabs.length; i++) {
            widgets.add(new ArrayList<RowWidget>());
            noScrollWidgets.add(new ArrayList<PosWidget>());
        }

        {
            int tabNum = CACHE_TAB_PRESETS;
            {
                widgets.get(tabNum).add(new RowWidget("\u00a76\u00a7oBaphomethLabs\u00a7r"));
            }
            {//public RowWidget(Text[] names, String[] tooltips, String[][] suggestions, boolean survival, PressAction... onPressActions) {
                widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("Item Lore"),Text.of("Bottle Lore"),Text.of("Watermark")},new int[]{65,64,65},
                new String[]{"Add \u00a76\u00a7oBaphomethLabs\u00a7r watermark to lore","Add \u00a76\u00a7oBottled by BaphomethLabs\u00a7r watermark to lore",
                "Add watermark in custom_data"},null,false,btn -> {
                    if(!selItem.isEmpty()) {
                        boolean removed = false;
                        NbtElement loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",NbtElement.LIST_TYPE);
                        if(loreEl != null) {
                            NbtList lore = (NbtList)loreEl;
                            if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'{\"color\":\"gold\",\"text\":\"BaphomethLabs\"}'")) {
                                removed = true;
                                lore.remove(lore.size()-1);
                                if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                            if(!removed && lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'{\"color\":\"gold\",\"text\":\"Bottled by BaphomethLabs\"}'")) {
                                lore.remove(lore.size()-1);
                                if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                        }
                        if(!removed) {
                            NbtList lore;
                            if(loreEl != null)
                                lore = (NbtList)loreEl;
                            else
                                lore = new NbtList();
                            lore.add(NbtString.of("\"\""));
                            lore.add(NbtString.of("{\"color\":\"gold\",\"text\":\"BaphomethLabs\"}"));
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel = true;
                }, btn -> {
                    if(!selItem.isEmpty()) {
                        boolean removed = false;
                        NbtElement loreEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:lore",NbtElement.LIST_TYPE);
                        if(loreEl != null) {
                            NbtList lore = (NbtList)loreEl;
                            if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'{\"color\":\"gold\",\"text\":\"Bottled by BaphomethLabs\"}'")) {
                                removed = true;
                                lore.remove(lore.size()-1);
                                if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                            if(!removed && lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'{\"color\":\"gold\",\"text\":\"BaphomethLabs\"}'")) {
                                lore.remove(lore.size()-1);
                                if(lore.size()>0 && BlackMagick.nbtToString(lore.get(lore.size()-1)).equals("'\"\"'"))
                                    lore.remove(lore.size()-1);
                                ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                    BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                                if(!newStack.isEmpty())
                                    BlackMagick.setItemMain(newStack);
                            }
                        }
                        if(!removed) {
                            NbtList lore;
                            if(loreEl != null)
                                lore = (NbtList)loreEl;
                            else
                                lore = new NbtList();
                            lore.add(NbtString.of("\"\""));
                            lore.add(NbtString.of("{\"color\":\"gold\",\"text\":\"Bottled by BaphomethLabs\"}"));
                            ItemStack newStack = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),"components.minecraft:lore",lore)));
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel = true;
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
                                BlackMagick.itemToNbt(selItem),"components.minecraft:custom_data.\"\u00a76\u00a7oBaphomethLabs\u00a7r\"",NbtInt.of(42))));
                            if(!newStack.isEmpty())
                                BlackMagick.setItemMain(newStack);
                        }
                    }
                    unsel = true;
                }));
            }
            {
                widgets.get(tabNum).add(new RowWidget("Player Heads"));
            }
            {
                final int i = tabNum; final int j = widgets.get(tabNum).size();
                widgets.get(tabNum).add(new RowWidget("Owner","Create player head from player name",btn -> {
                    String inp = widgets.get(i).get(j).btn()[0];
                    if(!inp.equals("")) {
                        if(client.player.getMainHandStack().isEmpty()) {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(
                                (NbtCompound)BlackMagick.nbtFromString("{id:player_head}"),"components.minecraft:profile",NbtString.of(inp))));
                        }
                        else {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:profile",NbtString.of(inp))));
                        }
                    }
                    else {
                        if(client.player.getMainHandStack().isEmpty())
                            BlackMagick.setItemMain(new ItemStack(Items.PLAYER_HEAD));
                        else {
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:profile",null)));
                        }
                    }
                },null,false));
            }
            {
                final int i = tabNum; final int j = widgets.get(tabNum).size();
                widgets.get(tabNum).add(new RowWidget("Skin","Create player head from give command (with the name removed)",btn -> {
                    String inp = widgets.get(i).get(j).btn()[0];
                    if(inp.equals("")) {
                        if(!client.player.getMainHandStack().isEmpty())
                            BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),"components.minecraft:profile",null)));
                    }
                    else if(inp.contains("name:\"textures\"") && inp.contains(",value:\"")) {
                        String value = inp;
                        value = value.substring(value.indexOf(",value:\"")+8);
                        if(value.contains("\"")) {
                            value = value.substring(0,value.indexOf("\""));
                            NbtCompound temp;
                            if(selItem.isEmpty())
                                temp = BlackMagick.validCompound(BlackMagick.nbtFromString("{id:player_head}"));
                            else
                                temp = BlackMagick.itemToNbt(selItem);
                            NbtElement parseValue = BlackMagick.nbtFromString("[{name:\"textures\",value:\""+value+"\"}]");
                            if(parseValue != null && parseValue.getType()==NbtElement.LIST_TYPE) {
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
                widgets.get(tabNum).add(new RowWidget("Sounds"));
            }
            {
                final int i = tabNum; final int j = widgets.get(tabNum).size();
                widgets.get(tabNum).add(new RowWidget("Play","Listen to the sound (client-side only)",btn -> {
                    String inp = widgets.get(i).get(j).btn()[0];
                    if(!inp.trim().equals("")) {
                        String sound = inp.trim();
                        sound = sound.replaceAll("[^a-zA-Z0-9_.:]","");
                        if(!sound.equals("")) {
                            client.player.playSoundToPlayer(SoundEvent.of(new Identifier(sound)), SoundCategory.MASTER, 1, 1);
                        }
                    }
                }, FortytwoEdit.SOUNDS,true));
            }
            {
                final int i = tabNum; final int j = widgets.get(tabNum).size();
                widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("Head Sound")},new int[]{80},
                        new String[]{"Create a preset head that plays the sound when on a note block"},null,false,btn -> {
                    String inp = widgets.get(i).get(j-1).btn()[0];
                    if(!inp.trim().equals("")) {
                        String sound = inp.trim();
                        sound = sound.replaceAll("[^a-zA-Z0-9_.:]","");
                        if(!sound.equals("")) {
                            ItemStack item = BlackMagick.itemFromString(
                                "{id:player_head,components:{profile:{properties:[{name:\"textures\",value:"+
                                "\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDov"+
                                "L3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2VlYjc3ZDRkMjU3MjRhOWNhZjJjN2NkZjJkODgzOTliMTQxN2M2YjlmZjUyMTM2NTliNjUzYmU0Mz"+
                                "c2ZTMiDQogICAgfQ0KICB9DQp9\"}]},note_block_sound:\""+sound+"\",item_name:'[{\"text\":\""+sound+"\"}]'}}");
                            if(!item.isEmpty())
                                BlackMagick.setItemMain(item);
                        }
                    }
                }));
            }
            {
                widgets.get(tabNum).add(new RowWidget("Banners"));
            }
            {
                final int i = tabNum; final int j = widgets.get(tabNum).size();
                widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("Symbol")},new int[]{40,55,49,55},
                new String[]{"Create banner(s) with preset designs\n\nChar Color | Chars | Base Color"+"\n\n"+BANNER_PRESET_CHARS},new String[][]
                {ComponentHelper.DYES,BANNER_CHAR_LIST,ComponentHelper.DYES},false,btn -> {
                    String[] inps = widgets.get(i).get(j).btn();
                    if(client.player.getAbilities().creativeMode) {

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
                            NbtList items = new NbtList();
                            while(chars.length()>0) {
                                NbtCompound bannerItem = BlackMagick.createBanner(chars.charAt(0),inps[2].toLowerCase(),inps[0].toLowerCase());
                                if(bannerItem!=null)
                                    items.add(bannerItem);
                                if(chars.length()==1)
                                    chars = "";
                                else
                                    chars = chars.substring(1);
                            }
                            if(items.size()>0)
                                bannerStack = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString("{id:bundle,components:{bundle_contents:"+items.asString()+"}}"));
                        }

                        if(!bannerStack.isEmpty()) {
                            BlackMagick.setItemMain(bannerStack);
                        }
                    }
                }));
            }
            {
                widgets.get(tabNum).add(new RowWidget());
            }
        }

        {
            int tabNum = CACHE_TAB_NBT;
            {
                final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
                cacheI.put("giveBox",new int[]{i,j});
                EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
                noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3,35));
                this.allTxtWidgets.add(w);
                w.setChangeListener(value -> {
                    setErrorMsg(null);
                    boolean isCompound = false;
                    ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).active = false;
                    ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).setTooltip(Tooltip.of(Text.of("Invalid item")));
                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = false;
                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("Not a compound")));
                    if(value != null && !value.trim().equals("")) {
                        String inp = ""+value;
                        ItemStack item = ItemStack.EMPTY;

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
                            if(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE) != null) {
                                isCompound = true;
                                item = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE));

                                if(!ItemStack.areEqual(BlackMagick.itemFromNbt(BlackMagick.itemToNbt(selItem).copyFrom(
                                BlackMagick.validCompound(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE)))),selItem)) {
                                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = true;
                                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("Merge on current item")));
                                }
                                else {
                                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = false;
                                    ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("Item unchanged")));
                                }
                            }

                            setErrorMsg(BlackMagick.getItemCompoundErrors(inp,inpError));
                            if(item.isEmpty() && inpError == null)
                                setErrorMsg("Invalid item");

                            if(!item.isEmpty() && inpError == null) {
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).active = true;
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).setTooltip(Tooltip.of(
                                    Text.of("Set current item to:\n"+BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(item)))));
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
                            
                            ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = false;
                            ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("Not a compound")));
                            try {
                                item = ItemStackArgumentType.itemStack(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createStack(1,false);
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
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).active = true;
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).setTooltip(Tooltip.of(
                                    Text.of("Set current item to:\n"+BlackMagick.nbtToString(BlackMagick.itemToNbtStorage(item)))));
                            }
                        }

                        if(ItemStack.areEqual(item,selItem)) {
                            ItemBuilder.this.markSaved(w);
                            ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).active = false;
                            ((ButtonWidget)noScrollWidgets.get(i).get(j+2).w).setTooltip(Tooltip.of(Text.of("Item unchanged")));
                            if(isCompound) {
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = false;
                                ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("Item unchanged")));
                            }
                        }
                        else {
                            ItemBuilder.this.markUnsaved(w);
                        }

                    }
                    else {
                        ItemBuilder.this.markSaved(w);
                    }

                    if(!value.equals(BlackMagick.itemToNbt(selItem).asString())) {
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).active = true;
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).setTooltip(Tooltip.of(Text.of("Copy current item")));
                    }
                    else {
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).active = false;
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).setTooltip(Tooltip.of(Text.of("Already cloned")));
                    }
                    if(selItem.isEmpty()) {
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).active = false;
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+1).w).setTooltip(Tooltip.of(Text.of("No item to clone")));
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).active = false;
                        ((ButtonWidget)noScrollWidgets.get(i).get(j+3).w).setTooltip(Tooltip.of(Text.of("No item to merge on")));
                    }
                });
            }
            {
                final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Clone"), button -> {
                    if(!client.player.getMainHandStack().isEmpty()) {
                        String itemData = BlackMagick.itemToNbt(client.player.getMainHandStack()).asString();
                        ((EditBoxWidget)noScrollWidgets.get(i).get(j-1).w).setText(itemData);
                    }
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3,y+35+22*6+1,60,20).build(),15-3,35+22*6+1));
            }
            {
                final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
                ButtonWidget w = ButtonWidget.builder(Text.of("Give"), btn -> {
                    String inp = ((EditBoxWidget)noScrollWidgets.get(i).get(j-2).w).getText();
                    ItemBuilder.this.markSaved((EditBoxWidget)noScrollWidgets.get(i).get(j-2).w);
                    ItemBuilder.this.unsel = true;

                    if(client.player.getAbilities().creativeMode) {
                        ItemStack item = ItemStack.EMPTY;

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
                            if(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE) != null) {
                                item = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE));
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
                                item = ItemStackArgumentType.itemStack(BlackMagick.getCommandRegistries()).parse(new StringReader(inp)).createStack(1,false);
                            } catch(Exception ex) {}

                            if(!item.isEmpty())
                                item.setCount(count);
                        }

                        BlackMagick.setItemMain(item);
                    }
                }).dimensions(x+15-3+5+60,y+35+22*6+1,60,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3+5+60,35+22*6+1));
            }
            {
                final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Merge"), button -> {
                    String inp = ((EditBoxWidget)noScrollWidgets.get(i).get(j-3).w).getText();
                    ItemBuilder.this.markSaved((EditBoxWidget)noScrollWidgets.get(i).get(j-3).w);
                    ItemBuilder.this.unsel = true;

                    if(client.player.getAbilities().creativeMode && !selItem.isEmpty()) {
                        NbtCompound nbt = null;

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
                            if(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE) != null) {
                                nbt = BlackMagick.validCompound(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE));
                            }
                        }

                        if(nbt != null) {
                            nbt = BlackMagick.itemToNbt(selItem).copyFrom(nbt);
                            if(!BlackMagick.itemFromNbt(nbt).isEmpty())
                                BlackMagick.setItemMain(BlackMagick.itemFromNbt(nbt));
                        }
                    }
                }).dimensions(x+15-3+5+60+5+60,y+35+22*6+1,60,20).build(),15-3+5+60+5+60,35+22*6+1));
            }
            {
                widgets.get(tabNum).add(new RowWidget());
            }
            ((EditBoxWidget)noScrollWidgets.get(tabNum).get(0).w).setText("");
        }

        {
            int tabNum = CACHE_TAB_SAVED;
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of(""), btn -> {
                    viewBlackMarket = !viewBlackMarket;
                    setSavedModeTooltip();
                    updateSavedTab();
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3, y+35+1,20,20).build(),15-3,35+1));
            }
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of(""), btn -> {
                    if(viewBlackMarket) {
                        FortytwoEdit.refreshWebItems(true);
                        getWebItems();

                        refreshSaved();

                        ((HotbarStorageAccessor)client.getCreativeHotbarStorage()).setLoaded(false);
                        client.getCreativeHotbarStorage().getSavedHotbar(0);
                    }
                    else {
                        savedModeSet = !savedModeSet;
                        setSavedModeTooltip();
                        updateSavedTab();
                    }
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3, y+35+1+22,20,20).build(),15-3,35+1+22));
            }
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
                widgets.get(tabNum).add(new RowWidget(i));
            {
                widgets.get(tabNum).add(new RowWidget());
            }
            refreshSaved();
            if(webItems == null)
                getWebItems();
            setSavedModeTooltip();
        }
        
        // //createBlock armor stand pose TODO old pose tab logic
        // {
        //     ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2611"), button -> this.btnSetPose()).dimensions(x+15-3, y+35+1,20,20).build();
        //     newButton.setTooltip(Tooltip.of(Text.of("Set Pose")));
        //     if(!client.player.getAbilities().creativeMode)
        //         newButton.active = false;
        //     setPoseButton = newButton;
        //     noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1));
        // }
        // {
        //     ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2227"), button -> this.btnGetPose()).dimensions(x+15-3, y+35+1+22,20,20).build();
        //     newButton.setTooltip(Tooltip.of(Text.of("Get from item")));
        //     noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1+22));
        // }
        // {
        //     ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2612"), button -> this.btnResetPose()).dimensions(x+15-3, y+35+1+22*2,20,20).build();
        //     newButton.setTooltip(Tooltip.of(Text.of("Clear All")));
        //     noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1+22*2));
        // }
        // {
        //     RowWidget newButton = new RowWidget(0,"Head");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(0,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(0,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(0,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newButton = new RowWidget(1,"Right Arm");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(1,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(1,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(1,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newButton = new RowWidget(2,"Left Arm");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(2,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(2,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(2,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newButton = new RowWidget(3,"Right Leg");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(3,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(3,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(3,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newButton = new RowWidget(4,"Left Leg");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(4,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(4,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(4,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newButton = new RowWidget(5,"Body");
        //     sliderBtns.add(newButton);
        //     widgets.get(tabNum).add(newButton);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(5,0);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(5,1);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // {
        //     RowWidget newSlider = new RowWidget(5,2);
        //     sliders.add(newSlider);
        //     widgets.get(tabNum).add(newSlider);
        // }
        // btnResetPose();

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
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        for(RowWidget r : widgets.get(tabNum)) {
            for(TextFieldWidget t : r.txts) {
                this.unsavedTxtWidgets.remove(t);
                this.allTxtWidgets.remove(t);
            }
        }
        for(PosWidget r : noScrollWidgets.get(tabNum)) {
            this.unsavedTxtWidgets.remove(r.w);
            this.allTxtWidgets.remove(r.w);
        }
        widgets.get(tabNum).clear();
        noScrollWidgets.get(tabNum).clear();

        if(tabNum == CACHE_TAB_MAIN) {   //createBlock components
            {
                widgets.get(tabNum).add(new RowWidgetComponent("id"));
            }
            if(!selItem.isEmpty()) {
                {
                    widgets.get(tabNum).add(new RowWidgetComponent("count"));
                }
                {
                    widgets.get(tabNum).add(new RowWidget("components"));
                }
                List<String> unset = new ArrayList<String>();
                for(String c : FortytwoEdit.COMPONENTS)
                {
                    if(ComponentHelper.hasComponent(selItem.getComponents(),c))
                        widgets.get(tabNum).add(new RowWidgetComponent("components."+c));
                    else
                        unset.add(c);

                }
                List<String> unused = new ArrayList<String>();
                if(!unset.isEmpty()) {
                    widgets.get(tabNum).add(new RowWidget("unset"));
                    for(String c : unset) {
                        if(ComponentHelper.componentRead(selItem,c))
                            widgets.get(tabNum).add(new RowWidgetComponent("components."+c));
                        else
                            unused.add(c);
                    }
                }
                if(!unused.isEmpty()) {
                    widgets.get(tabNum).add(new RowWidget());
                    if(!showUnusedComponents) {
                        {
                            ButtonWidget w = ButtonWidget.builder(Text.of("Show Unused"), btn -> {
                                showUnusedComponents = !showUnusedComponents;
                                unsel = true;
                                createTab(CACHE_TAB_MAIN);
                            }).dimensions((width/2)-40,5,80,20).build();
                            w.setTooltip(Tooltip.of(Text.of("Show all vanilla components")));
                            widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,(width/2)-40-x,0)}));
                        }
                    }
                    else {
                        {
                            ButtonWidget w = ButtonWidget.builder(Text.of("Hide Unused"), btn -> {
                                showUnusedComponents = !showUnusedComponents;
                                unsel = true;
                                createTab(CACHE_TAB_MAIN);
                            }).dimensions((width/2)-40,5,80,20).build();
                            w.setTooltip(Tooltip.of(Text.of("Only show relevant components")));
                            widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,(width/2)-40-x,0)}));
                        }
                        for(String c : unused)
                            widgets.get(tabNum).add(new RowWidgetComponent("components."+c));
                    }
                }
            }
            {
                widgets.get(tabNum).add(new RowWidget());
            }
        }
        else if(tabNum == CACHE_TAB_INV) {   //createBlock inventory
            {
                widgets.get(tabNum).add(new RowWidget("Inventory"));
            }
            {
                for(int i=0; i<5; i++)
                    widgets.get(tabNum).add(new RowWidgetInvRow(i));
            }
            for(int i=0; i<2; i++) {
                ItemStack current = i==0 ? client.player.getMainHandStack() : client.player.getOffHandStack();
                if(current != null && !current.isEmpty()) {
                    String id = current.getItem().toString();
                    int[] size = BlackMagick.containerSize(id);

                    if(id.equals("bundle")) {
                        {
                            widgets.get(tabNum).add(new RowWidget(i==0 ? "Selected Bundle" : "Offhand Bundle"));
                        }
                        NbtList itemsList = new NbtList();
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents[0]",NbtElement.COMPOUND_TYPE) != null) {
                            itemsList = (NbtList)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:bundle_contents");
                        }

                        ItemStack[] stacks = new ItemStack[itemsList.size()+1];
                        for(int index=0; index<itemsList.size(); index++) {
                            stacks[index+1] = BlackMagick.itemFromNbt((NbtCompound)itemsList.get(index));
                        }

                        int index = 0;
                        for(int r=0; r<=(stacks.length-1)/9; r++) {
                            ItemStack[] stackRow = new ItemStack[stacks.length-index > 9 ? 9 : stacks.length-index];
                            for(int c=0; c<stackRow.length; c++)
                                stackRow[c] = stacks[index+c];
                            index+=9;
                            widgets.get(tabNum).add(new RowWidgetInvRow(stackRow));
                        }
                    }
                    else if(id.equals("armor_stand")) {
                        {
                            widgets.get(tabNum).add(new RowWidget(i==0 ? "Selected Armor Stand" : "Offhand Armor Stand"));
                        }
                        ItemStack[] stacks = new ItemStack[6];

                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.ArmorItems[3]",NbtElement.COMPOUND_TYPE) != null) {
                            NbtList itemsList = ((NbtList)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.ArmorItems"));
                            for(int a=0; a<4; a++) {
                                stacks[a] = BlackMagick.itemFromNbt((NbtCompound)itemsList.get(a));
                            }
                        }
                        
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.HandItems[1]",NbtElement.COMPOUND_TYPE) != null) {
                            NbtList itemsList = ((NbtList)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:entity_data.HandItems"));
                            for(int h=0; h<2; h++) {
                                stacks[4+h] = BlackMagick.itemFromNbt((NbtCompound)itemsList.get(h));
                            }
                        }

                        widgets.get(tabNum).add(new RowWidgetInvRow(stacks,new int[]{1,2,3,4,0,5}));
                    }
                    else if(size[0]>0 && size[1]>0) {
                        {
                            widgets.get(tabNum).add(new RowWidget(i==0 ? "Selected Container" : "Offhand Container"));
                        }
                        NbtList itemsList = null;
                        if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container[0]",NbtElement.COMPOUND_TYPE) != null)
                            itemsList = (NbtList)BlackMagick.getNbtPath(BlackMagick.itemToNbt(current),"components.minecraft:container");
                        for(int r=0; r<size[0]; r++) {
                            ItemStack[] stacks = new ItemStack[size[1]];
                            if(itemsList != null)
                                for(int c=0; c<size[1]; c++) {
                                    for(int index=0; index<itemsList.size(); index++) {
                                        if(((NbtCompound)itemsList.get(index)).contains("slot",NbtElement.INT_TYPE)
                                        && ((NbtCompound)itemsList.get(index)).getInt("slot")==(r*size[1]+c)) {
                                            stacks[c] = BlackMagick.itemFromNbt(
                                                (NbtCompound)BlackMagick.getNbtPath((NbtCompound)itemsList.get(index),"item",NbtElement.COMPOUND_TYPE));
                                        }
                                    }
                                }
                            widgets.get(tabNum).add(new RowWidgetInvRow(stacks));
                        }
                    }
                }
            }
            {
                widgets.get(tabNum).add(new RowWidget("Saved Hotbars"));
            }
            for(int h=0; h<HotbarStorage.STORAGE_ENTRY_COUNT; h++) {
                List<ItemStack> row = client.getCreativeHotbarStorage().getSavedHotbar(h).deserialize(client.world.getRegistryManager());
                ItemStack[] stacks = new ItemStack[9];
                for(int c=0; c<stacks.length; c++) {
                    if(row.size() > c)
                        stacks[c] = row.get(c);
                    else
                        stacks[c] = ItemStack.EMPTY;
                }
                widgets.get(tabNum).add(new RowWidgetInvRow(stacks));
            }
            {
                widgets.get(tabNum).add(new RowWidget());
            }
        }

        if(tab == tabNum)
            btnTab(tab);
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
    public void createBlankTab(int mode, NbtCompound args) {
        int tabNum = CACHE_TAB_BLANK;
        jsonPreview = null;
        showBannerPreview = false;
        tabScroll[tabNum] = 0d;
        setErrorMsg(null);

        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        for(RowWidget r : widgets.get(tabNum)) {
            for(TextFieldWidget t : r.txts) {
                this.unsavedTxtWidgets.remove(t);
                this.allTxtWidgets.remove(t);
            }
        }
        for(PosWidget r : noScrollWidgets.get(tabNum)) {
            this.unsavedTxtWidgets.remove(r.w);
            this.allTxtWidgets.remove(r.w);
        }
        widgets.get(tabNum).clear();
        noScrollWidgets.get(tabNum).clear();

        boolean valid = false;

        if(mode==0) { // display current component
            if(args.contains("path",NbtElement.STRING_TYPE)) {
                String path = args.get("path").asString();
                String[] path2;
                if(args.contains("path2",NbtElement.LIST_TYPE) && ((NbtList)args.get("path2")).size()>0
                && ((NbtList)args.get("path2")).get(0).getType()==NbtElement.STRING_TYPE) {
                    NbtList pathList = (NbtList)args.get("path2");
                    path2 = new String[pathList.size()];
                    for(int i=0; i<path2.length; i++)
                        path2[i] = pathList.get(i).asString();
                }
                else
                    path2 = null;

                String fullPath;
                if(path2 != null)
                    fullPath = path+path2[0];
                else
                    fullPath = path;

                valid = true;

                final ButtonWidget saveBtn;
                {
                    saveBtn = ButtonWidget.builder(Text.of(path2 != null ? "Done" : "Save"), btn -> {
                        if(path2 == null) {
                            if(blankTabEl != null && client.player.getAbilities().creativeMode) {
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
                                NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                                if(blankTabEl != null)
                                    newArgs.put("overrideEl",blankTabEl);
                                createBlankTab(0,newArgs);
                            }
                            else {
                                NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                                if(blankTabEl != null)
                                    newArgs.put("overrideEl",blankTabEl);
                                NbtList pathList = new NbtList();
                                for(int i=1; i<path2.length; i++)
                                    pathList.add(NbtString.of(path2[i]));
                                newArgs.put("path2",pathList);
                                createBlankTab(0,newArgs);
                            }
                        }
                        unsel = true;
                    }).dimensions(x+240-5-40,y+5,40,20).build();
                    if(path2 == null && !client.player.getAbilities().creativeMode) {
                        saveBtn.active = false;
                        saveBtn.setTooltip(Tooltip.of(ERROR_CREATIVE));
                    }
                    noScrollWidgets.get(tabNum).add(new PosWidget(saveBtn,240-5-40,5));
                }

                NbtElement el = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                if(args.contains("overrideEl"))
                    el = args.get("overrideEl");
                if(el != null)
                    setEditingElement(path,el.copy(),saveBtn);
                else
                    setEditingElement(path,null,saveBtn);
                    
                NbtElement el2 = null;
                PathType elType = ComponentHelper.getPathInfo(fullPath).type();
                if(path2 == null && el != null)
                    el2 = el.copy();
                else if(path2 != null) {
                    el2 = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el),fullPath);
                    setEditingElement(path,blankTabEl,saveBtn,fullPath);
                }

                if(path2 == null) {
                    NbtElement selItemComp = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                    ButtonWidget w = ButtonWidget.builder(Text.of("Cancel"),btn -> this.btnTab(CACHE_TAB_MAIN)).dimensions(x+5,y+5,40,20).build();
                    if(selItemComp == null)
                        w.setTooltip(Tooltip.of(Text.of("Keep component unset")));
                    else
                        w.setTooltip(Tooltip.of(Text.of("Keep component as:\n"+BlackMagick.nbtToString(selItemComp))));
                    noScrollWidgets.get(tabNum).add(new PosWidget(w,5,5));
                }
                // else {
                //     NbtElement pageCreateEl = el2!=null ? el2.copy() : null;

                //     ButtonWidget w = ButtonWidget.builder(Text.of("Cancel"),btn -> {
                //         setEditingElement(path,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                //             BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),fullPath,pageCreateEl),path),saveBtn,
                //             path2==null ? null : fullPath);
                //         if(path2.length==1) {
                //             NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                //             if(blankTabEl != null)
                //                 newArgs.put("overrideEl",blankTabEl);
                //             createBlankTab(0,newArgs);
                //         }
                //         else {
                //             NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                //             if(blankTabEl != null)
                //                 newArgs.put("overrideEl",blankTabEl);
                //             NbtList pathList = new NbtList();
                //             for(int i=1; i<path2.length; i++)
                //                 pathList.add(NbtString.of(path2[i]));
                //             newArgs.put("path2",pathList);
                //             createBlankTab(0,newArgs);
                //         }
                //     }).dimensions(x+5,y+5,40,20).build();
                //     if(pageCreateEl == null)
                //         w.setTooltip(Tooltip.of(Text.of("Keep element unset")));
                //     else
                //         w.setTooltip(Tooltip.of(Text.of("Keep element as:\n"+BlackMagick.nbtToString(pageCreateEl))));
                //     noScrollWidgets.get(tabNum).add(new PosWidget(w,5,5));
                // }

                {
                    TextFieldWidget w = new TextFieldWidget(this.textRenderer,x+5+40+5,y+5,(240-5-40)-(5+40+5)-5,20,Text.of(""));
                    w.setEditable(false);
                    w.setMaxLength(131072);
                    w.setText(cleanPath(fullPath));
                    w.setTooltip(Tooltip.of(Text.of("Current path:\n"+fullPath)));
                    noScrollWidgets.get(tabNum).add(new PosWidget(w,5+40+5,5));
                }

                if(elType == PathType.COMPOUND) {
                    if(el2==null || el2.getType()!=NbtElement.COMPOUND_TYPE)
                        el2 = new NbtCompound();

                    Set<String> keys = Sets.newHashSet();
                    for(String k : ComponentHelper.getPathInfo(fullPath).keys()) {
                        keys.add(k);
                    }
                    for(String k : ((NbtCompound)el2).getKeys()) {
                        keys.add(k);
                    }
                    Map<String,Set<String>> keyGroups = Maps.newHashMap();
                    String opt = "Optional";
                    String req = "Required";
                    String unk = "Unknown";

                    for(String k : BlackMagick.sortSet(keys)) {
                        PathInfo pi = ComponentHelper.getPathInfo(fullPath+"."+k);
                        String thisGroup;
                        if(pi.keyGroup()!=null)
                            thisGroup = pi.keyGroup();
                        else if(pi.type()==PathType.UNKNOWN)
                            thisGroup = unk;
                        else
                            thisGroup = opt;

                        if(!keyGroups.containsKey(thisGroup))
                            keyGroups.put(thisGroup,Sets.newHashSet());

                        keyGroups.get(thisGroup).add(k);
                    }

                    List<String> keyGroupLbls = BlackMagick.sortSet(keyGroups.keySet());
                    if(keyGroupLbls.contains(opt)) {
                        keyGroupLbls.remove(opt);
                        keyGroupLbls.add(0,opt);
                    }
                    if(keyGroupLbls.contains(req)) {
                        keyGroupLbls.remove(req);
                        keyGroupLbls.add(0,req);
                    }
                    if(keyGroupLbls.contains(unk)) {
                        keyGroupLbls.remove(unk);
                        keyGroupLbls.add(0,unk);
                    }

                    for(int i=0; i<keyGroupLbls.size(); i++) {
                        widgets.get(tabNum).add(new RowWidget(keyGroupLbls.get(i)));
                        for(String k : BlackMagick.sortSet(keyGroups.get(keyGroupLbls.get(i)))) {
                            widgets.get(tabNum).add(new RowWidgetElement(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn,k));
                        }
                    }

                    {
                        widgets.get(tabNum).add(new RowWidget());
                    }
                }
                else if(elType == PathType.LIST) {
                    NbtList currentList = null;
                    if(el2 != null && el2.getType()==NbtElement.LIST_TYPE)
                        currentList = ((NbtList)el2).copy();
                    
                    {
                        ButtonWidget keyBtn = ButtonWidget.builder(Text.of("List ("+(currentList==null ? "null" : ("Size "+currentList.size()))+")"), btn -> {})
                            .dimensions((width/2)-40,5,80,20).build();
                        keyBtn.active = false;
                        String key = fullPath;
                        if(key.contains(".") && key.length()>key.lastIndexOf(".")+1)
                            key = key.substring(key.lastIndexOf(".")+1);
                        if(key.contains("]") && key.length()>key.lastIndexOf("]")+1)
                            key = key.substring(key.lastIndexOf("]")+1);
                        if(key.startsWith("minecraft:"))
                            key = key.replaceFirst("minecraft:","");
                        keyBtn.setTooltip(Tooltip.of(getButtonTooltip(ComponentHelper.getPathInfo(fullPath),key)));
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(keyBtn,(width/2)-40-x,0)}));
                    }

                    if(currentList==null)
                        currentList = new NbtList();

                    for(int i=0; i<=currentList.size(); i++) {
                        widgets.get(tabNum).add(new RowWidgetElement(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn,i,currentList.size()-1));
                    }

                    {
                        widgets.get(tabNum).add(new RowWidget());
                    }
                }
                else if(elType == PathType.TEXT) {
                    jsonEffectMode = -1;
                    jsonEffectPath = null;
                    jsonEffectBase = null;
                    cacheI.remove("jsonAdd");
                    String startVal = "";

                    if(el2 != null && el2.getType()==NbtElement.STRING_TYPE)
                        startVal = ((NbtString)el2).asString(); // keep asString
                    else
                        startVal = "{\"text\":\"\"}";

                    if(args.contains("jsonOverride",NbtElement.STRING_TYPE))
                        startVal = args.get("jsonOverride").asString(); // keep asString

                    {
                        cacheI.put("jsonBox",new int[]{tabNum,noScrollWidgets.get(tabNum).size()});
                        EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
                        w.setChangeListener(value -> {
                            setErrorMsg(null);
                            updateJsonPreview(fullPath,value);
                            if(jsonBaseValid) {
                                setEditingElement(path,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                                    BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,blankTabEl),fullPath,
                                    NbtString.of(value)),path),saveBtn,
                                    path2==null ? null : fullPath);
                            }
                            else {
                                setErrorMsg("Invalid JSON");
                                setEditingElement(path,blankTabEl,saveBtn,path2==null ? null : fullPath);
                            }
                        });
                        noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3,35));
                        this.allTxtWidgets.add(w);
                        w.setText(startVal);
                    }
                    {
                        noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Add Text"), button -> {
                            jsonEffectMode = 1;
                            String baseJson;
                            if(jsonBaseValid)
                                baseJson = jsonBaseText;
                            else
                                baseJson = "{\"text\":\"\"}";
                                
                            NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                            newArgs.putString("baseJson",baseJson);
                            if(args.contains("path2",NbtElement.LIST_TYPE))
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(2,newArgs);

                            unsel = true;
                        }).dimensions(x+15-3,y+35+22*6+1,60,20).build(),15-3,35+22*6+1));
                    }
                    {
                        noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Add Effect"), button -> {
                            jsonEffectMode = 0;
                            String baseJson;
                            if(jsonBaseValid)
                                baseJson = jsonBaseText;
                            else
                                baseJson = "{\"text\":\"\"}";
                                
                            NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                            newArgs.putString("baseJson",baseJson);
                            if(args.contains("path2",NbtElement.LIST_TYPE))
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(2,newArgs);

                            unsel = true;
                        }).dimensions(x+15-3+60+5,y+35+22*6+1,60,20).build(),15-3+60+5,35+22*6+1));
                    }
                    if(path.contains("written_book_content")) {
                        noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Set Event"), button -> {
                            jsonEffectMode = 2;
                            String baseJson;
                            if(jsonBaseValid)
                                baseJson = jsonBaseText;
                            else
                                baseJson = "{\"text\":\"\"}";
                                
                            NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                            newArgs.putString("baseJson",baseJson);
                            if(args.contains("path2",NbtElement.LIST_TYPE))
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(2,newArgs);

                            unsel = true;
                        }).dimensions(x+15-3+60+5+60+5,y+35+22*6+1,60,20).build(),15-3+60+5+60+5,35+22*6+1));
                    }
                }
                else if(elType == PathType.DECIMAL_COLOR) {//TODO dec page
                    widgets.get(tabNum).add(new RowWidgetElement(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn));
                }
                else if(elType == PathType.BANNER) {
                    if(el2==null || el2.getType()!=NbtElement.COMPOUND_TYPE)
                        el2 = new NbtCompound();

                    NbtCompound bannerNbt = (NbtCompound)el2;
                    String bannerCol = null;
                    String bannerPat = null;
                    ItemStack bannerItem = ItemStack.EMPTY;

                    if(fullPath.contains("components.")) {
                        String itemPath = fullPath.substring(0,fullPath.lastIndexOf("components."));
                        if(itemPath.length()>0)
                            bannerItem = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),path,blankTabEl),itemPath)));
                        else
                            bannerItem = BlackMagick.itemFromNbt(BlackMagick.validCompound(BlackMagick.setNbtPath(
                                BlackMagick.itemToNbt(selItem),path,blankTabEl)));
                    }
                    bannerShield = false;
                    String bannerItemId = bannerItem.getItem().toString();
                    if(bannerItemId.equals("shield")) {
                        bannerShield = true;
                        showBannerPreview = true;
                        bannerChangePreview.readNbt(BlackMagick.validCompound(BlackMagick.nbtFromString("{ArmorItems:[{},{},{},{}],HandItems:["
                            +BlackMagick.itemToNbtStorage(bannerItem).asString()+",{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}")));
                    }
                    else if(bannerItemId.contains("_banner")) {
                        showBannerPreview = true;
                        bannerChangePreview.readNbt(BlackMagick.validCompound(BlackMagick.nbtFromString("{ArmorItems:[{},{},{},"
                            +BlackMagick.itemToNbtStorage(bannerItem).asString()+"],HandItems:[{},{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}")));
                    }

                    if(bannerNbt.contains("color",NbtElement.STRING_TYPE))
                        bannerCol = bannerNbt.getString("color");
                    if(bannerNbt.contains("pattern",NbtElement.STRING_TYPE))
                        bannerPat = bannerNbt.getString("pattern");

                    List<String> bannerVals = new ArrayList<>();
                    for(int i=0; i<ComponentHelper.DYES.length; i++)
                        bannerVals.add(ComponentHelper.DYES[i]);
                    for(int i=0; i<ComponentHelper.BANNER_PATTERNS.length; i++)
                        bannerVals.add(ComponentHelper.BANNER_PATTERNS[i]);

                    int row=0;
                    while(bannerVals.size()>0) {
                        String[] currentVals = new String[Math.min(8,bannerVals.size())];
                        for(int i=0; i<currentVals.length; i++)
                            currentVals[i] = bannerVals.remove(0);
                        widgets.get(tabNum).add(
                            new RowWidget(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn,row<2,currentVals,row<2 ? bannerCol : bannerPat));
                        row++;
                    }

                }
                else if(elType == PathType.POSE) {//TODO pose page
                    widgets.get(tabNum).add(new RowWidgetElement(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn));
                }
                else {
                    widgets.get(tabNum).add(new RowWidgetElement(path,path2==null ? null : (NbtList)args.get("path2"),saveBtn));
                }
            }
        }
        else if(mode==2) { // json effects
            if(args.contains("path",NbtElement.STRING_TYPE) && args.contains("baseJson",NbtElement.STRING_TYPE) && jsonEffectMode>=0 && jsonEffectMode<=2) {
                String path = args.get("path").asString();
                String baseJson = args.get("baseJson").asString(); // keep asString
                jsonEffectPath = path;
                jsonEffectBase = baseJson;
                valid = true;
                {
                    ButtonWidget w = ButtonWidget.builder(Text.of("Cancel"),btn -> {
                        jsonEffectMode = -1;
                        jsonEffectPath = null;
                        jsonEffectBase = null;
                        cacheI.remove("jsonAdd");
                        NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}"));
                        if(args.contains("path2",NbtElement.LIST_TYPE))
                            newArgs.put("path2",args.get("path2"));
                        newArgs.put("jsonOverride",args.get("baseJson"));
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                    }).dimensions(x+5,y+5,40,20).build();
                    w.setTooltip(Tooltip.of(Text.of("Keep text as:\n"+baseJson)));
                    noScrollWidgets.get(tabNum).add(new PosWidget(w,5,5));
                }
                final ButtonWidget saveBtn;
                {
                    cacheI.put("jsonAdd",new int[]{tabNum,noScrollWidgets.get(tabNum).size()});
                    saveBtn = ButtonWidget.builder(Text.of("Add"), btn -> {
                        if(jsonEffectValid) {
                            jsonEffectMode = -1;
                            jsonEffectPath = null;
                            jsonEffectBase = null;
                            cacheI.remove("jsonAdd");
                            NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString(
                                "{path:\""+path+"\"}"));
                            newArgs.put("jsonOverride",NbtString.of(jsonEffectFull));
                            if(args.contains("path2",NbtElement.LIST_TYPE))
                                newArgs.put("path2",args.get("path2"));
                            if(blankTabEl != null)
                                newArgs.put("overrideEl",blankTabEl);
                            createBlankTab(0,newArgs);
                        }
                        unsel = true;
                    }).dimensions(x+240-5-40,y+5,40,20).build();
                    noScrollWidgets.get(tabNum).add(new PosWidget(saveBtn,240-5-40,5));
                }

                {
                    String[] path2;
                    if(args.contains("path2",NbtElement.LIST_TYPE) && ((NbtList)args.get("path2")).size()>0
                    && ((NbtList)args.get("path2")).get(0).getType()==NbtElement.STRING_TYPE) {
                        NbtList pathList = (NbtList)args.get("path2");
                        path2 = new String[pathList.size()];
                        for(int i=0; i<path2.length; i++)
                            path2[i] = pathList.get(i).asString();
                    }
                    else
                        path2 = null;
                        
                    String fullPath;
                    if(path2 != null)
                        fullPath = path+path2[0];
                    else
                        fullPath = path;

                    TextFieldWidget w = new TextFieldWidget(this.textRenderer,x+5+40+5,y+5,(240-5-40)-(5+40+5)-5,20,Text.of(""));
                    w.setEditable(false);
                    w.setMaxLength(131072);
                    w.setText(cleanPath(fullPath));
                    w.setTooltip(Tooltip.of(Text.of("Current path:\n"+fullPath)));
                    noScrollWidgets.get(tabNum).add(new PosWidget(w,5+40+5,5));
                }

                updateJsonPreview(path,baseJson);

                if(jsonEffectMode == 0) {
                    widgets.get(tabNum).add(new RowWidget("Gradient"));
                }
                else if(jsonEffectMode == 1) {
                    widgets.get(tabNum).add(new RowWidget("Text Element"));
                }
                if(jsonEffectMode == 0 || jsonEffectMode == 1) {
                    cacheI.put("jsonEffectTxt",new int[]{tabNum,widgets.get(tabNum).size()});
                    TextFieldWidget w = new TextFieldWidget(this.textRenderer,x+15-3,y+35,240-36,20,Text.of(""));
                    w.setMaxLength(131072);
                    w.setChangedListener(value -> {
                        updateJsonEffect();
                        if(jsonEffectMode == 1) {
                            if(jsonEffects[7]==1 || jsonEffects[7]==2) {
                                String[] suggestions = jsonEffects[7]==1 ? FortytwoEdit.getCacheKeybinds() : FortytwoEdit.getCacheTranslations();
                                suggsOnChanged(w,suggestions,null);
                            }
                            else
                                resetSuggs();
                        }
                    });
                    widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                    this.allTxtWidgets.add(w);
                }
                if(jsonEffectMode == 0 || jsonEffectMode == 1) {
                    cacheI.put("jsonEffectBtns",new int[]{tabNum,widgets.get(tabNum).size()});
                    widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("\u00a7ll"),Text.of("\u00a7oo"),Text.of("\u00a7nn"),Text.of("\u00a7mm"),
                    Text.of("\u00a7kk")},new int[]{20,20,20,20,20},new String[]{"none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r",
                    "none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r"},
                    null,true,btn -> {
                        unsel = true;
                        jsonEffects[0]++;
                        if(jsonEffects[0]>2)
                            jsonEffects[0]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    },btn -> {
                        unsel = true;
                        jsonEffects[1]++;
                        if(jsonEffects[1]>2)
                            jsonEffects[1]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    },btn -> {
                        unsel = true;
                        jsonEffects[2]++;
                        if(jsonEffects[2]>2)
                            jsonEffects[2]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    },btn -> {
                        unsel = true;
                        jsonEffects[3]++;
                        if(jsonEffects[3]>2)
                            jsonEffects[3]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    },btn -> {
                        unsel = true;
                        jsonEffects[4]++;
                        if(jsonEffects[4]>2)
                            jsonEffects[4]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    }));
                }
                if(jsonEffectMode == 0) {
                    widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("[Radial]")},
                    new int[]{60},new String[]{"Radial | Linear"},null,true,btn -> {
                        unsel = true;
                        jsonEffects[5]++;
                        if(jsonEffects[5]>1)
                            jsonEffects[5]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    }));
                }
                else if(jsonEffectMode == 1) {
                    ButtonWidget w = ButtonWidget.builder(Text.of("Color [RGB]"), btn -> {
                        unsel = true;
                        jsonEffects[6]++;
                        if(jsonEffects[6]>2)
                            jsonEffects[6]=0;
                        updateJsonEffect();
                        updateJsonEffectBtns();
                    }).dimensions(0,0,80,20).build();
                    w.setTooltip(Tooltip.of(Text.of("RGB | Vanilla | None")));
                    TextFieldWidget w2 = new TextFieldWidget(this.textRenderer,0,0,240-36-80-5,20,Text.of(""));
                    w2.setMaxLength(131072);
                    w2.setChangedListener(value -> {
                        if(jsonEffects[6]==1) {
                            jsonLastColor = value;
                            suggsOnChanged(w2,ComponentHelper.FORMAT_COLORS,null);
                            updateJsonEffect();
                        }
                        else
                            resetSuggs();
                    });
                    widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0), new PosWidget(w2,15+80+5,0)}));
                }
                if(jsonEffectMode == 0) {
                    {
                        RgbSlider w = new RgbSlider(3);
                        RgbSlider w2 = new RgbSlider(6);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                    }
                    {
                        RgbSlider w = new RgbSlider(4);
                        RgbSlider w2 = new RgbSlider(7);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                    }
                    {
                        RgbSlider w = new RgbSlider(5);
                        RgbSlider w2 = new RgbSlider(8);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                    }
                    {
                        TextFieldWidget w = new TextFieldWidget(textRenderer, x+15, 0, 60, 20, Text.of(""));
                        TextFieldWidget w2 = new TextFieldWidget(textRenderer, x+15+100+5, 0, 60, 20, Text.of(""));
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                    }
                }
                else if(jsonEffectMode == 1) {
                    {
                        RgbSlider w = new RgbSlider(0);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                    }
                    {
                        RgbSlider w = new RgbSlider(1);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                    }
                    {
                        RgbSlider w = new RgbSlider(2);
                        widgets.get(tabNum).add(new RowWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget(new Text[]{Text.of("[Text]")},
                        new int[]{80},new String[]{"Text | Keybind | Translate"},null,true,btn -> {
                            unsel = true;
                            jsonEffects[7]++;
                            if(jsonEffects[7]>2)
                                jsonEffects[7]=0;
                            updateJsonEffect();
                            updateJsonEffectBtns();
                        }));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("Translations Only"));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("Params:",3));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("Fallback:",4));
                    }
                }
                else if(jsonEffectMode == 2) {
                    {
                        cacheI.put("clickEventLbl",new int[]{tabNum,widgets.get(tabNum).size()});
                        widgets.get(tabNum).add(new RowWidget("clickEvent"));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("action:",5));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("value:",6));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("hoverEvent"));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("action:",7));
                    }
                    {
                        widgets.get(tabNum).add(new RowWidget("contents:",8));
                    }
                }
                {
                    widgets.get(tabNum).add(new RowWidget());
                }
                updateJsonEffectBtns();
                updateRgbSliders();
            }
        }

        if(!valid)
            btnTab(CACHE_TAB_MAIN);
        else
            btnTab(tabNum);
        resetSuggs();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class TabWidget
    extends ElementListWidget<AbstractWidget> { //modified from net.minecraft.client.gui.screen.world.EditGameRulesScreen$RuleListWidget
        public TabWidget(final int tab) {
            super(ItemBuilder.this.client, ItemBuilder.this.width-30, ItemBuilder.this.backgroundHeight-32-5, ItemBuilder.this.y+32,
                (tab == CACHE_TAB_INV || tab == CACHE_TAB_SAVED) ? 20 : 22);
            
            for(int i=0; i<widgets.get(tab).size(); i++)
                this.addEntry((AbstractWidget)ItemBuilder.this.widgets.get(tab).get(i));
        }

        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            AbstractWidget abstractRuleWidget = (AbstractWidget)this.getHoveredEntry();
            if (abstractRuleWidget != null && abstractRuleWidget.description != null) {
                ItemBuilder.this.setTooltip(abstractRuleWidget.description);
            }
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {

        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {

        }
    }

    protected class RowWidget extends AbstractWidget {

        protected final List<ClickableWidget> children;
        protected ButtonWidget[] btns;
        protected int[] btnX;
        protected int[] btnY = null;
        protected TextFieldWidget[] txts;
        protected int[] txtX;
        protected String lbl;
        protected boolean lblCentered = false;
        protected int lblColor = LABEL_COLOR;
        protected ItemStack[] savedStacks;
        protected boolean[] savedStacksWarn;
        protected int savedStacksMode = -1;
        protected int savedRow = -1;
        protected PoseSlider poseSlider;
        protected RgbSlider rgbSlider;
        protected int rgbNum;
        protected ItemStack item = null;
        protected int itemXoff = 0;
        protected PosWidget[] wids;

        /**
         * Blank row
         */
        public RowWidget() {
            super();
            this.children = Lists.newArrayList();
            setup();
        }//TODO delete unused rowwidgets

        /**
         * btn(size) txt
         */
        public RowWidget(String name, String tooltip, PressAction onPress, String[] suggestions, boolean survival) {
            super();
            this.children = Lists.newArrayList();
            setup();

            int size = sizeFromName(name);

            this.btns = new ButtonWidget[]{ButtonWidget.builder(Text.of(name), onPress).dimensions(ItemBuilder.this.x+15,5,size,20).build()};
            this.btnX = new int[]{15};
            if(tooltip != null)
                this.btns[0].setTooltip(Tooltip.of(Text.of(tooltip)));
            if(!client.player.getAbilities().creativeMode && !survival)
                this.btns[0].active = false;
            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+size};
            this.txts[0].setChangedListener(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    //ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    //ItemBuilder.this.markSaved(this.txts[0]);
                }
                suggsOnChanged(this.txts[0],suggestions,null);
            });
            this.txts[0].setMaxLength(131072);
            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
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
         * Used for JSON effects tab.
         * lbl(size) txt [custom suggs]
         */
        public RowWidget(String name, int suggsNum) {
            super();
            this.children = Lists.newArrayList();
            setup();

            int size = sizeFromName(name);

            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+size};
            this.txts[0].setChangedListener(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    //ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    //ItemBuilder.this.markSaved(this.txts[0]);
                }
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                    switch(suggsNum) {
                        case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{\"text\":\"\"}]"}); break;
                        case 4: resetSuggs(); break;
                        case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                        case 6: resetSuggs(); break;
                        case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                        case 8: suggs.setSuggestions(new String[]
                                {"{\"text\":\"\"}","{\"id\":\"stone\"}","{\"id\":\"bundle\",\"components\":\"{bundle_content:[{id:\\\"stone\\\"}]}\"}"}); break;
                        default: resetSuggs(); break;
                    }
                }
                else{
                    if(suggs != null)
                        suggs.refresh();
                    else {
                        resetSuggs();
                        suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                        switch(suggsNum) {
                            case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{\"text\":\"\"}]"}); break;
                            case 4: resetSuggs(); break;
                            case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                            case 6: resetSuggs(); break;
                            case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                            case 8: suggs.setSuggestions(new String[]
                                {"{\"text\":\"\"}","{\"id\":\"stone\"}","{\"id\":\"bundle\",\"components\":\"{bundle_content:[{id:\\\"stone\\\"}]}\"}"}); break;
                            default: resetSuggs(); break;
                        }
                    }
                }
                if(suggsNum >= 3 && suggsNum <= 8) {
                    updateJsonEffect();
                }
            });
            this.txts[0].setMaxLength(131072);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
            }
            lbl = name;
        }

        /**
         * btn...(sizes) txt...(sizes)
         */
        public RowWidget(Text[] names, int[] sizes, String[] tooltips, String[][] suggestions, boolean survival, PressAction... onPressActions) {
            super();
            this.children = Lists.newArrayList();
            setup();

            if(names.length <= sizes.length && names.length == tooltips.length && names.length == onPressActions.length) {
                this.btns = new ButtonWidget[names.length];
                this.btnX = new int[names.length];
                this.txts = new TextFieldWidget[sizes.length-this.btns.length];
                this.txtX = new int[this.txts.length];

                int currentX = 15;
                for(int i=0; i<this.btns.length; i++) {
                    this.btnX[i] = currentX;
                    this.btns[i] = ButtonWidget.builder(names[i], onPressActions[i]).dimensions(currentX,5,sizes[i],20).build();
                    currentX += 5 + sizes[i];
                    if(tooltips[i] != null)
                        this.btns[i].setTooltip(Tooltip.of(Text.of(tooltips[i])));

                    if(!client.player.getAbilities().creativeMode && !survival)
                        this.btns[i].active = false;
                    this.children.add(this.btns[i]);
                }
                for(int i=0; i<this.txts.length; i++) {
                    this.txtX[i] = currentX;
                    this.txts[i] = new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, currentX, 5, sizes[this.btns.length+i], 20, Text.of(""));
                    currentX += sizes[this.btns.length+i];

                    final int ii = i;
                    this.txts[i].setChangedListener(value -> {
                        if(value != null && !value.equals("")) {
                            this.txts[ii].setEditableColor(0xFFFFFF);
                            //ItemBuilder.this.markUnsaved(this.txts[ii]);
                        }
                        else {
                            this.txts[ii].setEditableColor(LABEL_COLOR);
                            //ItemBuilder.this.markSaved(this.txts[ii]);
                        }

                        String[] suggestionsList = null;
                        if(suggestions != null && suggestions.length > ii && suggestions[ii] != null)
                            suggestionsList = suggestions[ii];
                        suggsOnChanged(this.txts[ii],suggestionsList,null);
                    });
                    this.txts[i].setMaxLength(131072);

                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }
        }

        /**
         * Saved row (9 btns)
         */
        public RowWidget(int row) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.savedRow = row;
            this.savedStacks = new ItemStack[9];
            this.savedStacksWarn = new boolean[9];
            this.savedStacksMode = 1;
            this.btns = new ButtonWidget[9];
            this.btnX = new int[9];
            int currentX = 10+30;
            for(int i=0; i<9; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    ItemBuilder.this.unsel = true;
                    if(!viewBlackMarket) {
                        if(savedModeSet) {
                            ItemBuilder.this.savedError = false;
                            NbtCompound nbt = new NbtCompound();
                            if(!client.player.getMainHandStack().isEmpty()) {
                                nbt = BlackMagick.itemToNbtStorage(client.player.getMainHandStack());
                            }
                            savedItems.set(index,nbt);
                            FortytwoEdit.setSavedItems(savedItems);
                            refreshSaved();
                        }
                        else {
                            if(client.player.getAbilities().creativeMode) {
                                ItemStack item = BlackMagick.itemFromNbt((NbtCompound)(savedItems.get(index)));
                                if(item!=null && !item.isEmpty())
                                    BlackMagick.setItemMain(item);
                            }
                        }
                    }
                    else {
                        if(client.player.getAbilities().creativeMode) {
                            ItemStack item = BlackMagick.itemFromNbt((NbtCompound)(webItems.get(index)));
                            if(item!=null && !item.isEmpty())
                                BlackMagick.setItemMain(item);
                        }
                    }
                }).dimensions(currentX,5,20,20).build();
                currentX += 20;
                this.btns[i].active = false;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);

                this.children.add(this.btns[i]);
            }
        }

        /**
         * banner row (8 btns)
         */
        public RowWidget(String blankElPath, NbtList path2, ButtonWidget saveBtn, boolean isDye, String[] vals, String currentVal) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.savedStacks = new ItemStack[vals.length];
            this.savedStacksMode = 0;

            ItemStack[] patternItems = isDye ? null : new ItemStack[vals.length];

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString();
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"."+(isDye ? "color" : "pattern");

            if(!isDye) {
                for(int i=0; i<vals.length; i++)
                    patternItems[i] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick
                        .nbtFromString("{id:white_banner,components:{banner_patterns:[{color:red,pattern:"+vals[i]+"}]}}"));

                if(!bannerShield)
                    for(int i=0; i<vals.length; i++)
                        this.savedStacks[i] = patternItems[i];
                else
                    for(int i=0; i<vals.length; i++)
                        this.savedStacks[i] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick
                            .nbtFromString("{id:shield,components:{base_color:white,banner_patterns:[{color:red,pattern:"+vals[i]+"}]}}"));
            }
            else {
                for(int i=0; i<vals.length; i++)
                    this.savedStacks[i] = BlackMagick.itemFromNbt((NbtCompound)BlackMagick.nbtFromString("{id:"+vals[i]+"_dye}"));
            }
            this.btns = new ButtonWidget[vals.length];
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
                        List<Text> textList = patternItems[i].getTooltip(TooltipContext.DEFAULT,null,TooltipType.BASIC);
                        if(textList.size()>1) {
                            disabled = false;
                            tt = Tooltip.of(Text.of(textList.get(1).getString().replace("Red ","")));
                        }
                    }
                    if(disabled) {
                        tt = Tooltip.of(Text.of("Pattern disabled: "+vals[i]).copy().formatted(Formatting.RED));
                        this.savedStacks[i] = new ItemStack(Items.BARRIER);
                    }
                }
                else
                    tt = Tooltip.of(savedStacks[i].getName());

                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,NbtString.of(vals[col])),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(currentX,5,20,20).build();
                currentX += 20;
                
                if(currentVal != null && (vals[i].equals(currentVal) || currentVal.equals("minecraft:"+vals[i])))
                    this.btns[i].active = false;

                this.btns[i].setTooltip(tt);
                this.children.add(this.btns[i]);
            }
        }

        /**
         * Pose slider
         */
        public RowWidget(int part, int num) {//TODO pose slider row
            super();
            this.children = Lists.newArrayList();
            setup();

            this.poseSlider = new PoseSlider(part,num);

            this.children.add(this.poseSlider);
        }

        /**
         * Pose slider btn
         */
        public RowWidget(int part, String name) {//TODO pose slider btn row
            super();
            this.children = Lists.newArrayList();
            setup();

            this.btns = new ButtonWidget[]{ButtonWidget.builder(Text.of(name), btn -> {
                for(int i=0; i<3; i++)
                    sliders.get(part*3+i).setSlider(0f);
                if(armorPose != null)
                    armorPose[part] = null;
                this.btns[0].setTooltip(Tooltip.of(Text.of("No pose")));
                this.btns[0].active = false;
                updatePose();
                //unsavedPose = true;
            }).dimensions(ItemBuilder.this.x+15,5,60,20).build()};
            this.btnX = new int[]{100};

            this.children.add(this.btns[0]);
        }

        /**
         * btn? rgbSlider
         */
        public RowWidget(int rgbNum, String name, int size, String tooltip, PressAction onPress, boolean survival) {//TODO rgb slider row
            super();
            this.children = Lists.newArrayList();
            setup();

            this.rgbSlider = new RgbSlider(rgbNum);
            this.rgbNum = rgbNum;

            if(size>0) {
                this.btns = new ButtonWidget[]{ButtonWidget.builder(Text.of(name), onPress).dimensions(ItemBuilder.this.x+15,5,size,20).build()};
                this.btnX = new int[]{15};
                if(tooltip != null)
                    this.btns[0].setTooltip(Tooltip.of(Text.of(tooltip)));
                if(!client.player.getAbilities().creativeMode && !survival)
                    this.btns[0].active = false;
                this.children.add(this.btns[0]);
            }
            if(rgbNum == 1 || rgbNum ==2) {
                this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15, 5, 60, 20, Text.of(""))};
                this.txtX = new int[]{15};
                this.txts[0].setMaxLength(16);
                this.children.add(this.txts[0]);
            }

            this.children.add(this.rgbSlider);
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
                this.children.add(p[i].w);
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
            int min = ItemBuilder.this.textRenderer.getWidth(text)+4;
            while(min>size && size<100) {
                size += 20;
            }
            return size;
        }

        protected void setup() {
            btns = new ButtonWidget[0];
            btnX = new int[0];
            txts = new TextFieldWidget[0];
            txtX = new int[0];
            wids = new PosWidget[0];
        }

        public String[] btn() {
            String[] texts = new String[this.txts.length];
            for(int i=0; i<texts.length; i++) {
                this.txts[i].setEditableColor(LABEL_COLOR);
                ItemBuilder.this.markSaved(this.txts[i]);
                texts[i] = this.txts[i].getText();
            }
            ItemBuilder.this.unsel = true;
            return texts;
        }

        public void updateSavedDisplay() {
            if(savedStacksMode == 1)
                for(int i=0; i<9; i++) {
                    NbtCompound current = viewBlackMarket ? (NbtCompound)webItems.get(savedRow*9+i) : (NbtCompound)savedItems.get(savedRow*9+i);
                    this.btns[i].active = savedModeSet && !viewBlackMarket;
                    ItemStack parsedStack = BlackMagick.itemFromNbt(current);
                    if(savedStacksWarn != null && savedStacksWarn.length == 9)
                        savedStacksWarn[i] = false;
                    if(!parsedStack.isEmpty()) {
                        savedStacks[i] = parsedStack;
                        if(!BlackMagick.itemToNbtStorage(parsedStack).asString().equals(current.asString())) {
                            if(savedStacksWarn != null && savedStacksWarn.length == 9)
                                savedStacksWarn[i] = true;
                            this.btns[i].setTooltip(Tooltip.of(Text.empty().append(BlackMagick.jsonFromString("{\"text\":\"Failed to read item\",\"color\":\"red\"}")
                                .text()).append(Text.of("\n")).append(BlackMagick.getElementDifferences(current, BlackMagick.itemToNbtStorage(parsedStack)))));
                        }
                        else
                            this.btns[i].setTooltip(makeItemTooltip(current,parsedStack));
                        if(client.player.getAbilities().creativeMode)
                            this.btns[i].active = true;
                    }
                    else {
                        if(current.asString().equals("{}")) {
                            this.btns[i].setTooltip(null);
                            savedStacks[i] = ItemStack.EMPTY;
                        }
                        else {
                            this.btns[i].setTooltip(makeItemTooltip(current,null));
                            savedStacks[i] = FortytwoEdit.ITEM_ERROR;
                        }
                    }
                }
        }

        public void setSlider(float val) {
            if(this.poseSlider != null)
                this.poseSlider.setVal(val);
        }
        //TODO setslider logic
        public void setSlider(int val) {
            if(this.rgbSlider != null)
                this.rgbSlider.setVal(val);
        }

        @Override
        public List<? extends Element> children() {
            return this.children;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
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
                this.wids[i].w.setX(x+this.wids[i].x);
                this.wids[i].w.setY(y+this.wids[i].y);
                this.wids[i].w.render(context, mouseX, mouseY, tickDelta);
            }
            if(lbl != null) {
                if(lblCentered)
                    context.drawCenteredTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.width/2, y+6, lblColor);
                else
                    context.drawTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.x+15+3, y+6, lblColor);
            }
            if(item != null) {
                drawItem(context,item,x+15+2+itemXoff,y+2);
            }
            if(poseSlider != null) {
                this.poseSlider.render(context, mouseX, mouseY, tickDelta);
                this.poseSlider.setX(ItemBuilder.this.x+2+10+20+5);
                this.poseSlider.setY(y);
            }
            if(rgbSlider != null) {
                this.rgbSlider.render(context, mouseX, mouseY, tickDelta);
                this.rgbSlider.setX(ItemBuilder.this.x+2+10+20+5+40);
                this.rgbSlider.setY(y);
            }
            if(savedStacksMode == 1 && this.savedStacks != null && this.savedStacks.length == 9)
                for(int i=0; i<9; i++) {
                    if(this.savedStacks[i] != null) {
                        drawItem(context,this.savedStacks[i],x+this.btnX[i]+2,y+2);
                        if(savedModeSet && !viewBlackMarket && !this.savedStacks[i].isEmpty())
                            drawItem(context,savedModeItems[2],x+this.btnX[i]+2,y+2);
                        if(savedStacksWarn != null && savedStacksWarn.length==9 && savedStacksWarn[i])
                            drawItem(context,savedModeItems[2],x+this.btnX[i]+2,y+2);
                    }
                }
            else if(savedStacksMode == 0 && this.savedStacks != null && this.savedStacks.length == this.btnX.length)
                for(int i=0; i<this.savedStacks.length; i++)
                    if(this.savedStacks[i] != null && !this.savedStacks[i].isEmpty())
                        drawItem(context,this.savedStacks[i],x+this.btnX[i]+2,y+2);
            if(rgbNum == 1) {
                if(rgbChanged[0]) {
                    this.txts[0].setText(""+getRgbDec());
                    this.txts[0].setEditableColor(getRgbDec());
                    updateRgbItems();
                    rgbChanged[0] = false;
                }
            }
            if(rgbNum == 2) {
                if(rgbChanged[1]) {
                    this.txts[0].setText(""+getRgbHex());
                    this.txts[0].setEditableColor(getRgbDec());
                    updateRgbItems();
                    rgbChanged[1] = false;
                }
                for(int i=0; i<3; i++)
                    drawItem(context,rgbItems[i], x+15+2+i*25, y+2+22);
            }

        }

    }

    class RowWidgetComponent extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.of(Text.of("Set component"));

        /**
         * For use in components screen only.
         * Row contains txt/btn, btns to edit/delete/add, or btns to set trinary/binary values.
         * 
         * @param path nbt path in format: components.minecraft:foo.bar.list[0]
         */
        public RowWidgetComponent(String path) {
            super();

            PathInfo pi = ComponentHelper.getPathInfo(path);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==NbtElement.STRING_TYPE;
            Text btnTt = getButtonTooltip(pi,path);
            
            String keyBtnTxt = cleanPath(path);
            if(keyBtnTxt.equals("enchantment_glint_override"))
                keyBtnTxt = "ench glint override";
            int size = sizeFromName(keyBtnTxt);
            if((size>80) && (pi.type() == PathType.TRINARY || pi.type() == PathType.TOOLTIP_UNIT))
                size = 80;
            ButtonWidget keyBtn = ButtonWidget.builder(Text.of(keyBtnTxt), btn -> {})
                .dimensions(ItemBuilder.this.x+ROW_LEFT,5,size,20).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.of(btnTt));

            if(ComponentHelper.isComplex(pi.type())) {

                NbtElement startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                final String startVal = (isString && startEl != null) ? startEl.asString() : BlackMagick.nbtToString(startEl);
    
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(getButtonText(path,startEl), btn -> {
                    createBlankTab(0,BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}")));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_LEFT+size+5,5,ROW_RIGHT-ROW_LEFT-20-size-5,20).build(),
                ButtonWidget.builder(Text.of(startVal.equals("") ? "+" : "X"), btn -> {
                    if(startVal.equals("")) {
                        createBlankTab(0,BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+path+"\"}")));
                    }
                    else if(path.startsWith("components.")) {
                        String comp = path.replaceFirst("components.","");
                        BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new NbtCompound())));
                    }
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-20,5,20,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_LEFT+size+5,ROW_RIGHT-20};
                if(!startVal.equals("")) {
                    this.btns[1].setTooltip(Tooltip.of(Text.of("Edit component:\n"+startVal)));
                    this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                }
                else
                    this.btns[1].active = false;
    
                this.btns[2].setTooltip(Tooltip.of(Text.of(startVal.equals("") ? "Create component" : "Delete component")));
                if(!client.player.getAbilities().creativeMode && !startVal.equals("")) {
                    this.btns[2].active = false;
                    this.btns[2].setTooltip(Tooltip.of(ERROR_CREATIVE));
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
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("False"), btn -> {
                    String comp = path.replaceFirst("components.","");
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new NbtCompound())));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,new NbtCompound())));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                this.btns[startVal+1].active=false;
    
                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!client.player.getAbilities().creativeMode)
                            this.btns[i].setTooltip(Tooltip.of(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type() == PathType.TRINARY) {
                final int startVal;
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path) != null) {
                    String tempVal = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path).asString();
                    if(tempVal.equals("1b") || tempVal.equals("1"))
                        startVal = 2;
                    else
                        startVal = 1;
                }
                else
                    startVal = 0;
    
                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("Unset"), btn -> {
                    String comp = path.replaceFirst("components.","");
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new NbtCompound())));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("False"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,NbtByte.ZERO)));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("True"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,NbtByte.ONE)));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                this.btns[startVal+1].active=false;
    
                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!client.player.getAbilities().creativeMode)
                            this.btns[i].setTooltip(Tooltip.of(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else if(pi.type() == PathType.TOOLTIP_UNIT) {
                final int startVal;
                if(BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path) != null) {
                    String tempVal = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path).asString();
                    if(tempVal.equals("{show_in_tooltip:0b}") || tempVal.equals("{show_in_tooltip:0}"))
                        startVal = 1;
                    else
                        startVal = 2;
                }
                else
                    startVal = 0;
    
                final int btnSize = 35;
                final int btnSpacing = 2;
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("False"), btn -> {
                    String comp = path.replaceFirst("components.","");
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new NbtCompound())));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("Hide"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,
                        BlackMagick.validCompound(BlackMagick.nbtFromString("{show_in_tooltip:0b}")))));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("Show"), btn -> {
                    BlackMagick.setItemMain(BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,new NbtCompound())));
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                this.btns[startVal+1].active=false;
    
                for(int i=0; i<btns.length; i++) {
                    if(i>0 && this.btns[i].active) {
                        if(!client.player.getAbilities().creativeMode)
                            this.btns[i].setTooltip(Tooltip.of(ERROR_CREATIVE));
                        else
                            this.btns[i].setTooltip(TT_SET);
                    }
                    this.children.add(this.btns[i]);
                }
            }
            else { // inline component
                String[] baseSuggestions = pi.suggs();

                NbtElement tempEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),path);
                final String startVal = (isString && tempEl != null) ? tempEl.asString() : BlackMagick.nbtToString(tempEl);

                this.btns = new ButtonWidget[]{ButtonWidget.builder(Text.of(keyBtnTxt), btn -> {
                    String inp = this.txts[0].getText();
                    if(path.equals("id") && inp.equals("")) {
                        inp = "stone";
                        this.txts[0].setText("stone");
                    }
                    NbtElement el = (isString && inp.length()>0) ? NbtString.of(inp) : BlackMagick.nbtFromString(inp);
                    if(inp.equals("") || el != null) {
                        ItemStack newItem;
                        if(inp.equals("")) {
                            String comp = path.replaceFirst("components.","");
                            newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,null),"components.!"+comp,new NbtCompound()));
                        }
                        else
                            newItem = BlackMagick.itemFromNbt(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el));
                        BlackMagick.setItemMain(newItem);
                        createTab(CACHE_TAB_MAIN); // required certain components
                    }
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_LEFT,5,size,20).build()};
                this.btnX = new int[]{ROW_LEFT};

                this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer,
                    ItemBuilder.this.x+ROW_LEFT+5+size, 5, 240-41-size, 20, Text.of(""))};
                this.txtX = new int[]{ROW_LEFT+5+size};
                this.txts[0].setMaxLength(131072);

                this.txts[0].setChangedListener(value -> {
                    setErrorMsg(null);
                    MutableText newBtnTt = btnTt.copy();
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
                        else if(value.equals("air"))
                            value = "";
                    }

                    if((value != null && !value.equals(startVal)) || removeItem) {
                        this.txts[0].setEditableColor(0xFFFFFF);
                        if(!noUnsaved)
                            ItemBuilder.this.markUnsaved(this.txts[0]);
                        else
                            ItemBuilder.this.markSaved(this.txts[0]);

                        if(path.startsWith("components.") && value.length()>0) {
                            NbtElement el = isString ? NbtString.of(value) : BlackMagick.nbtFromString(value);
                            if(el != null)
                                setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),path,el).asString(),inpError));
                            else {
                                try {
                                    ItemStackArgumentType.itemStack(BlackMagick.getCommandRegistries()).parse(new StringReader("stone["+path.replaceAll("components.","")+"="+value+"]"));
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
                            if(value.length()==0)
                                value = "stone";
                            try {
                                ItemStackArgumentType.itemStack(BlackMagick.getCommandRegistries()).parse(new StringReader(value));
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
                                    IntegerArgumentType.integer(1,selItem.getMaxCount()).parse(new StringReader(value));
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
                            this.txts[0].setEditableColor(0xFF5555);
                        }
                        else {
                            if(client.player.getAbilities().creativeMode) {
                                if(value.length()==0)
                                    newBtnTt = Text.empty().append(Text.of("Remove "+keyType+"\n\n")).append(btnTt);
                                else
                                    newBtnTt = Text.empty().append(Text.of("Set "+keyType+"\n\n")).append(btnTt);
                                this.btns[0].active = true;
                            }
                            else
                                newBtnTt = Text.empty().append(ERROR_CREATIVE).append("\n\n").append(btnTt);
                        }
                    }
                    else {
                        this.txts[0].setEditableColor(LABEL_COLOR);
                        ItemBuilder.this.markSaved(this.txts[0]);
                    }

                    suggsOnChanged(this.txts[0],baseSuggestions,startVal);
                    this.btns[0].setTooltip(Tooltip.of(newBtnTt));
                });

                this.btns[0].setTooltip(Tooltip.of(btnTt));
                this.txts[0].setText(startVal);

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }

        }

    }

    class RowWidgetElement extends RowWidget {

        private static final Tooltip TT_SET = Tooltip.of(Text.of("Set element"));

        /**
         * Row to edit any element in base compound.
         * Contains button to set and txt to input (or others depending on path and key).
         * Element is removed on set when txt is empty.
         */
        public RowWidgetElement(String blankElPath, NbtList path2, ButtonWidget saveBtn, String key) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString();
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"."+key;

            PathInfo pi = ComponentHelper.getPathInfo(fullPath);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==NbtElement.STRING_TYPE;

            NbtElement startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
            final String startVal = (isString && startEl != null) ? startEl.asString() : BlackMagick.nbtToString(startEl);
            NbtElement currentEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
            final String currentVal = (isString && currentEl != null) ? currentEl.asString() : BlackMagick.nbtToString(currentEl);

            String keyBtnTxt = key.replace("minecraft:","");
            int size = sizeFromName(keyBtnTxt);
            if((size>80) && (pi.type() == PathType.TRINARY || pi.type() == PathType.TOOLTIP_UNIT))
                size = 80;
            ButtonWidget keyBtn = ButtonWidget.builder(Text.of(keyBtnTxt), btn -> {})
                .dimensions(ItemBuilder.this.x+ROW_LEFT,5,size,20).build();
            keyBtn.active = false;
            keyBtn.setTooltip(Tooltip.of(getButtonTooltip(pi,key)));

            if(ComponentHelper.isComplex(pi.type())) {
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(getButtonText(fullPath,currentEl), btn -> {
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    NbtList newPath2 = new NbtList();
                    newPath2.add(NbtString.of(currentPath2+"."+key));
                    if(path2 != null) {
                        for(int i=0; i<path2.size(); i++)
                            newPath2.add(path2.get(i));
                    }
                    newArgs.put("path2",newPath2);
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_LEFT+size+5,5,ROW_RIGHT-ROW_LEFT-20-size-5,20).build(),
                ButtonWidget.builder(Text.of(currentVal.equals("") ? "+" : "X"), btn -> {
                    if(currentVal.equals("")) {
                        NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                        NbtList newPath2 = new NbtList();
                        newPath2.add(NbtString.of(currentPath2+"."+key));
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
                        NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                        if(path2 != null) {
                            newArgs.put("path2",path2);
                        }
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                    }
                    unsel = true;
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-20,5,20,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_LEFT+size+5,ROW_RIGHT-20};
                if(!currentVal.equals("")) {
                    this.btns[1].setTooltip(Tooltip.of(Text.of("Edit element:\n"+currentVal)));
                    this.btns[1].setTooltipDelay(TOOLTIP_DELAY);
                }
                else
                    this.btns[1].active = false;
                this.btns[2].setTooltip(Tooltip.of(Text.of(currentVal.equals("") ? "Create element" : "Delete element")));
    
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
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("Unset"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,NbtByte.ZERO),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("True"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,NbtByte.ONE),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active=false;
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
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(3*btnSize+2*btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("Hide"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,
                        BlackMagick.validCompound(BlackMagick.nbtFromString("{show_in_tooltip:0b}"))),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("Show"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,new NbtCompound()),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(3*btnSize+2*btnSpacing),ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active=false;
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
                this.btns = new ButtonWidget[]{
                keyBtn,
                ButtonWidget.builder(Text.of("False"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-(2*btnSize+btnSpacing),5,btnSize,20).build(),
                ButtonWidget.builder(Text.of("True"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,new NbtCompound()),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_RIGHT-btnSize,5,btnSize,20).build()};
                this.btnX = new int[]{ROW_LEFT,ROW_RIGHT-(2*btnSize+btnSpacing),ROW_RIGHT-btnSize};
    
                for(int i=0; i<btns.length; i++) {
                    if(i==selBtn+1)
                        this.btns[i].active=false;
                    else if(i>0)
                        this.btns[i].setTooltip(TT_SET);
                    this.children.add(this.btns[i]);
                }
            }
            else {
                String[] baseSuggestions = pi.suggs();

                this.btns = new ButtonWidget[]{keyBtn};
                this.btnX = new int[]{ROW_LEFT};

                this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer,
                    ItemBuilder.this.x+ROW_LEFT+5+size, 5, ROW_RIGHT-ROW_LEFT-size-5, 20, Text.of(""))};
                this.txtX = new int[]{ROW_LEFT+5+size};
                this.txts[0].setMaxLength(131072);

                this.txts[0].setChangedListener(value -> {
                    setErrorMsg(null);

                    NbtElement el = isString ? NbtString.of(value) : BlackMagick.nbtFromString(value);
                    if(value.length()==0)
                        el = null;

                    if((value != null && !value.equals(startVal))) {
                        this.txts[0].setEditableColor(0xFFFFFF);
                        
                        if(value.length()>0 && el==null) {
                            setErrorMsg("Invalid element");
                        }

                        if(inpError != null) {
                            this.txts[0].setEditableColor(0xFF5555);
                        }
                    }
                    else {
                        this.txts[0].setEditableColor(LABEL_COLOR);
                    }
                    
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                        path2==null ? null : pagePath);

                    suggsOnChanged(this.txts[0],baseSuggestions,startVal);

                });

                this.txts[0].setText(currentVal);

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }

        }

        /**
         * Row to edit any element in a list.
         * Contains button to delete, clone, move, and edit the element in the list.
         * Add one of these for each index of the list, and also for index = NbtList.size()
         */
        public RowWidgetElement(String blankElPath, NbtList path2, ButtonWidget saveBtn, int index, int maxIndex) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString();
            String pagePath = blankElPath+currentPath2;
            String fullPath = pagePath+"["+index+"]";
            
            if(index>=0 && index<=maxIndex) {

                PathInfo pi = ComponentHelper.getPathInfo(fullPath);
                boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==NbtElement.STRING_TYPE;

                NbtElement startEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
                final String startVal = (isString && startEl != null) ? startEl.asString() : BlackMagick.nbtToString(startEl);
                NbtElement currentEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
                final String currentVal = (isString && currentEl != null) ? currentEl.asString() : BlackMagick.nbtToString(currentEl);

                Text btnTxt = null;
                int listElWidth = ROW_RIGHT-ROW_LEFT-15-15-15;

                if(ComponentHelper.isComplex(pi.type())) {
                    btnTxt = getButtonText(fullPath,currentEl);
                }
                else {
                    String[] baseSuggestions = pi.suggs();
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

                    this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer,
                        ItemBuilder.this.x+ROW_LEFT, 5, listElWidth, 20, Text.of(""))};
                    this.txtX = new int[]{ROW_LEFT};
                    this.txts[0].setMaxLength(131072);

                    this.txts[0].setChangedListener(value -> {
                        setErrorMsg(null);

                        NbtElement el = isString ? NbtString.of(value) : BlackMagick.nbtFromString(value);

                        if((value != null && !value.equals(startVal)))
                            this.txts[0].setEditableColor(0xFFFFFF);
                        else
                            this.txts[0].setEditableColor(LABEL_COLOR);
 
                        if(el==null)
                            setErrorMsg("Invalid element");
                        if(inpError != null) {
                            this.txts[0].setEditableColor(0xFF5555);
                        }
                        
                        if(el != null)
                            setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                                BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                                path2==null ? null : pagePath);

                        suggsOnChanged(this.txts[0],baseSuggestions,startVal);
                    });

                    this.txts[0].setText(currentVal);
                }

                int btnOffset = 0;
                if(btnTxt == null) {
                    this.btns = new ButtonWidget[4];
                    this.btnX = new int[4];
                    this.btnY = new int[]{0,0,0,10};
                }
                else {
                    this.btns = new ButtonWidget[5];
                    this.btnX = new int[5];
                    this.btnY = new int[]{0,0,0,0,10};
                    btnOffset = 1;

                    this.btns[0] = ButtonWidget.builder(btnTxt, btn -> {
                        NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                        NbtList newPath2 = new NbtList();
                        newPath2.add(NbtString.of(currentPath2+"["+index+"]"));
                        if(path2 != null) {
                            for(int i=0; i<path2.size(); i++)
                                newPath2.add(path2.get(i));
                        }
                        newArgs.put("path2",newPath2);
                        if(blankTabEl != null)
                            newArgs.put("overrideEl",blankTabEl);
                        createBlankTab(0,newArgs);
                        unsel = true;
                    }).dimensions(ItemBuilder.this.x+ROW_LEFT,5,listElWidth,20).build();
                    this.btnX[0] = ROW_LEFT;
                    this.btns[0].setTooltip(Tooltip.of(Text.of("Edit element:\n"+currentVal)));
                    this.btns[0].setTooltipDelay(TOOLTIP_DELAY);
                }

                this.btnX[btnOffset+0] = ROW_LEFT+listElWidth;
                this.btnX[btnOffset+1] = ROW_LEFT+listElWidth+15;
                this.btnX[btnOffset+2] = ROW_LEFT+listElWidth+15+15;
                this.btnX[btnOffset+3] = ROW_LEFT+listElWidth+15+15;

                //del btn
                int currentBtn = btnOffset;
                this.btns[currentBtn] = ButtonWidget.builder(Text.of("X"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,null),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(this.btnX[currentBtn],5+this.btnY[currentBtn],15,20).build();
                if(index<0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.of(Text.of("Delete")));

                //clone btn
                currentBtn++;
                this.btns[currentBtn] = ButtonWidget.builder(Text.of("*"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.cloneListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(this.btnX[currentBtn],5+this.btnY[currentBtn],15,20).build();
                if(index<0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.of(Text.of("Clone")));

                //up btn
                currentBtn++;
                this.btns[currentBtn] = ButtonWidget.builder(Text.of("\u2227"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.moveListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index,true),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(this.btnX[currentBtn],5+this.btnY[currentBtn],15,10).build();
                if(index<=0 || index>maxIndex)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.of(Text.of("Move Up")));

                //down btn
                currentBtn++;
                this.btns[currentBtn] = ButtonWidget.builder(Text.of("\u2228"), btn -> {
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.moveListElement(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,index,false),blankElPath),saveBtn,
                        path2==null ? null : pagePath);
                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(this.btnX[currentBtn],5+this.btnY[currentBtn],15,10).build();
                if(index>=maxIndex || index<0)
                    this.btns[currentBtn].active = false;
                else
                    this.btns[currentBtn].setTooltip(Tooltip.of(Text.of("Move Down")));

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
                for(int i=0; i<txts.length; i++) {
                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }
            else { // add new element to list
                Text btnTxt = Text.of("Add Element");
                PathInfo pi = ComponentHelper.getPathInfo(pagePath);
                PathInfo pie = ComponentHelper.getPathInfo(pagePath+"[0]");

                this.btns = new ButtonWidget[]{
                ButtonWidget.builder(btnTxt, btn -> {
                    NbtElement el = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath);
                    NbtList list;
                    if(el != null && el.getType()==NbtElement.LIST_TYPE) {
                        list = (NbtList)el;
                    }
                    else
                        list = new NbtList();
                    
                    if(list.size()>0) {
                        NbtElement newEl = BlackMagick.getDefaultNbt(list.get(0).getType());
                        if(newEl.getType() == NbtElement.STRING_TYPE && pie.type()==PathType.TEXT)
                            newEl = NbtString.of("\"\"");
                        if(newEl != null)
                            list.add(newEl);
                    }
                    else {
                        if(pi.type()==PathType.LIST && pi.listType()>=0) {
                            NbtElement newEl = BlackMagick.getDefaultNbt(pi.listType());
                            if(newEl.getType() == NbtElement.STRING_TYPE && pie.type()==PathType.TEXT)
                                newEl = NbtString.of("\"\"");
                            if(newEl != null)
                                list.add(newEl);
                        }
                        else {
                            FortytwoEdit.LOGGER.warn("Failed to add element to unknown list at path: "+pagePath);
                        }
                    }

                    if(list.size()>0) {
                        setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                            BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),pagePath,list),blankElPath),saveBtn,
                            path2==null ? null : pagePath);
                    }

                    NbtCompound newArgs = BlackMagick.validCompound(BlackMagick.nbtFromString("{path:\""+blankElPath+"\"}"));
                    if(path2 != null) {
                        newArgs.put("path2",path2);
                    }
                    if(blankTabEl != null)
                        newArgs.put("overrideEl",blankTabEl);
                    createBlankTab(0,newArgs);
                }).dimensions(ItemBuilder.this.x+ROW_LEFT,5,80,20).build()};
                this.btnX = new int[]{ROW_LEFT};
                this.btns[0].setTooltip(Tooltip.of(Text.of("Add a default element to the list")));

                for(int i=0; i<btns.length; i++)
                    this.children.add(this.btns[i]);
            }

        }

        /**
         * Used for editPath fallback (only one textfieldwidget).
         */
        public RowWidgetElement(String blankElPath, NbtList path2, ButtonWidget saveBtn) {
            super();

            String currentPath2;
            if(path2==null)
                currentPath2 = "";
            else
                currentPath2 = path2.get(0).asString();
            String fullPath = blankElPath+currentPath2;

            FortytwoEdit.LOGGER.warn("Fallback page created for path: "+fullPath);

            PathInfo pi = ComponentHelper.getPathInfo(fullPath);
            boolean isString = ComponentHelper.pathTypeToNbtType(pi.type())==NbtElement.STRING_TYPE;

            NbtElement tempEl = BlackMagick.getNbtPath(BlackMagick.itemToNbt(selItem),fullPath);
            final String startVal = (isString && tempEl != null) ? tempEl.asString() : BlackMagick.nbtToString(tempEl);
            tempEl = BlackMagick.getNbtPath(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath);
            final String currentVal = (isString && tempEl != null) ? tempEl.asString() : BlackMagick.nbtToString(tempEl);

            String[] baseSuggestions = pi.suggs();

            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer,
                ItemBuilder.this.x+ROW_LEFT, 5, ROW_RIGHT-ROW_LEFT, 20, Text.of(""))};
            this.txtX = new int[]{ROW_LEFT};
            this.txts[0].setMaxLength(131072);

            this.txts[0].setChangedListener(value -> {
                setErrorMsg(null);
                NbtElement el = isString ? NbtString.of(value) : BlackMagick.nbtFromString(value);

                if(el != null)
                    setEditingElement(blankElPath,BlackMagick.getNbtPath(BlackMagick.setNbtPath(
                        BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl),fullPath,el),blankElPath),saveBtn,
                        path2==null ? null : fullPath);
                else
                    setErrorMsg("Invalid element");

                if((value != null && !value.equals(startVal))) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    if(inpError == null)
                        setErrorMsg(BlackMagick.getItemCompoundErrors(BlackMagick.setNbtPath(BlackMagick.itemToNbt(selItem),blankElPath,blankTabEl).asString(),inpError));
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                }

                if(inpError != null)
                    this.txts[0].setEditableColor(0xFF5555);

                suggsOnChanged(this.txts[0],baseSuggestions,startVal);
            });

            this.txts[0].setText(currentVal);

            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
            }            
        }

    }

    class RowWidgetInvRow extends RowWidget {
        
        private final Sprite[] SPRITES = new Sprite[]{
            client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE),
            client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE),
            client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE),
            client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE),
            client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT)};

        private int slotSprites[] = null;

        private static final Identifier SEL_SLOT = new Identifier("hud/hotbar_selection");
        private boolean isHotbarRow = false;

        /**
         * Used for player inventory rows. Always make 5 rows (3 for inv, 1 for hotbar, 1 for armor/offhand).
         * 
         * @param row
         */
        public RowWidgetInvRow(int row) {
            super();
    
            this.savedStacksMode = 0;
            if(row >= 0 && row < 4) {
                this.savedStacks = new ItemStack[9];
                this.btns = new ButtonWidget[9];
                this.btnX = new int[9];
            }
            else if(row == 4) {
                this.slotSprites = new int[]{1,2,3,4,5};
                this.savedStacks = new ItemStack[5];
                this.btns = new ButtonWidget[5];
                this.btnX = new int[5];
                isHotbarRow = true;
            }

            int currentX = 10+30;
            if(row==4)
                currentX += 20;
            for(int i=0; i<this.savedStacks.length; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                this.savedStacks[i] = cacheInv[index];
                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    ItemBuilder.this.unsel = true;
                }).dimensions(currentX,5,20,20).build();
                if(cacheInv[index] != null && !cacheInv[index].isEmpty())
                    this.btns[i].setTooltip(makeItemTooltip(cacheInv[index]));
                currentX += 20;
                if(row==4 && i==3)
                    currentX += 40;
                this.btns[i].active = false;
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
        public RowWidgetInvRow(ItemStack[] stacks, int[] slotSprites) {
            super();
    
            this.savedStacksMode = 0;
            this.savedStacks = stacks;
            this.btns = new ButtonWidget[stacks.length];
            this.btnX = new int[stacks.length];
            if(slotSprites != null)
                this.slotSprites = slotSprites;

            int currentX = 10+30;
            for(int i=0; i<this.savedStacks.length; i++) {
                this.btnX[i] = currentX;
                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    ItemBuilder.this.unsel = true;
                }).dimensions(currentX,5,20,20).build();
                if(this.savedStacks[i] != null && !this.savedStacks[i].isEmpty())
                    this.btns[i].setTooltip(makeItemTooltip(this.savedStacks[i]));
                currentX += 20;
                this.btns[i].active = false;
                this.btns[i].setTooltipDelay(TOOLTIP_DELAY_SHORT);

                this.children.add(this.btns[i]);
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            if(this.slotSprites != null && savedStacksMode == 0 && this.savedStacks != null
            && this.savedStacks.length == this.btnX.length && this.slotSprites.length == this.savedStacks.length) {
                for(int i=0; i<this.savedStacks.length; i++)
                    if((this.savedStacks[i] == null || this.savedStacks[i].isEmpty()) && this.slotSprites[i]>0 && this.slotSprites[i]<=this.SPRITES.length)
                        context.drawSprite(x+this.btnX[i]+2,y+2,0,16,16,this.SPRITES[this.slotSprites[i]-1]);
            }
            if(this.isHotbarRow) {
                int sel = client.player.getInventory().selectedSlot;
                context.drawGuiTexture(SEL_SLOT, x+(sel*20)+40-2, y-20-2, 24, 23);
            }
        }

    }

    private static abstract class AbstractWidget
    extends ElementListWidget.Entry<AbstractWidget> {
        @Nullable
        final List<OrderedText> description;

        public AbstractWidget() {
            this.description = null;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    class PoseSlider
    extends SliderWidget {//TODO pose slider
        private final double min;
        private final double max;
        public float val;
        private final int part;
        private final int num;

        public PoseSlider(int part, int num) {
            super(0, 0, 180, 20, Text.of(""), 0.0);
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
            val = (float)((int)(this.value*(max-min)+min));

            if(ItemBuilder.this.armorPose == null)
                ItemBuilder.this.armorPose = new float[6][];

            if(ItemBuilder.this.armorPose[part] == null)
                ItemBuilder.this.armorPose[part] = new float[3];

            ItemBuilder.this.armorPose[part][num] = (float)val;
            updatePose();
            //ItemBuilder.this.unsavedPose = true;

            if(sliderBtns.size() > part) {
                sliderBtns.get(part).btns[0].setTooltip(Tooltip.of(Text.of("Remove Pose")));
                if(client.player.getAbilities().creativeMode)
                    sliderBtns.get(part).btns[0].active = true;
            }
        }

        @Override
        protected void updateMessage() {
            if((float)((int)val) == val)
                this.setMessage(Text.of(""+(int)val));
            else
                this.setMessage(Text.of(""+val));
        }

        public void setVal(float newVal) {
            while(newVal > 180)
                newVal -= 360;
            while(newVal < -180)
                newVal += 360;
            this.value = (double)((newVal - min)/(max-min));
            val = newVal;

            if(ItemBuilder.this.armorPose == null)
                ItemBuilder.this.armorPose = new float[6][];

            if(ItemBuilder.this.armorPose[part] == null)
                ItemBuilder.this.armorPose[part] = new float[3];

            ItemBuilder.this.armorPose[part][num] = val;
            updatePose();
            updateMessage();
            //ItemBuilder.this.unsavedPose = true;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    class RgbSlider //TODO rgb slider
    extends SliderWidget {
        private final double min;
        private final double max;
        public int num;

        public RgbSlider(int num) {
            super(0, 0, num<=2 ? 180-40 : 120-15-5, 20, Text.of(""), 0.0);
            this.min = 0f;
            this.max = 255f;
            this.num = num;
            if(num>2)
                this.num = num-3;
            this.value = (rgb[this.num] - min) / (max - min);
            this.applyValue();
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            rgb[num] = (int)(this.value*(max-min)+min);
            for(int i=0; i<rgbChanged.length; i++)
                rgbChanged[i] = true;
            updateRgbSliders();
        }

        @Override
        protected void updateMessage() {
            String color = "\u00a7";
            if(num == 0 || num == 3)
                color += "4";
            else if(num == 1 || num == 4)
                color += "2";
            else
                color += "1";
            this.setMessage(Text.of(color+rgb[num]));
        }

        public void setVal(int newVal) {
            if(newVal > 255)
                newVal = 255;
            else if(newVal < 0)
                newVal = 0;
            rgb[num] = newVal;
            this.value = (double)((newVal - min)/(max-min));
            updateMessage();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static class Tab {
        public int pos;
        public String lbl;
        public ItemStack display;
        public boolean hideTabs;

        public Tab(int pos, String lbl, ItemStack display) {
            this.pos = pos;
            this.lbl = lbl;
            this.display = display;

            if(lbl == null)
                this.lbl = "";
            if(display == null)
                this.display = new ItemStack(Items.BARRIER);
        }

        public Tab() {
            this.pos = -1;
            this.lbl = "";
            this.display = new ItemStack(Items.AIR);
            this.hideTabs = true;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class PosWidget {
        public ClickableWidget w;
        public int x;
        public int y;

        public PosWidget(ClickableWidget w, int x, int y) {
            this.w = w;
            this.x = x;
            this.y = y;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if(!tabs[tab].hideTabs) {
            for(Tab t: tabs) {
                if(t.pos>=0) {
                    if(t.pos<LEFT_TABS)
                        drawItem(context,t.display,x-TAB_SIZE+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*t.pos+(TAB_SIZE/2-8));
                    else
                        drawItem(context,t.display,x+240+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(t.pos-LEFT_TABS)+(TAB_SIZE/2-8));
                }
            }

            if(tab == CACHE_TAB_SAVED) { 
                if(viewBlackMarket)
                    drawItem(context,savedModeItems[1], x+14, y+38);
                else
                    drawItem(context,savedModeItems[0], x+14, y+38);
            }
            
            if(tab == 5 && editArmorStand) //TODO render armorstand
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)renderArmorStand);
            else if(tab == 6) {
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)renderArmorPose);
            }
            else
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)this.client.player);

            drawItem(context,selItem, x+240-20-5+2, y+5+2);
            txtFormat.setX(x+50);
            txtFormat.render(context, mouseX, mouseY, delta);
            if(!this.unsavedTxtWidgets.isEmpty())
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Unsaved"), this.width / 2, y-11, 0xFFFFFF);
            if(savedError)
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Failed to read saved items!"), this.width / 2, y-11-10, 0xFF5555);
        }
        else {
            if(jsonPreview != null) {
                txtFormat.setX(x-15);
                txtFormat.render(context, mouseX, mouseY, delta);

                if(jsonPreviewBook) {
                    int i = x - 150 - 1;
                    int j = y+7;
                    StringVisitable stringVisitable = jsonPreview;
                    List<OrderedText> page = this.textRenderer.wrapLines(stringVisitable, 114);
                    int l = Math.min(128 / this.textRenderer.fontHeight, page.size());
                    for (int m = 0; m < l; ++m) {
                        OrderedText orderedText = page.get(m);
                        context.drawText(this.textRenderer, orderedText, i + 36, j + 32 + m * this.textRenderer.fontHeight, 0, false);
                    }
                    Style style = this.getBookTextStyleAt(page, i, j, mouseX, mouseY);
                    if (style != null) {
                        context.drawHoverEvent(this.textRenderer, style, mouseX, mouseY);
                    }
                }
                else
                    context.drawCenteredTextWithShadow(this.textRenderer, jsonPreview, this.width / 2, y-14, 0xFFFFFF);
    
                if(tab != CACHE_TAB_BLANK) {
                    jsonPreview = null;
                }
            }
            else if(blankTabUnsaved && tab == CACHE_TAB_BLANK)
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Unsaved"), this.width / 2, y-11, 0xFFFFFF);

            if(showBannerPreview && bannerChangePreview != null) {
                if(!bannerShield)
                    InventoryScreen.drawEntity(context,x+240,y,x+240+100,y+400,2*RENDER_SIZE,0f,x+240+50,y+200,(LivingEntity)bannerChangePreview);
                else
                    InventoryScreen.drawEntity(context,x+240,y,x+240+100,y+200,2*RENDER_SIZE,0f,x+240+50,y+100,(LivingEntity)bannerChangePreview);
            }
        }
        if(inpErrorTrim != null)
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of(inpErrorTrim), this.width / 2, y+this.backgroundHeight+3, 0xFF5555);

        if(suggs != null)
            suggs.render(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context, delta, mouseX, mouseY, 1);
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        resetSuggs();
        super.resize(client, width, height);
        setErrorMsg(inpError);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(suggs != null && suggs.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            if(this.unsavedTxtWidgets.isEmpty() && !activeTxt() && !tabs[tab].hideTabs) {
                if(!pauseSaveScroll && tabWidget != null) {
                    tabScroll[tab] = tabWidget.getScrollAmount();
                }
                this.client.setScreen(null);
                return true;
            }
        }
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if(!pauseSaveScroll && tabWidget != null) {
                tabScroll[tab] = tabWidget.getScrollAmount();
            }
        }
        if(keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            if(!activeTxt()) {
                if(this.unsavedTxtWidgets.isEmpty() && !tabs[tab].hideTabs && hotbarLeftBtn.active) {
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
        if (suggs != null && suggs.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (suggs != null && suggs.mouseClicked(mouseX, mouseY, button)) {
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
        else if(tab == CACHE_TAB_MAIN && widgets.get(tab).isEmpty())
            createTab(tab);

        if(unsel) {
            unfocus();
            unsel = false;
        }
    }

}
