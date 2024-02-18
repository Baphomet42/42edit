package baphomethlabs.fortytwoedit.gui.screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import baphomethlabs.fortytwoedit.mixin.HotbarStorageAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class ItemBuilder extends GenericScreen {

    protected boolean unsel = false;
    protected static int tab = 0;
    private boolean firstInit = true;
    protected static final int TAB_OFFSET = 5;
    protected static final int TAB_SIZE = 24;
    protected static final int TAB_SPACING = 2;
    protected static final Tab[] tabs = new Tab[]{new Tab(0,"General",new ItemStack(Items.GOLDEN_SWORD)), new Tab(1,"Blocks",new ItemStack(Items.PURPLE_SHULKER_BOX)),
        new Tab(2,"Misc",FortytwoEdit.HEAD42), new Tab(4,"Custom NBT",new ItemStack(Items.COMMAND_BLOCK)),
        new Tab(9,"Saved Items",new ItemStack(Items.JIGSAW)), new Tab(3,"Entity Data",new ItemStack(Items.ENDER_DRAGON_SPAWN_EGG)),
        new Tab(5,"Armor Pose",new ItemStack(Items.ARMOR_STAND)), new Tab(8,"Inventory",new ItemStack(Items.ENDER_CHEST)),
        new Tab(7,"Text",new ItemStack(Items.NAME_TAG)), new Tab("TextEdit",true), new Tab("TextEffect",true),
        new Tab(6,"Attributes",new ItemStack(Items.ENCHANTED_BOOK)), new Tab("ListEdit",true)};
    private static final int LEFT_TABS = 5;
    protected ItemStack selItem = null;
    protected String cacheItemFull = "";
    protected String cacheBlock = "";
    protected ArrayList<ArrayList<String>> cacheStates = new ArrayList<>();
    protected ButtonWidget itemBtn;
    protected int itemsEqual = -1;
    protected ButtonWidget swapBtn;
    protected ButtonWidget swapCopyBtn;
    protected ButtonWidget throwCopyBtn;
    private TextFieldWidget txtFormat;
    private TabWidget tabWidget;
    private final ArrayList<ArrayList<PosWidget>> noScrollWidgets = new ArrayList<ArrayList<PosWidget>>();
    private final ArrayList<ArrayList<NbtWidget>> widgets = new ArrayList<ArrayList<NbtWidget>>();
    private final Set<ClickableWidget> unsavedTxtWidgets = Sets.newHashSet();
    private final Set<ClickableWidget> allTxtWidgets = Sets.newHashSet();
    public static boolean savedModeSet = false;
    private NbtList savedItems = null;
    private boolean savedError = false;
    private static boolean viewBlackMarket = false;
    private static NbtList webItems = null;
    private static final ItemStack[] savedModeItems = new ItemStack[]{ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString(
        "{id:player_head,Count:1,tag:{SkullOwner:{Id:[I;1997664788,2121809964,-2050088034,-1558826484],Properties:{textures:[{Value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQ"
        +"iIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZDlmMThjOWQ4NWY5MmY3"
        +"MmY4NjRkNjdjMTM2N2U5YTQ1ZGMxMGYzNzE1NDljNDZhNGQ0ZGQ5ZTRmMTNmZjQiDQogICAgfQ0KICB9DQp9\"}]}}}}")),
        ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:player_head,Count:1,tag:{SkullOwner:{Id:[I;-1528398943,522535610,-1505917405,2012489093],"
        +"Properties:{textures:[{Value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3"
        +"RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MjY0ODZmNDI0ODljZWYwMmM5ZTk4ZGQ4YmU1YTNmMzhlODc5MTQ3NTQzMjZlNzdjODM3YzFiMmJjYmE2NSINCiAgICB9DQogIH0NCn0=\"}]}}}}")),
        new ItemStack(Items.BARRIER)};
    private ArmorStandEntity renderArmorStand;
    private ArmorStandEntity renderArmorPose;
    protected final int playerX = 240+10;
    protected final int playerY = -10;
    private static final int RENDER_SIZE = 35;
    private ArrayList<NbtWidget> sliders = new ArrayList<>();
    private ArrayList<NbtWidget> sliderBtns = new ArrayList<>();
    private ButtonWidget setPoseButton;
    private boolean editArmorStand = false;
    private boolean unsavedPose = false;
    private float[][] armorPose;
    public static final String BANNER_PRESET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
    public static final String[] BANNER_CHAR_LIST = new String[BANNER_PRESET_CHARS.replaceAll("\\s","").length()];
    public static final String[] DYES = {"black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"};
    private TextSuggestor suggs;
    private Set<TextFieldWidget> currentTxt = Sets.newHashSet();
    private static int[] rgb = new int[]{66,6,102,0,0,0};
    private ArrayList<NbtWidget> rgbSliders = new ArrayList<>();
    private boolean rgbChanged[] = {true,true};//used to update txtwids {general rgb dec, general rgb hex}
    private static final ItemStack[] rgbItems = new ItemStack[]{new ItemStack(Items.LEATHER_CHESTPLATE),new ItemStack(Items.POTION),new ItemStack(Items.FILLED_MAP)};
    private String jsonItem = "";
    private NbtString jsonName = null;
    private NbtList jsonLore = null;
    private NbtList jsonPages = null;
    private NbtString jsonCurrent = null;
    private boolean jsonUnsaved = false;
    private boolean json2Unsaved = false;
    private Map<String,int[]> cacheI = Maps.newHashMap();
    private Text jsonPreview = Text.of("");
    private Text jsonPreview2 = Text.of("");
    private int jsonEffectMode = -1;
    private static String jsonLastColor = "reset";
    private boolean jsonEffectValid = false;
    private String jsonEffectFull = "";
    private static int[] jsonEffects = new int[8];//bold,italic,underlined,strikethrough,obfuscated,radgrad,colmode,elmode
    private static double[] tabScroll = new double[tabs.length];
    private boolean pauseSaveScroll = false;
    private String listItem = "";
    private NbtList[] listEdit = new NbtList[6];//Potions,SusEffects,Fireworks,Banners,Attributes,Enchants
    private NbtCompound listCurrent = null;
    private boolean listCurrentValid = false;
    private String listCurrentPath = "";
    private int listCurrentIndex = -1;
    private boolean listUnsaved = false;
    private int[] listPotionBtns = new int[3];//showparticles,showicon,ambient
    private int[] listFireworkBtns = new int[2];//flicker,trail
    private String listBannerPat = null;
    private String listBannerCol = null;
    private boolean bannerShield = false;
    private static ArmorStandEntity bannerChangePreview = null;
    private static final ItemStack[] listItems = new ItemStack[]{
        ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:potion,Count:1,tag:{CustomPotionColor:10027263}}")),new ItemStack(Items.SUSPICIOUS_STEW),
        new ItemStack(Items.FIREWORK_ROCKET),FortytwoEdit.BANNERBRICK,new ItemStack(Items.NETHER_STAR),new ItemStack(Items.ENCHANTED_BOOK)};
    private ItemStack[] cacheInv = new ItemStack[41];
    private int cacheInvSlot = -1;
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = 1;

        if(firstInit) {
            if(tab == 9 || tab == 10)
                tab = 8;
            else if(tab == 12)
                tab = 11;
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
            this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnChangeSlot(true)).dimensions(width/2 - 15,y+5,15,20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnChangeSlot(false)).dimensions(width/2,y+5,15,20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.of("Q"), button -> this.btnThrow(false)).dimensions(width/2 + 15,y+5,15,20).build());
            throwCopyBtn = this.addDrawableChild(ButtonWidget.builder(Text.of("Q*"), button -> this.btnThrow(true)).dimensions(width/2 + 30,y+5,20,20).build());
            
            if(!client.player.getAbilities().creativeMode) {
                swapCopyBtn.active = false;
                throwCopyBtn.active = false;
                swapBtn.active = false;
            }

            itemBtn = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> this.btnCopyNbt()).dimensions(x+240-20-5,y+5,20,20).build());
        }
        updateItem();

        //tabs
        if(widgets.size() == 0)
            createWidgets();
        this.tabWidget = null;
        for(int i=0; i<noScrollWidgets.get(tab).size(); i++) {
            PosWidget p = noScrollWidgets.get(tab).get(i);
            p.w.setX(x+p.x);
            p.w.setY(y+p.y);
            this.addDrawableChild(p.w);
        }
        if(tab != 3 && tab != 9 && widgets.get(tab).size()!=0) {
            this.tabWidget = new TabWidget(tab);
            this.tabWidget.setRenderBackground(false);
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
            bannerChangePreview.readNbt((NbtCompound)BlackMagick.elementFromString("{Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
        }

        //banner
        int i=0;
        for(char c : BANNER_PRESET_CHARS.replaceAll("\\s","").toCharArray()) {
            BANNER_CHAR_LIST[i] = ""+c;
            i++;
        }

    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnSwapOff(boolean copy) {
        if(!copy) {
            if(client.player.getAbilities().creativeMode) {
                ItemStack mainhand = null;
                ItemStack offhand = null;
                if(!client.player.getMainHandStack().isEmpty())
                    mainhand=client.player.getMainHandStack().copy();
                if(!client.player.getOffHandStack().isEmpty())
                    offhand=client.player.getOffHandStack().copy();

                if(mainhand!=null && offhand!=null) {
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
                else if(mainhand!=null && offhand==null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
                else if(mainhand==null && offhand!=null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        }
        else {
            if(client.player.getAbilities().creativeMode) {
                if(!client.player.getMainHandStack().isEmpty()) {
                    ItemStack item=client.player.getMainHandStack().copy();
                    client.interactionManager.clickCreativeStack(item, 45);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
                else if(!client.player.getOffHandStack().isEmpty()) {
                    ItemStack item=client.player.getOffHandStack().copy();
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        }
        unsel = true;
    }

    protected void btnChangeSlot(boolean left) {
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
        unsel = true;
    }

    protected void btnThrow(boolean copy) {
        if(!copy) {
            client.player.dropSelectedItem(true);
            client.player.playerScreenHandler.sendContentUpdates();
        }
        else if(client.player.getAbilities().creativeMode) {
            ItemStack item = client.player.getMainHandStack().copy();
            client.player.dropSelectedItem(true);
            client.player.playerScreenHandler.sendContentUpdates();
            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
            client.player.playerScreenHandler.sendContentUpdates();
        }
        unsel = true;
    }

    protected void btnTab(int i) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        tab = i;
        suggs = null;
        this.resize(this.client,this.width,this.height);
    }

    protected void btnCopyNbt() {
        if(client.player.getMainHandStack() != null && !client.player.getMainHandStack().isEmpty()) {
            String itemData = BlackMagick.formatItem(client.player.getMainHandStack());
            client.player.sendMessage(Text.of(itemData),false);
            client.keyboard.setClipboard(itemData);
        }
        unsel = true;
    }

    private void updateItem() {
        client.player.playerScreenHandler.sendContentUpdates();
        selItem = client.player.getMainHandStack().copy();
        if(selItem==null || selItem.isEmpty()) {
            itemBtn.active = false;
            itemBtn.setTooltip(null);
            if(!cacheBlock.equals("")) {
                cacheBlock = "";
                cacheStates = new ArrayList<>();
            }
            cacheItemFull = "air";
        }
        else {
            itemBtn.active = true;
            String newCacheItem = BlackMagick.formatItem(selItem);
            if(!cacheItemFull.equals(newCacheItem)) {
                cacheItemFull = newCacheItem;
                itemBtn.setTooltip(makeItemTooltip(selItem));
            }
            if(!selItem.getItem().toString().equals(cacheBlock))
                getBlockStates(selItem.getItem());
        }
    }

    private void compareItems() {
        
        if(!client.player.getMainHandStack().isEmpty() && !client.player.getOffHandStack().isEmpty()) {
            if(BlackMagick.formatItem(client.player.getMainHandStack(),false,true)
            .equals(BlackMagick.formatItem(client.player.getOffHandStack(),false,true))) {
                if(itemsEqual != 1) {
                    itemsEqual = 1;
                    updateCompareItems();
                }
            }
            else {
                if(itemsEqual != 0) {
                    itemsEqual = 0;
                    updateCompareItems();
                }
            }
        }
        else {
            if(itemsEqual != -1) {
                itemsEqual = -1;
                updateCompareItems();
            }
        }

    }

    private void updateCompareItems() {
        if(itemsEqual == -1) {
            swapBtn.setMessage(Text.of("c"));
        }
        else if(itemsEqual == 0) {
            swapBtn.setMessage(Text.of("\u00a7cc"));
        }
        else if(itemsEqual == 1) {
            swapBtn.setMessage(Text.of("\u00a7ac"));
        }
    }

    protected void markUnsaved(ClickableWidget widget) {
        this.unsavedTxtWidgets.add(widget);
    }

    protected void markSaved(ClickableWidget widget) {
        this.unsavedTxtWidgets.remove(widget);
    }

    protected boolean activeTxt() {
        for(ClickableWidget w : allTxtWidgets)
            if(w.isFocused())
                return true;
        return false;
    }

    protected Tooltip makeItemTooltip(ItemStack stack) {
        if(stack==null || stack.isEmpty())
            return Tooltip.of(Text.of("Failed to read item"));
        String itemData = "";
        if(stack.hasNbt()) {
            itemData += stack.getNbt().asString();
        }
        itemData = makeItemTooltipShorten(itemData);
        if(stack.getCount()>1)
            itemData += " " + stack.getCount();
        
        return Tooltip.of(Text.empty().append(stack.getName()).append(Text.of("\n"+stack.getItem().toString()+itemData)));

    }

    protected Tooltip makeItemTooltip(NbtCompound nbt) {
        if(nbt==null)
            return Tooltip.of(Text.of("Failed to read item"));
        String itemData = "";
        if(nbt.contains("tag",NbtElement.COMPOUND_TYPE)) {
            itemData += nbt.get("tag").asString();
        }
        itemData = makeItemTooltipShorten(itemData);
        if(nbt.contains("Count",NbtElement.INT_TYPE) && ((NbtInt)nbt.get("Count")).intValue() >1)
            itemData += " " + nbt.get("Count").asString();
        if(nbt.contains("id",NbtElement.STRING_TYPE))
            return Tooltip.of(Text.empty().append("Unknown Item").append(Text.of("\n"+nbt.get("id").asString()+itemData)));
        else return Tooltip.of(Text.of("Failed to read item"));
    }

    private String makeItemTooltipShorten(String itemData) {
        //remove SkullOwner Properties
        while(itemData.contains("Properties:{textures:")) {
            int propertiesIndex = itemData.indexOf("Properties:{textures:");
            String firstHalf = itemData.substring(0,propertiesIndex)+"Properties:{...}";
            String secondHalf = itemData.substring(propertiesIndex+21);
            int bracketCount = 1;
            int nextOpenBracket;
            int nextCloseBracket;
            while(bracketCount!=0 && secondHalf.length()>0) {
                nextOpenBracket = secondHalf.indexOf('{');
                nextCloseBracket = secondHalf.indexOf('}');
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
        //remove SkullOwner UUID
        while(itemData.contains("Id:[I;")) {
            int idIndex = itemData.indexOf("Id:[I;");
            String firstHalf = itemData.substring(0,idIndex)+"Id:[...]";
            String secondHalf = itemData.substring(idIndex+6);
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
        //remove SkullOwnerOrig
        while(itemData.contains("SkullOwnerOrig:[I;")) {
            int origIndex = itemData.indexOf("SkullOwnerOrig:[I;");
            String firstHalf = itemData.substring(0,origIndex)+"\u00a74\u26a0\u00a7rSkullOwnerOrig:[...]";
            String secondHalf = itemData.substring(origIndex+18);
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
        return itemData;
    }

    private void refreshSaved() {
        savedItems = FortytwoEdit.getSavedItems();
        if(savedItems == null) {
            savedError = true;
            NbtList nbt = new NbtList();
            while(nbt.size()<9*FortytwoEdit.SAVED_ROWS) {
                NbtCompound air = new NbtCompound();
                air.putString("id","air");
                air.putInt("Count",0);
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
            air.putString("id","air");
            air.putInt("Count",0);
            webItems.add(air);
        }
        updateSavedTab();
    }
    private void updateSavedTab() {
        if(widgets.get(4).size() == FortytwoEdit.SAVED_ROWS)
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
                widgets.get(4).get(i).updateSavedDisplay();
    }

    private void setSavedModeTooltip() {
        if(viewBlackMarket) {
            noScrollWidgets.get(4).get(0).w.setTooltip(Tooltip.of(Text.of("Black Market Items")));
            noScrollWidgets.get(4).get(1).w.setTooltip(Tooltip.of(Text.of("Refresh from Web")));
            noScrollWidgets.get(4).get(1).w.setMessage(Text.of("\u27F3"));
        }
        else {
            noScrollWidgets.get(4).get(0).w.setTooltip(Tooltip.of(Text.of("Local Items")));
            if(savedModeSet) {
                noScrollWidgets.get(4).get(1).w.setTooltip(Tooltip.of(Text.of("C - Save to slot")));
                noScrollWidgets.get(4).get(1).w.setMessage(Text.of("C"));
            }
            else {
                noScrollWidgets.get(4).get(1).w.setTooltip(Tooltip.of(Text.of("V - Get item")));
                noScrollWidgets.get(4).get(1).w.setMessage(Text.of("V"));
            }
        }
    }

    private void resetSuggs() {
        if(!currentTxt.isEmpty()) {
            currentTxt.clear();
        }
        suggs = null;
    }

    private NbtCompound updateArmorStand(ItemStack stand) {
        resetArmorStand();
        if(stand != null && stand.hasNbt()) {
            NbtCompound tag = stand.getNbt().copy();
            if(tag.contains("EntityTag",NbtElement.COMPOUND_TYPE)) {
                NbtCompound entity = tag.getCompound("EntityTag");
                entity.put("Pos",BlackMagick.elementFromString("[0d,0d,0d]"));
                entity.put("Motion",BlackMagick.elementFromString("[0d,0d,0d]"));
                entity.put("Rotation",BlackMagick.elementFromString("[0f,0f]"));
                renderArmorStand.readNbt(entity.copy());
                updatePose();
                if(entity.contains("Pose",NbtElement.COMPOUND_TYPE)) {
                    return (NbtCompound)entity.get("Pose").copy();
                }
            }
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

        setPoseButton.setTooltip(Tooltip.of(Text.of("Set Pose\n\nPose:"+pose.asString())));
        return pose.copy();
    }

    private void btnResetPose() {
        for(int i=0; i<sliders.size(); i++)
            sliders.get(i).setSlider(0f);

        for(int i=0; i<sliderBtns.size(); i++) {
            sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("No pose")));
            sliderBtns.get(i).btns[0].active = false;
        }

        armorPose = null;
        updatePose();
        unsavedPose = false;
        unsel = true;
    }

    private void btnGetPose() {
        btnResetPose();
        if(client.player.getMainHandStack().getItem().toString().equals("armor_stand") && sliders.size()==6*3) {
            NbtCompound pose = updateArmorStand(client.player.getMainHandStack().copy());
            if(pose != null) {
                armorPose = new float[6][];

                String part = "Head";
                int partNum = 0;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }
                
                part = "RightArm";
                partNum = 1;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }
                
                part = "LeftArm";
                partNum = 2;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }
                
                part = "RightLeg";
                partNum = 3;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }
                
                part = "LeftLeg";
                partNum = 4;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }
                
                part = "Body";
                partNum = 5;
                if(pose.contains(part,NbtElement.LIST_TYPE) && ((NbtList)pose.get(part)).size() == 3
                        && ((NbtList)pose.get(part)).get(0).getType() == NbtElement.FLOAT_TYPE) {
                    NbtList list = (NbtList)pose.get(part);
                    armorPose[partNum] = new float[3];
                    for(int i=0; i<3; i++)
                        sliders.get(partNum*3+i).setSlider(((NbtFloat)list.get(i)).floatValue());
                }

            }
        }
        updatePose();
        unsavedPose = false;
        unsel = true;

        for(int i=0; i<armorPose.length; i++) {
            if(armorPose[i] == null) {
                sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("No pose")));
                sliderBtns.get(i).btns[0].active = false;
            }
            else {
                sliderBtns.get(i).btns[0].setTooltip(Tooltip.of(Text.of("Remove Pose")));
                if(client.player.getAbilities().creativeMode)
                    sliderBtns.get(i).btns[0].active = true;
            }
        }
    }

    private void btnSetPose() {
        ItemStack item = BlackMagick.setId("armor_stand");
        if(armorPose != null) {
            boolean empty = true;
            for(int i=0; i<armorPose.length; i++)
                if(armorPose[i] != null)
                    empty = false;

            if(!empty) {
                BlackMagick.setNbt(item,"EntityTag/Pose",updatePose());
                unsavedPose = false;
                unsel = true;
                return;
            }
        }
        BlackMagick.removeNbt(item,"EntityTag/Pose");
        unsavedPose = false;
        unsel = true;
    }

    private void getBlockStates(Item i) {
        cacheBlock = i.toString();
        cacheStates = new ArrayList<>();
        BlockState blockState = Block.getBlockFromItem(i).getDefaultState();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(entry.getKey().getName());
            for(Comparable<?> val : entry.getKey().getValues()) {
                list.add((String)Util.getValueAsString(entry.getKey(), val));
            }
            cacheStates.add(list);
        }
    }

    private String[] getStatesArr() {
        if(cacheStates.size()>0) {
            String[] temp = new String[cacheStates.size()];
            for(int i=0; i<temp.length; i++)
                temp[i]=cacheStates.get(i).get(0);
            return temp;
        }
        return null;
    }

    private String[] getStateVals(String key) {
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
        rgbItems[0] = ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:leather_chestplate,Count:1,tag:{display:{color:"+getRgbDec()+"}}}"));
        rgbItems[1] = ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:potion,Count:1,tag:{CustomPotionColor:"+getRgbDec()+"}}"));
        rgbItems[2] = ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:filled_map,Count:1,tag:{display:{MapColor:"+getRgbDec()+"}}}"));
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

    private void updateJsonTab() {

        if(!jsonItem.equals(cacheItemFull)) {
            jsonItem = ""+cacheItemFull;

            jsonName = null;
            NbtElement el = BlackMagick.getNbtFromPath(null, "0:/tag/display/Name");
            if(el != null && el.getType() == NbtElement.STRING_TYPE)
                jsonName = (NbtString)el;

            jsonLore = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/display/Lore");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.STRING_TYPE)
                jsonLore = (NbtList)el;

            jsonPages = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/pages");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.STRING_TYPE)
                jsonPages = (NbtList)el;
            createWidgets(8);
            btnTab(8);
        }

    }

    private void updateJsonPreview() {
        updateJsonPreview(null);
    }
    private void updateJsonPreview(String jsonEffect) {
        jsonEffectValid = false;
        if(cacheI.containsKey("jsonBox")) {
            int[] jsonBoxI = cacheI.get("jsonBox");
            jsonPreview = BlackMagick.jsonFromString(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0]).get(jsonBoxI[1]).w).getText()).text();
            jsonPreview2 = BlackMagick.jsonFromString(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0]).get(jsonBoxI[1]).w).getText()).text();
            if(BlackMagick.jsonFromString(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0]).get(jsonBoxI[1]).w).getText()).isValid()) {
                if(jsonEffect != null && BlackMagick.jsonFromString(appendJsonEffect(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0])
                .get(jsonBoxI[1]).w).getText(),jsonEffect)).isValid()) {
                    jsonPreview2 = BlackMagick.jsonFromString(appendJsonEffect(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0]).get(jsonBoxI[1]).w).getText(),jsonEffect)).text();
                    if(jsonEffect.length()>0 && !jsonEffect.equals("{\"text\":\"\"}"))
                        jsonEffectValid = true;
                    jsonEffectFull = appendJsonEffect(((EditBoxWidget)noScrollWidgets.get(jsonBoxI[0]).get(jsonBoxI[1]).w).getText(),jsonEffect);
                }
                if(listCurrentPath.equals("display/Name")) {
                    jsonPreview = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"italic\":true}").text()).append(jsonPreview.copy());
                    jsonPreview2 = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"italic\":true}").text()).append(jsonPreview2.copy());
                }
                else if(listCurrentPath.equals("display/Lore")) {
                    jsonPreview = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"color\":\"dark_purple\",\"italic\":true}").text()).append(jsonPreview.copy());
                    jsonPreview2 = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"color\":\"dark_purple\",\"italic\":true}").text()).append(jsonPreview2.copy());
                }
            }
        }
        if(tab == 10 && noScrollWidgets.size() > 10 && noScrollWidgets.get(10).size() > 1) {
            ((ButtonWidget)noScrollWidgets.get(10).get(1).w).active = (jsonEffectValid && client.player.getAbilities().creativeMode);
            if(jsonEffect != null && jsonEffect.length()>0)
                ((ButtonWidget)noScrollWidgets.get(10).get(1).w).setTooltip(Tooltip.of(Text.of(jsonEffect)));
            else
                ((ButtonWidget)noScrollWidgets.get(10).get(1).w).setTooltip(null);
        }
    }

    private String appendJsonEffect(String jsonBase, String jsonEffect) {
        if(jsonEffectMode == 0 || jsonEffectMode == 1) {
            if(jsonBase.length()==0 || jsonBase.equals("{}") || jsonBase.equals("[]") || jsonBase.equals("[{}]") || jsonBase.equals("{\"text\":\"\"}") || jsonBase.equals("[{\"text\":\"\"}]"))
                return jsonEffect;
            else if(jsonBase.length()>=4 && jsonBase.charAt(0)=='[' && jsonBase.charAt(jsonBase.length()-1)==']'
            && jsonBase.charAt(1)=='{' && jsonBase.charAt(jsonBase.length()-2)=='}')
                return jsonBase.substring(0,jsonBase.length()-1) +","+ jsonEffect +"]";
            else if(jsonBase.length()>=2 && jsonBase.charAt(0)=='{' && jsonBase.charAt(jsonBase.length()-1)=='}')
                return "["+jsonBase+","+jsonEffect+"]";
            return "{\"text\":\"Invalid JSON\",\"color\":\"red\"}";
        }
        else if(jsonEffectMode == 2) {
            if(jsonBase.length()>4 && jsonBase.charAt(0)=='[' && jsonBase.charAt(jsonBase.length()-1)==']'
            && jsonBase.charAt(1)=='{' && jsonBase.charAt(jsonBase.length()-2)=='}')
                return jsonBase.substring(0,jsonBase.length()-2) + jsonEffect +"}]";
            else if(jsonBase.length()>2 && jsonBase.charAt(0)=='{' && jsonBase.charAt(jsonBase.length()-1)=='}')
                return jsonBase.substring(0,jsonBase.length()-1) + jsonEffect +"}";
            return "{\"text\":\"Invalid JSON\",\"color\":\"red\"}";
        }
        else
            return "{\"text\":\"Invalid JSON\",\"color\":\"red\"}";
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
        json2Unsaved = true;

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
            updateJsonPreview(val);
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
            updateJsonPreview(val);
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
            updateJsonPreview(val);
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

    private void updateListTab() {

        if(!listItem.equals(cacheItemFull)) {
            listItem = ""+cacheItemFull;

            int num = 0;
            listEdit[num] = null;
            NbtElement el = BlackMagick.getNbtFromPath(null, "0:/tag/custom_potion_effects");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            num++;
            listEdit[num] = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/effects");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            num++;
            listEdit[num] = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/Fireworks/Explosions");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            num++;
            listEdit[num] = null;
            cacheI.put("listEditBanner",new int[]{num,num});
            el = BlackMagick.getNbtFromPath(null, "0:/tag/BlockEntityTag/Patterns");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            num++;
            listEdit[num] = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/AttributeModifiers");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            num++;
            listEdit[num] = null;
            el = BlackMagick.getNbtFromPath(null, "0:/tag/Enchantments");
            if(el != null && el.getType() == NbtElement.LIST_TYPE && ((NbtList)el).size()>0 && ((NbtList)el).get(0).getType()==NbtElement.COMPOUND_TYPE)
                listEdit[num] = (NbtList)el;

            createWidgets(11);
            btnTab(11);
        }

    }

    private void updateCurrentList() {
        listCurrentValid = false;

        //update btns
        if(listCurrentPath.equals("custom_potion_effects") && cacheI.containsKey("listPotionBtns") && widgets.get(cacheI.get("listPotionBtns")[0])
        .size()>cacheI.get("listPotionBtns")[1]) {
            int[] listPotionBtnsI = cacheI.get("listPotionBtns");
            listCurrent.remove("show_particles");
            listCurrent.remove("show_icon");
            listCurrent.remove("ambient");
            int num = 0;
            String col = "";
            if(listPotionBtns[num]==1) {
                col = "\u00a7a";
                listCurrent.put("show_particles",NbtByte.of(true));
            }
            else if(listPotionBtns[num]==2) {
                col = "\u00a7c";
                listCurrent.put("show_particles",NbtByte.of(false));
            }
            widgets.get(listPotionBtnsI[0]).get(listPotionBtnsI[1]).btns[num].setMessage(Text.of(col+"ShowParticles"));
            num++;
            col = "";
            if(listPotionBtns[num]==1) {
                col = "\u00a7a";
                listCurrent.put("show_icon",NbtByte.of(true));
            }
            else if(listPotionBtns[num]==2) {
                col = "\u00a7c";
                listCurrent.put("show_icon",NbtByte.of(false));
            }
            widgets.get(listPotionBtnsI[0]).get(listPotionBtnsI[1]).btns[num].setMessage(Text.of(col+"ShowIcon"));
            num++;
            col = "";
            if(listPotionBtns[num]==1) {
                col = "\u00a7a";
                listCurrent.put("ambient",NbtByte.of(true));
            }
            else if(listPotionBtns[num]==2) {
                col = "\u00a7c";
                listCurrent.put("ambient",NbtByte.of(false));
            }
            widgets.get(listPotionBtnsI[0]).get(listPotionBtnsI[1]).btns[num].setMessage(Text.of(col+"Ambient"));
        }
        else if(listCurrentPath.equals("Fireworks/Explosions") && cacheI.containsKey("listFireworkBtns") && widgets.get(cacheI.get("listFireworkBtns")[0])
        .size()>cacheI.get("listFireworkBtns")[1]) {
            int[] listFireworkBtnsI = cacheI.get("listFireworkBtns");
            listCurrent.remove("Flicker");
            listCurrent.remove("Trail");
            int num = 0;
            String col = "";
            if(listFireworkBtns[num]==1) {
                col = "\u00a7a";
                listCurrent.put("Flicker",NbtByte.of(true));
            }
            else if(listFireworkBtns[num]==2) {
                col = "\u00a7c";
                listCurrent.put("Flicker",NbtByte.of(false));
            }
            widgets.get(listFireworkBtnsI[0]).get(listFireworkBtnsI[1]).btns[num].setMessage(Text.of(col+"Twinkle"));
            num++;
            col = "";
            if(listFireworkBtns[num]==1) {
                col = "\u00a7a";
                listCurrent.put("Trail",NbtByte.of(true));
            }
            else if(listFireworkBtns[num]==2) {
                col = "\u00a7c";
                listCurrent.put("Trail",NbtByte.of(false));
            }
            widgets.get(listFireworkBtnsI[0]).get(listFireworkBtnsI[1]).btns[num].setMessage(Text.of(col+"Trail"));
        }
        else if(listCurrentPath.equals("BlockEntityTag/Patterns") && cacheI.containsKey("listBannerLbl") && widgets.get(cacheI.get("listBannerLbl")[0])
        .size()>cacheI.get("listBannerLbl")[1]+2+4) {

            listCurrent.remove("Color");
            if(listBannerCol != null && dyeStringToInt(listBannerCol)!=-1)
                listCurrent.putInt("Color",dyeStringToInt(listBannerCol));

            int[] listBannerLblI = cacheI.get("listBannerLbl");
            listCurrent.remove("Pattern");
            if(listBannerPat != null)
                listCurrent.putString("Pattern",listBannerPat);

            for(int row=0; row<2; row++)
                for(int col=0; col<8; col++)
                    widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+row).btns[col].active =
                        !widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+row).patterns[col].equals(listBannerCol);

            for(int row=0; row<5; row++)
                for(int col=0; col<8; col++)
                    widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+2+row).btns[col].active =
                        !widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+2+row).patterns[col].equals(listBannerPat);
        }

        //verify list element
        if(listCurrentPath.equals("AttributeModifiers")) {
            List<Text> textList = new ArrayList<>();
            if(listCurrent.contains("AttributeName",NbtElement.STRING_TYPE) && (listCurrent.contains("Amount",NbtElement.DOUBLE_TYPE) || listCurrent.contains("Amount",NbtElement.INT_TYPE))) {
                NbtList list = new NbtList();
                list.add(listCurrent.copy());
                ItemStack item = selItem==null ? new ItemStack(Items.STONE) : new ItemStack(selItem.getItem());
                item.setSubNbt(listCurrentPath,list);
                textList = item.getTooltip(client.player,TooltipContext.Default.ADVANCED.withCreative());
                listCurrentValid = textList.size()>3;

                if(listCurrent.contains("Operation",NbtElement.INT_TYPE) && ( ((NbtInt)listCurrent.get("Operation")).intValue()<0 || ((NbtInt)listCurrent.get("Operation")).intValue()>2 ))
                    listCurrentValid = false;
            }
        }
        else if(listCurrentPath.equals("custom_potion_effects")) {
            NbtList list = new NbtList();
            list.add(listCurrent.copy());
            List<Text> textList = new ArrayList<>();
            ItemStack item = new ItemStack(Items.POTION);
            item.setSubNbt(listCurrentPath,list);
            Items.POTION.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
            if(textList.size()>0 && !textList.get(0).getString().equals("No Effects"))
                listCurrentValid = true;
        }
        else if(listCurrentPath.equals("effects")) {
            NbtList list = new NbtList();
            list.add(listCurrent.copy());
            List<Text> textList = new ArrayList<>();
            ItemStack item = new ItemStack(Items.SUSPICIOUS_STEW);
            item.setSubNbt(listCurrentPath,list);
            Items.SUSPICIOUS_STEW.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
            if(textList.size()>0 && !textList.get(0).getString().equals("No Effects"))
                listCurrentValid = true;
        }
        else if(listCurrentPath.equals("Enchantments")) {
            List<Text> textList = new ArrayList<>();
            Registries.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(listCurrent.copy())).ifPresent(e -> textList.add(e.getName(EnchantmentHelper.getLevelFromNbt(listCurrent.copy()))));
            if(textList.size()>0)
                listCurrentValid = true;
        }
        else if(listCurrentPath.equals("Fireworks/Explosions")) {
            List<Text> textList = new ArrayList<>();
            ItemStack item = new ItemStack(Items.FIREWORK_STAR);
            item.setSubNbt("Explosion",listCurrent.copy());
            Items.FIREWORK_STAR.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
            if(textList.size()>0)
                listCurrentValid = true;
        }
        else if(listCurrentPath.equals("BlockEntityTag/Patterns")) {
            NbtList list = new NbtList();
            list.add(listCurrent.copy());
            List<Text> textList = new ArrayList<>();
            ItemStack item = new ItemStack(Items.BLACK_BANNER);
            NbtCompound bet = new NbtCompound();
            bet.put("Patterns",list);
            item.setSubNbt("BlockEntityTag",bet);
            Items.BLACK_BANNER.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
            if(textList.size()>0 && listBannerCol != null && listBannerPat != null)
                listCurrentValid = true;

            bannerChangePreview.readNbt((NbtCompound)BlackMagick.elementFromString("{HandItems:[{},{}],ArmorItems:[{},{},{},{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
            if(cacheI.containsKey("listEditBanner")) {
                NbtList prevList = listEdit[cacheI.get("listEditBanner")[0]].copy();
                prevList.set(listCurrentIndex,listCurrent);
                NbtCompound prevBet = new NbtCompound();
                if(selItem.hasNbt() && selItem.getNbt().contains("BlockEntityTag",NbtElement.COMPOUND_TYPE))
                    prevBet = (NbtCompound)selItem.getNbt().get("BlockEntityTag").copy();
                prevBet.put("Patterns",prevList);
                ItemStack prevItem = selItem.copy();
                prevItem.setSubNbt("BlockEntityTag",prevBet);
                if(!bannerShield)
                    bannerChangePreview.readNbt((NbtCompound)BlackMagick.elementFromString("{ArmorItems:[{},{},{},{id:"+prevItem.getItem().toString()+
                        ",Count:1,tag:"+prevItem.getNbt().asString()+"}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
                else
                    bannerChangePreview.readNbt((NbtCompound)BlackMagick.elementFromString("{HandItems:[{id:shield,Count:1,tag:"+prevItem.getNbt().asString()
                        +"},{}],Invisible:1b,Pose:{RightArm:[-90f,-90f,0f]}}"));
            }
        }

        if(cacheI.containsKey("listSaveBtn") && noScrollWidgets.get(cacheI.get("listSaveBtn")[0]).size()>cacheI.get("listSaveBtn")[1]) {
            int[] listSaveBtnI = cacheI.get("listSaveBtn");
            noScrollWidgets.get(listSaveBtnI[0]).get(listSaveBtnI[1]-1).w.active = listCurrentValid || !listUnsaved;
            noScrollWidgets.get(listSaveBtnI[0]).get(listSaveBtnI[1]).w.active = listCurrentValid;
            noScrollWidgets.get(listSaveBtnI[0]).get(listSaveBtnI[1]-1).w.setTooltip(Tooltip.of(Text.of(listCurrent.asString())));
            noScrollWidgets.get(listSaveBtnI[0]).get(listSaveBtnI[1]).w.setTooltip(Tooltip.of(Text.of(listCurrent.asString())));
        }
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
            if(!BlackMagick.formatItem(cacheInv[i]).equals(BlackMagick.formatItem(current))) {
                changed = true;
                cacheInv[i] = current;
            }
        }
        if(cacheInvSlot != client.player.getInventory().selectedSlot) {
            changed = true;
            cacheInvSlot = client.player.getInventory().selectedSlot;
        }

        if(changed) {
            createWidgets(7);
            btnTab(7);
        }
    }

    public int dyeStringToInt(String inp) {
        switch(inp.toLowerCase()) {
            case "white": return 0;
            case "orange": return 1;
            case "magenta": return 2;
            case "light_blue": return 3;
            case "yellow": return 4;
            case "lime": return 5;
            case "pink": return 6;
            case "gray": return 7;
            case "light_gray": return 8;
            case "cyan": return 9;
            case "purple": return 10;
            case "blue": return 11;
            case "brown": return 12;
            case "green": return 13;
            case "red": return 14;
            case "black": return 15;
            default: return -1;
        }
    }
    public String dyeIntStringToString(String inp) {
        switch(inp) {
            case "0": return "white";
            case "1": return "orange";
            case "2": return "magenta";
            case "3": return "light_blue";
            case "4": return "yellow";
            case "5": return "lime";
            case "6": return "pink";
            case "7": return "gray";
            case "8": return "light_gray";
            case "9": return "cyan";
            case "10": return "purple";
            case "11": return "blue";
            case "12": return "brown";
            case "13": return "green";
            case "14": return "red";
            case "15": return "black";
            default: return null;
        }
    }

    private void createWidgets() {
        widgets.clear();
        noScrollWidgets.clear();
        unsavedTxtWidgets.clear();
        for(int i=0; i<tabs.length; i++) {
            widgets.add(new ArrayList<NbtWidget>());
            noScrollWidgets.add(new ArrayList<PosWidget>());
        }

        int tabNum = 0;
        //createBlock general
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("id",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                BlackMagick.setId(inp);
            }, FortytwoEdit.ITEMS,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Count",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                BlackMagick.setCount(inp);
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: {\"text\":\"Mjölnir\"}",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                    BlackMagick.setNbt(null,"display/Name",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"display/Name");
            },new String[] {"{\"text\":\"\"}"},false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("HideFlags",60,"1-Ench     2-Attribute     4-Unbreakable" +
                    "\n8-CanDestroy     16-CanPlaceOn     32-Various" +
                    "\n64-Armor Dye     128-Armor Trims" +
                    "\n255-Everything",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"HideFlags",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"HideFlags");
            },new String[] {"255"},false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Tools / Adventure"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Unbreakable")},new int[]{80},new String[]{null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/Unbreakable")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/Unbreakable").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/Unbreakable").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"Unbreakable");
                }
                else
                    BlackMagick.setNbt(null,"Unbreakable",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Damage",60,"Tools-" +
                    "\nNetherite-2031  Diamond-1561  Iron-250" +
                    "\nStone-131  Wood-59  Gold-32" + "\n" + "\nArmor (H/C/L/B)-" +
                    "\nNetherite-407,592,555,481  Diamond-363,528,495,429" +
                    "\nIron/Chain-165,240,225,195  Gold-77,112,105,91" +
                    "\nLeather-55,80,75,65  Turtle-275  Elytra-432" + "\n" +
                    "\nBow-384  Crossbow-326  Trident-250  Shield-336" +
                    "\nFlint-64  Shears-238  Fishing-64" +
                    "\nCarrotStick-25  FungusStick-100",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"Damage",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"Damage");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("RepairCost",60,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"RepairCost",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"RepairCost");
            },new String[]{"2147483647"},false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Custom Model",80,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"CustomModelData",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"CustomModelData");
            },null,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget(""));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Block:", 40, 2));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("CanPlaceOn"),Text.of("CanDestroy")},new int[]{80,80},new String[]{null,null},null,false,btn -> {
                String inp = widgets.get(i).get(j-1).btn()[0];
                NbtElement inpEl;
                NbtList list;
                if(!inp.equals("")) {
                    inp = "\""+inp+"\"";
                    inpEl = BlackMagick.elementFromString(inp);
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn")!=null &&
                        BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn").getType()==NbtElement.LIST_TYPE) {
                            list = (NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn");
                            if(list.size()==0) {
                                BlackMagick.setNbt(null,"CanPlaceOn/0:",inpEl,NbtElement.STRING_TYPE);
                            }
                            else {
                                if(list.contains(inpEl)) {
                                    int index = list.indexOf(inpEl);
                                    BlackMagick.removeNbt(null,"CanPlaceOn/"+index+":");
                                }
                                else {
                                    int index = list.size();
                                    BlackMagick.setNbt(null,"CanPlaceOn/"+index+":",inpEl);
                                }
                            }
                    }
                    else
                        BlackMagick.setNbt(null,"CanPlaceOn/0:",inpEl,NbtElement.STRING_TYPE);
                }
                else if(inp.equals(""))
                    BlackMagick.removeNbt(null,"CanPlaceOn");
            },btn -> {
                String inp = widgets.get(i).get(j-1).btn()[0];
                NbtElement inpEl;
                NbtList list;
                if(!inp.equals("")) {
                    inp = "\""+inp+"\"";
                    inpEl = BlackMagick.elementFromString(inp);
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy")!=null &&
                        BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy").getType()==NbtElement.LIST_TYPE) {
                            list = (NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy");
                            if(list.size()==0) {
                                BlackMagick.setNbt(null,"CanDestroy/0:",inpEl,NbtElement.STRING_TYPE);
                            }
                            else {
                                if(list.contains(inpEl)) {
                                    int index = list.indexOf(inpEl);
                                    BlackMagick.removeNbt(null,"CanDestroy/"+index+":");
                                }
                                else {
                                    int index = list.size();
                                    BlackMagick.setNbt(null,"CanDestroy/"+index+":",inpEl);
                                }
                            }
                    }
                    else
                        BlackMagick.setNbt(null,"CanDestroy/0:",inpEl,NbtElement.STRING_TYPE);
                }
                else if(inp.equals(""))
                    BlackMagick.removeNbt(null,"CanDestroy");
            }));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Display Color"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            NbtWidget w = new NbtWidget(0, "\u2227", 20, null, btn -> {
                widgets.get(i).get(j).btn();
                boolean found = false;

                NbtElement el = BlackMagick.getNbtFromPath(null,"0:/tag/display/color");
                if(el != null && el.getType() == NbtElement.INT_TYPE)
                    found = true;
                
                if(!found) {
                    el = BlackMagick.getNbtFromPath(null,"0:/tag/CustomPotionColor");
                    if(el != null && el.getType() == NbtElement.INT_TYPE)
                        found = true;
                }
                
                if(!found) {
                    el = BlackMagick.getNbtFromPath(null,"0:/tag/display/MapColor");
                    if(el != null && el.getType() == NbtElement.INT_TYPE)
                        found = true;
                }

                if(found) {
                    int c = ((NbtInt)el).intValue();
                    if(c<0)
                        c=0;
                    if(c>16777215)
                        c=16777215;
                    rgb[0] = (int)Math.floor(c / (256*256));
                    rgb[1] = (int)(Math.floor(c / 256) % 256);
                    rgb[2] = c % 256;
                    updateRgbSliders();
                }
            },true);
            rgbSliders.add(w);
            widgets.get(tabNum).add(w);
        }
        {
            NbtWidget w = new NbtWidget(1, null, 0, null, null,true);
            rgbSliders.add(w);
            widgets.get(tabNum).add(w);
        }
        {
            NbtWidget w = new NbtWidget(2, null, 0, null, null,true);
            rgbSliders.add(w);
            widgets.get(tabNum).add(w);
        }
        updateRgbSliders();
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of(""),Text.of(""),Text.of("")},new int[]{20,20,20},new String[]{null,null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                NbtElement el = BlackMagick.getNbtFromPath(null,"0:/tag/display/color");
                if(el != null && el.getType()==NbtElement.INT_TYPE && ((NbtInt)el).intValue()==getRgbDec())
                    BlackMagick.removeNbt(null,"display/color");
                else
                    BlackMagick.setNbt(null,"display/color",NbtInt.of(getRgbDec()));
            },btn -> {
                widgets.get(i).get(j).btn();
                NbtElement el = BlackMagick.getNbtFromPath(null,"0:/tag/CustomPotionColor");
                if(el != null && el.getType()==NbtElement.INT_TYPE && ((NbtInt)el).intValue()==getRgbDec())
                    BlackMagick.removeNbt(null,"CustomPotionColor");
                else
                    BlackMagick.setNbt(null,"CustomPotionColor",NbtInt.of(getRgbDec()));
            },btn -> {
                widgets.get(i).get(j).btn();
                NbtElement el = BlackMagick.getNbtFromPath(null,"0:/tag/display/MapColor");
                if(el != null && el.getType()==NbtElement.INT_TYPE && ((NbtInt)el).intValue()==getRgbDec())
                    BlackMagick.removeNbt(null,"display/MapColor");
                else
                    BlackMagick.setNbt(null,"display/MapColor",NbtInt.of(getRgbDec()));
            }));
        }


        tabNum++;
        //createBlock block data
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: {\"text\":\"Box\"}",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"BlockEntityTag/CustomName",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"BlockEntityTag/CustomName");
            },new String[] {"{\"text\":\"\"}"},false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Lock",40,"Example: Password",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"BlockEntityTag/Lock",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"BlockEntityTag/Lock");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("BlockState")},new int[]{60,70,69},new String[]{"Key | Value"},1,false,btn -> {
                String[] inps = widgets.get(i).get(j).btn();
                NbtElement inpEl;
                if(!inps[1].equals("") && !inps[0].equals("")) {
                    inps[1] = "\""+inps[1]+"\"";
                    inpEl = BlackMagick.elementFromString(inps[1]);
                    BlackMagick.setNbt(null,"BlockStateTag/"+inps[0],inpEl,NbtElement.STRING_TYPE);
                }
                else if(inps[1].equals(""))
                    BlackMagick.removeNbt(null,"BlockStateTag/"+inps[0]);
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("LootTable",60,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"BlockEntityTag/LootTable",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"BlockEntityTag/LootTable");
            },1,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Banners"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Symbol")},new int[]{40,55,49,55},
                    new String[]{"Char Color | Chars | Base Color"+"\n\n"+BANNER_PRESET_CHARS},new String[][] {DYES,BANNER_CHAR_LIST,DYES},false,btn -> {
                String[] inps = widgets.get(i).get(j).btn();
                CLICK:{
                    int baseColor = dyeStringToInt(inps[2]);
                    int charColor = dyeStringToInt(inps[0]);
                    if(charColor == -1 || baseColor == -1)
                        break CLICK;
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
                        BlackMagick.createBanner(baseColor,charColor,chars,inps[2].toLowerCase(),inps[0].toLowerCase());
                    else if(chars.length()>1) {
                        NbtList Items = new NbtList();
                        while(chars.length()>0) {
                            ItemStack bannerItem = BlackMagick.createBanner(baseColor,charColor,chars.substring(0,1),inps[2].toLowerCase(),inps[0].toLowerCase());
                            if(bannerItem!=null)
                                Items.add((NbtCompound)BlackMagick.getNbtFromPath(bannerItem,"0:"));
                            if(chars.length()==1)
                                chars = "";
                            else
                                chars = chars.substring(1);
                        }
                        ItemStack override = BlackMagick.setId("bundle");
                        override = BlackMagick.removeNbt(override,"");
                        BlackMagick.setNbt(override,"Items",Items);
                    }
                }
            }));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Utilities"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Fill Container"),Text.of("Box to Bundle")},new int[]{100,80},
                    new String[]{"Set count for all container items to 64",null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(client.player.getAbilities().creativeMode) {
                    if(!client.player.getMainHandStack().isEmpty()) {
                        ItemStack item = client.player.getMainHandStack().copy();
                        if(item.hasNbt()) {
                            NbtCompound nbt = item.getNbt().copy();
                            if(nbt.contains("BlockEntityTag") && nbt.get("BlockEntityTag").getType()==NbtElement.COMPOUND_TYPE
                            && ((NbtCompound)nbt.get("BlockEntityTag")).contains("Items")
                            && ((NbtCompound)nbt.get("BlockEntityTag")).get("Items").getType()==NbtElement.LIST_TYPE
                            && ((NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items")).size()>0
                            && ((NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items")).get(0).getType()==NbtElement.COMPOUND_TYPE) {
                                NbtList nbtItems = (NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items");
                                for(int ii=0; ii<nbtItems.size(); ii++) {
                                    item = BlackMagick.setNbt(item,"BlockEntityTag/Items/"+ii+":/Count",NbtInt.of(64));
                                }
                            }
                        }
                    }
                }
            }, btn -> {
                widgets.get(i).get(j).btn();
                if(client.player.getAbilities().creativeMode) {
                    ItemStack item0 = BlackMagick.setNbt(null,"Items",BlackMagick.getNbtFromPath(null,"0:/tag/BlockEntityTag/Items"));
                    NbtCompound tag = new NbtCompound();
                    if(item0.hasNbt())
                        tag = item0.getNbt();
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","bundle");
                    nbt.putInt("Count",1);
                    nbt.put("tag",tag);
                    ItemStack item = ItemStack.fromNbt(nbt);
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Bee Count [0]")},new int[]{100},new String[]{null},null,true,btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = client.player.getMainHandStack().copy();
                widgets.get(i).get(j).btns[0].setMessage(Text.of("Bee Count [0]"));
                if(!item.isEmpty() && item.hasNbt() && (item.getItem().toString().equals("beehive") || item.getItem().toString().equals("bee_nest"))) {
                    NbtCompound nbt = item.getNbt();
                    if(nbt.contains("BlockEntityTag") && nbt.get("BlockEntityTag").getType() == NbtElement.COMPOUND_TYPE) {
                        NbtCompound tag = (NbtCompound)nbt.get("BlockEntityTag");
                        if(tag.contains("Bees") && tag.get("Bees").getType() == NbtElement.LIST_TYPE) {
                            int beeCount = ((NbtList)tag.get("Bees")).size();
                            widgets.get(i).get(j).btns[0].setMessage(Text.of("Bee Count ["+beeCount+"]"));
                        }
                    }
                }
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Bundle Item",80,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                inp = inp.replaceAll("[^a-zA-Z_:]","");
                if(inp.length()>1) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","bundle");
                    nbt.putInt("Count",1);
                    NbtCompound tag = new NbtCompound();
                    NbtCompound innerItem = new NbtCompound();
                    innerItem.putString("id",inp);
                    innerItem.putInt("Count",1);
                    NbtList items = new NbtList();
                    items.add(innerItem);
                    tag.put("Items",items);
                    nbt.put("tag",tag);
                    ItemStack item = ItemStack.fromNbt(nbt);
                    if(item != null && client.player.getAbilities().creativeMode) {
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
            }, FortytwoEdit.ITEMS,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Item Frames"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Rotation",60,"Item Rotation\n0-7",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"EntityTag/ItemRotation",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/ItemRotation");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Item"),Text.of("Invis"),Text.of("Fixed")},new int[]{60,60,60},
                    new String[]{"Set display item as offhand",null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Item")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Item").getType()==NbtElement.COMPOUND_TYPE) {
                    BlackMagick.removeNbt(null,"EntityTag/Item");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Item",BlackMagick.getNbtFromPath(null,"1:"));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/Invisible");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Invisible",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/Fixed");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Fixed",NbtByte.of(true));
            }));
        }

        tabNum++;
        //createBlock misc
        {
            widgets.get(tabNum).add(new NbtWidget("Player Heads"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Owner",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\""+inp+"\"";
                    inpEl = BlackMagick.elementFromString(inp);
                    if(client.player.getMainHandStack().isEmpty())
                        BlackMagick.setNbt(BlackMagick.setId("player_head"),"SkullOwner",inpEl,NbtElement.STRING_TYPE);
                    else
                        BlackMagick.setNbt(null,"SkullOwner",inpEl,NbtElement.STRING_TYPE);
                }
                else {
                    if(client.player.getMainHandStack().isEmpty())
                        BlackMagick.setId("player_head");
                    else
                        BlackMagick.removeNbt(null,"SkullOwner");
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: Eddie",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"SkullOwner/Name",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"SkullOwner/Name");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Skin",40,null,btn -> {
                String id = widgets.get(i).get(j).btn()[0];
                String value = widgets.get(i).get(j).btn()[0];
                if(!client.player.getAbilities().creativeMode)
                    return;
                CLICK:
                if(value.contains("Value:\"") && id.contains("Id:[I;")) {
                    value = value.substring(value.indexOf("Value:\"")+7);
                    id = id.substring(id.indexOf("Id:[I;")+6);
                    if(value.contains("\"") && id.contains("]")) {
                        value = value.substring(0,value.indexOf("\""));
                        value = "\""+value+"\"";
                        int[] idArray = new int[4];
                        try {
                        for(int ii=0; ii<4; ii++) {
                            if(ii!=3 && id.contains(",")) {
                                idArray[ii] = Integer.parseInt(id.substring(0,id.indexOf(",")));
                                id = id.substring(id.indexOf(",")+1);
                            } else if(ii==3 && id.contains("]"))
                                idArray[ii] = Integer.parseInt(id.substring(0,id.indexOf("]")));
                            else
                                break CLICK;
                        }} catch(NumberFormatException e) {
                            break CLICK;
                        }
                        ItemStack item;
                        if(client.player.getMainHandStack().isEmpty()) {
                            item = BlackMagick.setId("player_head");
                            item = BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value));
                            item.getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                        }
                        else {
                            item = client.player.getMainHandStack().copy();
                            item = BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value));
                            item.getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                        }
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
                else if(id.equals("")) {
                    ItemStack stack = BlackMagick.removeNbt(null,"SkullOwner/Id");
                    BlackMagick.removeNbt(stack,"SkullOwner/Properties");
                }
            },null,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Sounds"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Play",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(!inp.trim().equals("")) {
                    String sound = inp.trim();
                    sound = sound.replaceAll("[^a-zA-Z0-9_.:]","");
                    if(!sound.equals("")) {
                        client.player.playSound(SoundEvent.of(new Identifier(sound)), SoundCategory.MASTER, 1, 1);
                    }
                }
            }, FortytwoEdit.SOUNDS,true));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Head Sound")},new int[]{80},new String[]{null},null,false,btn -> {
                String inp = widgets.get(i).get(j-1).btn()[0];
                if(!inp.trim().equals("")) {
                    String sound = inp.trim();
                    sound = sound.replaceAll("[^a-zA-Z0-9_.:]","");
                    if(!sound.equals("")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putString("id","player_head");
                        nbt.putInt("Count",1);
                        NbtCompound tag = new NbtCompound();
                        NbtCompound BET = new NbtCompound();
                        BET.putString("note_block_sound",sound);
                        tag.put("BlockEntityTag",BET);
                        NbtCompound SkullOwner = new NbtCompound();
                        SkullOwner.putString("Name","\u00a77[\u00a7f"+sound+"\u00a77]\u00a7r");
                        SkullOwner.put("Id",BlackMagick.elementFromString("[I;905869354,-357217255,-1221221082,-631504474]"));
                        SkullOwner.put("Properties",BlackMagick.elementFromString("{textures:[{Value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dX"
                        +"JlcyIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2VlYjc3ZDRkMjU3MjRhOWNhZjJjN2NkZ"
                        +"jJkODgzOTliMTQxN2M2YjlmZjUyMTM2NTliNjUzYmU0Mzc2ZTMiDQogICAgfQ0KICB9DQp9\"}]}"));
                        tag.put("SkullOwner",SkullOwner);
                        nbt.put("tag",tag);
                        ItemStack item = ItemStack.fromNbt(nbt);
                        if(item != null && client.player.getAbilities().creativeMode) {
                            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                            client.player.playerScreenHandler.sendContentUpdates();
                        }
                    }
                }
            }));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Books"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("title",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    ItemStack item = BlackMagick.setNbt(null,"title",inpEl,NbtElement.STRING_TYPE);
                    BlackMagick.setNbt(item,"filtered_title",inpEl,NbtElement.STRING_TYPE);
                }
                else {
                    ItemStack item = BlackMagick.removeNbt(null,"title");
                    BlackMagick.removeNbt(item,"filtered_title");
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("author",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inp = "\'"+inp+"\'";
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"author",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"author");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("generation",60,"0-3\nOriginal/ Copy of original/\nCopy of a copy/ Tattered",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"generation",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"generation");
            }, new String[] {"0","1","2","3"},false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Fireworks"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Flight",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"Fireworks/Flight",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"Fireworks/Flight");
            },new String[]{"-128","127"},false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Tropical Fish Bucket"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Variant",60,"[Size, Pattern, Base Color, Pattern Color]" + "\n" +
                    "\nSize-   0-Small   1-Large" + "\n" +
                    "\nPattern-   0-Kob/Flopper   1-Sunstreak/Stripey   2-Snooper/Glitter   3-Dasher/Blockfish   4-Brinely/Betty   5-Spotty/Clayfish" + "\n" +
                    "\nColor-   0-White   1-Orange   2-Magenta   3-Light Blue   4-Yellow   5-Lime   6-Pink   7-Gray" +
                    "   8-Light Gray   9-Cyan   10-Purple   11-Blue   12-Brown   13-Green   14-Red   15-Black",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals("")) {
                    BlackMagick.removeNbt(null,"BucketVariantTag");
                }
                else {
                    if(inp.length()>=2 && inp.charAt(0)=='[' && inp.charAt(inp.length()-1)==']' && inp.charAt(1)!='I')
                        inp = "[I;"+inp.substring(1);
                    NbtElement inpEl = BlackMagick.elementFromString(inp);
                    if(inpEl != null && inpEl.getType() == NbtElement.INT_ARRAY_TYPE && ((NbtIntArray)inpEl).size()==4) {
                        NbtIntArray inpArr = (NbtIntArray)inpEl;
                        int[] vars = {inpArr.get(0).intValue(),inpArr.get(1).intValue(),inpArr.get(2).intValue(),inpArr.get(3).intValue()};
                        int val = vars[0] +(((int)Math.pow(2,8))*vars[1])+(((int)Math.pow(2,16))*vars[2])+(((int)Math.pow(2,24))*vars[3]);
                        if(client.player.getMainHandStack().isEmpty()) {
                            BlackMagick.setNbt(BlackMagick.setId("tropical_fish_bucket"),"BucketVariantTag",NbtInt.of(val));
                        }
                        else
                            BlackMagick.setNbt(null,"BucketVariantTag",NbtInt.of(val));
                    }
                }
            },null,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Maps"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("map #",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(!inp.equals("")) {
                    ItemStack item = null;
                    if(client.player.getAbilities().creativeMode && client.player.getMainHandStack().isEmpty())
                        item = BlackMagick.setId("filled_map");
                    else if(client.player.getAbilities().creativeMode)
                        item = client.player.getMainHandStack().copy();
                    try{
                        BlackMagick.setNbt(item,"map",NbtInt.of(Integer.parseInt(inp)));
                    } catch(NumberFormatException e) {
                        return;
                    }
                }
                else
                    BlackMagick.removeNbt(null,"map");
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Decor",40,"int, string, byte, double, double, double\n\ndecor index, id (Name), type (0-26), x, z, rot (180 is upright)" + "\n" +
                    "\ntype" +
                    "\n0- player marker  1- frame marker  2- red marker" +
                    "\n3- blue marker  4- white x  5- red triangle" +
                    "\n6- large white dot  7- small white dot  8- mansion" +
                    "\n9- monument 10-25 banners  26- red x",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"Decorations");
                else {
                    ItemStack item = client.player.getMainHandStack();
                    String[] inps = new String[6];
                    inp = inp+",,,,,,,";
                    for(int ii=0; ii<6; ii++) {
                        inps[ii]=inp.substring(0,inp.indexOf(","));
                        inp = inp.substring(inp.indexOf(",")+1);
                    }
                    if(!inps[0].equals("") && inps[1].equals("") && inps[2].equals("") && inps[3].equals("")
                    && inps[4].equals("") && inps[5].equals(""))
                        item = BlackMagick.removeNbt(item,"Decorations/"+inps[0]+":");
                    else if(!inps[0].equals("") && !inps[1].equals("") && !inps[2].equals("") && !inps[3].equals("")
                    && !inps[4].equals("") && !inps[5].equals("")) {
                        int ii = 0;
                        Byte t = 0;
                        Double x = 0.0;
                        Double z = 0.0;
                        Double r = 0.0;
                        try {
                            ii = Integer.parseInt(inps[0]);
                            t = Byte.parseByte(inps[2]);
                            x = Double.parseDouble(inps[3]);
                            z = Double.parseDouble(inps[4]);
                            r = Double.parseDouble(inps[5]);
                            if(ii<0)
                                return;
                        } catch(NumberFormatException e) {
                            return;
                        }
                        item = BlackMagick.setNbt(item,"Decorations/"+ii+":/id",NbtString.of(inps[1]));
                        item = BlackMagick.setNbt(item,"Decorations/"+ii+":/type",NbtByte.of(t));
                        item = BlackMagick.setNbt(item,"Decorations/"+ii+":/x",NbtDouble.of(x));
                        item = BlackMagick.setNbt(item,"Decorations/"+ii+":/z",NbtDouble.of(z));
                        item = BlackMagick.setNbt(item,"Decorations/"+ii+":/rot",NbtDouble.of(r));
                    }
                }
            },null,false));
        }

        tabNum++;
        //createBlock nbt
        {
            EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
            noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3,35));
            this.allTxtWidgets.add(w);
            w.setChangeListener(value -> {
                if(value != null && !value.equals("")) {
                    ItemBuilder.this.markUnsaved(w);
                }
                else {
                    ItemBuilder.this.markSaved(w);
                }
            });
        }
        {
            final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
            noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Get"), button -> {
                if(!client.player.getMainHandStack().isEmpty()) {
                    String itemData = BlackMagick.formatItem(client.player.getMainHandStack());
                    ((EditBoxWidget)noScrollWidgets.get(i).get(j-1).w).setText(itemData);
                    ItemBuilder.this.markUnsaved((EditBoxWidget)noScrollWidgets.get(i).get(j-1).w);
                }
                ItemBuilder.this.unsel = true;
            }).dimensions(x+15-3,y+35+22*6+1,60,20).build(),15-3,35+22*6+1));
        }
        {
            final int i = tabNum; final int j = noScrollWidgets.get(tabNum).size();
            ButtonWidget w = ButtonWidget.builder(Text.of("Set"), button -> {
                String inp = ((EditBoxWidget)noScrollWidgets.get(i).get(j-2).w).getText();
                ItemBuilder.this.markSaved((EditBoxWidget)noScrollWidgets.get(i).get(j-2).w);
                ItemBuilder.this.unsel = true;

                if(inp.contains("/") && inp.indexOf("/")==0)
                    inp = inp.substring(1);
                if(inp.contains("give ") && inp.indexOf("give ")==0)
                    inp = inp.substring(5);
                if((inp.contains("@s ") && inp.indexOf("@s ")==0) || (inp.contains("@p ") && inp.indexOf("@p ")==0))
                        inp = inp.substring(3);
                if(inp.contains("{")) {
                    String id = (inp.substring(0,inp.indexOf("{")));
                    String tag = (inp.substring(inp.indexOf("{")));
                    while(tag.length()>2 && tag.charAt(tag.length()-1)!='}') {
                        tag = tag.substring(0,tag.length()-1);
                        if(tag.length()==2)
                            tag = "{}";
                    }
                    if(tag.charAt(tag.length()-1)!='}')
                        tag = "{}";
                    int Count = 1;
                    try {
                        if(tag.length()<(inp.substring(inp.indexOf("{")).length())) {
                            Count = Integer.parseInt(inp.substring(inp.indexOf("{")+tag.length()+1));
                            if(Count<1)
                                Count=1;
                            if(Count>64)
                                Count=64;
                        }
                    } catch(NumberFormatException ex) {}
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id",id);
                    nbt.putInt("Count",Count);
                    if(BlackMagick.elementFromString(tag) != null)
                        nbt.put("tag",BlackMagick.elementFromString(tag));
                    ItemStack item = ItemStack.fromNbt(nbt);
                    if(item != null && client.player.getAbilities().creativeMode) {
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
                else if(inp.contains(" ")) {
                    int Count = 1;
                    try {
                        if(inp.length() > inp.indexOf(" ")+1) {
                            Count = Integer.parseInt(inp.substring(inp.indexOf(" ")+1));
                            if(Count<1)
                                Count=1;
                            if(Count>64)
                                Count=64;
                        }
                    } catch(NumberFormatException ex) {}
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id",inp.substring(0,inp.indexOf(" ")));
                    nbt.putInt("Count",Count);
                    ItemStack item = ItemStack.fromNbt(nbt);
                    if(item != null && client.player.getAbilities().creativeMode) {
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
                else {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id",inp);
                    nbt.putInt("Count",1);
                    ItemStack item = ItemStack.fromNbt(nbt);
                    if(item != null && client.player.getAbilities().creativeMode) {
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
            }).dimensions(x+15-3+5+60,y+35+22*6+1,60,20).build();
            if(!client.player.getAbilities().creativeMode)
                w.active = false;
            noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3+5+60,35+22*6+1));
        }

        tabNum++;
        //createBlock saved items
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
            widgets.get(tabNum).add(new NbtWidget(i));
        refreshSaved();
        if(webItems == null)
            getWebItems();
        setSavedModeTooltip();

        tabNum++;
        //createBlock entity data
        {
            widgets.get(tabNum).add(new NbtWidget("Armor Stands"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("BasePlate"),Text.of("ShowArms"),Text.of("Small")},new int[]{60,60,60},new String[]{null,null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = BlackMagick.setId("armor_stand");
                if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate")!=null &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate").asString().equals("1b")) {
                    BlackMagick.removeNbt(item,"EntityTag/NoBasePlate");
                }
                else
                    BlackMagick.setNbt(item,"EntityTag/NoBasePlate",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = BlackMagick.setId("armor_stand");
                if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms")!=null &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms").asString().equals("1b")) {
                    BlackMagick.removeNbt(item,"EntityTag/ShowArms");
                }
                else
                    BlackMagick.setNbt(item,"EntityTag/ShowArms",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = BlackMagick.setId("armor_stand");
                if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small")!=null &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small").asString().equals("1b")) {
                    BlackMagick.removeNbt(item,"EntityTag/Small");
                }
                else
                    BlackMagick.setNbt(item,"EntityTag/Small",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Marker"),Text.of("Invisible")},new int[]{60,60},new String[]{null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = BlackMagick.setId("armor_stand");
                if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker")!=null &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker").asString().equals("1b")) {
                    BlackMagick.removeNbt(item,"EntityTag/Marker");
                }
                else
                    BlackMagick.setNbt(item,"EntityTag/Marker",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                ItemStack item = BlackMagick.setId("armor_stand");
                if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible")!=null &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible").asString().equals("1b")) {
                    BlackMagick.removeNbt(item,"EntityTag/Invisible");
                }
                else
                    BlackMagick.setNbt(item,"EntityTag/Invisible",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Disabled Slots",80,"Disable All - 16191",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"EntityTag/DisabledSlots",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/DisabledSlots");
            }, new String[] {"16191"},false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("All Entities"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Entity",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                ItemStack item;
                if(client.player.getMainHandStack().isEmpty()) {
                    item = BlackMagick.setId("ender_dragon_spawn_egg");
                    item = BlackMagick.setNbt(item,"display/Name",BlackMagick.elementFromString("'{\"text\":\"Custom Spawn Egg\",\"italic\":false}'"));
                }
                else
                    item = client.player.getMainHandStack().copy();
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\""+inp+"\"");
                    BlackMagick.setNbt(item,"EntityTag/id",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(item,"EntityTag/id");
            }, FortytwoEdit.ENTITIES,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: {\"text\":\"Monster\"}",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                    BlackMagick.setNbt(null,"EntityTag/CustomName",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/CustomName");
            },new String[] {"{\"text\":\"\"}"},false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("ShowName"),Text.of("NoGravity"),Text.of("Glowing")},new int[]{60,60,60},new String[]{null,null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/CustomNameVisible");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/CustomNameVisible",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/NoGravity");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/NoGravity",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/Glowing");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Glowing",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("VisualFire"),Text.of("Silent"),Text.of("NoAI")},new int[]{60,60,60},new String[]{null,null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/HasVisualFire");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/HasVisualFire",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/Silent");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Silent",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/NoAI");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/NoAI",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Invulnerable"),Text.of("Persistence")},new int[]{80,80},new String[]{null,null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/Invulnerable");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/Invulnerable",NbtByte.of(true));
            },btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired").asString().equals("1b")) {
                    BlackMagick.removeNbt(null,"EntityTag/PersistenceRequired");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/PersistenceRequired",NbtByte.of(true));
            }));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Items",40,"0 Mainhand    1 Offhand" + "\n2 Boots    3 Leggings" + "\n4 Chestplate    5 Helmet" +
                    "\n" + "\n6 Armor    7 Armor/Mainhand" + "\n" + "\n8 Remove Armor    9 Remove Hands",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                ItemStack stack = null;
                switch(inp) {
                    case "0":
                        if(client.player.getOffHandStack().isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/HandItems/0:",new NbtCompound());
                        else {
                            ItemStack item = client.player.getOffHandStack().copy();
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/HandItems/0:",nbt);
                        }
                        break;
                    case "1":
                        if(client.player.getOffHandStack().isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/HandItems/1:",new NbtCompound());
                        else {
                            ItemStack item = client.player.getOffHandStack().copy();
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/HandItems/1:",nbt);
                        }
                        break;
                    case "2":
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",nbt);
                            }
                        }
                        break;
                    case "3":
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",nbt);
                            }
                        }
                        break;
                    case "4":
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",nbt);
                            }
                        }
                        break;
                    case "5":
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",nbt);
                            }
                        }
                        break;
                    case "6":
                        {//boots
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",nbt);
                                }
                            }
                        }
                        {//leggings
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/1:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/1:",nbt);
                                }
                            }
                        }
                        {//chestplate
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/2:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/2:",nbt);
                                }
                            }
                        }
                        {//helmet
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/3:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/3:",nbt);
                                }
                            }
                        }
                        break;
                    case "7":
                        {//mainhand
                            if(client.player.getOffHandStack().isEmpty())
                                stack = BlackMagick.setNbt(null,"EntityTag/HandItems/0:",new NbtCompound());
                            else {
                                ItemStack item = client.player.getOffHandStack().copy();
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                stack = BlackMagick.setNbt(null,"EntityTag/HandItems/0:",nbt);
                            }
                        }
                        {//boots
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/0:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/0:",nbt);
                                }
                            }
                        }
                        {//leggings
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/1:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/1:",nbt);
                                }
                            }
                        }
                        {//chestplate
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/2:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/2:",nbt);
                                }
                            }
                        }
                        {//helmet
                            if(client.player.getArmorItems()!=null) {
                                Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                                ItemStack item = null;
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                if(items.hasNext())
                                    item = items.next();
                                else item = null;
                                if(item == null || item.isEmpty())
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/3:",new NbtCompound());
                                else {
                                    NbtCompound nbt = new NbtCompound();
                                    nbt.putInt("Count",item.getCount());
                                    nbt.putString("id",item.getItem().toString());
                                    if(item.hasNbt())
                                        nbt.put("tag",item.getNbt());
                                    stack = BlackMagick.setNbt(stack,"EntityTag/ArmorItems/3:",nbt);
                                }
                            }
                        }
                        break;
                    case "8":
                        BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                        break;
                    case "9":
                        BlackMagick.removeNbt(null,"EntityTag/HandItems");
                        break;
                    case "":
                        stack = BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                        BlackMagick.removeNbt(stack,"EntityTag/HandItems");
                        break;
                    default: break;
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Pos",40,"[0.0d, 0.0d, 0.0d]",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Pos");
                else {
                    NbtElement inpEl = BlackMagick.elementFromString(inp);
                    if(inpEl != null && inpEl.getType() == NbtElement.LIST_TYPE && ((NbtList)inpEl).size()==3 && ((NbtList)inpEl).get(0).getType()==NbtElement.DOUBLE_TYPE) {
                        BlackMagick.setNbt(null,"EntityTag/Pos",(NbtList)inpEl);
                    }
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Motion",40,"[0.0d, 0.0d, 0.0d]",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Motion");
                else {
                    NbtElement inpEl = BlackMagick.elementFromString(inp);
                    if(inpEl != null && inpEl.getType() == NbtElement.LIST_TYPE && ((NbtList)inpEl).size()==3 && ((NbtList)inpEl).get(0).getType()==NbtElement.DOUBLE_TYPE) {
                        BlackMagick.setNbt(null,"EntityTag/Motion",(NbtList)inpEl);
                    }
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Rotation",60,"[0.0f, 0.0f] (Yaw,Pitch)" + "\n" + "\nYaw 0 - 360 (South=0,West=90)" + "\nPitch -90 - 90 (Down=90)",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Rotation");
                else {
                    NbtElement inpEl = BlackMagick.elementFromString(inp);
                    if(inpEl != null && inpEl.getType() == NbtElement.LIST_TYPE && ((NbtList)inpEl).size()==2 && ((NbtList)inpEl).get(0).getType()==NbtElement.FLOAT_TYPE) {
                        BlackMagick.setNbt(null,"EntityTag/Rotation",(NbtList)inpEl);
                    }
                }
            },null,false));
        }
        {
            widgets.get(tabNum).add(new NbtWidget("End Crystals"));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget("Beam Target",80,"[x,y,z]",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/BeamTarget");
                else {
                    if(inp.length()>=2 && inp.charAt(0)=='[' && inp.charAt(inp.length()-1)==']' && inp.charAt(1)!='I')
                        inp = "[I;"+inp.substring(1);
                    NbtElement inpEl = BlackMagick.elementFromString(inp);
                    if(inpEl != null && inpEl.getType() == NbtElement.INT_ARRAY_TYPE && ((NbtIntArray)inpEl).size()==3) {
                        NbtIntArray inpArr = (NbtIntArray)inpEl;
                        BlackMagick.setNbt(null,"EntityTag/BeamTarget",
                            BlackMagick.elementFromString("{X:"+inpArr.get(0)+",Y:"+inpArr.get(1)+",Z:"+inpArr.get(2)+"}"));
                    }
                }
            },null,false));
        }
        {
            final int i = tabNum; final int j = widgets.get(tabNum).size();
            widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Show Bottom")},new int[]{80},new String[]{null},null,false,btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom").asString().equals("0b")) {
                    BlackMagick.removeNbt(null,"EntityTag/ShowBottom");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/ShowBottom",NbtByte.of(false));
            }));
        }

        tabNum++;
        //createBlock armor stand pose
        {
            ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2611"), button -> this.btnSetPose()).dimensions(x+15-3, y+35+1,20,20).build();
            newButton.setTooltip(Tooltip.of(Text.of("Set Pose")));
            if(!client.player.getAbilities().creativeMode)
                newButton.active = false;
            setPoseButton = newButton;
            noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1));
        }
        {
            ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2227"), button -> this.btnGetPose()).dimensions(x+15-3, y+35+1+22,20,20).build();
            newButton.setTooltip(Tooltip.of(Text.of("Get from item")));
            noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1+22));
        }
        {
            ButtonWidget newButton = ButtonWidget.builder(Text.of("\u2612"), button -> this.btnResetPose()).dimensions(x+15-3, y+35+1+22*2,20,20).build();
            newButton.setTooltip(Tooltip.of(Text.of("Clear All")));
            noScrollWidgets.get(tabNum).add(new PosWidget(newButton,15-3,35+1+22*2));
        }
        {
            NbtWidget newButton = new NbtWidget(0,"Head");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(0,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(0,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(0,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newButton = new NbtWidget(1,"Right Arm");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(1,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(1,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(1,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newButton = new NbtWidget(2,"Left Arm");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(2,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(2,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(2,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newButton = new NbtWidget(3,"Right Leg");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(3,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(3,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(3,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newButton = new NbtWidget(4,"Left Leg");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(4,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(4,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(4,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newButton = new NbtWidget(5,"Body");
            sliderBtns.add(newButton);
            widgets.get(tabNum).add(newButton);
        }
        {
            NbtWidget newSlider = new NbtWidget(5,0);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(5,1);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        {
            NbtWidget newSlider = new NbtWidget(5,2);
            sliders.add(newSlider);
            widgets.get(tabNum).add(newSlider);
        }
        btnResetPose();

        tabNum++;
        //see createWidgets(7)

        tabNum++;
        //see createWidgets(8)

        tabNum++;
        //see createWidgets(9)

        tabNum++;
        //see createWidgets(10)

        tabNum++;
        //see createWidgets(11)

        tabNum++;
        //see createWidgets(12)

    }

    public void createWidgets(int tabNum) {
        if(!pauseSaveScroll && tabWidget != null) {
            tabScroll[tab] = tabWidget.getScrollAmount();
            pauseSaveScroll = true;
        }
        widgets.get(tabNum).clear();
        noScrollWidgets.get(tabNum).clear();
        if(tabNum == 7) {   //createBlock inventory
            {
                widgets.get(tabNum).add(new NbtWidget("Inventory"));
            }
            {
                for(int i=0; i<5; i++)
                widgets.get(tabNum).add(new NbtWidgetInvRow(i));    //TODO highlight hotbar slot, armor symbols
            }
            for(int i=0; i<2; i++) {
                ItemStack current = i==0 ? client.player.getMainHandStack() : client.player.getOffHandStack();
                if(current != null && !current.isEmpty()) {
                    String id = current.getItem().toString();
                    int[] size = BlackMagick.containerSize(id);
                    
                    NbtList itemsList = null;
                    if(current.hasNbt() && current.getNbt().contains("BlockEntityTag",NbtElement.COMPOUND_TYPE)) {
                        NbtCompound bet = (NbtCompound)(current.getNbt().get("BlockEntityTag").copy());
                        if(bet.contains("Items",NbtElement.LIST_TYPE) && ((NbtList)bet.get("Items")).size()>0
                        && ((NbtList)bet.get("Items")).get(0).getType()==NbtElement.COMPOUND_TYPE) {
                            itemsList = ((NbtList)bet.get("Items"));
                        }
                    }

                    if(id.contains("armor_stand")) {
                        {
                            widgets.get(tabNum).add(new NbtWidget(i==0 ? "Selected Armor Stand" : "Offhand Armor Stand"));
                        }
                        //TODO
                    }

                    if((size[0]>0 && size[1]>0) || itemsList != null) {
                        {
                            widgets.get(tabNum).add(new NbtWidget(i==0 ? "Selected Container" : "Offhand Container"));
                        }
                        //TODO
                    }
                }
            }
            //TODO ender chest with timestamp, clear after joining world (or save cached by worldname/ip/etc)
            //TODO saved hotbars (read or readwrite?)
            //TODO enable buttons (only creative?), swap mode or copy mode
        }
        else if(tabNum == 8) {   //createBlock json
            boolean noItem = client.player.getMainHandStack().isEmpty();
            {
                widgets.get(tabNum).add(new NbtWidget("Name",new ItemStack(Items.NAME_TAG),50));
            }
            if(jsonName != null) {
                widgets.get(tabNum).add(new NbtWidget(jsonName.copy(),"display/Name",0,0));
            }
            else {
                widgets.get(tabNum).add(new NbtWidget(null,"display/Name",NbtString.of("{\"text\":\"\"}"),noItem));
            }
            {
                widgets.get(tabNum).add(new NbtWidget("Lore",new ItemStack(Items.PAPER),50));
            }
            if(jsonLore != null)
                for(int k=0; k<jsonLore.size(); k++) {
                    final int index = k;
                    widgets.get(tabNum).add(new NbtWidget(((NbtString)jsonLore.get(k)).copy(),"display/Lore",index,jsonLore.size()-1));
                }
            {
                widgets.get(tabNum).add(new NbtWidget(jsonLore,"display/Lore",NbtString.of("{\"text\":\"\"}"),noItem));
            }
            if(jsonItem.startsWith("written_book")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("pages",new ItemStack(Items.WRITTEN_BOOK),50));
                }
                if(jsonPages != null)
                    for(int k=0; k<jsonPages.size(); k++) {
                        final int index = k;
                        widgets.get(tabNum).add(new NbtWidget(((NbtString)jsonPages.get(k)).copy(),"pages",index,jsonPages.size()-1));
                    }
                {
                    widgets.get(tabNum).add(new NbtWidget(jsonPages,"pages",NbtString.of("{\"text\":\"\"}"),noItem));
                }
            }
        }
        else if(tabNum == 9) {  //createBlock jsonEdit
            jsonUnsaved = false;
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Cancel"), btn -> this.btnTab(8)).dimensions(x+5,y+5,40,20).build(),5,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Delete"), btn -> {
                    String delPath = listCurrentPath + "";
                    if(!delPath.equals("display/Name"))
                        delPath += "/"+listCurrentIndex+":";
                    BlackMagick.removeNbt(null,delPath);
                    this.btnTab(8);
                }).dimensions(x+120-40-10,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,120-40-10,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Clone"), btn -> {
                    ItemStack temp = BlackMagick.cloneListElement(null,listCurrentPath,listCurrentIndex);
                    if(temp != null && jsonUnsaved) {
                        BlackMagick.setNbt(temp,listCurrentPath+"/"+(listCurrentIndex+1)+":",NbtString.of(((EditBoxWidget)noScrollWidgets.get(cacheI.get("jsonBox")[0])
                            .get(cacheI.get("jsonBox")[1]).w).getText()));
                    }
                    this.btnTab(8);
                }).dimensions(x+120+10,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode || listCurrentPath.equals("display/Name"))
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,120+10,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Save"), btn -> {
                    String setPath = listCurrentPath + "";
                    if(!setPath.equals("display/Name"))
                        setPath += "/"+listCurrentIndex+":";
                    BlackMagick.setNbt(null,setPath,NbtString.of(((EditBoxWidget)noScrollWidgets.get(cacheI.get("jsonBox")[0])
                        .get(cacheI.get("jsonBox")[1]).w).getText()));
                    this.btnTab(8);
                }).dimensions(x+240-5-40,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,240-5-40,5));
            }
            {  
                TextFieldWidget w = new TextFieldWidget(this.textRenderer,x-15,y+5+1,15,18,Text.of(""));
                w.setEditable(false);
                w.setText("\u00a7");
                w.setTooltip(Tooltip.of(FortytwoEdit.formatTooltip));
                noScrollWidgets.get(tabNum).add(new PosWidget(w,-15,5+1));
            }
            {
                cacheI.put("jsonBox",new int[]{tabNum,noScrollWidgets.get(tabNum).size()});
                EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
                w.setText(jsonCurrent.asString());
                w.setChangeListener(value -> {
                    jsonUnsaved = true;
                    updateJsonPreview();
                });
                noScrollWidgets.get(tabNum).add(new PosWidget(w,15-3,35));
                this.allTxtWidgets.add(w);
                updateJsonPreview();
            }
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Add Text"), button -> {
                    jsonEffectMode = 1;
                    createWidgets(10);
                    this.btnTab(10);
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3,y+35+22*6+1,60,20).build(),15-3,35+22*6+1));
            }
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Add Effect"), button -> {
                    jsonEffectMode = 0;
                    createWidgets(10);
                    this.btnTab(10);
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3+60+5,y+35+22*6+1,60,20).build(),15-3+60+5,35+22*6+1));
            }
            if(listCurrentPath.equals("pages")) {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Set Event"), button -> {
                    jsonEffectMode = 2;
                    createWidgets(10);
                    this.btnTab(10);
                    ItemBuilder.this.unsel = true;
                }).dimensions(x+15-3+60+5+60+5,y+35+22*6+1,60,20).build(),15-3+60+5+60+5,35+22*6+1));
            }
        }
        else if(tabNum == 10) { //createBlock jsonEffects
            json2Unsaved = false;
            updateJsonPreview();
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Cancel"), btn -> {
                    json2Unsaved = false;
                    this.btnTab(9);
                    updateJsonPreview();
                }).dimensions(x+5,y+5,40,20).build(),5,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Add"), btn -> {
                    if(jsonEffectValid) {
                        String setPath = listCurrentPath + "";
                        if(!setPath.equals("display/Name"))
                            setPath += "/"+listCurrentIndex+":";
                        BlackMagick.setNbt(null,setPath,NbtString.of(jsonEffectFull));
                        this.btnTab(8);
                    }
                }).dimensions(x+240-5-40,y+5,40,20).build();
                w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,240-5-40,5));
            }
            {  
                TextFieldWidget w = new TextFieldWidget(this.textRenderer,x-15,y+5+1,15,18,Text.of(""));
                w.setEditable(false);
                w.setText("\u00a7");
                w.setTooltip(Tooltip.of(FortytwoEdit.formatTooltip));
                noScrollWidgets.get(tabNum).add(new PosWidget(w,-15,5+1));
            }
            if(jsonEffectMode == 0) {
                widgets.get(tabNum).add(new NbtWidget("Gradient"));
            }
            else if(jsonEffectMode == 1) {
                widgets.get(tabNum).add(new NbtWidget("Text Element"));
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

                            if(!currentTxt.contains(w)) {
                                resetSuggs();
                                currentTxt.add(w);
                                suggs = new TextSuggestor(client, w, textRenderer);
                                if(suggestions != null && suggestions.length > 0)
                                    suggs.setSuggestions(suggestions);
                            }
                            else{
                                if(suggs != null)
                                    suggs.refresh();
                                else {
                                    resetSuggs();
                                    suggs = new TextSuggestor(client, w, textRenderer);
                                    if(suggestions != null && suggestions.length > 0)
                                        suggs.setSuggestions(suggestions);
                                }
                            }
                        }
                        else
                            resetSuggs();
                    }
                });
                widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                this.allTxtWidgets.add(w);
            }
            if(jsonEffectMode == 0 || jsonEffectMode == 1) {
                cacheI.put("jsonEffectBtns",new int[]{tabNum,widgets.get(tabNum).size()});
                widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("\u00a7ll"),Text.of("\u00a7oo"),Text.of("\u00a7nn"),Text.of("\u00a7mm"),
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
                widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("[Radial]")},
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

                        String[] suggestions = new String[]{"reset","aqua","black","blue","dark_aqua","dark_blue","dark_gray","dark_green","dark_purple","dark_red",
                            "gold","gray","green","light_purple","red","white","yellow"};

                        if(!currentTxt.contains(w2)) {
                            resetSuggs();
                            currentTxt.add(w2);
                            suggs = new TextSuggestor(client, w2, textRenderer);
                            if(suggestions != null && suggestions.length > 0)
                                suggs.setSuggestions(suggestions);
                        }
                        else{
                            if(suggs != null)
                                suggs.refresh();
                            else {
                                resetSuggs();
                                suggs = new TextSuggestor(client, w2, textRenderer);
                                if(suggestions != null && suggestions.length > 0)
                                    suggs.setSuggestions(suggestions);
                            }
                        }

                        updateJsonEffect();
                    }
                    else
                        resetSuggs();
                });
                widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0), new PosWidget(w2,15+80+5,0)}));
            }
            if(jsonEffectMode == 0) {
                {
                    RgbSlider w = new RgbSlider(3);
                    RgbSlider w2 = new RgbSlider(6);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                }
                {
                    RgbSlider w = new RgbSlider(4);
                    RgbSlider w2 = new RgbSlider(7);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                }
                {
                    RgbSlider w = new RgbSlider(5);
                    RgbSlider w2 = new RgbSlider(8);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                }
                {
                    TextFieldWidget w = new TextFieldWidget(textRenderer, x+15, 0, 60, 20, Text.of(""));
                    TextFieldWidget w2 = new TextFieldWidget(textRenderer, x+15+100+5, 0, 60, 20, Text.of(""));
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0),new PosWidget(w2,15+100+5,0)}));
                }
            }
            else if(jsonEffectMode == 1) {
                {
                    RgbSlider w = new RgbSlider(0);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                }
                {
                    RgbSlider w = new RgbSlider(1);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                }
                {
                    RgbSlider w = new RgbSlider(2);
                    widgets.get(tabNum).add(new NbtWidget(new PosWidget[]{new PosWidget(w,15,0)}));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("[Text]")},
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
                    widgets.get(tabNum).add(new NbtWidget("Translations Only"));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("Params:",60,3));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("Fallback:",60,4));
                }
            }
            else if(jsonEffectMode == 2) {
                {
                    cacheI.put("clickEventLbl",new int[]{tabNum,widgets.get(tabNum).size()});
                    widgets.get(tabNum).add(new NbtWidget("clickEvent"));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("action:",60,5));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("value:",60,6));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("hoverEvent"));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("action:",60,7));
                }
                {
                    widgets.get(tabNum).add(new NbtWidget("contents:",60,8));
                }
            }
            updateJsonEffectBtns();
            updateRgbSliders();
        }
        else if(tabNum == 11) { //createBlock attributes/enchants
            boolean noItem = client.player.getMainHandStack().isEmpty();
            int num = 0;
            if(listItem.startsWith("potion") || listItem.startsWith("splash_potion") || listItem.startsWith("lingering_potion") || listItem.startsWith("tipped_arrow")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Potion Effects",listItems[num],50));
                }
                if(listEdit[num] != null)
                    for(int k=0; k<listEdit[num].size(); k++) {
                        final int index = k;
                        widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"custom_potion_effects",index,listEdit[num].size()-1));
                    }
                {
                    widgets.get(tabNum).add(new NbtWidget(listEdit[num],"custom_potion_effects",BlackMagick.elementFromString("{}"),noItem));
                }
            }
            num++;
            if(listItem.startsWith("suspicious_stew")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Stew Effects",listItems[num],50));
                }
                if(listEdit[num] != null)
                    for(int k=0; k<listEdit[num].size(); k++) {
                        final int index = k;
                        widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"effects",index,listEdit[num].size()-1));
                    }
                {
                    widgets.get(tabNum).add(new NbtWidget(listEdit[num],"effects",BlackMagick.elementFromString("{}"),noItem));
                }
            }
            num++;
            if(listItem.startsWith("firework")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Fireworks",listItems[num],50));
                }
                if(listEdit[num] != null)
                    for(int k=0; k<listEdit[num].size(); k++) {
                        final int index = k;
                        widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"Fireworks/Explosions",index,listEdit[num].size()-1));
                    }
                {
                    widgets.get(tabNum).add(new NbtWidget(listEdit[num],"Fireworks/Explosions",BlackMagick.elementFromString("{}"),noItem));
                }
            }
            num++;
            if((listItem.contains("{") && listItem.substring(0,listItem.indexOf("{")).contains("banner")) || (!listItem.contains("{") && listItem.contains("banner"))
            || listItem.startsWith("shield")) {
                bannerShield = listItem.startsWith("shield");
                {
                    widgets.get(tabNum).add(new NbtWidget("Banner Patterns",listItems[num],50));
                }
                if(listEdit[num] != null)
                    for(int k=0; k<listEdit[num].size(); k++) {
                        final int index = k;
                        widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"BlockEntityTag/Patterns",index,listEdit[num].size()-1));
                    }
                {
                    widgets.get(tabNum).add(new NbtWidget(listEdit[num],"BlockEntityTag/Patterns",BlackMagick.elementFromString("{}"),noItem));
                }
            }
            num++;
            {
                widgets.get(tabNum).add(new NbtWidget("Attributes",listItems[num],50));
            }
            if(listEdit[num] != null)
                for(int k=0; k<listEdit[num].size(); k++) {
                    final int index = k;
                    widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"AttributeModifiers",index,listEdit[num].size()-1));
                }
            {
                widgets.get(tabNum).add(new NbtWidget(listEdit[num],"AttributeModifiers",BlackMagick.elementFromString("{UUID:"+FortytwoEdit.randomUUID()+"}"),noItem));
            }
            num++;
            {
                widgets.get(tabNum).add(new NbtWidget("Enchantments",listItems[num],50));
            }
            if(listEdit[num] != null)
                for(int k=0; k<listEdit[num].size(); k++) {
                    final int index = k;
                    widgets.get(tabNum).add(new NbtWidget(((NbtCompound)listEdit[num].get(k)).copy(),"Enchantments",index,listEdit[num].size()-1));
                }
            {
                widgets.get(tabNum).add(new NbtWidget(listEdit[num],"Enchantments",BlackMagick.elementFromString("{}"),noItem));
            }
            num++;
        }
        else if(tabNum == 12) { //createBlock listEdit
            listUnsaved = false;
            {
                noScrollWidgets.get(tabNum).add(new PosWidget(ButtonWidget.builder(Text.of("Cancel"), btn -> this.btnTab(11)).dimensions(x+5,y+5,40,20).build(),5,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Delete"), btn -> {
                    BlackMagick.removeNbt(null,listCurrentPath+"/"+listCurrentIndex+":");
                    this.btnTab(11);
                }).dimensions(x+120-40-10,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,120-40-10,5));
            }
            {
                ButtonWidget w = ButtonWidget.builder(Text.of("Clone"), btn -> {
                    ItemStack temp = BlackMagick.cloneListElement(null,listCurrentPath,listCurrentIndex);
                    if(temp != null && listUnsaved) {
                        BlackMagick.setNbt(temp,listCurrentPath+"/"+(listCurrentIndex+1)+":",listCurrent);
                    }
                    this.btnTab(11);
                }).dimensions(x+120+10,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,120+10,5));
            }
            {
                cacheI.put("listSaveBtn",new int[]{tabNum,noScrollWidgets.get(tabNum).size()});
                ButtonWidget w = ButtonWidget.builder(Text.of("Save"), btn -> {
                    BlackMagick.setNbt(null,listCurrentPath+"/"+listCurrentIndex+":",listCurrent);
                    this.btnTab(11);
                }).dimensions(x+240-5-40,y+5,40,20).build();
                if(!client.player.getAbilities().creativeMode)
                    w.active = false;
                noScrollWidgets.get(tabNum).add(new PosWidget(w,240-5-40,5));
            }
            if(listCurrentPath.equals("AttributeModifiers")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Edit Attribute"));
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "AttributeName";
                    widgets.get(tabNum).add(new NbtWidget("Name:", 40, new String[]{"generic.movement_speed","generic.attack_damage","generic.attack_speed",
                    "generic.attack_knockback","generic.max_health","generic.max_absorption","generic.knockback_resistance","generic.armor","generic.armor_toughness",
                    "generic.luck"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            listCurrent.put(val,NbtString.of(value));
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "Amount";
                    widgets.get(tabNum).add(new NbtWidget("Amount:", 40, null, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && (el.getType() == NbtElement.DOUBLE_TYPE || el.getType() == NbtElement.INT_TYPE)) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "Operation";
                    widgets.get(tabNum).add(new NbtWidget("Operation:", 60, new String[]{"0","1","2"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && el.getType() == NbtElement.INT_TYPE && ((NbtInt)el).intValue()>=0 && ((NbtInt)el).intValue()<=2) {
                                listCurrent.put(val,(NbtInt)el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "Slot";
                    widgets.get(tabNum).add(new NbtWidget("Slot:", 40, new String[]{"mainhand","offhand","head","chest","legs","feet"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            listCurrent.put(val,NbtString.of(value));
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
            }
            else if(listCurrentPath.equals("custom_potion_effects")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Edit Effect"));
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "id";
                    widgets.get(tabNum).add(new NbtWidget("id:", 40, FortytwoEdit.EFFECTS, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            listCurrent.put(val,NbtString.of(value));
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "duration";
                    widgets.get(tabNum).add(new NbtWidget("duration:", 60, new String[]{"-1"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && el.getType() == NbtElement.INT_TYPE) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "amplifier";
                    widgets.get(tabNum).add(new NbtWidget("amplifier:", 60, null, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && (el.getType() == NbtElement.INT_TYPE || el.getType() == NbtElement.BYTE_TYPE)) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    cacheI.put("listPotionBtns",new int[]{tabNum,widgets.get(tabNum).size()});
                    widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("ShowParticles"),Text.of("ShowIcon"),Text.of("Ambient")},new int[]{80,60,60},
                    new String[]{"none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r",
                    "none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r"},null,true,btn -> {
                        unsel = true;
                        listPotionBtns[0]++;
                        if(listPotionBtns[0]>2)
                            listPotionBtns[0]=0;
                        updateCurrentList();
                    },btn -> {
                        unsel = true;
                        listPotionBtns[1]++;
                        if(listPotionBtns[1]>2)
                            listPotionBtns[1]=0;
                        updateCurrentList();
                    },btn -> {
                        unsel = true;
                        listPotionBtns[2]++;
                        if(listPotionBtns[2]>2)
                            listPotionBtns[2]=0;
                        updateCurrentList();
                    }));
                }
                String el = "show_particles";
                int num = 0;
                if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("0") || listCurrent.get(el).asString().equals("0b")))
                    listPotionBtns[num] = 2;
                else if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("1") || listCurrent.get(el).asString().equals("1b")))
                    listPotionBtns[num] = 1;
                else
                    listPotionBtns[num] = 0;
                num++;
                el = "show_icon";
                if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("0") || listCurrent.get(el).asString().equals("0b")))
                    listPotionBtns[num] = 2;
                else if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("1") || listCurrent.get(el).asString().equals("1b")))
                    listPotionBtns[num] = 1;
                else
                    listPotionBtns[num] = 0;
                num++;
                el = "ambient";
                if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("0") || listCurrent.get(el).asString().equals("0b")))
                    listPotionBtns[num] = 2;
                else if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("1") || listCurrent.get(el).asString().equals("1b")))
                    listPotionBtns[num] = 1;
                else
                    listPotionBtns[num] = 0;
            }
            else if(listCurrentPath.equals("effects")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Edit Effect"));
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "id";
                    widgets.get(tabNum).add(new NbtWidget("id:", 40, FortytwoEdit.EFFECTS, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            listCurrent.put(val,NbtString.of(value));
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "duration";
                    widgets.get(tabNum).add(new NbtWidget("duration:", 60, new String[]{"-1"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && el.getType() == NbtElement.INT_TYPE) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
            }
            else if(listCurrentPath.equals("Enchantments")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Edit Enchantment"));
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "id";
                    widgets.get(tabNum).add(new NbtWidget("Enchant:", 60, FortytwoEdit.ENCHANTS, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            listCurrent.put(val,NbtString.of(value));
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "lvl";
                    widgets.get(tabNum).add(new NbtWidget("Level:", 40, null, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && (el.getType() == NbtElement.SHORT_TYPE || el.getType() == NbtElement.INT_TYPE)) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
            }
            else if(listCurrentPath.equals("Fireworks/Explosions")) {
                {
                    widgets.get(tabNum).add(new NbtWidget("Edit Firework Explosion"));
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "Colors";
                    widgets.get(tabNum).add(new NbtWidget("Colors:", 40, new String[]{"[I;0]"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && el.getType() == NbtElement.INT_ARRAY_TYPE) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "FadeColors";
                    widgets.get(tabNum).add(new NbtWidget("Fade:", 40, new String[]{"[I;0]"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(value);
                            if(el != null && el.getType() == NbtElement.INT_ARRAY_TYPE) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    final int i = tabNum; final int j = widgets.get(tabNum).size();
                    String val = "Type";
                    widgets.get(tabNum).add(new NbtWidget("Type:", 40, new String[]{"0 - Small Ball","1 - Large Ball","2 - Star-shaped",
                    "3 - Creeper-shaped","4 - Burst"}, value -> {
                        if(listCurrent.contains(val)) {
                            listCurrent.remove(val);
                            widgets.get(i).get(j).txts[0].setEditableColor(0xFF5555);
                        }
                        if(value != null && value.length()>0) {
                            NbtElement el = BlackMagick.elementFromString(""+value.charAt(0));
                            if(el != null && (el.getType() == NbtElement.INT_TYPE || el.getType() == NbtElement.BYTE_TYPE)) {
                                listCurrent.put(val,el);
                                widgets.get(i).get(j).txts[0].setEditableColor(0xFFFFFF);
                            }
                        }
                    }));
                    if(listCurrent.contains(val))
                        widgets.get(i).get(j).txts[0].setText(listCurrent.get(val).asString());
                }
                {
                    cacheI.put("listFireworkBtns",new int[]{tabNum,widgets.get(tabNum).size()});
                    widgets.get(tabNum).add(new NbtWidget(new Text[]{Text.of("Twinkle"),Text.of("Trail")},new int[]{60,40},
                    new String[]{"none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r","none | \u00a7atrue\u00a7r | \u00a7cfalse\u00a7r"},null,true,btn -> {
                        unsel = true;
                        listFireworkBtns[0]++;
                        if(listFireworkBtns[0]>2)
                            listFireworkBtns[0]=0;
                        updateCurrentList();
                    },btn -> {
                        unsel = true;
                        listFireworkBtns[1]++;
                        if(listFireworkBtns[1]>2)
                            listFireworkBtns[1]=0;
                        updateCurrentList();
                    }));
                }
                String el = "Flicker";
                int num = 0;
                if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("0") || listCurrent.get(el).asString().equals("0b")))
                    listFireworkBtns[num] = 2;
                else if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("1") || listCurrent.get(el).asString().equals("1b")))
                    listFireworkBtns[num] = 1;
                else
                    listFireworkBtns[num] = 0;
                num++;
                el = "Trail";
                if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("0") || listCurrent.get(el).asString().equals("0b")))
                    listFireworkBtns[num] = 2;
                else if((listCurrent.contains(el,NbtElement.BYTE_TYPE) || listCurrent.contains(el,NbtElement.INT_TYPE))
                        && (listCurrent.get(el).asString().equals("1") || listCurrent.get(el).asString().equals("1b")))
                    listFireworkBtns[num] = 1;
                else
                    listFireworkBtns[num] = 0;
            }
            else if(listCurrentPath.equals("BlockEntityTag/Patterns")) {
                {
                    cacheI.put("listBannerLbl",new int[]{tabNum,widgets.get(tabNum).size()});
                    int row = 0;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"white","light_gray","gray","black","brown","red","orange","yellow"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"lime","green","cyan","light_blue","blue","purple","magenta","pink"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"bl","br","tl","tr","bs","ts","ls","rs"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"cs","ms","drs","dls","ss","cr","sc","bt"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"tt","bts","tts","ld","rd","lud","rud","mc"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"mr","vh","hh","vhr","hhb","bo","cbo","gra"}));
                    row++;
                    widgets.get(tabNum).add(new NbtWidget(row,new String[]{"gru","bri","cre","sku","flo","moj","glb","pig"}));
                }
                listBannerCol = "white";
                if(listCurrent.contains("Color",NbtElement.INT_TYPE))
                    listBannerCol = dyeIntStringToString(""+((NbtInt)listCurrent.get("Color")).intValue());
                listBannerPat = null;
                if(listCurrent.contains("Pattern",NbtElement.STRING_TYPE))
                    listBannerPat = ((NbtString)listCurrent.get("Pattern")).asString();
                int[] listBannerLblI = cacheI.get("listBannerLbl");
                for(int row=0; row<2; row++)
                    for(int col=0; col<8; col++)
                        widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+row).btns[col].active =
                            !widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+row).patterns[col].equals(listBannerCol);
                for(int row=0; row<5; row++)
                    for(int col=0; col<8; col++)
                        widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+2+row).btns[col].active =
                            !widgets.get(listBannerLblI[0]).get(listBannerLblI[1]+2+row).patterns[col].equals(listBannerPat);
            }
            updateCurrentList();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class TabWidget
    extends ElementListWidget<AbstractWidget> { //modified from net.minecraft.client.gui.screen.world.EditGameRulesScreen$RuleListWidget
        public TabWidget(final int tab) {
            super(ItemBuilder.this.client, ItemBuilder.this.width-30, ItemBuilder.this.backgroundHeight-32-5, ItemBuilder.this.y+32, tab == 4 ? 20 : 22);
            
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
    }

    protected class NbtWidget
    extends AbstractWidget {

        protected final List<ClickableWidget> children;
        protected ButtonWidget[] btns;
        protected int[] btnX;
        protected int[] btnY = null;
        protected TextFieldWidget[] txts;
        protected int[] txtX;
        protected String lbl;
        protected boolean lblCentered;
        protected ItemStack[] savedStacks;
        protected int savedStacksMode = -1;
        protected int savedRow = -1;
        protected PoseSlider poseSlider;
        protected RgbSlider rgbSlider;
        protected int rgbNum;
        protected ItemStack item = null;
        protected int itemXoff = 0;
        public String[] patterns;
        protected PosWidget[] wids;

        //default
        public NbtWidget() {
            super();
            this.children = Lists.newArrayList();
            setup();
        }

        //btn(size) txt
        public NbtWidget(String name, int size, String tooltip, PressAction onPress, String[] suggestions, boolean survival) {
            super();
            this.children = Lists.newArrayList();
            setup();

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
                    ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    ItemBuilder.this.markSaved(this.txts[0]);
                }
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                    if(suggestions != null && suggestions.length > 0)
                        suggs.setSuggestions(suggestions);
                }
                else{
                    if(suggs != null)
                        suggs.refresh();
                    else {
                        resetSuggs();
                        suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                        if(suggestions != null && suggestions.length > 0)
                            suggs.setSuggestions(suggestions);
                    }
                }
            });
            this.txts[0].setMaxLength(131072);
            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
            }
        }

        //btn(size) txt [custom suggs]
        public NbtWidget(String name, int size, String tooltip, PressAction onPress, int suggsNum, boolean survival) {
            super();
            this.children = Lists.newArrayList();
            setup();

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
                    ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    ItemBuilder.this.markSaved(this.txts[0]);
                }
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                    if(suggsNum == 1)
                        FortytwoEdit.setCommandSuggs("loot spawn ~ ~ ~ loot ", suggs, new String[][]{FortytwoEdit.LOOT});
                    else if(suggsNum == 2)
                        FortytwoEdit.setCommandSuggs("execute if block ~ ~ ~ ", suggs, new String[][]{FortytwoEdit.BLOCKS,FortytwoEdit.BLOCKTAGS});
                    else
                        resetSuggs();
                }
                else{
                    if(suggs != null)
                        suggs.refresh();
                    else {
                        resetSuggs();
                        suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                        if(suggsNum == 1)
                            FortytwoEdit.setCommandSuggs("loot spawn ~ ~ ~ loot ", suggs, new String[][]{FortytwoEdit.LOOT});
                        else if(suggsNum == 2)
                            FortytwoEdit.setCommandSuggs("execute if block ~ ~ ~ ", suggs, new String[][]{FortytwoEdit.BLOCKS,FortytwoEdit.BLOCKTAGS});
                        else
                            resetSuggs();
                    }
                }
            });
            this.txts[0].setMaxLength(131072);
            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
            }
        }

        //centered lbl
        public NbtWidget(String label) {
            super();
            this.children = Lists.newArrayList();
            setup();

            lbl = label;
            lblCentered = true;
        }

        //ItemStack centered lbl
        public NbtWidget(String label, ItemStack item, int itemX) {
            super();
            this.children = Lists.newArrayList();
            setup();

            lbl = label;
            lblCentered = true;
            this.item = item;
            this.itemXoff = itemX;
        }

        //lbl(size) txt [custom suggs]
        public NbtWidget(String name, int size, int suggsNum) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+size};
            this.txts[0].setChangedListener(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    ItemBuilder.this.markSaved(this.txts[0]);
                }
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                    switch(suggsNum) {
                        case 1: FortytwoEdit.setCommandSuggs("loot spawn ~ ~ ~ loot ", suggs, new String[][]{FortytwoEdit.LOOT}); break;
                        case 2: FortytwoEdit.setCommandSuggs("execute if block ~ ~ ~ ", suggs, new String[][]{FortytwoEdit.BLOCKS,FortytwoEdit.BLOCKTAGS}); break;
                        case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{\"text\":\"\"}]"}); break;
                        case 4: resetSuggs(); break;
                        case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                        case 6: resetSuggs(); break;
                        case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                        case 8: suggs.setSuggestions(new String[]
                                {"{\"text\":\"\"}","{\"id\":\"air\"}","{\"id\":\"bundle\",\"tag\":\"{Items:[{id:\\\"stone\\\",Count:1}]}\"}"}); break;
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
                            case 1: FortytwoEdit.setCommandSuggs("loot spawn ~ ~ ~ loot ", suggs, new String[][]{FortytwoEdit.LOOT}); break;
                            case 2: FortytwoEdit.setCommandSuggs("execute if block ~ ~ ~ ", suggs, new String[][]{FortytwoEdit.BLOCKS,FortytwoEdit.BLOCKTAGS}); break;
                            case 3: suggs.setSuggestions(new String[]{"[\"\"]","[{\"text\":\"\"}]"}); break;
                            case 4: resetSuggs(); break;
                            case 5: suggs.setSuggestions(new String[]{"change_page","copy_to_clipboard","run_command","open_url","open_file"}); break;
                            case 6: resetSuggs(); break;
                            case 7: suggs.setSuggestions(new String[]{"show_text","show_item"}); break;
                            case 8: suggs.setSuggestions(new String[]
                                {"{\"text\":\"\"}","{\"id\":\"air\"}","{\"id\":\"bundle\",\"tag\":\"{Items:[{id:\\\"stone\\\",Count:1}]}\"}"}); break;
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
            lblCentered = false;
        }

        //btn...(sizes) txt...(sizes)
        public NbtWidget(Text[] names, int[] sizes, String[] tooltips, String[][] suggestions, boolean survival, PressAction... onPressActions) {
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
                            ItemBuilder.this.markUnsaved(this.txts[ii]);
                        }
                        else {
                            this.txts[ii].setEditableColor(LABEL_COLOR);
                            ItemBuilder.this.markSaved(this.txts[ii]);
                        }
                        if(!currentTxt.contains(this.txts[ii])) {
                            resetSuggs();
                            currentTxt.add(this.txts[ii]);
                            suggs = new TextSuggestor(client, this.txts[ii], textRenderer);
                            if(suggestions != null && suggestions.length > ii && suggestions[ii] != null)
                                suggs.setSuggestions(suggestions[ii]);
                        }
                        else{
                            if(suggs != null)
                                suggs.refresh();
                            else {
                                resetSuggs();
                                suggs = new TextSuggestor(client, this.txts[ii], textRenderer);
                                if(suggestions != null && suggestions.length > ii && suggestions[ii] != null)
                                    suggs.setSuggestions(suggestions[ii]);
                            }
                        }
                    });
                    this.txts[i].setMaxLength(131072);

                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }
        }

        //btn...(sizes) txt...(sizes) [custom suggs]
        public NbtWidget(Text[] names, int[] sizes, String[] tooltips, int suggsNum, boolean survival, PressAction... onPressActions) {
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
                            ItemBuilder.this.markUnsaved(this.txts[ii]);
                        }
                        else {
                            this.txts[ii].setEditableColor(LABEL_COLOR);
                            ItemBuilder.this.markSaved(this.txts[ii]);
                        }
                        if(!currentTxt.contains(this.txts[ii])) {
                            resetSuggs();
                            currentTxt.add(this.txts[ii]);
                            suggs = new TextSuggestor(client, this.txts[ii], textRenderer);
                            if(cacheStates.size()>0) {
                                if(suggsNum == 1) {
                                    if(ii==0)
                                        suggs.setSuggestions(getStatesArr());
                                    else if(ii==1 && getStateVals(this.txts[0].getText()) != null)
                                        suggs.setSuggestions(getStateVals(this.txts[0].getText()));
                                }
                            }
                            else
                                resetSuggs();
                        }
                        else{
                            if(suggs != null)
                                suggs.refresh();
                            else {
                                resetSuggs();
                                suggs = new TextSuggestor(client, this.txts[ii], textRenderer);
                                if(cacheStates.size()>0) {
                                    if(suggsNum == 1) {
                                        if(ii==0)
                                            suggs.setSuggestions(getStatesArr());
                                        else if(ii==1 && getStateVals(this.txts[0].getText()) != null)
                                            suggs.setSuggestions(getStateVals(this.txts[0].getText()));
                                    }
                                }
                                else
                                    resetSuggs();
                            }
                        }
                    });
                    this.txts[i].setMaxLength(131072);

                    this.children.add(this.txts[i]);
                    ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
                }
            }
        }

        //saved row (9 btns)
        public NbtWidget(int row) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.savedRow = row;
            this.savedStacks = new ItemStack[9];
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
                            if(client.player.getMainHandStack().isEmpty()) {
                                nbt.putString("id","air");
                                nbt.putInt("Count",0);
                            }
                            else {
                                nbt = (NbtCompound)BlackMagick.getNbtFromPath(null,"0:");
                            }
                            savedItems.set(index,nbt);
                            FortytwoEdit.setSavedItems(savedItems);
                            refreshSaved();
                        }
                        else {
                            if(client.player.getAbilities().creativeMode) {
                                ItemStack item = ItemStack.fromNbt((NbtCompound)(savedItems.get(index)));
                                if(item!=null && !item.isEmpty()) {
                                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                                    client.player.playerScreenHandler.sendContentUpdates();
                                }
                            }
                        }
                    }
                    else {
                        if(client.player.getAbilities().creativeMode) {
                            ItemStack item = ItemStack.fromNbt((NbtCompound)(webItems.get(index)));
                            if(item!=null && !item.isEmpty()) {
                                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                                client.player.playerScreenHandler.sendContentUpdates();
                            }
                        }
                    }
                }).dimensions(currentX,5,20,20).build();
                currentX += 20;
                this.btns[i].active = false;

                this.children.add(this.btns[i]);
            }
        }

        //banner row (8 btns), row 0/1 for dye, else for pattern
        public NbtWidget(int row, String[] patterns) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.patterns = patterns;
            this.savedStacks = new ItemStack[patterns.length];
            this.savedStacksMode = 0;
            if(row>=2) {
                if(!bannerShield)
                    for(int i=0; i<patterns.length; i++)
                        this.savedStacks[i] = ItemStack.fromNbt((NbtCompound)BlackMagick
                            .elementFromString("{id:white_banner,Count:1,tag:{BlockEntityTag:{Patterns:[{Color:14,Pattern:"+patterns[i]+"}]}}}"));
                else
                    for(int i=0; i<patterns.length; i++)
                        this.savedStacks[i] = ItemStack.fromNbt((NbtCompound)BlackMagick
                            .elementFromString("{id:shield,Count:1,tag:{BlockEntityTag:{Base:0,Patterns:[{Color:14,Pattern:"+patterns[i]+"}]}}}"));
            }
            else {
                for(int i=0; i<patterns.length; i++)
                    this.savedStacks[i] = ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:"+patterns[i]+"_dye,Count:1}"));
            }
            this.btns = new ButtonWidget[patterns.length];
            this.btnX = new int[patterns.length];
            int currentX = 10+30;
            for(int i=0; i<btns.length; i++) {
                this.btnX[i] = currentX;
                final int col = i;
                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    ItemBuilder.this.unsel = true;
                    if(row>=2)
                        ItemBuilder.this.listBannerPat = patterns[col];
                    else
                        ItemBuilder.this.listBannerCol = patterns[col];
                    updateCurrentList();
                }).dimensions(currentX,5,20,20).build();
                currentX += 20;

                if(row>=2) {
                    List<Text> textList = new ArrayList<>();
                    Items.WHITE_BANNER.appendTooltip(ItemStack.fromNbt((NbtCompound)BlackMagick
                        .elementFromString("{id:white_banner,Count:1,tag:{BlockEntityTag:{Patterns:[{Color:14,Pattern:"+patterns[i]+"}]}}}")),
                        null,textList,TooltipContext.Default.ADVANCED.withCreative());
                    if(textList.size()>0)
                        this.btns[i].setTooltip(Tooltip.of(Text.of(textList.get(0).getString().replaceAll("Red ",""))));
                }
                else
                    this.btns[i].setTooltip(Tooltip.of(savedStacks[i].getName()));

                this.children.add(this.btns[i]);
            }
        }

        //pose slider
        public NbtWidget(int part, int num) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.poseSlider = new PoseSlider(part,num);

            this.children.add(this.poseSlider);
        }

        //pose slider btn
        public NbtWidget(int part, String name) {
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
                unsavedPose = true;
            }).dimensions(ItemBuilder.this.x+15,5,60,20).build()};
            this.btnX = new int[]{100};

            this.children.add(this.btns[0]);
        }

        //btn? rgbSlider
        public NbtWidget(int rgbNum, String name, int size, String tooltip, PressAction onPress, boolean survival) {
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

        //json btn
        public NbtWidget(NbtString nbt, String path, int index, int maxIndex) {
            super();
            this.children = Lists.newArrayList();
            setup();

            if(path.equals("display/Name")) {
                this.btns = new ButtonWidget[1];
                this.btnX = new int[]{15};
            }
            else {
                this.btns = new ButtonWidget[3];
                this.btnX = new int[]{15,15+190,15+190};
                this.btnY = new int[]{0,0,10};
            }

            Text preview = BlackMagick.jsonFromString(nbt.asString()).text();
            if(BlackMagick.jsonFromString(nbt.asString()).isValid()) {
                if(path.equals("display/Name"))
                    preview = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"italic\":true}").text()).append(preview.copy());
                else if(path.equals("display/Lore"))
                    preview = ((MutableText)BlackMagick.jsonFromString("{\"text\":\"\",\"color\":\"dark_purple\",\"italic\":true}").text()).append(preview.copy());
                else if(path.equals("pages")) {
                    MutableText builder = (MutableText)Text.of("");
                    ((StringVisitable)preview).visit((style, message) -> {
                        builder.append(((MutableText)Text.of(message.replaceAll("\n"," "))).setStyle(style));
                        return Optional.empty();
                    }, Style.EMPTY);
                    preview = builder;
                }
            }
            final NbtString copyList = nbt.copy();
            this.btns[0] = ButtonWidget.builder(preview, btn -> {
                ItemBuilder.this.unsel = true;
                jsonCurrent = copyList.copy();
                listCurrentPath = path;
                listCurrentIndex = index;
                createWidgets(9);
                btnTab(9);
            }).dimensions(this.btnX[0],5,190,20).build();
            this.btns[0].setTooltip(Tooltip.of(Text.of(nbt.asString())));
            if(!path.equals("display/Name")) {
                this.btns[1] = ButtonWidget.builder(Text.of("\u2227"), btn -> {
                    BlackMagick.moveListElement(null,path,index,true);
                    ItemBuilder.this.unsel = true;
                }).dimensions(this.btnX[1],5+this.btnY[1],20,10).build();
                if(index==0 || !client.player.getAbilities().creativeMode)
                    this.btns[1].active = false;
                this.btns[2] = ButtonWidget.builder(Text.of("\u2228"), btn -> {
                    BlackMagick.moveListElement(null,path,index,false);
                    ItemBuilder.this.unsel = true;
                }).dimensions(this.btnX[2],5+this.btnY[2],20,10).build();
                if(index==maxIndex || !client.player.getAbilities().creativeMode)
                    this.btns[2].active = false;
            }

            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
        }

        //list create new btn
        public NbtWidget(NbtList nbt, String path, NbtElement newEntry, boolean disabledBtns) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.btns = new ButtonWidget[1];
            this.btnX = new int[]{15};
            
            int index = 0;
            if(nbt != null)
                index = nbt.size();
            final int j = index;

            if(path.equals("display/Lore")) {
                this.btns = new ButtonWidget[3];
                this.btnX = new int[]{15,15+20+5,15+20+5+100+5};
            }

            this.btns[0] = ButtonWidget.builder(Text.of("+"), btn -> {
                if(path.equals("display/Name"))
                    BlackMagick.setNbt(null,path,NbtString.of("{\"text\":\"\"}"));
                else
                    BlackMagick.setNbt(null,path+"/"+j+":",newEntry);
                ItemBuilder.this.unsel = true;
            }).dimensions(this.btnX[0],5,20,20).build();

            if(path.equals("display/Lore")) {

                this.btns[1] = ButtonWidget.builder(Text.of("BaphomethLabs"), btn -> {
                    if(client.player.getAbilities().creativeMode && !client.player.getMainHandStack().isEmpty()) {
                        NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/display/Lore");
                        if(element!=null && element.getType()==NbtElement.LIST_TYPE) {
                            NbtList jsonList = (NbtList)element;
                            if(jsonList.size()>0 && jsonList.get(0).getType()==NbtElement.STRING_TYPE) {
                                if(jsonList.size()>=2 && jsonList.get(jsonList.size()-1).asString().equals("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}") &&
                                        jsonList.get(jsonList.size()-2).asString().equals("{\"text\":\"\"}")) {
                                    if(jsonList.size()==2)
                                        BlackMagick.removeNbt(null,"display/Lore");
                                    else {
                                        jsonList.remove(jsonList.size()-1);
                                        jsonList.remove(jsonList.size()-1);
                                        BlackMagick.setNbt(null,"display/Lore",jsonList);
                                    }
                                }
                                else {
                                    jsonList.add(NbtString.of("{\"text\":\"\"}"));
                                    jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                                    BlackMagick.setNbt(null,"display/Lore",jsonList);
                                }
                            }
                            else {
                                jsonList = new NbtList();
                                jsonList.add(NbtString.of("{\"text\":\"\"}"));
                                jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                                BlackMagick.setNbt(null,"display/Lore",jsonList);
                            }
                        }
                        else {
                            NbtList jsonList = new NbtList();
                            jsonList.add(NbtString.of("{\"text\":\"\"}"));
                            jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                            BlackMagick.setNbt(null,"display/Lore",jsonList);
                        }
                    }
                    ItemBuilder.this.unsel = true;
                }).dimensions(this.btnX[1],5,100,20).build();

                this.btns[2] = ButtonWidget.builder(Text.of("Bottling Co"), btn -> {
                    if(client.player.getAbilities().creativeMode && !client.player.getMainHandStack().isEmpty()) {
                        NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/display/Lore");
                        if(element!=null && element.getType()==NbtElement.LIST_TYPE) {
                            NbtList jsonList = (NbtList)element;
                            if(jsonList.size()>0 && jsonList.get(0).getType()==NbtElement.STRING_TYPE) {
                                if(jsonList.size()>=2 && jsonList.get(jsonList.size()-1).asString().equals("{\"text\":\"Bottled by BaphomethLabs\",\"color\":\"gold\"}") &&
                                        jsonList.get(jsonList.size()-2).asString().equals("{\"text\":\"\"}")) {
                                    if(jsonList.size()==2)
                                        BlackMagick.removeNbt(null,"display/Lore");
                                    else {
                                        jsonList.remove(jsonList.size()-1);
                                        jsonList.remove(jsonList.size()-1);
                                        BlackMagick.setNbt(null,"display/Lore",jsonList);
                                    }
                                }
                                else {
                                    jsonList.add(NbtString.of("{\"text\":\"\"}"));
                                    jsonList.add(NbtString.of("{\"text\":\"Bottled by BaphomethLabs\",\"color\":\"gold\"}"));
                                    BlackMagick.setNbt(null,"display/Lore",jsonList);
                                }
                            }
                            else {
                                jsonList = new NbtList();
                                jsonList.add(NbtString.of("{\"text\":\"\"}"));
                                jsonList.add(NbtString.of("{\"text\":\"Bottled by BaphomethLabs\",\"color\":\"gold\"}"));
                                BlackMagick.setNbt(null,"display/Lore",jsonList);
                            }
                        }
                        else {
                            NbtList jsonList = new NbtList();
                            jsonList.add(NbtString.of("{\"text\":\"\"}"));
                            jsonList.add(NbtString.of("{\"text\":\"Bottled by BaphomethLabs\",\"color\":\"gold\"}"));
                            BlackMagick.setNbt(null,"display/Lore",jsonList);
                        }
                    }
                    ItemBuilder.this.unsel = true;
                }).dimensions(this.btnX[2],5,80,20).build();

            }

            for(int i=0; i<btns.length; i++) {
                if(!client.player.getAbilities().creativeMode || disabledBtns)
                    this.btns[i].active = false;
                this.children.add(this.btns[i]);
            }
        }

        //list btn
        public NbtWidget(NbtCompound nbt, String path, int index, int maxIndex) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.btns = new ButtonWidget[3];
            this.btnX = new int[]{15,15+190,15+190};
            this.btnY = new int[]{0,0,10};

            List<Text> preview = new ArrayList<>();
            preview.add(Text.of(nbt.asString()));

            if(path.equals("AttributeModifiers")) {
                List<Text> textList = new ArrayList<>();
                boolean addedPreview = false;
                if(nbt.contains("AttributeName",NbtElement.STRING_TYPE) && (nbt.contains("Amount",NbtElement.DOUBLE_TYPE) || nbt.contains("Amount",NbtElement.INT_TYPE))) {
                    NbtList list = new NbtList();
                    list.add(nbt.copy());
                    ItemStack item = selItem==null ? new ItemStack(Items.STONE) : new ItemStack(selItem.getItem());
                    item.setSubNbt(path,list);
                    textList = item.getTooltip(client.player,TooltipContext.Default.ADVANCED.withCreative());
                    addedPreview = true;
                    if(textList.size()>6)
                        preview.add(((MutableText)Text.of("Any Slot: ")).append(textList.get(3)).setStyle(Style.EMPTY));
                    else if(textList.size()>3)
                        preview.add(((MutableText)textList.get(2)).append(Text.of(" ")).append(textList.get(3)).setStyle(Style.EMPTY));
                    else
                        addedPreview = false;

                    if(nbt.contains("Operation",NbtElement.INT_TYPE) && ( ((NbtInt)nbt.get("Operation")).intValue()<0 || ((NbtInt)nbt.get("Operation")).intValue()>2 ))
                        addedPreview = false;
                }
                if(!addedPreview)
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Attribute\",\"color\":\"red\"}").text());
            }
            else if(path.equals("Enchantments")) {
                List<Text> textList = new ArrayList<>();
                Registries.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(nbt)).ifPresent(e -> textList.add(e.getName(EnchantmentHelper.getLevelFromNbt(nbt))));
                if(textList.size()==0)
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Enchantment\",\"color\":\"red\"}").text());
                else
                    preview.add(((MutableText)textList.get(0)).setStyle(Style.EMPTY));
            }
            else if(path.equals("custom_potion_effects")) {
                NbtList list = new NbtList();
                list.add(nbt.copy());
                List<Text> textList = new ArrayList<>();
                ItemStack item = new ItemStack(Items.POTION);
                item.setSubNbt(path,list);
                Items.POTION.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
                if(textList.size()==0 || (textList.size()>0 && textList.get(0).getString().equals("No Effects")))
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Effect\",\"color\":\"red\"}").text());
                else
                    preview.add(((MutableText)textList.get(0)).setStyle(Style.EMPTY));
            }
            else if(path.equals("effects")) {
                NbtList list = new NbtList();
                list.add(nbt.copy());
                List<Text> textList = new ArrayList<>();
                ItemStack item = new ItemStack(Items.SUSPICIOUS_STEW);
                item.setSubNbt(path,list);
                Items.SUSPICIOUS_STEW.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
                if(textList.size()>0 && !textList.get(0).getString().equals("No Effects"))
                    preview.add(((MutableText)textList.get(0)).setStyle(Style.EMPTY));
                else
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Effect\",\"color\":\"red\"}").text());
            }
            else if(path.equals("Fireworks/Explosions")) {
                List<Text> textList = new ArrayList<>();
                ItemStack item = new ItemStack(Items.FIREWORK_STAR);
                NbtCompound el = nbt.copy();
                if(!(el.contains("Colors",NbtElement.INT_ARRAY_TYPE) && ((NbtIntArray)el.get("Colors")).size()>0))
                    el.put("Colors",BlackMagick.elementFromString("[I;0]"));
                if(el.contains("FadeColors"))
                    el.remove("FadeColors");
                item.setSubNbt("Explosion",el);
                Items.FIREWORK_STAR.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
                if(textList.size()==0)
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Explosion\",\"color\":\"red\"}").text());
                else {
                    MutableText btnLbl = (MutableText)textList.get(0);
                    if(textList.size()==3) {
                        btnLbl.append(Text.of(" ("));
                        btnLbl.append(textList.get(2));
                        btnLbl.append(Text.of(")"));
                    }
                    else if(textList.size()==4) {
                        btnLbl.append(Text.of(" ("));
                        btnLbl.append(textList.get(2));
                        btnLbl.append(Text.of("/"));
                        btnLbl.append(textList.get(3));
                        btnLbl.append(Text.of(")"));
                    }
                    btnLbl.append(Text.of(" ["));
                    btnLbl.append(textList.get(1));
                    btnLbl.append(Text.of("]"));
                    preview.add(btnLbl.setStyle(Style.EMPTY));
                }
            }
            else if(path.equals("BlockEntityTag/Patterns")) {
                NbtList list = new NbtList();
                list.add(nbt.copy());
                List<Text> textList = new ArrayList<>();
                ItemStack item = new ItemStack(Items.BLACK_BANNER);
                NbtCompound bet = new NbtCompound();
                bet.put("Patterns",list);
                item.setSubNbt("BlockEntityTag",bet);
                Items.BLACK_BANNER.appendTooltip(item,null,textList,TooltipContext.Default.ADVANCED.withCreative());
                if(textList.size()>0)
                    preview.add(((MutableText)textList.get(0)).setStyle(Style.EMPTY));
                else
                    preview.add(BlackMagick.jsonFromString("{\"text\":\"Invalid Pattern\",\"color\":\"red\"}").text());
            }

            final NbtCompound copyList = nbt.copy();
            this.btns[0] = ButtonWidget.builder(preview.get(preview.size()-1), btn -> {
                ItemBuilder.this.unsel = true;
                listCurrent = copyList.copy();
                listCurrentPath = path;
                listCurrentIndex = index;
                createWidgets(12);
                btnTab(12);
            }).dimensions(this.btnX[0],5,190,20).build();
            this.btns[0].setTooltip(Tooltip.of(Text.of(nbt.asString())));

            this.btns[1] = ButtonWidget.builder(Text.of("\u2227"), btn -> {
                BlackMagick.moveListElement(null,path,index,true);
                ItemBuilder.this.unsel = true;
            }).dimensions(this.btnX[1],5+this.btnY[1],20,10).build();
            if(index==0 || !client.player.getAbilities().creativeMode)
                this.btns[1].active = false;

            this.btns[2] = ButtonWidget.builder(Text.of("\u2228"), btn -> {
                BlackMagick.moveListElement(null,path,index,false);
                ItemBuilder.this.unsel = true;
            }).dimensions(this.btnX[2],5+this.btnY[2],20,10).build();
            if(index==maxIndex || !client.player.getAbilities().creativeMode)
                this.btns[2].active = false;

            for(int i=0; i<btns.length; i++)
                this.children.add(this.btns[i]);
        }

        //list lbl(size) txt
        public NbtWidget(String name, int size, String[] suggestions, Consumer<String> changedListener) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+size, 5, 240-41-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+size};
            this.txts[0].setChangedListener(value -> {
                listUnsaved = true;
                if(!currentTxt.contains(this.txts[0])) {
                    resetSuggs();
                    currentTxt.add(this.txts[0]);
                    suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                    if(suggestions != null && suggestions.length > 0)
                        suggs.setSuggestions(suggestions);
                }
                else{
                    if(suggs != null)
                        suggs.refresh();
                    else {
                        resetSuggs();
                        suggs = new TextSuggestor(client, this.txts[0], textRenderer);
                        if(suggestions != null && suggestions.length > 0)
                            suggs.setSuggestions(suggestions);
                    }
                }
                changedListener.accept(value);
                updateCurrentList();
            });
            this.txts[0].setMaxLength(131072);
            for(int i=0; i<txts.length; i++) {
                this.children.add(this.txts[i]);
                ItemBuilder.this.allTxtWidgets.add(this.txts[i]);
            }
            lbl = name;
            lblCentered = false;
        }

        //PosWidgets
        public NbtWidget(PosWidget[] p) {
            super();
            this.children = Lists.newArrayList();
            setup();

            wids = p;

            for(int i=0; i<p.length; i++) {
                this.children.add(p[i].w);
            }
        }

        private void setup() {
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
                    if(!ItemStack.fromNbt(current).isEmpty()) {
                        this.btns[i].setTooltip(makeItemTooltip(ItemStack.fromNbt(current)));
                        savedStacks[i] = ItemStack.fromNbt(current);
                        if(client.player.getAbilities().creativeMode)
                            this.btns[i].active = true;
                    }
                    else {
                        if((current).contains("id",NbtElement.STRING_TYPE) && 
                                (current).get("id").asString().equals("air")) {
                            this.btns[i].setTooltip(null);
                            savedStacks[i] = new ItemStack(Items.AIR);
                        }
                        else {
                            this.btns[i].setTooltip(makeItemTooltip(current));
                            savedStacks[i] = FortytwoEdit.UNKNOWN_ITEM;
                            if(client.player.getAbilities().creativeMode)
                                this.btns[i].active = true;
                        }
                    }
                }
        }

        public void setSlider(float val) {
            if(this.poseSlider != null)
                this.poseSlider.setVal(val);
        }

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
                    context.drawCenteredTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.width/2, y+6, LABEL_COLOR);
                else
                    context.drawTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.x+15+3, y+6, LABEL_COLOR);
            }
            if(item != null) {
                context.drawItem(item,x+15+2+itemXoff,y+2);
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
                    context.drawItem(this.savedStacks[i],x+this.btnX[i]+2,y+2);
                    if(savedModeSet && !viewBlackMarket && !this.savedStacks[i].getItem().toString().equals("air"))
                        context.drawItem(savedModeItems[2],x+this.btnX[i]+2,y+2);
                }
            else if(savedStacksMode == 0 && this.savedStacks != null && this.savedStacks.length == this.btnX.length)
                for(int i=0; i<this.savedStacks.length; i++)
                    if(this.savedStacks[i] != null && !this.savedStacks[i].isEmpty())
                        context.drawItem(this.savedStacks[i],x+this.btnX[i]+2,y+2);
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
                    context.drawItem(rgbItems[i], x+15+2+i*25, y+2+22);
            }

        }
    }

    class NbtWidgetInvRow extends NbtWidget {
        public NbtWidgetInvRow(int row) {
            super();
    
            this.savedStacksMode = 0;
            if(row >= 0 && row < 4) {
                this.savedStacks = new ItemStack[9];
                this.btns = new ButtonWidget[9];
                this.btnX = new int[9];
            }
            else if(row == 4) {
                this.savedStacks = new ItemStack[5];
                this.btns = new ButtonWidget[5];
                this.btnX = new int[5];
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

                this.children.add(this.btns[i]);
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
    extends SliderWidget {
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
            ItemBuilder.this.unsavedPose = true;

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
            ItemBuilder.this.unsavedPose = true;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    class RgbSlider
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

        public Tab(String lbl, boolean hideTabs) {
            this.pos = -1;
            this.lbl = lbl;
            this.display = new ItemStack(Items.BARRIER);
            this.hideTabs = hideTabs;

            if(lbl == null)
                this.lbl = "";
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
                        context.drawItem(t.display,x-TAB_SIZE+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*t.pos+(TAB_SIZE/2-8));
                    else
                        context.drawItem(t.display,x+240+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(t.pos-LEFT_TABS)+(TAB_SIZE/2-8));
                }
            }

            if(tab == 4 && viewBlackMarket)
                context.drawItem(savedModeItems[1], x+14, y+38);
            else if(tab == 4)
                context.drawItem(savedModeItems[0], x+14, y+38);
            
            if(tab == 5 && editArmorStand)
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)renderArmorStand);
            else if(tab == 6) {
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)renderArmorPose);
            }
            else
                InventoryScreen.drawEntity(context, x + playerX, y + playerY, x + playerX + 100, y + playerY + 100, RENDER_SIZE, 0f, mouseX, mouseY, (LivingEntity)this.client.player);

            context.drawItem(selItem, x+240-20-5+2, y+5+2);
            txtFormat.render(context, mouseX, mouseY, delta);
            if(!this.unsavedTxtWidgets.isEmpty() || unsavedPose || (tab == 12 && listUnsaved))
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Unsaved"), this.width / 2, y-11, 0xFFFFFF);
            if(savedError)
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Failed to read saved items!"), this.width / 2, y-11-10, 0xFF5555);
        }
        if((tab == 9 && jsonPreview != null) || (tab == 10 && jsonPreview2 != null)) {
            if(listCurrentPath.equals("pages")) {
                int i = x - 150 - 1;
                int j = y+7;
                StringVisitable stringVisitable = tab == 9 ? jsonPreview : jsonPreview2;
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
                context.drawCenteredTextWithShadow(this.textRenderer, tab==9 ? jsonPreview : jsonPreview2, this.width / 2, y-14, 0xFFFFFF);
        }
        if(tab == 12 && listCurrentPath.equals("BlockEntityTag/Patterns") && bannerChangePreview != null) {
            if(!bannerShield)
                InventoryScreen.drawEntity(context,x+240,y,x+240+100,y+400,2*RENDER_SIZE,0f,x+240+50,y+200,(LivingEntity)bannerChangePreview);
            else
                InventoryScreen.drawEntity(context,x+240,y,x+240+100,y+200,2*RENDER_SIZE,0f,x+240+50,y+100,(LivingEntity)bannerChangePreview);
        }


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
        suggs = null;
        super.resize(client, width, height);
        cacheItemFull = "";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (suggs != null && suggs.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            if(this.unsavedTxtWidgets.isEmpty() && !activeTxt() && !unsavedPose && !((tab == 9 || tab == 10) && jsonUnsaved)
            && !(tab == 10 && json2Unsaved) && !(tab == 12 && listUnsaved)) {
                if(!pauseSaveScroll && tabWidget != null) {
                    tabScroll[tab] = tabWidget.getScrollAmount();
                }
                this.client.setScreen(null);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if(!pauseSaveScroll && tabWidget != null) {
                tabScroll[tab] = tabWidget.getScrollAmount();
            }
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
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
        suggs = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        updateItem();
        compareItems();
        if(tab == 5 || tab == 6) {
            if(selItem.getItem().toString().equals("armor_stand")) {
                updateArmorStand(selItem.copy());
                editArmorStand = true;
            }
            else if(editArmorStand) {
                resetArmorStand();
                updatePose();
            }
        }
        else if(tab == 7)
            updateInvTab();
        else if(tab == 8)
            updateJsonTab();
        else if(tab == 11)
            updateListTab();
        if(unsel) {
            GuiNavigationPath guiNavigationPath = this.getFocusedPath();
            if (guiNavigationPath != null) {
                guiNavigationPath.setFocused(false);
            }
            unsel = false;
        }
    }

}
