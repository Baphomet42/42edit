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
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
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
    protected int playerX;
    protected int playerY;
    protected ItemStack item = null;
    protected ButtonWidget itemBtn;
    private TabWidget tabWidget;
    private final ArrayList<ArrayList<ClickableWidget>> noScrollWidgets = new ArrayList<ArrayList<ClickableWidget>>();
    private final ArrayList<ArrayList<NbtWidget>> widgets = new ArrayList<ArrayList<NbtWidget>>();
    private final Set<ClickableWidget> unsavedTxtWidgets = Sets.newHashSet();
    private final Set<ClickableWidget> allTxtWidgets = Sets.newHashSet();
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        
        //tabs
        for(int i = 0; i<tabs.length; i++) {
            int tabNum = i;
            if(i<5)
                tabs[i] = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x-TAB_SIZE,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i,TAB_SIZE,TAB_SIZE).build();
            else
                tabs[i] = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x+240,y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(i-5),TAB_SIZE,TAB_SIZE).build();
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
        for(int i=0; i<noScrollWidgets.get(tab).size(); i++)
            this.addDrawableChild(noScrollWidgets.get(tab).get(i));

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
            itemBtn.setTooltip(Tooltip.of(Text.of("")));
        }
        else {
            itemBtn.active = true;

            String itemData = "";
            if(item.hasNbt()) {
                itemData += item.getNbt().asString();
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
            
            itemBtn.setTooltip(Tooltip.of(Text.of(item.getItem().toString()+itemData)));
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
        //TODO btn [BaphomethLabs]
        //TODO btn [compare items]
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
        //TODO btn btn [CanPlace/Break]

        num = 0; tabNum++;
        //block data
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Name",40,null,btn -> {
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
            widgets.get(tabNum).add(new NbtWidget("Lock",40,null,btn -> {
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
        //TODO btn txt txt (tooltip) [BlockState]
        {
            widgets.get(tabNum).add(new NbtWidget("Utilities"));
            num++;
        }
        //TODO btn [fill container stacks]
        //TODO btn [container to bundle]
        //TODO btn lblcount [bee count]
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
        //TODO btn(tooltip) btn btn [item/invis/fixed]
        //TODO btn [drop]
        {
            widgets.get(tabNum).add(new NbtWidget("End Crystals"));
            num++;
        }
        //TODO standard btntxt (tooltip) for target pos
        //TODO showbase

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
            widgets.get(tabNum).add(new NbtWidget("Name",40,null,btn -> {
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
                            BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value))
                            .getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                        }
                        else {
                            item = client.player.getMainHandStack().copy();
                            BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value))
                            .getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                        }
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                    }
                }
                else if(id.equals("")) {
                    BlackMagick.removeNbt(null,"SkullOwner/Id");
                    BlackMagick.removeNbt(null,"SkullOwner/Properties");
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
                    BlackMagick.setNbt(null,"title",inpEl,NbtElement.STRING_TYPE);
                    BlackMagick.setNbt(null,"filtered_title",inpEl,NbtElement.STRING_TYPE);
                }
                else {
                    BlackMagick.removeNbt(null,"title");
                    BlackMagick.removeNbt(null,"filtered_title");
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
            widgets.get(tabNum).add(new NbtWidget("Variant",60,"Size, Pattern, Base Color, Pattern Color" + "\n" +
                    "\nSize-   0-Small   1-Large" + "\n" +
                    "\nPattern-   0-Kob/Flopper   1-Sunstreak/Stripey   2-Snooper/Glitter   3-Dasher/Blockfish   4-Brinely/Betty   5-Spotty/Clayfish" + "\n" +
                    "\nColor-   0-White   1-Orange   2-Magenta   3-Light Blue   4-Yellow   5-Lime   6-Pink   7-Gray" +
                    "   8-Light Gray   9-Cyan   10-Purple   11-Blue   12-Brown   13-Green   14-Red   15-Black",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                CLICK: {
                    if(inp.equals("")) {
                        BlackMagick.removeNbt(null,"BucketVariantTag");
                        break CLICK;
                    }
                    //
                    int[] vars = {0,0,0,0};
                    inp = inp+",X";
                    try {
                        for(int ii=0; ii<4; ii++) {
                            if(!inp.contains(","))
                                break CLICK;
                            if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                vars[ii]=Integer.parseInt(inp.substring(0,inp.indexOf(",")));
                            inp = inp.substring(inp.indexOf(",")+1);
                        }
                    } catch(NumberFormatException e) {
                        break CLICK;
                    }
                    //
                    if( (vars[0]<0 || vars[0]>1) || (vars[1]<0 || vars[1]>5)
                    || (vars[2]<0 || vars[2]>15) || (vars[3]<0 || vars[3]>15) )
                        break CLICK;
                    int val = vars[0] +(((int)Math.pow(2,8))*vars[1])+(((int)Math.pow(2,16))*vars[2])+(((int)Math.pow(2,24))*vars[3]);
                    if(client.player.getMainHandStack().isEmpty()) {
                        BlackMagick.setNbt(BlackMagick.setId("tropical_fish_bucket"),"BucketVariantTag",
                            BlackMagick.elementFromString(""+val),NbtElement.NUMBER_TYPE);
                    }
                    else
                        BlackMagick.setNbt(null,"BucketVariantTag",BlackMagick.elementFromString(""+val),NbtElement.NUMBER_TYPE);
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
            widgets.get(tabNum).add(new NbtWidget("Decor",40,"decor index, id (Name), type (0-26), x, z, rot (180 is upright)" + "\n" +
                    "\ntype" +
                    "\n0- player marker  1- frame marker  2- red marker" +
                    "\n3- blue marker  4- white x  5- red triangle" +
                    "\n6- large white dot  7- small white dot  8- mansion" +
                    "\n9- monument 10-25 banners  26- red x",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"Decorations");
                else {
                    String[] inps = new String[6];
                    inp = inp+",,,,,,,";
                    for(int ii=0; ii<6; ii++) {
                        inps[ii]=inp.substring(0,inp.indexOf(","));
                        inp = inp.substring(inp.indexOf(",")+1);
                    }
                    if(!inps[0].equals("") && inps[1].equals("") && inps[2].equals("") && inps[3].equals("")
                    && inps[4].equals("") && inps[5].equals(""))
                        BlackMagick.removeNbt(null,"Decorations/"+inps[0]+":");
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
                        BlackMagick.setNbt(null,"Decorations/"+ii+":/id",NbtString.of(inps[1]));
                        BlackMagick.setNbt(null,"Decorations/"+ii+":/type",NbtByte.of(t));
                        BlackMagick.setNbt(null,"Decorations/"+ii+":/x",NbtDouble.of(x));
                        BlackMagick.setNbt(null,"Decorations/"+ii+":/z",NbtDouble.of(z));
                        BlackMagick.setNbt(null,"Decorations/"+ii+":/rot",NbtDouble.of(r));
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
        //TODO btn [headsound]

        num = 0; tabNum++;
        //nbt
        {
            EditBoxWidget w = new EditBoxWidget(((ItemBuilder)ItemBuilder.this).client.textRenderer, ItemBuilder.this.x+15-3, y+35, 240-36, 22*6, Text.of(""), Text.of(""));
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
        //TODO

        num = 0; tabNum++;
        //entity data
        {
            widgets.get(tabNum).add(new NbtWidget("Armor Stands"));
            num++;
        }
        //TODO btn btn btn [base/arms/small]
        //TODO btn btn [marker/invis]
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
            widgets.get(tabNum).add(new NbtWidget("Name",40,null,btn -> {
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
        //TODO btn btn btn [showname/nograv/glowing]
        //TODO btn btn btn [visfire/silent/noai]
        //TODO btn btn [invul/persistence]
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Items",40,"0 Mainhand    1 Offhand" + "\n2 Boots    3 Leggings" + "\n4 Chestplate    5 Helmet" +
                    "\n" + "\n6 Armor    7 Armor/Mainhand" + "\n" + "\n8 Remove Armor    9 Remove Hand",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
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
                        }
                        break;
                    case "7":
                        {//mainhand
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
                        }
                        {//boots
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
                        }
                        break;
                    case "8":
                        BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                        break;
                    case "9":
                        BlackMagick.removeNbt(null,"EntityTag/HandItems");
                        break;
                    case "":
                        BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                        BlackMagick.removeNbt(null,"EntityTag/HandItems");
                        break;
                    default: break;
                }
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Pos",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                CLICK:{
                    if(inp.equals(""))
                        BlackMagick.removeNbt(null,"EntityTag/Pos");
                    else {
                        double[] pos = {0d,0d,0d};
                        if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")!=null
                        && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos").getType()==NbtElement.LIST_TYPE &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).size()==3 &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).getHeldType()==NbtElement.DOUBLE_TYPE)
                            for(int ii=0; ii<3; ii++)
                                pos[ii]=Double.parseDouble(
                                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).get(ii).asString());
                        inp = inp.replace(" ","");
                        inp = inp.replace("d","");
                        inp = inp.replace("D","");
                        inp = inp+",X";
                        try {
                            for(int ii=0; ii<3; ii++) {
                                if(!inp.contains(","))
                                    break CLICK;
                                if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                    pos[ii]=Double.parseDouble(inp.substring(0,inp.indexOf(",")));
                                inp = inp.substring(inp.indexOf(",")+1);
                            }
                        } catch(NumberFormatException e) {
                            break CLICK;
                        }
                        for(int ii=0; ii<3; ii++)
                            BlackMagick.setNbt(null,"EntityTag/Pos/"+ii+":",NbtDouble.of(pos[ii]));
                    }
                }
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Motion",40,null,btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                CLICK:{
                    if(inp.equals(""))
                        BlackMagick.removeNbt(null,"EntityTag/Motion");
                    else {
                        double[] mot = {0d,0d,0d};
                        if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")!=null
                        && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion").getType()==NbtElement.LIST_TYPE &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).size()==3 &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).getHeldType()==NbtElement.DOUBLE_TYPE)
                            for(int ii=0; ii<3; ii++)
                                mot[ii]=Double.parseDouble(
                                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).get(ii).asString());
                        inp = inp.replace(" ","");
                        inp = inp.replace("d","");
                        inp = inp.replace("D","");
                        inp = inp+",X";
                        try {
                            for(int ii=0; ii<3; ii++) {
                                if(!inp.contains(","))
                                    break CLICK;
                                if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                    mot[ii]=Double.parseDouble(inp.substring(0,inp.indexOf(",")));
                                inp = inp.substring(inp.indexOf(",")+1);
                            }
                        } catch(NumberFormatException e) {
                            break CLICK;
                        }
                        for(int ii=0; ii<3; ii++)
                            BlackMagick.setNbt(null,"EntityTag/Motion/"+ii+":",NbtDouble.of(mot[ii]));
                    }
                }
            })); num++;
        }
        {
            final int i = tabNum; final int j = num;
            widgets.get(tabNum).add(new NbtWidget("Rotation",60,"(Yaw,Pitch)" + "\n" + "\nYaw 0 - 360 (South=0,West=90)" + "\nPitch -90 - 90 (Down=90)",btn -> {
                String inp = widgets.get(i).get(j).btn()[0];
                CLICK:{
                    if(inp.equals(""))
                        BlackMagick.removeNbt(null,"EntityTag/Rotation");
                    else {
                        float[] rot = {0f,0f};
                        if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")!=null
                        && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation").getType()==NbtElement.LIST_TYPE &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).size()==2 &&
                        ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).getHeldType()==NbtElement.FLOAT_TYPE)
                            for(int ii=0; ii<2; ii++)
                                rot[ii]=Float.parseFloat(
                                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).get(ii).asString());
                        inp = inp.replace(" ","");
                        inp = inp.replace("f","");
                        inp = inp.replace("F","");
                        inp = inp+",X";
                        try {
                            for(int ii=0; ii<2; ii++) {
                                if(!inp.contains(","))
                                    break CLICK;
                                if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                    rot[ii]=Float.parseFloat(inp.substring(0,inp.indexOf(",")));
                                inp = inp.substring(inp.indexOf(",")+1);
                            }
                        } catch(NumberFormatException e) {
                            break CLICK;
                        }
                        for(int ii=0; ii<2; ii++)
                            BlackMagick.setNbt(null,"EntityTag/Rotation/"+ii+":",NbtFloat.of(rot[ii]));
                    }
                }
            })); num++;
        }

        num = 0; tabNum++;
        //banner maker
        //TODO btn txt txt txt (tooltip) [preset]
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
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class TabWidget
    extends ElementListWidget<AbstractWidget> {
        public TabWidget(final int tab) {
            super(ItemBuilder.this.client, ItemBuilder.this.width-30, ItemBuilder.this.height, ItemBuilder.this.y+32, ItemBuilder.this.height - 34, 22);
            
            for(int i=0; i<widgets.get(tab).size(); i++)
                this.addEntry((AbstractWidget)ItemBuilder.this.widgets.get(tab).get(i));
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
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

        @Override
        public List<? extends Element> children() {
            return this.children;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.children;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            for(int i=0; i<btns.length; i++) {
                this.btns[i].setX(x+this.btnX[i]);
                this.btns[i].setY(y);
                this.btns[i].render(matrices, mouseX, mouseY, tickDelta);
            }
            for(int i=0; i<txts.length; i++) {
                this.txts[i].setX(x+this.txtX[i]);
                this.txts[i].setY(y);
                this.txts[i].render(matrices, mouseX, mouseY, tickDelta);
            }
            if(lbl != null) {
                if(lblCentered)
                    drawCenteredTextWithShadow(matrices, ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.width/2, y+6, LABEL_COLOR);
                else
                    drawTextWithShadow(matrices, ItemBuilder.this.textRenderer, Text.of(this.lbl), ItemBuilder.this.x+15+3, y+6, LABEL_COLOR);
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
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY, 1);
        for(int i=0; i<tabsItem.length; i++) {
            if(i<5)
                this.itemRenderer.renderInGui(matrices, tabsItem[i],x-TAB_SIZE+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i+(TAB_SIZE/2-8));
            else
                this.itemRenderer.renderInGui(matrices, tabsItem[i],x+240+(TAB_SIZE/2-8),y+30+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*(i-5)+(TAB_SIZE/2-8));
        }
        InventoryScreen.drawEntity(matrices, playerX, playerY, 30, (float)(playerX) - mouseX, (float)(playerY - 50) - mouseY, (LivingEntity)this.client.player);
        this.itemRenderer.renderInGui(matrices, item, x+240-20-5+2, y+5+2);
        if(!this.unsavedTxtWidgets.isEmpty())
            drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of("Unsaved"), this.width / 2, y-11, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
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
