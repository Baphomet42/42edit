package baphomethlabs.fortytwoedit.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemBuilder extends GenericScreen {

    protected boolean unsel = false;
    protected int tab = 0;
    protected final int TAB_OFFSET = 5;
    protected final int TAB_SIZE = 24;
    protected final int TAB_SPACING = 2;
    protected final String[] tabsLbl = {"General","Blocks","Misc","Custom NBT","Saved Items","Entity Data","Banner Maker"};
    protected final ItemStack[] tabsItem = {new ItemStack(Items.GOLDEN_SWORD),new ItemStack(Items.PURPLE_SHULKER_BOX),
        FortytwoEdit.HEAD42,new ItemStack(Items.COMMAND_BLOCK),new ItemStack(Items.JIGSAW),new ItemStack(Items.ENDER_DRAGON_SPAWN_EGG),FortytwoEdit.BANNER42};
    protected final ButtonWidget[] tabs = new ButtonWidget[tabsLbl.length];
    private final int LEFT_TABS = 5;
    protected int playerX;
    protected int playerY;
    protected ItemStack item = null;
    protected ButtonWidget itemBtn;
    private TabWidget tabWidget;
    private final ArrayList<ArrayList<ClickableWidget>> noScrollWidgets = new ArrayList<ArrayList<ClickableWidget>>();
    private final ArrayList<ArrayList<NbtWidget>> widgets = new ArrayList<ArrayList<NbtWidget>>();
    private final Set<ClickableWidget> unsavedTxtWidgets = Sets.newHashSet();
    private final Set<ClickableWidget> allTxtWidgets = Sets.newHashSet();
    public boolean savedModeSet = false;
    private NbtList savedItems = null;
    private boolean savedError = false;
    public final String BANNER_PRESET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789";
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        
        //tabs
        for(int i = 0; i<tabs.length; i++) {
            int tabNum = i;
            if(i<LEFT_TABS)
                tabs[i] = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x-TAB_SIZE,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i,TAB_SIZE,TAB_SIZE).build();
            else
                tabs[i] = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x+240,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(i-LEFT_TABS),TAB_SIZE,TAB_SIZE).build();
            tabs[i].setTooltip(Tooltip.of(Text.of(tabsLbl[i])));
            if(tab==i)
                tabs[i].active = false;
            this.addDrawableChild(tabs[i]);
        }

        //main
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("c*"), button -> this.btnSwapOff(true)).dimensions(width/2 - 50,y+5,20,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("c"), button -> this.btnSwapOff(false)).dimensions(width/2 - 30,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnChangeSlot(true)).dimensions(width/2 - 15,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnChangeSlot(false)).dimensions(width/2,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Q"), button -> this.btnThrow(false)).dimensions(width/2 + 15,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Q*"), button -> this.btnThrow(true)).dimensions(width/2 + 30,y+5,20,20).build());
        playerX = x + 240+40;
        playerY = y + 40;
        itemBtn = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> this.btnCopyNbt()).dimensions(x+240-20-5,y+5,20,20).build());
        updateItem();

        //tabs
        if(widgets.size() == 0)
            createWidgets();
        this.tabWidget = null;
        if(tab != 3) {
            this.tabWidget = new TabWidget(tab);
            this.tabWidget.setRenderBackground(false);
            this.tabWidget.setRenderHorizontalShadows(false);
            this.addDrawableChild(this.tabWidget);
        }
        if(tab == 3) {
            if(noScrollWidgets.get(3).size()>=3) {
                noScrollWidgets.get(3).get(0).setX(x+15-3);
                noScrollWidgets.get(3).get(0).setY(y+35);
                noScrollWidgets.get(3).get(1).setX(x+15-3);
                noScrollWidgets.get(3).get(1).setY(y+35+22*6+1);
                noScrollWidgets.get(3).get(2).setX(x+15-3+5+60);
                noScrollWidgets.get(3).get(2).setY(y+35+22*6+1);
            }
            for(int i=0; i<noScrollWidgets.get(3).size(); i++)
                this.addDrawableChild(noScrollWidgets.get(3).get(i));
        }
        if(tab == 4) {
            if(savedItems == null)
                updateSaved();
            if(noScrollWidgets.get(4).size()>=1) {
                noScrollWidgets.get(4).get(0).setX(x+15-3);
                noScrollWidgets.get(4).get(0).setY(y+35+1);
            }
            for(int i=0; i<noScrollWidgets.get(4).size(); i++)
                this.addDrawableChild(noScrollWidgets.get(4).get(i));
        }

    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnSwapOff(boolean copy) {
        if(client.player.getAbilities().creativeMode) {
            if(!copy) {
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
            else {
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
        tab = i;
        this.resize(this.client,this.width,this.height);
    }

    protected void btnCopyNbt() {
        if(client.player.getMainHandStack() != null && !client.player.getMainHandStack().isEmpty()) {
            String itemData = client.player.getMainHandStack().getItem().toString();
            if(client.player.getMainHandStack().hasNbt())
                itemData += client.player.getMainHandStack().getNbt().asString();
            if(client.player.getMainHandStack().getCount()>1)
                itemData += " " + client.player.getMainHandStack().getCount();
            client.player.sendMessage(Text.of(itemData),false);
            client.keyboard.setClipboard(itemData);
        }
        unsel = true;
    }

    private void updateItem() {
        client.player.playerScreenHandler.sendContentUpdates();
        item = client.player.getMainHandStack().copy();
        if(item==null || item.isEmpty()) {
            itemBtn.active = false;
            itemBtn.setTooltip(null);
        }
        else {
            itemBtn.active = true;            
            itemBtn.setTooltip(makeItemTooltip(item));
        }
    }

    private void createWidgets() {
        widgets.clear();
        unsavedTxtWidgets.clear();
        for(int i=0; i<tabsLbl.length; i++) {
            widgets.add(new ArrayList<NbtWidget>());
            noScrollWidgets.add(new ArrayList<ClickableWidget>());
        }

        int tabNum = 0; int num = 0;
        //general
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("id",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                BlackMagick.setId(inp);
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Count",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                BlackMagick.setCount(inp);
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: {\"text\":\"Mjölnir\"}",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                    BlackMagick.setNbt(null,"display/Name",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"display/Name");
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"BaphomethLabs","Compare Items"},new int[]{100,100},new String[]{null,null},btn -> {
                widgets.get(i).get(j).btn();
                if(!client.player.getMainHandStack().isEmpty()) {
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
            }, btn -> {
                widgets.get(i).get(j).btn();
                if(!client.player.getMainHandStack().isEmpty() && !client.player.getOffHandStack().isEmpty()) {

                    String itemData = client.player.getMainHandStack().getItem().toString();
                    if(client.player.getMainHandStack().hasNbt())
                        itemData += client.player.getMainHandStack().getNbt().asString();

                    String itemData2 = client.player.getOffHandStack().getItem().toString();
                    if(client.player.getOffHandStack().hasNbt())
                        itemData2 += client.player.getOffHandStack().getNbt().asString();

                    if(itemData.equals(itemData2))
                        widgets.get(i).get(j).btns[1].setMessage(Text.of("Compare Items \u2611"));
                    else
                        widgets.get(i).get(j).btns[1].setMessage(Text.of("Compare Items \u2612"));
                }
                else
                    widgets.get(i).get(j).btns[1].setMessage(Text.of("Compare Items"));
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Tools / Adventure"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Unbreakable",80,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"Unbreakable",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"Unbreakable");

            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Custom Model",80,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"CustomModelData",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"CustomModelData");
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget(""));
            num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Block:", 40));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"CanPlaceOn","CanDestroy"},new int[]{80,80},new String[]{null,null},btn -> {
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
            })); num++;
        }

        num = 0; tabNum++;
        //block data
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"BlockState"},new int[]{60,70,69},new String[]{"Key | Value"},btn -> {
                String[] inps = widgets.get(i).get(j).btn();
                NbtElement inpEl;
                if(!inps[1].equals("") && !inps[0].equals("")) {
                    inps[1] = "\""+inps[1]+"\"";
                    inpEl = BlackMagick.elementFromString(inps[1]);
                    BlackMagick.setNbt(null,"BlockStateTag/"+inps[0],inpEl,NbtElement.STRING_TYPE);
                }
                else if(inps[1].equals(""))
                    BlackMagick.removeNbt(null,"BlockStateTag/"+inps[0]);
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Utilities"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Fill Container","Box to Bundle"},new int[]{100,80},new String[]{"Set count for all container items to 64",null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Bee Count [0]"},new int[]{100},new String[]{null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Bundle Item",80,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                inp = inp.replaceAll("[^a-zA-Z_]","");
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
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Item Frames"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Rotation",60,"Item Rotation\n0-7",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"EntityTag/ItemRotation",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/ItemRotation");
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Item","Invis","Fixed"},new int[]{60,60,60},new String[]{"Set display item as offhand",null,null},btn -> {
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
            })); num++;
        }

        num = 0; tabNum++;
        //misc
        {
            widgets.get(tabNum).add(new NbtWidget("Player Heads"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Books"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("generation",60,"0-4\nOriginal/ Copy of original/\nCopy of a copy/ Tattered",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"generation",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"generation");
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Tropical Fish Bucket"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Maps"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("Sounds"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Play",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(!inp.trim().equals("")) {
                    String sound = inp.trim();
                    sound = sound.replaceAll("[^a-zA-Z0-9.]","");
                    if(!sound.equals("")) {
                        client.player.playSound(SoundEvent.of(new Identifier(sound)), SoundCategory.MASTER, 1, 1);
                    }
                }
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Head Sound"},new int[]{80},new String[]{null},btn -> {
                String inp = widgets.get(i).get(j-1).btn()[0];
                if(!inp.trim().equals("")) {
                    String sound = inp.trim();
                    sound = sound.replaceAll("[^a-zA-Z0-9.]","");
                    if(!sound.equals("")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putString("id","player_head");
                        nbt.putInt("Count",1);
                        NbtCompound tag = new NbtCompound();
                        NbtCompound BET = new NbtCompound();
                        BET.putString("note_block_sound",sound);
                        tag.put("BlockEntityTag",BET);
                        NbtList lore = new NbtList();
                        lore.add(NbtString.of("{\"text\":\"Note block sound:\",\"italic\":false,\"color\":\"gray\"}"));
                        lore.add(NbtString.of("{\"text\":\""+sound+"\",\"italic\":false,\"color\":\"gray\"}"));
                        NbtCompound display = new NbtCompound();
                        display.put("Lore",lore);
                        tag.put("display",display);
                        nbt.put("tag",tag);
                        ItemStack item = ItemStack.fromNbt(nbt);
                        if(item != null && client.player.getAbilities().creativeMode) {
                            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                            client.player.playerScreenHandler.sendContentUpdates();
                        }
                    }
                }
            })); num++;
        }

        num = 0; tabNum++;
        //nbt
        {
            EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
            noScrollWidgets.get(tabNum).add(w);
            this.allTxtWidgets.add(w);
            w.setChangeListener(value -> {
                if(value != null && !value.equals("")) {
                    ItemBuilder.this.markUnsaved(w);
                }
                else {
                    ItemBuilder.this.markSaved(w);
                }
            });
            num++;
        }
        {
            final int i = tabNum;
            noScrollWidgets.get(tabNum).add(ButtonWidget.builder(Text.of("Get"), button -> {
                if(!client.player.getMainHandStack().isEmpty()) {
                    String itemData = client.player.getMainHandStack().getItem().toString();
                    if(client.player.getMainHandStack().hasNbt())
                        itemData += client.player.getMainHandStack().getNbt().asString();
                    if(client.player.getMainHandStack().getCount()>1)
                        itemData += " " + client.player.getMainHandStack().getCount();
                    ((EditBoxWidget)noScrollWidgets.get(i).get(0)).setText(itemData);
                    ItemBuilder.this.markUnsaved((EditBoxWidget)noScrollWidgets.get(i).get(0));
                }
                ItemBuilder.this.unsel = true;
            }).dimensions(x+15-3,y+35+22*6+1,60,20).build());
            num++;
        }
        {
            final int i = tabNum;
            noScrollWidgets.get(tabNum).add(ButtonWidget.builder(Text.of("Set"), button -> {
                String inp = ((EditBoxWidget)noScrollWidgets.get(i).get(0)).getText();
                ItemBuilder.this.markSaved((EditBoxWidget)noScrollWidgets.get(i).get(0));
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
            }).dimensions(x+15-3+5+60,y+35+22*6+1,60,20).build());
            num++;
        }

        num = 0; tabNum++;
        //saved items
        {
            noScrollWidgets.get(tabNum).add(CyclingButtonWidget.onOffBuilder(Text.literal("C"), Text.literal("V")).initially(savedModeSet).omitKeyText().build(x+15-3, y+35+1,20,20, Text.of(""), (button, trackOutput) -> {
                savedModeSet = (boolean)trackOutput;
                ItemBuilder.this.unsel = true;
            }));
        }
        for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
            widgets.get(tabNum).add(new NbtWidget(i));

        num = 0; tabNum++;
        //entity data
        {
            widgets.get(tabNum).add(new NbtWidget("Armor Stands"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"BasePlate","ShowArms","Small"},new int[]{60,60,60},new String[]{null,null,null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Marker","Invisible"},new int[]{60,60},new String[]{null,null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Disabled Slots",80,"Disable All - 16191",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString(inp);
                    BlackMagick.setNbt(null,"EntityTag/DisabledSlots",inpEl,NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/DisabledSlots");
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("All Entities"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Entity",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                ItemStack item;
                if(client.player.getMainHandStack().isEmpty())
                    item = BlackMagick.setId("ender_dragon_spawn_egg");
                else
                    item = client.player.getMainHandStack().copy();
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\""+inp+"\"");
                    BlackMagick.setNbt(item,"EntityTag/id",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(item,"EntityTag/id");
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Name",40,"Example: {\"text\":\"Monster\"}",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                NbtElement inpEl;
                if(!inp.equals("")) {
                    inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                    BlackMagick.setNbt(null,"EntityTag/CustomName",inpEl,NbtElement.STRING_TYPE);
                }
                else
                    BlackMagick.removeNbt(null,"EntityTag/CustomName");
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"ShowName","NoGravity","Glowing"},new int[]{60,60,60},new String[]{null,null,null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"VisualFire","Silent","NoAI"},new int[]{60,60,60},new String[]{null,null,null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Invulnerable","Persistence"},new int[]{80,80},new String[]{null,null},btn -> {
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            widgets.get(tabNum).add(new NbtWidget("End Crystals"));
            num++;
        }
        {
            final int i = tabNum; final int j = num;
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
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Show Bottom"},new int[]{80},new String[]{null},btn -> {
                widgets.get(i).get(j).btn();
                if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom")!=null &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom").getType()==NbtElement.BYTE_TYPE &&
                BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ShowBottom").asString().equals("0b")) {
                    BlackMagick.removeNbt(null,"EntityTag/ShowBottom");
                }
                else
                    BlackMagick.setNbt(null,"EntityTag/ShowBottom",NbtByte.of(false));
            })); num++;
        }

        num = 0; tabNum++;
        //banner maker
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget(new String[]{"Preset"},new int[]{40,55,49,55},
                    new String[]{"Char Color | Chars | Base Color"+"\n\n"+BANNER_PRESET_CHARS},btn -> {
                String[] inps = widgets.get(i).get(j).btn();
                CLICK:{
                    int baseColor = 0;
                    int charColor = 0;
                    switch(inps[2].toLowerCase()) {
                        case "white": baseColor=0; break;
                        case "orange": baseColor=1; break;
                        case "magenta": baseColor=2; break;
                        case "light_blue": baseColor=3; break;
                        case "yellow": baseColor=4; break;
                        case "lime": baseColor=5; break;
                        case "pink": baseColor=6; break;
                        case "gray": baseColor=7; break;
                        case "light_gray": baseColor=8; break;
                        case "cyan": baseColor=9; break;
                        case "purple": baseColor=10; break;
                        case "blue": baseColor=11; break;
                        case "brown": baseColor=12; break;
                        case "green": baseColor=13; break;
                        case "red": baseColor=14; break;
                        case "black": baseColor=15; break;
                        default: break CLICK;
                    }
                    switch(inps[0].toLowerCase()) {
                        case "white": charColor=0; break;
                        case "orange": charColor=1; break;
                        case "magenta": charColor=2; break;
                        case "light_blue": charColor=3; break;
                        case "yellow": charColor=4; break;
                        case "lime": charColor=5; break;
                        case "pink": charColor=6; break;
                        case "gray": charColor=7; break;
                        case "light_gray": charColor=8; break;
                        case "cyan": charColor=9; break;
                        case "purple": charColor=10; break;
                        case "blue": charColor=11; break;
                        case "brown": charColor=12; break;
                        case "green": charColor=13; break;
                        case "red": charColor=14; break;
                        case "black": charColor=15; break;
                        default: break CLICK;
                    }
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
            })); num++;
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
        if(nbt.contains("Count",NbtElement.INT_TYPE) && ((NbtInt)nbt.get("Count")).intValue() >1)
            itemData += " " + nbt.get("Count").asString();
        if(nbt.contains("id",NbtElement.STRING_TYPE))
            return Tooltip.of(Text.empty().append("Unknown Item").append(Text.of("\n"+nbt.get("id").asString()+itemData)));
        else return Tooltip.of(Text.of("Failed to read item"));
    }

    private void updateSaved() {
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
        if(widgets.get(4).size() == FortytwoEdit.SAVED_ROWS)
            for(int i=0; i<FortytwoEdit.SAVED_ROWS; i++)
                widgets.get(4).get(i).updateSavedDisplay();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class TabWidget
    extends ElementListWidget<AbstractWidget> {
        public TabWidget(final int tab) {
            super(ItemBuilder.this.client, ItemBuilder.this.width-30, ItemBuilder.this.height, ItemBuilder.this.y+32, ItemBuilder.this.y + ItemBuilder.this.backgroundHeight - 5, tab == 4 ? 20 : 22);
            
            for(int i=0; i<widgets.get(tab).size(); i++)
                this.addEntry((AbstractWidget)ItemBuilder.this.widgets.get(tab).get(i));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            AbstractWidget abstractRuleWidget = (AbstractWidget)this.getHoveredEntry();
            if (abstractRuleWidget != null && abstractRuleWidget.description != null) {
                ItemBuilder.this.setTooltip(abstractRuleWidget.description);
            }
        }
    }

    private class NbtWidget
    extends AbstractWidget {

        protected final List<ClickableWidget> children;
        private ButtonWidget[] btns;
        private int[] btnX;
        private TextFieldWidget[] txts;
        private int[] txtX;
        private String lbl;
        private boolean lblCentered;
        private ItemStack[] savedStacks;
        private int savedRow = -1;

        //btn(size) txt
        public NbtWidget(String name, int size, String tooltip, PressAction onPress) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.btns = new ButtonWidget[]{ButtonWidget.builder(Text.of(name), onPress).dimensions(ItemBuilder.this.x+15,5,size,20).build()};
            this.btnX = new int[]{15};
            if(tooltip != null)
                this.btns[0].setTooltip(Tooltip.of(Text.of(tooltip)));
            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+1+size, 5, 240-41-2-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+1+size};
            this.txts[0].setChangedListener(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    ItemBuilder.this.markSaved(this.txts[0]);
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

        //lbl(size) txt
        public NbtWidget(String name, int size) {
            super();
            this.children = Lists.newArrayList();
            setup();

            this.txts = new TextFieldWidget[]{new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15+5+1+size, 5, 240-41-2-size, 20, Text.of(""))};
            this.txtX = new int[]{15+5+1+size};
            this.txts[0].setChangedListener(value -> {
                if(value != null && !value.equals("")) {
                    this.txts[0].setEditableColor(0xFFFFFF);
                    ItemBuilder.this.markUnsaved(this.txts[0]);
                }
                else {
                    this.txts[0].setEditableColor(LABEL_COLOR);
                    ItemBuilder.this.markSaved(this.txts[0]);
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
        public NbtWidget(String[] names, int[] sizes, String[] tooltips, PressAction... onPressActions) {
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
                    this.btns[i] = ButtonWidget.builder(Text.of(names[i]), onPressActions[i]).dimensions(currentX,5,sizes[i],20).build();
                    currentX += 5 + sizes[i];
                    if(tooltips[i] != null)
                        this.btns[i].setTooltip(Tooltip.of(Text.of(tooltips[i])));

                    this.children.add(this.btns[i]);
                }
                for(int i=0; i<this.txts.length; i++) {
                    this.txtX[i] = currentX+1;
                    this.txts[i] = new TextFieldWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, currentX+1, 5, sizes[this.btns.length+i]-2, 20, Text.of(""));
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
            this.btns = new ButtonWidget[9];
            this.btnX = new int[9];
            int currentX = 15+30;
            for(int i=0; i<9; i++) {
                this.btnX[i] = currentX;
                final int index = row*9+i;
                this.btns[i] = ButtonWidget.builder(Text.of(""), btn -> {
                    ItemBuilder.this.unsel = true;
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
                        updateSaved();
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
                }).dimensions(currentX,5,20,20).build();
                currentX += 20;
                this.btns[i].active = false;

                this.children.add(this.btns[i]);
            }
        }

        private void setup() {
            btns = new ButtonWidget[0];
            btnX = new int[0];
            txts = new TextFieldWidget[0];
            txtX = new int[0];
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
            for(int i=0; i<9; i++) {
                if(!ItemStack.fromNbt((NbtCompound)(savedItems.get(savedRow*9+i))).isEmpty()) {
                    this.btns[i].setTooltip(makeItemTooltip(ItemStack.fromNbt((NbtCompound)(savedItems.get(savedRow*9+i)))));
                    savedStacks[i] = ItemStack.fromNbt((NbtCompound)(savedItems.get(savedRow*9+i)));
                }
                else {
                    if(((NbtCompound)(savedItems.get(savedRow*9+i))).contains("id",NbtElement.STRING_TYPE) && 
                            ((NbtCompound)(savedItems.get(savedRow*9+i))).get("id").asString().equals("air")) {
                        this.btns[i].setTooltip(null);
                        savedStacks[i] = new ItemStack(Items.AIR);
                    }
                    else {
                        this.btns[i].setTooltip(makeItemTooltip((NbtCompound)(savedItems.get(savedRow*9+i))));
                        savedStacks[i] = FortytwoEdit.UNKNOWN_ITEM;
                    }
                }
                this.btns[i].active = true;
            }
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
                this.btns[i].setY(y);
                this.btns[i].render(context, mouseX, mouseY, tickDelta);
            }
            for(int i=0; i<txts.length; i++) {
                this.txts[i].setX(x+this.txtX[i]);
                this.txts[i].setY(y);
                this.txts[i].render(context, mouseX, mouseY, tickDelta);
            }
            if(lbl != null) {
                if(lblCentered)
                    context.drawCenteredTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.width/2, y+6, LABEL_COLOR);
                else
                    context.drawTextWithShadow(ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.x+15+3, y+6, LABEL_COLOR);
            }
            if(this.savedStacks != null && this.savedStacks.length == 9)
                for(int i=0; i<9; i++)
                    context.drawItem(this.savedStacks[i],x+this.btnX[i]+2,y+2);
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
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.drawBackground(context, delta, mouseX, mouseY, 1);
        for(int i=0; i<tabsItem.length; i++) {
            if(i<LEFT_TABS)
                context.drawItem(tabsItem[i],x-TAB_SIZE+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i+(TAB_SIZE/2-8));
            else
                context.drawItem(tabsItem[i],x+240+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(i-LEFT_TABS)+(TAB_SIZE/2-8));
        }
        InventoryScreen.drawEntity(context, playerX, playerY, 30, (float)(playerX) - mouseX, (float)(playerY - 50) - mouseY, (LivingEntity)this.client.player);
        context.drawItem(item, x+240-20-5+2, y+5+2);
        if(!this.unsavedTxtWidgets.isEmpty())
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Unsaved"), this.width / 2, y-11, 0xFFFFFF);
        if(savedError)
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Failed to read saved items!"), this.width / 2, y-11-10, 0xFF5555);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(FortytwoEdit.magickGuiKey).getCode() || keyCode == KeyBindingHelper.getBoundKeyOf(client.options.inventoryKey).getCode()) {
            if(this.unsavedTxtWidgets.isEmpty() && !activeTxt()) {
                this.client.setScreen(null);
                return true;
            }
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        updateItem();
        if(unsel) {
            GuiNavigationPath guiNavigationPath = this.getFocusedPath();
            if (guiNavigationPath != null) {
                guiNavigationPath.setFocused(false);
            }
            unsel = false;
        }
    }

}
